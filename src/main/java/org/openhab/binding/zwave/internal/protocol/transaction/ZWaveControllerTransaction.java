package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;

public class ZWaveControllerTransaction extends ZWaveTransaction {
    private SerialMessage serialMessage;

    public ZWaveControllerTransaction(SerialMessage serialMessage, SerialMessageClass expectedReplyClass,
            TransactionPriority priority, int attempts) {
        super(expectedReplyClass, priority, attempts);

        this.serialMessage = serialMessage;
    }

    @Override
    public void setSerialMessage(SerialMessage serialMessage) {
        this.serialMessage = serialMessage;
    }

    @Override
    public SerialMessage getSerialMessage() {
        return serialMessage;
    }

    @Override
    public SerialMessageClass getSerialMessageClass() {
        return serialMessage.getMessageType();
    }

    @Override
    public int getMessageNode() {
        return nodeId;
    }
}
