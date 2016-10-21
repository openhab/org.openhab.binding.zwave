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
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class SetSucNodeMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SetSucNodeMessageClass.class);

    public ZWaveSerialPayload doRequest(int nodeId, SUCType type) {
        logger.debug("NODE {}: SetSucNodeID node as {}", nodeId, type.toString());

        byte[] payload = new byte[4];
        payload[0] = (byte) nodeId;
        switch (type) {
            case NONE:
                payload[1] = 0;
                payload[3] = 0;
                break;
            case BASIC:
                payload[1] = 1;
                payload[3] = 0;
                break;
            case SERVER:
                payload[1] = 1;
                payload[3] = 1;
                break;
        }

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.SetSucNodeID).withPayload(payload).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        logger.debug("NODE {}: SetSucNodeID node response.", nodeId);

        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("NODE {}: SetSucNodeID command OK.", nodeId);
        } else {
            logger.error("NODE {}: SetSucNodeID command failed.", nodeId);
            transaction.setTransactionCanceled();
        }

        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        logger.debug("NODE {}: SetSucNodeID node request.", nodeId);

        checkTransactionComplete(transaction, incomingMessage);
        if (incomingMessage.getMessagePayloadByte(1) != 0x00) {
            logger.error("NODE {}: SetSucNodeID command failed.", nodeId);
            return false;
        } else {
            return true;
        }
    }

    public enum SUCType {
        NONE,
        BASIC,
        SERVER
    }

}
