/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBarrierOperatorCommandClass.BarrierOperatorStateType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveBarrierOperatorConverter class.
 *
 * @author Chris Jackson
 */
public class ZWaveBarrierOperatorConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveBarrierOperatorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveBarrierOperatorConverter} class.
     *
     */
    public ZWaveBarrierOperatorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ZWaveTransaction> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveBasicCommandClass commandClass = (ZWaveBasicCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.BARRIER_OPERATOR, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {} endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());
        ZWaveTransaction transaction = node.encapsulate(commandClass.getValueMessage(), commandClass,
                channel.getEndpoint());
        List<ZWaveTransaction> response = new ArrayList<ZWaveTransaction>(1);
        response.add(transaction);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        State state = null;
        BarrierOperatorStateType value = (BarrierOperatorStateType) event.getValue();
        switch (channel.getDataType()) {
            case DecimalType:
                state = new DecimalType(value.getKey());
                break;
            case PercentType:
            case OnOffType:
                state = value == BarrierOperatorStateType.STATE_CLOSED ? OnOffType.OFF : OnOffType.ON;
                break;
            case OpenClosedType:
                state = value == BarrierOperatorStateType.STATE_CLOSED ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                break;
            default:
                logger.warn("No conversion in {} to {}", getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ZWaveTransaction> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {
        ZWaveBasicCommandClass commandClass = (ZWaveBasicCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.BARRIER_OPERATOR, channel.getEndpoint());

        Integer value = null;
        if (command instanceof DecimalType) {
            value = (int) ((DecimalType) command).longValue();
        }
        ZWaveTransaction serialMessage = node.encapsulate(commandClass.setValueMessage(value), commandClass,
                channel.getEndpoint());

        if (serialMessage == null) {
            logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}",
                    commandClass.getCommandClass().getLabel(), node.getNodeId(), channel.getEndpoint());
            return null;
        }

        List<ZWaveTransaction> messages = new ArrayList<ZWaveTransaction>();
        messages.add(serialMessage);
        return messages;
    }
}
