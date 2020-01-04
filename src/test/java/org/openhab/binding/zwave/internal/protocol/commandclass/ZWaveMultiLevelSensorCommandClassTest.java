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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.ZWaveMultiLevelSensorValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

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

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
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

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
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

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        // assertEquals(event.getNodeId(), 2);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getSensorType(), ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE);
        assertEquals(event.getValue(), new BigDecimal("194.5"));
        assertEquals(event.getSensorScale(), 0);
    }

    @Test
    public void sensorReportShort() {
        byte[] packetData = { 0x01, 0x09, 0x00, 0x04, 0x00, 0x3A, 0x03, 0x31, 0x05, 0x01, (byte) 0xFE };

        assertEquals(0, processCommandClassMessage(packetData).size());
    }

    @Test
    public void Sensor_AccelerationY() {
        byte[] packetData = { 0x01, 0x0B, 0x00, 0x04, 0x00, 0x09, 0x05, 0x31, 0x05, 0x35, 0x21, (byte) 0xE0, 0x3C };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMultiLevelSensorValueEvent event = (ZWaveMultiLevelSensorValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        // assertEquals(event.getNodeId(), 2);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getSensorType(), ZWaveMultiLevelSensorCommandClass.SensorType.ACCELERATION_Y);
        assertEquals(event.getValue(), new BigDecimal("-3.2"));
        assertEquals(event.getSensorScale(), 0);
    }

    @Test
    public void Sensor_AccelerationZ() {
        byte[] packetData = { 0x01, 0x0B, 0x00, 0x04, 0x00, 0x09, 0x05, 0x31, 0x05, 0x36, 0x21, 0x5E, (byte) 0x81 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMultiLevelSensorValueEvent event = (ZWaveMultiLevelSensorValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        // assertEquals(event.getNodeId(), 2);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getSensorType(), ZWaveMultiLevelSensorCommandClass.SensorType.ACCELERATION_Z);
        assertEquals(event.getValue(), new BigDecimal("9.4"));
        assertEquals(event.getSensorScale(), 0);
    }

    @Test
    public void getMessageDirectionV5() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 49, 4, 7, 0 };
        cls.setVersion(5);
        msg = cls.getMessage(ZWaveMultiLevelSensorCommandClass.SensorType.DIRECTION);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getMessageTemperatureV1() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 49, 4 };
        cls.setVersion(1);
        msg = cls.getMessage(ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getMessageTemperatureV5() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 49, 4, 1, 0 };
        cls.setVersion(5);
        msg = cls.getMessage(ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedSensorMessage() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        cls.setVersion(1);
        assertNull(cls.getSupportedSensorMessage());

        byte[] expectedResponseV5 = { 49, 1 };
        cls.setVersion(5);
        msg = cls.getSupportedSensorMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV5));
    }

    @Test
    public void getSupportedScaleMessage() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        cls.setVersion(1);
        assertNull(cls.getSupportedScaleMessage(SensorType.TEMPERATURE));

        byte[] expectedResponseV5 = { 49, 3, 1 };
        cls.setVersion(5);
        msg = cls.getSupportedScaleMessage(SensorType.TEMPERATURE);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV5));
    }

    @Test
    public void getReportMessage() {
        ZWaveMultiLevelSensorCommandClass cls = (ZWaveMultiLevelSensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        cls.setVersion(5);
        msg = cls.getReportMessage(SensorType.TEMPERATURE, 0, BigDecimal.valueOf(23.4));

        byte[] expectedResponseV5 = { 0x31, 0x05, 0x01, 0x22, 0x00, (byte) 0xEA };
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV5));
    }

}
