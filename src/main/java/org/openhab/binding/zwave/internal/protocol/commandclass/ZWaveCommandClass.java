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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Z-Wave Command Class. Z-Wave device functions are controlled by command classes. A command class can be have one or
 * multiple commands allowing the use of a certain function of the device.
 *
 * @author Chris Jackson
 * @author Brian Crosby
 */
public abstract class ZWaveCommandClass {

    private class ZWaveResponseHandlerMethod {
        int id;
        String name;
        Method method;

        ZWaveResponseHandlerMethod(int id, String name, Method method) {
            this.id = id;
            this.name = name;
            this.method = method;
        }
    };

    @XStreamOmitField
    Map<Integer, ZWaveResponseHandlerMethod> commands;

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveCommandClass.class);

    private static final int SIZE_MASK = 0x07;
    // private static final SCALE_MASK = 0x18; // unused
    // private static final SCALE_SHIFT = 0x03; // unused
    private static final int PRECISION_MASK = 0xe0;
    private static final int PRECISION_SHIFT = 0x05;

    @XStreamOmitField
    private ZWaveNode node;
    @XStreamOmitField
    private ZWaveController controller;

    private ZWaveEndpoint endpoint;

    private int version = 0;
    private int instances = 0;

    /**
     * True if this is a control only command class.
     * This means the device will only send commands, and we shouldn't initialise it as a server
     */
    private boolean control = false;

    @XStreamOmitField
    protected int versionMax = 1;

    @SuppressWarnings("unused")
    private int versionSupported = 0;

    /**
     * Protected constructor. Initiates a new instance of a Command Class.
     *
     * @param node the node this instance commands.
     * @param controller the controller to send messages to.
     * @param endpoint the endpoint this Command class belongs to.
     */
    protected ZWaveCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        initialise(node, controller, endpoint);
    }

    public void initialise(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        // Create the map of response command handlers
        commands = new HashMap<Integer, ZWaveResponseHandlerMethod>();
        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            ZWaveResponseHandler handler = method.getAnnotation(ZWaveResponseHandler.class);
            if (handler != null) {
                ZWaveResponseHandlerMethod handlerMethod = new ZWaveResponseHandlerMethod(handler.id(), handler.name(),
                        method);
                commands.put(handler.id(), handlerMethod);
            }
        }

        this.node = node;
        this.controller = controller;
        this.endpoint = endpoint;
        logger.debug("NODE {}: Command class {}, endpoint {} created", node.getNodeId(), getCommandClass().toString(),
                endpoint == null ? 0 : endpoint.getEndpointId());
    }

    /**
     * Returns the node this command class belongs to.
     *
     * @return node
     */
    protected ZWaveNode getNode() {
        return node;
    }

    /**
     * Returns the controller to send messages to.
     *
     * @return controller
     */
    protected ZWaveController getController() {
        return controller;
    }

    /**
     * Returns the endpoint this command class belongs to.
     *
     * @return the endpoint this command class belongs to.
     */
    public ZWaveEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Returns the version of the command class.
     *
     * @return node
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the version number for this command class that is supported by the device.
     *
     * @param version. The version number to set.
     */
    public void setVersion(int version) {
        // Record the version supported by the device
        // This gets recorded in the XML so we know more about the device
        versionSupported = version;

        // Now set the version we are actually going to support
        if (version > versionMax) {
            this.version = versionMax;
            logger.debug(
                    "NODE {}: Version = {}, version set to maximum supported by the binding. Enabling extra functionality.",
                    getNode().getNodeId(), versionMax);
        } else {
            this.version = version;
            logger.debug("NODE {}: Version = {}, version set. Enabling extra functionality.", getNode().getNodeId(),
                    version);
        }
    }

    /**
     * The maximum version implemented by this command class.
     */
    public int getMaxVersion() {
        return versionMax;
    }

    /**
     * Set the class to be a control command class.
     *
     * @param control true if this is a control command class within the device
     */
    public void setControlClass(boolean control) {
        this.control = control;
    }

    /**
     * Returns true if this command class is a control only class
     *
     * @return true if this command class is a control only class
     */
    public boolean isControlClass() {
        return control;
    }

    /**
     * Set options for this command class.
     * Options are provided from the device configuration database
     *
     * @param options class
     * @return true if options set ok
     */
    public boolean setOptions(Map<String, String> options) {
        return false;
    }

    /**
     * Returns the number of instances of this command class
     * in case the node supports the MULTI_INSTANCE command class (Version 1).
     *
     * @return the number of instances
     */
    public int getInstances() {
        return instances;
    }

    /**
     * Returns the number of instances of this command class
     * in case the node supports the MULTI_INSTANCE command class (Version 1).
     *
     * @param instances. The number of instances.
     */
    public void setInstances(int instances) {
        this.instances = instances;
    }

    /**
     * Returns the command class.
     *
     * @return command class
     */
    public abstract CommandClass getCommandClass();

    /**
     * Handles an incoming application command request.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     */
    public void handleApplicationCommandRequest(ZWaveCommandClassPayload payload, int endpoint)
            throws ZWaveSerialMessageException {

        if (commands == null) {
            logger.debug("NODE {}: Received {} V{} but class has no handlers.", getNode().getNodeId(),
                    getCommandClass(), getVersion());
        }

        ZWaveResponseHandlerMethod commandMethod = commands.get(payload.getCommandClassCommand());
        if (commandMethod == null) {
            // Check if there's a default handler
            commandMethod = commands.get(0);
            if (commandMethod == null) {
                logger.debug("NODE {}: Received {} V{} unknown command {}", getNode().getNodeId(), getCommandClass(),
                        getVersion(), payload.getCommandClassCommand());
                return;
            }
        }

        logger.debug("NODE {}: Received {} V{} {}", getNode().getNodeId(), getCommandClass(), getVersion(),
                commands.get(payload.getCommandClassCommand()).name);

        Object[] parms = { payload, endpoint };
        try {
            commands.get(payload.getCommandClassCommand()).method.invoke(this, parms);
        } catch (InvocationTargetException e) {
            // Handle exceptions from the command class processing
            if (e.getCause() instanceof ArrayIndexOutOfBoundsException) {
                logger.debug("NODE {}: ArrayIndexOutOfBoundsException {} V{} {} {}", getNode().getNodeId(),
                        getCommandClass(), getVersion(), commands.get(payload.getCommandClassCommand()).name,
                        SerialMessage.bb2hex(payload.getPayloadBuffer()));
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            logger.error("NODE {}: Error in handleApplicationCommandRequest", getNode().getNodeId(), e);
        }
    };

    /**
     * Gets an instance of the right command class.
     * Returns null if the command class is not found.
     *
     * @param i the code to instantiate
     * @param node the node this instance commands.
     * @param controller the controller to send messages to.
     * @return the ZWaveCommandClass instance that was instantiated, null otherwise
     */
    public static ZWaveCommandClass getInstance(int i, ZWaveNode node, ZWaveController controller) {
        return ZWaveCommandClass.getInstance(i, node, controller, null);
    }

    /**
     * Gets an instance of the right command class.
     * Returns null if the command class is not found.
     *
     * @param classId the code to instantiate
     * @param node the node this instance commands.
     * @param controller the controller to send messages to.
     * @param endpoint the endpoint this Command class belongs to
     * @return the ZWaveCommandClass instance that was instantiated, null otherwise
     */
    public static ZWaveCommandClass getInstance(int classId, ZWaveNode node, ZWaveController controller,
            ZWaveEndpoint endpoint) {
        try {
            CommandClass commandClass = CommandClass.getCommandClass(classId);
            if (commandClass != null && commandClass.equals(CommandClass.COMMAND_CLASS_MANUFACTURER_PROPRIETARY)) {
                commandClass = CommandClass.getCommandClass(node.getManufacturer(), node.getDeviceType());
            }
            if (commandClass == null) {
                logger.debug("NODE {}: Unknown command class 0x{}", node.getNodeId(), Integer.toHexString(classId));
                return null;
            }
            Class<? extends ZWaveCommandClass> commandClassClass = commandClass.getCommandClassClass();

            if (commandClassClass == null) {
                logger.debug("NODE {}: Unsupported command class {}", node.getNodeId(), commandClass.toString(),
                        classId);
                return null;
            }
            logger.debug("NODE {}: Creating new instance of command class {}", node.getNodeId(),
                    commandClass.toString());

            Constructor<? extends ZWaveCommandClass> constructor = commandClassClass.getConstructor(ZWaveNode.class,
                    ZWaveController.class, ZWaveEndpoint.class);
            return constructor.newInstance(new Object[] { node, controller, endpoint });
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.debug("NODE {}: Error instantiating command class 0x{}", node.getNodeId(),
                    Integer.toHexString(classId), e);
            return null;
        }
    }

    /**
     * Extract a decimal value from a byte array.
     *
     * @param payload.getPayloadByte(offset) the buffer to be parsed.
     * @param offset the offset at which to start reading
     * @return the extracted decimal value
     */
    protected BigDecimal extractValue(ZWaveCommandClassPayload payload, int offset) {
        int size = payload.getPayloadByte(offset) & SIZE_MASK;
        int precision = (payload.getPayloadByte(offset) & PRECISION_MASK) >> PRECISION_SHIFT;

        if ((size + offset) >= payload.getPayloadLength()) {
            throw new NumberFormatException("Error extracting value - length=" + payload.getPayloadLength()
                    + ", offset=" + offset + ", size=" + size + ".");
        }

        int value = 0;
        int i;
        for (i = 0; i < size; ++i) {
            value <<= 8;
            value |= payload.getPayloadByte(offset + i + 1) & 0xFF;
        }

        // Deal with sign extension. All values are signed
        BigDecimal result;
        if ((payload.getPayloadByte(offset + 1) & 0x80) == 0x80) {
            // MSB is signed
            if (size == 1) {
                value |= 0xffffff00;
            } else if (size == 2) {
                value |= 0xffff0000;
            }
        }

        result = BigDecimal.valueOf(value);

        BigDecimal divisor = BigDecimal.valueOf(Math.pow(10, precision));
        return result.divide(divisor);
    }

    /**
     * Extract a decimal value from a byte array.
     *
     * @param buffer the buffer to be parsed.
     * @param offset the offset at which to start reading
     * @return the extracted decimal value
     */
    protected int extractValue(byte[] buffer, int offset, int size) {
        int value = 0;
        for (int i = 0; i < size; ++i) {
            value <<= 8;
            value |= buffer[offset + i] & 0xFF;
        }

        // Deal with sign extension. All values are signed
        if ((buffer[offset] & 0x80) == 0x80) {
            // MSB is signed
            if (size == 1) {
                value |= 0xffffff00;
            } else if (size == 2) {
                value |= 0xffff0000;
            }
        }

        return value;
    }

    /**
     * Encodes a decimal value as a byte array.
     *
     * @param value the decimal value to encode
     * @param index the value index
     * @return the value buffer
     * @throws ArithmeticException when the supplied value is out of range.
     */
    protected byte[] encodeValue(BigDecimal value) throws ArithmeticException {

        // Remove any trailing zero's so we send the least amount of bytes possible
        BigDecimal normalizedValue = value.stripTrailingZeros();

        // Make our scale at least 0, precision cannot be more than 7 but
        // this is guarded by the Integer min / max values already.
        if (normalizedValue.scale() < 0) {
            normalizedValue = normalizedValue.setScale(0);
        }

        if (normalizedValue.unscaledValue().compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new ArithmeticException();
        } else if (normalizedValue.unscaledValue().compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0) {
            throw new ArithmeticException();
        }

        // default size = 4
        int size = 4;

        // it might fit in a byte or short
        if (normalizedValue.unscaledValue().intValue() >= Byte.MIN_VALUE
                && normalizedValue.unscaledValue().intValue() <= Byte.MAX_VALUE) {
            size = 1;
        } else if (normalizedValue.unscaledValue().intValue() >= Short.MIN_VALUE
                && normalizedValue.unscaledValue().intValue() <= Short.MAX_VALUE) {
            size = 2;
        }

        int precision = normalizedValue.scale();

        byte[] result = new byte[size + 1];
        // precision + scale (unused) + size
        result[0] = (byte) ((precision << PRECISION_SHIFT) | size);
        int unscaledValue = normalizedValue.unscaledValue().intValue(); // ie. 22.5 = 225
        for (int i = 0; i < size; i++) {
            result[size - i] = (byte) ((unscaledValue >> (i * 8)) & 0xFF);
        }
        return result;
    }

    /**
     * Command class enumeration. Lists all command classes available.
     * Unsupported command classes by the binding return null for the command class Class.
     *
     */
    @XStreamAlias("commandClass")
    public enum CommandClass {
        COMMAND_CLASS_NO_OPERATION(0x00, ZWaveNoOperationCommandClass.class),
        COMMAND_CLASS_BASIC(0x20, ZWaveBasicCommandClass.class),
        COMMAND_CLASS_CONTROLLER_REPLICATION(0x21, ZWaveControllerReplicationCommandClass.class),
        COMMAND_CLASS_APPLICATION_STATUS(0x22, ZWaveApplicationStatusCommandClass.class),
        COMMAND_CLASS_ZIP(0x23, null),
        COMMAND_CLASS_SECURITY_PANEL_MODE(0x24, null),
        COMMAND_CLASS_SWITCH_BINARY(0x25, ZWaveBinarySwitchCommandClass.class),
        COMMAND_CLASS_SWITCH_MULTILEVEL(0x26, ZWaveMultiLevelSwitchCommandClass.class),
        COMMAND_CLASS_SWITCH_ALL(0x27, ZWaveSwitchAllCommandClass.class),
        COMMAND_CLASS_SWITCH_TOGGLE_BINARY(0x28, ZWaveBinaryToggleSwitchCommandClass.class),
        COMMAND_CLASS_SWITCH_TOGGLE_MULTILEVEL(0x29, ZWaveMultiLevelToggleSwitchCommandClass.class),
        COMMAND_CLASS_CHIMNEY_FAN(0x2A, ZWaveChimneyFanCommandClass.class),
        COMMAND_CLASS_SCENE_ACTIVATION(0x2B, ZWaveSceneActivationCommandClass.class),
        COMMAND_CLASS_SCENE_ACTUATOR_CONF(0x2C, ZWaveSceneActuatorConfigurationCommandClass.class),
        COMMAND_CLASS_SCENE_CONTROLLER_CONF(0x2D, ZWaveSceneControllerConfigurationCommandClass.class),
        COMMAND_CLASS_SECURITY_PANEL_ZONE(0x2E, null),
        COMMAND_CLASS_SECURITY_PANEL_ZONE_SENSOR(0x2F, null),
        COMMAND_CLASS_SENSOR_BINARY(0x30, ZWaveBinarySensorCommandClass.class),
        COMMAND_CLASS_SENSOR_MULTILEVEL(0x31, ZWaveMultiLevelSensorCommandClass.class),
        COMMAND_CLASS_METER(0x32, ZWaveMeterCommandClass.class),
        COMMAND_CLASS_SWITCH_COLOR(0x33, ZWaveColorCommandClass.class),
        COMMAND_CLASS_NETWORK_MANAGEMENT_INCLUSION(0x34, null),
        COMMAND_CLASS_METER_PULSE(0x35, ZWaveMeterPulseCommandClass.class),
        COMMAND_CLASS_BASIC_TARIFF_INFO(0x36, null),
        COMMAND_CLASS_HRV_STATUS(0x37, null),
        COMMAND_CLASS_HRV_CONTROL(0x39, null),
        COMMAND_CLASS_DCP_CONFIG(0x3A, null),
        COMMAND_CLASS_DCP_MONITOR(0x3B, null),
        COMMAND_CLASS_METER_TBL_CONFIG(0x3C, ZWaveMeterTblConfigurationCommandClass.class),
        COMMAND_CLASS_METER_TBL_MONITOR(0x3D, ZWaveMeterTblMonitorCommandClass.class),
        COMMAND_CLASS_METER_TBL_PUSH(0x3E, null),
        COMMAND_CLASS_THERMOSTAT_HEATING(0x38, null),
        COMMAND_CLASS_PREPAYMENT(0x3F, null),
        COMMAND_CLASS_THERMOSTAT_MODE(0x40, ZWaveThermostatModeCommandClass.class),
        COMMAND_CLASS_PREPAYMENT_ENCAPSULATION(0x41, null),
        COMMAND_CLASS_THERMOSTAT_OPERATING_STATE(0x42, ZWaveThermostatOperatingStateCommandClass.class),
        COMMAND_CLASS_THERMOSTAT_SETPOINT(0x43, ZWaveThermostatSetpointCommandClass.class),
        COMMAND_CLASS_THERMOSTAT_FAN_MODE(0x44, ZWaveThermostatFanModeCommandClass.class),
        COMMAND_CLASS_THERMOSTAT_FAN_STATE(0x45, ZWaveThermostatFanStateCommandClass.class),
        COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE(0x46, ZWaveClimateControlScheduleCommandClass.class),
        COMMAND_CLASS_THERMOSTAT_SETBACK(0x47, ZWaveThermostatSetbackCommandClass.class),
        COMMAND_CLASS_RATE_TBL_CONFIG(0x48, null),
        COMMAND_CLASS_RATE_TBL_MONITOR(0x49, null),
        COMMAND_CLASS_TARIFF_CONFIG(0x4A, null),
        COMMAND_CLASS_TARIFF_TBL_MONITOR(0x4B, null),
        COMMAND_CLASS_DOOR_LOCK_LOGGING(0x4C, ZWaveDoorLockLoggingCommandClass.class),
        COMMAND_CLASS_NETWORK_MANAGEMENT_BASIC(0x4D, null),
        COMMAND_CLASS_SCHEDULE_ENTRY_LOCK(0x4E, ZWaveScheduleEntryLockCommandClass.class),
        COMMAND_CLASS_ZIP_6LOWPAN(0x4F, null),
        COMMAND_CLASS_BASIC_WINDOW_COVERING(0x50, ZWaveBasicWindowCoveringCommandClass.class),
        COMMAND_CLASS_MTP_WINDOW_COVERING(0x51, ZWaveMtpWindowCoveringCommandClass.class),
        COMMAND_CLASS_NETWORK_MANAGEMENT_PROXY(0x52, null),
        COMMAND_CLASS_SCHEDULE(0x53, ZWaveScheduleCommandClass.class),
        COMMAND_CLASS_NETWORK_MANAGEMENT_PRIMARY(0x54, null),
        COMMAND_CLASS_TRANSPORT_SERVICE(0x55, null),
        COMMAND_CLASS_CRC_16_ENCAP(0x56, ZWaveCRC16EncapsulationCommandClass.class),
        COMMAND_CLASS_APPLICATION_CAPABILITY(0x57, null),
        COMMAND_CLASS_ZIP_ND(0x58, null),
        COMMAND_CLASS_ASSOCIATION_GRP_INFO(0x59, ZWaveAssociationGroupInfoCommandClass.class),
        COMMAND_CLASS_DEVICE_RESET_LOCALLY(0x5A, ZWaveDeviceResetLocallyCommandClass.class),
        COMMAND_CLASS_CENTRAL_SCENE(0x5B, ZWaveCentralSceneCommandClass.class),
        COMMAND_CLASS_IP_ASSOCIATION(0x5C, null),
        COMMAND_CLASS_ANTITHEFT(0x5D, null),
        COMMAND_CLASS_ZWAVEPLUS_INFO(0x5E, ZWavePlusCommandClass.class),
        COMMAND_CLASS_ZIP_GATEWAY(0x5F, null),
        COMMAND_CLASS_MULTI_CHANNEL(0x60, ZWaveMultiInstanceCommandClass.class),
        COMMAND_CLASS_ZIP_PORTAL(0x61, null),
        COMMAND_CLASS_DOOR_LOCK(0x62, ZWaveDoorLockCommandClass.class),
        COMMAND_CLASS_USER_CODE(0x63, ZWaveUserCodeCommandClass.class),
        COMMAND_CLASS_HUMIDITY_CONTROL_SETPOINT(0x64, null),
        COMMAND_CLASS_DMX(0x65, null),
        COMMAND_CLASS_BARRIER_OPERATOR(0x66, ZWaveBarrierOperatorCommandClass.class),
        COMMAND_CLASS_NETWORK_MANAGEMENT_INSTALLATION_MAINTENANCE(0x67, null),
        COMMAND_CLASS_ZIP_NAMING(0x68, null),
        COMMAND_CLASS_MAILBOX(0x69, null),
        COMMAND_CLASS_WINDOW_COVERING(0x6A, null),
        COMMAND_CLASS_IRRIGATION(0x6B, null),
        COMMAND_CLASS_SUPERVISION(0x6C, null),
        COMMAND_CLASS_HUMIDITY_CONTROL_MODE(0x6D, null),
        COMMAND_CLASS_HUMIDITY_CONTROL_OPERATING_STATE(0x6E, null),
        COMMAND_CLASS_ENTRY_CONTROL(0x6F, null),
        COMMAND_CLASS_CONFIGURATION(0x70, ZWaveConfigurationCommandClass.class),
        COMMAND_CLASS_ALARM(0x71, ZWaveAlarmCommandClass.class),
        COMMAND_CLASS_MANUFACTURER_SPECIFIC(0x72, ZWaveManufacturerSpecificCommandClass.class),
        COMMAND_CLASS_POWERLEVEL(0x73, ZWavePowerLevelCommandClass.class),
        COMMAND_CLASS_INCLUSION_CONTROLLER(0x74, null),
        COMMAND_CLASS_PROTECTION(0x75, ZWaveProtectionCommandClass.class),
        COMMAND_CLASS_LOCK(0x76, ZWaveLockCommandClass.class),
        COMMAND_CLASS_NODE_NAMING(0x77, ZWaveNodeNamingCommandClass.class),
        COMMAND_CLASS_FIRMWARE_UPDATE_MD(0x7A, ZWaveFirmwareUpdateCommandClass.class),
        COMMAND_CLASS_GROUPING_NAME(0x7B, ZWaveGroupingNameCommandClass.class),
        COMMAND_CLASS_REMOTE_ASSOCIATION_ACTIVATE(0x7C, null),
        COMMAND_CLASS_REMOTE_ASSOCIATION(0x7D, null),
        COMMAND_CLASS_BATTERY(0x80, ZWaveBatteryCommandClass.class),
        COMMAND_CLASS_CLOCK(0x81, ZWaveClockCommandClass.class),
        COMMAND_CLASS_HAIL(0x82, ZWaveHailCommandClass.class),
        COMMAND_CLASS_WAKE_UP(0x84, ZWaveWakeUpCommandClass.class),
        COMMAND_CLASS_ASSOCIATION(0x85, ZWaveAssociationCommandClass.class),
        COMMAND_CLASS_VERSION(0x86, ZWaveVersionCommandClass.class),
        COMMAND_CLASS_INDICATOR(0x87, ZWaveIndicatorCommandClass.class),
        COMMAND_CLASS_PROPRIETARY(0x88, null),
        COMMAND_CLASS_LANGUAGE(0x89, ZWaveLanguageCommandClass.class),
        COMMAND_CLASS_TIME(0x8A, ZWaveTimeCommandClass.class),
        COMMAND_CLASS_TIME_PARAMETERS(0x8B, ZWaveTimeParametersCommandClass.class),
        COMMAND_CLASS_GEOGRAPHIC_LOCATION(0x8C, null),
        COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION(0x8E, ZWaveMultiAssociationCommandClass.class),
        COMMAND_CLASS_MULTI_CMD(0x8F, ZWaveMultiCommandCommandClass.class),
        COMMAND_CLASS_ENERGY_PRODUCTION(0x90, ZWaveEnergyProductionCommandClass.class),
        // Note that MANUFACTURER_PROPRIETARY shouldn't be instantiated directly
        // The getInstance method will catch this and translate to the correct
        // class for the device.
        COMMAND_CLASS_MANUFACTURER_PROPRIETARY(0x91, ZWaveManufacturerProprietaryCommandClass.class),
        COMMAND_CLASS_SCREEN_MD(0x92, null),
        COMMAND_CLASS_SCREEN_ATTRIBUTES(0x93, null),
        COMMAND_CLASS_SIMPLE_AV_CONTROL(0x94, null),
        COMMAND_CLASS_AV_CONTENT_DIRECTORY_MD(0x95, null),
        COMMAND_CLASS_AV_RENDERER_STATUS(0x96, null),
        COMMAND_CLASS_AV_CONTENT_SEARCH_MD(0x97, null),
        COMMAND_CLASS_SECURITY(0x98, ZWaveSecurityCommandClass.class),
        COMMAND_CLASS_AV_TAGGING_MD(0x99, null),
        COMMAND_CLASS_IP_CONFIGURATION(0x9A, null),
        COMMAND_CLASS_ASSOCIATION_COMMAND_CONFIGURATION(0x9B, null),
        COMMAND_CLASS_SENSOR_ALARM(0x9C, ZWaveAlarmSensorCommandClass.class),
        COMMAND_CLASS_SILENCE_ALARM(0x9D, ZWaveAlarmSilenceCommandClass.class),
        COMMAND_CLASS_SENSOR_CONFIGURATION(0x9E, ZWaveSensorConfigurationCommandClass.class),
        COMMAND_CLASS_SECURITY_2(0x9F, null),
        COMMAND_CLASS_MARK(0xEF, null),
        COMMAND_CLASS_NON_INTEROPERABLE(0xF0, null);

        // MANUFACTURER_PROPRIETARY class definitions are defined by the manufacturer and device id

        /**
         * A mapping between the integer code and its corresponding
         * Command class to facilitate lookup by code.
         */
        private static Map<Integer, CommandClass> codeToCommandClassMapping;

        /**
         * A mapping between the string label and its corresponding
         * Command class to facilitate lookup by label.
         */
        private static Map<String, CommandClass> labelToCommandClassMapping;

        /**
         * Get unique command class code for manufacturer and device ID.
         *
         * To support manufacturer specific implementations of a manufacturer proprietary command class we use the
         * manufacturer and the device id to generate a unique key.
         *
         * @param manufacturer the manufacturer ID
         * @param deviceId the device ID
         * @return a unique command class key
         */
        private static int getKeyFromManufacturerAndDeviceId(int manufacturer, int deviceId) {
            return manufacturer << 16 | deviceId;
        }

        private int key;
        private Class<? extends ZWaveCommandClass> commandClassClass;

        private CommandClass(int key, Class<? extends ZWaveCommandClass> commandClassClass) {
            this.key = key;
            this.commandClassClass = commandClassClass;
        }

        private CommandClass(int manufacturer, int deviceId, String label,
                Class<? extends ZWaveCommandClass> commandClassClass) {
            this(getKeyFromManufacturerAndDeviceId(manufacturer, deviceId), commandClassClass);
        }

        private static void initMapping() {
            codeToCommandClassMapping = new HashMap<Integer, CommandClass>();
            labelToCommandClassMapping = new HashMap<String, CommandClass>();
            for (CommandClass s : values()) {
                codeToCommandClassMapping.put(s.key, s);
                labelToCommandClassMapping.put(s.name(), s);
            }
        }

        /**
         * Lookup function based on the command class code.
         * Returns null if there is no command class with code i
         *
         * @param i the code to lookup
         * @return enumeration value of the command class.
         */
        public static CommandClass getCommandClass(int i) {
            if (codeToCommandClassMapping == null) {
                initMapping();
            }

            return codeToCommandClassMapping.get(i);
        }

        /**
         * Lookup function based on the manufacturer and device ID.
         *
         * @param manufacturer the manufacturer ID
         * @param deviceId the device ID
         * @return enumeration value of the command class or null if there is no command class.
         */
        public static CommandClass getCommandClass(int manufacturer, int deviceId) {
            return getCommandClass(getKeyFromManufacturerAndDeviceId(manufacturer, deviceId));
        }

        /**
         * Lookup function based on the command class label.
         * Returns null if there is no command class with that label.
         *
         * @param label the label to lookup
         * @return enumeration value of the command class.
         */
        public static CommandClass getCommandClass(String label) {
            if (labelToCommandClassMapping == null) {
                initMapping();
            }

            return labelToCommandClassMapping.get(label.toUpperCase());
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }

        /**
         * @return the command class Class
         */
        public Class<? extends ZWaveCommandClass> getCommandClassClass() {
            return commandClassClass;
        }
    }
}
