/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * The {@link ZWaveCommandClassPayload} implements an encapsulated command class payload.
 *
 * @author Chris Jackson - Initial implementation
 *
 */
public class ZWaveCommandClassPayload implements ZWaveMessagePayload {
    protected final byte[] payload;

    public ZWaveCommandClassPayload(final byte[] payload) {
        this.payload = payload;
    }

    public ZWaveCommandClassPayload(final ZWaveCommandClassPayload initialPayload, final int start) {
        payload = Arrays.copyOfRange(initialPayload.getPayloadBuffer(), start, initialPayload.getPayloadLength());
    }

    public ZWaveCommandClassPayload(final ZWaveCommandClassPayload initialPayload, final int start, final int end) {
        payload = Arrays.copyOfRange(initialPayload.getPayloadBuffer(), start, end);
    }

    public ZWaveCommandClassPayload(final SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        ByteArrayOutputStream payloadData = new ByteArrayOutputStream();
        int length = incomingMessage.getMessagePayloadByte(2) + 3;
        for (int index = 3; index < length; index++) {
            payloadData.write((byte) incomingMessage.getMessagePayloadByte(index));
        }

        this.payload = payloadData.toByteArray();
    }

    public int getCommandClassId() {
        return payload[0] & 0xFF;
    }

    public int getCommandClassCommand() {
        if (payload.length >= 2) {
            return payload[1] & 0xFF;
        }
        return -1;
    }

    public int getPayloadByte(int offset) {
        return payload[offset] & 0xFF;
    }

    public int getPayloadLength() {
        return payload.length;
    }

    @Override
    public byte[] getPayloadBuffer() {
        return payload;
    }

    public byte[] getPayloadBuffer(int start, int end) {
        return Arrays.copyOfRange(payload, start, end);
    }
}
