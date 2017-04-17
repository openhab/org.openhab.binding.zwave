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
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class GetRoutingInfoMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(GetRoutingInfoMessageClass.class);

    private static final int NODE_BYTES = 29; // 29 bytes = 232 bits, one for each supported node by Z-Wave;

    public ZWaveSerialPayload doRequest(int nodeId) {
        logger.debug("NODE {}: Request routing info", nodeId);

        byte[] payload = { (byte) nodeId, (byte) 0, // Don't remove bad nodes
                (byte) 0, // Don't remove non-repeaters
                (byte) 3 // Function ID
        };

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.GetRoutingInfo).withPayload(payload).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        logger.debug("NODE {}: Got NodeRoutingInfo request.", nodeId);

        // Get the node
        ZWaveNode node = zController.getNode(nodeId);
        if (node == null) {
            logger.error("NODE {}: Routing information for unknown node", nodeId);
            return false;
        }

        node.clearNeighbors();
        boolean hasNeighbors = false;
        for (int by = 0; by < NODE_BYTES; by++) {
            for (int bi = 0; bi < 8; bi++) {
                if ((incomingMessage.getMessagePayloadByte(by) & (0x01 << bi)) != 0) {
                    hasNeighbors = true;

                    // Add the node to the neighbor list
                    node.addNeighbor((by << 3) + bi + 1);
                }
            }
        }

        if (!hasNeighbors) {
            logger.debug("NODE {}: No neighbors reported", nodeId);
        } else {
            String neighbors = "Neighbor nodes:";
            for (Integer neighborNode : node.getNeighbors()) {
                neighbors += " " + neighborNode;
            }
            logger.debug("NODE {}: {}", nodeId, neighbors);
        }

        zController.notifyEventListeners(
                new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.NodeRoutingInfo, nodeId, ZWaveNetworkEvent.State.Success));

        transaction.setTransactionComplete();
        return true;
    }
}
