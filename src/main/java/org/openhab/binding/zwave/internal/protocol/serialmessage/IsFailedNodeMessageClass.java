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

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller.
 * It queries the controller to see if the node is on its 'failed nodes' list.
 *
 * @author Chris Jackson
 * @author Wez Hunter
 */
public class IsFailedNodeMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(IsFailedNodeMessageClass.class);

    public ZWaveSerialPayload doRequest(int nodeId) {
        logger.debug("NODE {}: Requesting IsFailedNode status from controller.", nodeId);

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.IsFailedNodeID).withPayload(nodeId).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        ZWaveNode node = zController.getNode(nodeId);
        if (node == null) {
            logger.debug("NODE {}: Failed node message for unknown node", nodeId);
            return false;
        }

        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("NODE {}: Is currently marked as failed by the controller!", nodeId);
            // node.setNodeState(ZWaveNodeState.FAILED);
        } else {
            logger.debug("NODE {}: Is currently marked as healthy by the controller", nodeId);
            node.setNodeState(ZWaveNodeState.ALIVE);
        }

        transaction.setTransactionComplete();
        return true;
    }
}
