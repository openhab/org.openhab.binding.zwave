/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass;
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
}
