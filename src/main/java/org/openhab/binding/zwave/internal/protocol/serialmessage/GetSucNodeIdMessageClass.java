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
 * This class processes a serial message to get the SUC node ID
 *
 * @author Chris Jackson
 */
public class GetSucNodeIdMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(GetSucNodeIdMessageClass.class);

    private int sucNode = 0;

    public ZWaveSerialPayload doRequest() {
        logger.debug("Get SUC NodeID");

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.GetSucNodeId).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got SUC NodeID response.");

        if (incomingMessage.getMessagePayloadByte(0) != 0x00) {
            logger.debug("NODE {}: Node is SUC.", incomingMessage.getMessagePayloadByte(0));
            sucNode = incomingMessage.getMessagePayloadByte(0);
        } else {
            logger.debug("No SUC Node is set");
        }

        transaction.setTransactionComplete();

        return true;
    }

    public int getSucNodeId() {
        return sucNode;
    }
}
