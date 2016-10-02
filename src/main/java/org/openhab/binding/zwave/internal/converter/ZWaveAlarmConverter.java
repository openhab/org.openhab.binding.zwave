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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ZWaveAlarmValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveAlarmConverter class. Converter for communication with the {@link ZWaveAlarmCommandClass}. Implements polling of
 * the alarm status and receiving of alarm events.
 *
 * @author Chris Jackson
 */
public class ZWaveAlarmConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveAlarmConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveAlarmConverter} class.
     *
     * @param controller the {@link ZWaveController} to use for sending messages.
     */
    public ZWaveAlarmConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ZWaveTransaction> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveAlarmCommandClass commandClass = (ZWaveAlarmCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.ALARM, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        String alarmType = channel.getArguments().get("type");
        logger.debug("NODE {}: Generating poll message for {}, endpoint {}, alarm {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint(), alarmType);

        ZWaveTransaction transaction;
        if (alarmType != null) {
            transaction = commandClass.getMessage(AlarmType.valueOf(alarmType));
        } else {
            transaction = commandClass.getValueMessage();
        }

        if (transaction == null) {
            return null;
        }

        transaction = node.encapsulate(transaction, commandClass, channel.getEndpoint());

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

        ZWaveAlarmValueEvent eventAlarm = (ZWaveAlarmValueEvent) event;
        switch (eventAlarm.getReportType()) {
            case ALARM:
                return handleAlarmReport(channel, eventAlarm, alarmType);
            case NOTIFICATION:
                return handleNotificationReport(channel, eventAlarm, alarmType);
        }

        return null;
    }

    private State handleAlarmReport(ZWaveThingChannel channel, ZWaveAlarmValueEvent eventAlarm, String alarmType) {
        // Don't trigger event if this item is bound to another alarm type
        if (alarmType != null && AlarmType.valueOf(alarmType) != eventAlarm.getAlarmType()) {
            return null;
        }

        // Default to using the value.
        // If this is V3 then we'll use the event status instead
        int value = eventAlarm.getValue();

        State state = null;
        switch (channel.getDataType()) {
            case OnOffType:
                state = value == 0 ? OnOffType.OFF : OnOffType.ON;
                break;
            case OpenClosedType:
                state = value == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                break;
            default:
                logger.warn("No conversion in {} to {}", getClass().getSimpleName(), channel.getDataType());
                break;
        }
        return state;
    }

    private State handleNotificationReport(ZWaveThingChannel channel, ZWaveAlarmValueEvent eventAlarm,
            String alarmType) {

        // Don't trigger event if this item is bound to another event type
        if (alarmType != null && AlarmType.valueOf(alarmType) != eventAlarm.getAlarmType()) {
            return null;
        }

        // Handle event 0 as 'clear the event'
        int event = eventAlarm.getAlarmEvent() == 0 ? 0 : eventAlarm.getAlarmStatus();

        // TODO: Handle these event to state specific conversions in a table.
        State state = null;
        switch (channel.getDataType()) {
            case OnOffType:
                state = event == 0 ? OnOffType.OFF : OnOffType.ON;
                break;
            case OpenClosedType:
                state = eventAlarm.getValue() == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                if (eventAlarm.getAlarmType() == AlarmType.ACCESS_CONTROL) {
                    switch (event) {
                        case 22: // Window/Door is open
                            state = eventAlarm.getValue() == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                            break;
                        case 23: // Window/Door is closed
                            state = eventAlarm.getValue() == 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                            break;
                        default:
                            break;
                    }
                }
                break;
            case DecimalType:
                state = new DecimalType(eventAlarm.getAlarmEvent());
                break;
            default:
                logger.warn("No conversion in {} to {}", getClass().getSimpleName(), channel.getDataType());
                break;
        }
        return state;
    }
}
