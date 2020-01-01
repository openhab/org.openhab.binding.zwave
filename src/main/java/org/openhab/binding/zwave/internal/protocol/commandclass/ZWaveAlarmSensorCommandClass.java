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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
 * Handles the Alarm Sensor command class. Alarm sensors indicate an event for each of their supported alarm types.
 * The event is reported as occurs (0xFF) or does not occur (0x00).
 * The commands include the possibility to get a given value and report a value.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("COMMAND_CLASS_SENSOR_ALARM")
public class ZWaveAlarmSensorCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveAlarmSensorCommandClass.class);

    private static final int SENSOR_ALARM_GET = 0x01;
    private static final int SENSOR_ALARM_REPORT = 0x02;
    private static final int SENSOR_ALARM_SUPPORTED_GET = 0x03;
    private static final int SENSOR_ALARM_SUPPORTED_REPORT = 0x04;

    private final Map<AlarmType, Alarm> alarms = new HashMap<AlarmType, Alarm>();

    @XStreamOmitField
    private boolean initialiseDone = false;

    private boolean isGetSupported = true;
    private boolean isSupportedGetSupported = true;

    /**
     * Creates a new instance of the ZWaveAlarmSensorCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveAlarmSensorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_SENSOR_ALARM;
    }

    @ZWaveResponseHandler(id = SENSOR_ALARM_REPORT, name = "SENSOR_ALARM_REPORT")
    public void handleSensorAlarmReport(ZWaveCommandClassPayload payload, int endpoint) {
        int sourceNode = payload.getPayloadByte(2);
        int alarmTypeCode = payload.getPayloadByte(3);
        int value = payload.getPayloadByte(4);

        // Alarm type seems to be supported, add it to the list if it's not already there.
        Alarm alarm = getAlarm(alarmTypeCode);
        if (alarm != null) {
            alarm.setInitialised();

            logger.debug("NODE {}: Alarm Report: Source={}, Type={}({}), Value={}", getNode().getNodeId(), sourceNode,
                    alarm.getAlarmType().getLabel(), alarmTypeCode, value);

            ZWaveAlarmSensorValueEvent zEvent = new ZWaveAlarmSensorValueEvent(getNode().getNodeId(), endpoint,
                    alarm.getAlarmType(), value);
            getController().notifyEventListeners(zEvent);
        }
    }

    @ZWaveResponseHandler(id = SENSOR_ALARM_SUPPORTED_REPORT, name = "SENSOR_ALARM_SUPPORTED_REPORT")
    public void handleSensorAlarmSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        int numBytes = payload.getPayloadByte(2);

        int manufacturerId = this.getNode().getManufacturer();
        int deviceType = this.getNode().getDeviceType();

        // TODO: This bodge should be removed and configured through the database!!!
        // Fibaro alarm sensors do not provide a bitmap of alarm types, but list them byte by byte.
        if (manufacturerId == 0x010F && deviceType == 0x0700) {
            logger.debug(
                    "Detected Fibaro FGK - 101 Door / Window sensor, activating workaround for incorrect encoding of supported alarm bitmap.");

            for (int i = 0; i < numBytes; ++i) {
                int index = payload.getPayloadByte(i + 3);
                if (index >= AlarmType.values().length) {
                    continue;
                }

                // Alarm type seems to be supported, add it to the list if it's not already there.
                getAlarm(index);
            }
        } else {
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
        }

        initialiseDone = true;
    }

    private Alarm getAlarm(int alarmTypeCode) {
        AlarmType alarmType = AlarmType.getAlarmType(alarmTypeCode);
        if (alarmType == null) {
            logger.debug("NODE {}: Unknown Alarm Type = {}, ignoring report.", this.getNode().getNodeId(),
                    alarmTypeCode);
            return null;
        }

        // Add alarm to the list if it's not already there.
        Alarm alarm = alarms.get(alarmType);
        if (alarm == null) {
            logger.debug("NODE {}: Adding new alarm type {}({})", this.getNode().getNodeId(), alarmType.getLabel(),
                    alarmTypeCode);
            alarm = new Alarm(alarmType);
            this.alarms.put(alarmType, alarm);
        }

        return alarm;
    }

    /**
     * Gets a SerialMessage with the SENSOR_ALARM_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        // TODO: Why does this return!!!???!!!
        for (Map.Entry<AlarmType, Alarm> entry : this.alarms.entrySet()) {
            return getMessage(entry.getValue().getAlarmType());
        }

        // in case there are no supported alarms, get them.
        return getSupportedMessage();
    }

    /**
     * Gets a SerialMessage with the SENSOR_ALARM_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getMessage(AlarmType alarmType) {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command SENSOR_ALARM_GET, type {}", getNode().getNodeId(),
                alarmType.getLabel());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_ALARM_GET).withPayload(alarmType.getKey()).withExpectedResponseCommand(SENSOR_ALARM_REPORT)
                        .withPriority(TransactionPriority.Get).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }
        if ("false".equals(options.get("supportedGetSupported"))) {
            isSupportedGetSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the SENSOR_ALARM_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public ZWaveCommandClassTransactionPayload getSupportedMessage() {
        if (isSupportedGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support supported get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command SENSOR_ALARM_SUPPORTED_GET",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_ALARM_SUPPORTED_GET).withExpectedResponseCommand(SENSOR_ALARM_SUPPORTED_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Initializes the alarm sensor command class. Requests the supported alarm types.
     */
    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(getSupportedMessage());
        }
        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        // If we want to refresh, then reset the init flag on all sensors
        if (refresh == true) {
            logger.debug("====---- Resetting init flag!");
            for (Map.Entry<AlarmType, Alarm> entry : this.alarms.entrySet()) {
                entry.getValue().resetInitialised();
            }
        }

        logger.debug("RESET IS {}", refresh);
        for (Map.Entry<AlarmType, Alarm> entry : this.alarms.entrySet()) {
            logger.debug("NODE {}: ====---- Checking alarm {} - init {}", this.getNode().getNodeId(),
                    entry.getValue().getAlarmType(), entry.getValue().getInitialised());
            if (entry.getValue().getInitialised() == false) {
                // TODO: This bodge should be configured through the database
                if (this.getNode().getManufacturer() == 0x010F && this.getNode().getDeviceType() == 0x0700) {
                    logger.warn(
                            "NODE {}: Detected Fibaro FGK - 101 Door / Window sensor, only requesting alarm type {}.",
                            this.getNode().getNodeId(), entry.getValue().getAlarmType().getLabel());
                    break;
                }
                result.add(getMessage(entry.getValue().getAlarmType()));
            }
        }

        return result;
    }

    /**
     * Z-Wave AlarmType enumeration. The alarm type indicates the type of alarm that is reported.
     *
     * @author Jan-Willem Spuij
     * @author Chris Jackson
     */
    @XStreamAlias("alarmSensorType")
    public enum AlarmType {
        GENERAL(0, "General"),
        SMOKE(1, "Smoke"),
        CARBON_MONOXIDE(2, "Carbon Monoxide"),
        CARBON_DIOXIDE(3, "Carbon Dioxide"),
        HEAT(4, "Heat"),
        FLOOD(5, "Flood"),
        ACCESS_CONTROL(6, "Access Control"),
        BURGLAR(7, "Burglar"),
        POWER_MANAGEMENT(8, "Power Management"),
        SYSTEM(9, "System"),
        EMERGENCY(10, "Emergency"),
        COUNT(11, "Count");

        /**
         * A mapping between the integer code and its corresponding Alarm type to facilitate lookup by code.
         */
        private static Map<Integer, AlarmType> codeToAlarmTypeMapping;

        private int key;
        private String label;

        private AlarmType(int key, String label) {
            this.key = key;
            this.label = label;
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

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }

    /**
     * Class to hold alarm state
     *
     */
    @XStreamAlias("alarmSensor")
    private static class Alarm {
        AlarmType alarmType;
        @XStreamOmitField
        boolean initialised = false;

        public Alarm(AlarmType type) {
            alarmType = type;
        }

        public AlarmType getAlarmType() {
            return alarmType;
        }

        public void setInitialised() {
            initialised = true;
        }

        public void resetInitialised() {
            initialised = false;
        }

        public boolean getInitialised() {
            return initialised;
        }
    }

    /**
     * Z-Wave Alarm Sensor Event class. Indicates that an alarm value changed.
     *
     * @author Jan-Willem Spuij
     */
    public class ZWaveAlarmSensorValueEvent extends ZWaveCommandClassValueEvent {

        private AlarmType alarmType;

        /**
         * Constructor. Creates a instance of the ZWaveAlarmSensorValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param alarmType the alarm type that triggered the event;
         * @param value the value for the event.
         */
        private ZWaveAlarmSensorValueEvent(int nodeId, int endpoint, AlarmType alarmType, Integer value) {
            super(nodeId, endpoint, CommandClass.COMMAND_CLASS_SENSOR_ALARM, value);
            this.alarmType = alarmType;
        }

        /**
         * Gets the alarm type for this alarm sensor value event.
         */
        public AlarmType getAlarmType() {
            return alarmType;
        }

        @Override
        public Integer getValue() {
            return (Integer) super.getValue();
        }
    }
}
