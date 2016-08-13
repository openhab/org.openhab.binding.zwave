package org.openhab.binding.zwave.internal.protocol;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;

/**
 * Builder class for serial message
 *
 * @author Chris Jackson - Initial Implementation
 *
 */
public class ZWaveMessageBuilder {
    protected SerialMessageClass messageClass;
    protected int nodeId = 255;
    protected byte[] payload;
    protected SerialMessageType messageType = SerialMessageType.Request;

    public ZWaveMessageBuilder(final SerialMessageClass messageClass) {
        this.messageClass = messageClass;
    }

    public ZWaveMessageBuilder withNodeId(final int nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public ZWaveMessageBuilder withMessageType(final SerialMessageType messageType) {
        this.messageType = messageType;
        return this;
    }

    public ZWaveMessageBuilder withPayload(int... payload) {
        this.payload = new byte[payload.length];
        int cnt = 0;
        for (int val : payload) {
            this.payload[cnt++] = (byte) (val & 0xff);
        }
        return this;
    }

    public ZWaveMessageBuilder withPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    public SerialMessage build() {
        SerialMessage serialMessage = new SerialMessage(nodeId, messageClass, messageType);
        serialMessage.setMessagePayload(payload);

        return serialMessage;
    }
}
