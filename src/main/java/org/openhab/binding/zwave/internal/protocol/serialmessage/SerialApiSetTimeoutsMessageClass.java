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
public class SerialApiSetTimeoutsMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(SerialApiSetTimeoutsMessageClass.class);

    public SerialMessage doRequest(int ackTimeout, int byteTimeout) {
        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessageClass.SerialApiSetTimeouts, SerialMessageType.Request,
                SerialMessageClass.SerialApiSetTimeouts, SerialMessagePriority.High);

        byte[] newPayload = { (byte) ackTimeout, (byte) byteTimeout };

        newMessage.setMessagePayload(newPayload);
        return newMessage;
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got SerialApiSetTimeouts response. ACK={}, BYTE={}", incomingMessage.getMessagePayloadByte(0),
                incomingMessage.getMessagePayloadByte(1));

        checkTransactionComplete(lastSentMessage, incomingMessage);

        return true;
    }
}
