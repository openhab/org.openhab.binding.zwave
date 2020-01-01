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

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Tests for {@link ZWaveCrc16EncapsulationCommandClass}
 *
 * @author Chris Jackson - initial contribution
 *
 */
public class ZWaveCrc16EncapsulationCommandClassTest extends ZWaveCommandClassTest {
    // This test uses the data from the spec
    @Ignore
    @Test
    public void ProcessMessageZWaveSpec() {
        byte[] packetData = { 0x01, 0x0d, 0x00, 0x04, 0x00, 0x07, 0x07, 0x56, 0x01, 0x20, 0x02, 0x4d, 0x26,
                (byte) 0xe8 };

        processCommandClassMessage(packetData);
    }

    @Test
    public void ProcessMessage1() {
        byte[] packetData = { 0x01, 0x0d, 0x00, 0x04, 0x00, 0x07, 0x07, 0x56, 0x01, 0x20, 0x03, 0x00, (byte) 0x8c, 0x58,
                0x56 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);
        assertEquals(events.size(), 1);
        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);
        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_BASIC);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getValue(), 0);
    }

    @Test
    public void ProcessMessage2() {
        byte[] packetData = { 0x01, 0x11, 0x00, 0x04, 0x00, 0x04, 0x0A, 0x56, 0x01, 0x31, 0x05, 0x04, 0x22, 0x00, 0x08,
                (byte) 0xF8, (byte) 0xD3, (byte) 0xCF, 0x4D };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);
        assertEquals(events.size(), 1);
        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);
        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getValue(), new BigDecimal("0.8"));
    }
}
