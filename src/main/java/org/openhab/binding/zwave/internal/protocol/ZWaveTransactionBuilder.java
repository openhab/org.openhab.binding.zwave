package org.openhab.binding.zwave.internal.protocol;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
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
    private boolean requiresData = false;
    private int dataTimeout = 3000;

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

    public ZWaveTransactionBuilder withRequiresData(final boolean requiresData) {
        this.requiresData = requiresData;
        return this;
    }

    public ZWaveTransactionBuilder withDataTimeout(final int dataTimeout) {
        this.dataTimeout = dataTimeout;
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
                expectedResponseCommandClassCommand, priority, attempts, requiresData, dataTimeout);
    }
}
