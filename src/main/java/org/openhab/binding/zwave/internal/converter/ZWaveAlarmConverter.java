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
package org.openhab.binding.zwave.internal.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ZWaveAlarmValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveAlarmConverter class. Converter for communication with the {@link ZWaveAlarmCommandClass}. Implements polling of
 * the alarm status and receiving of alarm events.
 *
 * @author Chris Jackson
 */
public class ZWaveAlarmConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveAlarmConverter.class);

    private static Map<String, Map<NotificationEvent, State>> notifications = new HashMap<String, Map<NotificationEvent, State>>();

    static {
        Map<NotificationEvent, State> events = new HashMap<NotificationEvent, State>();

        // Alarm Burglar
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.HOME_SECURITY__NONE, OnOffType.OFF);
        events.put(NotificationEvent.HOME_SECURITY__INTRUSION, OnOffType.ON);
        events.put(NotificationEvent.HOME_SECURITY__INTRUSION_UNKNOWN, OnOffType.ON);
        notifications.put("alarm_burglar", events);

        // Smoke Alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.SMOKE__NONE, OnOffType.OFF);
        events.put(NotificationEvent.SMOKE__SMOKE_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.SMOKE__SMOKE_DETECTED_UNKNOWN, OnOffType.ON);
        events.put(NotificationEvent.SMOKE__SMOKE_ALARM_TEST, OnOffType.ON);
        notifications.put("alarm_smoke", events);

        // Door alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.ACCESS_CONTROL__NONE, OpenClosedType.CLOSED);
        events.put(NotificationEvent.ACCESS_CONTROL__DOOR_WINDOW_OPEN, OpenClosedType.OPEN);
        events.put(NotificationEvent.ACCESS_CONTROL__DOOR_WINDOW_CLOSED, OpenClosedType.CLOSED);
        notifications.put("sensor_door", events);

        // Entry alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.ACCESS_CONTROL__NONE, OnOffType.ON);
        events.put(NotificationEvent.ACCESS_CONTROL__AUTO_LOCK, OnOffType.ON);
        events.put(NotificationEvent.ACCESS_CONTROL__REMOTE_LOCK, OnOffType.ON);
        events.put(NotificationEvent.ACCESS_CONTROL__KEYPAD_LOCK, OnOffType.ON);
        events.put(NotificationEvent.ACCESS_CONTROL__MANUAL_LOCK, OnOffType.ON);
        events.put(NotificationEvent.ACCESS_CONTROL__REMOTE_UNLOCK, OnOffType.OFF);
        events.put(NotificationEvent.ACCESS_CONTROL__KEYPAD_UNLOCK, OnOffType.OFF);
        events.put(NotificationEvent.ACCESS_CONTROL__MANUAL_UNLOCK, OnOffType.OFF);
        notifications.put("alarm_entry", events);

        // Heat alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.HEAT__NONE, OnOffType.OFF);
        events.put(NotificationEvent.HEAT__HIGH_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.HEAT__HIGH_DETECTED_UNKNOWN, OnOffType.ON);
        notifications.put("alarm_heat", events);

        // Motion alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.HOME_SECURITY__NONE, OnOffType.OFF);
        events.put(NotificationEvent.HOME_SECURITY__MOTION, OnOffType.ON);
        events.put(NotificationEvent.HOME_SECURITY__MOTION_UNKNOWN, OnOffType.ON);
        notifications.put("alarm_motion", events);

        // Flood alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.WATER__NONE, OnOffType.OFF);
        events.put(NotificationEvent.WATER__LEAK_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.WATER__LEAK_DETECTED_UNKNOWN, OnOffType.ON);
        notifications.put("alarm_flood", events);

        // Tamper alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.HOME_SECURITY__NONE, OnOffType.OFF);
        events.put(NotificationEvent.SYSTEM__NONE, OnOffType.OFF);
        events.put(NotificationEvent.HOME_SECURITY__TAMPER, OnOffType.ON);
        events.put(NotificationEvent.HOME_SECURITY__TAMPER_MOVED, OnOffType.ON);
        events.put(NotificationEvent.HOME_SECURITY__TAMPER_INVALID_CODE, OnOffType.ON);
        events.put(NotificationEvent.SYSTEM__TAMPERING, OnOffType.ON);
        notifications.put("alarm_tamper", events);

        // Battery alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.POWER_MANAGEMENT__NONE, OnOffType.OFF);
        events.put(NotificationEvent.POWER_MANAGEMENT__REPLACE_BATTERY_NOW, OnOffType.ON);
        notifications.put("alarm_battery", events);

        // Power alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.POWER_MANAGEMENT__NONE, OnOffType.OFF);
        events.put(NotificationEvent.POWER_MANAGEMENT__MAINS_DISCONNECTED, OnOffType.ON);
        events.put(NotificationEvent.POWER_MANAGEMENT__MAINS_APPLIED, OnOffType.OFF);
        events.put(NotificationEvent.POWER_MANAGEMENT__MAINS_RECONNECTED, OnOffType.OFF);
        notifications.put("alarm_power", events);

        // Carbon Monoxide alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.CO__NONE, OnOffType.OFF);
        events.put(NotificationEvent.CO__CO_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.CO__CO_DETECTED_UNKNOWN, OnOffType.ON);
        events.put(NotificationEvent.CO__CO_ALARM_TEST, OnOffType.ON);
        notifications.put("alarm_co", events);

        // Carbon Dioxide alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.CO2__NONE, OnOffType.OFF);
        events.put(NotificationEvent.CO2__CO2_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.CO2__CO2_DETECTED_UNKNOWN, OnOffType.ON);
        events.put(NotificationEvent.CO2__CO2_ALARM_TEST, OnOffType.ON);
        notifications.put("alarm_co2", events);

        // Combustible gas alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.GAS__NONE, OnOffType.OFF);
        events.put(NotificationEvent.GAS__COMBUSTIBLE_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.GAS__COMBUSTIBLE_DETECTED_UNKNOWN, OnOffType.ON);
        events.put(NotificationEvent.GAS__ALARM_TEST, OnOffType.ON);
        notifications.put("alarm_combustiblegas", events);

        // Emergency alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.EMERGENCY__NONE, OnOffType.OFF);
        events.put(NotificationEvent.EMERGENCY__CONTACT_FIRE, OnOffType.ON);
        events.put(NotificationEvent.EMERGENCY__CONTACT_POLICE, OnOffType.ON);
        events.put(NotificationEvent.EMERGENCY__CONTACT_MEDICAL, OnOffType.ON);
        notifications.put("alarm_emergency", events);

        // Siren Notification
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.SIREN__NONE, OnOffType.OFF);
        events.put(NotificationEvent.SIREN__ACTIVE, OnOffType.ON);
        notifications.put("notification_siren", events);
    }

    /**
     * Constructor. Creates a new instance of the {@link ZWaveAlarmConverter} class.
     *
     * @param controller the {@link ZWaveController} to use for sending messages.
     */
    public ZWaveAlarmConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveAlarmCommandClass commandClass = (ZWaveAlarmCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_ALARM, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        // Notifications can't be polled for their state
        // This will return an event that allows us to know if the event is configured for pull or push
        // and not the actual state of the event.
        if (commandClass.getVersion() >= 3) {
            return null;
        }

        String alarmType = channel.getArguments().get("type");
        Integer alarmEvent = (channel.getArguments().get("event") == null) ? null
                : Integer.parseInt(channel.getArguments().get("event"));
        logger.debug("NODE {}: Generating poll message for {}, endpoint {}, alarm {}, event {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint(), alarmType, alarmEvent);

        ZWaveCommandClassTransactionPayload transaction;
        if (alarmType != null) {
            transaction = commandClass.getMessage(AlarmType.valueOf(alarmType), alarmEvent);
        } else {
            transaction = commandClass.getValueMessage();
        }

        if (transaction == null) {
            return null;
        }

        transaction = node.encapsulate(transaction, channel.getEndpoint());

        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>();
        response.add(transaction);
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String alarmType = channel.getArguments().get("type");
        // Integer alarmEvent = (channel.getArguments().get("event") == null) ? null
        // : Integer.parseInt(channel.getArguments().get("event"));

        ZWaveAlarmValueEvent eventAlarm = (ZWaveAlarmValueEvent) event;
        logger.debug("NODE {}: Alarm converter processing {}", eventAlarm.getNodeId(), eventAlarm.getReportType());
        switch (eventAlarm.getReportType()) {
            case ALARM:
                return handleAlarmReport(channel, eventAlarm, alarmType);
            case NOTIFICATION:
                return handleNotificationReport(channel, eventAlarm);
        }

        return null;
    }

    private State handleAlarmReport(ZWaveThingChannel channel, ZWaveAlarmValueEvent eventAlarm, String alarmType) {
        // Don't trigger event if this item is bound to another alarm type
        if (alarmType != null && AlarmType.valueOf(alarmType) != eventAlarm.getAlarmType()) {
            return null;
        }

        // Check if an event is specified
        if (channel.getArguments().get("event") != null
                && eventAlarm.getAlarmEvent() != Integer.parseInt(channel.getArguments().get("event"))) {
            return null;
        }

        // Processing for special channel types
        if (channel.getUID().getId().equals("alarm_number")) {
            return new DecimalType(eventAlarm.getV1AlarmCode());
        }
        if (channel.getUID().getId().equals("alarm_raw")) {
            Map<String, Object> object = new HashMap<String, Object>();
            object.put("type", eventAlarm.getV1AlarmCode());
            object.put("value", eventAlarm.getValue());
            return new StringType(propertiesToJson(object));
        }

        // Default to using the value.
        int value = eventAlarm.getValue();

        State state = null;
        switch (channel.getDataType()) {
            case DecimalType:
                state = new DecimalType(value);
                break;
            case OnOffType:
                state = value == 0 ? OnOffType.OFF : OnOffType.ON;
                break;
            case OpenClosedType:
                state = value == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                break;
            default:
                logger.debug("NODE {}: No conversion in {} to {}", eventAlarm.getNodeId(), getClass().getSimpleName(),
                        channel.getDataType());
                break;
        }
        return state;
    }

    private State handleNotificationReport(ZWaveThingChannel channel, ZWaveAlarmValueEvent eventAlarm) {

        // Don't trigger event if this item is bound to another event type
        // if (alarmType != null && AlarmType.valueOf(alarmType) != eventAlarm.getAlarmType()) {
        // return null;
        // }

        NotificationEvent notification = NotificationEvent.getEvent(eventAlarm.getAlarmType().toString(),
                eventAlarm.getAlarmEvent());

        // Handle event 0 as 'clear the event'
        int event = eventAlarm.getAlarmEvent();// == 0 ? 0 : eventAlarm.getAlarmStatus();
        logger.debug("NODE {}: Alarm converter NOTIFICATION event is {}, type {}", eventAlarm.getNodeId(), event,
                channel.getDataType());
        if (notification == null) {
            logger.debug("NODE {}: Alarm converter NOTIFICATION event has no notification for {}",
                    eventAlarm.getNodeId(), eventAlarm.getAlarmType().toString());
            return null;
        }

        // We ignore event 0xFE as that indicates "no further events",
        // and event 0xFF which indicates that unsolicited events are enabled
        if (event == 0xFE || event == 0xFF) {
            return null;
        }

        // Don't trigger event if there is no event match. Note that 0 is always acceptable
        // if (alarmEvent != null && event != 0 && alarmEvent != event) {
        // return null;
        // }

        String channelType = channel.getChannelTypeUID().getId();
        switch (channelType) {
            case "alarm_raw":
                Map<String, Object> object = new HashMap<String, Object>();
                object.put("type", eventAlarm.getAlarmType());
                object.put("event", eventAlarm.getAlarmEvent());
                object.put("status", eventAlarm.getAlarmStatus());
                object.put("level", eventAlarm.getV1AlarmLevel());

                NotificationEvent notificationEvent = NotificationEvent.getEvent(eventAlarm.getAlarmType().toString(),
                        eventAlarm.getAlarmEvent());
                if (notificationEvent != null) {
                    object.put("notification", notificationEvent.toString());
                    if (eventAlarm.getParameters() != null) {

                        // Include any parameters present.
                        // Expand array since propertiesToJson does not support arrays.
                        int[] params = eventAlarm.getParameters();
                        for (int i = 0; i < params.length; i++) {
                            object.put("parameter-" + (i + 1), params[i]);
                        }

                        // Special cases kept for backwards compatibility.
                        switch (notificationEvent) {
                            case ACCESS_CONTROL__KEYPAD_LOCK:
                            case ACCESS_CONTROL__KEYPAD_UNLOCK:
                                object.put("code", eventAlarm.getParameters()[0]);
                                break;
                            default:
                                break;
                        }
                    }
                }

                return new StringType(propertiesToJson(object));
            case "alarm_number":
            case "notification_access_control":
            case "notification_power_management":
                return new DecimalType(eventAlarm.getAlarmEvent());
            default:
                Map<NotificationEvent, State> events = notifications.get(channelType);
                if (events == null) {
                    logger.debug("NODE {}: Alarm converter NOTIFICATION event is {}, channel {} is not implemented.",
                            eventAlarm.getNodeId(), event, channelType);
                    return null;
                }
                return events.get(notification);
        }
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        ZWaveAlarmCommandClass commandClass = (ZWaveAlarmCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_ALARM, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        String eventString = channel.getArguments().get("event" + command.toString());
        if (eventString == null) {
            logger.debug("NODE {}: No event found with name 'event{}'", node.getNodeId(), command.toString());
            return null;
        }
        String splits[] = eventString.split(":");
        if (splits.length != 2) {
            logger.debug("NODE {}: Incorrectly formatted event found with name 'event{}' = {}", node.getNodeId(),
                    command.toString(), eventString);
            return null;
        }

        AlarmType notificationType = AlarmType.valueOf(splits[0]);
        int event = Integer.valueOf(splits[1]);

        ZWaveCommandClassTransactionPayload transaction = node
                .encapsulate(commandClass.getNotificationReportMessage(notificationType, event), channel.getEndpoint());

        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>();
        response.add(transaction);
        return response;
    }

    private enum NotificationEvent {
        SMOKE__NONE("SMOKE", 0),
        SMOKE__SMOKE_DETECTED("SMOKE", 1),
        SMOKE__SMOKE_DETECTED_UNKNOWN("SMOKE", 2),
        SMOKE__SMOKE_ALARM_TEST("SMOKE", 3),
        SMOKE__REPLACE("SMOKE", 4),

        CO__NONE("CARBON_MONOXIDE", 0),
        CO__CO_DETECTED("CARBON_MONOXIDE", 1),
        CO__CO_DETECTED_UNKNOWN("CARBON_MONOXIDE", 2),
        CO__CO_ALARM_TEST("CARBON_MONOXIDE", 3),
        CO__REPLACE("CARBON_MONOXIDE", 4),

        CO2__NONE("CARBON_DIOXIDE", 0),
        CO2__CO2_DETECTED("CARBON_DIOXIDE", 1),
        CO2__CO2_DETECTED_UNKNOWN("CARBON_DIOXIDE", 2),
        CO2__CO2_ALARM_TEST("CARBON_DIOXIDE", 3),
        CO2__REPLACE("CARBON_DIOXIDE", 4),

        HEAT__NONE("HEAT", 0),
        HEAT__HIGH_DETECTED("HEAT", 1),
        HEAT__HIGH_DETECTED_UNKNOWN("HEAT", 2),
        HEAT__RAPID_RISE("HEAT", 3),
        HEAT__RAPID_RISE_UNKNOWN("HEAT", 4),
        HEAT__LOW_DETECTED("HEAT", 5),
        HEAT__LOW_DETECTED_UNKNOWN("HEAT", 5),

        WATER__NONE("FLOOD", 0),
        WATER__LEAK_DETECTED("FLOOD", 1),
        WATER__LEAK_DETECTED_UNKNOWN("FLOOD", 2),
        WATER__LOW_LEVEL("FLOOD", 3),
        WATER__LOW_LEVEL_UNKNOWN("FLOOD", 4),
        WATER__REPLACE_FILTER("FLOOD", 5),

        ACCESS_CONTROL__NONE("ACCESS_CONTROL", 0),
        ACCESS_CONTROL__MANUAL_LOCK("ACCESS_CONTROL", 1),
        ACCESS_CONTROL__MANUAL_UNLOCK("ACCESS_CONTROL", 2),
        ACCESS_CONTROL__REMOTE_LOCK("ACCESS_CONTROL", 3),
        ACCESS_CONTROL__REMOTE_UNLOCK("ACCESS_CONTROL", 4),
        ACCESS_CONTROL__KEYPAD_LOCK("ACCESS_CONTROL", 5),
        ACCESS_CONTROL__KEYPAD_UNLOCK("ACCESS_CONTROL", 6),
        ACCESS_CONTROL__MANUAL_NOT_FULLY_LOCKED("ACCESS_CONTROL", 7),
        ACCESS_CONTROL__REMOTE_NOT_FULLY_LOCKED("ACCESS_CONTROL", 8),
        ACCESS_CONTROL__AUTO_LOCK("ACCESS_CONTROL", 9),
        ACCESS_CONTROL__AUTO_NOT_FULLY_LOCKED("ACCESS_CONTROL", 10),
        ACCESS_CONTROL__LOCK_JAMMED("ACCESS_CONTROL", 11),
        ACCESS_CONTROL__ALL_USER_CODES_DELETED("ACCESS_CONTROL", 12),
        ACCESS_CONTROL__USER_CODE_DELETED("ACCESS_CONTROL", 13),
        ACCESS_CONTROL__USER_CODE_ADDED("ACCESS_CONTROL", 14),
        ACCESS_CONTROL__USER_CODE_DUPLICATE("ACCESS_CONTROL", 15),
        ACCESS_CONTROL__KEYPAD_TEMPORARILY_DISABLED("ACCESS_CONTROL", 16),
        ACCESS_CONTROL__KEYPAD_BUSY("ACCESS_CONTROL", 17),
        ACCESS_CONTROL__ADMIN_CODE_ADDED("ACCESS_CONTROL", 18),
        ACCESS_CONTROL__MANUAL_CODE_TOO_LONG("ACCESS_CONTROL", 19),
        ACCESS_CONTROL__REMOTE_UNLOCK_CODE_INVALID("ACCESS_CONTROL", 20),
        ACCESS_CONTROL__REMOTE_LOCK_CODE_INVALID("ACCESS_CONTROL", 21),
        ACCESS_CONTROL__DOOR_WINDOW_OPEN("ACCESS_CONTROL", 22),
        ACCESS_CONTROL__DOOR_WINDOW_CLOSED("ACCESS_CONTROL", 23),
        ACCESS_CONTROL__BARRIER_INITIALISING("ACCESS_CONTROL", 64),
        ACCESS_CONTROL__BARRIER_FORCE_EXCEEDED("ACCESS_CONTROL", 65),
        ACCESS_CONTROL__BARRIER_MOTOR_TIME_EXCEEDED("ACCESS_CONTROL", 66),
        ACCESS_CONTROL__BARRIER_MECHANICAL_LIMIT("ACCESS_CONTROL", 67),
        ACCESS_CONTROL__BARRIER_ERROR_UL_REQUIREMENT("ACCESS_CONTROL", 68),
        ACCESS_CONTROL__BARRIER_DISABLED_UL_REQUIREMENT("ACCESS_CONTROL", 69),
        ACCESS_CONTROL__BARRIER_MALFUNCTION("ACCESS_CONTROL", 70),
        ACCESS_CONTROL__BARRIER_VACATION_MODE("ACCESS_CONTROL", 71),
        ACCESS_CONTROL__BARRIER_SAFETY_OBSTICAL("ACCESS_CONTROL", 72),
        ACCESS_CONTROL__BARRIER_SUPERVISORY_ERROR("ACCESS_CONTROL", 73),
        ACCESS_CONTROL__BARRIER_SENSOR_BATTERY_LOW("ACCESS_CONTROL", 74),
        ACCESS_CONTROL__BARRIER_SHORT_DETECTED("ACCESS_CONTROL", 75),
        ACCESS_CONTROL__BARRIER_NON_ZWAVE("ACCESS_CONTROL", 76),

        HOME_SECURITY__NONE("BURGLAR", 0),
        HOME_SECURITY__INTRUSION("BURGLAR", 1),
        HOME_SECURITY__INTRUSION_UNKNOWN("BURGLAR", 2),
        HOME_SECURITY__TAMPER("BURGLAR", 3),
        HOME_SECURITY__TAMPER_INVALID_CODE("BURGLAR", 4),
        HOME_SECURITY__GLASS_BREAK("BURGLAR", 5),
        HOME_SECURITY__GLASS_BREAK_UNKNOWN("BURGLAR", 6),
        HOME_SECURITY__MOTION("BURGLAR", 7),
        HOME_SECURITY__MOTION_UNKNOWN("BURGLAR", 8),
        HOME_SECURITY__TAMPER_MOVED("BURGLAR", 9),

        POWER_MANAGEMENT__NONE("POWER_MANAGEMENT", 0),
        POWER_MANAGEMENT__MAINS_APPLIED("POWER_MANAGEMENT", 1),
        POWER_MANAGEMENT__MAINS_DISCONNECTED("POWER_MANAGEMENT", 2),
        POWER_MANAGEMENT__MAINS_RECONNECTED("POWER_MANAGEMENT", 3),
        POWER_MANAGEMENT__SURGE_DETECTED("POWER_MANAGEMENT", 4),
        POWER_MANAGEMENT__VOLTAGE_DROP("POWER_MANAGEMENT", 5),
        POWER_MANAGEMENT__OVER_CURRENT("POWER_MANAGEMENT", 6),
        POWER_MANAGEMENT__OVER_VOLTAGE("POWER_MANAGEMENT", 7),
        POWER_MANAGEMENT__OVER_LOAD("POWER_MANAGEMENT", 8),
        POWER_MANAGEMENT__LOAD_ERROR("POWER_MANAGEMENT", 9),
        POWER_MANAGEMENT__REPLACE_BATTERY_SOON("POWER_MANAGEMENT", 10),
        POWER_MANAGEMENT__REPLACE_BATTERY_NOW("POWER_MANAGEMENT", 11),
        POWER_MANAGEMENT__BATTERY_CHARGING("POWER_MANAGEMENT", 12),
        POWER_MANAGEMENT__BATTERY_FULL("POWER_MANAGEMENT", 13),
        POWER_MANAGEMENT__CHARGE_BATTERY_SOON("POWER_MANAGEMENT", 14),
        POWER_MANAGEMENT__CHARGE_BATTERY_NOW("POWER_MANAGEMENT", 15),

        SYSTEM__NONE("SYSTEM", 0),
        SYSTEM__HARDWARE_FAILURE("SYSTEM", 1),
        SYSTEM__SOFTWARE_FAILURE("SYSTEM", 2),
        SYSTEM__HARDWARE_FAILURE_MANUFACTURER_CODE("SYSTEM", 3),
        SYSTEM__SOFTWARE_FAILURE_MANUFACTURER_CODE("SYSTEM", 4),
        SYSTEM__HEARTBEAT("SYSTEM", 5),
        SYSTEM__TAMPERING("SYSTEM", 6),
        SYSTEM__EMERGENCY_SHUTOFF("SYSTEM", 7),

        EMERGENCY__NONE("EMERGENCY", 0),
        EMERGENCY__CONTACT_POLICE("EMERGENCY", 1),
        EMERGENCY__CONTACT_FIRE("EMERGENCY", 2),
        EMERGENCY__CONTACT_MEDICAL("EMERGENCY", 3),

        CLOCK__NONE("CLOCK", 0),
        CLOCK__WAKEUP_ALERT("CLOCK", 1),
        CLOCK__TIMER_ENDED("CLOCK", 2),
        CLOCK__TIME_REMAINING("CLOCK", 3),

        APPLIANCE__NONE("APPLIANCE", 0),
        APPLIANCE__PROGRAM_STARTED("APPLIANCE", 1),
        APPLIANCE__PROGRAM_IN_PROGRESS("APPLIANCE", 2),
        APPLIANCE__PROGRAM_COMPLETE("APPLIANCE", 3),
        APPLIANCE__REPLACE_FILTER("APPLIANCE", 4),
        APPLIANCE__FAILED_TO_REACH_TEMPERATURE("APPLIANCE", 5),
        APPLIANCE__SUPPLYING_WATER("APPLIANCE", 6),
        APPLIANCE__WATER_FAILURE("APPLIANCE", 7),
        APPLIANCE__BOILING("APPLIANCE", 8),
        APPLIANCE__BOILING_FAILURE("APPLIANCE", 9),
        APPLIANCE__WASHING("APPLIANCE", 10),
        APPLIANCE__WASHING_FAILURE("APPLIANCE", 11),
        APPLIANCE__RINSING("APPLIANCE", 12),
        APPLIANCE__RINSING_FAILURE("APPLIANCE", 13),
        APPLIANCE__DRAINING("APPLIANCE", 14),
        APPLIANCE__DRAINING_FAILURE("APPLIANCE", 15),
        APPLIANCE__SPINNING("APPLIANCE", 16),
        APPLIANCE__SPINNING_FAILURE("APPLIANCE", 17),
        APPLIANCE__DRYING("APPLIANCE", 18),
        APPLIANCE__DRYING_FAILURE("APPLIANCE", 19),
        APPLIANCE__FAN_FAILURE("APPLIANCE", 20),
        APPLIANCE__COMPRESSOR_FAILURE("APPLIANCE", 21),

        HOME_HEALTH__NONE("HOME_HEALTH", 0),
        HOME_HEALTH__LEAVING_BED("HOME_HEALTH", 1),
        HOME_HEALTH__SITTING_ON_BED("HOME_HEALTH", 2),
        HOME_HEALTH__LYING_ON_BED("HOME_HEALTH", 3),
        HOME_HEALTH__POSTURE_CHANGED("HOME_HEALTH", 4),
        HOME_HEALTH__SITTING_ON_BED_EDGE("HOME_HEALTH", 5),
        HOME_HEALTH__VOC_LEVEL("HOME_HEALTH", 6),

        SIREN__NONE("SIREN", 0),
        SIREN__ACTIVE("SIREN", 1),

        GAS__NONE("GAS", 0),
        GAS__COMBUSTIBLE_DETECTED("GAS", 1),
        GAS__COMBUSTIBLE_DETECTED_UNKNOWN("GAS", 2),
        GAS__TOXIC_DETECTED("GAS", 3),
        GAS__TOXIC_DETECTED_UNKNOWN("GAS", 4),
        GAS__ALARM_TEST("GAS", 5),
        GAS__MAINTENANCE_REPLACEMENT("GAS", 5);

        private static Map<Integer, NotificationEvent> codeHashMap;

        private String type;
        private int event;

        private NotificationEvent(String type, int event) {
            this.type = type;
            this.event = event;
        }

        private static void initMapping() {
            codeHashMap = new HashMap<Integer, NotificationEvent>();
            for (NotificationEvent notificationEvent : values()) {
                codeHashMap.put(Objects.hash(notificationEvent.getType(), notificationEvent.getEvent()),
                        notificationEvent);
            }
        }

        /**
         * Lookup function based on the notification type and event code.
         * Returns null if the event does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the alarm type.
         */
        public static NotificationEvent getEvent(String type, int event) {
            if (codeHashMap == null) {
                initMapping();
            }

            return codeHashMap.get(Objects.hash(type, event));
        }

        /**
         * @return the notification type
         */
        public String getType() {
            return type;
        }

        public int getEvent() {
            return event;
        }
    }

}
