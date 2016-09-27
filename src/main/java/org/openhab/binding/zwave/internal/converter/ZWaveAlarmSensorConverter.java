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
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass.ZWaveAlarmSensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveAlarmSensorConverter class. Converter for communication with the {@link ZWaveAlarmSensorCommandClass}.
 * Implements polling of the alarm sensor status and receiving of alarm sensor events.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij - OH1 implementation
 */
public class ZWaveAlarmSensorConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveAlarmSensorConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveAlarmSensorConverter} class.
     *
     */
    public ZWaveAlarmSensorConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ZWaveTransaction> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveAlarmSensorCommandClass commandClass = (ZWaveAlarmSensorCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SENSOR_ALARM, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        String alarmType = channel.getArguments().get("alarmType");
        logger.debug("NODE {}: Generating poll message for {}, endpoint {}, alarm {}", node.getNodeId(),
                commandClass.getCommandClass(), channel.getEndpoint(), alarmType);

        ZWaveTransaction transaction;
        if (alarmType != null) {
            transaction = commandClass.getMessage(AlarmType.valueOf(alarmType));
        } else {
            transaction = commandClass.getValueMessage();
        }

        if (transaction == null) {
            return null;
        }

        node.encapsulate(transaction, commandClass, channel.getEndpoint());

        List<ZWaveTransaction> response = new ArrayList<ZWaveTransaction>();
        response.add(transaction);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String alarmType = channel.getArguments().get("type");
        ZWaveAlarmSensorValueEvent alarmEvent = (ZWaveAlarmSensorValueEvent) event;

        // Don't trigger event if this item is bound to another alarm type
        if (alarmType != null && AlarmType.valueOf(alarmType) != alarmEvent.getAlarmType()) {
            return null;
        }

        return alarmEvent.getValue() == 0 ? OnOffType.OFF : OnOffType.ON;
    }
}
