/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class SendDataMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(SendDataMessageClass.class);

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.trace("Handle Message Send Data Response");
        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("NODE {}: Sent Data successfully placed on stack.", transaction.getNodeId());

            // This response is our controller ACK
            // lastSentMessage.setAckRecieved();
        } else {
            // This is an error. This means that the transaction is complete!
            // Set the flag, and return false.
            logger.error("NODE {}: Sent Data was not placed on stack.", transaction.getNodeId());

            // We ought to cancel the transaction
            transaction.setTransactionCanceled();

            return false;
        }

        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        // SendData REQuests must be associated with a transaction
        if (transaction == null) {
            return false;
        }

        logger.trace("Handle Message Send Data Request");

        // int callbackId = incomingMessage.getMessagePayloadByte(0);
        TransmissionState status = TransmissionState.getTransmissionState(incomingMessage.getMessagePayloadByte(1));

        if (status == null) {
            logger.warn("Transmission state not found, ignoring.");
            return false;
        }

        ZWaveNode node = zController.getNode(transaction.getNodeId());
        if (node == null) {
            logger.warn("Node {} not found!", transaction.getNodeId());
            return false;
        }

        logger.debug("NODE {}: SendData Request. CallBack ID = {}, Status = {}({})", node.getNodeId(),
                incomingMessage.getCallbackId(), status.getLabel(), status.getKey());

        switch (status) {
            case COMPLETE_OK:
                // Consider this as a received frame since the controller did receive an ACK from the device.
                node.incrementReceiveCount();

                // If the node not ALIVE, but we've just received an ACK from it, then it's ALIVE!
                if (node.getNodeState() != ZWaveNodeState.ALIVE) {
                    node.setNodeState(ZWaveNodeState.ALIVE);
                } else {
                    node.resetResendCount();
                }
                // Mark the transaction as DONE.
                // If the transaction needs data, then it will continue to wait for this data
                transaction.setTransactionComplete();
                return true;
            case COMPLETE_NO_ACK:
                // Handle WAKE_UP_NO_MORE_INFORMATION differently
                // Since the system can time out if the node goes to sleep before
                // we get the response, then don't treat this like a timeout
                byte[] payload = transaction.getSerialMessage().getMessagePayload();
                if (payload.length >= 4
                        && (payload[2] & 0xFF) == ZWaveCommandClass.CommandClass.COMMAND_CLASS_WAKE_UP.getKey()
                        && (payload[3] & 0xFF) == ZWaveWakeUpCommandClass.WAKE_UP_NO_MORE_INFORMATION) {

                    logger.debug("NODE {}: WAKE_UP_NO_MORE_INFORMATION. Treated as ACK.", node.getNodeId());
                    return true;
                }

            case COMPLETE_FAIL:
            case COMPLETE_NOT_IDLE:
            case COMPLETE_NOROUTE:
                try {
                    // handleFailedSendDataRequest(zController, lastSentMessage);
                } finally {
                    // transactionComplete = true;
                }
                transaction.setTransactionCanceled();
                break;
            default:
                break;
        }

        return false;
    }

    /*
     * public boolean handleFailedSendDataRequest(ZWaveController zController, SerialMessage originalMessage) {
     * ZWaveNode node = zController.getNode(originalMessage.getMessageNode());
     * if (node == null) {
     * logger.error("Unknown node in handleFailedSendDataRequest");
     * return false;
     * }
     *
     * logger.debug("NODE {}: Handling failed message.", node.getNodeId());
     *
     * // Increment the resend count.
     * // This will set the node to DEAD if we've exceeded the retries.
     * node.incrementResendCount();
     *
     * // No retries if the node is DEAD or FAILED
     * if (node.isDead()) {
     * logger.error("NODE {}: Node is DEAD. Dropping message.", node.getNodeId());
     * return false;
     * }
     *
     * // If this device isn't listening, queue the message in the wakeup class
     * if (!node.isListening() && !node.isFrequentlyListening()) {
     * ZWaveWakeUpCommandClass wakeUpCommandClass = (ZWaveWakeUpCommandClass) node
     * .getCommandClass(CommandClass.COMMAND_CLASS_WAKE_UP);
     *
     * if (wakeUpCommandClass != null) {
     * // It's a battery operated device, place in wake-up queue.
     * // As this message failed, we assume the device is asleep
     * wakeUpCommandClass.setAwake(false);
     * // wakeUpCommandClass.processOutgoingWakeupMessage(originalMessage);
     * return false;
     * }
     * }
     *
     * logger.error("NODE {}: Got an error while sending data. Resending message.", node.getNodeId());
     * // zController.sendData(originalMessage);
     * return true;
     * }
     */

    /**
     * Transmission state enumeration. Indicates the transmission state of the message to the node.
     *
     * @author Jan-Willem Spuij
     */
    public enum TransmissionState {
        COMPLETE_OK(0x00, "Transmission complete and ACK received"),
        COMPLETE_NO_ACK(0x01, "Transmission complete, no ACK received"),
        COMPLETE_FAIL(0x02, "Transmission failed"),
        COMPLETE_NOT_IDLE(0x03, "Transmission failed, network busy"),
        COMPLETE_NOROUTE(0x04, "Tranmission complete, no return route");

        /**
         * A mapping between the integer code and its corresponding transmission state
         * class to facilitate lookup by code.
         */
        private static Map<Integer, TransmissionState> codeToTransmissionStateMapping;

        private int key;
        private String label;

        private TransmissionState(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToTransmissionStateMapping = new HashMap<Integer, TransmissionState>();
            for (TransmissionState s : values()) {
                codeToTransmissionStateMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the transmission state code.
         * Returns null when there is no transmission state with code i.
         *
         * @param i the code to lookup
         * @return enumeration value of the transmission state.
         */
        public static TransmissionState getTransmissionState(int i) {
            if (codeToTransmissionStateMapping == null) {
                initMapping();
            }

            return codeToTransmissionStateMapping.get(i);
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
}
