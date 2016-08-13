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
public class SerialApiSoftResetMessageClass extends ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SerialApiSoftResetMessageClass.class);

    public ZWaveTransaction doRequest() {

        // Create the request
        SerialMessage serialMessage = new ZWaveMessageBuilder(SerialMessageClass.SerialApiSoftReset).build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.High).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) {
        logger.debug(String.format("Received soft reset response"));

        checkTransactionComplete(transaction, incomingMessage);

        return true;
    }
}
