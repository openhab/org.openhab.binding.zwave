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
 * This class processes a serial message to enable or disable the controller SUC/SIS functionality
 *
 * @author Chris Jackson
 */
public class EnableSucMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EnableSucMessageClass.class);

    public SerialMessage doRequest(SUCType type) {
        logger.debug("Assigning Controller SUC functionality to {}", type.toString());

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessageClass.EnableSuc, SerialMessageType.Request,
                SerialMessageClass.EnableSuc, SerialMessagePriority.High);

        byte[] newPayload = new byte[2];
        switch (type) {
            case NONE:
                newPayload[0] = 0;
                newPayload[1] = 0;
                break;
            case BASIC:
                newPayload[0] = 1;
                newPayload[1] = 0;
                break;
            case SERVER:
                newPayload[0] = 1;
                newPayload[1] = 1;
                break;
        }
        newMessage.setMessagePayload(newPayload);
        return newMessage;
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got EnableSUC response.");

        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("EnableSUC was successful");
        } else {
            logger.debug("Unable to disable a running SUC!");
        }

        checkTransactionComplete(lastSentMessage, incomingMessage);
        return true;
    }

    public enum SUCType {
        NONE,
        BASIC,
        SERVER
    }
}
