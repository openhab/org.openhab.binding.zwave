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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBatteryCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass.ZWaveStartStopEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveBinarySwitchConverter class. Converter for communication with the {@link ZWaveBatteryCommandClass}. Implements
 * polling of the battery status and receiving of battery events.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveMultiLevelSwitchConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveMultiLevelSwitchConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveMultiLevelSwitchConverter} class.
     *
     */
    public ZWaveMultiLevelSwitchConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveMultiLevelSwitchCommandClass commandClass = (ZWaveMultiLevelSwitchCommandClass) node.resolveCommandClass(
                ZWaveCommandClass.CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL, channel.getEndpoint());
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
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        boolean configInvertControl = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_control"));
        boolean configInvertPercent = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_percent"));

        if (event instanceof ZWaveStartStopEvent) {
            return handleStartStopEvent(channel, (ZWaveStartStopEvent) event);
        }

        int value = (int) event.getValue();

        // A value of 254 means the device doesn't know it's current position
        if (value == 254) {
            return UnDefType.UNDEF;
        }

        // If we read greater than 99%, then change it to 100%
        // This just appears better in OH otherwise you can't get 100%!
        if (value >= 99) {
            value = 100;
        }

        State state = null;
        switch (channel.getDataType()) {
            case PercentType:
                if (value < 0 || value > 100) {
                    break;
                }

                if (configInvertPercent) {
                    state = new PercentType(100 - value);
                } else {
                    state = new PercentType(value);
                }

                break;
            case OnOffType:
                if (value == 0) {
                    state = OnOffType.OFF;
                } else {
                    state = OnOffType.ON;
                }

                if (configInvertControl) {
                    if (state == OnOffType.ON) {
                        state = OnOffType.OFF;
                    } else {
                        state = OnOffType.ON;
                    }
                }
                break;
            case IncreaseDecreaseType:
                // state = IncreaseDecreaseType.INCREASE;
                break;
            default:
                logger.warn("No conversion in {} to {}", getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;
    }

    private State handleStartStopEvent(ZWaveThingChannel channel, ZWaveStartStopEvent event) {
        logger.debug("NODE {}: ZWaveMultiLevelSwitchConverter.handleEvent() <<{}>>", event.getNodeId(),
                channel.getChannelTypeUID().getId());
        if (channel.getChannelTypeUID().getId().equals("switch_startstop")) {
            Map<String, Object> object = new HashMap<String, Object>();
            object.put("direction", event.direction);

            logger.debug("NODE {}: ZWaveMultiLevelSwitchConverter.handleEvent() <<{}>>", event.getNodeId(),
                    propertiesToJson(object));

            return new StringType(propertiesToJson(object));
        }

        return null;
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        ZWaveMultiLevelSwitchCommandClass commandClass = (ZWaveMultiLevelSwitchCommandClass) node.resolveCommandClass(
                ZWaveCommandClass.CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL, channel.getEndpoint());
        if (commandClass == null) {
            logger.debug("NODE {}: Command class SWITCH_MULTILEVEL not found when processing command on endpoint {}",
                    node.getNodeId(), channel.getEndpoint());
            return null;
        }

        ZWaveCommandClassTransactionPayload transaction = null;
        boolean restoreLastValue = "true".equalsIgnoreCase(channel.getArguments().get("config_restorelastvalue"));
        boolean configInvertControl = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_control"));
        boolean configInvertPercent = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_percent"));

        if (command instanceof StopMoveType && command == StopMoveType.STOP) {
            // Special handling for the STOP command
            transaction = commandClass.stopLevelChangeMessage();
        } else if (command instanceof UpDownType) {
            if (configInvertControl == false) {
                if (command == UpDownType.UP) {
                    transaction = commandClass.startLevelChangeMessage(true, 0xff);
                } else {
                    transaction = commandClass.startLevelChangeMessage(false, 0xff);
                }
            } else {
                if (command == UpDownType.UP) {
                    transaction = commandClass.startLevelChangeMessage(false, 0xff);
                } else {
                    transaction = commandClass.startLevelChangeMessage(true, 0xff);
                }
            }
        } else if (command instanceof PercentType) {
            int value;
            if (configInvertPercent) {
                value = 100 - ((PercentType) command).intValue();
            } else {
                value = ((PercentType) command).intValue();
            }
            // zwave has a max value of 99 for percentages.
            if (value >= 100) {
                value = 99;
            }
            logger.trace("NODE {}: Converted command '{}' to value {} for channel = {}, endpoint = {}.",
                    node.getNodeId(), command.toString(), value, channel.getUID(), channel.getEndpoint());

            transaction = commandClass.setValueMessage(value);
        } else if (command instanceof OnOffType) {
            int value;
            int onValue = restoreLastValue ? 0xFF : 99;

            if (configInvertControl) {
                value = command == OnOffType.ON ? 0 : onValue;
            } else {
                value = command == OnOffType.ON ? onValue : 0;
            }

            logger.trace("NODE {}: Converted command '{}' to value {} for channel = {}, endpoint = {}.",
                    node.getNodeId(), command.toString(), value, channel.getUID(), channel.getEndpoint());

            transaction = commandClass.setValueMessage(value);
        }

        // encapsulate the message in case this is a multi-instance node
        transaction = node.encapsulate(transaction, channel.getEndpoint());
        if (transaction == null) {
            logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}",
                    commandClass.getCommandClass(), node.getNodeId(), channel.getEndpoint());
            return null;
        }

        // Queue the command
        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>(2);
        messages.add(transaction);

        // Poll an update once we've sent the command if this is a STOP
        // Don't poll immediately since some devices return the original value, and some the new value.
        // This conflicts with OH that will move the slider immediately.
        if (command instanceof StopMoveType && command == StopMoveType.STOP) {
            messages.add(node.encapsulate(commandClass.getValueMessage(), channel.getEndpoint()));
        }
        return messages;
    }
}
