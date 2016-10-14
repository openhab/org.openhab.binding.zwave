package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * Builder class for ZWave transaction
 *
 * @author Chris Jackson - Initial Implementation
 *
 */
public class ZWaveTransactionBuilder {
    private int nodeId = 255;
    private SerialMessage serialMessage;
    private TransactionPriority priority = TransactionPriority.Get;
    private SerialMessageClass expectedResponseClass;
    private CommandClass expectedResponseCommandClass;
    private int expectedResponseCommandClassCommand;
    private int attempts;

    public ZWaveTransactionBuilder(SerialMessage serialMessage) {
        this.serialMessage = serialMessage;
    }

    public ZWaveTransactionBuilder withNodeId(int nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public ZWaveTransactionBuilder withPriority(final TransactionPriority priority) {
        this.priority = priority;
        return this;
    }

    public ZWaveTransactionBuilder withAttempts(final int attempts) {
        this.attempts = attempts;
        return this;
    }

    public ZWaveTransactionBuilder withExpectedResponseClass(SerialMessageClass expectedResponseClass) {
        this.expectedResponseClass = expectedResponseClass;
        return this;
    }

    public ZWaveTransactionBuilder withExpectedResponseCommandClass(CommandClass expectedResponseCommandClass,
            int expectedResponseCommandClassCommand) {
        this.expectedResponseCommandClass = expectedResponseCommandClass;
        this.expectedResponseCommandClassCommand = expectedResponseCommandClassCommand;
        return this;
    }

    public ZWaveTransaction build() {
        if (serialMessage.getMessageClass() == SerialMessageClass.SendData) {
            nodeId = serialMessage.getMessageNode();
        }

        return new ZWaveTransaction(nodeId, serialMessage, expectedResponseClass, expectedResponseCommandClass,
                expectedResponseCommandClassCommand, priority, attempts);
    }
}
