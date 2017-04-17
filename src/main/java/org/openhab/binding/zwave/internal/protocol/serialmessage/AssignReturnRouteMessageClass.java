/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import java.io.ByteArrayOutputStream;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class AssignReturnRouteMessageClass extends ZWaveCommandProcessor {
    private final static Logger logger = LoggerFactory.getLogger(AssignReturnRouteMessageClass.class);

    public SerialMessage doRequest(int nodeId, int destinationId, int callbackId) {
        logger.debug("NODE {}: Assigning return route to node {}", nodeId, destinationId);

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessageClass.AssignReturnRoute, SerialMessageType.Request,
                SerialMessageClass.AssignReturnRoute, SerialMessagePriority.High);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(nodeId);
        outputData.write(destinationId);
        outputData.write(callbackId);
        newMessage.setMessagePayload(outputData.toByteArray());

        return newMessage;
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        logger.debug("NODE {}: Got AssignReturnRoute response.", nodeId);

        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("NODE {}: AssignReturnRoute command in progress.", nodeId);
            lastSentMessage.setAckRecieved();
        } else {
            logger.debug("NODE {}: AssignReturnRoute command failed.", nodeId);
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssignReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));
            incomingMessage.setTransactionCanceled();
        }

        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        logger.debug("NODE {}: Got AssignReturnRoute request.", nodeId);
        if (incomingMessage.getMessagePayloadByte(1) != 0x00) {
            logger.debug("NODE {}: Assign return routes failed.", nodeId);
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssignReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));
        } else {
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssignReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Success));
        }

        checkTransactionComplete(lastSentMessage, incomingMessage);

        return true;
    }
}
