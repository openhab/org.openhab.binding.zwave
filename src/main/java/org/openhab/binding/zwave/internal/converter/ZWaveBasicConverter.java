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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBasicCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
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

    private final Logger logger = LoggerFactory.getLogger(ZWaveBasicConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveBasicConverter} class.
     *
     */
    public ZWaveBasicConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveBasicCommandClass commandClass = (ZWaveBasicCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {} endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());
        ZWaveCommandClassTransactionPayload transaction = node.encapsulate(commandClass.getValueMessage(),
                channel.getEndpoint());
        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(transaction);
        return response;
    }

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

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        ZWaveBasicCommandClass commandClass = (ZWaveBasicCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC, channel.getEndpoint());

        Integer value = null;
        if (command instanceof OnOffType) {
            value = command == OnOffType.ON ? 0xff : 0x00;
        } else if (command instanceof DecimalType) {
            value = (int) ((DecimalType) command).longValue();
        }

        if (value == null) {
            logger.debug("NODE {}: Unknown conversion {}", node.getNodeId(), command.getClass().getSimpleName());
            return null;
        }

        ZWaveCommandClassTransactionPayload serialMessage = node.encapsulate(commandClass.setValueMessage(value),
                channel.getEndpoint());

        if (serialMessage == null) {
            logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}",
                    commandClass.getCommandClass(), node.getNodeId(), channel.getEndpoint());
            return null;
        }

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(serialMessage);
        return messages;
    }
}
