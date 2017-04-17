/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class ApplicationCommandMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(ApplicationCommandMessageClass.class);

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) {
        try {
            int nodeId = incomingMessage.getMessagePayloadByte(1);
            ZWaveNode node = zController.getNode(nodeId);

            if (node == null) {
                logger.warn("NODE {}: Not initialized yet (ie node unknown), ignoring message.", nodeId);
                return false;
            }
            logger.debug("NODE {}: Application Command Request ({}:{})", nodeId, node.getNodeState().toString(),
                    node.getNodeInitStage().toString());

            node.processCommand(new ZWaveCommandClassPayload(incomingMessage));

            // If we have a transaction, then it's been correlated with the expected response.
            // We can therefore complete it
            if (transaction != null) {
                transaction.setTransactionComplete();
            }
        } catch (ZWaveSerialMessageException e) {
            logger.error("Error processing frame: {} >> {}", incomingMessage.toString(), e.getMessage());
        }
        return true;
    }

    @Override
    public boolean correlateTransactionResponse(ZWaveTransaction transaction, SerialMessage incomingMessage) {
        if (transaction == null) {
            return false;
        }

        logger.debug("ApplicationCommandClass correlateTransactionResponse: transaction: {}", transaction);
        // logger.debug("ApplicationCommandClass correlateTransactionResponse: reply class: {}",
        // transaction.getExpectedReplyClass());
        logger.debug("ApplicationCommandClass correlateTransactionResponse: expected cmd class: {}",
                transaction.getExpectedCommandClass());
        logger.debug("ApplicationCommandClass correlateTransactionResponse: expected cmd: {}",
                transaction.getExpectedCommandClassCommand());
        // logger.debug("ApplicationCommandClass correlateTransactionResponse: incoming class: {}",
        // incomingMessage.getMessageClass());

        if (transaction.getExpectedReplyClass() != incomingMessage.getMessageClass()) {
            logger.debug("NO EXPECTED REPLY CLASS match! ({} <> {})", transaction.getExpectedReplyClass(),
                    incomingMessage.getMessageClass());
            return false;
        }

        // If this is a response, check the callbackId
        try {
            // If the expected command class is defined, then check it
            if (transaction.getExpectedCommandClass() == null
                    || transaction.getExpectedCommandClass().getKey() != incomingMessage.getMessagePayloadByte(3)) {
                logger.debug("NO EXPECTED COMMAND CLASS match! ({} <> {}) - {}", transaction.getExpectedCommandClass(),
                        incomingMessage.getMessagePayloadByte(3), SerialMessage.bb2hex(transaction.getPayloadBuffer()));

                return false;
            }

            // If the expected command class command is defined, then check it
            if (transaction.getExpectedCommandClassCommand() == null
                    || transaction.getExpectedCommandClassCommand() != incomingMessage.getMessagePayloadByte(4)) {
                logger.debug("NO EXPECTED COMMAND CLASS COMMAND match! ({} <> {}) - {}",
                        transaction.getExpectedCommandClassCommand(), incomingMessage.getMessagePayloadByte(4),
                        SerialMessage.bb2hex(transaction.getPayloadBuffer()));

                return false;
            }

            return true;
        } catch (ZWaveSerialMessageException e) {
            logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! EXCEPTION");
        }

        return false;
    }
}
