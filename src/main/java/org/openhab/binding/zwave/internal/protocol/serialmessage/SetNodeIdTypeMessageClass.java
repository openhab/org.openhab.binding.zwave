/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * This class sets the NodeId Type to 8 bits
 *
 * @author Bob Eckhoff - Initial Conteribution
 */
public class SetNodeIdTypeMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(SetNodeIdTypeMessageClass.class);

    public ZWaveSerialPayload doRequest() {
        logger.debug("SetNodeIdType for controller");

        byte[] payload = new byte[2];
        payload[0] = (byte) 0x80;
        payload[1] = (byte) 0x01;  // 1=8bit, 2=16bit

        // Possibly restrict command to library 7
        // GetVersionMessageClass instance = new GetVersionMessageClass();
        // if(instance.getLibraryType() == 7) {}
        // What happens with no command?
        
        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.SetUpZwaveApi).withPayload(payload).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeIdType = transaction.getSerialMessage().getMessagePayloadByte(1);

        logger.debug("SetNodeIdType node response {}", nodeIdType);

        if (incomingMessage.getMessagePayloadByte(1) != 0x00) {
            logger.debug("Eight bit NodeId command OK.");
        } else {
            logger.error("Eight bit NodeId command failed.");
            transaction.setTransactionCanceled();
        }

        return true;
    }
}
