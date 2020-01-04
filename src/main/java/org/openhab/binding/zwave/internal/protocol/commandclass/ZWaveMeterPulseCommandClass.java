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
 * Handles the protection command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_METER_PULSE")
public class ZWaveMeterPulseCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveMeterPulseCommandClass.class);

    private static final int METER_PULSE_GET = 4;
    private static final int METER_PULSE_REPORT = 5;

    /**
     * Creates a new instance of the ZWaveMeterPulseCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveMeterPulseCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_METER_PULSE;
    }

    @ZWaveResponseHandler(id = METER_PULSE_REPORT, name = "METER_PULSE_REPORT")
    public void handleMeterPulseReport(ZWaveCommandClassPayload payload, int endpoint) {
        int count = (payload.getPayloadByte(2) << 24) + (payload.getPayloadByte(3) << 16)
                + (payload.getPayloadByte(4) << 8) + payload.getPayloadByte(5);
        logger.debug("NODE {}: Received meter pulse count {}", getNode().getNodeId(), count);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), count);
        getController().notifyEventListeners(zEvent);
    }

    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command METER_PULSE_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), METER_PULSE_GET)
                .withPriority(TransactionPriority.Get).withExpectedResponseCommand(METER_PULSE_REPORT).build();
    }
}
