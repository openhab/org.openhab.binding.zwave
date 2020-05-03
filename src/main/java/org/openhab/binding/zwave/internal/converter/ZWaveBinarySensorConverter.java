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
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass.ZWaveBinarySensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveBinarySensorConverter class. Converter for communication with the {@link ZWaveBinarySensorConverter}. Implements
 * polling of the binary sensor status and receiving of binary sensor events.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveBinarySensorConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveBinarySensorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveBinarySensorConverter} class.
     *
     */
    public ZWaveBinarySensorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveBinarySensorCommandClass commandClass = (ZWaveBinarySensorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SENSOR_BINARY, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());

        String sensorType = channel.getArguments().get("type");

        ZWaveCommandClassTransactionPayload transaction;
        if (sensorType != null && commandClass.getVersion() > 1) {
            transaction = node.encapsulate(commandClass.getValueMessage(SensorType.valueOf(sensorType)),
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
        // logger.debug("ZWaveBinarySensorValueEvent 1");

        String sensorType = channel.getArguments().get("type");
        // logger.debug("ZWaveBinarySensorValueEvent 2");
        ZWaveBinarySensorValueEvent sensorEvent = (ZWaveBinarySensorValueEvent) event;
        // logger.debug("ZWaveBinarySensorValueEvent 3");

        // Don't trigger event if this item is bound to another alarm type
        if (sensorType != null && SensorType.valueOf(sensorType) != sensorEvent.getSensorType()) {
            // logger.debug("ZWaveBinarySensorValueEvent 4");
            return null;
        }

        switch (channel.getDataType()) {
            case OnOffType:
                return sensorEvent.getValue() == 0 ? OnOffType.OFF : OnOffType.ON;
            case OpenClosedType:
                return sensorEvent.getValue() == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
            default:
                logger.debug("Unknown data type {} for BinarySensor", channel.getDataType());
                break;
        }

        return null;
    }
}
