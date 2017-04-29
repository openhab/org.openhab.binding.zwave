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
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class RequestNodeInfoMessageClass extends ZWaveCommandProcessor {
    private final static Logger logger = LoggerFactory.getLogger(RequestNodeInfoMessageClass.class);

    public SerialMessage doRequest(int nodeId) {
        SerialMessage newMessage = new SerialMessage(SerialMessageClass.RequestNodeInfo, SerialMessageType.Request,
                SerialMessageClass.ApplicationUpdate, SerialMessagePriority.High);
        byte[] newPayload = { (byte) nodeId };
        newMessage.setMessagePayload(newPayload);
        return newMessage;
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.trace("Handle RequestNodeInfo Response");
        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("Request node info successfully placed on stack.");
        } else {
            logger.debug("Request node info not placed on stack due to error.");
        }

        checkTransactionComplete(lastSentMessage, incomingMessage);

        return true;
    }
}
