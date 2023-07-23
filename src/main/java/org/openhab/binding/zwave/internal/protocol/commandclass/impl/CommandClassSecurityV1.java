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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to implement the Z-Wave command class <b>COMMAND_CLASS_SECURITY</b> version <b>1</b>.<br>
 *
 * Command Class Security<br>
 *
 * This class provides static methods for processing received messages (message handler) and
 * methods to get a message to send on the Z-Wave network.<br>
 *
 * Note that this code is autogenerated. Manual changes may be overwritten.
 *
 * @author Chris Jackson - Initial contribution of Java code generator
 */
public class CommandClassSecurityV1 {
    private static final Logger logger = LoggerFactory.getLogger(CommandClassSecurityV1.class);

    /**
     * Integer command class key for COMMAND_CLASS_SECURITY
     */
    public final static int COMMAND_CLASS_KEY = 0x98;

    /**
     * Security Commands Supported Get Command Constant
     */
    public final static int SECURITY_COMMANDS_SUPPORTED_GET = 0x02;
    /**
     * Security Commands Supported Report Command Constant
     */
    public final static int SECURITY_COMMANDS_SUPPORTED_REPORT = 0x03;
    /**
     * Security Scheme Get Command Constant
     */
    public final static int SECURITY_SCHEME_GET = 0x04;
    /**
     * Security Scheme Report Command Constant
     */
    public final static int SECURITY_SCHEME_REPORT = 0x05;
    /**
     * Network Key Set Command Constant
     */
    public final static int NETWORK_KEY_SET = 0x06;
    /**
     * Network Key Verify Command Constant
     */
    public final static int NETWORK_KEY_VERIFY = 0x07;
    /**
     * Security Scheme Inherit Command Constant
     */
    public final static int SECURITY_SCHEME_INHERIT = 0x08;
    /**
     * Security Nonce Get Command Constant
     */
    public final static int SECURITY_NONCE_GET = 0x40;
    /**
     * Security Nonce Report Command Constant
     */
    public final static int SECURITY_NONCE_REPORT = 0x80;
    /**
     * Security Message Encapsulation Command Constant
     */
    public final static int SECURITY_MESSAGE_ENCAPSULATION = 0x81;
    /**
     * Security Message Encapsulation Nonce Get Command Constant
     */
    public final static int SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET = 0xC1;

