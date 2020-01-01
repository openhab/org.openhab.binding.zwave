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

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveBinarySensorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 * @author Jorg de Jong
 */
public class ZWaveBinarySensorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveBinarySensorCommandClass cls = (ZWaveBinarySensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_BINARY);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 48, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getValueMessageType() {
        ZWaveBinarySensorCommandClass cls = (ZWaveBinarySensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_BINARY);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 48, 2, 10 };
        cls.setVersion(2);
        msg = cls.getValueMessage(SensorType.DOORWINDOW);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveBinarySensorCommandClass cls = (ZWaveBinarySensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_BINARY);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV2 = { 48, 1 };
        cls.setVersion(2);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV2));
    }

    @Test
    public void reportSupportedSensors() throws Exception {
        byte[] packetData = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x2F, 0x04, 0x30, 0x04, 0x00, 0x15, (byte) 0xFB };
        SerialMessage msg = new SerialMessage(packetData);

        ZWaveBinarySensorCommandClass cls = (ZWaveBinarySensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_BINARY);
        cls.setVersion(2);
        cls.handleApplicationCommandRequest(new ZWaveCommandClassPayload(msg), 0);
        assertEquals(3, cls.getSupportedTypes().size());
        assertTrue(cls.getSupportedTypes().contains(SensorType.DOORWINDOW));
        assertTrue(cls.getSupportedTypes().contains(SensorType.TAMPER));
        assertTrue(cls.getSupportedTypes().contains(SensorType.MOTION));
    }

}
