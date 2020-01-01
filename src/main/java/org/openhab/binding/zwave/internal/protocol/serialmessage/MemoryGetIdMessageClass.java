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
public class MemoryGetIdMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(MemoryGetIdMessageClass.class);

    private int homeId = 0;
    private int ownNodeId = 0;

    public ZWaveSerialPayload doRequest() {
        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.MemoryGetId).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        homeId = ((incomingMessage.getMessagePayloadByte(0)) << 24) | ((incomingMessage.getMessagePayloadByte(1)) << 16)
                | ((incomingMessage.getMessagePayloadByte(2)) << 8) | (incomingMessage.getMessagePayloadByte(3));
        ownNodeId = incomingMessage.getMessagePayloadByte(4);
        logger.debug("Got MessageMemoryGetId response. Home id = 0x{}, Controller Node id = {}",
                Integer.toHexString(homeId), ownNodeId);

        transaction.setTransactionComplete();
        return true;
    }

    public int getNodeId() {
        return ownNodeId;
    }

    public int getHomeId() {
        return homeId;
    }
}
