package org.openhab.binding.zwave.internal.protocol;

import java.util.Arrays;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;

/**
 * The {@link ZWaveSerialPayload} implements an encapsulated serial payload.
 *
 * @author Chris Jackson - Initial implementation
 *
 */
public class ZWaveSerialPayload implements ZWaveMessagePayload {
    private final byte[] payload;
    SerialMessageClass messageClass;

    public ZWaveSerialPayload(final byte[] payload) {
        this.payload = payload;
    }

    public ZWaveSerialPayload(final SerialMessageClass messageClass, byte[] payload) {
        this.messageClass = messageClass;
        this.payload = payload;
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

    // @Override
    // String toString() {
    // return String.format("Class:%02X, Cmd:%d" payload[0])
    // }
}
