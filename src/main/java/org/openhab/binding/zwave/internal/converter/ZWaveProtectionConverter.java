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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.LocalProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.RfProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.Type;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveProtectionConverter class. Converters between binding items and the Z-Wave API for protection.
 *
 * @author Jorg de Jong
 */
public class ZWaveProtectionConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveProtectionConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveConverterBase} class.
     *
     */
    public ZWaveProtectionConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String type = channel.getArguments().get("type");

        // Don't trigger event if this item is bound to another type
        if (type != null && !event.getType().equals(Type.valueOf(type))) {
            return null;
        }

        Enum<?> e = (Enum<?>) event.getValue();
        return new DecimalType(e.ordinal());
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        String type = channel.getArguments().get("type");

        ZWaveProtectionCommandClass commandClass = (ZWaveProtectionCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_PROTECTION, channel.getEndpoint());

        if (commandClass == null) {
            logger.debug("NODE {}: Protection command class not found", node.getNodeId());
            return null;
        }
        logger.debug("NODE {}: Protection command {} received for {}", node.getNodeId(), type, command.toString());

        ZWaveCommandClassTransactionPayload serialMessage = null;

        if (type != null) {
            if (Type.PROTECTION_LOCAL.name().equalsIgnoreCase(type)) {
                int value = ((DecimalType) command).intValue();
                if (value >= 0 && value < LocalProtectionType.values().length) {
                    serialMessage = node.encapsulate(
                            commandClass.setValueMessage(LocalProtectionType.values()[value], null),
                            channel.getEndpoint());
                }

            }
            if (Type.PROTECTION_RF.name().equalsIgnoreCase(type)) {
                int value = ((DecimalType) command).intValue();
                if (value >= 0 && value < RfProtectionType.values().length) {
                    serialMessage = node.encapsulate(
                            commandClass.setValueMessage(null, RfProtectionType.values()[value]),
                            channel.getEndpoint());
                }
            }
        }

        if (serialMessage == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass(), channel.getEndpoint());
            return null;
        }

        logger.debug("NODE {}: Sending Message: {}", node.getNodeId(), serialMessage);
        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(serialMessage);

        // Request an update so that OH knows when the protection settings has changed.
        serialMessage = node.encapsulate(commandClass.getValueMessage(), channel.getEndpoint());

        if (serialMessage != null) {
            messages.add(serialMessage);
        }

        return messages;
    }
}
