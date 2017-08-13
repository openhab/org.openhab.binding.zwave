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
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveAlarmConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ReportType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveAlarmConverterTest extends ZWaveCommandClassConverterTest {
    private ZWaveThingChannel createChannel(String channelType, DataType dataType, String type, String event) {
        ChannelUID uid = new ChannelUID("zwave:node:bridge:" + channelType);

        Map<String, String> args = new HashMap<String, String>();
        if (type != null) {
            args.put("type", type);
        }
        if (event != null) {
            args.put("event", event);
        }
        return new ZWaveThingChannel(null, uid, dataType, CommandClass.COMMAND_CLASS_ALARM.toString(), 0, args);
    }

    private ZWaveCommandClassValueEvent createEvent(AlarmType type, ReportType reportType, Integer event,
            Integer status) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveAlarmCommandClass cls = new ZWaveAlarmCommandClass(node, controller, endpoint);

        return cls.new ZWaveAlarmValueEvent(1, 0, reportType, type, 0, event, status);
    }

    private ZWaveCommandClassValueEvent createEvent(Integer v1Code, Integer value) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveAlarmCommandClass cls = new ZWaveAlarmCommandClass(node, controller, endpoint);

        return cls.new ZWaveAlarmValueEvent(1, 0, ReportType.ALARM, null, v1Code, 0, value);
    }

    @Test
    public void Alarm_Smoke() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel("alarm_smoke", DataType.OnOffType, AlarmType.SMOKE.toString(), "0");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.SMOKE, ReportType.ALARM, 0,
                0xff);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), OnOffType.class);
        assertEquals(state, OnOffType.ON);
    }

    @Test
    public void Notification_Smoke_OnOff() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel("alarm_smoke", DataType.OnOffType, AlarmType.SMOKE.toString(), "0");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.SMOKE, ReportType.NOTIFICATION,
                0, 0xff);
        State state = converter.handleEvent(channel, event);
        assertEquals(state.getClass(), OnOffType.class);
        assertEquals(state, OnOffType.OFF);

        event = createEvent(ZWaveAlarmCommandClass.AlarmType.SMOKE, ReportType.NOTIFICATION, 1, 0xff);
        state = converter.handleEvent(channel, event);
        assertEquals(state.getClass(), OnOffType.class);
        assertEquals(state, OnOffType.ON);

        event = createEvent(ZWaveAlarmCommandClass.AlarmType.SMOKE, ReportType.NOTIFICATION, 2, 0xff);
        state = converter.handleEvent(channel, event);
        assertEquals(state.getClass(), OnOffType.class);
        assertEquals(state, OnOffType.ON);
    }

    @Test
    public void Notification_Smoke_Decimal() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        // Note the different data type than is returned (which is a decimal)
        ZWaveThingChannel channel = createChannel("alarm_number", DataType.OnOffType, AlarmType.SMOKE.toString(), null);

        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.SMOKE, ReportType.NOTIFICATION,
                3, 0xff);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(state, new DecimalType(3));
    }

    @Test
    public void Notification_Door() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel("sensor_door", DataType.OpenClosedType, null, null);

        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.ACCESS_CONTROL,
                ReportType.NOTIFICATION, 22, 0xff);
        State state = converter.handleEvent(channel, event);
        assertEquals(OpenClosedType.class, state.getClass());
        assertEquals(OpenClosedType.OPEN, state);

        event = createEvent(ZWaveAlarmCommandClass.AlarmType.ACCESS_CONTROL, ReportType.NOTIFICATION, 23, 0xff);
        state = converter.handleEvent(channel, event);
        assertEquals(OpenClosedType.class, state.getClass());
        assertEquals(OpenClosedType.CLOSED, state);
    }

    @Test
    public void Notification_Appliance() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel("alarm_smoke", DataType.OnOffType, null, null);

        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.APPLIANCE, ReportType.ALARM, 0,
                0xff);
        State state = converter.handleEvent(channel, event);
        assertEquals(OnOffType.class, state.getClass());
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void Notification_PowerManagement_PowerApplied() {
        // Simulates the Nexia doorbell
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        ZWaveThingChannel channel = createChannel("alarm_power", DataType.OnOffType,
                AlarmType.POWER_MANAGEMENT.toString(), null);

        // Power has been applied
        ZWaveCommandClassValueEvent event = createEvent(ZWaveAlarmCommandClass.AlarmType.POWER_MANAGEMENT,
                ReportType.NOTIFICATION, 1, 0xff);
        State state = converter.handleEvent(channel, event);
        assertEquals(OnOffType.class, state.getClass());
        assertEquals(OnOffType.OFF, state);

        // Power has been removed
        event = createEvent(ZWaveAlarmCommandClass.AlarmType.POWER_MANAGEMENT, ReportType.NOTIFICATION, 2, 0xff);
        state = converter.handleEvent(channel, event);
        assertEquals(OnOffType.class, state.getClass());
        assertEquals(OnOffType.ON, state);

        // Events cleared
        event = createEvent(ZWaveAlarmCommandClass.AlarmType.POWER_MANAGEMENT, ReportType.NOTIFICATION, 0, 0xff);
        state = converter.handleEvent(channel, event);
        assertEquals(OnOffType.class, state.getClass());
        assertEquals(OnOffType.OFF, state);
    }

    @Test
    public void sendNotification() {
        ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");

        List<ZWaveCommandClassTransactionPayload> msgs;
        DecimalType command;
        Map<String, String> args = new HashMap<String, String>();
        args.put("event1", AlarmType.SMOKE.toString() + ":1");
        args.put("event2", AlarmType.ACCESS_CONTROL.toString() + ":22");
        args.put("event3", AlarmType.BURGLAR.toString() + ":1");
        args.put("event4", AlarmType.EMERGENCY.toString() + ":1");
        args.put("event5", AlarmType.EMERGENCY.toString() + ":2");
        args.put("event6", AlarmType.EMERGENCY.toString() + ":3");
        ZWaveThingChannel channel = new ZWaveThingChannel(null, uid, DataType.OnOffType,
                CommandClass.COMMAND_CLASS_ALARM.toString(), 0, args);

        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);

        Map<String, String> options = new HashMap<String, String>();
        ZWaveNode node = CreateMockedNode(3, options);

        command = new DecimalType(1);
        byte[] expectedResponse1 = { 113, 5, 0, 0, 0, -1, 1, 1 };
        msgs = converter.receiveCommand(channel, node, command);
        assertEquals(1, msgs.size());
        assertTrue(Arrays.equals(msgs.get(0).getPayloadBuffer(), expectedResponse1));

        command = new DecimalType(2);
        byte[] expectedResponse2 = { 113, 5, 0, 0, 0, -1, 6, 22 };
        msgs = converter.receiveCommand(channel, node, command);
        assertEquals(1, msgs.size());
        assertTrue(Arrays.equals(msgs.get(0).getPayloadBuffer(), expectedResponse2));
    }

    @Test
    public void Alarm_AlarmNumber() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        Map<String, String> args = new HashMap<String, String>();

        // Note test here that data type is ignored
        ZWaveThingChannel channel = new ZWaveThingChannel(null, new ChannelUID("zwave:node:bridge:alarm_number"),
                DataType.OnOffType, CommandClass.COMMAND_CLASS_ALARM.toString(), 0, args);

        ZWaveCommandClassValueEvent event = createEvent(1, 0xff);
        DecimalType state = (DecimalType) converter.handleEvent(channel, event);
        assertEquals(1, state.intValue());

        event = createEvent(18, 0xff);
        state = (DecimalType) converter.handleEvent(channel, event);
        assertEquals(18, state.intValue());
    }

    @Test
    public void Alarm_AlarmRaw() {
        ZWaveAlarmConverter converter = new ZWaveAlarmConverter(null);
        Map<String, String> args = new HashMap<String, String>();

        // Note test here that data type is ignored
        ZWaveThingChannel channel = new ZWaveThingChannel(null, new ChannelUID("zwave:node:bridge:alarm_raw"),
                DataType.StringType, CommandClass.COMMAND_CLASS_ALARM.toString(), 0, args);

        ZWaveCommandClassValueEvent event = createEvent(AlarmType.ACCESS_CONTROL, ReportType.NOTIFICATION, 6, 255);
        StringType state = (StringType) converter.handleEvent(channel, event);

        assertEquals("{\"type\":\"ACCESS_CONTROL\",\"event\":\"6\",\"status\":\"255\"}", state.toString());
    }
}