    /**
     * Creates a new message with the SECURITY_COMMANDS_SUPPORTED_GET command.<br>
     *
     * Security Commands Supported Get<br>
     *
     *
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getSecurityCommandsSupportedGet() {
        logger.debug("Creating command message SECURITY_COMMANDS_SUPPORTED_GET version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(SECURITY_COMMANDS_SUPPORTED_GET);

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the SECURITY_COMMANDS_SUPPORTED_GET command<br>
     *
     * Security Commands Supported Get<br>
     *
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleSecurityCommandsSupportedGet(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the SECURITY_COMMANDS_SUPPORTED_REPORT command.<br>
     *
     * Security Commands Supported Report<br>
     *
     *
     * @param reportsToFollow {@link Integer}
     * @param commandClassSupport {@link List<Integer>}
     * @param commandClassControl {@link List<Integer>}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getSecurityCommandsSupportedReport(Integer reportsToFollow, List<Integer> commandClassSupport,
            List<Integer> commandClassControl) {
        logger.debug("Creating command message SECURITY_COMMANDS_SUPPORTED_REPORT version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(SECURITY_COMMANDS_SUPPORTED_REPORT);

        // Process 'Reports to follow'
        outputData.write(reportsToFollow);

        // Process 'Command Class support'
        for (Integer valCommandClassSupport : commandClassSupport) {
            outputData.write(valCommandClassSupport);
        }

        // Process 'COMMAND_CLASS_MARK'

        // Process 'Command Class control'
        for (Integer valCommandClassControl : commandClassControl) {
            outputData.write(valCommandClassControl);
        }

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the SECURITY_COMMANDS_SUPPORTED_REPORT command<br>
     *
     * Security Commands Supported Report<br>
     *
     *
     * The output data {@link Map} has the following properties<br>
     *
     * <ul>
     * <li>REPORTS_TO_FOLLOW {@link Integer}
     * <li>COMMAND_CLASS_SUPPORT {@link List}<{@link Integer}>
     * <li>COMMAND_CLASS_CONTROL {@link List}<{@link Integer}>
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleSecurityCommandsSupportedReport(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // We're using variable length fields, so track the offset
        int msgOffset = 2;

        // Process 'Reports to follow'
        response.put("REPORTS_TO_FOLLOW", Integer.valueOf(payload[msgOffset]));
        msgOffset += 1;

        // Process 'Command Class support'
        List<Integer> valCommandClassSupport = new ArrayList<Integer>();
        while (msgOffset < payload.length) {
            // Detect the marker
            if ((payload[msgOffset] & 0xFF) == 0xEF) {
                break;
            }
            valCommandClassSupport.add(payload[msgOffset] & 0xFF);
            msgOffset++;
        }
        response.put("COMMAND_CLASS_SUPPORT", valCommandClassSupport);

        // Process 'COMMAND_CLASS_MARK'
        // Adjust position to account for the marker
        msgOffset += 1;

        // Process 'Command Class control'
        List<Integer> valCommandClassControl = new ArrayList<Integer>();
        while (msgOffset < payload.length) {
            valCommandClassControl.add(payload[msgOffset] & 0xFF);
            msgOffset++;
        }
        response.put("COMMAND_CLASS_CONTROL", valCommandClassControl);

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the SECURITY_SCHEME_GET command.<br>
     *
     * Security Scheme Get<br>
     *
     *
     * @param supportedSecuritySchemes {@link Integer}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getSecuritySchemeGet(Integer supportedSecuritySchemes) {
        logger.debug("Creating command message SECURITY_SCHEME_GET version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(SECURITY_SCHEME_GET);

        // Process 'Supported Security Schemes'
        outputData.write(supportedSecuritySchemes);

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the SECURITY_SCHEME_GET command<br>
     *
     * Security Scheme Get<br>
     *
     *
     * The output data {@link Map} has the following properties<br>
     *
     * <ul>
     * <li>SUPPORTED_SECURITY_SCHEMES {@link Integer}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleSecuritySchemeGet(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Process 'Supported Security Schemes'
        response.put("SUPPORTED_SECURITY_SCHEMES", Integer.valueOf(payload[2]));

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the SECURITY_SCHEME_REPORT command.<br>
     *
     * Security Scheme Report<br>
     *
     *
     * @param supportedSecuritySchemes {@link Integer}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getSecuritySchemeReport(Integer supportedSecuritySchemes) {
        logger.debug("Creating command message SECURITY_SCHEME_REPORT version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(SECURITY_SCHEME_REPORT);

        // Process 'Supported Security Schemes'
        outputData.write(supportedSecuritySchemes);

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the SECURITY_SCHEME_REPORT command<br>
     *
     * Security Scheme Report<br>
     *
     *
     * The output data {@link Map} has the following properties<br>
     *
     * <ul>
     * <li>SUPPORTED_SECURITY_SCHEMES {@link Integer}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleSecuritySchemeReport(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Process 'Supported Security Schemes'
        response.put("SUPPORTED_SECURITY_SCHEMES", Integer.valueOf(payload[2]));

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the NETWORK_KEY_SET command.<br>
     *
     * Network Key Set<br>
     *
     *
     * @param networkKeyByte {@link byte[]}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getNetworkKeySet(byte[] networkKeyByte) {
        logger.debug("Creating command message NETWORK_KEY_SET version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(NETWORK_KEY_SET);

        // Process 'Network Key byte'
        try {
            outputData.write(networkKeyByte);
        } catch (IOException e) {
        }

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the NETWORK_KEY_SET command<br>
     *
     * Network Key Set<br>
     *
     *
     * The output data {@link Map} has the following properties<br>
     *
     * <ul>
     * <li>NETWORK_KEY_BYTE {@link byte[]}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleNetworkKeySet(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // We're using variable length fields, so track the offset
        int msgOffset = 2;

        // Process 'Network Key byte'
        ByteArrayOutputStream valNetworkKeyByte = new ByteArrayOutputStream();
        while (msgOffset < payload.length) {
            valNetworkKeyByte.write(payload[msgOffset]);
            msgOffset++;
        }
        response.put("NETWORK_KEY_BYTE", valNetworkKeyByte.toByteArray());

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the NETWORK_KEY_VERIFY command.<br>
     *
     * Network Key Verify<br>
     *
     *
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getNetworkKeyVerify() {
        logger.debug("Creating command message NETWORK_KEY_VERIFY version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(NETWORK_KEY_VERIFY);

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the NETWORK_KEY_VERIFY command<br>
     *
     * Network Key Verify<br>
     *
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleNetworkKeyVerify(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the SECURITY_SCHEME_INHERIT command.<br>
     *
     * Security Scheme Inherit<br>
     *
     *
     * @param supportedSecuritySchemes {@link Integer}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getSecuritySchemeInherit(Integer supportedSecuritySchemes) {
        logger.debug("Creating command message SECURITY_SCHEME_INHERIT version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(SECURITY_SCHEME_INHERIT);

        // Process 'Supported Security Schemes'
        outputData.write(supportedSecuritySchemes);

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the SECURITY_SCHEME_INHERIT command<br>
     *
     * Security Scheme Inherit<br>
     *
     *
     * The output data {@link Map} has the following properties<br>
     *
     * <ul>
     * <li>SUPPORTED_SECURITY_SCHEMES {@link Integer}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleSecuritySchemeInherit(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Process 'Supported Security Schemes'
        response.put("SUPPORTED_SECURITY_SCHEMES", Integer.valueOf(payload[2]));

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the SECURITY_NONCE_GET command.<br>
     *
     * Security Nonce Get<br>
     *
     *
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getSecurityNonceGet() {
        logger.debug("Creating command message SECURITY_NONCE_GET version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(SECURITY_NONCE_GET);

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the SECURITY_NONCE_GET command<br>
     *
     * Security Nonce Get<br>
     *
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleSecurityNonceGet(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the SECURITY_NONCE_REPORT command.<br>
     *
     * Security Nonce Report<br>
     *
     *
     * @param nonceByte {@link byte[]}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getSecurityNonceReport(byte[] nonceByte) {
        logger.debug("Creating command message SECURITY_NONCE_REPORT version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(SECURITY_NONCE_REPORT);

        // Process 'Nonce byte'
        try {
            outputData.write(nonceByte);
        } catch (IOException e) {
        }

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the SECURITY_NONCE_REPORT command<br>
     *
     * Security Nonce Report<br>
     *
     *
     * The output data {@link Map} has the following properties<br>
     *
     * <ul>
     * <li>NONCE_BYTE {@link byte[]}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleSecurityNonceReport(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // Process 'Nonce byte'
        byte[] valNonceByte = new byte[8];
        for (int cntNonceByte = 0; cntNonceByte < 8; cntNonceByte++) {
            valNonceByte[cntNonceByte] = payload[2 + cntNonceByte];
        }
        response.put("NONCE_BYTE", valNonceByte);

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the SECURITY_MESSAGE_ENCAPSULATION command.<br>
     *
     * Security Message Encapsulation<br>
     *
     *
     * @param initializationVectorByte {@link byte[]}
     * @param sequenceCounter {@link Integer}
     * @param sequenced {@link Boolean}
     * @param secondFrame {@link Boolean}
     * @param commandByte {@link byte[]}
     * @param receiversNonceIdentifier {@link Integer}
     * @param messageAuthenticationCodeByte {@link byte[]}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getSecurityMessageEncapsulation(byte[] initializationVectorByte, Integer sequenceCounter,
            Boolean sequenced, Boolean secondFrame, byte[] commandByte, Integer receiversNonceIdentifier,
            byte[] messageAuthenticationCodeByte) {
        logger.debug("Creating command message SECURITY_MESSAGE_ENCAPSULATION version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(SECURITY_MESSAGE_ENCAPSULATION);

        // Process 'Initialization Vector byte'
        try {
            outputData.write(initializationVectorByte);
        } catch (IOException e) {
        }

        // Process 'Properties1'
        int valProperties1 = 0;
        valProperties1 |= sequenceCounter & 0x0F;
        valProperties1 |= sequenced ? 0x10 : 0;
        valProperties1 |= secondFrame ? 0x20 : 0;
        outputData.write(valProperties1);

        // Process 'Command byte'
        try {
            outputData.write(commandByte);
        } catch (IOException e) {
        }

        // Process 'Receivers nonce Identifier'
        outputData.write(receiversNonceIdentifier);

        // Process 'Message Authentication Code byte'
        try {
            outputData.write(messageAuthenticationCodeByte);
        } catch (IOException e) {
        }

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the SECURITY_MESSAGE_ENCAPSULATION command<br>
     *
     * Security Message Encapsulation<br>
     *
     *
     * The output data {@link Map} has the following properties<br>
     *
     * <ul>
     * <li>INITIALIZATION_VECTOR_BYTE {@link byte[]}
     * <li>SEQUENCE_COUNTER {@link Integer}
     * <li>SEQUENCED {@link Boolean}
     * <li>SECOND_FRAME {@link Boolean}
     * <li>COMMAND_BYTE {@link byte[]}
     * <li>RECEIVERS_NONCE_IDENTIFIER {@link Integer}
     * <li>MESSAGE_AUTHENTICATION_CODE_BYTE {@link byte[]}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleSecurityMessageEncapsulation(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // We're using variable length fields, so track the offset
        int msgOffset = 2;

        // Process 'Initialization Vector byte'
        byte[] valInitializationVectorByte = new byte[8];
        for (int cntInitializationVectorByte = 0; cntInitializationVectorByte < 8; cntInitializationVectorByte++) {
            valInitializationVectorByte[cntInitializationVectorByte] = payload[msgOffset + cntInitializationVectorByte];
        }
        response.put("INITIALIZATION_VECTOR_BYTE", valInitializationVectorByte);
        msgOffset += 8;

        // Process 'Properties1'
        response.put("SEQUENCE_COUNTER", Integer.valueOf(payload[msgOffset] & 0x0F));
        response.put("SEQUENCED", Boolean.valueOf((payload[msgOffset] & 0x10) != 0));
        response.put("SECOND_FRAME", Boolean.valueOf((payload[msgOffset] & 0x20) != 0));
        msgOffset += 1;

        // Process 'Command byte'
        ByteArrayOutputStream valCommandByte = new ByteArrayOutputStream();
        while (msgOffset < payload.length - 9) {
            valCommandByte.write(payload[msgOffset]);
            msgOffset++;
        }
        response.put("COMMAND_BYTE", valCommandByte.toByteArray());

        // Process 'Receivers nonce Identifier'
        response.put("RECEIVERS_NONCE_IDENTIFIER", Integer.valueOf(payload[msgOffset]));
        msgOffset += 1;

        // Process 'Message Authentication Code byte'
        byte[] valMessageAuthenticationCodeByte = new byte[8];
        for (int cntMessageAuthenticationCodeByte = 0; cntMessageAuthenticationCodeByte < 8; cntMessageAuthenticationCodeByte++) {
            valMessageAuthenticationCodeByte[cntMessageAuthenticationCodeByte] = payload[msgOffset
                    + cntMessageAuthenticationCodeByte];
        }
        response.put("MESSAGE_AUTHENTICATION_CODE_BYTE", valMessageAuthenticationCodeByte);
        msgOffset += 8;

        // Return the map of processed response data;
        return response;
    }

    /**
     * Creates a new message with the SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET command.<br>
     *
     * Security Message Encapsulation Nonce Get<br>
     *
     *
     * @param initializationVectorByte {@link byte[]}
     * @param sequenceCounter {@link Integer}
     * @param sequenced {@link Boolean}
     * @param secondFrame {@link Boolean}
     * @param commandByte {@link byte[]}
     * @param receiversNonceIdentifier {@link Integer}
     * @param messageAuthenticationCodeByte {@link byte[]}
     * @return the {@link byte[]} array with the command to send
     */
    static public byte[] getSecurityMessageEncapsulationNonceGet(byte[] initializationVectorByte,
            Integer sequenceCounter, Boolean sequenced, Boolean secondFrame, byte[] commandByte,
            Integer receiversNonceIdentifier, byte[] messageAuthenticationCodeByte) {
        logger.debug("Creating command message SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET version 1");

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(COMMAND_CLASS_KEY);
        outputData.write(SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET);

        // Process 'Initialization Vector byte'
        try {
            outputData.write(initializationVectorByte);
        } catch (IOException e) {
        }

        // Process 'Properties1'
        int valProperties1 = 0;
        valProperties1 |= sequenceCounter & 0x0F;
        valProperties1 |= sequenced ? 0x10 : 0;
        valProperties1 |= secondFrame ? 0x20 : 0;
        outputData.write(valProperties1);

        // Process 'Command byte'
        try {
            outputData.write(commandByte);
        } catch (IOException e) {
        }

        // Process 'Receivers nonce Identifier'
        outputData.write(receiversNonceIdentifier);

        // Process 'Message Authentication Code byte'
        try {
            outputData.write(messageAuthenticationCodeByte);
        } catch (IOException e) {
        }

        return outputData.toByteArray();
    }

