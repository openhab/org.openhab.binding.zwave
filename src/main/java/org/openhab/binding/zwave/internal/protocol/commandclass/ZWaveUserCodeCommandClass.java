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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.impl.CommandClassUserCodeV1;
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

    /**
     * Request the number of user codes that can be stored
     */

    private static final int UNKNOWN = -1;

    /**
     * The total number of users that the device supports
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

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_USER_CODE;
    }

    @ZWaveResponseHandler(id = CommandClassUserCodeV1.USERS_NUMBER_REPORT, name = "USERS_NUMBER_REPORT")
    public void handleUserNumberReportReport(ZWaveCommandClassPayload payload, int endpoint) {
        Map<String, Object> response = CommandClassUserCodeV1.handleUsersNumberReport(payload.getPayloadBuffer());
        numberOfUsersSupported = (int) response.get("SUPPORTED_USERS");
        logger.debug("NODE {}: UserCode numberOfUsersSupported={}", getNode().getNodeId(), numberOfUsersSupported);
    }

    @ZWaveResponseHandler(id = CommandClassUserCodeV1.USER_CODE_REPORT, name = "USER_CODE_REPORT")
    public void handleUserCodeReportReport(ZWaveCommandClassPayload payload, int endpoint) {
        Map<String, Object> response = CommandClassUserCodeV1.handleUserCodeReport(payload.getPayloadBuffer());
        int id = (int) response.get("USER_IDENTIFIER");
        UserIdStatusType status = UserIdStatusType.valueOf((String) response.get("USER_ID_STATUS"));
        logger.debug("NODE {}: USER_CODE_REPORT {} is {}", getNode().getNodeId(), id, status);

        byte[] code = (byte[]) response.get("USER_CODE");

        // Check if this code only contains ASCII numbers - as per the current standard
        boolean isAscii = true;
        for (byte b : code) {
            if (b < '0' || b > '9') {
                isAscii = false;
                break;
            }
        }

        String asciiCode;
        if (isAscii) {
            try {
                asciiCode = new String(code, "ASCII");
            } catch (UnsupportedEncodingException e) {
                logger.debug("Encoding unsupported reading user code");
                asciiCode = "";
            }
        } else {
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (byte b : code) {
                if (!first) {
                    builder.append(" ");
                }
                first = false;
                builder.append(String.format("%02X", b));
            }

            asciiCode = builder.toString();
        }

        userCodeList.put(id, new UserCode(status, asciiCode));
        ZWaveUserCodeValueEvent zEvent = new ZWaveUserCodeValueEvent(getNode().getNodeId(), endpoint, id, asciiCode,
                status);
        getController().notifyEventListeners(zEvent);
    }

    public ZWaveCommandClassTransactionPayload getSupported() {
        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(),
                CommandClassUserCodeV1.getUsersNumberGet())
                        .withExpectedResponseCommand(CommandClassUserCodeV1.USERS_NUMBER_REPORT)
                        .withPriority(TransactionPriority.Get).build();
    }

    public ZWaveCommandClassTransactionPayload getUserCode(int id) {
        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(),
                CommandClassUserCodeV1.getUserCodeGet(id))
                        .withExpectedResponseCommand(CommandClassUserCodeV1.USER_CODE_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     *
     * @param id
     * @param asciiCode
     * @return
     */
    public ZWaveCommandClassTransactionPayload setUserCode(int id, String asciiCode) {
        byte[] code;
        UserIdStatusType status;

        // Check if this is an ASCII code, or a Hex code
        String tmpCode = asciiCode.trim();

        if (tmpCode == null || tmpCode.length() == 0) {
            status = UserIdStatusType.AVAILABLE;
            code = new byte[0];
        } else {
            status = UserIdStatusType.OCCUPIED;

            String[] codeElements = tmpCode.split(" ");
            if (codeElements.length > 1) {
                // HEX
                code = new byte[codeElements.length];

                try {
                    int cnt = 0;
                    for (String element : codeElements) {
                        code[cnt++] = (byte) Integer.parseInt(element, 16);
                    }
                } catch (NumberFormatException e) {
                    logger.error("NumberFormatException converting user code: {}", asciiCode);
                    return null;
                }
            } else {
                // ASCII
                code = new byte[asciiCode.length()];

                try {
                    int cnt = 0;
                    for (Byte aCodeDigit : asciiCode.getBytes("UTF-8")) {
                        code[cnt++] = aCodeDigit;
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.error("UnsupportedEncodingException converting user code: {}", asciiCode);
                }
            }
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(),
                CommandClassUserCodeV1.getUserCodeSet(id, status.toString(), code))
                        .withPriority(TransactionPriority.Config).build();
    }

    public boolean userCodeIsValid(String userCode) {
        // Check length of userCode.code
        if (userCode.length() < USER_CODE_MIN_LENGTH || userCode.length() > USER_CODE_MAX_LENGTH) {
            logger.debug("NODE {}: Ignoring user code {}: was {} digits but must be between {} and {}",
                    getNode().getNodeId(), userCode, userCode.length(), USER_CODE_MIN_LENGTH, USER_CODE_MAX_LENGTH);
            return false;
        }
        // Check that userCode.code is numeric
        for (char c : userCode.toCharArray()) {
            if (!Character.isDigit(c)) {
                logger.debug("NODE {}: Ignoring user code {}: found non-digit of '{}' in code", getNode().getNodeId(),
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
     * Gets a user code from the cache
     *
     * @param code the code to return;
     * @return {@link UserCode} or null if code not known
     */
    public UserCode getCachedUserCode(int code) {
        return userCodeList.get(code);
    }

    /**
     * Z-Wave UserIDStatus enumeration. The user ID status type indicates
     * the state of the user ID.
     *
     * @see {@link ZWaveUserCodeCommandClass#USER_CODE_GET}
     * @see {@link ZWaveUserCodeCommandClass#USER_CODE_SET}
     */
    @XStreamAlias("userIdStatusType")
    public static enum UserIdStatusType {
        AVAILABLE(0x00, "Available (not set)"),
        OCCUPIED(0x01, "Occupied"),
        RESERVED_BY_ADMINISTRATOR(0x02, "Reserved by administrator"),
        STATUS_NOT_AVAILABLE(0xFE, "Status not available"),;
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

        public String getCode() {
            return code;
        }

        public UserIdStatusType getState() {
            return state;
        }
    }

    public class ZWaveUserCodeValueEvent extends ZWaveCommandClassValueEvent {
        private int id;
        private String code;
        private UserIdStatusType status;

        private ZWaveUserCodeValueEvent(int nodeId, int endpoint, int id, String bs, UserIdStatusType status) {
            super(nodeId, endpoint, CommandClass.COMMAND_CLASS_USER_CODE, bs);
            this.id = id;
            this.code = bs;
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
