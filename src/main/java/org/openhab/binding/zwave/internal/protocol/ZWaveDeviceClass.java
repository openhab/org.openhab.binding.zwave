/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Z-Wave device class. A Z-Wave device class groups devices with the same functionality together in a class.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("deviceClass")
public class ZWaveDeviceClass {
    @XStreamOmitField
    private final Logger logger = LoggerFactory.getLogger(ZWaveDeviceClass.class);

    private Basic basicDeviceClass;
    private Generic genericDeviceClass;
    private Specific specificDeviceClass;

    /**
     * Constructor. Creates a new instance of the Z-Wave device class.
     *
     * @param basicDeviceClass the basic device class of this node.
     * @param genericDeviceClass the generic device class of this node.
     * @param specificDeviceClass the specific device class of this node.
     */
    public ZWaveDeviceClass(Basic basicDeviceClass, Generic genericDeviceClass, Specific specificDeviceClass) {
        logger.trace("Constructing Zwave Device Class");

        this.basicDeviceClass = basicDeviceClass;
        this.genericDeviceClass = genericDeviceClass;
        this.specificDeviceClass = specificDeviceClass;
    }

    /**
     * Returns the basic device class of the node.
     *
     * @return the basicDeviceClass
     */
    public Basic getBasicDeviceClass() {
        return basicDeviceClass;
    }

    /**
     * Set the basic device class of the node.
     *
     * @param basicDeviceClass the basicDeviceClass to set
     */
    public void setBasicDeviceClass(Basic basicDeviceClass) {
        this.basicDeviceClass = basicDeviceClass;
    }

    /**
     * Get the generic device class of the node.
     *
     * @return the genericDeviceClass
     */
    public Generic getGenericDeviceClass() {
        return genericDeviceClass;
    }

    /**
     * Set the generic device class of the node.
     *
     * @param genericDeviceClass the genericDeviceClass to set
     */
    public void setGenericDeviceClass(Generic genericDeviceClass) {
        this.genericDeviceClass = genericDeviceClass;
    }

    /**
     * Get the specific device class of the node.
     *
     * @return the specificDeviceClass
     */
    public Specific getSpecificDeviceClass() {
        return specificDeviceClass;
    }

