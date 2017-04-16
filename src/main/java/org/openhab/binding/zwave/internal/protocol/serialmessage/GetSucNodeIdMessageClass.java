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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message to get the SUC node ID
 *
 * @author Chris Jackson
 */
public class GetSucNodeIdMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(GetSucNodeIdMessageClass.class);

    int sucNode = 0;

    public SerialMessage doRequest() {
        logger.debug("Get SUC NodeID");

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessageClass.GetSucNodeId, SerialMessageType.Request,
                SerialMessageClass.GetSucNodeId, SerialMessagePriority.High);

        return newMessage;
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got SUC NodeID response.");

        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("NODE {}: Node is SUC.", incomingMessage.getMessagePayloadByte(0));
            sucNode = incomingMessage.getMessagePayloadByte(0);
        } else {
            logger.debug("No SUC Node is set");
        }

        checkTransactionComplete(lastSentMessage, incomingMessage);
        return true;
    }

    public int getSucNodeId() {
        return sucNode;
    }
}
