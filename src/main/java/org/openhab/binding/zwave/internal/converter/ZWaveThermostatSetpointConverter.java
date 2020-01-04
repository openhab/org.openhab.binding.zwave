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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatSetpointCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatSetpointCommandClass.SetpointType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatSetpointCommandClass.ZWaveThermostatSetpointValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * ZWaveThermostatSetpointConverter class. Converter for communication with the
 * {@link ZWaveThermostatSetpointCommandClass}. Implements polling of the setpoint status and receiving of setpoint
 * events.
 *
 * @author Chris Jackson
 * @author Matthew Bowman
 * @author Dave Hock
 */
public class ZWaveThermostatSetpointConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveThermostatSetpointConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveThermostatSetpointConverter} class.
     *
     */
    public ZWaveThermostatSetpointConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveThermostatSetpointCommandClass commandClass = (ZWaveThermostatSetpointCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT,
                        channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());

        String setpointType = channel.getArguments().get("type");

        ZWaveCommandClassTransactionPayload transaction;
        if (setpointType != null) {
            transaction = node.encapsulate(commandClass.getMessage(SetpointType.getSetpointType(setpointType)),
                    channel.getEndpoint());
        } else {
            transaction = node.encapsulate(commandClass.getValueMessage(), channel.getEndpoint());
        }

        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(transaction);
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String setpointType = channel.getArguments().get("type");
        ZWaveThermostatSetpointValueEvent setpointEvent = (ZWaveThermostatSetpointValueEvent) event;

        // Don't trigger event if this item is bound to another setpoint type
        if (setpointType != null && SetpointType.valueOf(setpointType) != setpointEvent.getSetpointType()) {
            return null;
        }

        BigDecimal value = (BigDecimal) event.getValue();

        switch (setpointEvent.getScale()) {
            case 0:
                return new QuantityType<>(value, SIUnits.CELSIUS);
            case 1:
                return new QuantityType<>(value, ImperialUnits.FAHRENHEIT);
            default:
                logger.debug("NODE {}: Unknown temperature scale {}", event.getNodeId(), setpointEvent.getScale());
                break;
        }

        return new DecimalType(value);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        String scaleString = channel.getArguments().get("config_scale");
        String setpointType = channel.getArguments().get("type");

        int scale = 0;
        if (scaleString != null) {
            scale = Integer.parseInt(scaleString);
        }

        logger.debug("NODE {}: Thermostat command received for {}", node.getNodeId(), command.toString());

        BigDecimal value;
        if (command instanceof QuantityType) {
            QuantityType<?> quantity = (QuantityType<?>) command;
            if (quantity.getUnit() == SIUnits.CELSIUS) {
                scale = 0;
            } else if (quantity.getUnit() == ImperialUnits.FAHRENHEIT) {
                scale = 1;
            }
            value = quantity.toBigDecimal();
        } else if (command instanceof DecimalType) {
            value = ((DecimalType) command).toBigDecimal();
        } else {
            logger.debug("NODE {}: Thermostat command received with unsupported type {}", node.getNodeId(),
                    command.getClass().getSimpleName());

            return null;
        }

        ZWaveThermostatSetpointCommandClass commandClass = (ZWaveThermostatSetpointCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT,
                        channel.getEndpoint());

        ZWaveCommandClassTransactionPayload serialMessage;

        if (setpointType != null) {
            serialMessage = node.encapsulate(commandClass.setMessage(scale, SetpointType.valueOf(setpointType), value),
                    channel.getEndpoint());
        } else {
            serialMessage = node.encapsulate(commandClass.setMessage(scale, value), channel.getEndpoint());
        }

        if (serialMessage == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass(), channel.getEndpoint());
            return null;
        }

        logger.debug("NODE {}: Sending Message: {}", node.getNodeId(), serialMessage);
        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(serialMessage);

        // Request an update so that OH knows when the setpoint has changed.
        if (setpointType != null) {
            serialMessage = node.encapsulate(commandClass.getMessage(SetpointType.valueOf(setpointType)),
                    channel.getEndpoint());
        } else {
            serialMessage = node.encapsulate(commandClass.getValueMessage(), channel.getEndpoint());
        }

        if (serialMessage != null) {
            messages.add(serialMessage);
        }

        return messages;
    }
}
