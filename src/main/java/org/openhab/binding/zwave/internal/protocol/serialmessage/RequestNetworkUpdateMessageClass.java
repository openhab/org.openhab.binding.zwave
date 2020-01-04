/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class RequestNetworkUpdateMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(RequestNetworkUpdateMessageClass.class);

    private final int ZW_SUC_UPDATE_DONE = 0x00;
    private final int ZW_SUC_UPDATE_ABORT = 0x01;
    private final int ZW_SUC_UPDATE_WAIT = 0x02;
    private final int ZW_SUC_UPDATE_DISABLED = 0x03;
    private final int ZW_SUC_UPDATE_OVERFLOW = 0x04;

    public ZWaveSerialPayload doRequest() {
        logger.debug("Request network update.");

        return new ZWaveTransactionMessageBuilder(SerialMessageClass.RequestNetworkUpdate).build();
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

        logger.debug("Got RequestNetworkUpdate request.");
        ZWaveNetworkEvent.State state;
        switch (incomingMessage.getMessagePayloadByte(1)) {
            case ZW_SUC_UPDATE_DONE:
                // The node is working properly (removed from the failed nodes list). Replace process is stopped.
                logger.debug("Network updated.");
                state = ZWaveNetworkEvent.State.Success;
                break;
            case ZW_SUC_UPDATE_ABORT:
                logger.error("The update process aborted because of an error.");
                state = ZWaveNetworkEvent.State.Failure;
                break;
            case ZW_SUC_UPDATE_WAIT:
                logger.error("The SUC node is busy.");
                state = ZWaveNetworkEvent.State.Failure;
                break;
            case ZW_SUC_UPDATE_DISABLED:
                logger.error("The SUC functionality is disabled.");
                state = ZWaveNetworkEvent.State.Failure;
                break;
            case ZW_SUC_UPDATE_OVERFLOW:
                logger.error("The SUC node is busy.");
                state = ZWaveNetworkEvent.State.Failure;
                break;
            default:
                logger.info("The controller requested an update after more than 64 changes");
                state = ZWaveNetworkEvent.State.Failure;
                break;
        }

        zController.notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.RequestNetworkUpdate, 0, state,
                incomingMessage.getMessagePayloadByte(1)));

        return true;
    }
}
