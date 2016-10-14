package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;

public class ZWaveControllerTransaction extends ZWaveTransaction {
    private SerialMessage serialMessage;

    public ZWaveControllerTransaction(SerialMessage serialMessage, SerialMessageClass expectedReplyClass,
            TransactionPriority priority, int attempts) {
        super(expectedReplyClass, priority, attempts);

        this.serialMessage = serialMessage;
    }

    public void setSerialMessage(SerialMessage serialMessage) {
        this.serialMessage = serialMessage;
    }

    public SerialMessage getSerialMessage() {
        return serialMessage;
    }

    @Override
    public SerialMessageClass getSerialMessageClass() {
        return serialMessage.getMessageType();
    }

    public int getMessageNode() {
        return nodeId;
    }
}
