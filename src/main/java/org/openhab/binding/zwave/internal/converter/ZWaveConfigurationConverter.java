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
import org.openhab.binding.zwave.internal.protocol.ZWaveConfigurationParameter;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass.ZWaveConfigurationParameterEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveConfigurationConverter class. Converter for communication with the {@link ZWaveConfigurationCommandClass}.
 *
 * @author Chris Jackson
 */
public class ZWaveConfigurationConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveConfigurationConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveConfigurationConverter} class.
     *
     */
    public ZWaveConfigurationConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveConfigurationCommandClass commandClass = (ZWaveConfigurationCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_CONFIGURATION, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());
        String parmNumber = channel.getArguments().get("parameter");
        if (parmNumber == null) {
            logger.debug("NODE {}: 'parameter' option must be specified.", node.getNodeId());
            return null;
        }
        int parmValue = Integer.parseInt(parmNumber);
        if (parmValue < 0 || parmValue > 255) {
            logger.debug("NODE {}: 'parameter' option must be between 0 and 255.", node.getNodeId());
            return null;
        }

        ZWaveCommandClassTransactionPayload transaction = node.encapsulate(commandClass.getConfigMessage(parmValue),
                channel.getEndpoint());
        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(transaction);
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String parmNumber = channel.getArguments().get("parameter");
        ZWaveConfigurationParameterEvent cfgEvent = (ZWaveConfigurationParameterEvent) event;
        // Make sure this is for the parameter we want
        if (cfgEvent.getParameter() != null && cfgEvent.getParameter().getIndex() != Integer.parseInt(parmNumber)) {
            return null;
        }

        State state;
        switch (channel.getDataType()) {
            case DecimalType:
                state = new DecimalType(cfgEvent.getParameter().getValue());
                break;
            case PercentType:
                state = new DecimalType(cfgEvent.getParameter().getValue());
                break;
            default:
                state = null;
                logger.warn("No conversion from {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {

        String parmNumber = channel.getArguments().get("parameter");
        if (parmNumber == null) {
            logger.debug("NODE {}: 'parameter' option must be specified.", node.getNodeId());
            return null;
        }

        int paramIndex = Integer.parseInt(parmNumber);
        if (paramIndex < 0 || paramIndex > 255) {
            logger.debug("NODE {}: 'parameter' option must be between 0 and 255.", node.getNodeId());
            return null;
        }
        /*
         * ZWaveProductDatabase database = new ZWaveProductDatabase();
         * if (database.FindProduct(node.getManufacturer(), node.getDeviceType(), node.getDeviceId(),
         * node.getApplicationVersion()) == false) {
         * logger.debug("NODE {}: database can't find product.", node.getNodeId());
         * return;
         * }
         *
         * List<ZWaveDbConfigurationParameter> configList = database.getProductConfigParameters();
         * if (configList == null) {
         * logger.debug("NODE {}: Device has no configuration.", node.getNodeId());
         * return;
         * }
         *
         * ZWaveDbConfigurationParameter dbParameter = null;
         * for (ZWaveDbConfigurationParameter parameter : configList) {
         * if (parameter.Index == paramIndex) {
         * dbParameter = parameter;
         * break;
         * }
         * }
         * if (dbParameter == null) {
         * logger.debug("NODE {}: Device has no parameter {}.", node.getNodeId(), paramIndex);
         * return;
         * }
         */

        ZWaveConfigurationCommandClass commandClass = (ZWaveConfigurationCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_CONFIGURATION, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        ZWaveConfigurationParameter configParameter = commandClass.getParameter(paramIndex);
        if (configParameter == null) {
            logger.debug("NODE {}: Config parameter {} not found in converter", node.getNodeId(), paramIndex);
            return null;
        }

        if (command instanceof DecimalType) {
            configParameter.setValue((int) ((DecimalType) command).longValue());
        } else {
            logger.debug("NODE {}: Config parameter {} no conversion from {}", node.getNodeId(), paramIndex,
                    command.getClass().getSimpleName());
            return null;
        }

        // Set the parameter
        ZWaveCommandClassTransactionPayload transaction = node
                .encapsulate(commandClass.setConfigMessage(configParameter), channel.getEndpoint());
        if (transaction == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}", node.getNodeId(),
                    commandClass.getCommandClass());
            return null;
        }

        List<ZWaveCommandClassTransactionPayload> transactions = new ArrayList<ZWaveCommandClassTransactionPayload>();
        transactions.add(transaction);

        // And request a read-back
        transaction = node.encapsulate(commandClass.getConfigMessage(paramIndex), channel.getEndpoint());
        transactions.add(transaction);

        return transactions;
    }
}
