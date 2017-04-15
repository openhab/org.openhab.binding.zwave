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
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ReportType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ZWaveAlarmValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

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
        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_ALARM);
        assertEquals(event.getReportType(), ReportType.ALARM);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.GENERAL);
        assertEquals(event.getAlarmStatus(), 0x00);
    }

    @Test
    public void Alarm_Smoke() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x10, 0x28, 0x09, 0x71, 0x05, 0x00, 0x00, 0x00, (byte) 0xFF, 0x01,
                0x00, 0x01, 0x03, 0x52 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_ALARM);
        assertEquals(event.getReportType(), ReportType.NOTIFICATION);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.SMOKE);
        assertEquals(event.getAlarmStatus(), 0xFF);
        assertEquals(event.getAlarmEvent(), 0);
    }

    @Test
    public void Notification_Burglar_Motion() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x10, 0x28, 0x09, 0x71, 0x05, 0x00, 0x00, 0x00, (byte) 0xFF, 0x07,
                0x08, 0x00, 0x05, 0x5B };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_ALARM);
        assertEquals(event.getReportType(), ReportType.NOTIFICATION);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.BURGLAR);
        assertEquals(event.getAlarmEvent(), 8);
        assertEquals(event.getAlarmStatus(), 0xFF);
    }

    @Test
    public void Notification_AccessControl_Door() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x10, 0x28, 0x09, 0x71, 0x05, 0x00, 0x00, 0x00, (byte) 0xFF, 0x06,
                0x16, 0x00, 0x05, 0x44 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_ALARM);
        assertEquals(event.getReportType(), ReportType.NOTIFICATION);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.ACCESS_CONTROL);
        assertEquals(event.getAlarmEvent(), 22);
        assertEquals(event.getAlarmStatus(), 0xFF);
    }

    @Test
    public void Notification_PowerManagement_PowerApplied() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x00, 0x0E, 0x09, 0x71, 0x05, 0x00, 0x00, 0x00, (byte) 0xFF, 0x08,
                0x01, 0x00, 0x00, 0x6E };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_ALARM);
        assertEquals(event.getReportType(), ReportType.NOTIFICATION);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.POWER_MANAGEMENT);
        assertEquals(event.getAlarmEvent(), 1);
        assertEquals(event.getAlarmStatus(), 0xFF);
    }

    @Test
    public void Notification_Lock_UnsupportedV1() {
        // This test simulates receiving a lock alarm that is not a standard supported alarm type.
        // We should get the alarm number supported with null type
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x00, 0x0E, 0x04, 0x71, 0x05, 0x12, 0x00, (byte) 0x87 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(CommandClass.COMMAND_CLASS_ALARM, event.getCommandClass());
        assertEquals(ReportType.ALARM, event.getReportType());
        assertEquals(null, event.getAlarmType());
        assertEquals(18, event.getV1AlarmCode());
        assertEquals(0x00, event.getAlarmStatus());
    }

    @Test
    public void getSupportedMessage() {
        ZWaveAlarmCommandClass cls = (ZWaveAlarmCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_ALARM);
        ZWaveCommandClassTransactionPayload msg;

        cls.setVersion(1);
        assertNull(cls.getSupportedMessage());

        byte[] expectedResponseV2 = { 113, 7 };
        cls.setVersion(2);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV2));
    }

    @Test
    public void getSupportedEventMessage() {
        ZWaveAlarmCommandClass cls = (ZWaveAlarmCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_ALARM);
        ZWaveCommandClassTransactionPayload msg;

        cls.setVersion(1);
        assertNull(cls.getSupportedEventMessage(1));

        byte[] expectedResponseV3 = { 113, 1, 1 };
        cls.setVersion(3);
        msg = cls.getSupportedEventMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV3));
    }

    @Test
    public void getMessage() {
        ZWaveAlarmCommandClass cls = (ZWaveAlarmCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_ALARM);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 113, 4, 6 };
        cls.setVersion(1);
        msg = cls.getMessage(AlarmType.ACCESS_CONTROL, 0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));

        byte[] expectedResponseV2 = { 113, 4, 0, 6 };
        cls.setVersion(2);
        msg = cls.getMessage(AlarmType.ACCESS_CONTROL, 0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV2));

        cls.setVersion(3);
        msg = cls.getMessage(AlarmType.ACCESS_CONTROL, 1);
        assertNull(msg);
    }

    @Test
    public void getNotificationReportMessage() {
        ZWaveAlarmCommandClass cls = (ZWaveAlarmCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_ALARM);
        ZWaveCommandClassTransactionPayload msg;

        cls.setVersion(1);
        msg = cls.getNotificationReportMessage(AlarmType.BURGLAR, 1);
        assertNull(msg);

        byte[] expectedResponseV2 = { 113, 5, 0, 0, 0, -1, 7, 1 };
        cls.setVersion(2);
        msg = cls.getNotificationReportMessage(AlarmType.BURGLAR, 1);

        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV2));
    }

    @Test
    public void handleSupportedReport() {
        byte[] packetData = { 0x71, 0x08, 0x01, (byte) 0x80 };

        ZWaveAlarmCommandClass alarmCommandClass = (ZWaveAlarmCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ALARM);
        ZWaveCommandClassPayload serialMessage = new ZWaveCommandClassPayload(packetData);
        alarmCommandClass.handleNotificationSupportedReport(serialMessage, 0);

        assertEquals(1, alarmCommandClass.getSupportedAlarms().size());
        assertEquals(AlarmType.BURGLAR, alarmCommandClass.getSupportedAlarms().iterator().next());
    }
}
