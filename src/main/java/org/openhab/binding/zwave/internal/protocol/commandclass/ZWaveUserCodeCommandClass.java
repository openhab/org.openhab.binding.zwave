/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 * Handles the User Code command class.
 *
 * @author Chris Jackson
 * @author Dave Badia
 */
@XStreamAlias("COMMAND_CLASS_USER_CODE")
public class ZWaveUserCodeCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveUserCodeCommandClass.class);
    public static final int USER_CODE_MIN_LENGTH = 4;
    public static final int USER_CODE_MAX_LENGTH = 10;

    private static final int USER_CODE_SET = 0x01;
    private static final int USER_CODE_GET = 0x02;
    private static final int USER_CODE_REPORT = 0x03;

    /**
     * Request the number of user codes that can be stored
     */
    private static final int USER_NUMBER_GET = 0x04;
    private static final int USER_NUMBER_REPORT = 0x05;

    private static final int UNKNOWN = -1;

    /**
     * The total number of users that the device supports as determined by {@link #USER_NUMBER_REPORT}
     */
    private int numberOfUsersSupported = UNKNOWN;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private Map<Integer, UserCode> userCodeList = new HashMap<Integer, UserCode>();

    /**
     * Creates a new instance of the ZWaveUserCodeCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveUserCodeCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_USER_CODE;
    }

    @ZWaveResponseHandler(id = USER_NUMBER_REPORT, name = "USER_NUMBER_REPORT")
    public void handleUserNumberReportReport(ZWaveCommandClassPayload payload, int endpoint) {
        numberOfUsersSupported = payload.getPayloadByte(2);
        logger.debug("NODE {}: UserCode numberOfUsersSupported={}", getNode().getNodeId(), numberOfUsersSupported);
    }

    @ZWaveResponseHandler(id = USER_CODE_REPORT, name = "USER_CODE_REPORT")
    public void handleUserCodeReportReport(ZWaveCommandClassPayload payload, int endpoint) {
        int id = payload.getPayloadByte(2);
        UserIdStatusType status = UserIdStatusType.getDoorLockStateType(payload.getPayloadByte(3));
        logger.debug("NODE {}: USER_CODE_REPORT {} is {}", getNode().getNodeId(), id, status);
        String code = "";
        int size = payload.getPayloadLength() - 4;
        if (size > USER_CODE_MAX_LENGTH) {
            logger.debug("NODE {}: UserCode({}) length is too long ({} bytes)", getNode().getNodeId(), id, size);
            size = USER_CODE_MAX_LENGTH;
        }
        if (status == UserIdStatusType.OCCUPIED) {
            byte[] strBuffer = Arrays.copyOfRange(payload.getPayloadBuffer(), 4, size + 4);
            try {
                code = new String(strBuffer, "ASCII");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.debug("NODE {}: USER_CODE_REPORT {} is {} [{}]", getNode().getNodeId(), id, status, code);
        }
        userCodeList.put(id, new UserCode(status, code));
        ZWaveUserCodeValueEvent zEvent = new ZWaveUserCodeValueEvent(this.getNode().getNodeId(), endpoint, id, code,
                status);
        getController().notifyEventListeners(zEvent);
    }

    public ZWaveCommandClassTransactionPayload getSupported() {
        logger.debug("NODE {}: Creating new message for application command USER_NUMBER_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), USER_NUMBER_GET)
                .withPriority(TransactionPriority.Get).withExpectedResponseCommand(USER_NUMBER_REPORT).build();
    }

    public ZWaveCommandClassTransactionPayload getUserCode(int id) {
        logger.debug("NODE {}: Creating new message for application command USER_CODE_GET({})",
                this.getNode().getNodeId(), id);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), USER_CODE_GET)
                .withPayload(id).withPriority(TransactionPriority.Config).withExpectedResponseCommand(USER_CODE_REPORT)
                .build();
    }

    public ZWaveCommandClassTransactionPayload setUserCode(int id, String code) {
        boolean codeIsZeros = true;

        // Zeros means delete the code
        try {
            codeIsZeros = Integer.parseInt(code) == 0;
        } catch (NumberFormatException e) {
            logger.debug("NODE {}: Error parsing user code. Code will be removed");
        }

        if (codeIsZeros) {
            code = ""; // send no code since we will set UserIdStatusType.AVAILBLE
        }

        if (codeIsZeros || userCodeIsValid(code)) {
            logger.debug("NODE {}: {} user code for {}", this.getNode().getNodeId(),
                    codeIsZeros ? "Removing" : "Setting", id);

            ByteArrayOutputStream outputData = new ByteArrayOutputStream();
            outputData.write(id); // identifier, must be 1 or higher
            if (codeIsZeros) {
                outputData.write(UserIdStatusType.AVAILABLE.key); // status
            } else {
                outputData.write(UserIdStatusType.OCCUPIED.key); // status
                try {
                    for (Byte aCodeDigit : code.getBytes("UTF-8")) {
                        outputData.write(aCodeDigit);
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.error("Got UnsupportedEncodingException", e);
                }
            }

            return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                    USER_CODE_SET).withPayload(outputData.toByteArray()).withPriority(TransactionPriority.Config)
                            .build();
        }

        return null;
    }

    public boolean userCodeIsValid(String userCode) {
        // Check length of userCode.code
        if (userCode.length() < USER_CODE_MIN_LENGTH || userCode.length() > USER_CODE_MAX_LENGTH) {
            logger.error("NODE {}: Ignoring user code {}: was {} digits but must be between {} and {}",
                    getNode().getNodeId(), userCode, userCode.length(), USER_CODE_MIN_LENGTH, USER_CODE_MAX_LENGTH);
            return false;
        }
        // Check that userCode.code is numeric
        for (char c : userCode.toCharArray()) {
            if (!Character.isDigit(c)) {
                logger.error("NODE {}: Ignoring user code {}: found non-digit of '{}' in code", getNode().getNodeId(),
                        userCode, c);
                return false;
            }
        }
        return true;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        logger.debug("NODE {}: User Code initialize", getNode().getNodeId());
        Collection<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (numberOfUsersSupported == UNKNOWN || refresh == true) {
            // Request it and wait for response
            logger.debug("NODE {}: numberOfUsersSupported=-1, refreshing", getNode().getNodeId());
            result.add(getSupported());
        }
        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        logger.debug("NODE {}: User Code initialize starting, refresh={}", getNode().getNodeId(), refresh);
        if (dynamicDone == true || refresh == false) {
            return null;
        }
        // Just do this once for now.
        // TODO: something more reliable for ensuring we have all codes!
        dynamicDone = true;

        // Request all user codes
        Collection<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        for (int cnt = 1; cnt <= numberOfUsersSupported; cnt++) {
            result.add(getUserCode(cnt));
        }
        return result;
    }

    /**
     * Return the number of supported codes
     *
     * @return number of codes supported by the device
     */
    public int getNumberOfSupportedCodes() {
        return numberOfUsersSupported;
    }

    /**
     * Z-Wave UserIDStatus enumeration. The user ID status type indicates
     * the state of the user ID.
     *
     * @see {@link ZWaveUserCodeCommandClass#USER_CODE_GET}
     * @see {@link ZWaveUserCodeCommandClass#USER_CODE_SET}
     */
    @XStreamAlias("userIdStatusType")
    static enum UserIdStatusType {
        AVAILABLE(0x00, "Available (not set)"),
        OCCUPIED(0x01, "Occupied"),
        RESERVED_BY_ADMINISTRATOR(0x02, "Reserved by administrator"),
        STATUS_NOT_AVAILABLE(0x11, "Status not available"),;
        /**
         * A mapping between the integer code and its corresponding door
         * lock state type to facilitate lookup by code.
         */
        private static Map<Integer, UserIdStatusType> codeToUserIdStatusTypeMapping;

        private int key;
        private String label;

        private static void initMapping() {
            codeToUserIdStatusTypeMapping = new ConcurrentHashMap<Integer, UserIdStatusType>();
            for (UserIdStatusType d : values()) {
                codeToUserIdStatusTypeMapping.put(d.key, d);
            }
        }

        /**
         * Lookup function based on the user id status type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the user id status type.
         */
        public static UserIdStatusType getDoorLockStateType(int i) {
            if (codeToUserIdStatusTypeMapping == null) {
                initMapping();
            }
            return codeToUserIdStatusTypeMapping.get(i);
        }

        UserIdStatusType(int key, String label) {
            this.key = key;
            this.label = label;
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

    @XStreamAlias("userCode")
    public class UserCode {
        UserIdStatusType state;
        String code;

        UserCode(UserIdStatusType state, String code) {
            this.state = state;
            this.code = code;
        }

        String getCode() {
            return code;
        }

        UserIdStatusType getState() {
            return state;
        }
    }

    public class ZWaveUserCodeValueEvent extends ZWaveCommandClassValueEvent {
        private int id;
        private String code;
        private UserIdStatusType status;

        private ZWaveUserCodeValueEvent(int nodeId, int endpoint, int id, String code, UserIdStatusType status) {
            super(nodeId, endpoint, CommandClass.COMMAND_CLASS_USER_CODE, code);
            this.id = id;
            this.status = status;
        }

        public int getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public UserIdStatusType getStatus() {
            return status;
        }
    }
}
