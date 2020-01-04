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
public class DeleteReturnRouteMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(DeleteReturnRouteMessageClass.class);

    public ZWaveSerialPayload doRequest(int nodeId) {
        logger.debug("NODE {}: Deleting return routes", nodeId);

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.DeleteReturnRoute).withResponseNodeId(nodeId)
                .withPayload(nodeId).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        if (transaction == null) {
            logger.debug("NODE {}: transaction not correlated for DeleteReturnRouteMessageClass");
            return false;
        }
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        logger.debug("NODE {}: Got DeleteReturnRoute response.", nodeId);
        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            // lastSentMessage.setAckRecieved();
            logger.debug("NODE {}: DeleteReturnRoute command started.", nodeId);
        } else {
            logger.debug("NODE {}: DeleteReturnRoute command failed.", nodeId);
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.DeleteReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));
            transaction.setTransactionCanceled();
        }

        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        if (transaction == null) {
            logger.debug("NODE {}: transaction not correlated for DeleteReturnRouteMessageClass");
            return false;
        }
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        logger.debug("NODE {}: Got DeleteReturnRoute request.", nodeId);
        if (incomingMessage.getMessagePayloadByte(1) != 0x00) {
            logger.debug("NODE {}: Delete return routes failed.", nodeId);
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.DeleteReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));
        } else {
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.DeleteReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Success));
        }

        transaction.setTransactionComplete();
        return true;
    }
}
