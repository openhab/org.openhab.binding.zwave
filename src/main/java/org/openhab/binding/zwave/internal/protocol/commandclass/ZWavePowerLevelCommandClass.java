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

import java.util.ArrayList;
import java.util.Collection;

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
 * Handles the power level command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_POWERLEVEL")
public class ZWavePowerLevelCommandClass extends ZWaveCommandClass implements ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWavePowerLevelCommandClass.class);

    private static final int POWERLEVEL_SET = 1;
    private static final int POWERLEVEL_GET = 2;
    private static final int POWERLEVEL_REPORT = 3;
    private static final int POWERLEVEL_TEST_SET = 4;
    private static final int POWERLEVEL_TEST_GET = 5;
    private static final int POWERLEVEL_TEST_REPORT = 6;

    private int powerLevel = 0;
    private int powerTimeout = 0;

    @XStreamOmitField
    private boolean initialiseDone = false;

    /**
     * Creates a new instance of the ZWavePowerLevelCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWavePowerLevelCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_POWERLEVEL;
    }

    @ZWaveResponseHandler(id = POWERLEVEL_REPORT, name = "POWERLEVEL_REPORT")
    public void handleZwavePlusReport(ZWaveCommandClassPayload payload, int endpoint) {
        powerLevel = payload.getPayloadByte(2);
        powerTimeout = payload.getPayloadByte(3);
        logger.debug("NODE {}: Received POWERLEVEL report -{}dB with {} second timeout", getNode().getNodeId(),
                powerLevel, powerTimeout);
        ZWavePowerLevelCommandClassChangeEvent event = new ZWavePowerLevelCommandClassChangeEvent(getNode().getNodeId(),
                powerLevel, powerTimeout);
        getController().notifyEventListeners(event);
        initialiseDone = true;
    }

    public ZWaveCommandClassTransactionPayload setValueMessage(int level, int timeout) {
        logger.debug("NODE {}: Creating new message for application command POWERLEVEL_SET, level={}, timeout={}",
                getNode().getNodeId(), level, timeout);

        if (level < 0 || level > 9) {
            logger.debug("NODE {}: Invalid power level {}.", getNode().getNodeId(), level);
            return null;
        }

        if (timeout < 0 || timeout > 255) {
            logger.debug("NODE {}: Invalid timeout {}.", getNode().getNodeId(), timeout);
            return null;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), POWERLEVEL_SET)
                .withPayload(level, timeout).withPriority(TransactionPriority.Config).build();
    }

    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command POWERLEVEL_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), POWERLEVEL_GET)
                .withPriority(TransactionPriority.Config).withExpectedResponseCommand(POWERLEVEL_REPORT).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        if (refresh == true || initialiseDone == false) {
            result.add(getValueMessage());
        }

        return result;
    }

    /**
     * return the current power level setting
     *
     * @return power level setting
     */
    public int getLevel() {
        return powerLevel;
    }

    /**
     * Return the current timeout in seconds (0 - 255)
     *
     * @return timeout
     */
    public int getTimeout() {
        return powerTimeout;
    }

    public class ZWavePowerLevelCommandClassChangeEvent extends ZWaveCommandClassValueEvent {
        private int timeout;

        public ZWavePowerLevelCommandClassChangeEvent(int nodeId, int level, int timeout) {
            super(nodeId, 0, CommandClass.COMMAND_CLASS_POWERLEVEL, level);
            this.timeout = timeout;
        }

        public int getLevel() {
            return (int) getValue();
        }

        public int getTimeout() {
            return timeout;
        }
    }
}
