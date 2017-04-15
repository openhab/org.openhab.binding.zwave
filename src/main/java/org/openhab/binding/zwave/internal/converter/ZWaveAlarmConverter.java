/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    private static final Logger logger = LoggerFactory.getLogger(ZWaveAlarmConverter.class);

    private static Map<String, Map<NotificationEvent, State>> notifications = new HashMap<String, Map<NotificationEvent, State>>();

    static {
        Map<NotificationEvent, State> events = new HashMap<NotificationEvent, State>();

        // Smoke Alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.SMOKE__NONE, OnOffType.OFF);
        events.put(NotificationEvent.SMOKE__SMOKE_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.SMOKE__SMOKE_DETECTED_UNKNOWN, OnOffType.ON);
        events.put(NotificationEvent.SMOKE__SMOKE_ALARM_TEST, OnOffType.ON);
        notifications.put("alarm_smoke", events);

        // Door alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.ACCESS__NONE, OpenClosedType.CLOSED);
        events.put(NotificationEvent.ACCESS__DOOR_WINDOW_OPEN, OpenClosedType.OPEN);
        events.put(NotificationEvent.ACCESS__DOOR_WINDOW_CLOSED, OpenClosedType.CLOSED);
        notifications.put("sensor_door", events);

        // Heat alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.HEAT__NONE, OnOffType.OFF);
        events.put(NotificationEvent.HEAT__HIGH_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.HEAT__HIGH_DETECTED_UNKNOWN, OnOffType.ON);
        notifications.put("alarm_heat", events);

        // Motion alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.BURGLAR__NONE, OnOffType.OFF);
        events.put(NotificationEvent.BURGLAR__MOTION, OnOffType.ON);
        events.put(NotificationEvent.BURGLAR__MOTION_UNKNOWN, OnOffType.ON);
        notifications.put("alarm_motion", events);

        // Flood alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.WATER__NONE, OnOffType.OFF);
        events.put(NotificationEvent.WATER__LEAK_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.WATER__LEAK_DETECTED_UNKNOWN, OnOffType.ON);
        notifications.put("alarm_flood", events);

        // Tamper alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.BURGLAR__NONE, OnOffType.OFF);
        events.put(NotificationEvent.BURGLAR__TAMPER, OnOffType.ON);
        events.put(NotificationEvent.BURGLAR__TAMPER_UNKNOWN, OnOffType.ON);
        events.put(NotificationEvent.BURGLAR__TAMPER_INVALID_CODE, OnOffType.ON);
        notifications.put("alarm_tamper", events);

        // Battery alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.POWER__NONE, OnOffType.OFF);
        events.put(NotificationEvent.POWER__REPLACE_BATTERY_NOW, OnOffType.ON);
        notifications.put("alarm_battery", events);

        // Battery alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.POWER__NONE, OnOffType.OFF);
        events.put(NotificationEvent.POWER__MAINS_DISCONNECTED, OnOffType.ON);
        events.put(NotificationEvent.POWER__MAINS_APPLIED, OnOffType.OFF);
        events.put(NotificationEvent.POWER__MAINS_RECONNECTED, OnOffType.OFF);
        notifications.put("alarm_power", events);

        // Carbon Monoxide alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.CARBON_MONOXIDE__NONE, OnOffType.OFF);
        events.put(NotificationEvent.CARBON_MONOXIDE__CO_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.CARBON_MONOXIDE__CO_DETECTED_UNKNOWN, OnOffType.ON);
        events.put(NotificationEvent.CARBON_MONOXIDE__CO_ALARM_TEST, OnOffType.ON);
        notifications.put("alarm_co", events);

        // Carbon Dioxide alarms
        events = new HashMap<NotificationEvent, State>();
        events.put(NotificationEvent.CARBON_DIOXIDE__NONE, OnOffType.OFF);
        events.put(NotificationEvent.CARBON_DIOXIDE__CO2_DETECTED, OnOffType.ON);
        events.put(NotificationEvent.CARBON_DIOXIDE__CO2_DETECTED_UNKNOWN, OnOffType.ON);
        events.put(NotificationEvent.CARBON_DIOXIDE__CO2_ALARM_TEST, OnOffType.ON);
        notifications.put("alarm_co2", events);
    }

    /**
     * Constructor. Creates a new instance of the {@link ZWaveAlarmConverter} class.
     *
     * @param controller the {@link ZWaveController} to use for sending messages.
     */
    public ZWaveAlarmConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String alarmType = channel.getArguments().get("type");
        Integer alarmEvent = (channel.getArguments().get("event") == null) ? null
                : Integer.parseInt(channel.getArguments().get("event"));

        ZWaveAlarmValueEvent eventAlarm = (ZWaveAlarmValueEvent) event;
        logger.debug("Alarm converter processing {}", eventAlarm.getReportType());
        switch (eventAlarm.getReportType()) {
            case ALARM:
                return handleAlarmReport(channel, eventAlarm, alarmType);
            case NOTIFICATION:
                return handleNotificationReport(channel, eventAlarm, alarmType, alarmEvent);
        }

        return null;
    }

    private State handleAlarmReport(ZWaveThingChannel channel, ZWaveAlarmValueEvent eventAlarm, String alarmType) {
        // Don't trigger event if this item is bound to another alarm type
        if (alarmType != null && AlarmType.valueOf(alarmType) != eventAlarm.getAlarmType()) {
            return null;
        }

        // Processing for special channel types
        if (channel.getUID().getId().equals("alarm_number")) {
            return new DecimalType(eventAlarm.getV1AlarmCode());
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
                logger.debug("No conversion in {} to {}", getClass().getSimpleName(), channel.getDataType());
                break;
        }
        return state;
    }

    private State handleNotificationReport(ZWaveThingChannel channel, ZWaveAlarmValueEvent eventAlarm, String alarmType,
            Integer alarmEvent) {

        // Don't trigger event if this item is bound to another event type
        if (alarmType != null && AlarmType.valueOf(alarmType) != eventAlarm.getAlarmType()) {
            return null;
        }

        NotificationEvent notification = NotificationEvent.getEvent(eventAlarm.getAlarmType().toString(),
                eventAlarm.getAlarmEvent());

        // Handle event 0 as 'clear the event'
        int event = eventAlarm.getAlarmEvent();// == 0 ? 0 : eventAlarm.getAlarmStatus();
        logger.debug("Alarm converter NOTIFICATION event is {} - {}, type {}", event, channel.getDataType());
        if (notification == null) {
            return null;
        }

        // We ignore event 0xFE as that indicates "no further events",
        // and event 0xFF which indicates that unsolicited events are enabled
        if (event == 0xFE || event == 0xFF) {
            return null;
        }

        // Don't trigger event if there is no event match. Note that 0 is always acceptable
        if (alarmEvent != null && event != 0 && alarmEvent != event) {
            return null;
        }

        String channelType = channel.getUID().getId();
        switch (channelType) {
            case "alarm_number":
                return new DecimalType(eventAlarm.getAlarmEvent());
            default:
                Map<NotificationEvent, State> events = notifications.get(channelType);
                if (events == null) {
                    logger.debug("Alarm converter NOTIFICATION event is {}, channel {} is not implemented.", event,
                            channelType);
                    return null;
                }
                return events.get(notification);
        }
    }

    /**
     * {@inheritDoc}
     */
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

        CARBON_MONOXIDE__NONE("CARBON_MONOXIDE", 0),
        CARBON_MONOXIDE__CO_DETECTED("CARBON_MONOXIDE", 1),
        CARBON_MONOXIDE__CO_DETECTED_UNKNOWN("CARBON_MONOXIDE", 2),
        CARBON_MONOXIDE__CO_ALARM_TEST("CARBON_MONOXIDE", 3),
        CARBON_MONOXIDE__REPLACE("CARBON_MONOXIDE", 4),

        CARBON_DIOXIDE__NONE("CARBON_DIOXIDE", 0),
        CARBON_DIOXIDE__CO2_DETECTED("CARBON_DIOXIDE", 1),
        CARBON_DIOXIDE__CO2_DETECTED_UNKNOWN("CARBON_DIOXIDE", 2),
        CARBON_DIOXIDE__CO2_ALARM_TEST("CARBON_DIOXIDE", 3),
        CARBON_DIOXIDE__REPLACE("CARBON_DIOXIDE", 4),

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

        ACCESS__NONE("ACCESS_CONTROL", 0),
        ACCESS__MANUAL_LOCK("ACCESS_CONTROL", 1),
        ACCESS__MANUAL_UNLOCK("ACCESS_CONTROL", 2),
        ACCESS__REMOTE_LOCK("ACCESS_CONTROL", 3),
        ACCESS__REMOTE_UNLOCK("ACCESS_CONTROL", 4),
        ACCESS__KEYPAD_LOCK("ACCESS_CONTROL", 5),
        ACCESS__KEYPAD_UNLOCK("ACCESS_CONTROL", 6),
        ACCESS__MANUAL_NOT_FULLY_LOCKED("ACCESS_CONTROL", 7),
        ACCESS__REMOTE_NOT_FULLY_LOCKED("ACCESS_CONTROL", 8),
        ACCESS__AUTO_LOCK("ACCESS_CONTROL", 9),
        ACCESS__AUTO_NOT_FULLY_LOCKED("ACCESS_CONTROL", 10),
        ACCESS__LOCK_JAMMED("ACCESS_CONTROL", 11),
        ACCESS__ALL_USER_CODES_DELETED("ACCESS_CONTROL", 12),
        ACCESS__USER_CODE_DELETED("ACCESS_CONTROL", 13),
        ACCESS__USER_CODE_ADDED("ACCESS_CONTROL", 14),
        ACCESS__USER_CODE_DUPLICATE("ACCESS_CONTROL", 15),
        ACCESS__KEYPAD_TEMPORARILY_DISABLED("ACCESS_CONTROL", 16),
        ACCESS__KEYPAD_BUSY("ACCESS_CONTROL", 17),
        ACCESS__ADMIN_CODE_ADDED("ACCESS_CONTROL", 18),
        ACCESS__MANUAL_CODE_TOO_LONG("ACCESS_CONTROL", 19),
        ACCESS__REMOTE_UNLOCK_CODE_INVALID("ACCESS_CONTROL", 20),
        ACCESS__REMOTE_LOCK_CODE_INVALID("ACCESS_CONTROL", 21),
        ACCESS__DOOR_WINDOW_OPEN("ACCESS_CONTROL", 22),
        ACCESS__DOOR_WINDOW_CLOSED("ACCESS_CONTROL", 23),
        ACCESS__BARRIER_INITIALISING("ACCESS_CONTROL", 64),
        ACCESS__BARRIER_FORCE_EXCEEDED("ACCESS_CONTROL", 65),
        ACCESS__BARRIER_MOTOR_TIME_EXCEEDED("ACCESS_CONTROL", 66),
        ACCESS__BARRIER_MECHANICAL_LIMIT("ACCESS_CONTROL", 67),
        ACCESS__BARRIER_ERROR_UL_REQUIREMENT("ACCESS_CONTROL", 68),
        ACCESS__BARRIER_DISABLED_UL_REQUIREMENT("ACCESS_CONTROL", 69),
        ACCESS__BARRIER_MALFUNCTION("ACCESS_CONTROL", 70),
        ACCESS__BARRIER_VACATION_MODE("ACCESS_CONTROL", 71),
        ACCESS__BARRIER_SAFETY_OBSTICAL("ACCESS_CONTROL", 72),
        ACCESS__BARRIER_SUPERVISORY_ERROR("ACCESS_CONTROL", 73),
        ACCESS__BARRIER_SENSOR_BATTERY_LOW("ACCESS_CONTROL", 74),
        ACCESS__BARRIER_SHORT_DETECTED("ACCESS_CONTROL", 75),
        ACCESS__BARRIER_NON_ZWAVE("ACCESS_CONTROL", 76),

        BURGLAR__NONE("BURGLAR", 0),
        BURGLAR__INTRUSION("BURGLAR", 1),
        BURGLAR__TAMPER_UNKNOWN("BURGLAR", 2),
        BURGLAR__TAMPER("BURGLAR", 3),
        BURGLAR__TAMPER_INVALID_CODE("BURGLAR", 4),
        BURGLAR__GLASS_BREAK("BURGLAR", 5),
        BURGLAR__GLASS_BREAK_UNKNOWN("BURGLAR", 6),
        BURGLAR__MOTION("BURGLAR", 7),
        BURGLAR__MOTION_UNKNOWN("BURGLAR", 8),

        POWER__NONE("POWER_MANAGEMENT", 0),
        POWER__MAINS_APPLIED("POWER_MANAGEMENT", 1),
        POWER__MAINS_DISCONNECTED("POWER_MANAGEMENT", 2),
        POWER__MAINS_RECONNECTED("POWER_MANAGEMENT", 3),
        POWER__SURGE_DETECTED("POWER_MANAGEMENT", 4),
        POWER__VOLTAGE_DROP("POWER_MANAGEMENT", 5),
        POWER__OVER_CURRENT("POWER_MANAGEMENT", 6),
        POWER__OVER_VOLTAGE("POWER_MANAGEMENT", 7),
        POWER__OVER_LOAD("POWER_MANAGEMENT", 8),
        POWER__LOAD_ERROR("POWER_MANAGEMENT", 9),
        POWER__REPLACE_BATTERY_SOON("POWER_MANAGEMENT", 10),
        POWER__REPLACE_BATTERY_NOW("POWER_MANAGEMENT", 11),
        POWER__BATTERY_CHARGING("POWER_MANAGEMENT", 12),
        POWER__BATTERY_FULL("POWER_MANAGEMENT", 13),
        POWER__CHARGE_BATTERY_SOON("POWER_MANAGEMENT", 14),
        POWER__CHARGE_BATTERY_NOW("POWER_MANAGEMENT", 15),

        SYSTEM__NONE("SYSTEM", 0),
        SYSTEM__HARDWARE_FAILURE("SYSTEM", 1),
        SYSTEM__SOFTWARE_FAILURE("SYSTEM", 2),
        SYSTEM__HARDWARE_FAILURE_MANUFACTURER_CODE("SYSTEM", 3),
        SYSTEM__SOFTWARE_FAILURE_MANUFACTURER_CODE("SYSTEM", 3),

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
        HOME_HEALTH__VOC_LEVEL("HOME_HEALTH", 6);

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
