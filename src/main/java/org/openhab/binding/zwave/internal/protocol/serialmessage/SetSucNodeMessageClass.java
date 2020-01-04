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
public class SetSucNodeMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(SetSucNodeMessageClass.class);

    public ZWaveSerialPayload doRequest(int nodeId, boolean enable) {
        logger.debug("NODE {}: SetSucNodeID node {}", nodeId, enable);

        byte[] payload = new byte[4];
        payload[0] = (byte) nodeId;
        payload[1] = (byte) (enable ? 1 : 0);
        payload[2] = 0;
        payload[3] = (byte) (enable ? 1 : 0);

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
        transaction.setTransactionComplete();
        if (incomingMessage.getMessagePayloadByte(1) != 0x00) {
            logger.error("NODE {}: SetSucNodeID command failed.", nodeId);
            return false;
        } else {
            return true;
        }
    }
}
