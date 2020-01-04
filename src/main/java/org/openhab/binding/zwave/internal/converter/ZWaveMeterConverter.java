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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.MeterScale;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.ZWaveMeterValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveMultiLevelSensorConverter class. Converter for communication with the {@link ZWaveMultiLevelSensorCommandClass}.
 * Implements polling of the sensor status and receiving of sensor events.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveMeterConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveMeterConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveMeterConverter} class.
     *
     * @param controller the {@link ZWaveController} to use for sending messages.
     */
    public ZWaveMeterConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveMeterCommandClass commandClass = (ZWaveMeterCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_METER, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());
        ZWaveCommandClassTransactionPayload serialMessage;

        // Don't refresh channels that are the reset button
        if (channel.getDataType() == DataType.OnOffType) {
            return null;
        }

        String meterScale = channel.getArguments().get("type");
        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());

        if (meterScale != null) {
            serialMessage = node.encapsulate(commandClass.getMessage(MeterScale.getMeterScale(meterScale)),
                    channel.getEndpoint());
        } else {
            serialMessage = node.encapsulate(commandClass.getValueMessage(), channel.getEndpoint());
        }

        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(serialMessage);
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        // We ignore any meter reports for item bindings configured with 'reset=true'
        // since we don't want to be updating the 'reset' switch
        if (channel.getDataType() == DataType.OnOffType) {
            return null;
        }

        String meterScale = channel.getArguments().get("type");
        String meterZero = channel.getArguments().get("zero"); // needs to be a config setting - not arg
        ZWaveMeterValueEvent meterEvent = (ZWaveMeterValueEvent) event;

        // Don't trigger event if this item is bound to another sensor type
        if (meterScale != null && MeterScale.getMeterScale(meterScale) != meterEvent.getMeterScale()) {
            return null;
        }

        BigDecimal val = (BigDecimal) event.getValue();

        // If we've set a zero, then anything below this value needs to be considered ZERO
        if (meterZero != null) {
            if (val.doubleValue() <= Double.parseDouble(meterZero)) {
                val = BigDecimal.ZERO;
            }
        }

        return new DecimalType(val);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        // Is this channel a reset button - if not, just return
        if (channel.getDataType() != DataType.OnOffType) {
            return null;
        }

        // It's not an ON command from a button switch, do not reset
        if (command != OnOffType.ON) {
            return null;
        }

        ZWaveMeterCommandClass commandClass = (ZWaveMeterCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_METER, channel.getEndpoint());

        // Get the reset message - will return null if not supported
        ZWaveCommandClassTransactionPayload transaction = node.encapsulate(commandClass.getResetMessage(),
                channel.getEndpoint());

        if (transaction == null) {
            return null;
        }

        // Queue reset message
        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(transaction);

        // And poll the device
        for (ZWaveCommandClassTransactionPayload serialGetMessage : commandClass.getDynamicValues(true)) {
            messages.add(node.encapsulate(serialGetMessage, channel.getEndpoint()));
        }
        return messages;
    }
}
