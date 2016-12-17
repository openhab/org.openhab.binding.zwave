/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import static org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * Z-Wave Plus device type class. Defines the zwave plus device types and the mandatory command classes.
 *
 * @author Jorg de Jong
 */
public class ZWavePlusDeviceClass {

    public enum ZWavePlusDeviceType {
        // @formatter:off
        UNKNOWN_TYPE(0x0000),
        CENTRAL_CONTROLLER(0x0100, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_CRC_16_ENCAP, COMMAND_CLASS_APPLICATION_STATUS),
        DISPLAY_SIMPLE(0x0200, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION),
        DOOR_LOCK_KEYPAD(0x0300, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_DOOR_LOCK, COMMAND_CLASS_USER_CODE, COMMAND_CLASS_BATTERY),
        FAN_SWITCH(0x0400, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SWITCH_MULTILEVEL),
        GATEWAY(0x0500, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_CRC_16_ENCAP, COMMAND_CLASS_MULTI_CHANNEL, COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION, COMMAND_CLASS_WAKE_UP,COMMAND_CLASS_APPLICATION_STATUS),
        LIGHT_DIMMER_SWITCH(0x0600, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SWITCH_MULTILEVEL),
        ON_OFF_POWER_SWITCH(0x0700, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SWITCH_BINARY),
        POWER_STRIP(0x0800, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_MULTI_CHANNEL, COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION, COMMAND_CLASS_SWITCH_BINARY),
        REMOTE_CONTROL_AV(0x0900, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION),
        REMOTE_CONTROL_MULTI_PURPOSE(0x0a00, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION,COMMAND_CLASS_WAKE_UP),
        REMOTE_CONTROL_SIMPLE(0x0b00, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_CENTRAL_SCENE),
        KEY_FOB(0x0b01, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_CENTRAL_SCENE),
        SENSOR_NOTIFICATION(0x0c00, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        SMOKE_ALARM_SENSOR(0x0c01, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        CO_ALARM_SENSOR(0x0c02, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        CO2_ALARM_SENSOR(0x0c03, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        HEAT_ALARM_SENSOR(0x0c04, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        WATER_ALARM_SENSOR(0x0c05, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        ACCESS_CONTROL_SENSOR(0x0c06, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        HOME_SECURITY_SENSOR(0x0c07, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        POWER_MANAGEMENT_SENSOR(0x0c08, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        SYSTEM_SENSOR(0x0c09, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        EMERGENCY_ALARM_SENSOR(0x0c0a, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        CLOCK_SENSOR(0x0c0b, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        MULTIDEVICE_NOTIFICATION_SENSOR(0x0cff, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY),
        MULTILEVEL_SENSOR(0x0d00, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        AIR_TEMPERATURE_SENSOR(0x0d01, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        GENERAL_PURPOSE_SENSOR(0x0d02, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        LUMINANCE_SENSOR(0x0d03, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        POWER_SENSOR(0x0d04, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        HUMIDITY_SENSOR(0x0d05, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        VELOCITY_SENSOR(0x0d06, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        DIRECTION_SENSOR(0x0d07, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        ATMOSPHERIC_PRESSURE_SENSOR(0x0d08, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        BAROMETRIC_PRESSURE_SENSOR(0x0d09, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        SOLAR_RADIATION_SENSOR(0x0d0a, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        DEW_POINT_SENSOR(0x0d0b, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        RAIN_RATE_SENSOR(0x0d0c, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        TIDE_LEVEL_SENSOR(0x0d0d, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        WEIGHT_SENSOR(0x0d0e, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        VOLTAGE_SENSOR(0x0d0f, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        CURRENT_SENSOR(0x0d10, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        CO2_LEVEL_SENSOR(0x0d11, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        AIR_FLOW_SENSOR(0x0d12, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        TANK_CAPACITY_SENSOR(0x0d13, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        DISTANCE_SENSOR(0x0d14, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        ANGLE_POSITION_SENSOR(0x0d15, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        ROTATION_SENSOR(0x0d16, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        WATER_TEMPERATURE_SENSOR(0x0d17, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        SOIL_TEMPERATURE_SENSOR(0x0d18, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        SEISMIC_INTENSITY_SENSOR(0x0d19, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        SEISMIC_MAGNITUDE_SENSOR(0x0d1a, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        ULTRAVIOLET_SENSOR(0x0d1b, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        ELECTRICAL_RESISTIVITY_SENSOR(0x0d1c, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        ELECTRICAL_CONDUCTIVITY_SENSOR(0x0b1d, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        LOUDNESS_SENSOR(0x0b1e, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        MOISTURE_SENSOR(0x0b1f, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        FREQUENCY_SENSOR(0x0b20, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        TIME_SENSOR(0x0b21, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        TARGET_TEMPERATURE_SENSOR(0x0b22, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        MULTIDEVICE_SENSOR(0x0bff, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_SENSOR_MULTILEVEL),
        SET_TOP_BOX(0x0e00, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_CRC_16_ENCAP, COMMAND_CLASS_MULTI_CHANNEL, COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION, COMMAND_CLASS_WAKE_UP, COMMAND_CLASS_APPLICATION_STATUS),
        SIREN(0x0f00, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION),
        SUB_ENERGY_METER(0x1000, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_CRC_16_ENCAP, COMMAND_CLASS_METER),
        SUB_SYSTEM_CONTROLLER(0x1100, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_MULTI_CHANNEL, COMMAND_CLASS_WAKE_UP, COMMAND_CLASS_APPLICATION_STATUS),
        THERMOSTAT_HVAC(0x1200, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_THERMOSTAT_SETPOINT, COMMAND_CLASS_THERMOSTAT_MODE),
        THERMOSTAT_SETBACK(0x1300, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_THERMOSTAT_SETPOINT),
        TV(0x1400, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_CRC_16_ENCAP, COMMAND_CLASS_MULTI_CHANNEL, COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION, COMMAND_CLASS_WAKE_UP, COMMAND_CLASS_APPLICATION_STATUS),
        VALVE_OPEN_CLOSE(0x1500, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SWITCH_MULTILEVEL, COMMAND_CLASS_SWITCH_BINARY),
        WALL_CONTROLLER(0x1600, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_CENTRAL_SCENE),
        WHOLE_HOME_METER_SIMPLE(0x1700, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_CRC_16_ENCAP, COMMAND_CLASS_METER),
        WINDOW_COVERING_NO_POSITION_ENDPOINT(0x1800, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION,COMMAND_CLASS_VERSION, COMMAND_CLASS_SWITCH_MULTILEVEL, COMMAND_CLASS_SWITCH_BINARY),
        WINDOW_COVERING_ENDPOINT_AWARE(0x1900, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SWITCH_MULTILEVEL, COMMAND_CLASS_SWITCH_BINARY),
        WINDOW_COVERING_POSITION_ENDPOINT_AWARE(0x1a00, COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_ZWAVEPLUS_INFO, COMMAND_CLASS_ASSOCIATION_GRP_INFO, COMMAND_CLASS_MANUFACTURER_SPECIFIC, COMMAND_CLASS_POWERLEVEL, COMMAND_CLASS_ASSOCIATION, COMMAND_CLASS_VERSION, COMMAND_CLASS_SWITCH_MULTILEVEL, COMMAND_CLASS_SWITCH_BINARY);
        // @formatter:on

        private static Map<Integer, ZWavePlusDeviceType> zwavePlusDeviceTypes = new HashMap<>();

        private int key;
        private Set<CommandClass> mandatoryCommandClasses = new HashSet<>();

        ZWavePlusDeviceType(int key, CommandClass... classes) {
            this.key = key;
            mandatoryCommandClasses.addAll(Arrays.asList(classes));
        }

        public int getKey() {
            return key;
        }

        public Set<CommandClass> getMandatoryCommandClasses() {
            return mandatoryCommandClasses;
        }

        private static void setup() {
            for (ZWavePlusDeviceType type : ZWavePlusDeviceType.values()) {
                zwavePlusDeviceTypes.put(type.getKey(), type);
            }
        }

        /**
         * Lookup the zwavePlus Device info.
         *
         * @param zwPlusDeviceType the device type
         * @return the device info if available
         */
        public static ZWavePlusDeviceType getZWavePlusDeviceType(int zwPlusDeviceType) {
            if (zwavePlusDeviceTypes.isEmpty()) {
                setup();
            }
            return zwavePlusDeviceTypes.get(zwPlusDeviceType);
        }
    }
}
