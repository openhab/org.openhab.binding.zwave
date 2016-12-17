package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class for transaction payload
 *
 * @author Chris Jackson - Initial Implementation
 *
 */
public class ZWaveCommandClassTransactionPayloadBuilder {
    private int nodeId = 255;
    private CommandClass commandClass;
    private int command;
    private byte[] payload;
    private TransactionPriority priority = TransactionPriority.Get;
    private CommandClass expectedResponseCommandClass;
    private int expectedResponseCommandClassCommand;
    private final boolean payloadBuilt;

    private static final Logger logger = LoggerFactory.getLogger(ZWaveCommandClassTransactionPayloadBuilder.class);

    public ZWaveCommandClassTransactionPayloadBuilder(final int nodeId, final CommandClass commandClass,
            final int command) {
        this.nodeId = nodeId;
        this.commandClass = commandClass;
        this.command = command;
        this.payloadBuilt = false;
    }

    public ZWaveCommandClassTransactionPayloadBuilder(final int nodeId, final byte[] payload) {
        this.nodeId = nodeId;
        if (payload[0] < 0) {
            this.commandClass = CommandClass.getCommandClass(payload[0] + 256);
        } else {
            this.commandClass = CommandClass.getCommandClass(payload[0]);
        }
        this.command = payload[1];
        this.payload = payload;
        this.payloadBuilt = true;
    }

    public ZWaveCommandClassTransactionPayloadBuilder withPayloadBuffer(final byte[] payload) {
        this.payload = payload;
        return this;
    }

    public ZWaveCommandClassTransactionPayloadBuilder withPayload(int... payload) {
        this.payload = new byte[payload.length];
        int cnt = 0;
        for (int val : payload) {
            this.payload[cnt++] = (byte) (val & 0xff);
        }
        return this;
    }

    public ZWaveCommandClassTransactionPayloadBuilder withPayload(final byte[] payload) {
        this.payload = payload;
        return this;
    }

    public ZWaveCommandClassTransactionPayloadBuilder withPriority(final TransactionPriority priority) {
        this.priority = priority;
        return this;
    }

    public ZWaveCommandClassTransactionPayloadBuilder withExpectedResponseCommand(
            int expectedResponseCommandClassCommand) {
        this.expectedResponseCommandClass = commandClass;
        this.expectedResponseCommandClassCommand = expectedResponseCommandClassCommand;
        return this;
    }

    public ZWaveCommandClassTransactionPayload build() {
        byte[] output;
        if (payloadBuilt == true) {
            output = payload;
        } else {
            if (payload == null) {
                output = new byte[2];
            } else {
                output = new byte[2 + payload.length];
            }

            output[0] = (byte) commandClass.getKey();
            output[1] = (byte) command;
            for (int i = 2; i < output.length; ++i) {
                output[i] = payload[i - 2];
            }
        }

        logger.debug("At build {}", expectedResponseCommandClass);

        return new ZWaveCommandClassTransactionPayload(nodeId, output, priority, expectedResponseCommandClass,
                expectedResponseCommandClassCommand);
    }
}
