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
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass.StartStopDirection;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass.ZWaveStartStopEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveMultiLevelSwitchCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMultiLevelSwitchCommandClassTest extends ZWaveCommandClassTest {
    @Test
    public void getValueMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 38, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 38, 1, 56 };
        cls.setVersion(1);
        msg = cls.setValueMessage(56);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void startLevelChangeMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 38, 4, 32, 0, 43 };
        cls.setVersion(1);
        msg = cls.startLevelChangeMessage(true, 43);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void stopLevelChangeMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 38, 5 };
        cls.setVersion(1);
        msg = cls.stopLevelChangeMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV3 = { 38, 6 };
        cls.setVersion(3);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV3));
    }

    @Test
    public void initialize() {
        ZWaveMultiLevelSwitchCommandClass cls = (ZWaveMultiLevelSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL);
        Collection<ZWaveCommandClassTransactionPayload> msgs;

        cls.setVersion(1);
        msgs = cls.initialize(true);
        assertEquals(0, msgs.size());

        cls.setVersion(3);
        msgs = cls.initialize(true);
        assertEquals(1, msgs.size());
    }

    @Test
    public void startStop_Stop() {
        byte[] packetData = { 0x01, 0x0C, 0x00, 0x04, 0x00, 0x11, 0x02, 0x26, 0x05, (byte) 0xC7 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(1, events.size());

        ZWaveStartStopEvent event = (ZWaveStartStopEvent) events.get(0);

        assertEquals(CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL, event.getCommandClass());
        assertEquals(StartStopDirection.STOP, event.direction);
    }

    @Test
    public void startStop_Increase() {
        byte[] packetData = { 0x01, 0x0C, 0x00, 0x04, 0x00, 0x11, 0x06, 0x26, 0x04, 0x38, 0x00, (byte) 0xFF, 0x00,
                (byte) 0x05 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(1, events.size());

        ZWaveStartStopEvent event = (ZWaveStartStopEvent) events.get(0);

        assertEquals(CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL, event.getCommandClass());
        assertEquals(StartStopDirection.INCREASE, event.direction);
    }

    @Test
    public void startStop_Decrease() {
        byte[] packetData = { 0x01, 0x0C, 0x00, 0x04, 0x00, 0x11, 0x06, 0x26, 0x04, 0x78, 0x00, (byte) 0xFF, 0x00,
                (byte) 0x45 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(1, events.size());

        ZWaveStartStopEvent event = (ZWaveStartStopEvent) events.get(0);

        assertEquals(CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL, event.getCommandClass());
        assertEquals(StartStopDirection.DECREASE, event.direction);
    }
}
