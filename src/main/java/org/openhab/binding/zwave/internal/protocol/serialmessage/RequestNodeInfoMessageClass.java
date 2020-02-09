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
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class RequestNodeInfoMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(RequestNodeInfoMessageClass.class);

    public ZWaveSerialPayload doRequest(int nodeId) {
        // Create the request - note the long timeout
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.RequestNodeInfo).withPayload(nodeId)
                .withExpectedResponseClass(SerialMessageClass.ApplicationUpdate).withResponseNodeId(nodeId)
                .withTimeout(25000).withRequiresData(true).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.trace("Handle RequestNodeInfo Response");
        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("Request node info successfully placed on stack.");
        } else {
            logger.debug("Request node info not placed on stack due to error.");
            transaction.setTransactionCanceled();
        }

        return true;
    }
}
