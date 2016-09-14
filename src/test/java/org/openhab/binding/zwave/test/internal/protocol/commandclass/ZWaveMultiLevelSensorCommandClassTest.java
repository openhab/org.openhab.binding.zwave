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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveMultiLevelSensorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMultiLevelSensorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void Sensor_Luminance() {
        byte[] packetData = { 0x01, 0x0C, 0x00, 0x04, 0x00, 0x02, 0x06, 0x31, 0x05, 0x03, 0x0A, 0x00, 0x67,
                (byte) 0xA9 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMultiLevelSensorValueEvent event = (ZWaveMultiLevelSensorValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.SENSOR_MULTILEVEL);
        // assertEquals(event.getNodeId(), 2);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getSensorType(), ZWaveMultiLevelSensorCommandClass.SensorType.LUMINANCE);
        assertEquals(event.getValue(), new BigDecimal("103"));
        assertEquals(event.getSensorScale(), 1);
    }

    @Test
    public void Sensor_Temperature() {
        byte[] packetData = { 0x01, 0x0C, 0x00, 0x04, 0x00, 0x02, 0x06, 0x31, 0x05, 0x01, 0x22, 0x01, 0x12,
                (byte) 0xF7 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMultiLevelSensorValueEvent event = (ZWaveMultiLevelSensorValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.SENSOR_MULTILEVEL);
        // assertEquals(event.getNodeId(), 2);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getSensorType(), ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE);
        assertEquals(event.getValue(), new BigDecimal("27.4"));
        assertEquals(event.getSensorScale(), 0);
    }

    @Test
    public void Sensor_Temperature2() {
        byte[] packetData = { 0x01, 0x0C, 0x00, 0x04, 0x00, 0x16, 0x06, 0x31, 0x05, 0x01, 0x22, 0x07, (byte) 0x99,
                0x6E };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMultiLevelSensorValueEvent event = (ZWaveMultiLevelSensorValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.SENSOR_MULTILEVEL);
        // assertEquals(event.getNodeId(), 2);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getSensorType(), ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE);
        assertEquals(event.getValue(), new BigDecimal("194.5"));
        assertEquals(event.getSensorScale(), 0);
    }

    @Test
    public void getMessageDirectionV5() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.SENSOR_MULTILEVEL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 11, 0, 19, 99, 4, 49, 4, 7, 0, 0, 16, -94 };
        cls.setVersion(5);
        msg = cls.getMessage(ZWaveMultiLevelSensorCommandClass.SensorType.DIRECTION);
        msg.setCallbackId(16);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getMessageTemperatureV1() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.SENSOR_MULTILEVEL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 49, 4, 0, 1, -80 };
        cls.setVersion(1);
        msg = cls.getMessage(ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE);
        msg.setCallbackId(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getMessageTemperatureV5() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.SENSOR_MULTILEVEL);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 11, 0, 19, 99, 4, 49, 4, 1, 0, 0, 1, -75 };
        cls.setVersion(5);
        msg = cls.getMessage(ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE);
        msg.setCallbackId(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedSensorMessage() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.SENSOR_MULTILEVEL);
        SerialMessage msg;

        cls.setVersion(1);
        msg = cls.getSupportedSensorMessage();
        assertNull(msg);

        byte[] expectedResponseV5 = { 1, 9, 0, 19, 99, 2, 49, 1, 0, 0, -76 };
        cls.setVersion(5);
        msg = cls.getSupportedSensorMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV5));
    }

    @Test
    public void getSupportedScaleMessage() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.SENSOR_MULTILEVEL);
        SerialMessage msg;

        cls.setVersion(1);
        msg = cls.getSupportedScaleMessage(SensorType.TEMPERATURE);
        assertNull(msg);

        byte[] expectedResponseV5 = { 1, 10, 0, 19, 99, 3, 49, 3, 1, 0, 0, -75 };
        cls.setVersion(5);
        msg = cls.getSupportedScaleMessage(SensorType.TEMPERATURE);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV5));
    }

}
