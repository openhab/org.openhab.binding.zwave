/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the clock command class.
 *
 * @author Chris Jackson
 * @author Jorg de Jong
 *
 */
@XStreamAlias("COMMAND_CLASS_CLOCK")
public class ZWaveClockCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveClockCommandClass.class);

    private static final int CLOCK_SET = 4;
    private static final int CLOCK_GET = 5;
    private static final int CLOCK_REPORT = 6;

    /**
     * Creates a new instance of the ZWaveClockCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveClockCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_CLOCK;
    }

    @ZWaveResponseHandler(id = CLOCK_REPORT, name = "CLOCK_REPORT")
    public void handleCentralSceneNotification(ZWaveCommandClassPayload payload, int endpoint) {
        int day = payload.getPayloadByte(2) >> 5;
        int hour = payload.getPayloadByte(2) & 0x1f;
        int minute = payload.getPayloadByte(3);
        String days[] = new DateFormatSymbols().getWeekdays();
        int javaDay = day == 7 ? 1 : day + 1;
        logger.debug(String.format("NODE %d: Received clock report: %s %02d:%02d", getNode().getNodeId(), days[javaDay],
                hour, minute));

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, javaDay);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        Date nodeTime = cal.getTime();
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), nodeTime);
        getController().notifyEventListeners(zEvent);

    }

    /**
     * Gets a SerialMessage with the CLOCK_GET command
     *
     * @return the serial message.
     */
    @Override
    public ZWaveTransaction getValueMessage() {
        logger.debug("NODE {}: Creating new message for command CLOCK_GET", getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder().withCommandClass(getCommandClass(), CLOCK_GET)
                .withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), CLOCK_REPORT).withPriority(TransactionPriority.Get)
                .build();
    }

    /**
     * Gets a SerialMessage with the CLOCK_SET command
     *
     * @return the serial message.
     */
    public ZWaveTransaction getSetMessage(Calendar cal) {
        logger.debug("NODE {}: Creating new message for command CLOCK_SET", getNode().getNodeId());

        int day = cal.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : cal.get(Calendar.DAY_OF_WEEK) - 1;
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder().withCommandClass(getCommandClass(), CLOCK_SET)
                .withNodeId(getNode().getNodeId()).withPayload((day << 5) | hour, minute).build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.RealTime).build();
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
