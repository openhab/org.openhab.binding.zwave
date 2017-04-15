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
public class AssignSucReturnRouteMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(AssignSucReturnRouteMessageClass.class);

    public SerialMessage doRequest(int nodeId, int callbackId) {
        logger.debug("NODE {}: Assigning SUC return route", nodeId);

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessageClass.AssignSucReturnRoute, SerialMessageType.Request,
                SerialMessageClass.AssignSucReturnRoute, SerialMessagePriority.High);
        byte[] newPayload = { (byte) nodeId, (byte) callbackId };
        newMessage.setMessagePayload(newPayload);
        return newMessage;
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        logger.debug("NODE {}: Got AssignSucReturnRoute response.", nodeId);

        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("NODE {}: AssignSucReturnRoute operation started.", nodeId);
        } else {
            logger.debug("NODE {}: AssignSucReturnRoute command failed.", nodeId);
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssignSucReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));
        }

        checkTransactionComplete(lastSentMessage, incomingMessage);
        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        logger.debug("NODE {}: Got AssignSucReturnRoute request.", nodeId);

        if (incomingMessage.getMessagePayloadByte(1) != 0x00) {
            logger.debug("NODE {}: Assign SUC return routes failed with error 0x{}.", nodeId,
                    Integer.toHexString(incomingMessage.getMessagePayloadByte(1)));
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssignSucReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Failure));

            return false;
        } else {
            zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssignSucReturnRoute, nodeId,
                    ZWaveNetworkEvent.State.Success));

            return true;
        }
    }
}
