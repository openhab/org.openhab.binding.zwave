/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Version command class. The Version Command Class is used to obtain the library type, the protocol version
 * used by the node, the individual command class versions used by the node and the vendor specific application version
 * from a device.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("COMMAND_CLASS_VERSION")
public class ZWaveVersionCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveVersionCommandClass.class);
    private static final int MAX_SUPPORTED_VERSION = 2;

    public static final int VERSION_GET = 0x11;
    public static final int VERSION_REPORT = 0x12;
    public static final int VERSION_COMMAND_CLASS_GET = 0x13;
    public static final int VERSION_COMMAND_CLASS_REPORT = 0x14;

    private LibraryType libraryType = LibraryType.LIB_UNKNOWN;
    private String protocolVersion;
    private String applicationVersion;
    private Integer hardwareVersion;

    /**
     * Creates a new instance of the ZWaveVersionCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveVersionCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_VERSION;
    }

    @ZWaveResponseHandler(id = VERSION_REPORT, name = "VERSION_REPORT")
    public void handleVersionReport(ZWaveCommandClassPayload payload, int endpoint) {
        logger.debug("NODE {}: Process Version Report", getNode().getNodeId());
        libraryType = LibraryType.getLibraryType(payload.getPayloadByte(2));
        protocolVersion = Integer.toString(payload.getPayloadByte(3)) + "."
                + Integer.toString(payload.getPayloadByte(4));
        applicationVersion = Integer.toString(payload.getPayloadByte(5)) + "."
                + Integer.toString(payload.getPayloadByte(6));

        logger.debug("NODE {}: Library Type        = {} ({})", getNode().getNodeId(), libraryType.key,
                libraryType.label);
        logger.debug("NODE {}: Protocol Version    = {}", getNode().getNodeId(), protocolVersion);
        logger.debug("NODE {}: Application Version = {}", getNode().getNodeId(), applicationVersion);

        if (payload.getPayloadLength() > 8) {
            // Version 2 includes hardware version, as well as additional versions for other processors
            // The version 1 and version 2 requests are the same. We differentiate using the length since we may
            // not have request the version of the VERSION command class at this point.
            hardwareVersion = payload.getPayloadByte(7);
            logger.debug("NODE {}: Hardware Version     = {}", getNode().getNodeId(), hardwareVersion);
        }
    }

    @ZWaveResponseHandler(id = VERSION_COMMAND_CLASS_REPORT, name = "VERSION_COMMAND_CLASS_REPORT")
    public void handleVersionCommandClassReport(ZWaveCommandClassPayload payload, int endpoint) {
        logger.debug("NODE {}: Process Version Command Class Report", getNode().getNodeId());
        int commandClassCode = payload.getPayloadByte(2);
        int commandClassVersion = payload.getPayloadByte(3);

        CommandClass commandClass = CommandClass.getCommandClass(commandClassCode);
        if (commandClass == null) {
            logger.error("NODE {}: Unsupported command class 0x{}", getNode().getNodeId(),
                    Integer.toHexString(commandClassCode));
            return;
        }

        logger.debug("NODE {}: Requested Command Class = {}, Version = {}", getNode().getNodeId(), commandClass,
                commandClassVersion);

        // The version is set on the command class for this node. By updating the version, extra functionality
        // is unlocked in the command class.
        // The messages are backwards compatible, so it's not a problem that there is a slight delay when the
        // command class version is queried on the node.
        ZWaveCommandClass zwaveCommandClass = getNode().getCommandClass(commandClass);
        if (zwaveCommandClass == null) {
            logger.error("NODE {}: Unsupported command class {} (0x{})", getNode().getNodeId(), commandClass,
                    Integer.toHexString(commandClassCode));
            return;
        }

        // If the device reports version 0, it means it doesn't support this command class!
        if (commandClassVersion == 0) {
            logger.info("NODE {}: Command Class {} has version 0!", getNode().getNodeId(), commandClass);

            // Remove other command classes.
            if (commandClassVersion == 0) {
                getNode().removeCommandClass(commandClass);
                logger.debug("NODE {}: Removed Command Class {}", getNode().getNodeId(), commandClass);
                return;
            }
        }

        zwaveCommandClass.setVersion(commandClassVersion);
    }

    /**
     * Gets a SerialMessage with the VERSION_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getVersionMessage() {
        logger.debug("NODE {}: Creating new message for command VERSION_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), VERSION_GET)
                .withPriority(TransactionPriority.Config).withExpectedResponseCommand(VERSION_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the VERSION COMMAND CLASS GET command.
     * This version is used to differentiate between multiple versions of a command
     * and to enable extra functionality.
     *
     * @param commandClass The command class to get the version for.
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getCommandClassVersionMessage(CommandClass commandClass) {
        logger.debug("NODE {}: Creating new message for application command VERSION_COMMAND_CLASS_GET command class {}",
                this.getNode().getNodeId(), commandClass);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                VERSION_COMMAND_CLASS_GET).withPayload(commandClass.getKey()).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(VERSION_COMMAND_CLASS_REPORT).build();
    }

    /**
     * Check the version of a command class by sending a VERSION_COMMAND_CLASS_GET message to the node.
     *
     * @param commandClass the command class to check the version for.
     * @return serial message to be sent
     */
    public ZWaveCommandClassTransactionPayload checkVersion(ZWaveCommandClass commandClass) {
        ZWaveVersionCommandClass versionCommandClass = (ZWaveVersionCommandClass) this.getNode()
                .getCommandClass(CommandClass.COMMAND_CLASS_VERSION);

        if (versionCommandClass == null) {
            logger.debug(
                    "NODE {}: Version command class not supported,"
                            + "reverting to version 1 for command class {} (0x{})",
                    getNode().getNodeId(), commandClass.getCommandClass(),
                    Integer.toHexString(commandClass.getCommandClass().getKey()));
            return null;
        }

        return versionCommandClass.getCommandClassVersionMessage(commandClass.getCommandClass());
    };

    /**
     * Returns the current ZWave library type
     */
    public LibraryType getLibraryType() {
        return libraryType;
    }

    /**
     * Returns the version of the protocol used by the device
     *
     * @return Protocol version as double (version . subversion)
     */
    public String getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Returns the version of the firmware used by the device
     *
     * @return Application version as double (version . subversion)
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }

    public enum LibraryType {
        LIB_UNKNOWN(0, "Unknown"),
        LIB_CONTROLLER_STATIC(1, "Static Controller"),
        LIB_CONTROLLER(2, "Controller"),
        LIB_SLAVE_ENHANCED(3, "Slave Enhanced"),
        LIB_SLAVE(4, "Slave"),
        LIB_INSTALLER(5, "Installer"),
        LIB_SLAVE_ROUTING(6, "Routing Slave"),
        LIB_CONTROLLER_BRIDGE(7, "Bridge Controller"),
        LIB_TEST(8, "Test"),
        LIB_AV_REMOTE(10, "AV Remote"),
        LIB_AV_DEVICE(11, "AV Device");

        /**
         * A mapping between the integer code and its corresponding Library type
         * to facilitate lookup by code.
         */
        private static Map<Integer, LibraryType> libraryMapping;

        private int key;
        private String label;

        private LibraryType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            libraryMapping = new HashMap<Integer, LibraryType>();
            for (LibraryType s : values()) {
                libraryMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the sensor type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the sensor type.
         */
        public static LibraryType getLibraryType(int i) {
            if (libraryMapping == null) {
                initMapping();
            }

            if (libraryMapping.get(i) == null) {
                return LIB_UNKNOWN;
            }

            return libraryMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }
}
