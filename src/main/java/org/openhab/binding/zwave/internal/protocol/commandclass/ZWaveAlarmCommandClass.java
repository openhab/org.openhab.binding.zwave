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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
@XStreamAlias("COMMAND_CLASS_ALARM")
public class ZWaveAlarmCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassDynamicState, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private final static Logger logger = LoggerFactory.getLogger(ZWaveAlarmCommandClass.class);

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

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_ALARM;
    }

    @ZWaveResponseHandler(id = NOTIFICATION_REPORT, name = "NOTIFICATION_REPORT")
    public void handleNotificationReport(ZWaveCommandClassPayload payload, int endpoint) {
        int v1AlarmTypeCode = payload.getPayloadByte(2);
        int v1AlarmLevel = payload.getPayloadByte(3);
        int notificationEvent = 0;
        int notificationStatus = 0;
        int notificationTypeCode = 0;
        int[] parameters = null;

        AlarmType alarmType;
        ReportType eventType;

        if (getVersion() == 1 || payload.getPayloadLength() < 5) {
            eventType = ReportType.ALARM;
            alarmType = AlarmType.getAlarmType(v1AlarmTypeCode);

            notificationStatus = v1AlarmLevel;

            logger.debug("NODE {}: ALARM report - {} = {}", getNode().getNodeId(), v1AlarmTypeCode, v1AlarmLevel);
        } else if (getVersion() == 2) {
            eventType = ReportType.ALARM;
            notificationStatus = payload.getPayloadByte(5);
            notificationTypeCode = payload.getPayloadByte(6);
            notificationEvent = payload.getPayloadByte(7);
            alarmType = AlarmType.getAlarmType(notificationTypeCode);

            notificationStatus = v1AlarmLevel;

            logger.debug("NODE {}: ALARM report - {} = {}", getNode().getNodeId(), v1AlarmTypeCode, v1AlarmLevel);
        } else {
            eventType = ReportType.NOTIFICATION;
            // Indicates if reports are enabled or disabled
            notificationStatus = payload.getPayloadByte(5);
            notificationTypeCode = payload.getPayloadByte(6);
            notificationEvent = payload.getPayloadByte(7);
            alarmType = AlarmType.getAlarmType(notificationTypeCode);

            int parameterLength = payload.getPayloadByte(8) & 0x1f;
            boolean containsSequence = (payload.getPayloadByte(8) & 0x80) != 0;

            if (parameterLength != 0) {
                parameters = new int[parameterLength];
                for (int cnt = 0; cnt < parameterLength; cnt++) {
                    parameters[cnt] = payload.getPayloadByte(9 + cnt);
                }
            }

            logger.debug("NODE {}: NOTIFICATION report - {} = {}, event={}, status={}, plen={}", getNode().getNodeId(),
                    v1AlarmTypeCode, v1AlarmLevel, notificationEvent, notificationStatus, parameterLength);
        }

        if (alarmType != null) {
            // Alarm type seems to be supported, add it to the list.
            Alarm alarm = alarms.get(alarmType);
            if (alarm == null) {
                alarm = new Alarm(alarmType);
                alarms.put(alarmType, alarm);
            }
            alarm.setInitialised();
        }

        logger.debug("NODE {}: Alarm Type = {} ({})", getNode().getNodeId(), alarmType, v1AlarmTypeCode);

        ZWaveAlarmValueEvent event = new ZWaveAlarmValueEvent(getNode().getNodeId(), endpoint, eventType, alarmType,
                v1AlarmTypeCode, v1AlarmLevel, notificationEvent, notificationStatus);
        if (parameters != null) {
            event.setParameters(parameters);
        }

        getController().notifyEventListeners(event);

        dynamicDone = true;
    }

    @ZWaveResponseHandler(id = NOTIFICATION_SUPPORTED_REPORT, name = "NOTIFICATION_SUPPORTED_REPORT")
    public void handleNotificationSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        // Check if this is a V1 alarm report
        v1Supported = (payload.getPayloadByte(2) & 0x80) == 0;
        if (v1Supported) {
            logger.debug("NODE: NOTIFICATION_SUPPORTED_REPORT reports V1 ALARM support", getNode().getNodeId());
        }

        int numBytes = payload.getPayloadByte(2) & 0x1f;
        for (int i = 0; i < numBytes; ++i) {
            for (int bit = 0; bit < 8; ++bit) {
                if (((payload.getPayloadByte(i + 3)) & (1 << bit)) == 0) {
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

    @ZWaveResponseHandler(id = EVENT_SUPPORTED_REPORT, name = "EVENT_SUPPORTED_REPORT")
    public void handleEventSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        int notificationType = payload.getPayloadByte(2);
        int numBytes = payload.getPayloadByte(3) & 0x1f;
        List<Integer> types = new ArrayList<>();
        for (int i = 0; i < numBytes; ++i) {
            for (int bit = 0; bit < 8; ++bit) {
                if (((payload.getPayloadByte(i + 4)) & (1 << bit)) == 0) {
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
            logger.debug("NODE {}: Unknown Alarm Type = {}, ignoring report.", getNode().getNodeId(), alarmTypeCode);
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
    public ZWaveCommandClassTransactionPayload getNotificationReportMessage(AlarmType notificationType, int event) {
        if (getVersion() == 1) {
            logger.debug("NODE {}: NOTIFICATION_REPORT not supported for V1", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command NOTIFICATION_REPORT", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                NOTIFICATION_REPORT).withPayload(0, 0, 0, 0xff, notificationType.getKey(), event)
                        .withPriority(TransactionPriority.High).build();
    }

    /**
     * Gets a SerialMessage with the ALARM_GET command.
     * This just gets the first alarm!
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
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
    public ZWaveCommandClassTransactionPayload getSupportedMessage() {
        if (getVersion() == 1) {
            logger.debug("NODE {}: NOTIFICATION_SUPPORTED_GET not supported for V1", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command NOTIFICATION_SUPPORTED_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                NOTIFICATION_SUPPORTED_GET).withExpectedResponseCommand(NOTIFICATION_SUPPORTED_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the EVENT_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public ZWaveCommandClassTransactionPayload getSupportedEventMessage(int index) {
        if (getVersion() < 2) {
            logger.debug("NODE {}: EVENT_SUPPORTED_GET not supported for V1-2", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command EVENT_SUPPORTED_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                EVENT_SUPPORTED_GET).withPayload(new byte[] { (byte) index })
                        .withExpectedResponseCommand(EVENT_SUPPORTED_REPORT).withPriority(TransactionPriority.Config)
                        .build();
    }

    /**
     * Gets a SerialMessage with the NOTIFICATION_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getMessage(AlarmType alarmType, Integer alarmEvent) {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command NOTIFICATION_GET V{}",
                getNode().getNodeId(), getVersion());

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        switch (getVersion()) {
            case 1:
                outputData.write(alarmType.getKey());
                break;
            case 2:
                outputData.write(0);
                outputData.write(alarmType.getKey());
                break;
            case 3:
            default:
                // Notifications can't be polled for their state
                // This will return an event that allows us to know if the event is configured for pull or push
                // and not the actual state of the event.
                return null;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                NOTIFICATION_GET).withPayload(outputData.toByteArray()).withExpectedResponseCommand(NOTIFICATION_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        if (refresh == true) {
            supportedInitialised = false;
            eventsSupportedInitialised = false;
        }

        if (getVersion() > 1 && supportedInitialised == false) {
            result.add(getSupportedMessage());
        }

        if (getVersion() > 2 && eventsSupportedInitialised == false) {
            for (Entry<AlarmType, Alarm> alarmEntry : alarms.entrySet()) {
                if (alarmEntry.getValue().getReportedEvents().isEmpty()) {
                    result.add(getSupportedEventMessage(alarmEntry.getKey().key));
                }
            }
        }

        return result;
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
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
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        for (Map.Entry<AlarmType, Alarm> entry : alarms.entrySet()) {
            if (refresh == true || entry.getValue().getInitialised() == false) {
                if (getVersion() < 3) {
                    result.add(getMessage(entry.getValue().getAlarmType(), 0));
                } else {
                    // Notifications can't be polled for their state
                    // This will return an event that allows us to know if the event is configured for pull or push
                    // and not the actual state of the event.
                }
            }
        }

        return result;
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
        HOME_HEALTH(13),
        SIREN(14),
        WATER_VALVE(15),
        WEATHER(16),
        IRRIGATION(17),
        GAS(18),
        PEST_CONTROL(19),
        LIGHT_SENSOR(20),
        WATER_QUALITY(21),
        HOME_MONITORING(22);

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
        private int v1AlarmCode;
        private int v1AlarmLevel;
        private int[] parameters;

        /**
         * Constructor. Creates a instance of the ZWaveAlarmValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param eventType
         * @param alarmType the alarm type that triggered the event;
         * @param v1AlarmCode
         * @param alarmEvent
         * @param alarmStatus
         */
        public ZWaveAlarmValueEvent(int nodeId, int endpoint, ReportType eventType, AlarmType alarmType,
                int v1AlarmCode, int v1AlarmLevel, int alarmEvent, int alarmStatus) {
            super(nodeId, endpoint, CommandClass.COMMAND_CLASS_ALARM, alarmStatus);
            this.eventType = eventType;
            this.alarmType = alarmType;
            this.alarmEvent = alarmEvent;
            this.alarmStatus = alarmStatus;
            this.v1AlarmCode = v1AlarmCode;
            this.v1AlarmLevel = v1AlarmLevel;
        }

        public void setParameters(int[] parameters) {
            this.parameters = parameters;
        }

        public int[] getParameters() {
            return parameters;
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

        public int getV1AlarmCode() {
            return v1AlarmCode;
        }

        public int getV1AlarmLevel() {
            return v1AlarmLevel;
        }

        @Override
        public Integer getValue() {
            return (Integer) super.getValue();
        }
    }
}
