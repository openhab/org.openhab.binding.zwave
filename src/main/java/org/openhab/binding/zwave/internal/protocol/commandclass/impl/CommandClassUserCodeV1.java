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
package org.openhab.binding.zwave.internal.protocol.commandclass.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to implement the Z-Wave command class <b>COMMAND_CLASS_USER_CODE</b> version <b>1</b>.
 * <p>
 * Command Class User Code
 * <p>
 * This class provides static methods for processing received messages (message handler) and
 * methods to get a message to send on the Z-Wave network.
 * <p>
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
public class CommandClassUserCodeV1 {
    private static final Logger logger = LoggerFactory.getLogger(CommandClassUserCodeV1.class);

    /**
     * Integer command class key for COMMAND_CLASS_USER_CODE
     */
    public final static int COMMAND_CLASS_KEY = 0x63;

    /**
     * User Code Set Command Constant
     */
    public final static int USER_CODE_SET = 0x01;
    /**
     * User Code Get Command Constant
     */
    public final static int USER_CODE_GET = 0x02;
    /**
     * User Code Report Command Constant
     */
    public final static int USER_CODE_REPORT = 0x03;
    /**
     * Users Number Get Command Constant
     */
    public final static int USERS_NUMBER_GET = 0x04;
    /**
     * Users Number Report Command Constant
     */
    public final static int USERS_NUMBER_REPORT = 0x05;

    /**
     * Map holding constants for User ID Status
     */
    private static Map<Integer, String> constantUserIdStatus = new HashMap<Integer, String>();
    static {

        // Constants for User ID Status
        constantUserIdStatus.put(0xFE, "STATUS_NOT_AVAILABLE");
        constantUserIdStatus.put(0x00, "AVAILABLE");
        constantUserIdStatus.put(0x01, "OCCUPIED");
    }

