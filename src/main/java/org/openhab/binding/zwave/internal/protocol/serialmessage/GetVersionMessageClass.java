/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import org.apache.commons.lang.ArrayUtils;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class GetVersionMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(GetVersionMessageClass.class);

    private String zWaveVersion = "Unknown";
    private int ZWaveLibraryType = 0;

    public SerialMessage doRequest() {
        return new SerialMessage(SerialMessageClass.GetVersion, SerialMessageType.Request,
                SerialMessageClass.GetVersion, SerialMessagePriority.High);
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        ZWaveLibraryType = incomingMessage.getMessagePayloadByte(12);
        zWaveVersion = new String(ArrayUtils.subarray(incomingMessage.getMessagePayload(), 0, 11));
        logger.debug(String.format("Got MessageGetVersion response. Version = %s, Library Type = 0x%02X", zWaveVersion,
                ZWaveLibraryType));

        checkTransactionComplete(lastSentMessage, incomingMessage);

        return true;
    }

    public String getVersion() {
        return zWaveVersion;
    }

    public int getLibraryType() {
        return ZWaveLibraryType;
    }
}
