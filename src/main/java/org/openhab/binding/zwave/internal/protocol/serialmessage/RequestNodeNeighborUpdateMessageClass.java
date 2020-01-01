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
public class RequestNodeNeighborUpdateMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(RequestNodeNeighborUpdateMessageClass.class);

    final int REQUEST_NEIGHBOR_UPDATE_STARTED = 0x21;
    final int REQUEST_NEIGHBOR_UPDATE_DONE = 0x22;
    final int REQUEST_NEIGHBOR_UPDATE_FAILED = 0x23;

    public ZWaveSerialPayload doRequest(int nodeId) {
        logger.debug("NODE {}: Request neighbor update", nodeId);

        // Create the request - note the long timeout
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.RequestNodeNeighborUpdate).withPayload(nodeId)
                .withExpectedResponseClass(SerialMessageClass.RequestNodeNeighborUpdate).withResponseNodeId(nodeId)
                .withTimeout(75000).build();
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        // This should only be received as part of a transaction
        if (transaction == null) {
            logger.debug("NodeNeighborUpdate request without transaction");
            return false;
        }

        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        logger.debug("NODE {}: Got NodeNeighborUpdate request.", nodeId);
        switch (incomingMessage.getMessagePayloadByte(1)) {
            case REQUEST_NEIGHBOR_UPDATE_STARTED:
                logger.debug("NODE {}: NodeNeighborUpdate STARTED", nodeId);
                break;
            case REQUEST_NEIGHBOR_UPDATE_DONE:
                logger.debug("NODE {}: NodeNeighborUpdate DONE", nodeId);

                zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.NodeNeighborUpdate,
                        nodeId, ZWaveNetworkEvent.State.Success));
                transaction.setTransactionComplete();
                break;
            case REQUEST_NEIGHBOR_UPDATE_FAILED:
                logger.debug("NODE {}: NodeNeighborUpdate FAILED", nodeId);

                zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.NodeNeighborUpdate,
                        nodeId, ZWaveNetworkEvent.State.Failure));
                transaction.setTransactionCanceled();
                break;
        }
        return true;
    }
}
