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
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * ZWaveMultiLevelSensorConverter class. Converter for communication with the {@link ZWaveMultiLevelSensorCommandClass}.
 * Implements polling of the sensor status and receiving of sensor events.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveMultiLevelSensorConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveMultiLevelSensorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveMultiLevelSensorConverter} class.
     *
     */
    public ZWaveMultiLevelSensorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveMultiLevelSensorCommandClass commandClass = (ZWaveMultiLevelSensorCommandClass) node.resolveCommandClass(
                ZWaveCommandClass.CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());

        String sensorType = channel.getArguments().get("type");

        ZWaveCommandClassTransactionPayload transaction;
        if (sensorType != null) {
            transaction = node.encapsulate(commandClass.getMessage(SensorType.valueOf(sensorType)),
                    channel.getEndpoint());
        } else {
            transaction = node.encapsulate(commandClass.getValueMessage(), channel.getEndpoint());
        }

        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>(1);
        response.add(transaction);
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String sensorType = channel.getArguments().get("type");
        ZWaveMultiLevelSensorValueEvent sensorEvent = (ZWaveMultiLevelSensorValueEvent) event;

        // Don't trigger event if this item is bound to another sensor type
        if (sensorType == null) {
            logger.debug("NODE {}: No sensorType set for channel {}", event.getNodeId(), channel.getUID());
            return null;
        }

        // Report channels aren't updated
        if (channel.getChannelTypeUID().getId().equals("sensor_report")) {
            return null;
        }

        if (SensorType.valueOf(sensorType) != sensorEvent.getSensorType()) {
            return null;
        }

        BigDecimal val = (BigDecimal) event.getValue();

        // Perform a scale conversion if needed

        SensorType senType = SensorType.valueOf(sensorType);
        switch (senType) {
            case TEMPERATURE:
                switch (sensorEvent.getSensorScale()) {
                    case 0:
                        return new QuantityType<>(val, SIUnits.CELSIUS);
                    case 1:
                        return new QuantityType<>(val, ImperialUnits.FAHRENHEIT);
                    default:
                        logger.debug("NODE {}: Unknown temperature scale {}", event.getNodeId(),
                                sensorEvent.getSensorScale());
                        break;
                }
                break;
            case LUMINANCE:
                switch (sensorEvent.getSensorScale()) {
                    case 0:
                        return new QuantityType<>(val, SmartHomeUnits.LUX);
                    case 1:
                        return new QuantityType<>(val, SmartHomeUnits.PERCENT);
                    default:
                        logger.debug("NODE {}: Unknown Luminance scale {}", event.getNodeId(),
                                sensorEvent.getSensorScale());
                }
                break;
            default:
                logger.debug("NODE {}: Sensor conversion not performed for {}.", event.getNodeId(), senType);
                break;
        }

        return new DecimalType(val);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        if (!channel.getChannelTypeUID().getId().equals("sensor_report")) {
            return null;
        }

        ZWaveMultiLevelSensorCommandClass commandClass = (ZWaveMultiLevelSensorCommandClass) node.resolveCommandClass(
                ZWaveCommandClass.CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        if (!(command instanceof DecimalType)) {
            return null;
        }

        String sensorType = channel.getArguments().get("type");
        if (sensorType == null) {
            logger.debug("NODE {}: No sensorType set for channel {}", node.getNodeId(), channel.getUID());
            return null;
        }

        int sensorScale = 0;
        String sensorScaleConfig = channel.getArguments().get("config_scale");
        if (sensorScaleConfig != null) {
            sensorScale = Integer.parseInt(sensorScaleConfig);
        }

        BigDecimal value = ((DecimalType) command).toBigDecimal();

        ZWaveCommandClassTransactionPayload payload = node.encapsulate(
                commandClass.getReportMessage(SensorType.valueOf(sensorType), sensorScale, value),
                channel.getEndpoint());

        if (payload == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass(), channel.getEndpoint());
            return null;
        }

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();
        messages.add(payload);
        return messages;
    }

}