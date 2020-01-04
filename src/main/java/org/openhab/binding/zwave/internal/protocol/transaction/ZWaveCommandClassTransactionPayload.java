/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveMessagePayloadTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZWaveCommandClassTransactionPayload extends ZWaveCommandClassPayload
        implements ZWaveMessagePayloadTransaction {
    private final int nodeId;
    private final CommandClass expectedResponseCommandClass;
    private final Integer expectedResponseCommandClassCommand;
    private TransactionPriority priority;
    private int maxAttempts = 0;

    private boolean requiresSecurity = false;
    private boolean requiresResponse = true;

    /**
     *
     * @param nodeId
     * @param payload
     * @param priority
     * @param expectedResponseCommandClass
     * @param expectedResponseCommandClassCommand
     */
    public ZWaveCommandClassTransactionPayload(int nodeId, byte[] payload, TransactionPriority priority,
            CommandClass expectedResponseCommandClass, Integer expectedResponseCommandClassCommand) {
        super(payload);
        this.nodeId = nodeId;
        this.priority = priority;
        this.expectedResponseCommandClass = expectedResponseCommandClass;
        this.expectedResponseCommandClassCommand = expectedResponseCommandClassCommand;

        // logger.debug("At ZWaveCommandClassTransactionPayload {}::{}", this.expectedResponseCommandClass,
        // this.expectedResponseCommandClassCommand);
    }

    public int getNodeId() {
        return nodeId;
    }

    @Override
    public TransactionPriority getPriority() {
        return priority;
    }

    public CommandClass getExpectedResponseCommandClass() {
        // logger.debug("At getExpectedResponseCommandClass {}", expectedResponseCommandClass);
        if (expectedResponseCommandClass != null) {
            return expectedResponseCommandClass;
        }
        return expectedResponseCommandClass;
    }

    public Integer getExpectedResponseCommandClassCommand() {
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
        return 5000;
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

    public void setRequiresSecurity() {
        requiresSecurity = true;
    }

    public boolean getRequiresSecurity() {
        return requiresSecurity;
    }

    public void setRequiresResponse(boolean requiresResponse) {
        this.requiresResponse = requiresResponse;
    }

    public boolean getRequiresResponse() {
        return requiresResponse;
    }
}
