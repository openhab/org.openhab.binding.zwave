/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Alarm/Notification command class.
 * The event is reported as occurs (0xFF) or does not occur (0x00).
 *
 * A push mode notification sensor sends unsolicited notification reports. The transmission of unsolicited notification
 * reports may be disabled or enabled via the notification set message. Even if enabled, unsolicited notification
 * reports can only be transmitted if an association target is defined.
 * Push functionality such as event reporting via Notification CC and relay control via Basic CC are advertised via the
 * Association Group Information (AGI) CC.
 *
 * A pull mode notification sensor collects notification reports in a list of pending notification reports. A
 * notification report is returned in response to a notification get message. Multiple notification reports may be
 * retrieved from the list by repeated notification get messages.
 * A notification report may be returned persistently until it is actively cleared via the Notification Set message.
 *
 * @author Chris Jackson
 */
@XStreamAlias("alarmCommandClass")
public class ZWaveAlarmCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassDynamicState, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveAlarmCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 8;

    private static final int NOTIFICATION_GET = 4;
    private static final int NOTIFICATION_REPORT = 5;
    private static final int NOTIFICATION_SET = 6;

    // Version 2
    private static final int NOTIFICATION_SUPPORTED_GET = 7;
    private static final int NOTIFICATION_SUPPORTED_REPORT = 8;

    // Version 3
    private static final int EVENT_SUPPORTED_GET = 1;
    private static final int EVENT_SUPPORTED_REPORT = 2;

    private final Map<AlarmType, Alarm> alarms = new HashMap<AlarmType, Alarm>();

    private boolean v1Supported = false;

    @XStreamOmitField
    private boolean supportedInitialised = false;
    @XStreamOmitField
    private boolean eventsSupportedInitialised = false;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    public enum ReportType {
        ALARM,
        NOTIFICATION
    };

    /**
     * Creates a new instance of the ZWaveAlarmCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveAlarmCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.ALARM;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received ALARM command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case NOTIFICATION_REPORT:
                logger.debug("NODE {}: Process NOTIFICATION_REPORT V{}", getNode().getNodeId(), getVersion());
                processNotificationReport(serialMessage, offset, endpoint);
                break;

            case NOTIFICATION_SUPPORTED_REPORT:
                logger.debug("NODE {}: Process NOTIFICATION_SUPPORTED_REPORT", getNode().getNodeId());
                processNotificationSupportedReport(serialMessage, offset, endpoint);
                break;

            case EVENT_SUPPORTED_REPORT:
                logger.debug("NODE {}: Process EVENT_SUPPORTED_REPORT", getNode().getNodeId());
                processEventSupportedReport(serialMessage, offset, endpoint);
                break;

            default:
                logger.warn(String.format("Unsupported Command 0x%02X for command class %s (0x%02X).", command,
                        getCommandClass().getLabel(), getCommandClass().getKey()));
                break;
        }
    }

    protected void processNotificationReport(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        int v1AlarmTypeCode = serialMessage.getMessagePayloadByte(offset + 1);
        int v1AlarmLevel = serialMessage.getMessagePayloadByte(offset + 2);
        int notificationEvent = 0;
        int notificationStatus = 0;
        int notificationTypeCode = 0;

        AlarmType alarmType;
        ReportType eventType;

        if (getVersion() == 1 || serialMessage.getMessagePayload().length < (offset + 4)) {
            eventType = ReportType.ALARM;
            alarmType = AlarmType.getAlarmType(v1AlarmTypeCode);

            notificationStatus = v1AlarmLevel;

            logger.debug("NODE {}: ALARM report - {} = {}", getNode().getNodeId(), v1AlarmTypeCode, v1AlarmLevel);
        } else {
            eventType = ReportType.NOTIFICATION;
            // Indicates if reports are enabled or disabled
            notificationStatus = serialMessage.getMessagePayloadByte(offset + 4);
            notificationTypeCode = serialMessage.getMessagePayloadByte(offset + 5);
            notificationEvent = serialMessage.getMessagePayloadByte(offset + 6);
            alarmType = AlarmType.getAlarmType(notificationTypeCode);

            int parameterLength = serialMessage.getMessagePayloadByte(offset + 5) & 0x1f;
            boolean containsSequence = (serialMessage.getMessagePayloadByte(offset + 5) & 0x80) != 0;

            logger.debug("NODE {}: NOTIFICATION report - {} = {}, event={}, status={}", getNode().getNodeId(),
                    v1AlarmTypeCode, v1AlarmLevel, notificationEvent, notificationStatus);
        }

        if (alarmType == null) {
            logger.error("NODE {}: Unknown Alarm Type = {}, ignoring report.", getNode().getNodeId(), v1AlarmTypeCode);
            return;
        }

        // Alarm type seems to be supported, add it to the list.
        Alarm alarm = alarms.get(alarmType);
        if (alarm == null) {
            alarm = new Alarm(alarmType);
            alarms.put(alarmType, alarm);
        }
        alarm.setInitialised();

        logger.debug("NODE {}: Alarm Type = {} ({})", getNode().getNodeId(), alarmType.toString(), v1AlarmTypeCode);

        ZWaveAlarmValueEvent zEvent = new ZWaveAlarmValueEvent(getNode().getNodeId(), endpoint, eventType, alarmType,
                notificationEvent, notificationStatus);
        getController().notifyEventListeners(zEvent);

        dynamicDone = true;
    }

    /**
     * Process NOTIFICATION_SUPPORTED_REPORT
     *
     * @param serialMessage
     * @param offset
     * @param endpoint
     * @throws ZWaveSerialMessageException
     */
    protected void processNotificationSupportedReport(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {

        // Check if this is a V1 alarm report
        v1Supported = (serialMessage.getMessagePayloadByte(offset + 1) & 0x80) == 0;
        if (v1Supported) {
            logger.debug("NODE: NOTIFICATION_SUPPORTED_REPORT reports V1 ALARM support", getNode().getNodeId());

        }

        int numBytes = serialMessage.getMessagePayloadByte(offset + 1) & 0x1f;
        for (int i = 0; i < numBytes; ++i) {
            for (int bit = 0; bit < 8; ++bit) {
                if (((serialMessage.getMessagePayloadByte(offset + i + 2)) & (1 << bit)) == 0) {
                    continue;
                }

                int index = (i << 3) + bit;
                if (index >= AlarmType.values().length) {
                    continue;
                }

                // (n)th bit is set. n is the index for the alarm type enumeration.
                // Alarm type seems to be supported, add it to the list if it's not already there.
                getAlarm(index);
            }
        }
        supportedInitialised = true;
    }

    /**
     * Process EVENT_SUPPORTED_REPORT
     *
     * @param serialMessage
     * @param offset
     * @param endpoint
     * @throws ZWaveSerialMessageException
     */
    protected void processEventSupportedReport(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        int notificationType = serialMessage.getMessagePayloadByte(offset + 1);
        int numBytes = serialMessage.getMessagePayloadByte(offset + 2) & 0x1f;
        List<Integer> types = new ArrayList<>();
        for (int i = 0; i < numBytes; ++i) {
            for (int bit = 0; bit < 8; ++bit) {
                if (((serialMessage.getMessagePayloadByte(offset + i + 3)) & (1 << bit)) == 0) {
                    continue;
                }

                int index = (i << 3) + bit;
                types.add(index);
                getAlarm(notificationType).getReportedEvents().add(index);
            }
        }
        logger.debug("NODE {}: AlarmType: {} reported events -> {}", getNode().getNodeId(),
                AlarmType.getAlarmType(notificationType), types);

        eventsSupportedInitialised = true;
    }

    private Alarm getAlarm(int alarmTypeCode) {
        AlarmType alarmType = AlarmType.getAlarmType(alarmTypeCode);
        if (alarmType == null) {
            logger.error("NODE {}: Unknown Alarm Type = {}, ignoring report.", getNode().getNodeId(), alarmTypeCode);
            return null;
        }

        // Add alarm to the list if it's not already there.
        Alarm alarm = alarms.get(alarmType);
        if (alarm == null) {
            logger.debug("NODE {}: Adding new alarm type {}({})", getNode().getNodeId(), alarmType.toString(),
                    alarmTypeCode);
            alarm = new Alarm(alarmType);
            alarms.put(alarmType, alarm);
        }

        return alarm;
    }

    /**
     * Send a notification event
     *
     * @param notificationType
     * @param event
     * @return
     */
    public SerialMessage getNotificationReportMessage(AlarmType notificationType, int event) {
        if (getVersion() == 1) {
            logger.debug("NODE {}: NOTIFICATION_REPORT not supported for V1", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command NOTIFICATION_REPORT", getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.High);
        byte[] newPayload = { (byte) getNode().getNodeId(), 8, (byte) getCommandClass().getKey(),
                (byte) NOTIFICATION_REPORT, 0, 0, 0, (byte) 0xff, (byte) notificationType.getKey(), (byte) event };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Gets a SerialMessage with the ALARM_GET command
     *
     * @return the serial message
     */
    @Override
    public SerialMessage getValueMessage() {
        // TODO: Why does this return!!!???!!!
        // TODO Is this used
        for (Map.Entry<AlarmType, Alarm> entry : alarms.entrySet()) {
            return getMessage(entry.getValue().getAlarmType(), 0);
        }

        return getMessage(AlarmType.GENERAL, 0);
    }

    /**
     * Gets a SerialMessage with the NOTIFICATION_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public SerialMessage getSupportedMessage() {
        if (getVersion() == 1) {
            logger.debug("NODE {}: NOTIFICATION_SUPPORTED_GET not supported for V1", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command NOTIFICATION_SUPPORTED_GET", getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.High);
        byte[] newPayload = { (byte) getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                (byte) NOTIFICATION_SUPPORTED_GET };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Gets a SerialMessage with the EVENT_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public SerialMessage getSupportedEventMessage(int index) {
        if (getVersion() == 1) {
            logger.debug("NODE {}: EVENT_SUPPORTED_GET not supported for V1-2", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command EVENT_SUPPORTED_GET", getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.High);
        byte[] newPayload = { (byte) getNode().getNodeId(), 3, (byte) getCommandClass().getKey(),
                (byte) EVENT_SUPPORTED_GET, (byte) index };
        result.setMessagePayload(newPayload);

        return result;
    }

    /**
     * Gets a SerialMessage with the NOTIFICATION_GET command
     *
     * @return the serial message
     */
    public SerialMessage getMessage(AlarmType alarmType, int event) {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command NOTIFICATION_GET V{}",
                getNode().getNodeId(), getVersion());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = null;
        switch (getVersion()) {
            case 1:
                newPayload = new byte[] { (byte) getNode().getNodeId(), 3, (byte) getCommandClass().getKey(),
                        (byte) NOTIFICATION_GET, (byte) alarmType.getKey() };
                result.setMessagePayload(newPayload);
                break;
            case 2:
                newPayload = new byte[] { (byte) getNode().getNodeId(), 4, (byte) getCommandClass().getKey(),
                        (byte) NOTIFICATION_GET, 0, (byte) alarmType.getKey() };
                break;
            default:
            case 3:
                newPayload = new byte[] { (byte) getNode().getNodeId(), 5, (byte) getCommandClass().getKey(),
                        (byte) NOTIFICATION_GET, 0, (byte) alarmType.getKey(), (byte) event };
                break;
        }

        result.setMessagePayload(newPayload);
        return result;
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        if (refresh == true) {
            supportedInitialised = false;
            eventsSupportedInitialised = false;
        }

        // First get a list of all the notifications supported
        if (getVersion() > 1 && supportedInitialised == false) {
            result.add(getSupportedMessage());
        }

        // Once we have all the notifications, get a list of all the events
        if (getVersion() > 2 && eventsSupportedInitialised == false) {
            for (Entry<AlarmType, Alarm> alarmEntry : alarms.entrySet()) {
                if (alarmEntry.getValue().getReportedEvents().isEmpty()) {
                    result.add(getSupportedEventMessage(alarmEntry.getKey().key));
                }
            }
        }

        return result;
    }

    /**
     * Returns a {@link Set} of {@link AlarmType}s supported by this device
     *
     * @return {@link Set} of {@link AlarmType}s
     */
    public Set<AlarmType> getSupportedAlarms() {
        return alarms.keySet();
    }

    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        for (Map.Entry<AlarmType, Alarm> entry : alarms.entrySet()) {
            if (refresh == true || entry.getValue().getInitialised() == false) {
                if (getVersion() < 3) {
                    result.add(getMessage(entry.getValue().getAlarmType(), 0));
                } else {
                    // If we don't have any events, then don't initialise
                    if (entry.getValue().getReportedEvents().isEmpty()) {
                        entry.getValue().setInitialised();
                    }

                    // For the NOTIFICATION class, only request for events that are supported
                    for (int event : entry.getValue().getReportedEvents()) {
                        result.add(getMessage(entry.getValue().getAlarmType(), event));
                    }
                }
            }
        }

        return result;
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        // TODO: False logic!
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Z-Wave AlarmType enumeration. The alarm type indicates the type of alarm that is reported.
     */
    @XStreamAlias("alarmType")
    public enum AlarmType {
        GENERAL(0),
        SMOKE(1),
        CARBON_MONOXIDE(2),
        CARBON_DIOXIDE(3),
        HEAT(4),
        FLOOD(5),
        ACCESS_CONTROL(6),
        BURGLAR(7),
        POWER_MANAGEMENT(8),
        SYSTEM(9),
        EMERGENCY(10),
        CLOCK(11),
        APPLIANCE(12),
        HOME_HEALTH(13);

        /**
         * A mapping between the integer code and its corresponding Alarm type to facilitate lookup by code.
         */
        private static Map<Integer, AlarmType> codeToAlarmTypeMapping;

        private int key;

        private AlarmType(int key) {
            this.key = key;
        }

        private static void initMapping() {
            codeToAlarmTypeMapping = new HashMap<Integer, AlarmType>();
            for (AlarmType s : values()) {
                codeToAlarmTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the alarm type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the alarm type.
         */
        public static AlarmType getAlarmType(int i) {
            if (codeToAlarmTypeMapping == null) {
                initMapping();
            }

            return codeToAlarmTypeMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }
    }

    /**
     * Class to hold alarm state
     *
     */
    @XStreamAlias("alarmState")
    private class Alarm {
        AlarmType alarmType;

        @XStreamOmitField
        boolean initialised = false;

        List<Integer> reportedEvents = new ArrayList<>();

        public Alarm(AlarmType type) {
            alarmType = type;
        }

        public AlarmType getAlarmType() {
            return alarmType;
        }

        public void setInitialised() {
            initialised = true;
        }

        public boolean getInitialised() {
            return initialised;
        }

        public List<Integer> getReportedEvents() {
            return reportedEvents;
        }
    }

    /**
     * Z-Wave Alarm Event class. Indicates that an alarm value changed.
     */
    public class ZWaveAlarmValueEvent extends ZWaveCommandClassValueEvent {

        private ReportType eventType;
        private AlarmType alarmType;
        private int alarmEvent;
        private int alarmStatus;

        /**
         * Constructor. Creates a instance of the ZWaveAlarmValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param eventType
         * @param alarmType the alarm type that triggered the event;
         * @param value the value for the event.
         */
        public ZWaveAlarmValueEvent(int nodeId, int endpoint, ReportType eventType, AlarmType alarmType, int alarmEvent,
                int alarmStatus) {
            super(nodeId, endpoint, CommandClass.ALARM, alarmStatus);
            this.eventType = eventType;
            this.alarmType = alarmType;
            this.alarmEvent = alarmEvent;
            this.alarmStatus = alarmStatus;
        }

        /**
         * Gets the type of report (ALARM or NOTIFICATION)
         */
        public ReportType getReportType() {
            return eventType;
        }

        /**
         * Gets the alarm type for this alarm value event.
         */
        public AlarmType getAlarmType() {
            return alarmType;
        }

        /**
         * Gets the alarm event for this event
         */
        public int getAlarmEvent() {
            return alarmEvent;
        }

        /**
         * Gets the alarm status for this event
         */
        public int getAlarmStatus() {
            return alarmStatus;
        }

        @Override
        public Integer getValue() {
            return (Integer) super.getValue();
        }
    }
}
