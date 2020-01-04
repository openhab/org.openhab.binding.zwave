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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveColorConverter class. Converter for communication with the {@link ZWaveColorCommandClass}. Implements
 * polling of the status and receiving of events.
 *
 * @author Chris Jackson
 */
public class ZWaveColorConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveColorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveColorConverter} class.
     *
     */
    public ZWaveColorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveColorCommandClass commandClass = (ZWaveColorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SWITCH_COLOR, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());

        // Add a poll to update the color
        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        Collection<ZWaveCommandClassTransactionPayload> transactions = commandClass.getColor();
        for (ZWaveCommandClassTransactionPayload msg : transactions) {
            messages.add(node.encapsulate(msg, channel.getEndpoint()));
        }
        return messages;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        ZWaveColorValueEvent colorEvent = null;
        if (!(event instanceof ZWaveColorValueEvent)) {
            return null;
        }

        colorEvent = (ZWaveColorValueEvent) event;

        Map<ZWaveColorType, Integer> colorMap = colorEvent.getColorMap();
        State state;
        switch (channel.getDataType()) {
            case HSBType:
                int red = colorMap.get(ZWaveColorType.RED) != null ? colorMap.get(ZWaveColorType.RED) : 0;
                int green = colorMap.get(ZWaveColorType.GREEN) != null ? colorMap.get(ZWaveColorType.GREEN) : 0;
                int blue = colorMap.get(ZWaveColorType.BLUE) != null ? colorMap.get(ZWaveColorType.BLUE) : 0;
                state = HSBType.fromRGB(red, green, blue);
                break;
            case PercentType:
                if ("COLD_WHITE".equals(channel.getArguments().get("colorMode"))) {
                    state = new PercentType(colorMap.get(ZWaveColorType.COLD_WHITE));
                } else if ("WARM_WHITE".equals(channel.getArguments().get("colorMode"))) {
                    state = new PercentType(colorMap.get(ZWaveColorType.WARM_WHITE));
                } else {
                    state = new PercentType(0);
                }
                break;
            default:
                state = null;
                logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        ZWaveColorCommandClass commandClass = (ZWaveColorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SWITCH_COLOR, channel.getEndpoint());

        Collection<ZWaveCommandClassTransactionPayload> rawMessages = null;

        Map<ZWaveColorType, Integer> colors = new TreeMap<>();

        // Since we get an HSB, there is brightness information. However, we only deal with the color class here
        // so we need to scale the color and let the brightness be handled by the multi_level command class
        if ("RGB".equals(channel.getArguments().get("colorMode"))) {
            // Command must be color - convert to zwave format
            HSBType color = (HSBType) command;

            // Queue the command
            colors.put(ZWaveColorType.RED, scaleColor(color.getRed()));
            colors.put(ZWaveColorType.GREEN, scaleColor(color.getGreen()));
            colors.put(ZWaveColorType.BLUE, scaleColor(color.getBlue()));
            if (commandClass.isColorSupported(ZWaveColorType.COLD_WHITE)) {
                colors.put(ZWaveColorType.COLD_WHITE, 0);
            }
            if (commandClass.isColorSupported(ZWaveColorType.WARM_WHITE)) {
                colors.put(ZWaveColorType.WARM_WHITE, 0);
            }
        } else if ("COLD_WHITE".equals(channel.getArguments().get("colorMode"))) {
            PercentType color = (PercentType) command;

            // Queue the command
            colors.put(ZWaveColorType.COLD_WHITE, scaleColor(color));
        } else if ("WARM_WHITE".equals(channel.getArguments().get("colorMode"))) {
            PercentType color = (PercentType) command;

            // Queue the command
            colors.put(ZWaveColorType.WARM_WHITE, scaleColor(color));
        } else if ("DIFF_WHITE".equals(channel.getArguments().get("colorMode"))) {
            PercentType color = (PercentType) command;

            // Queue the command
            int value = scaleColor(color);
            colors.put(ZWaveColorType.COLD_WHITE, 255 - value);
            colors.put(ZWaveColorType.WARM_WHITE, value);
        } else {
            logger.warn("NODE {}: Unknown color mode {}.", node.getNodeId(), channel.getArguments().get("colorMode"));
        }

        logger.debug("NODE {}: Converted command '{}' to {} for channel = {}, endpoint = {}.", node.getNodeId(),
                command.toString(), colors, channel.getUID(), channel.getEndpoint());

        rawMessages = commandClass.setColor(colors);

        if (rawMessages == null) {
            return null;
        }

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        for (ZWaveCommandClassTransactionPayload msg : rawMessages) {
            messages.add(node.encapsulate(msg, channel.getEndpoint()));
        }

        // Add a poll to update the color
        rawMessages = commandClass.getColor();
        for (ZWaveCommandClassTransactionPayload msg : rawMessages) {
            messages.add(node.encapsulate(msg, channel.getEndpoint()));
        }
        return messages;
    }

    private int scaleColor(PercentType value) {
        return (int) (value.floatValue() * 255.0 / 100.0);
    }
}
