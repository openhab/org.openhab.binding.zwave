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

import java.io.ByteArrayOutputStream;
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
 * Handles the time parameters command class.
 *
 * @author Jorg de Jong
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_TIME_PARAMETERS")
public class ZWaveTimeParametersCommandClass extends ZWaveCommandClass implements ZWaveCommandClassDynamicState {

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

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_TIME_PARAMETERS;
    }

    /**
     * Gets a SerialMessage with the TIME_GET command
     *
     * @return the serial message.
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for command TIME_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), TIME_GET)
                .withPriority(TransactionPriority.Get).withExpectedResponseCommand(TIME_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the TIME_SET command
     *
     * @return the serial message.
     */
    public ZWaveCommandClassTransactionPayload getSetMessage(Calendar cal) {
        logger.debug("NODE {}: Creating new message for command TIME_SET", getNode().getNodeId());

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((cal.get(Calendar.YEAR) & 0xff00) >> 8);
        outputData.write((cal.get(Calendar.YEAR) & 0xff));
        outputData.write(cal.get(Calendar.MONTH) + 1);
        outputData.write(cal.get(Calendar.DAY_OF_MONTH));
        outputData.write(cal.get(Calendar.HOUR_OF_DAY));
        outputData.write(cal.get(Calendar.MINUTE));
        outputData.write(cal.get(Calendar.SECOND));

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), TIME_SET)
                .withPayload(outputData.toByteArray()).withPriority(TransactionPriority.RealTime).build();
    }

    @ZWaveResponseHandler(id = TIME_REPORT, name = "TIME_REPORT")
    public void handleTimeReport(ZWaveCommandClassPayload payload, int endpoint) {
        int year = (payload.getPayloadByte(2) << 8 | payload.getPayloadByte(3));
        int month = payload.getPayloadByte(4);
        int day = payload.getPayloadByte(5);
        int hour = payload.getPayloadByte(6);
        int minute = payload.getPayloadByte(7);
        int second = payload.getPayloadByte(8);

        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month - 1, day, hour, minute, second);
        logger.debug("NODE {}: Received time report: {}", getNode().getNodeId(), cal.getTime());

        Date nodeTime = cal.getTime();
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), nodeTime);
        getController().notifyEventListeners(zEvent);
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (refresh == true && getEndpoint() == null) {
            result.add(getValueMessage());
        }
        return result;
    }
}
