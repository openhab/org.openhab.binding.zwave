package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveMessagePayloadTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

public class ZWaveCommandClassTransactionPayload extends ZWaveCommandClassPayload
        implements ZWaveMessagePayloadTransaction {
    private final int nodeId;
    private final CommandClass expectedResponseCommandClass;
    private final int expectedResponseCommandClassCommand;
    private TransactionPriority priority;
    private int maxAttempts = 0;

    public ZWaveCommandClassTransactionPayload(int nodeId, byte[] payload, TransactionPriority priority,
            CommandClass expectedResponseCommandClass, int expectedResponseCommandClassCommand) {
        super(payload);
        this.nodeId = nodeId;
        this.priority = priority;
        this.expectedResponseCommandClass = expectedResponseCommandClass;
        this.expectedResponseCommandClassCommand = expectedResponseCommandClassCommand;
    }

    public int getNodeId() {
        return nodeId;
    }

    public TransactionPriority getPriority() {
        return priority;
    }

    public CommandClass getExpectedResponseCommandClass() {
        return expectedResponseCommandClass;
    }

    public int getExpectedResponseCommandClassCommand() {
        return expectedResponseCommandClassCommand;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setPriority(TransactionPriority priority) {
        this.priority = priority;
    }

    @Override
    public int getDestinationNode() {
        return nodeId;
    }

}
