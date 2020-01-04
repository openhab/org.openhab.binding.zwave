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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatFanModeCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveThermostatFanModeConverter class. Converter for communication with the
 * {@link ZWaveThermostatFanModeCommandClass}. Implements polling of the fan mode state and receiving of fan mode
 * events.
 *
 * @author Chris Jackson
 * @author Dan Cunningham
 */
public class ZWaveThermostatFanModeConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveThermostatFanModeConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveThermostatFanModeConverter} class.
     *
     */
    public ZWaveThermostatFanModeConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveThermostatFanModeCommandClass commandClass = (ZWaveThermostatFanModeCommandClass) node.resolveCommandClass(
                ZWaveCommandClass.CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_MODE, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());
        ZWaveCommandClassTransactionPayload transaction = node.encapsulate(commandClass.getValueMessage(),
                channel.getEndpoint());
        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(transaction);
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channelString, ZWaveCommandClassValueEvent event) {
        return new DecimalType((int) event.getValue());
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {

        ZWaveThermostatFanModeCommandClass commandClass = (ZWaveThermostatFanModeCommandClass) node.resolveCommandClass(
                ZWaveCommandClass.CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_MODE, channel.getEndpoint());

        int value = ((DecimalType) command).intValue();

        ZWaveCommandClassTransactionPayload serialMessage = node.encapsulate(commandClass.setValueMessage(value),
                channel.getEndpoint());
        logger.debug("NODE {}: receiveCommand sending message {} ", node.getNodeId(), serialMessage);
        if (serialMessage == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass(), channel.getEndpoint());
            return null;
        }

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(serialMessage);
        return messages;
    }
}
