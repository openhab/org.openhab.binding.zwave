/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
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
@XStreamAlias("versionCommandClass")
public class ZWaveVersionCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private final static Logger logger = LoggerFactory.getLogger(ZWaveVersionCommandClass.class);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received COMMAND_CLASS_VERSION command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case VERSION_REPORT:
                logger.debug("NODE {}: Process Version Report", getNode().getNodeId());
                libraryType = LibraryType.getLibraryType(serialMessage.getMessagePayloadByte(offset + 1));
                protocolVersion = Integer.toString(serialMessage.getMessagePayloadByte(offset + 2)) + "."
                        + Integer.toString(serialMessage.getMessagePayloadByte(offset + 3));
                applicationVersion = Integer.toString(serialMessage.getMessagePayloadByte(offset + 4)) + "."
                        + Integer.toString(serialMessage.getMessagePayloadByte(offset + 5));

                logger.debug("NODE {}: Library Type        = {} ({})", getNode().getNodeId(), libraryType.key,
                        libraryType.label);
                logger.debug("NODE {}: Protocol Version    = {}", getNode().getNodeId(), protocolVersion);
                logger.debug("NODE {}: Application Version = {}", getNode().getNodeId(), applicationVersion);

                if (serialMessage.getMessagePayload().length > (offset + 6)) {
                    // Version 2 includes hardware version, as well as additional versions for other processors
                    // The version 1 and version 2 requests are the same. We differentiate using the length since we may
                    // not have request the version of the VERSION command class at this point.
                    hardwareVersion = serialMessage.getMessagePayloadByte(offset + 6);
                    logger.debug("NODE {}: Hardware Version     = {}", getNode().getNodeId(), hardwareVersion);
                }
                break;
            case VERSION_COMMAND_CLASS_REPORT:
                logger.debug("NODE {}: Process Version Command Class Report", getNode().getNodeId());
                int commandClassCode = serialMessage.getMessagePayloadByte(offset + 1);
                int commandClassVersion = serialMessage.getMessagePayloadByte(offset + 2);

                CommandClass commandClass = CommandClass.getCommandClass(commandClassCode);
                if (commandClass == null) {
                    logger.debug(String.format("NODE %d: Unsupported command class 0x%02x", getNode().getNodeId(),
                            commandClassCode));
                    return;
                }

                logger.debug("NODE {}: Requested Command Class = {}, Version = {}", getNode().getNodeId(),
                        commandClass.getLabel(), commandClassVersion);

                // The version is set on the command class for this node. By updating the version, extra functionality
                // is unlocked in the command class.
                // The messages are backwards compatible, so it's not a problem that there is a slight delay when the
                // command class version is queried on the node.
                ZWaveCommandClass zwaveCommandClass = getNode().getCommandClass(commandClass);
                if (zwaveCommandClass == null) {
                    logger.debug(String.format("NODE %d: Unsupported command class %s (0x%02x)", getNode().getNodeId(),
                            commandClass.getLabel(), commandClassCode));
                    return;
                }

                // If the device reports version 0, it means it doesn't support this command class!
                if (commandClassVersion == 0) {
                    logger.info("NODE {}: Command Class {} has version 0!", getNode().getNodeId(),
                            commandClass.getLabel());

                    // Do not remove mandatory generic command classes.
                    for (CommandClass mandatoryCommandClass : getNode().getDeviceClass().getGenericDeviceClass()
                            .getMandatoryCommandClasses()) {
                        if (mandatoryCommandClass == commandClass) {
                            commandClassVersion = 1;
                            break;
                        }
                    }

                    // Do not remove mandatory specific command classes.
                    if (commandClassVersion == 0) {
                        for (CommandClass mandatoryCommandClass : getNode().getDeviceClass().getSpecificDeviceClass()
                                .getMandatoryCommandClasses()) {
                            if (mandatoryCommandClass == commandClass) {
                                commandClassVersion = 1;
                                break;
                            }
                        }
                    }

                    // Remove other command classes.
                    if (commandClassVersion == 0) {
                        getNode().removeCommandClass(commandClass);
                        logger.debug("NODE {}: Removed Command Class {}", getNode().getNodeId(),
                                commandClass.getLabel());
                        return;
                    }
                }

                zwaveCommandClass.setVersion(commandClassVersion);
                break;
            default:
                logger.warn(String.format("Unsupported Command 0x%02X for command class %s (0x%02X).", command,
                        getCommandClass().getLabel(), getCommandClass().getKey()));
        }
    }

    /**
     * Gets a SerialMessage with the VERSION_GET command
     *
     * @return the serial message
     */
    public SerialMessage getVersionMessage() {
        logger.debug("NODE {}: Creating new message for command VERSION_GET", getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Config);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                (byte) VERSION_GET };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Gets a SerialMessage with the VERSION COMMAND CLASS GET command.
     * This version is used to differentiate between multiple versions of a command
     * and to enable extra functionality.
     *
     * @param commandClass The command class to get the version for.
     * @return the serial message
     */
    public SerialMessage getCommandClassVersionMessage(CommandClass commandClass) {
        logger.debug("NODE {}: Creating new message for application command VERSION_COMMAND_CLASS_GET command class {}",
                getNode().getNodeId(), commandClass.getLabel());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Config);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 3, (byte) getCommandClass().getKey(),
                (byte) VERSION_COMMAND_CLASS_GET, (byte) commandClass.getKey() };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Check the version of a command class by sending a VERSION_COMMAND_CLASS_GET message to the node.
     *
     * @param commandClass the command class to check the version for.
     * @return serial message to be sent
     */
    public SerialMessage checkVersion(ZWaveCommandClass commandClass) {
        ZWaveVersionCommandClass versionCommandClass = (ZWaveVersionCommandClass) this.getNode()
                .getCommandClass(CommandClass.VERSION);

        if (versionCommandClass == null) {
            logger.debug(String.format(
                    "NODE %d: Version command class not supported,"
                            + "reverting to version 1 for command class %s (0x%02x)",
                    getNode().getNodeId(), commandClass.getCommandClass().getLabel(),
                    commandClass.getCommandClass().getKey()));
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
