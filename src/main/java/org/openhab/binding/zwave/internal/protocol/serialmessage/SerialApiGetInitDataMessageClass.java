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

import java.util.ArrayList;

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
public class SerialApiGetInitDataMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(SerialApiGetInitDataMessageClass.class);

    private final ArrayList<Integer> zwaveNodes = new ArrayList<Integer>();

    private static final int NODE_BYTES = 29; // 29 bytes = 232 bits, one for each supported node by Z-Wave;

    public ZWaveSerialPayload doRequest() {
        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.SerialApiGetInitData).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got MessageSerialApiGetInitData response.");
        int nodeBytes = incomingMessage.getMessagePayloadByte(2);

        if (nodeBytes != NODE_BYTES) {
            logger.error("Invalid number of node bytes = {}", nodeBytes);
            return false;
        }

        int nodeId = 1;

        // loop bytes
        for (int i = 3; i < 3 + nodeBytes; i++) {
            int incomingByte = incomingMessage.getMessagePayloadByte(i);
            // loop bits in byte
            for (int j = 0; j < 8; j++) {
                int b1 = incomingByte & (int) Math.pow(2.0D, j);
                int b2 = (int) Math.pow(2.0D, j);
                if (b1 == b2) {
                    logger.debug("NODE {}: Node found", nodeId);

                    zwaveNodes.add(nodeId);
                }
                nodeId++;
            }
        }

        logger.debug("ZWave Controller using {} API",
                ((incomingMessage.getMessagePayloadByte(1) & 0x01) == 1) ? "Slave" : "Controller");
        logger.debug("ZWave Controller is {} Controller",
                ((incomingMessage.getMessagePayloadByte(1) & 0x04) == 1) ? "Secondary" : "Primary");
        logger.debug("------------Number of Nodes Found Registered to ZWave Controller------------");
        logger.debug("# Nodes = {}", zwaveNodes.size());
        logger.debug("----------------------------------------------------------------------------");

        transaction.setTransactionComplete();
        return true;
    }

    public ArrayList<Integer> getNodes() {
        return zwaveNodes;
    }
}
