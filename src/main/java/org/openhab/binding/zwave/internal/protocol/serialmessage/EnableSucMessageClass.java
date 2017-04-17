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
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message to enable or disable the controller SUC/SIS functionality
 *
 * @author Chris Jackson
 */
public class EnableSucMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(EnableSucMessageClass.class);

    public ZWaveSerialPayload doRequest(SUCType type) {
        logger.debug("Assigning Controller SUC functionality to {}", type.toString());

        byte[] payload = new byte[2];
        switch (type) {
            case NONE:
                payload[0] = 0;
                payload[1] = 0;
                break;
            case BASIC:
                payload[0] = 1;
                payload[1] = 0;
                break;
            case SERVER:
                payload[0] = 1;
                payload[1] = 1;
                break;
        }

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.EnableSuc).withPayload(payload).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got EnableSUC response.");

        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("EnableSUC was successful");
        } else {
            logger.error("Unable to disable a running SUC!");
        }

        transaction.setTransactionComplete();
        return true;
    }

    public enum SUCType {
        NONE,
        BASIC,
        SERVER
    }
}
