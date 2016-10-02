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
import org.openhab.binding.zwave.internal.protocol.ZWaveMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class RequestNodeInfoMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RequestNodeInfoMessageClass.class);

    public ZWaveTransaction doRequest(int nodeId) {
        // Create the request
        SerialMessage serialMessage = new ZWaveMessageBuilder(SerialMessageClass.RequestNodeInfo).withPayload(nodeId)
                .build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.High).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.trace("Handle RequestNodeInfo Response");
        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("Request node info successfully placed on stack.");
        } else {
            logger.error("Request node info not placed on stack due to error.");
        }

        checkTransactionComplete(transaction, incomingMessage);

        return true;
    }
}
