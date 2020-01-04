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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveDoorLockCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris Jackson
 */
public class ZWaveDoorLockConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveDoorLockConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveDoorLockConverter} class.
     *
     */
    public ZWaveDoorLockConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveDoorLockCommandClass commandClass = (ZWaveDoorLockCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_DOOR_LOCK, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {} endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());
        ZWaveCommandClassTransactionPayload serialMessage = node.encapsulate(commandClass.getValueMessage(),
                channel.getEndpoint());
        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(serialMessage);
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        logger.debug("NODE {}: Handle door lock event {}", event.getNodeId(), event.getType());

        switch ((ZWaveDoorLockCommandClass.Type) event.getType()) {
            case DOOR_LOCK_STATE:
                return handleEventLockState(channel, event);
            case DOOR_CONDITION:
                return handleEventCondition(channel, event);
            default:
                return null;
        }
    }

    private State handleEventLockState(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        if (!channel.getUID().getId().equals("lock_door")) {
            return null;
        }

        switch (channel.getDataType()) {
            case OnOffType:
                return (Integer) event.getValue() == 0x00 ? OnOffType.OFF : OnOffType.ON;
            default:
                logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return null;
    }

    private State handleEventCondition(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        if (!channel.getUID().getId().equals("sensor_door")) {
            return null;
        }

        switch (channel.getDataType()) {
            case OpenClosedType:
                return ((Integer) event.getValue() & 0x01) == 0x00 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            default:
                logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return null;
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        ZWaveDoorLockCommandClass commandClass = (ZWaveDoorLockCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_DOOR_LOCK, channel.getEndpoint());
        if (commandClass == null) {
            logger.warn("NODE {}: Command class COMMAND_CLASS_DOOR_LOCK not found", node.getNodeId());
            return null;
        }

        Integer value = null;
        if (command instanceof OnOffType) {
            value = ((OnOffType) command) == OnOffType.ON ? 0xff : 0x00;
        }
        if (value == null) {
            return null;
        }
        if (value > 0) {
            value = 0xff;
        }
        ZWaveCommandClassTransactionPayload transaction = node.encapsulate(commandClass.setValueMessage(value),
                channel.getEndpoint());

        if (transaction == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass(), channel.getEndpoint());
            return null;
        }

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(transaction);
        return messages;
    }
}
