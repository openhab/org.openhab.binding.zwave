/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the binary toggle switch command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("binaryToggleSwitchCommandClass")
public class ZWaveBinaryToggleSwitchCommandClass extends ZWaveCommandClass
        implements ZWaveBasicCommands, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveBinaryToggleSwitchCommandClass.class);

    private static final int SWITCH_TOGGLE_SET = 1;
    private static final int SWITCH_TOGGLE_GET = 2;
    private static final int SWITCH_TOGGLE_REPORT = 3;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveUserCodeCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveBinaryToggleSwitchCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.SWITCH_TOGGLE_BINARY;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received binary toggle switch command (v{})", this.getNode().getNodeId(),
                this.getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case SWITCH_TOGGLE_REPORT:
                processSwitchBinaryToggleReport(serialMessage, offset, endpoint);

                dynamicDone = true;
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        this.getNode().getNodeId(), command, this.getCommandClass().getLabel(),
                        this.getCommandClass().getKey()));
        }
    }

    /**
     * Processes a report message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    protected void processSwitchBinaryToggleReport(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        int value = serialMessage.getMessagePayloadByte(offset + 1);
        logger.debug(String.format("NODE %d: Switch binary toggle report, value = 0x%02X", this.getNode().getNodeId(),
                value));
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                this.getCommandClass(), value);
        this.getController().notifyEventListeners(zEvent);
    }

    @Override
    public ZWaveTransaction getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command SWITCH_TOGGLE_GET",
                this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), SWITCH_TOGGLE_GET).withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), SWITCH_TOGGLE_REPORT)
                .withPriority(TransactionPriority.Get).build();
    }

    @Override
    public ZWaveTransaction setValueMessage(int value) {
        logger.debug("NODE {}: Creating new message for application command SWITCH_TOGGLE_SET",
                this.getNode().getNodeId());
        // TODO: Value isn't used!!!
        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), SWITCH_TOGGLE_SET).withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.Set).build();
    }

    @Override
    public Collection<ZWaveTransaction> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveTransaction> result = new ArrayList<ZWaveTransaction>();

        if (refresh == true || dynamicDone == false) {
            result.add(getValueMessage());
        }
        return result;
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }
}
