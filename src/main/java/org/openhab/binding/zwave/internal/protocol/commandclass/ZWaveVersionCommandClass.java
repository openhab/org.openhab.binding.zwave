/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Version command class. The Version Command Class is used to obtain the library type, the protocol version
 * used by the node, the individual command class versions used by the node and the vendor specific application version
 * New fields omitted and applicationVersion overwritten for backward compatibility.
 * 
 * @author Chris Jackson - Initial contribution
 * @author Jan-Willem Spuij - Contributor
 * @author Robert Eckhoff - Upgrade to version 3
 */
@XStreamAlias("COMMAND_CLASS_VERSION")
public class ZWaveVersionCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveVersionCommandClass.class);
    private static final int MAX_SUPPORTED_VERSION = 3;

    public static final int VERSION_GET = 0x11;
    public static final int VERSION_REPORT = 0x12;
    public static final int VERSION_COMMAND_CLASS_GET = 0x13;
    public static final int VERSION_COMMAND_CLASS_REPORT = 0x14;
    public static final int VERSION_CAPABILITIES_GET = 0x15;
    public static final int VERSION_CAPABILITIES_REPORT = 0x16;
    public static final int VERSION_ZWAVE_SOFTWARE_GET = 0x17;
    public static final int VERSION_ZWAVE_SOFTWARE_REPORT = 0x18;

    private LibraryType libraryType = LibraryType.LIB_UNKNOWN;
    private String protocolVersion;
    private String applicationVersion;
    private Integer hardwareVersion;
    @XStreamOmitField
    private boolean supportsZWaveSoftwareVersion = false;
    @XStreamOmitField
    private String sdkVersion;
    @XStreamOmitField
    private String applicationFrameworkAPIVersion;
    @XStreamOmitField
    private Integer applicationFrameworkBuild;
    @XStreamOmitField
    private String hostInterfaceVersion;
    @XStreamOmitField
    private Integer hostInterfaceBuild;
    @XStreamOmitField
    private String zWaveProtocolVersion;
    @XStreamOmitField
    private Integer zWaveProtocolBuild;
    @XStreamOmitField
    private String applicationVersionLong;
    @XStreamOmitField
    private String applicationVersionLongShort;
    @XStreamOmitField
    private Integer applicationVersionBuild;

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
    public void initialise(@Nullable ZWaveNode node, @Nullable ZWaveController controller,
            @Nullable ZWaveEndpoint endpoint) {
        super.initialise(node, controller, endpoint);
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
            // Check if Version 3
            getNode().sendMessage(getVersionCapabilitiesMessage());
        }

        getController().notifyEventListeners(new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                CommandClass.COMMAND_CLASS_VERSION, applicationVersion));
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

    @ZWaveResponseHandler(id = VERSION_CAPABILITIES_REPORT, name = "VERSION_CAPABILITIES_REPORT")
    public void handleVersionCapabilitiesReport(ZWaveCommandClassPayload payload, int endpoint) {
        logger.debug("NODE {}: Processing VERSION_CAPABILITIES_REPORT", getNode().getNodeId());
        // The Version Capabilities Report uses a bitmask. The Z-Wave software version capability is indicated by
        // the low 3 bits being set (0b00000111), not by the entire byte equaling 7.
        supportsZWaveSoftwareVersion = (payload.getPayloadByte(2) & 0x07) == 0x07;
        if (supportsZWaveSoftwareVersion) {
            setVersion(MAX_SUPPORTED_VERSION);
            logger.debug("NODE {}: Z-Wave Software Version is supported", getNode().getNodeId());
            getNode().sendMessage(getVersionZWaveSoftwareMessage());
        }
    }

    @ZWaveResponseHandler(id = VERSION_ZWAVE_SOFTWARE_REPORT, name = "VERSION_ZWAVE_SOFTWARE_REPORT")
    public void handleVersionZWaveSoftwareReport(ZWaveCommandClassPayload payload, int endpoint) {
        logger.debug("NODE {}: Processing VERSION_ZWAVE_SOFTWARE_REPORT", getNode().getNodeId());
        sdkVersion = Integer.toString(payload.getPayloadByte(2)) + "." + Integer.toString(payload.getPayloadByte(3))
                + "." + Integer.toString(payload.getPayloadByte(4));
        logger.debug("NODE {}: SDK Version                  = {}", getNode().getNodeId(), sdkVersion);
        applicationFrameworkAPIVersion = Integer.toString(payload.getPayloadByte(5)) + "."
                + Integer.toString(payload.getPayloadByte(6)) + "." + Integer.toString(payload.getPayloadByte(7));
        logger.debug("NODE {}: Application Framework API Version = {}", getNode().getNodeId(),
                applicationFrameworkAPIVersion);
        applicationFrameworkBuild = ((payload.getPayloadByte(8) & 0xFF) << 8) | (payload.getPayloadByte(9) & 0xFF);
        logger.debug("NODE {}: Application Framework Build     = {}", getNode().getNodeId(), applicationFrameworkBuild);
        hostInterfaceVersion = Integer.toString(payload.getPayloadByte(10)) + "."
                + Integer.toString(payload.getPayloadByte(11)) + "." + Integer.toString(payload.getPayloadByte(12));
        logger.debug("NODE {}: Host Interface Version          = {}", getNode().getNodeId(), hostInterfaceVersion);
        hostInterfaceBuild = ((payload.getPayloadByte(13) & 0xFF) << 8) | (payload.getPayloadByte(14) & 0xFF);
        logger.debug("NODE {}: Host Interface Build           = {}", getNode().getNodeId(), hostInterfaceBuild);
        zWaveProtocolVersion = Integer.toString(payload.getPayloadByte(15)) + "."
                + Integer.toString(payload.getPayloadByte(16)) + "." + Integer.toString(payload.getPayloadByte(17));
        logger.debug("NODE {}: Z-Wave Protocol Version        = {}", getNode().getNodeId(), zWaveProtocolVersion);
        zWaveProtocolBuild = ((payload.getPayloadByte(18) & 0xFF) << 8) | (payload.getPayloadByte(19) & 0xFF);
        logger.debug("NODE {}: Z-Wave Protocol Build           = {}", getNode().getNodeId(), zWaveProtocolBuild);
        applicationVersionLong = Integer.valueOf(payload.getPayloadByte(20)) + "."
                + Integer.valueOf(payload.getPayloadByte(21)) + "." + Integer.valueOf(payload.getPayloadByte(22));
        applicationVersionLongShort = Integer.valueOf(payload.getPayloadByte(20)) + "."
                + Integer.valueOf(payload.getPayloadByte(21));
        logger.debug("NODE {}: Application Version Long        = {}", getNode().getNodeId(), applicationVersionLong);
        applicationVersionBuild = ((payload.getPayloadByte(23) & 0xFF) << 8) | (payload.getPayloadByte(24) & 0xFF);
        logger.debug("NODE {}: Application Version Build       = {}", getNode().getNodeId(), applicationVersionBuild);
        // Override the application version with the long-form version if supported and applicable
        if (supportsZWaveSoftwareVersion && applicationVersionLongShort.equals(applicationVersion)) {
            applicationVersion = applicationVersionLong;
        }

        // Notify listeners as applicationVersionLong (the long-form application version) arrives here,
        // after the VERSION_REPORT notification, and would otherwise remain stale on re-interview.
        getController().notifyEventListeners(new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                CommandClass.COMMAND_CLASS_VERSION, getApplicationVersion()));
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
     * Gets a SerialMessage with the VERSION_CAPABILITIES_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getVersionCapabilitiesMessage() {
        logger.debug("NODE {}: Creating new message for command VERSION_CAPABILITIES_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                VERSION_CAPABILITIES_GET).withPriority(TransactionPriority.Config)
                .withExpectedResponseCommand(VERSION_CAPABILITIES_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the VERSION_CAPABILITIES_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getVersionZWaveSoftwareMessage() {
        logger.debug("NODE {}: Creating new message for command VERSION_ZWAVE_SOFTWARE_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                VERSION_ZWAVE_SOFTWARE_GET).withPriority(TransactionPriority.Config)
                .withExpectedResponseCommand(VERSION_ZWAVE_SOFTWARE_REPORT).build();
    }

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
     * Returns the version of the ZWave firmware used by the device
     * Either as double or triple depending on the device capabilities.
     * Filters out bad data that may be reported by the device.
     *
     * @return Version (version . subversion . (patch))
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
