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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
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
public class ZWaveClockCommandClass extends ZWaveCommandClass implements ZWaveCommandClassDynamicState {

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
        logger.debug("NODE {}: Received clock report: {} {}:{}", getNode().getNodeId(), days[javaDay], hour, minute);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, javaDay);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        Date nodeTime = cal.getTime();
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), nodeTime);
        getController().notifyEventListeners(zEvent);

    }

    @ZWaveResponseHandler(id = CLOCK_GET, name = "CLOCK_GET")
    public void handleClockGetRequest(ZWaveCommandClassPayload payload, int endpoint) {
        Calendar calendar = Calendar.getInstance();
        logger.debug("NODE {}: Answering with {}", getNode().getNodeId(), calendar);
        getController().enqueue(getReportMessage(calendar));
    }

    /**
     * Gets a SerialMessage with the CLOCK_GET command
     *
     * @return the serial message.
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for command CLOCK_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), CLOCK_GET)
                .withPriority(TransactionPriority.Get).withExpectedResponseCommand(CLOCK_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the CLOCK_SET command
     *
     * @return the serial message.
     */
    public ZWaveCommandClassTransactionPayload getSetMessage(Calendar cal) {
        logger.debug("NODE {}: Creating new message for command CLOCK_SET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), CLOCK_SET)
                .withPayload(getTimePayload(cal)).withPriority(TransactionPriority.RealTime)
                .withExpectedResponseCommand(CLOCK_REPORT).build();
    }

    public ZWaveCommandClassTransactionPayload getReportMessage(Calendar cal) {
        logger.debug("NODE {}: Creating new message for command CLOCK_REPORT", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), CLOCK_REPORT)
                .withPayload(getTimePayload(cal)).withPriority(TransactionPriority.RealTime).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (refresh == true && getEndpoint() == null) {
            result.add(getValueMessage());
        }
        return result;
    }

    private int[] getTimePayload(Calendar cal) {
        int day = cal.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : cal.get(Calendar.DAY_OF_WEEK) - 1;
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        return new int[] { (day << 5) | hour, minute };
    }
}