    /**
     * Set the specific device class of the node.
     *
     * @param specificDeviceClass the specificDeviceClass to set
     * @exception IllegalArgumentException thrown when the specific device class does not match
     *                the generic device class.
     */
    public void setSpecificDeviceClass(Specific specificDeviceClass) throws IllegalArgumentException {

        // The specific Device class does not match the generic device class.
        if (specificDeviceClass.genericDeviceClass != Generic.NOT_KNOWN
                && specificDeviceClass.genericDeviceClass != this.genericDeviceClass) {
            throw new IllegalArgumentException("specificDeviceClass");
        }

        this.specificDeviceClass = specificDeviceClass;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((basicDeviceClass == null) ? 0 : basicDeviceClass.hashCode());
        result = prime * result + ((genericDeviceClass == null) ? 0 : genericDeviceClass.hashCode());
        result = prime * result + ((specificDeviceClass == null) ? 0 : specificDeviceClass.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        ZWaveDeviceClass other = (ZWaveDeviceClass) obj;
        if (basicDeviceClass != other.basicDeviceClass) {
            return false;
        }
        if (genericDeviceClass != other.genericDeviceClass) {
            return false;
        }
        if (specificDeviceClass != other.specificDeviceClass) {
            return false;
        }
        return true;
    }

    /**
     * Z-Wave basic Device Class enumeration. The Basic Device Class provides
     * the device with a role in the Z-Wave network.
     *
     * @author Brian Crosby
     * @author Jan-Willem Spuij
     * @since 1.3.0
     */
    public enum Basic {
        NOT_KNOWN(0, "Not Known"),
        CONTROLLER(1, "Controller"),
        STATIC_CONTROLLER(2, "Static Controller"),
        SLAVE(3, "Slave"),
        ROUTING_SLAVE(4, "Routing Slave");

        /**
         * A mapping between the integer code and its corresponding Basic device
         * class to facilitate lookup by code.
         */
        private static Map<Integer, Basic> codeToBasicMapping;

        private int key;
        private String label;

        private Basic(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToBasicMapping = new HashMap<Integer, Basic>();
            for (Basic s : values()) {
                codeToBasicMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the basic device class code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the basic device class.
         */
        public static Basic getBasic(int i) {
            if (codeToBasicMapping == null) {
                initMapping();
            }

            return codeToBasicMapping.get(i);
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

    /**
     * Z-Wave Generic Device Class enumeration. The Generic Device Class describes functionality of a device in the
     * Network. Generic Device Classes can have Command Classes that are mandatory or recommended for all devices that
     * belong to this device class. Generic device class do not relate directly to Basic Device Classes. E.G. a
     * BINARY_SWITCH can be a ROUTING_SLAVE or a SLAVE.
     *
     * @author Brian Crosby
     * @author Jan-Willem Spuij
     * @author Chris Jackson
     */
    public enum Generic {
        NOT_KNOWN(0, "Not Known"),
        REMOTE_CONTROLLER(0x01, "Remote Controller"),
        STATIC_CONTROLLER(0x02, "Static Controller"),
        AV_CONTROL_POINT(0x03, "A/V Control Point"),
        DISPLAY(0x06, "Display"),
        SENSOR_NOTIFICATION(0x07, "Sensor Notification"),
        THERMOSTAT(0x08, "Thermostat"),
        WINDOW_COVERING(0x09, "Window Covering"),
        REPEATER_SLAVE(0x0f, "Repeater Slave"),
        BINARY_SWITCH(0x10, "Binary Switch"),
        MULTILEVEL_SWITCH(0x11, "Multi-Level Switch"),
        REMOTE_SWITCH(0x12, "Remote Switch"),
        TOGGLE_SWITCH(0x13, "Toggle Switch"),
        Z_IP_GATEWAY(0x14, "Z/IP Gateway"),
        Z_IP_NODE(0x15, "Z/IP Node"),
        VENTILATION(0x16, "Ventilation"),
        REMOTE_SWITCH_2(0x18, "Remote Switch 2"),
        BINARY_SENSOR(0x20, "Binary Sensor"),
        MULTILEVEL_SENSOR(0x21, "Multi-Level Sensor"),
        WATER_CONTROL(0x22, "Water Control"),
        PULSE_METER(0x30, "Pulse Meter"),
        METER(0x31, "Meter"),
        ENTRY_CONTROL(0x40, "Entry Control"),
        SEMI_INTEROPERABLE(0x50, "Semi-Interoperable"),
        ALARM_SENSOR(0xa1, "Alarm Sensor"),
        NON_INTEROPERABLE(0xff, "Non-Interoperable");

        /**
         * A mapping between the integer code and its corresponding Generic
         * Device class to facilitate lookup by code.
         */
        private static Map<Integer, Generic> codeToGenericMapping;

        private int key;
        private String label;

        private Generic(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToGenericMapping = new HashMap<Integer, Generic>();
            for (Generic s : values()) {
                codeToGenericMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the generic device class code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the generic device class.
         */
        public static Generic getGeneric(int i) {
            if (codeToGenericMapping == null) {
                initMapping();
            }

            return codeToGenericMapping.get(i);
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

        /**
         * Get the mandatory command classes for this device class.
         *
         * @return the mandatory command classes.
         */
        public CommandClass[] getMandatoryCommandClasses() {
            switch (this) {
                case NOT_KNOWN:
                    return new CommandClass[0];
                case REMOTE_CONTROLLER:
                case STATIC_CONTROLLER:
                case SENSOR_NOTIFICATION:
                case REPEATER_SLAVE:
                case TOGGLE_SWITCH:
                case REMOTE_SWITCH:
                case REMOTE_SWITCH_2:
                case WINDOW_COVERING:
                case THERMOSTAT:
                case AV_CONTROL_POINT:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC };
                case BINARY_SWITCH:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC,
                            CommandClass.SWITCH_BINARY };
                case MULTILEVEL_SWITCH:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC,
                            CommandClass.SWITCH_MULTILEVEL };
                case BINARY_SENSOR:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC,
                            CommandClass.SENSOR_BINARY };
                case MULTILEVEL_SENSOR:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC,
                            CommandClass.SENSOR_MULTILEVEL };
                case PULSE_METER:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC,
                            CommandClass.METER_PULSE };
                case METER:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC };
                case ENTRY_CONTROL:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC, CommandClass.LOCK };
                case ALARM_SENSOR:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC };
                case SEMI_INTEROPERABLE:
                    return new CommandClass[] { CommandClass.NO_OPERATION, CommandClass.BASIC,
                            CommandClass.MANUFACTURER_SPECIFIC, CommandClass.VERSION, CommandClass.PROPRIETARY };
                default:
                    return new CommandClass[0];
            }
        }
    }

    /**
     * Z-Wave Specific Device Class enumeration. Specific Device Classes are a more detailed definition of a device, and
     * are based on a Generic Device Class. The Specific Device Class inherits all the mandatory commands from the
     * Generic Device Class. In addition to these commands, more mandatory or recommended Command Classes can be
     * specified for a Specific Device Class.
     *
     * @author Brian Crosby
     * @author Jan-Willem Spuij
     * @author Chris Jackson
     */
    public enum Specific {
        NOT_USED(0, Generic.NOT_KNOWN, "Not Known"),
        PORTABLE_REMOTE_CONTROLLER(1, Generic.REMOTE_CONTROLLER, "Portable Remote Controller"),
        PORTABLE_SCENE_CONTROLLER(2, Generic.REMOTE_CONTROLLER, "Portable Scene Controller"),
        PORTABLE_INSTALLER_TOOL(3, Generic.REMOTE_CONTROLLER, "Portable Installer Tool"),
        AV_REMOTE_CONTROLLER(4, Generic.REMOTE_CONTROLLER, "AV Remote Controller"),
        SIMPLE_REMOTE_CONTROLLER(6, Generic.REMOTE_CONTROLLER, "Simple Remote Controller"),

        PC_CONTROLLER(1, Generic.STATIC_CONTROLLER, "PC Controller"),
        SCENE_CONTROLLER(2, Generic.STATIC_CONTROLLER, "Scene Controller"),
        INSTALLER_TOOL(3, Generic.STATIC_CONTROLLER, "Static Installer Tool"),
        SET_TOP_BOX_CONTROLLER(4, Generic.STATIC_CONTROLLER, "Set-Top Box Controller"),
        SUB_SYSTEM_CONTROLLER(4, Generic.STATIC_CONTROLLER, "Sub-System Controller"),
        TV_CONTROLLER(6, Generic.STATIC_CONTROLLER, "TV Controller"),
        SPECIFIC_TYPE_GATEWAY(7, Generic.STATIC_CONTROLLER, "Gateway"),

        SATELLITE_RECEIVER(4, Generic.AV_CONTROL_POINT, "Satellite Receiver"),
        SATELLITE_RECEIVER_V2(17, Generic.AV_CONTROL_POINT, "Satellite Receiver V2"),
        DOORBELL(18, Generic.AV_CONTROL_POINT, "Doorbell"),

        SIMPLE_DISPLAY(1, Generic.DISPLAY, "Simple Display"),

        THERMOSTAT_HEATING(1, Generic.THERMOSTAT, "Heating Thermostat"),
        THERMOSTAT_GENERAL(2, Generic.THERMOSTAT, "General Thermostat"),
        SETBACK_SCHEDULE_THERMOSTAT(3, Generic.THERMOSTAT, "Setback Schedule Thermostat"),
        SETPOINT_THERMOSTAT(4, Generic.THERMOSTAT, "Setpoint Thermostat"),
        SETBACK_THERMOSTAT(5, Generic.THERMOSTAT, "Setback Thermostat"),
        THERMOSTAT_GENERAL_V2(6, Generic.THERMOSTAT, "General Thermostat V2"),

        SIMPLE_WINDOW_COVERING(1, Generic.WINDOW_COVERING, "Simple Window Covering Control"),

        BASIC_REPEATER_SLAVE(1, Generic.REPEATER_SLAVE, "Basic Repeater Slave"),

        POWER_SWITCH_BINARY(1, Generic.BINARY_SWITCH, "Binary Power Switch"),
        SCENE_SWITCH_BINARY_DISCONTINUED(2, Generic.BINARY_SWITCH, "Binary Scene Switch (Discontinued)"),
        SCENE_SWITCH_BINARY(3, Generic.BINARY_SWITCH, "Binary Scene Switch"),
        POWER_STRIP(4, Generic.BINARY_SWITCH, "Power Strip Device"),
        SIREN_SWITCH_BINARY(5, Generic.BINARY_SWITCH, "Siren Switch"),
        VALVE_SWITCH_BINARY(6, Generic.BINARY_SWITCH, "Valve Switch"),

        POWER_SWITCH_MULTILEVEL(1, Generic.MULTILEVEL_SWITCH, "Multilevel Power Switch"),
        SCENE_SWITCH_MULTILEVEL_DISCONTINUED(2, Generic.MULTILEVEL_SWITCH, "Multilevel Scene Switch (Discontinued)"),
        MOTOR_MULTIPOSITION(3, Generic.MULTILEVEL_SWITCH, "Multiposition Motor"),
        SCENE_SWITCH_MULTILEVEL(4, Generic.MULTILEVEL_SWITCH, "Multilevel Scene Switch"),
        MOTOR_CONTROL_CLASS_A(5, Generic.MULTILEVEL_SWITCH, "Motor Control Class A"),
        MOTOR_CONTROL_CLASS_B(6, Generic.MULTILEVEL_SWITCH, "Motor Control Class B"),
        MOTOR_CONTROL_CLASS_C(7, Generic.MULTILEVEL_SWITCH, "Motor Control Class C"),

        SWITCH_REMOTE_BINARY(1, Generic.REMOTE_SWITCH, "Binary Remote Switch"),
        SWITCH_REMOTE_MULTILEVEL(2, Generic.REMOTE_SWITCH, "Multilevel Remote Switch"),
        SWITCH_REMOTE_TOGGLE_BINARY(3, Generic.REMOTE_SWITCH, "Binary Toggle Remote Switch"),
        SWITCH_REMOTE_TOGGLE_MULTILEVEL(4, Generic.REMOTE_SWITCH, "Multilevel Toggle Remote Switch"),

        SWITCH_REMOTE2_MULTILEVEL(1, Generic.REMOTE_SWITCH_2, "Multilevel Remote Switch"),

        SWITCH_TOGGLE_BINARY(1, Generic.TOGGLE_SWITCH, "Binary Toggle Switch"),
        SWITCH_TOGGLE_MULTILEVEL(2, Generic.TOGGLE_SWITCH, "Multilevel Toggle Switch"),

        Z_IP_TUNNELING_GATEWAY(1, Generic.Z_IP_GATEWAY, "Z/IP Tunneling Gateway"),
        Z_IP_ADVANCED_GATEWAY(2, Generic.Z_IP_GATEWAY, "Z/IP Advanced Gateway"),

        Z_IP_TUNNELING_NODE(1, Generic.Z_IP_NODE, "Z/IP Tunneling Node"),
        Z_IP_ADVANCED_NODE(2, Generic.Z_IP_NODE, "Z/IP Advanced Node"),

        RESIDENTIAL_HEAT_RECOVERY_VENTILATION(1, Generic.VENTILATION, "Residential Heat Recovery Ventilation"),

        ROUTING_SENSOR_BINARY(1, Generic.BINARY_SENSOR, "Routing Binary Sensor"),

        ROUTING_SENSOR_MULTILEVEL(1, Generic.MULTILEVEL_SENSOR, "Routing Multilevel Sensor"),

        SIMPLE_METER(1, Generic.METER, "Simple Meter"),
        ADVANCED_ENERGY_CONTROL(2, Generic.METER, "Advanced Energy Control"),
        WHOLE_HOME_METER_SIMPLE(3, Generic.METER, "Whole Home Meter Simple"),

        NOTIFICATION_SENSOR(1, Generic.SENSOR_NOTIFICATION, "Notification Sensor"),

        DOOR_LOCK(1, Generic.ENTRY_CONTROL, "Door Lock"),
        ADVANCED_DOOR_LOCK(2, Generic.ENTRY_CONTROL, "Advanced Door Lock"),
        SECURE_KEYPAD_DOOR_LOCK(3, Generic.ENTRY_CONTROL, "Secure Keypad Door Lock"),
        SECURE_BARRIER(7, Generic.ENTRY_CONTROL, "Secure Barrier Add-on"),
        SPECIFIC_TYPE_SECURE_LOCKBOX(10, Generic.ENTRY_CONTROL, "SPECIFIC_TYPE_SECURE_LOCKBOX"),

        ENERGY_PRODUCTION(1, Generic.SEMI_INTEROPERABLE, "Energy Production"),

        ALARM_SENSOR_ROUTING_BASIC(1, Generic.ALARM_SENSOR, "Basic Routing Alarm Sensor"),
        ALARM_SENSOR_ROUTING(2, Generic.ALARM_SENSOR, "Routing Alarm Sensor"),
        ALARM_SENSOR_ZENSOR_BASIC(3, Generic.ALARM_SENSOR, "Basic Zensor Alarm Sensor"),
        ALARM_SENSOR_ZENSOR(4, Generic.ALARM_SENSOR, "Zensor Alarm Sensor"),
        ALARM_SENSOR_ZENSOR_ADVANCED(5, Generic.ALARM_SENSOR, "Advanced Zensor Alarm Sensor"),
        SMOKE_SENSOR_ROUTING_BASIC(6, Generic.ALARM_SENSOR, "Basic Routing Smoke Sensor"),
        SMOKE_SENSOR_ROUTING(7, Generic.ALARM_SENSOR, "Routing Smoke Sensor"),
        SMOKE_SENSOR_ZENSOR_BASIC(8, Generic.ALARM_SENSOR, "Basic Zensor Smoke Sensor"),
        SMOKE_SENSOR_ZENSOR(9, Generic.ALARM_SENSOR, "Zensor Smoke Sensor"),
        SMOKE_SENSOR_ZENSOR_ADVANCED(10, Generic.ALARM_SENSOR, "Advanced Zensor Smoke Sensor");

        /**
         * A mapping between the integer code and its corresponding Generic Device class to facilitate lookup by code.
         */
        private static Map<Generic, Map<Integer, Specific>> codeToSpecificMapping;

        private int key;
        private Generic genericDeviceClass;
        private String label;

        private Specific(int key, Generic genericDeviceClass, String label) {
            this.key = key;
            this.label = label;
            this.genericDeviceClass = genericDeviceClass;
        }

        private static void initMapping() {
            codeToSpecificMapping = new HashMap<Generic, Map<Integer, Specific>>();
            for (Specific s : values()) {
                if (!codeToSpecificMapping.containsKey(s.genericDeviceClass)) {
                    codeToSpecificMapping.put(s.genericDeviceClass, new HashMap<Integer, Specific>());
                }
                codeToSpecificMapping.get(s.genericDeviceClass).put(s.key, s);
            }
        }

        /**
         * Lookup function based on the generic device class and the specific device class code.
         * Returns null if the code does not exist.
         *
         * @param genericDeviceClass the generic device class
         * @param i the specific device class code
         * @return the Specific enumeration
         */
        public static Specific getSpecific(Generic genericDeviceClass, int i) {
            if (codeToSpecificMapping == null) {
                initMapping();
            }
            // special case for SPECIFIC_TYPE_NOT_USED. It's valid for all
            // generic classes (and bound to NOT_KNOWN).
            if (i == 0) {
                return codeToSpecificMapping.get(Generic.NOT_KNOWN).get(i);
            }

            if (!codeToSpecificMapping.containsKey(genericDeviceClass)) {
                return null;
            }

            return codeToSpecificMapping.get(genericDeviceClass).get(i);
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

        /**
         * Get the mandatory command classes for this device class.
         *
         * @return the mandatory command classes.
         */
        public CommandClass[] getMandatoryCommandClasses() {
            switch (this) {
                case SIREN_SWITCH_BINARY:
                    return new CommandClass[] { CommandClass.MANUFACTURER_SPECIFIC };
                case NOT_USED:
                case PORTABLE_REMOTE_CONTROLLER:
                case PC_CONTROLLER:
                case BASIC_REPEATER_SLAVE:
                case SWITCH_REMOTE_BINARY:
                case SWITCH_REMOTE_MULTILEVEL:
                case SWITCH_REMOTE_TOGGLE_BINARY:
                case SWITCH_REMOTE_TOGGLE_MULTILEVEL:
                case ROUTING_SENSOR_BINARY: // In the documentation Binary Sensor command class is specified, but this
                                            // is already on the generic class...
                case ROUTING_SENSOR_MULTILEVEL: // In the documentation Multilevel Sensor command class is specified,
                                                // but this is already on the generic class...
                case DOOR_LOCK:
                case THERMOSTAT_HEATING:
                    return new CommandClass[0];
                case PORTABLE_SCENE_CONTROLLER:
                case SCENE_CONTROLLER:
                    return new CommandClass[] { CommandClass.ASSOCIATION, CommandClass.SCENE_CONTROLLER_CONF,
                            CommandClass.MANUFACTURER_SPECIFIC };
                case POWER_SWITCH_BINARY:
                case POWER_SWITCH_MULTILEVEL:
                    return new CommandClass[0];
                case SCENE_SWITCH_BINARY:
                case SCENE_SWITCH_BINARY_DISCONTINUED:
                case SCENE_SWITCH_MULTILEVEL:
                case SCENE_SWITCH_MULTILEVEL_DISCONTINUED:
                    return new CommandClass[] { CommandClass.SCENE_ACTIVATION, CommandClass.SCENE_ACTUATOR_CONF,
                            CommandClass.MANUFACTURER_SPECIFIC };
                case MOTOR_MULTIPOSITION:
                    return new CommandClass[] { CommandClass.VERSION, CommandClass.MANUFACTURER_SPECIFIC };
                case MOTOR_CONTROL_CLASS_A:
                case MOTOR_CONTROL_CLASS_B:
                case MOTOR_CONTROL_CLASS_C:
                    return new CommandClass[] { CommandClass.SWITCH_BINARY, CommandClass.VERSION,
                            CommandClass.MANUFACTURER_SPECIFIC };
                case SWITCH_TOGGLE_BINARY:
                    return new CommandClass[] { CommandClass.SWITCH_TOGGLE_BINARY };
                case SWITCH_TOGGLE_MULTILEVEL:
                    return new CommandClass[] { CommandClass.SWITCH_TOGGLE_MULTILEVEL };
                case ENERGY_PRODUCTION:
                    return new CommandClass[] { CommandClass.ENERGY_PRODUCTION };
                case SIMPLE_WINDOW_COVERING:
                    return new CommandClass[] { CommandClass.BASIC_WINDOW_COVERING };
                case THERMOSTAT_GENERAL:
                    return new CommandClass[] { CommandClass.MANUFACTURER_SPECIFIC, CommandClass.THERMOSTAT_MODE,
                            CommandClass.THERMOSTAT_SETPOINT };
                case SETBACK_SCHEDULE_THERMOSTAT:
                    // TODO: Battery and Wake Up command classes are mandatory for battery operated setback schedule
                    // thermostats.
                    return new CommandClass[] { CommandClass.CLIMATE_CONTROL_SCHEDULE,
                            CommandClass.MANUFACTURER_SPECIFIC, CommandClass.MULTI_CMD, CommandClass.VERSION };
                case SATELLITE_RECEIVER:
                    return new CommandClass[] { CommandClass.SIMPLE_AV_CONTROL, CommandClass.MANUFACTURER_SPECIFIC,
                            CommandClass.VERSION };
                case SIMPLE_METER:
                    return new CommandClass[] { CommandClass.METER, CommandClass.MANUFACTURER_SPECIFIC,
                            CommandClass.VERSION };
                case ADVANCED_ENERGY_CONTROL:
                    return new CommandClass[] { CommandClass.METER_TBL_CONFIG, CommandClass.METER_TBL_MONITOR,
                            CommandClass.MANUFACTURER_SPECIFIC, CommandClass.VERSION };

                case ALARM_SENSOR_ROUTING_BASIC:
                case SMOKE_SENSOR_ROUTING_BASIC:
                    return new CommandClass[] { CommandClass.SENSOR_ALARM, CommandClass.MANUFACTURER_SPECIFIC,
                            CommandClass.ASSOCIATION, CommandClass.VERSION };
                case ALARM_SENSOR_ROUTING:
                case ALARM_SENSOR_ZENSOR_ADVANCED:
                case SMOKE_SENSOR_ROUTING:
                case SMOKE_SENSOR_ZENSOR_ADVANCED:
                    return new CommandClass[] { CommandClass.SENSOR_ALARM, CommandClass.MANUFACTURER_SPECIFIC,
                            CommandClass.BATTERY, CommandClass.ASSOCIATION, CommandClass.VERSION };
                case ALARM_SENSOR_ZENSOR_BASIC:
                case SMOKE_SENSOR_ZENSOR_BASIC:
                    return new CommandClass[] { CommandClass.SENSOR_ALARM, CommandClass.MANUFACTURER_SPECIFIC,
                            CommandClass.VERSION };
                case ALARM_SENSOR_ZENSOR:
                case SMOKE_SENSOR_ZENSOR:
                    return new CommandClass[] { CommandClass.SENSOR_ALARM, CommandClass.MANUFACTURER_SPECIFIC,
                            CommandClass.BATTERY, CommandClass.VERSION };

                case SECURE_KEYPAD_DOOR_LOCK:
                    return new CommandClass[] { CommandClass.SECURITY, CommandClass.MANUFACTURER_SPECIFIC,
                            CommandClass.DOOR_LOCK, CommandClass.USER_CODE, CommandClass.VERSION };

                case SPECIFIC_TYPE_GATEWAY:
                    return new CommandClass[] { CommandClass.VERSION, CommandClass.MANUFACTURER_SPECIFIC,
                            CommandClass.SECURITY };
                default:
                    return new CommandClass[0];
            }
        }
    }

    /*
     * public List<CommandClass> getMandatoryCommandClasses() {
     * Set<CommandClass> classList = new HashSet<CommandClass>();
     *
     * switch (genericDeviceClass) {
     * case REMOTE_CONTROLLER:
     * switch (specificDeviceClass) {
     * case PORTABLE_REMOTE_CONTROLLER:
     * break;
     * case PORTABLE_SCENE_CONTROLLER:
     * classList.add(CommandClass.SCENE_CONTROLLER_CONF);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * classList.add(CommandClass.ASSOCIATION);
     * break;
     * case PORTABLE_INSTALLER_TOOL:
     * classList.add(CommandClass.CONTROLLER_REPLICATION);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * classList.add(CommandClass.VERSION);
     * classList.add(CommandClass.MULTI_CMD);
     * break;
     * default:
     * logger.warn("Device class not handled {} {}", genericDeviceClass, specificDeviceClass);
     * break;
     * }
     * break;
     * case STATIC_CONTROLLER:
     * switch (specificDeviceClass) {
     * case PC_CONTROLLER:
     * break;
     * case SCENE_CONTROLLER:
     * classList.add(CommandClass.SCENE_CONTROLLER_CONF);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * classList.add(CommandClass.ASSOCIATION);
     * break;
     * case INSTALLER_TOOL:
     * classList.add(CommandClass.CONTROLLER_REPLICATION);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * classList.add(CommandClass.VERSION);
     * classList.add(CommandClass.MULTI_CMD);
     * break;
     * default:
     * logger.warn("Device class not handled {} {}", genericDeviceClass, specificDeviceClass);
     * break;
     * }
     * break;
     * case THERMOSTAT:
     * switch (specificDeviceClass) {
     * case THERMOSTAT_HEATING:
     * break;
     * case THERMOSTAT_GENERAL:
     * classList.add(CommandClass.THERMOSTAT_MODE);
     * classList.add(CommandClass.THERMOSTAT_SETPOINT);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * break;
     * case THERMOSTAT_GENERAL_V2:
     * classList.add(CommandClass.THERMOSTAT_MODE);
     * classList.add(CommandClass.THERMOSTAT_SETPOINT);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * classList.add(CommandClass.ASSOCIATION);
     * break;
     * case SETBACK_SCHEDULE_THERMOSTAT:
     * classList.add(CommandClass.CLIMATE_CONTROL_SCHEDULE);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * classList.add(CommandClass.ASSOCIATION);
     * classList.add(CommandClass.MULTI_CMD);
     * break;
     * case SETPOINT_THERMOSTAT:
     * classList.add(CommandClass.THERMOSTAT_SETPOINT);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * classList.add(CommandClass.ASSOCIATION);
     * classList.add(CommandClass.MULTI_CMD);
     * break;
     * case SETBACK_THERMOSTAT:
     * classList.add(CommandClass.THERMOSTAT_MODE);
     * classList.add(CommandClass.THERMOSTAT_SETPOINT);
     * classList.add(CommandClass.THERMOSTAT_SETBACK);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * classList.add(CommandClass.ASSOCIATION);
     * break;
     * default:
     * logger.warn("Device class not handled {} {}", genericDeviceClass, specificDeviceClass);
     * break;
     * }
     * break;
     *
     * case WINDOW_COVERING:
     * switch (specificDeviceClass) {
     * case SIMPLE_WINDOW_COVERING:
     * classList.add(CommandClass.BASIC_WINDOW_COVERING);
     * break;
     * default:
     * logger.warn("Device class not handled {} {}", genericDeviceClass, specificDeviceClass);
     * break;
     * }
     * break;
     * case BINARY_SWITCH:
     * classList.add(CommandClass.SWITCH_BINARY);
     * switch (specificDeviceClass) {
     * case POWER_SWITCH_BINARY:
     * classList.add(CommandClass.SWITCH_ALL);
     * classList.add(CommandClass.MANUFACTURER_SPECIFIC);
     * break;
     * case SCENE_SWITCH_BINARY:
     * classList.add(CommandClass.SCENE_ACTIVATION);
     * classList.add(CommandClass.SCENE_ACTUATOR_CONF);
     * break;
     * case SIREN_SWITCH_BINARY:
     * break;
     * case VALVE_SWITCH_BINARY:
     * break;
     * default:
     * logger.warn("Device class not handled {} {}", genericDeviceClass, specificDeviceClass);
     * break;
     * }
     * break;
     *
     * case GARAGE_DOOR:
     * case REPEATER_SLAVE:
     * case TOGGLE_SWITCH:
     * case REMOTE_SWITCH:
     * case REMOTE_SWITCH_2:
     * case AV_CONTROL_POINT:
     * classList.add(CommandClass.NO_OPERATION);
     * classList.add(CommandClass.BASIC);
     * break;
     *
     * }
     *
     * return classList;
     * }
     */
}
