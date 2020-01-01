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
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the door lock logging command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_DOOR_LOCK_LOGGING")
public class ZWaveDoorLockLoggingCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveDoorLockLoggingCommandClass.class);

    private static final int LOGGING_SUPPORTED_GET = 1;
    private static final int DOOR_LOCK_LOGGING_RECORDS_SUPPORTED_REPORT = 2;
    private static final int LOGGING_RECORD_GET = 3;
    private static final int DOOR_LOCK_LOGGING_RECORD_REPORT = 4;

    private int supportedMessages = -1;

    /**
     * Creates a new instance of the ZWaveDoorLockLoggingCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveDoorLockLoggingCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_DOOR_LOCK_LOGGING;
    }

    @ZWaveResponseHandler(id = DOOR_LOCK_LOGGING_RECORDS_SUPPORTED_REPORT, name = "DOOR_LOCK_LOGGING_RECORDS_SUPPORTED_REPORT")
    public void handleDoorLockLoggingRecordsSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        supportedMessages = payload.getPayloadByte(2);
        logger.debug("NODE {}: DOOR_LOCK_LOGGING_RECORDS_SUPPORTED_REPORT supports {} entries", getNode().getNodeId(),
                supportedMessages);
    }

    @ZWaveResponseHandler(id = DOOR_LOCK_LOGGING_RECORD_REPORT, name = "DOOR_LOCK_LOGGING_RECORD_REPORT")
    public void handleDoorLockConfigReport(ZWaveCommandClassPayload payload, int endpoint) {

        LogType eventType = LogType.getLogType(payload.getPayloadByte(2));
        int eventOffset = payload.getPayloadByte(10);
        if (eventOffset > supportedMessages) {
            eventOffset = supportedMessages;
        }
        int year = payload.getPayloadByte(3) << 8 + payload.getPayloadByte(4);
        int month = payload.getPayloadByte(5) & 0x0f;
        int day = payload.getPayloadByte(6) & 0x1f;
        int hour = payload.getPayloadByte(7) & 0x1f;
        int minute = payload.getPayloadByte(8) & 0x3f;
        int second = payload.getPayloadByte(9) & 0x3f;
        boolean valid = ((payload.getPayloadByte(7) & 0xe0) > 0) ? true : false;

        int userCode = payload.getPayloadByte(11);
        int userCodeLength = payload.getPayloadByte(12);

        logger.debug("NODE {}: Received door lock log report {}", getNode().getNodeId(), eventType);
    }

    public ZWaveCommandClassTransactionPayload getSupported() {
        logger.debug("NODE {}: Creating new message for application command LOGGING_SUPPORTED_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                LOGGING_SUPPORTED_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(DOOR_LOCK_LOGGING_RECORDS_SUPPORTED_REPORT).build();
    }

    public ZWaveCommandClassTransactionPayload getEntry(int id) {
        logger.debug("NODE {}: Creating new message for application command LOGGING_RECORD_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                LOGGING_RECORD_GET).withPayload(id).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(DOOR_LOCK_LOGGING_RECORD_REPORT).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        if (refresh == false && supportedMessages != -1) {
            return null;
        }
        Collection<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        result.add(getSupported());
        return result;
    }

    @XStreamAlias("logType")
    public enum LogType {
        LOCKED_USING_CODE(1),
        UNLOCKED_USING_CODE(2),
        LOCKED_USING_BUTTON(3),
        UNLOCKED_USING_BUTTON(4),
        LOCK_ATTEMPT_OUT_OF_SCHEDULE_CODE(5),
        UNLOCK_ATTEMPT_OUT_OF_SCHEDULE_CODE(6),
        ILLEGAL_CODE(7),
        LOCKED_MANUALLY(8),
        UNLOCKED_MANUALLY(9),
        LOCKED_AUTO(10),
        UNLOCKED_AUTO(11),
        LOCKED_REMOTE_OUT_OF_SCHEDULE_CODE(12),
        UNLOCKED_REMOTE_OUT_OF_SCHEDULE_CODE(13),
        LOCKED_USING_REMOTE(14),
        UNLOCKED_USING_REMOTE(15),
        ILLEGAL_REMOTE_CODE(16),
        LOCKED_MANUALLY_2(17),
        UNLOCKED_MANUALLY_2(18),
        LOCK_SECURED(19),
        LOCK_UNSECURED(20),
        USER_CODE_ADDED(21),
        USER_CODE_DELETED(22),
        ALL_CODES_DELETED(23),
        MASTER_CODE_CHANGED(24),
        USER_CODE_CHANGED(25),
        LOCK_RESET(26),
        CONFIG_CHANGED(27),
        LOW_BATTERY(28),
        NEW_BATTERY_INSTALLED(29);

        /**
         * A mapping between the integer code and its corresponding Alarm type to facilitate lookup by code.
         */
        private static Map<Integer, LogType> codeToTypeMapping;

        private int key;

        private LogType(int key) {
            this.key = key;
        }

        private static void initMapping() {
            codeToTypeMapping = new HashMap<Integer, LogType>();
            for (LogType s : values()) {
                codeToTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the alarm type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the alarm type.
         */
        public static LogType getLogType(int i) {
            if (codeToTypeMapping == null) {
                initMapping();
            }

            return codeToTypeMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }
    }

}
