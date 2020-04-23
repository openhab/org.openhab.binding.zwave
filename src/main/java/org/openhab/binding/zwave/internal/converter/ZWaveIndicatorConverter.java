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
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveIndicatorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveIndicatorCommandClass.IndicatorProperty;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveIndicatorCommandClass.IndicatorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveIndicatorCommandClass.ZWaveIndicator;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris Jackson
 */
public class ZWaveIndicatorConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveIndicatorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveIndicatorConverter} class.
     *
     */
    public ZWaveIndicatorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String indicatorType = channel.getArguments().get("type");

        List<ZWaveIndicator> indicators = (List<ZWaveIndicator>) event.getValue();
        State state = null;

        ZWaveIndicator indicator = null;

        for (ZWaveIndicator indicatorCheck : indicators) {
            if (indicatorCheck.type.equals(IndicatorType.valueOf(indicatorType))) {
                indicator = indicatorCheck;
                break;
            }
        }

        if (indicator == null) {
            return null;
        }

        switch (channel.getChannelTypeUID().getId()) {
            case "indicator_level":
                if (indicator.property != IndicatorProperty.MULTILEVEL
                        && indicator.property != IndicatorProperty.BINARY) {
                    return null;
                }
                state = new PercentType(indicator.value);
                break;
            case "indicator_period":
                if (indicator.property != IndicatorProperty.ON_OFF_PERIOD) {
                    return null;
                }
                state = new DecimalType(indicator.value);
                break;
            case "indicator_flash":
                if (indicator.property != IndicatorProperty.ON_OFF_CYCLES) {
                    return null;
                }
                state = new DecimalType(indicator.value);
                break;
            default:
                logger.warn("Unknown INDICATOR channel type {}", channel.getChannelTypeUID().getId());
                return null;
        }

        return state;
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {

        String indicatorStringType = channel.getArguments().get("type");
        String indicatorStringProperty = channel.getArguments().get("property");

        ZWaveIndicatorCommandClass commandClass = (ZWaveIndicatorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_INDICATOR, channel.getEndpoint());
        if (commandClass == null) {
            logger.warn("NODE {}: Command class COMMAND_CLASS_INDICATOR not found", node.getNodeId());
            return null;
        }

        IndicatorProperty property = null;

        switch (channel.getChannelTypeUID().getId()) {
            case "indicator_level":
                property = IndicatorProperty.MULTILEVEL;
                if ("BINARY".equalsIgnoreCase(indicatorStringProperty)) {
                    property = IndicatorProperty.BINARY;
                }
                break;
            case "indicator_period":
                property = IndicatorProperty.ON_OFF_PERIOD;
                break;
            case "indicator_flash":
                property = IndicatorProperty.ON_OFF_CYCLES;
                break;
            default:
                logger.warn("NODE {}: Unknown INDICATOR channel type {}", node.getNodeId(),
                        channel.getChannelTypeUID().getId());
                return null;
        }

        Integer value = null;
        if (command instanceof OnOffType) {
            value = ((OnOffType) command) == OnOffType.ON ? 0xff : 0x00;
        } else if (command instanceof DecimalType) {
            value = ((DecimalType) command).intValue();
        } else {
            logger.warn("NODE {}: Unknown INDICATOR command type {}", node.getNodeId(),
                    command.getClass().getSimpleName());
            return null;
        }

        ZWaveCommandClassTransactionPayload transaction;

        if (commandClass.getVersion() == 1) {
            transaction = commandClass.setValueMessage(value);
        } else {
            ZWaveIndicator indicator = new ZWaveIndicator();
            indicator.type = IndicatorType.valueOf(indicatorStringType);
            indicator.property = property;
            indicator.value = value;
            List<ZWaveIndicator> indicators = new ArrayList<>();
            indicators.add(indicator);
            transaction = commandClass.setValueMessage(indicators);
        }

        if (transaction == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass(), channel.getEndpoint());
            return null;
        }

        transaction = node.encapsulate(transaction, channel.getEndpoint());

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(transaction);
        return messages;
    }
}
