/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveTimeParametersCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveTimeParametersConverter class. Converter for communication with the {@link ZWaveTimeParametersCommandClass}.
 *
 * @author Chris Jackson
 */
public class ZWaveTimeParametersConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveTimeParametersConverter.class);

    private Date lastClockUpdate = new Date();

    /**
     * Constructor. Creates a new instance of the {@link ZWaveTimeParametersConverter} class.
     *
     */
    public ZWaveTimeParametersConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveTimeParametersCommandClass commandClass = (ZWaveTimeParametersCommandClass) node.resolveCommandClass(
                ZWaveCommandClass.CommandClass.COMMAND_CLASS_TIME_PARAMETERS, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {} endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());
        ZWaveCommandClassTransactionPayload transaction = node.encapsulate(commandClass.getValueMessage(), commandClass,
                channel.getEndpoint());
        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(transaction);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        int offsetAllowed = Integer.MAX_VALUE;
        String offsetString = channel.getArguments().get("config_offset");
        if (offsetString != null && Double.valueOf(offsetString) != 0) {
            offsetAllowed = Double.valueOf(offsetString).intValue();
        }

        State state = null;
        switch (channel.getDataType()) {
            case DecimalType:
                // Calculate the clock offset
                Date nodeTime = (Date) event.getValue();
                long clockOffset = Math.abs(nodeTime.getTime() - System.currentTimeMillis()) / 1000;

                // If the clock is outside the offset, then update
                if (clockOffset > offsetAllowed && lastClockUpdate.getTime() < (new Date().getTime() - 30000)) {
                    logger.debug("NODE {}: Clock was {} seconds off. Time will be updated.", event.getNodeId(),
                            clockOffset);

                    ZWaveNode node = controller.getNode(event.getNodeId());
                    ZWaveTimeParametersCommandClass commandClass = (ZWaveTimeParametersCommandClass) node
                            .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_TIME_PARAMETERS,
                                    channel.getEndpoint());

                    ZWaveCommandClassTransactionPayload serialMessage = node.encapsulate(
                            commandClass.getSetMessage(Calendar.getInstance()), commandClass, channel.getEndpoint());
                    if (serialMessage == null) {
                        logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}",
                                commandClass.getCommandClass(), node.getNodeId(), channel.getEndpoint());
                        return null;
                    } else {
                        controller.sendData(serialMessage);
                    }

                    // We keep track of the last time we set the time to avoid a pathalogical loop if the time set
                    // doesn't work
                    lastClockUpdate = new Date();

                    // And request a read-back
                    serialMessage = node.encapsulate(commandClass.getValueMessage(), commandClass,
                            channel.getEndpoint());
                    if (serialMessage == null) {
                        logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}",
                                commandClass.getCommandClass(), node.getNodeId(), channel.getEndpoint());
                        return null;
                    } else {
                        controller.sendData(serialMessage);
                    }
                }

                state = new DecimalType(clockOffset);
                break;
            default:
                logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        ZWaveTimeParametersCommandClass commandClass = (ZWaveTimeParametersCommandClass) node.resolveCommandClass(
                ZWaveCommandClass.CommandClass.COMMAND_CLASS_TIME_PARAMETERS, channel.getEndpoint());

        ZWaveCommandClassTransactionPayload serialMessage = node
                .encapsulate(commandClass.getSetMessage(Calendar.getInstance()), commandClass, channel.getEndpoint());
        if (serialMessage == null) {
            logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}",
                    commandClass.getCommandClass(), node.getNodeId(), channel.getEndpoint());
            return null;
        }

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(serialMessage);
        return messages;
    }
}
