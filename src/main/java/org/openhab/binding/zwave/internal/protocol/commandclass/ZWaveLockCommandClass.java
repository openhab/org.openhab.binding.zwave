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

/**
 * Handles the Lock command class.
 *
 * @author Chris Jackson
 * @author Dave Badia
 */
@XStreamAlias("COMMAND_CLASS_LOCK")
public class ZWaveLockCommandClass extends ZWaveCommandClass {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveLockCommandClass.class);

    private static final int LOCK_SET = 0x01;
    /**
     * Request the state of the lock, ie {@link #LOCK_REPORT}
     */
    private static final int LOCK_GET = 0x02;
    /**
     * Details about the state of the lock (locked, unlocked)
     */
    private static final int LOCK_REPORT = 0x03;

    /**
     * Creates a new instance of the ZWaveAlarmCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveLockCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_LOCK;
    }

    @ZWaveResponseHandler(id = LOCK_REPORT, name = "LOCK_REPORT")
    public void handleIndicatorReport(ZWaveCommandClassPayload payload, int endpoint) {
        int lockState = payload.getPayloadByte(2);
        logger.debug("NODE {}: Lock report - lockState={}", this.getNode().getNodeId(), lockState);

        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                getCommandClass(), lockState);
        this.getController().notifyEventListeners(zEvent);
    }

    /**
     * Gets a SerialMessage with the LOCK_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command LOCK_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), LOCK_GET)
                .withExpectedResponseCommand(LOCK_REPORT).withPriority(TransactionPriority.Get).build();
    }

    public ZWaveCommandClassTransactionPayload setValueMessage(int value) {
        logger.debug("NODE {}: Creating new message for application command LOCK_SET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), LOCK_SET)
                .withPayload(value).withPriority(TransactionPriority.Set).build();
    }
}