    /**
     * Creates a new message with the USER_CODE_SET command.
     * <p>
     * User Code Set
     *
     * @param userIdentifier {@link Integer}
     * @param userIdStatus {@link String}
     *            Can be one of the following -:
     *            <ul>
     *            <li>STATUS_NOT_AVAILABLE
     *            <li>AVAILABLE
     *            <li>OCCUPIED
     *            </ul>
     * @param userCode {@link byte[]}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getUserCodeSet(Integer userIdentifier, String userIdStatus, byte[] userCode) {
        logger.debug("Creating command message USER_CODE_SET version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(USER_CODE_SET);

        // Process 'User Identifier'
        outputData.write(userIdentifier);

        // Process 'User ID Status'
        boolean foundUserIdStatus = false;
        for (Integer entry : constantUserIdStatus.keySet()) {
            if (constantUserIdStatus.get(entry).equals(userIdStatus)) {
                outputData.write(entry);
                foundUserIdStatus = true;
                break;
            }
        }
        if (!foundUserIdStatus) {
            throw new IllegalArgumentException("Unknown constant value '" + userIdStatus + "' for userIdStatus");
        }

        // Process 'USER_CODE'
        if (userCode != null) {
            if (userCode.length > 10) {
                throw new IllegalArgumentException("Length of array userCode exceeds maximum length of 10 bytes");
            }
            try {
                outputData.write(userCode);
            } catch (IOException e) {
            }
        }

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the USER_CODE_SET command.
     * <p>
     * User Code Set
     * <p>
     * The output data {@link Map} has the following properties -:
     *
     * <ul>
     * <li>USER_IDENTIFIER {@link Integer}
     * <li>USER_ID_STATUS {@link String}
     * <li>USER_CODE {@link byte[]}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleUserCodeSet(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Process 'User Identifier'
        response.put("USER_IDENTIFIER", Integer.valueOf(payload[2]));

        // Process 'User ID Status'
        response.put("USER_ID_STATUS", constantUserIdStatus.get(payload[3] & 0xff));

        // Process 'USER_CODE'
        int lenUserCode = Math.min(10, payload.length - 4);
        byte[] valUserCode = new byte[lenUserCode];
        for (int cntUserCode = 0; cntUserCode < lenUserCode; cntUserCode++) {
            valUserCode[cntUserCode] = payload[4 + cntUserCode];
        }
        response.put("USER_CODE", valUserCode);

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the USER_CODE_GET command.
     * <p>
     * User Code Get
     *
     * @param userIdentifier {@link Integer}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getUserCodeGet(Integer userIdentifier) {
        logger.debug("Creating command message USER_CODE_GET version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(USER_CODE_GET);

        // Process 'User Identifier'
        outputData.write(userIdentifier);

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the USER_CODE_GET command.
     * <p>
     * User Code Get
     * <p>
     * The output data {@link Map} has the following properties -:
     *
     * <ul>
     * <li>USER_IDENTIFIER {@link Integer}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleUserCodeGet(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Process 'User Identifier'
        response.put("USER_IDENTIFIER", Integer.valueOf(payload[2]));

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the USER_CODE_REPORT command.
     * <p>
     * User Code Report
     *
     * @param userIdentifier {@link Integer}
     * @param userIdStatus {@link String}
     *            Can be one of the following -:
     *            <ul>
     *            <li>STATUS_NOT_AVAILABLE
     *            <li>AVAILABLE
     *            <li>OCCUPIED
     *            </ul>
     * @param userCode {@link byte[]}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getUserCodeReport(Integer userIdentifier, String userIdStatus, byte[] userCode) {
        logger.debug("Creating command message USER_CODE_REPORT version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(USER_CODE_REPORT);

        // Process 'User Identifier'
        outputData.write(userIdentifier);

        // Process 'User ID Status'
        boolean foundUserIdStatus = false;
        for (Integer entry : constantUserIdStatus.keySet()) {
            if (constantUserIdStatus.get(entry).equals(userIdStatus)) {
                outputData.write(entry);
                foundUserIdStatus = true;
                break;
            }
        }
        if (!foundUserIdStatus) {
            throw new IllegalArgumentException("Unknown constant value '" + userIdStatus + "' for userIdStatus");
        }

        // Process 'USER_CODE'
        if (userCode != null) {
            if (userCode.length > 10) {
                throw new IllegalArgumentException("Length of array userCode exceeds maximum length of 10 bytes");
            }
            try {
                outputData.write(userCode);
            } catch (IOException e) {
            }
        }

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the USER_CODE_REPORT command.
     * <p>
     * User Code Report
     * <p>
     * The output data {@link Map} has the following properties -:
     *
     * <ul>
     * <li>USER_IDENTIFIER {@link Integer}
     * <li>USER_ID_STATUS {@link String}
     * <li>USER_CODE {@link byte[]}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleUserCodeReport(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Process 'User Identifier'
        response.put("USER_IDENTIFIER", Integer.valueOf(payload[2]) & 0xff);

        // Process 'User ID Status'
        response.put("USER_ID_STATUS", constantUserIdStatus.get(payload[3] & 0xff));

        // Process 'USER_CODE'
        int lenUserCode = Math.min(10, payload.length - 4);
        byte[] valUserCode = new byte[lenUserCode];
        for (int cntUserCode = 0; cntUserCode < lenUserCode; cntUserCode++) {
            valUserCode[cntUserCode] = payload[4 + cntUserCode];
        }
        response.put("USER_CODE", valUserCode);

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the USERS_NUMBER_GET command.
     * <p>
     * Users Number Get
     *
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getUsersNumberGet() {
        logger.debug("Creating command message USERS_NUMBER_GET version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(USERS_NUMBER_GET);

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the USERS_NUMBER_GET command.
     * <p>
     * Users Number Get
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleUsersNumberGet(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the USERS_NUMBER_REPORT command.
     * <p>
     * Users Number Report
     *
     * @param supportedUsers {@link Integer}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getUsersNumberReport(Integer supportedUsers) {
        logger.debug("Creating command message USERS_NUMBER_REPORT version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(USERS_NUMBER_REPORT);

        // Process 'Supported Users'
        outputData.write(supportedUsers);

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the USERS_NUMBER_REPORT command.
     * <p>
     * Users Number Report
     * <p>
     * The output data {@link Map} has the following properties -:
     *
     * <ul>
     * <li>SUPPORTED_USERS {@link Integer}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleUsersNumberReport(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Process 'Supported Users'
        response.put("SUPPORTED_USERS", Integer.valueOf(payload[2] & 0xff));

        // Return the map of processed response data;
        return response;
    }

}
