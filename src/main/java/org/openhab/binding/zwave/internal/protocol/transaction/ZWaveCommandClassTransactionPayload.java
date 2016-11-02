package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveMessagePayloadTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZWaveCommandClassTransactionPayload extends ZWaveCommandClassPayload
        implements ZWaveMessagePayloadTransaction {
    private final int nodeId;
    private final CommandClass expectedResponseCommandClass;
    private final int expectedResponseCommandClassCommand;
    private TransactionPriority priority;
    private int maxAttempts = 0;

    private static final Logger logger = LoggerFactory.getLogger(ZWaveCommandClassTransactionPayload.class);

    public ZWaveCommandClassTransactionPayload(int nodeId, byte[] payload, TransactionPriority priority,
            CommandClass expectedResponseCommandClass, int expectedResponseCommandClassCommand) {
        super(payload);
        this.nodeId = nodeId;
        this.priority = priority;
        this.expectedResponseCommandClass = expectedResponseCommandClass;
        this.expectedResponseCommandClassCommand = expectedResponseCommandClassCommand;

        logger.debug("At ZWaveCommandClassTransactionPayload {}::", this.expectedResponseCommandClass,
                this.expectedResponseCommandClassCommand);
    }

    public int getNodeId() {
        return nodeId;
    }

    @Override
    public TransactionPriority getPriority() {
        return priority;
    }

    public CommandClass getExpectedResponseCommandClass() {
        logger.debug("At getExpectedResponseCommandClass {}", expectedResponseCommandClass);
        if (expectedResponseCommandClass != null) {
            return expectedResponseCommandClass;
        }
        return expectedResponseCommandClass;
    }

    public int getExpectedResponseCommandClassCommand() {
        return expectedResponseCommandClassCommand;
    }

    @Override
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

    @Override
    public SerialMessage getSerialMessage() {
        SerialMessage serialMessage = new SerialMessage(nodeId, SerialMessageClass.SendData, SerialMessageType.Request);

        byte[] output;
        if (payload == null) {
            output = new byte[2];
        } else {
            output = new byte[2 + payload.length];
        }

        output[0] = (byte) nodeId;
        output[1] = (byte) (output.length - 2);

        for (int i = 2; i < output.length; ++i) {
            output[i] = payload[i - 2];
        }

        serialMessage.setMessagePayload(output);
        return serialMessage;
    }

    @Override
    public int getTimeout() {
        return 2550;
    }

    @Override
    public SerialMessageClass getSerialMessageClass() {
        return SerialMessageClass.SendData;
    }

    @Override
    public SerialMessageClass getExpectedResponseSerialMessageClass() {
        // If we're not waiting for a response, then return null
        if (expectedResponseCommandClass == null) {
            return null;
        }
        return SerialMessageClass.ApplicationCommandHandler;
    }

    @Override
    public boolean requiresData() {
        return false;
    }

}
