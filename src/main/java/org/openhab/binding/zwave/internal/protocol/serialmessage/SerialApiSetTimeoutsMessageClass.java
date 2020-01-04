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
public class SerialApiSetTimeoutsMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(SerialApiSetTimeoutsMessageClass.class);

    /**
     * Sets the serial API timeouts. Timeouts are in 10ms ticks.
     *
     * @param ackTimeout timeout from sending a frame to receiving the ACK
     * @param byteTimeout timeout between receiving each subsequent byte
     * @return {@line ZWaveSerialPayload} the message to send
     */
    public ZWaveSerialPayload doRequest(int ackTimeout, int byteTimeout) {
        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.SerialApiSetTimeouts)
                .withPayload(ackTimeout, byteTimeout).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got SerialApiSetTimeouts response. ACK={}, BYTE={}", incomingMessage.getMessagePayloadByte(0),
                incomingMessage.getMessagePayloadByte(1));

        transaction.setTransactionComplete();
        return true;
    }
}
