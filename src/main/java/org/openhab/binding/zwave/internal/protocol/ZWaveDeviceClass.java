/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.zwave.internal.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Z-Wave device class definitions.
 * A Z-Wave device class groups devices with the same functionality together in a class.
 *
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
public class ZWaveDeviceClass {
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
        if (specificDeviceClass.genericDeviceClass != Generic.GENERIC_TYPE_NOT_USED
                && specificDeviceClass.genericDeviceClass != this.genericDeviceClass) {
            throw new IllegalArgumentException("specificDeviceClass");
        }

        this.specificDeviceClass = specificDeviceClass;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((basicDeviceClass == null) ? 0 : basicDeviceClass.hashCode());
        result = prime * result + ((genericDeviceClass == null) ? 0 : genericDeviceClass.hashCode());
        result = prime * result + ((specificDeviceClass == null) ? 0 : specificDeviceClass.hashCode());
        return result;
    }

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
     */

    public enum Basic {
        BASIC_TYPE_UNKNOWN(Integer.MAX_VALUE),
        BASIC_TYPE_CONTROLLER(0x01),
        BASIC_TYPE_ROUTING_SLAVE(0x04),
        BASIC_TYPE_SLAVE(0x03),
        BASIC_TYPE_STATIC_CONTROLLER(0x02);

        /**
         * A mapping between the integer code and its corresponding Basic device
         * class to facilitate lookup by code.
         */
        private static Map<Integer, Basic> codeToBasicMapping;

        private int key;

        private Basic(int key) {
            this.key = key;
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
    }

    /**
     * Z-Wave Generic Device Class enumeration. The Generic Device Class describes functionality of a device in the
     * Network. Generic Device Classes can have Command Classes that are mandatory or recommended for all devices that
     * belong to this device class. Generic device class do not relate directly to Basic Device Classes. E.G. a
     * BINARY_SWITCH can be a ROUTING_SLAVE or a SLAVE.
     */
    public enum Generic {
        GENERIC_TYPE_NOT_USED(0x00),
        GENERIC_TYPE_AV_CONTROL_POINT(0x03),
        GENERIC_TYPE_DISPLAY(0x04),
        GENERIC_TYPE_ENTRY_CONTROL(0x40),
        GENERIC_TYPE_GENERIC_CONTROLLER(0x01),
        GENERIC_TYPE_METER(0x31),
        GENERIC_TYPE_METER_PULSE(0x30),
        GENERIC_TYPE_NON_INTEROPERABLE(0xFF),
        GENERIC_TYPE_REPEATER_SLAVE(0x0F),
        GENERIC_TYPE_SECURITY_PANEL(0x17),
        GENERIC_TYPE_SEMI_INTEROPERABLE(0x50),
        GENERIC_TYPE_SENSOR_ALARM(0xA1),
        GENERIC_TYPE_SENSOR_BINARY(0x20),
        GENERIC_TYPE_SENSOR_MULTILEVEL(0x21),
        GENERIC_TYPE_STATIC_CONTROLLER(0x02),
        GENERIC_TYPE_SWITCH_BINARY(0x10),
        GENERIC_TYPE_SWITCH_MULTILEVEL(0x11),
        GENERIC_TYPE_SWITCH_REMOTE(0x12),
        GENERIC_TYPE_SWITCH_TOGGLE(0x13),
        GENERIC_TYPE_THERMOSTAT(0x08),
        GENERIC_TYPE_VENTILATION(0x16),
        GENERIC_TYPE_WINDOW_COVERING(0x09),
        GENERIC_TYPE_ZIP_NODE(0x15),
        GENERIC_TYPE_WALL_CONTROLLER(0x18),
        GENERIC_TYPE_NETWORK_EXTENDER(0x05),
        GENERIC_TYPE_APPLIANCE(0x06),
        GENERIC_TYPE_SENSOR_NOTIFICATION(0x07);

        /**
         * A mapping between the integer code and its corresponding Generic
         * Device class to facilitate lookup by code.
         */
        private static Map<Integer, Generic> codeToGenericMapping;

        private int key;

        private Generic(int key) {
            this.key = key;
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
    }

    public enum Specific {
        SPECIFIC_TYPE_NOT_USED(Generic.GENERIC_TYPE_NOT_USED, 0x00),
        SPECIFIC_TYPE_DOORBELL(Generic.GENERIC_TYPE_AV_CONTROL_POINT, 0x12),
        SPECIFIC_TYPE_SOUND_SWITCH(Generic.GENERIC_TYPE_AV_CONTROL_POINT, 0x01),
        SPECIFIC_TYPE_SATELLITE_RECEIVER(Generic.GENERIC_TYPE_AV_CONTROL_POINT, 0x04),
        SPECIFIC_TYPE_SATELLITE_RECEIVER_V2(Generic.GENERIC_TYPE_AV_CONTROL_POINT, 0x11),
        SPECIFIC_TYPE_SIMPLE_DISPLAY(Generic.GENERIC_TYPE_DISPLAY, 0x01),
        SPECIFIC_TYPE_DOOR_LOCK(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x01),
        SPECIFIC_TYPE_ADVANCED_DOOR_LOCK(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x02),
        SPECIFIC_TYPE_SECURE_KEYPAD_DOOR_LOCK(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x03),
        SPECIFIC_TYPE_SECURE_KEYPAD_DOOR_LOCK_DEADBOLT(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x04),
        SPECIFIC_TYPE_SECURE_DOOR(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x05),
        SPECIFIC_TYPE_SECURE_GATE(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x06),
        SPECIFIC_TYPE_SECURE_BARRIER_ADDON(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x07),
        SPECIFIC_TYPE_SECURE_BARRIER_OPEN_ONLY(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x08),
        SPECIFIC_TYPE_SECURE_BARRIER_CLOSE_ONLY(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x09),
        SPECIFIC_TYPE_SECURE_LOCKBOX(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x0A),
        SPECIFIC_TYPE_SECURE_KEYPAD(Generic.GENERIC_TYPE_ENTRY_CONTROL, 0x0B),
        SPECIFIC_TYPE_PORTABLE_REMOTE_CONTROLLER(Generic.GENERIC_TYPE_GENERIC_CONTROLLER, 0x01),
        SPECIFIC_TYPE_PORTABLE_SCENE_CONTROLLER(Generic.GENERIC_TYPE_GENERIC_CONTROLLER, 0x02),
        SPECIFIC_TYPE_PORTABLE_INSTALLER_TOOL(Generic.GENERIC_TYPE_GENERIC_CONTROLLER, 0x03),
        SPECIFIC_TYPE_REMOTE_CONTROL_AV(Generic.GENERIC_TYPE_GENERIC_CONTROLLER, 0x04),
        SPECIFIC_TYPE_REMOTE_CONTROL_SIMPLE(Generic.GENERIC_TYPE_GENERIC_CONTROLLER, 0x06),
        SPECIFIC_TYPE_SIMPLE_METER(Generic.GENERIC_TYPE_METER, 0x01),
        SPECIFIC_TYPE_ADV_ENERGY_CONTROL(Generic.GENERIC_TYPE_METER, 0x02),
        SPECIFIC_TYPE_WHOLE_HOME_METER_SIMPLE(Generic.GENERIC_TYPE_METER, 0x03),
        SPECIFIC_TYPE_REPEATER_SLAVE(Generic.GENERIC_TYPE_REPEATER_SLAVE, 0x01),
        SPECIFIC_TYPE_VIRTUAL_NODE(Generic.GENERIC_TYPE_REPEATER_SLAVE, 0x02),
        SPECIFIC_TYPE_ZONED_SECURITY_PANEL(Generic.GENERIC_TYPE_SECURITY_PANEL, 0x01),
        SPECIFIC_TYPE_ENERGY_PRODUCTION(Generic.GENERIC_TYPE_SEMI_INTEROPERABLE, 0x01),
        SPECIFIC_TYPE_ADV_ZENSOR_NET_ALARM_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x05),
        SPECIFIC_TYPE_ADV_ZENSOR_NET_SMOKE_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x0A),
        SPECIFIC_TYPE_BASIC_ROUTING_ALARM_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x01),
        SPECIFIC_TYPE_BASIC_ROUTING_SMOKE_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x06),
        SPECIFIC_TYPE_BASIC_ZENSOR_NET_ALARM_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x03),
        SPECIFIC_TYPE_BASIC_ZENSOR_NET_SMOKE_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x08),
        SPECIFIC_TYPE_ROUTING_ALARM_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x02),
        SPECIFIC_TYPE_ROUTING_SMOKE_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x07),
        SPECIFIC_TYPE_ZENSOR_NET_ALARM_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x04),
        SPECIFIC_TYPE_ZENSOR_NET_SMOKE_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x09),
        SPECIFIC_TYPE_ALARM_SENSOR(Generic.GENERIC_TYPE_SENSOR_ALARM, 0x0B),
        SPECIFIC_TYPE_ROUTING_SENSOR_BINARY(Generic.GENERIC_TYPE_SENSOR_BINARY, 0x01),
        SPECIFIC_TYPE_ROUTING_SENSOR_MULTILEVEL(Generic.GENERIC_TYPE_SENSOR_MULTILEVEL, 0x01),
        SPECIFIC_TYPE_CHIMNEY_FAN(Generic.GENERIC_TYPE_SENSOR_MULTILEVEL, 0x02),
        SPECIFIC_TYPE_PC_CONTROLLER(Generic.GENERIC_TYPE_STATIC_CONTROLLER, 0x01),
        SPECIFIC_TYPE_SCENE_CONTROLLER(Generic.GENERIC_TYPE_STATIC_CONTROLLER, 0x02),
        SPECIFIC_TYPE_STATIC_INSTALLER_TOOL(Generic.GENERIC_TYPE_STATIC_CONTROLLER, 0x03),
        SPECIFIC_TYPE_SET_TOP_BOX(Generic.GENERIC_TYPE_STATIC_CONTROLLER, 0x04),
        SPECIFIC_TYPE_SUB_SYSTEM_CONTROLLER(Generic.GENERIC_TYPE_STATIC_CONTROLLER, 0x05),
        SPECIFIC_TYPE_TV(Generic.GENERIC_TYPE_STATIC_CONTROLLER, 0x06),
        SPECIFIC_TYPE_GATEWAY(Generic.GENERIC_TYPE_STATIC_CONTROLLER, 0x07),
        SPECIFIC_TYPE_POWER_SWITCH_BINARY(Generic.GENERIC_TYPE_SWITCH_BINARY, 0x01),
        SPECIFIC_TYPE_SCENE_SWITCH_BINARY(Generic.GENERIC_TYPE_SWITCH_BINARY, 0x03),
        SPECIFIC_TYPE_POWER_STRIP(Generic.GENERIC_TYPE_SWITCH_BINARY, 0x04),
        SPECIFIC_TYPE_SIREN(Generic.GENERIC_TYPE_SWITCH_BINARY, 0x05),
        SPECIFIC_TYPE_VALVE_OPEN_CLOSE(Generic.GENERIC_TYPE_SWITCH_BINARY, 0x06),
        SPECIFIC_TYPE_COLOR_TUNABLE_BINARY(Generic.GENERIC_TYPE_SWITCH_BINARY, 0x02),
        SPECIFIC_TYPE_IRRIGATION_CONTROLLER(Generic.GENERIC_TYPE_SWITCH_BINARY, 0x07),
        SPECIFIC_TYPE_CLASS_A_MOTOR_CONTROL(Generic.GENERIC_TYPE_SWITCH_MULTILEVEL, 0x05),
        SPECIFIC_TYPE_CLASS_B_MOTOR_CONTROL(Generic.GENERIC_TYPE_SWITCH_MULTILEVEL, 0x06),
        SPECIFIC_TYPE_CLASS_C_MOTOR_CONTROL(Generic.GENERIC_TYPE_SWITCH_MULTILEVEL, 0x07),
        SPECIFIC_TYPE_MOTOR_MULTIPOSITION(Generic.GENERIC_TYPE_SWITCH_MULTILEVEL, 0x03),
        SPECIFIC_TYPE_POWER_SWITCH_MULTILEVEL(Generic.GENERIC_TYPE_SWITCH_MULTILEVEL, 0x01),
        SPECIFIC_TYPE_SCENE_SWITCH_MULTILEVEL(Generic.GENERIC_TYPE_SWITCH_MULTILEVEL, 0x04),
        SPECIFIC_TYPE_FAN_SWITCH(Generic.GENERIC_TYPE_SWITCH_MULTILEVEL, 0x08),
        SPECIFIC_TYPE_COLOR_TUNABLE_MULTILEVEL(Generic.GENERIC_TYPE_SWITCH_MULTILEVEL, 0x02),
        SPECIFIC_TYPE_SWITCH_REMOTE_BINARY(Generic.GENERIC_TYPE_SWITCH_REMOTE, 0x01),
        SPECIFIC_TYPE_SWITCH_REMOTE_MULTILEVEL(Generic.GENERIC_TYPE_SWITCH_REMOTE, 0x02),
        SPECIFIC_TYPE_SWITCH_REMOTE_TOGGLE_BINARY(Generic.GENERIC_TYPE_SWITCH_REMOTE, 0x03),
        SPECIFIC_TYPE_SWITCH_REMOTE_TOGGLE_MULTILEVEL(Generic.GENERIC_TYPE_SWITCH_REMOTE, 0x04),
        SPECIFIC_TYPE_SWITCH_TOGGLE_BINARY(Generic.GENERIC_TYPE_SWITCH_TOGGLE, 0x01),
        SPECIFIC_TYPE_SWITCH_TOGGLE_MULTILEVEL(Generic.GENERIC_TYPE_SWITCH_TOGGLE, 0x02),
        SPECIFIC_TYPE_SETBACK_SCHEDULE_THERMOSTAT(Generic.GENERIC_TYPE_THERMOSTAT, 0x03),
        SPECIFIC_TYPE_SETBACK_THERMOSTAT(Generic.GENERIC_TYPE_THERMOSTAT, 0x05),
        SPECIFIC_TYPE_SETPOINT_THERMOSTAT(Generic.GENERIC_TYPE_THERMOSTAT, 0x04),
        SPECIFIC_TYPE_THERMOSTAT_GENERAL(Generic.GENERIC_TYPE_THERMOSTAT, 0x02),
        SPECIFIC_TYPE_THERMOSTAT_GENERAL_V2(Generic.GENERIC_TYPE_THERMOSTAT, 0x06),
        SPECIFIC_TYPE_THERMOSTAT_HEATING(Generic.GENERIC_TYPE_THERMOSTAT, 0x01),
        SPECIFIC_TYPE_RESIDENTIAL_HRV(Generic.GENERIC_TYPE_VENTILATION, 0x01),
        SPECIFIC_TYPE_SIMPLE_WINDOW_COVERING(Generic.GENERIC_TYPE_WINDOW_COVERING, 0x01),
        SPECIFIC_TYPE_ZIP_ADV_NODE(Generic.GENERIC_TYPE_ZIP_NODE, 0x02),
        SPECIFIC_TYPE_ZIP_TUN_NODE(Generic.GENERIC_TYPE_ZIP_NODE, 0x01),
        SPECIFIC_TYPE_BASIC_WALL_CONTROLLER(Generic.GENERIC_TYPE_WALL_CONTROLLER, 0x01),
        SPECIFIC_TYPE_SECURE_EXTENDER(Generic.GENERIC_TYPE_NETWORK_EXTENDER, 0x01),
        SPECIFIC_TYPE_GENERAL_APPLIANCE(Generic.GENERIC_TYPE_APPLIANCE, 0x01),
        SPECIFIC_TYPE_KITCHEN_APPLIANCE(Generic.GENERIC_TYPE_APPLIANCE, 0x02),
        SPECIFIC_TYPE_LAUNDRY_APPLIANCE(Generic.GENERIC_TYPE_APPLIANCE, 0x03),
        SPECIFIC_TYPE_NOTIFICATION_SENSOR(Generic.GENERIC_TYPE_SENSOR_NOTIFICATION, 0x01);

        /**
         * A mapping between the integer code and its corresponding Generic Device class to facilitate lookup by code.
         */
        private static Map<Generic, Map<Integer, Specific>> codeToSpecificMapping;

        private int key;
        private Generic genericDeviceClass;

        private Specific(Generic genericDeviceClass, int key) {
            this.key = key;
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

            // Special case for SPECIFIC_TYPE_NOT_USED as it's valid for all generic classes.
            if (i == 0) {
                return Specific.SPECIFIC_TYPE_NOT_USED;
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
    }
}
