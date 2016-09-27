package org.openhab.binding.zwave.internal.protocol;

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

    public int getCommandClassId() {
        return payload[0];
    }

    public int getCommandClassCommand() {
        return payload[1];
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

    // @Override
    // String toString() {
    // return String.format("Class:%02X, Cmd:%d" payload[0])
    // }
}