    /**
     * Processes a received frame with the SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET command<br>
     *
     * Security Message Encapsulation Nonce Get<br>
     *
     *
     * The output data {@link Map} has the following properties<br>
     *
     * <ul>
     * <li>INITIALIZATION_VECTOR_BYTE {@link byte[]}
     * <li>SEQUENCE_COUNTER {@link Integer}
     * <li>SEQUENCED {@link Boolean}
     * <li>SECOND_FRAME {@link Boolean}
     * <li>COMMAND_BYTE {@link byte[]}
     * <li>RECEIVERS_NONCE_IDENTIFIER {@link Integer}
     * <li>MESSAGE_AUTHENTICATION_CODE_BYTE {@link byte[]}
     * </ul>
     *
     * @param payload the {@link byte[]} payload data to process
     * @return a {@link Map} of processed response data
     */
    public static Map<String, Object> handleSecurityMessageEncapsulationNonceGet(byte[] payload) {
        // Create our response map
        Map<String, Object> response = new HashMap<String, Object>();

        // We're using variable length fields, so track the offset
        int msgOffset = 2;

        // Process 'Initialization Vector byte'
        byte[] valInitializationVectorByte = new byte[8];
        for (int cntInitializationVectorByte = 0; cntInitializationVectorByte < 8; cntInitializationVectorByte++) {
            valInitializationVectorByte[cntInitializationVectorByte] = payload[msgOffset + cntInitializationVectorByte];
        }
        response.put("INITIALIZATION_VECTOR_BYTE", valInitializationVectorByte);
        msgOffset += 8;

        // Process 'Properties1'
        response.put("SEQUENCE_COUNTER", Integer.valueOf(payload[msgOffset] & 0x0F));
        response.put("SEQUENCED", Boolean.valueOf((payload[msgOffset] & 0x10) != 0));
        response.put("SECOND_FRAME", Boolean.valueOf((payload[msgOffset] & 0x20) != 0));
        msgOffset += 1;

        // Process 'Command byte'
        ByteArrayOutputStream valCommandByte = new ByteArrayOutputStream();
        while (msgOffset < payload.length - 9) {
            valCommandByte.write(payload[msgOffset]);
            msgOffset++;
        }
        response.put("COMMAND_BYTE", valCommandByte.toByteArray());

        // Process 'Receivers nonce Identifier'
        response.put("RECEIVERS_NONCE_IDENTIFIER", Integer.valueOf(payload[msgOffset]));
        msgOffset += 1;

        // Process 'Message Authentication Code byte'
        byte[] valMessageAuthenticationCodeByte = new byte[8];
        for (int cntMessageAuthenticationCodeByte = 0; cntMessageAuthenticationCodeByte < 8; cntMessageAuthenticationCodeByte++) {
            valMessageAuthenticationCodeByte[cntMessageAuthenticationCodeByte] = payload[msgOffset
                    + cntMessageAuthenticationCodeByte];
        }
        response.put("MESSAGE_AUTHENTICATION_CODE_BYTE", valMessageAuthenticationCodeByte);
        msgOffset += 8;

        // Return the map of processed response data;
        return response;
    }
}
