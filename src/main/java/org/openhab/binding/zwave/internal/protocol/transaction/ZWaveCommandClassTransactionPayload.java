package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

public class ZWaveCommandClassTransactionPayload extends ZWaveCommandClassPayload {
    private final TransactionPriority priority;
    private final int nodeId;
    private final CommandClass expectedResponseCommandClass;
    private final int expectedResponseCommandClassCommand;

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

}
