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
 * @author Jorg de Jong
 */
public class DeleteSucReturnRouteMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DeleteSucReturnRouteMessageClass.class);

    public ZWaveSerialPayload doRequest(int nodeId) {
        logger.debug("NODE {}: Deleting SUC return routes", nodeId);

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.DeleteSUCReturnRoute).withPayload(nodeId).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        logger.debug("NODE {}: Got DeleteSUCReturnRoute response.", nodeId);
        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            // lastSentMessage.setAckRecieved();
            logger.debug("NODE {}: DeleteSUCReturnRoute command in progress.", nodeId);
        } else {
            logger.error("NODE {}: DeleteSUCReturnRoute command failed.", nodeId);
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.DeleteSucReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));
        }

        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        logger.debug("NODE {}: Got DeleteSUCReturnRoute request.", nodeId);
        if (incomingMessage.getMessagePayloadByte(1) != 0x00) {
            logger.error("NODE {}: Delete SUC return routes failed.", nodeId);
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.DeleteSucReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));
        } else {
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.DeleteSucReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Success));
        }

        return true;
    }
}
