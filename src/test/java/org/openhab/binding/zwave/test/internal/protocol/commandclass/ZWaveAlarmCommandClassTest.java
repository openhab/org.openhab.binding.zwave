/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ReportType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ZWaveAlarmValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveAlarmCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveAlarmCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void Alarm_General_V1() {
        byte[] packetData = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x49, 0x04, 0x71, 0x05, 0x00, 0x00, (byte) 0xC8 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.ALARM);
        assertEquals(event.getReportType(), ReportType.ALARM);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.GENERAL);
        assertEquals(event.getAlarmStatus(), 0x00);
    }

    @Test
    public void Alarm_Smoke() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x10, 0x28, 0x0A, 0x71, 0x05, 0x00, 0x00, 0x00, (byte) 0xFF, 0x01,
                0x00, 0x01, 0x03, 0x51 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.ALARM);
        assertEquals(event.getReportType(), ReportType.NOTIFICATION);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.SMOKE);
        assertEquals(event.getAlarmStatus(), 0xFF);
        assertEquals(event.getAlarmEvent(), 0);
    }

    @Test
    public void Notification_Burglar_Motion() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x10, 0x28, 0x0A, 0x71, 0x05, 0x00, 0x00, 0x00, (byte) 0xFF, 0x07,
                0x08, 0x00, 0x05, 0x58 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.ALARM);
        assertEquals(event.getReportType(), ReportType.NOTIFICATION);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.BURGLAR);
        assertEquals(event.getAlarmEvent(), 8);
        assertEquals(event.getAlarmStatus(), 0xFF);
    }

    @Test
    public void Notification_AccessControl_Door() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x10, 0x28, 0x0A, 0x71, 0x05, 0x00, 0x00, 0x00, (byte) 0xFF, 0x06,
                0x16, 0x00, 0x05, 0x47 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.ALARM);
        assertEquals(event.getReportType(), ReportType.NOTIFICATION);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.ACCESS_CONTROL);
        assertEquals(event.getAlarmEvent(), 22);
        assertEquals(event.getAlarmStatus(), 0xFF);
    }

    @Test
    public void Notification_PowerManagement_PowerApplied() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x00, 0x0E, 0x0A, 0x71, 0x05, 0x00, 0x00, 0x00, (byte) 0xFF, 0x08,
                0x01, 0x00, 0x00, 0x6D };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.ALARM);
        assertEquals(event.getReportType(), ReportType.NOTIFICATION);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.POWER_MANAGEMENT);
        assertEquals(event.getAlarmEvent(), 1);
        assertEquals(event.getAlarmStatus(), 0xFF);
    }

    @Test
    public void getSupportedMessage() {
        ZWaveAlarmCommandClass cls = (ZWaveAlarmCommandClass) getCommandClass(CommandClass.ALARM);
        SerialMessage msg;

        cls.setVersion(1);
        msg = cls.getSupportedMessage();
        assertNull(msg);

        byte[] expectedResponseV2 = { 1, 9, 0, 19, 99, 2, 113, 7, 0, 0, -14 };
        cls.setVersion(2);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV2));
    }

    @Test
    public void getSupportedEventMessage() {
        ZWaveAlarmCommandClass cls = (ZWaveAlarmCommandClass) getCommandClass(CommandClass.ALARM);
        SerialMessage msg;

        cls.setVersion(1);
        msg = cls.getSupportedEventMessage(1);
        assertNull(msg);

        byte[] expectedResponseV3 = { 1, 10, 0, 19, 99, 3, 113, 1, 1, 0, 0, -9 };
        cls.setVersion(3);
        msg = cls.getSupportedEventMessage(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV3));
    }

    @Test
    public void getMessage() {
        ZWaveAlarmCommandClass cls = (ZWaveAlarmCommandClass) getCommandClass(CommandClass.ALARM);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, 113, 4, 6, 0, 0, -11 };
        cls.setVersion(1);
        msg = cls.getMessage(AlarmType.ACCESS_CONTROL, 0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));

        byte[] expectedResponseV2 = { 1, 11, 0, 19, 99, 4, 113, 4, 0, 6, 0, 0, -13 };
        cls.setVersion(2);
        msg = cls.getMessage(AlarmType.ACCESS_CONTROL, 0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV2));

        byte[] expectedResponseV3 = { 1, 12, 0, 19, 99, 5, 113, 4, 0, 6, 1, 0, 0, -12 };
        cls.setVersion(3);
        msg = cls.getMessage(AlarmType.ACCESS_CONTROL, 1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV3));
    }

    @Test
    public void getNotificationReportMessage() {
        ZWaveAlarmCommandClass cls = (ZWaveAlarmCommandClass) getCommandClass(CommandClass.ALARM);
        SerialMessage msg;

        cls.setVersion(1);
        msg = cls.getNotificationReportMessage(AlarmType.BURGLAR, 1);
        assertNull(msg);

        byte[] expectedResponseV2 = { 1, 15, 0, 19, 99, 8, 113, 5, 0, 0, 0, -1, 7, 1, 0, 0, 5 };
        cls.setVersion(2);
        msg = cls.getNotificationReportMessage(AlarmType.BURGLAR, 1);

        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV2));
    }

    @Test
    public void handleSupportedReport() {
        byte[] packetData = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x49, 0x04, 0x71, 0x08, 0x01, (byte) 0x80, 0x44 };

        ZWaveAlarmCommandClass alarmCommandClass = (ZWaveAlarmCommandClass) getCommandClass(CommandClass.ALARM);
        SerialMessage serialMessage = new SerialMessage(packetData);
        try {
            alarmCommandClass.handleApplicationCommandRequest(serialMessage, 4, 0);

            assertEquals(1, alarmCommandClass.getSupportedAlarms().size());
            assertEquals(AlarmType.BURGLAR, alarmCommandClass.getSupportedAlarms().iterator().next());
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
