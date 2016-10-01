package org.openhab.binding.zwave.internal.protocol;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * The {@link ZWaveCommandClassPayload} implements an encapsulated command class payload.
 *
 * @author Chris Jackson - Initial implementation
 *
 */
public class ZWaveCommandClassPayload {
    private final byte[] payload;

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
        for (int index = 3; index < incomingMessage.getMessagePayload().length; index++) {
            payloadData.write((byte) incomingMessage.getMessagePayloadByte(index));
        }

        this.payload = payloadData.toByteArray();
    }

    public int getCommandClassId() {
        return payload[0] & 0xFF;
    }

    public int getCommandClassCommand() {
        return payload[1] & 0xFF;
    }

    public int getPayloadByte(int offset) {
        return payload[offset] & 0xFF;
    }

    public int getPayloadLength() {
        return payload.length;
    }

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
