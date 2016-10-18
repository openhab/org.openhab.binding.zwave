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
    private static final Logger logger = LoggerFactory.getLogger(ApplicationCommandMessageClass.class);

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

            checkTransactionComplete(transaction, incomingMessage);
        } catch (ZWaveSerialMessageException e) {
            logger.error("Error processing frame: {} >> {}", incomingMessage.toString(), e.getMessage());
        }
        return true;
    }

    @Override
    public boolean correlateTransactionResponse(ZWaveTransaction transaction, SerialMessage incomingMessage) {
        if (transaction.getExpectedReplyClass() != incomingMessage.getMessageClass()) {
            return false;
        }

        // If this is a response, check the callbackId
        try {
            // If the expected command class is defined, then check it
            if (transaction.getExpectedCommandClass() == null
                    || transaction.getExpectedCommandClass().getKey() != incomingMessage.getMessagePayloadByte(3)) {
                return false;
            }

            // If the expected command class command is defined, then check it
            if (transaction.getExpectedCommandClassCommand() == null
                    || transaction.getExpectedCommandClassCommand() != incomingMessage.getMessagePayloadByte(4)) {
                return false;
            }

            return true;
        } catch (ZWaveSerialMessageException e) {
        }

        return false;
    }
}
