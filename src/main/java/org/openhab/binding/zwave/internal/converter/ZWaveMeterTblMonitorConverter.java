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
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterTblMonitorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterTblMonitorCommandClass.MeterTblMonitorScale;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterTblMonitorCommandClass.ZWaveMeterTblMonitorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveMeterTblMonitorConverter class. Converter for communication with the {@link ZWaveMeterTblMonitorCommandClass}.
 * Implements polling of the sensor status and receiving of sensor events.
 *
 * @author Jorg de Jong
 */
public class ZWaveMeterTblMonitorConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveMeterTblMonitorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveMeterTblMonitorConverter} class.
     *
     * @param controller the {@link ZWaveController} to use for sending messages.
     */
    public ZWaveMeterTblMonitorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveMeterTblMonitorCommandClass commandClass = (ZWaveMeterTblMonitorCommandClass) node.resolveCommandClass(
                ZWaveCommandClass.CommandClass.COMMAND_CLASS_METER_TBL_MONITOR, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint());

        List<ZWaveCommandClassTransactionPayload> response = new ArrayList<ZWaveCommandClassTransactionPayload>();
        for (ZWaveCommandClassTransactionPayload msg : commandClass.getDynamicValues(true)) {
            response.add(node.encapsulate(msg, channel.getEndpoint()));
        }
        return response;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String meterScale = channel.getArguments().get("type");
        ZWaveMeterTblMonitorValueEvent meterEvent = (ZWaveMeterTblMonitorValueEvent) event;

        // Don't trigger event if this item is bound to another sensor type
        if (meterScale != null && MeterTblMonitorScale.getMeterScale(meterScale) != meterEvent.getMeterScale()) {
            logger.debug("Not the right scale {} <> {}", meterScale, meterEvent.getMeterScale());
            return null;
        }

        BigDecimal val = (BigDecimal) event.getValue();

        return new DecimalType(val);
    }
}
