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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBatteryCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
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
public class ZWaveBinarySwitchConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveBinarySwitchConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveBinarySwitchConverter} class.
     *
     */
    public ZWaveBinarySwitchConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveBinarySwitchCommandClass commandClass = (ZWaveBinarySwitchCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SWITCH_BINARY, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());
        ZWaveCommandClassTransactionPayload serialMessage = node.encapsulate(commandClass.getValueMessage(),
                channel.getEndpoint());
        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(serialMessage);
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        return (Integer) event.getValue() == 0 ? OnOffType.OFF : OnOffType.ON;
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        ZWaveBinarySwitchCommandClass commandClass = (ZWaveBinarySwitchCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SWITCH_BINARY, channel.getEndpoint());
        if (commandClass == null) {
            logger.debug("NODE {}: Command class class COMMAND_CLASS_SWITCH_BINARY for endpoint {} not found",
                    node.getNodeId(), channel.getEndpoint());
            return null;
        }

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        int value = 0;
        if (command instanceof OnOffType) {
            value = command == OnOffType.ON ? 0xff : 0x00;
        }
        ZWaveCommandClassTransactionPayload transaction = node.encapsulate(commandClass.setValueMessage(value),
                channel.getEndpoint());
        if (transaction == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass(), channel.getEndpoint());
            return null;
        }
        messages.add(transaction);

        return messages;
    }
}
