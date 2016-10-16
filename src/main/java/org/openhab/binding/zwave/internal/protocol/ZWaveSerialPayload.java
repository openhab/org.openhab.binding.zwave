package org.openhab.binding.zwave.internal.protocol;

import java.util.Arrays;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;

/**
 * The {@link ZWaveSerialPayload} implements an encapsulated serial payload.
 *
 * @author Chris Jackson - Initial implementation
 *
 */
public class ZWaveSerialPayload implements ZWaveMessagePayloadTransaction {
    private final byte[] payload;
    private SerialMessageClass messageClass;
    private int maxAttempts = 0;

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

    @Override
    public int getDestinationNode() {
        return 0;
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    @Override
    public SerialMessage getSerialMessage() {
        SerialMessage msg = new SerialMessage(messageClass, SerialMessageType.Request);
        msg.setMessagePayload(payload);

        return msg;
    }
}
