package org.openhab.binding.zwave.internal.protocol.serialmessage;

import org.openhab.binding.zwave.internal.protocol.*;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authored by dushan.p@viewqwest.com on 2/8/17.
 */
public class ApplicationCommandHandlerBridgeMessageClass extends ApplicationCommandMessageClass {

    private final static Logger logger = LoggerFactory.getLogger(ApplicationCommandHandlerBridgeMessageClass.class);

    /**
     * Method for handling the request from the controller
     *
     * @param zController     the ZWave controller
     * @param lastSentMessage The original message we sent to the controller
     * @param incomingMessage The response from the controller
     * @return
     * @throws ZWaveSerialMessageException
     */
    @Override
    public boolean handleRequest(ZWaveController zController, SerialMessage lastSentMessage, SerialMessage incomingMessage) {
        try {
            logger.trace("Handle Message Application Command Request");
            int nodeId = incomingMessage.getMessagePayloadByte(2);
            ZWaveNode node = zController.getNode(nodeId);

            if (node == null) {
                logger.warn("NODE {}: Not initialized yet, ignoring message.", nodeId);
                return false;
            }
            logger.debug("NODE {}: Application Command Request ({}:{})", nodeId, node.getNodeState().toString(),
                    node.getNodeInitStage().toString());

            // We've just received a message from a node, therefore it's ALIVE!
            node.setNodeState(ZWaveNodeState.ALIVE);

            node.resetResendCount();
            node.incrementReceiveCount();

            int commandClassCode = incomingMessage.getMessagePayloadByte(4);

            ZWaveCommandClass zwaveCommandClass = resolveZWaveCommandClass(node, commandClassCode, zController);
            if (zwaveCommandClass == null) {
                return false; // Error message was logged in resolveZWaveCommandClass
            }

            final int commandByte = incomingMessage.getMessagePayloadByte(5);
            if (zwaveCommandClass instanceof ZWaveSecurityCommandClass && (ZWaveSecurityCommandClass
                    .bytesAreEqual(ZWaveSecurityCommandClass.SECURITY_MESSAGE_ENCAP, commandByte)
                    || ZWaveSecurityCommandClass
                    .bytesAreEqual(ZWaveSecurityCommandClass.SECURITY_MESSAGE_ENCAP_NONCE_GET, commandByte))) {
                boolean isEncapNonceGet = ZWaveSecurityCommandClass
                        .bytesAreEqual(ZWaveSecurityCommandClass.SECURITY_MESSAGE_ENCAP_NONCE_GET, commandByte);

                // Intercept security encapsulated messages here and decrypt them.
                // TODO: Decide if this should be here, or treated like other encapsulation classes........
                // TODO: It just feels a bit wrong as it breaks protocol layers (which may be needed of course!)
                ZWaveSecurityCommandClass zwaveSecurityCommandClass = (ZWaveSecurityCommandClass) zwaveCommandClass;
                logger.debug("NODE {}: Preparing to decrypt security encapsulated message, messagePayload={}", nodeId,
                        SerialMessage.bb2hex(incomingMessage.getMessagePayload()));
                int toDecryptLength = incomingMessage.getMessageBuffer().length - 9;
                byte[] toDecrypt = new byte[toDecryptLength];
                System.arraycopy(incomingMessage.getMessageBuffer(), 8, toDecrypt, 0, toDecryptLength);
                byte[] decryptedBytes = zwaveSecurityCommandClass.decryptMessage(toDecrypt, 0);
                if (decryptedBytes == null) {
                    logger.error("NODE {}: Failed to decrypt message out of {} .", nodeId, incomingMessage);
                } else {
                    // call handleApplicationCommandRequest with the decrypted message. Note that we do NOT set
                    // incomingMessage as that needs to be processed below with the original security encapsulated
                    // message
                    final SerialMessage decryptedMessage = new SerialMessage(incomingMessage.getMessageClass(),
                            incomingMessage.getMessageType(), incomingMessage.getExpectedReply(),
                            incomingMessage.getPriority());
                    decryptedMessage.setMessagePayload(decryptedBytes);
                    // Get the new command class with the decrypted contents
                    zwaveCommandClass = resolveZWaveCommandClass(node, decryptedBytes[1], zController);
                    boolean failed = false; // Use a flag bc we need to handle isEncapNonceGet either way
                    if (zwaveCommandClass == null) {
                        failed = true; // Error message was logged in resolveZWaveCommandClass
                    } else {
                        // Note that we do not call node.doesMessageRequireSecurityEncapsulation since it was
                        // encapsulated.
                        // Messages that are not required to be are allowed to be, just not the other way around
                        logger.debug(
                                "NODE {}: After decrypt, found Command Class {}, passing to handleApplicationCommandRequest",
                                nodeId, zwaveCommandClass.getCommandClass().getLabel());
                        zwaveCommandClass.handleApplicationCommandRequest(decryptedMessage, 2, 0);
                    }
                    if (isEncapNonceGet) {
                        // the device also needs another nonce; send it regardless of the success/failure of decryption
                        zwaveSecurityCommandClass.sendNonceReport();
                    }
                    if (failed) {
                        return false;
                    }
                }
            } else { // Message does not require decryption
                if (node.doesMessageRequireSecurityEncapsulation(incomingMessage)) {
                    // Should have been security encapsulation but wasn't!
                    logger.error(
                            "NODE {}: Command Class {} {} was required to be security encapsulation but it wasn't!  Dropping message.",
                            nodeId, zwaveCommandClass.getCommandClass().getKey(),
                            zwaveCommandClass.getCommandClass().getLabel());
                    // do not call zwaveCommandClass.handleApplicationCommandRequest();
                } else {
                    logger.trace("NODE {}: Found Command Class {}, passing to handleApplicationCommandRequest", nodeId,
                            zwaveCommandClass.getCommandClass().getLabel());
                    zwaveCommandClass.handleApplicationCommandRequest(incomingMessage, 5, 0);
                }
            }

            if (node.getNodeId() == lastSentMessage.getMessageNode()) {
                checkTransactionComplete(lastSentMessage, incomingMessage);
            } else {
                logger.debug("NODE {}: Transaction not completed: node address inconsistent.  lastSent={}, incoming={}",
                        lastSentMessage.getMessageNode(), lastSentMessage.getMessageNode(),
                        incomingMessage.getMessageNode());
            }
        } catch (ZWaveSerialMessageException e) {
            logger.error("Error processing frame: {} >> {}", incomingMessage.toString(), e.getMessage());
        }
        return true;
    }
}
