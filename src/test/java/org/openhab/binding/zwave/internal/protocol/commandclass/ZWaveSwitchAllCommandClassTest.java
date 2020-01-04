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
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSwitchAllCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSwitchAllCommandClass.SwitchAllMode;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveSwitchAllCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveSwitchAllCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveSwitchAllCommandClass cls = (ZWaveSwitchAllCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_ALL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 39, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveSwitchAllCommandClass cls = (ZWaveSwitchAllCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_ALL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 39, 1, 2 };
        cls.setVersion(1);
        msg = cls.setValueMessage(SwitchAllMode.SWITCH_ALL_INCLUDE_OFF_ONLY.ordinal());
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void allOnMessage() {
        ZWaveSwitchAllCommandClass cls = (ZWaveSwitchAllCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_ALL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 39, 4 };
        cls.setVersion(1);
        msg = cls.allOnMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void allOffMessage() {
        ZWaveSwitchAllCommandClass cls = (ZWaveSwitchAllCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_ALL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 39, 5 };
        cls.setVersion(1);
        msg = cls.allOffMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void processSwitchAllReport() {
        byte[] packetData = { 0x01, 0x0B, 0x00, 0x04, 0x00, 0x02, 0x05, 0x27, 0x03, (byte) 0xFF, 0x07, 0x00, 0x2B };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_SWITCH_ALL);
        assertEquals(event.getValue(), 0xff);
    }
}
