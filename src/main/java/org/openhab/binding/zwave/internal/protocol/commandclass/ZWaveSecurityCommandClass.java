/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.impl.CommandClassSecurityV1;
import org.openhab.binding.zwave.internal.protocol.security.ZWaveNonce;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * ZWave security command class 1
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_SECURITY")
public abstract class ZWaveSecurityCommandClass extends ZWaveCommandClass {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveSecurityCommandClass.class);

    @XStreamOmitField
    private SecretKey networkKey;

    // Our last nonce we sent to the remove
    private ZWaveNonce ourNonce = null;
    // The last nonce we received from the remote
    private ZWaveNonce theirNonce = null;

    private static final String AES = "AES";

    private boolean securelyIncluded = false;

    private static final List<Byte> securityRequired = Arrays.asList(new Byte[] {
            CommandClassSecurityV1.NETWORK_KEY_SET, CommandClassSecurityV1.NETWORK_KEY_VERIFY,
            CommandClassSecurityV1.SECURITY_SCHEME_INHERIT, CommandClassSecurityV1.SECURITY_COMMANDS_SUPPORTED_GET,
            CommandClassSecurityV1.SECURITY_COMMANDS_SUPPORTED_REPORT });

    /**
     * Creates a new instance of the ZWaveSecurityCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveSecurityCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_SECURITY;
    }

    /**
     * A controlling device MUST send Security Scheme Get Command immediately after the successful inclusion of a node
     * that supports the Security Command class.<br>
     *
     * A node is considered newly included if it has been included for less than 10 seconds.<br>
     *
     * A newly included node MUST return the Security Scheme Report Command in response to this command.<br>
     *
     * Whether a node has been included securely or non-securely, the node MUST NOT respond to the Security Scheme Get
     * command if it is not newly included.
     *
     * @return {@link ZWaveCommandClassTransactionPayload} to send
     */
    public ZWaveCommandClassTransactionPayload getSecuritySchemeGetMessage() {
        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(),
                CommandClassSecurityV1.getSecuritySchemeGet(0))
                        .withExpectedResponseCommand(CommandClassSecurityV1.SECURITY_SCHEME_REPORT)
                        .withPriority(TransactionPriority.High).build();
    }

    /**
     * This command is used to advertise security scheme 0 support by the node being included. Upon reception, the
     * including controller MUST send the network key immediately without waiting for input, by using 16 times 0x00 as
     * the temporary key. The including controller MUST NOT perform any validation of the Supported Security Schemes
     * byte.
     *
     * @param payload
     * @param endpoint
     */
    @ZWaveResponseHandler(id = CommandClassSecurityV1.SECURITY_SCHEME_REPORT, name = "SECURITY_SCHEME_REPORT")
    public void handleSecuritySchemeReport(ZWaveCommandClassPayload payload, int endpoint) {
    }

    /**
     * The Device can use the Network Key Set Command to set the network key in a Z-Wave node. Transmission of the
     * Network Key Set command requires existence of a common agreed security scheme. The device uses the agreed
     * temporary key to encapsulate the Network Key Set command.
     *
     * @return {@link ZWaveCommandClassTransactionPayload} to send
     */
    public ZWaveCommandClassTransactionPayload getSetSecurityKeyMessage() {
        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(),
                CommandClassSecurityV1.getNetworkKeySet(networkKey.getEncoded()))
                        .withExpectedResponseCommand(CommandClassSecurityV1.NETWORK_KEY_VERIFY)
                        .withPriority(TransactionPriority.High).build();
    }

    /**
     * When the included node has received a Network Key Set that is has successfully decrypted, verified by the MAC, it
     * MUST send a Network Key Verify Command to the including controller. If the controller is capable of decrypting
     * the Network Key Verify command it would indicate that the included node has successfully entered the secure
     * network. Since there is no timeout for the Network Key Verify, the controller can send a Security Commands
     * Supported Get command, and if no response is received, it SHOULD be concluded that the node has not been included
     * properly.
     *
     * @param payload
     * @param endpoint
     */
    @ZWaveResponseHandler(id = CommandClassSecurityV1.NETWORK_KEY_VERIFY, name = "NETWORK_KEY_VERIFY")
    public void handleSecurityNetworkKeyVerify(ZWaveCommandClassPayload payload, int endpoint) {
    }

    public ZWaveCommandClassTransactionPayload getSecurityCommandsSupportedMessage() {
        return null;
        // return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
        //// SECURITY_COMMANDS_SUPPORTED_GET).withExpectedResponseCommand(SECURITY_COMMANDS_SUPPORTED_REPORT)
        // .withPriority(TransactionPriority.High).build();
    }

    /**
     * This command advertises which command classes are supported using security encapsulation.<br>
     *
     * All non-securely supported command classes MAY be explicitly advertised in the Security Commands Supported
     * Report.<br>
     *
     * All securely supported command classes MUST be explicitly advertised in the Security Commands Supported Report if
     * they are only supported securely.<br>
     *
     * Secure communication MUST be used when transmitting this command.<br>
     *
     * @param payload
     * @param endpoint
     */
    @ZWaveResponseHandler(id = CommandClassSecurityV1.SECURITY_COMMANDS_SUPPORTED_REPORT, name = "SECURITY_COMMANDS_SUPPORTED_REPORT")
    public void handleSecurityCommandsSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
    }

    public ZWaveCommandClassTransactionPayload getSecurityNonceGet() {
        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(),
                CommandClassSecurityV1.getSecurityNonceGet())
                        .withExpectedResponseCommand(CommandClassSecurityV1.SECURITY_NONCE_REPORT)
                        .withPriority(TransactionPriority.High).build();
    }

    @ZWaveResponseHandler(id = CommandClassSecurityV1.SECURITY_NONCE_REPORT, name = "SECURITY_NONCE_REPORT")
    public void handleSecurityNonceReport(ZWaveCommandClassPayload payload, int endpoint) {
        // theirNonce;
        Map<String, Object> response = CommandClassSecurityV1.handleSecurityNonceReport(payload.getPayloadBuffer());
        theirNonce = new ZWaveNonce((byte[]) response.get("NONCE_BYTE"));
    }

    @ZWaveResponseHandler(id = CommandClassSecurityV1.SECURITY_NONCE_GET, name = "SECURITY_NONCE_GET")
    public void handleSecurityNonceGet(ZWaveCommandClassPayload payload, int endpoint) {
    }

    @ZWaveResponseHandler(id = CommandClassSecurityV1.SECURITY_MESSAGE_ENCAPSULATION, name = "SECURITY_MESSAGE_ENCAPSULATION")
    public void handleSecurityMessageEncapsulation(ZWaveCommandClassPayload payload, int endpoint) {
    }

    @ZWaveResponseHandler(id = CommandClassSecurityV1.SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET, name = "SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET")
    public void handleSecurityMessageEncapsulationNonceGet(ZWaveCommandClassPayload payload, int endpoint) {
    }

    /**
     * Sets the network key. The key is provided as a string of hexadecimal values. Values can be space or comma
     * delimitered, or can have no separation between values. Values can be prefixed with 0x or not.
     *
     * @param value {@link String} containing the new network key
     */
    public void setNetworkKey(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Network key must not be null");
        }

        String hexString = value.replace("0x", "");
        hexString = hexString.replace(",", "");
        hexString = hexString.replace(" ", "");

        if ((hexString.length() % 2) != 0) {
            throw new IllegalArgumentException("Network key must contain an even number of characters");
        }

        byte keyBytes[] = new byte[hexString.length() / 2];
        char enc[] = hexString.toCharArray();
        for (int i = 0; i < enc.length; i += 2) {
            StringBuilder curr = new StringBuilder(2);
            curr.append(enc[i]).append(enc[i + 1]);
            keyBytes[i / 2] = (byte) Integer.parseInt(curr.toString(), 16);
        }

        networkKey = new SecretKeySpec(keyBytes, AES);
        logger.debug("NODE {}: Updated networkKey", getNode().getNodeId());
    }

    /**
     * Checks if the device has completed secure inclusion
     *
     * @return true if secure inclusion was completed
     */
    public boolean isSecurelyIncluded() {
        return securelyIncluded;
    }

    public static boolean doesCommandRequireSecurityEncapsulation(int commandKey) {
        return securityRequired.contains(commandKey);
    }

    public boolean isNonceAvailable() {
        if (theirNonce == null) {
            return false;
        }
        return theirNonce.isValid();
    }
}
