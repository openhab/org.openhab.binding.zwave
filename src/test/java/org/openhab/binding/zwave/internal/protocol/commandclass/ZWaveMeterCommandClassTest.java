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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.MeterScale;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.ZWaveMeterValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveMeterCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMeterCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void Meter_Electric_Watts() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x01,
                (byte) 0xB7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x5E };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMeterValueEvent event = (ZWaveMeterValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_METER);
        // assertEquals(event.getNodeId(), 44);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getMeterScale(), ZWaveMeterCommandClass.MeterScale.E_W);
        assertEquals(event.getMeterType(), ZWaveMeterCommandClass.MeterType.ELECTRIC);
        assertEquals(event.getValue(), new BigDecimal("43.9"));
    }

    @Test
    public void Meter_Electric_Watts_2() {
        byte[] packetData = { 0x01, 0x0E, 0x00, 0x04, 0x00, 0x1B, 0x08, 0x32, 0x02, 0x21, 0x74, 0x00, 0x02, 0x5C, 0x1D,
                (byte) 0xC0 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMeterValueEvent event = (ZWaveMeterValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_METER);
        // assertEquals(event.getNodeId(), 27);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getMeterScale(), ZWaveMeterCommandClass.MeterScale.E_W);
        assertEquals(event.getMeterType(), ZWaveMeterCommandClass.MeterType.ELECTRIC);
        assertEquals(event.getValue(), new BigDecimal("154.653"));
    }

    @Test
    public void Meter_Supported() {
        byte[] packetData = { 0x32, 0x04, (byte) 0xE1, 0x35 };

        ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(packetData);
        ZWaveMeterCommandClass meterCommandClass = (ZWaveMeterCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_METER);
        meterCommandClass.handleMeterSupportedReport(payload, 0);
        // E_KWh, E_W, E_V, E_A
    }

    @Test
    public void Meter_SupportedV3() {
        byte[] packetData = { 0x32, 0x04, (byte) 0xE1, (byte) 0xC7, 0x01, 0x03 };

        ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(packetData);
        ZWaveMeterCommandClass meterCommandClass = (ZWaveMeterCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_METER);
        meterCommandClass.handleMeterSupportedReport(payload, 0);
        // [E_KWh, E_W, E_KVARH, E_KVAh, E_Power_Factor]
    }

    @Test
    public void setValueMessage() {
        ZWaveMeterCommandClass cls = (ZWaveMeterCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_METER);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 50, 1 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getResetMessage() {
        ZWaveMeterCommandClass cls = (ZWaveMeterCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_METER);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 50, 5 };
        cls.setVersion(2);
        Map<String, String> options = new HashMap<String, String>();
        options.put("meterCanReset", "true");
        cls.setOptions(options);
        msg = cls.getResetMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveMeterCommandClass cls = (ZWaveMeterCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_METER);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 50, 3 };
        cls.setVersion(1);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getMessage() {
        ZWaveMeterCommandClass cls = (ZWaveMeterCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_METER);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 50, 1, 0 };
        cls.setVersion(1);
        msg = cls.getMessage(MeterScale.E_KWh);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setOptions() {
        ZWaveMeterCommandClass cls = (ZWaveMeterCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_METER);

        // Not supported in V1
        assertEquals(0, cls.initialize(true).size());

        // Get supported in V2
        cls.setVersion(2);
        assertEquals(1, cls.initialize(true).size());

        Map<String, String> options = new HashMap<String, String>(2);
        options.put("meterType", "ELECTRIC");
        options.put("meterScale", "E_W");
        cls.setOptions(options);

        // Don't request if we've set the type and scale
        assertEquals(0, cls.initialize(true).size());
    }

    @Test
    public void Meter_Electric_PAN04_KWh() {
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x10, 0x51, 0x0A, 0x32, 0x02, 0x21, 0x44, 0x00, 0x00, 0x01,
                (byte) 0x93, 0x00, 0x00, 0x67 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveMeterValueEvent event = (ZWaveMeterValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_METER);
        assertEquals(0, event.getEndpoint());
        assertEquals(ZWaveMeterCommandClass.MeterScale.E_KWh, event.getMeterScale());
        assertEquals(ZWaveMeterCommandClass.MeterType.ELECTRIC, event.getMeterType());
        assertEquals(new BigDecimal("4.03"), event.getValue());
    }

    @Test
    public void Meter_Electric_PAN04_Error() {
        // Command has invalid length - only 3 bytes of data when says it has 4
        byte[] packetData = { 0x01, 0x10, 0x00, 0x04, 0x10, 0x46, 0x07, 0x32, 0x02, 0x21, 0x44, 0x00, 0x00, 0x01,
                (byte) 0xee };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 0);
    }

}
