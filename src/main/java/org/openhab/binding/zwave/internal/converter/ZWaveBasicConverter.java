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
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBasicCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveBasicConverter class. Converter for communication with the {@link ZWaveBasicCommandClass}. Supports control of
 * most devices through the BASIC command class. Note that some devices report their status as BASIC Report messages as
 * well. We try to handle these devices as best as possible.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveBasicConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveBasicConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveBasicConverter} class.
     *
     */
    public ZWaveBasicConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveBasicCommandClass commandClass = (ZWaveBasicCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.BASIC, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {} endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());
        SerialMessage serialMessage = node.encapsulate(commandClass.getValueMessage(), commandClass,
                channel.getEndpoint());
        List<SerialMessage> response = new ArrayList<SerialMessage>(1);
        response.add(serialMessage);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        State state = null;
        int value = (int) event.getValue();
        switch (channel.getDataType()) {
            case DecimalType:
                state = new DecimalType(value);
                break;
            case PercentType:
                // Special handling for 255
                if (value == 255) {
                    value = 100;
                }
                state = convertPercent(value, "true".equalsIgnoreCase(channel.getArguments().get("invertPercent")));
                break;
            case OnOffType:
                state = (Integer) event.getValue() == 0 ? OnOffType.OFF : OnOffType.ON;
                break;
            case OpenClosedType:
                state = (Integer) event.getValue() == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                break;
            default:
                logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {
        ZWaveBasicCommandClass commandClass = (ZWaveBasicCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.BASIC, channel.getEndpoint());

        Integer value = null;
        if (command instanceof DecimalType) {
            value = (int) ((DecimalType) command).longValue();
        }
        SerialMessage serialMessage = node.encapsulate(commandClass.setValueMessage(value), commandClass,
                channel.getEndpoint());

        if (serialMessage == null) {
            logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}",
                    commandClass.getCommandClass().getLabel(), node.getNodeId(), channel.getEndpoint());
            return null;
        }

        List<SerialMessage> messages = new ArrayList<SerialMessage>();
        messages.add(serialMessage);
        return messages;
    }
}
