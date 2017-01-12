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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
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
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveAlarmCommandClass commandClass = (ZWaveAlarmCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.ALARM, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        String alarmType = channel.getArguments().get("type");
        Integer alarmEvent = channel.getArguments().get("event") == null ? null
                : Integer.parseInt(channel.getArguments().get("event"));

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}, alarm {}, event {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint(), alarmType, alarmEvent);

        SerialMessage serialMessage;
        if (alarmType != null) {
            serialMessage = node.encapsulate(
                    commandClass.getMessage(AlarmType.valueOf(alarmType), alarmEvent == null ? 0 : alarmEvent),
                    commandClass, channel.getEndpoint());
        } else {
            serialMessage = node.encapsulate(commandClass.getValueMessage(), commandClass, channel.getEndpoint());
        }

        if (serialMessage == null) {
            return null;
        }

        List<SerialMessage> response = new ArrayList<SerialMessage>();
        response.add(serialMessage);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String alarmType = channel.getArguments().get("type");
        Integer alarmEvent = channel.getArguments().get("event") == null ? null
                : Integer.parseInt(channel.getArguments().get("event"));

        ZWaveAlarmValueEvent eventAlarm = (ZWaveAlarmValueEvent) event;
        logger.debug("Alarm converter processing {}", eventAlarm.getReportType());
        switch (eventAlarm.getReportType()) {
            case ALARM:
                return handleAlarmReport(channel, eventAlarm, alarmType);
            case NOTIFICATION:
                return handleNotificationReport(channel, eventAlarm, alarmType, alarmEvent);
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

    private State handleNotificationReport(ZWaveThingChannel channel, ZWaveAlarmValueEvent eventAlarm, String alarmType,
            Integer alarmEvent) {

        // Don't trigger event if this item is bound to another event type
        if (alarmType != null && AlarmType.valueOf(alarmType) != eventAlarm.getAlarmType()) {
            return null;
        }

        // Handle event 0 as 'clear the event'
        int event = eventAlarm.getAlarmEvent();// == 0 ? 0 : eventAlarm.getAlarmStatus();
        logger.debug("Alarm converter NOTIFICATION event is {}, type {}", event, channel.getDataType());

        // We ignore event 0xFE as that indicates "no further events",
        // and event 0xFF which indicates that unsolicited events are enabled
        if (event == 0xFE || event == 0xFF) {
            return null;
        }

        // Don't trigger event if there is no event match. Note that 0 is always acceptable
        if (alarmEvent != null && event != 0 && alarmEvent != event) {
            return null;
        }

        // TODO: Handle these event to state specific conversions in a table.
        State state = null;
        switch (channel.getDataType()) {
            case OnOffType:
                state = event == 0 ? OnOffType.OFF : OnOffType.ON;
                break;
            case OpenClosedType:
                state = event == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                switch (eventAlarm.getAlarmType()) {
                    case ACCESS_CONTROL:
                        switch (event) {
                            case 22: // Window/Door is open
                                state = OpenClosedType.OPEN;
                                break;
                            case 23: // Window/Door is closed
                                state = OpenClosedType.CLOSED;
                                logger.debug("Alarm converter NOTIFICATION 3");
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {
        ZWaveAlarmCommandClass commandClass = (ZWaveAlarmCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.ALARM, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        String eventString = channel.getArguments().get("event" + command.toString());
        if (eventString == null) {
            logger.debug("NODE {}: No event found with name 'event{}'", node.getNodeId(), command.toString());
            return null;
        }
        String splits[] = eventString.split(":");
        if (splits.length != 2) {
            logger.debug("NODE {}: Incorrectly formatted event found with name 'event{}' = {}", node.getNodeId(),
                    command.toString(), eventString);
            return null;
        }

        AlarmType notificationType = AlarmType.valueOf(splits[0]);
        int event = Integer.valueOf(splits[1]);

        SerialMessage serialMessage = node.encapsulate(
                commandClass.getNotificationReportMessage(notificationType, event), commandClass,
                channel.getEndpoint());

        if (serialMessage == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass().getLabel(), channel.getEndpoint());
            return null;
        }

        List<SerialMessage> messages = new ArrayList<SerialMessage>();
        messages.add(serialMessage);
        return messages;
    }
}
