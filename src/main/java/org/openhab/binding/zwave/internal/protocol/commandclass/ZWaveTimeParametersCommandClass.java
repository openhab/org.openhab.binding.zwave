/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

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
 * Handles the time parameters command class.
 *
 * @author Jorg de Jong
 * @author Chris Jackson
 */
@XStreamAlias("timeParametersCommandClass")
public class ZWaveTimeParametersCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveTimeParametersCommandClass.class);

    private static final int TIME_SET = 1;
    private static final int TIME_GET = 2;
    private static final int TIME_REPORT = 3;

    /**
     * Creates a new instance of the ZWaveTimeParametersCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveTimeParametersCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.TIME_PARAMETERS;
    }

    /**
     * Gets a SerialMessage with the TIME_GET command
     *
     * @return the serial message.
     */
    @Override
    public ZWaveTransaction getValueMessage() {
        logger.debug("NODE {}: Creating new message for command TIME_GET", getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder().withCommandClass(getCommandClass(), TIME_GET)
                .withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), TIME_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the TIME_SET command
     *
     * @return the serial message.
     */
    public ZWaveTransaction getSetMessage(Calendar cal) {
        logger.debug("NODE {}: Creating new message for command TIME_SET", getNode().getNodeId());

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((cal.get(Calendar.YEAR) & 0xff00) >> 8);
        outputData.write((cal.get(Calendar.YEAR) & 0xff));
        outputData.write(cal.get(Calendar.MONTH) + 1);
        outputData.write(cal.get(Calendar.DAY_OF_MONTH));
        outputData.write(cal.get(Calendar.HOUR_OF_DAY));
        outputData.write(cal.get(Calendar.MINUTE));
        outputData.write(cal.get(Calendar.SECOND));

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder().withCommandClass(getCommandClass(), TIME_SET)
                .withNodeId(getNode().getNodeId()).withPayload(outputData.toByteArray()).build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.RealTime).build();
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received TIME_PARAMETERS command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case TIME_REPORT:
                int year = (serialMessage.getMessagePayloadByte(offset + 1) << 8
                        | serialMessage.getMessagePayloadByte(offset + 2));
                int month = serialMessage.getMessagePayloadByte(offset + 3);
                int day = serialMessage.getMessagePayloadByte(offset + 4);
                int hour = serialMessage.getMessagePayloadByte(offset + 5);
                int minute = serialMessage.getMessagePayloadByte(offset + 6);
                int second = serialMessage.getMessagePayloadByte(offset + 7);

                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(year, month - 1, day, hour, minute, second);
                logger.debug("NODE {}: Received time report: {}", getNode().getNodeId(), cal.getTime());

                Date nodeTime = cal.getTime();
                ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                        getCommandClass(), nodeTime);
                getController().notifyEventListeners(zEvent);
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        getNode().getNodeId(), command, getCommandClass().getLabel(), getCommandClass().getKey()));
                break;
        }
    }

    @Override
    public Collection<ZWaveTransaction> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveTransaction> result = new ArrayList<ZWaveTransaction>();
        if (refresh == true && getEndpoint() == null) {
            result.add(getValueMessage());
        }
        return result;
    }
}
