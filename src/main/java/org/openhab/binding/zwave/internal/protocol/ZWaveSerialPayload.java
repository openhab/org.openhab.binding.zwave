/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import java.util.Arrays;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;

/**
 * The {@link ZWaveSerialPayload} implements an encapsulated serial payload.
 *
 * @author Chris Jackson - Initial implementation
 *
 */
public class ZWaveSerialPayload implements ZWaveMessagePayloadTransaction {
    private int nodeId = 255;
    private final byte[] payload;
    private SerialMessageClass messageClass;
    private SerialMessageClass responseClass;
    private int maxAttempts = 0;
    private boolean requiresData = false;
    private int timeout;
    private TransactionPriority priority;

    public ZWaveSerialPayload(final byte[] payload) {
        this.payload = payload;
    }

    public ZWaveSerialPayload(final int nodeId, final SerialMessageClass messageClass, final byte[] payload,
            final SerialMessageClass responseClass, final boolean requiresData, final int timeout,
            TransactionPriority priority) {
        this.nodeId = nodeId;
        this.messageClass = messageClass;
        this.payload = payload;
        this.responseClass = responseClass;
        this.requiresData = requiresData;
        this.timeout = timeout;
        this.priority = priority;
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
        return nodeId;
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

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public SerialMessageClass getSerialMessageClass() {
        return messageClass;
    }

    @Override
    public SerialMessageClass getExpectedResponseSerialMessageClass() {
        return responseClass;
    }

    @Override
    public TransactionPriority getPriority() {
        // Serial commands to the controller are high priority
        return priority;
    }

    @Override
    public boolean requiresData() {
        return requiresData;
    }

    @Override
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
}
