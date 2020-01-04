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
            logger.debug("NODE {}: sentData successfully placed on stack.", transaction.getNodeId());

            // This response is our controller ACK
            // lastSentMessage.setAckRecieved();
        } else {
            // The packet was not accepted! It should be retried.
            // Set the flag, and return false.
            logger.debug("NODE {}: sentData was not placed on stack.", transaction.getNodeId());

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
            logger.debug("Node {} not found!", transaction.getNodeId());
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
                // If we're waiting for data, then don't complete the transaction
                if (transaction.getExpectedReplyClass() == null) {
                    transaction.setTransactionComplete();
                }
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
                // handleFailedSendDataRequest(zController, transaction);
                transaction.setTransactionCanceled();
                break;

            default:
                break;
        }

        return false;
    }

    /**
     * Transmission state enumeration. Indicates the transmission state of the message to the node.
     *
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

        private final int key;
        private final String label;

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
