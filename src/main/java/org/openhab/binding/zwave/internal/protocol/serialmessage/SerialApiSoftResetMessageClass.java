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
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class SerialApiSoftResetMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(SerialApiSoftResetMessageClass.class);

    public SerialMessage doRequest() {
        return new SerialMessage(SerialMessageClass.SerialApiSoftReset, SerialMessageType.Request,
                SerialMessageClass.SerialApiSoftReset, SerialMessagePriority.High);
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) {
        logger.debug(String.format("Received soft reset response"));

        checkTransactionComplete(lastSentMessage, incomingMessage);

        return true;
    }
}
