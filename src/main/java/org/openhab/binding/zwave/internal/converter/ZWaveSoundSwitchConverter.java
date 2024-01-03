/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSoundSwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveSoundSwitchConverter class. Converter for communication with the {@link ZWaveSoundSwitchCommandClass}.
 * Implements polling of the mode state and receiving of mode state events.
 *
 * @author Kennet Nielsen
 */
public class ZWaveSoundSwitchConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveSoundSwitchConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveSoundSwitchConverter} class.
     *
     */
    public ZWaveSoundSwitchConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveSoundSwitchCommandClass commandClass = (ZWaveSoundSwitchCommandClass) node
                .getCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SOUND_SWITCH);
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());
        ZWaveCommandClassTransactionPayload payload = null;
        if (channel.getChannelTypeUID().getId().equals("sound_volume")) {
            payload = commandClass.getConfigMessage();
        } else if (channel.getChannelTypeUID().getId().equals("sound_default_tone")) {
            payload = commandClass.getConfigMessage();
        } else {
            payload = commandClass.getValueMessage();
        }

        ZWaveCommandClassTransactionPayload transaction = node.encapsulate(payload, channel.getEndpoint());
        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(transaction);
        return response;
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        ZWaveSoundSwitchCommandClass commandClass = (ZWaveSoundSwitchCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SOUND_SWITCH, channel.getEndpoint());

        ZWaveCommandClassTransactionPayload payload = null;
        if (channel.getChannelTypeUID().getId().equals("sound_volume")) {
            payload = commandClass.setConfigMessage(((PercentType) command).intValue(), 0);
        } else if (channel.getChannelTypeUID().getId().equals("sound_default_tone")) {
            payload = commandClass.setConfigMessage(255, ((DecimalType) command).intValue());
        } else {
            payload = commandClass.setValueMessage(((DecimalType) command).intValue());
        }
        ZWaveCommandClassTransactionPayload serialMessage = node.encapsulate(payload, channel.getEndpoint());

        if (serialMessage == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass(), channel.getEndpoint());
            return null;
        }

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(serialMessage);
        return messages;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        switch ((ZWaveSoundSwitchCommandClass.Type) event.getType()) {
            case VOLUME:
                if (!channel.getChannelTypeUID().getId().equals("sound_volume")) {
                    return null;
                }
                return new PercentType((Integer) event.getValue());
            case TONE_PLAY:
                if (!channel.getChannelTypeUID().getId().equals("sound_tone_play")) {
                    return null;
                }
                return new DecimalType((Integer) event.getValue());
            case DEFAULT_TONE:
                if (!channel.getChannelTypeUID().getId().equals("sound_default_tone")) {
                    return null;
                }
                return new DecimalType((Integer) event.getValue());
            default:
                return null;
        }
    }
}
