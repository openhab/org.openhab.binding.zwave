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
public class RequestNetworkUpdateMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(RequestNetworkUpdateMessageClass.class);

    private final int ZW_SUC_UPDATE_DONE = 0x00;
    private final int ZW_SUC_UPDATE_ABORT = 0x01;
    private final int ZW_SUC_UPDATE_WAIT = 0x02;
    private final int ZW_SUC_UPDATE_DISABLED = 0x03;
    private final int ZW_SUC_UPDATE_OVERFLOW = 0x04;

    public ZWaveTransaction doRequest() {
        logger.debug("Request network update.");

        SerialMessage serialMessage = new ZWaveMessageBuilder(SerialMessageClass.RequestNetworkUpdate).build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.High).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got RequestNetworkUpdate response.");

        if (incomingMessage.getMessagePayloadByte(0) == 0x01) {
            logger.debug("RequestNetworkUpdate started.");
        } else {
            logger.warn("RequestNetworkUpdate not placed on stack.");
        }

        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {

        logger.debug("Got ReplaceFailedNode request.");
        switch (incomingMessage.getMessagePayloadByte(1)) {
            case ZW_SUC_UPDATE_DONE:
                // The node is working properly (removed from the failed nodes list). Replace process is stopped.
                logger.debug("Network updated.");
                break;
            case ZW_SUC_UPDATE_ABORT:
                logger.error("The update process aborted because of an error.");
                break;
            case ZW_SUC_UPDATE_WAIT:
                logger.error("The SUC node is busy.");
                break;
            case ZW_SUC_UPDATE_DISABLED:
                logger.error("The SUC functionality is disabled.");
                break;
            case ZW_SUC_UPDATE_OVERFLOW:
                logger.error("The SUC node is busy.");
                break;
            default:
                logger.info("The controller requested an update after more than 64 changes");
                break;
        }

        return true;
    }
}
