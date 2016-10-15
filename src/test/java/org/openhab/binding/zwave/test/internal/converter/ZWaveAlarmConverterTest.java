/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.converter;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveAlarmConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ReportType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveAlarmConverterTest extends ZWaveCommandClassConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");

    private ZWaveThingChannel createChannel(DataType dataType, String type, String event) {
        Map<String, String> args = new HashMap<String, String>();
        if (type != null) {
            args.put("type", type);
        }
        if (event != null) {
            args.put("event", event);
        }
        return new ZWaveThingChannel(null, uid, dataType, CommandClass.ALARM.toString(), 0, args);
    }

    private ZWaveCommandClassValueEvent createEvent(AlarmType type, ReportType reportType, Integer event,
            Integer status) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveAlarmCommandClass cls = new ZWaveAlarmCommandClass(node, controller, endpoint);

        return cls.new ZWaveAlarmValueEvent(1, 0, reportType, type, event, status);
    }

    @Test
    public void Alarm_Smoke() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel(DataType.OnOffType, AlarmType.SMOKE.toString(), "0");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.SMOKE, ReportType.ALARM, 0,
                0xff);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), OnOffType.class);
        assertEquals(state, OnOffType.ON);
    }

    @Test
    public void Notification_Smoke_OnOff() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel(DataType.OnOffType, AlarmType.SMOKE.toString(), "0");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.SMOKE, ReportType.NOTIFICATION,
                0, 0xff);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), OnOffType.class);
        assertEquals(state, OnOffType.OFF);
    }

    @Test
    public void Notification_Smoke_Decimal() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel(DataType.DecimalType, AlarmType.SMOKE.toString(), null);

        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.SMOKE, ReportType.NOTIFICATION,
                3, 0xff);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(state, new DecimalType(3));
    }

    @Test
    public void Notification_PowerManagement_PowerApplied() {
        // Simulates the Nexia doorbell
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel(DataType.OnOffType, AlarmType.POWER_MANAGEMENT.toString(), null);

        // Power has been applied
        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.POWER_MANAGEMENT,
                ReportType.NOTIFICATION, 1, 0xff);
        State state = converter.handleEvent(channel, event);
        assertEquals(state.getClass(), OnOffType.class);
        assertEquals(state, OnOffType.ON);

        // Events cleared
        event = createEvent(ZWaveAlarmCommandClass.AlarmType.POWER_MANAGEMENT, ReportType.NOTIFICATION, 0, 0xff);
        state = converter.handleEvent(channel, event);
        assertEquals(state.getClass(), OnOffType.class);
        assertEquals(state, OnOffType.OFF);
    }

    @Test
    public void sendNotification() {
        List<SerialMessage> msgs;
        DecimalType command;
        Map<String, String> args = new HashMap<String, String>();
        args.put("event1", AlarmType.SMOKE.toString() + ":1");
        args.put("event2", AlarmType.ACCESS_CONTROL.toString() + ":22");
        args.put("event3", AlarmType.BURGLAR.toString() + ":1");
        args.put("event4", AlarmType.EMERGENCY.toString() + ":1");
        args.put("event5", AlarmType.EMERGENCY.toString() + ":2");
        args.put("event6", AlarmType.EMERGENCY.toString() + ":3");
        ZWaveThingChannel channel = new ZWaveThingChannel(null, uid, DataType.OnOffType, CommandClass.ALARM.toString(),
                0, args);

        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);

        Map<String, String> options = new HashMap<String, String>();
        ZWaveNode node = CreateMockedNode(3, options);

        command = new DecimalType(1);
        byte[] expectedResponse1 = { 1, 15, 0, 19, 0, 8, 113, 5, 0, 0, 0, -1, 1, 1, 0, 0, 96 };
        msgs = converter.receiveCommand(channel, node, command);
        assertEquals(1, msgs.size());
        msgs.get(0).setCallbackId(0);
        assertTrue(Arrays.equals(msgs.get(0).getMessageBuffer(), expectedResponse1));

        command = new DecimalType(2);
        byte[] expectedResponse2 = { 1, 15, 0, 19, 0, 8, 113, 5, 0, 0, 0, -1, 6, 22, 0, 0, 112 };
        msgs = converter.receiveCommand(channel, node, command);
        assertEquals(1, msgs.size());
        msgs.get(0).setCallbackId(0);
        byte[] x = msgs.get(0).getMessageBuffer();
        assertTrue(Arrays.equals(msgs.get(0).getMessageBuffer(), expectedResponse2));
    }
}
