package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;

/**
 * Builder class for transaction
 *
 * @author Chris Jackson - Initial Implementation
 *
 */
public class ZWaveTransactionMessageBuilder {
    private SerialMessageClass messageClass;
    private SerialMessageClass expectedResponseClass;
    private int nodeId = 255;
    private byte[] payload;
    private int timeout;
    private boolean requiresData = false;
    private TransactionPriority priority = TransactionPriority.Controller;

    public ZWaveTransactionMessageBuilder(final SerialMessageClass messageClass) {
        this.messageClass = messageClass;
    }

    public ZWaveTransactionMessageBuilder withResponseNodeId(final int nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public ZWaveTransactionMessageBuilder withPayload(int... payload) {
        this.payload = new byte[payload.length];
        int cnt = 0;
        for (int val : payload) {
            this.payload[cnt++] = (byte) (val & 0xff);
        }
        return this;
    }

    public ZWaveTransactionMessageBuilder withPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    public ZWaveTransactionMessageBuilder withExpectedResponseClass(SerialMessageClass expectedResponseClass) {
        this.expectedResponseClass = expectedResponseClass;
        return this;
    }

    public ZWaveTransactionMessageBuilder withTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public ZWaveTransactionMessageBuilder withRequiresData(boolean requiresData) {
        this.requiresData = requiresData;
        return this;
    }

    public ZWaveSerialPayload build() {
        return new ZWaveSerialPayload(nodeId, messageClass, payload, expectedResponseClass, requiresData, timeout,
                priority);
    }
}
