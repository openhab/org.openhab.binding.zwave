/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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
public class ZWaveSecurityCommandClass extends ZWaveCommandClass {
    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveSecurityCommandClass.class);

    private static final byte[] DERIVE_ENCRYPT_KEY = { (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA };
    private static final byte[] DERIVE_AUTH_KEY = { 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55,
            0x55, 0x55, 0x55, 0x55, 0x55 };

    @XStreamOmitField
    private SecretKey networkKey;

    @XStreamOmitField
    private SecretKey encryptionKey;

    @XStreamOmitField
    private SecretKey authenticationKey;

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
        ZWaveCommandClassTransactionPayload payload = new ZWaveCommandClassTransactionPayloadBuilder(
                getNode().getNodeId(), CommandClassSecurityV1.getNetworkKeySet(networkKey.getEncoded()))
                        .withExpectedResponseCommand(CommandClassSecurityV1.NETWORK_KEY_VERIFY)
                        .withPriority(TransactionPriority.High).build();
        payload.setRequiresSecurity();
        return payload;
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
        logger.debug("NODE {}: NONCE Received start...", getNode().getNodeId());
        if (theirNonce == null) {
            logger.debug("NODE {}: NONCE Received start null", getNode().getNodeId());
        } else {
            logger.debug("NODE {}: NONCE Received start {}", getNode().getNodeId(), theirNonce.toString());
        }
        // theirNonce;
        Map<String, Object> response = CommandClassSecurityV1.handleSecurityNonceReport(payload.getPayloadBuffer());
        theirNonce = new ZWaveNonce((byte[]) response.get("NONCE_BYTE"));
        logger.debug("NODE {}: NONCE Received {}", getNode().getNodeId(), theirNonce.toString());
    }

    @ZWaveResponseHandler(id = CommandClassSecurityV1.SECURITY_NONCE_GET, name = "SECURITY_NONCE_GET")
    public void handleSecurityNonceGet(ZWaveCommandClassPayload payload, int endpoint) {
        ourNonce = new ZWaveNonce();
        getController().enqueue(new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(),
                CommandClassSecurityV1.getSecurityNonceReport(ourNonce.getNonceBytes()))
                        .withPriority(TransactionPriority.High).build());
    }

    public byte[] getSecurityMessageDecapsulation(byte[] ciphertextBytes) {
        Map<String, Object> response = CommandClassSecurityV1.handleSecurityMessageEncapsulation(ciphertextBytes);

        // Get the IV
        byte[] initializationVector = new byte[16];
        System.arraycopy(response.get("INITIALIZATION_VECTOR_BYTE"), 0, initializationVector, 0, 8);
        System.arraycopy(ourNonce.getNonceBytes(), 0, initializationVector, 8, 8);

        byte[] cyphertextMac = new byte[8];
        System.arraycopy(response.get("MESSAGE_AUTHENTICATION_CODE_BYTE"), 0, cyphertextMac, 0, 8);

        if ((Integer) response.get("RECEIVERS_NONCE_IDENTIFIER") != ourNonce.getId()) {
            logger.debug("NODE {}: SECURITY_ERROR NONCE ID invalid!", getNode().getNodeId());
            return null;
        }

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/OFB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new IvParameterSpec(initializationVector));
            byte[] plaintextBytes = cipher.doFinal((byte[]) response.get("COMMAND_BYTE"));

            byte[] messageAuthenticationCode = generateMAC((byte[]) response.get("COMMAND_BYTE"),
                    (byte) getNode().getNodeId(), (byte) getController().getOwnNodeId(), initializationVector);

            if (!Arrays.equals(messageAuthenticationCode, (byte[]) response.get("MESSAGE_AUTHENTICATION_CODE_BYTE"))) {
                logger.debug("NODE {}: SECURITY_ERROR Failed authentication!", getNode().getNodeId());
                return null;
            }

            return plaintextBytes;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public byte[] getSecurityMessageEncapsulation(byte[] payload) {

        /// tmpNonce.setNonceBytes(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        // theirNonce.setNonceBytes(new byte[] { 1, 1, 1, 1, 1, 1, 1, 1 });

        // Create the initialisation vector which is an 8 byte random number followed by their nonce
        ZWaveNonce tmpNonce = new ZWaveNonce();
        byte[] initializationVector = new byte[16];
        System.arraycopy(tmpNonce.getNonceBytes(), 0, initializationVector, 0, 8);
        System.arraycopy(theirNonce.getNonceBytes(), 0, initializationVector, 8, 8);

        try {
            // Create the message payload with a fake MAC
            // This puts all the elements in the correct part of the packet for encryption
            byte[] messageAuthenticationCode = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
            byte[] securePayload = CommandClassSecurityV1.getSecurityMessageEncapsulation(tmpNonce.getNonceBytes(), 0,
                    false, false, payload, (int) theirNonce.getId(), messageAuthenticationCode);

            // Now encrypt the secure part of the securePayload
            Cipher encryptCipher = Cipher.getInstance("AES/OFB/NoPadding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new IvParameterSpec(initializationVector));
            byte[] ciphertextBytes = encryptCipher.doFinal(securePayload, 10, securePayload.length - 19);
            System.arraycopy(ciphertextBytes, 0, securePayload, 10, securePayload.length - 19);

            // Now generate the MAC
            messageAuthenticationCode = generateMAC(securePayload, (byte) getController().getOwnNodeId(),
                    (byte) getNode().getNodeId(), initializationVector);

            // And copy the MAC to the end of securePayload
            System.arraycopy(messageAuthenticationCode, 0, securePayload, securePayload.length - 8, 8);

            return securePayload;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    // @ZWaveResponseHandler(id = CommandClassSecurityV1.SECURITY_MESSAGE_ENCAPSULATION, name =
    // "SECURITY_MESSAGE_ENCAPSULATION")
    // public void handleSecurityMessageEncapsulation(ZWaveCommandClassPayload payload, int endpoint) {
    // CommandClassSecurityV1.handleSecurityMessageEncapsulation(payload);
    // }

    // @ZWaveResponseHandler(id = CommandClassSecurityV1.SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET, name =
    // "SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET")
    // public void handleSecurityMessageEncapsulationNonceGet(ZWaveCommandClassPayload payload, int endpoint) {
    // }

    /**
     * Sets the network key. The key is provided as a string of hexadecimal values. Values can be space or comma
     * delimitered, or can have no separation between values. Values can be prefixed with 0x or not.
     *
     * @param value {@link String} containing the new network key
     */
    public void setNetworkKey(String value) {
        if (value == null) {
            logger.debug("Network key must not be null");
            return;
        }

        String hexString = value.replace("0x", "");
        hexString = hexString.replace(",", "");
        hexString = hexString.replace(" ", "");

        if ((hexString.length() % 2) != 0) {
            logger.debug("Network key must contain an even number of characters");
            return;
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

        setupNetworkKey(true);
    }

    public void setupNetworkKey(boolean useSchemeZero) {
        logger.info("NODE {}: setupNetworkKey useSchemeZero={}", getNode().getNodeId(), useSchemeZero);
        SecretKey key;
        if (useSchemeZero) {
            logger.info("NODE {}: Using Scheme0 Network Key for Key Exchange since we are in inclusion mode.",
                    getNode().getNodeId());
            // Scheme0 network key is a key of all zeros
            key = new SecretKeySpec(new byte[16], AES);
        } else {
            // Use the real key
            logger.trace("NODE {}: Using Real Network Key.", getNode().getNodeId());
            key = networkKey;
        }

        try {
            // Derived the message encryption key from the network key
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptionKey = new SecretKeySpec(cipher.doFinal(DERIVE_ENCRYPT_KEY), AES);

            // Derived the message auth key from the network key
            cipher.init(Cipher.ENCRYPT_MODE, key);
            authenticationKey = new SecretKeySpec(cipher.doFinal(DERIVE_AUTH_KEY), AES);
        } catch (GeneralSecurityException e) {
            logger.error("NODE {}: Error building derived keys {}", getNode().getNodeId(), e);
        }
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
            logger.debug("NODE {}: isNonceAvailable = null", getNode().getNodeId());
            return false;
        }
        return theirNonce.isValid();
    }

    /**
     * Generate the MAC (Message Authentication Code) for an encrypted message
     *
     * @param commandByte the security command we're sending
     * @param ciphertext
     * @param sendingNode
     * @param receivingNode
     * @param iv
     * @return
     * @throws GeneralSecurityException
     */
    public byte[] generateMAC(byte[] payload, byte sendingNode, byte receivingNode, byte[] iv)
            throws GeneralSecurityException {
        // Build a buffer containing a 4-byte header and the encrypted message data
        // padded with zeros to a 16-byte boundary.
        byte[] tempAuth = new byte[16];

        int bufferSize = payload.length - 19 + 4; // -19 for packet overhead, +4 to account for the header
        byte[] buffer = new byte[bufferSize];
        buffer[0] = payload[1]; // Command byte
        buffer[1] = sendingNode;
        buffer[2] = receivingNode;
        buffer[3] = (byte) (payload.length - 19);

        System.arraycopy(payload, 10, buffer, 4, payload.length - 19);

        // Encrypt the IV with ECB
        Cipher encryptCipher = Cipher.getInstance("AES/ECB/NoPadding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, authenticationKey);
        tempAuth = encryptCipher.doFinal(iv);

        // Working buffer
        byte[] encpck = new byte[16];
        int block = 0;

        // Now XOR the buffer with our encrypted IV
        for (int i = 0; i < bufferSize; i++) {
            encpck[block] = buffer[i];
            block++;

            // If we hit a block boundary, then XOR and encrypt
            if (block == 16) {
                for (int j = 0; j < 16; j++) {
                    // here we do our xor
                    tempAuth[j] = (byte) (encpck[j] ^ tempAuth[j]);
                    encpck[j] = 0;
                }
                // Reset encpck for good measure
                Arrays.fill(encpck, (byte) 0);

                // Reset our block counter back to 0
                block = 0;

                encryptCipher.init(Cipher.ENCRYPT_MODE, authenticationKey);
                tempAuth = encryptCipher.doFinal(tempAuth);
            }
        }

        // Add any left over data that isn't a full block size
        if (block > 0) {
            for (int i = 0; i < 16; i++) {
                // encpck from block to 16 is already guaranteed to be 0 so its safe to XOR it with out tempAuth
                tempAuth[i] = (byte) (encpck[i] ^ tempAuth[i]);
            }

            encryptCipher.init(Cipher.ENCRYPT_MODE, authenticationKey);
            tempAuth = encryptCipher.doFinal(tempAuth);
        }

        // We only care about the first 8 bytes of tempAuth as the MAC
        byte[] mac = new byte[8];
        System.arraycopy(tempAuth, 0, mac, 0, 8);
        return mac;
    }
}
