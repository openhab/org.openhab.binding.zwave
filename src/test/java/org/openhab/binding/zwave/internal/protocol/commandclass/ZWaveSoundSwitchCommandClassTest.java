/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveBasicCommandClass}.
 *
 * @author Kennet Nielsen - Initial version
 */
public class ZWaveSoundSwitchCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveSoundSwitchCommandClass cls = (ZWaveSoundSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SOUND_SWITCH);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 0x79, 9 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveSoundSwitchCommandClass cls = (ZWaveSoundSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SOUND_SWITCH);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 0x79, 8, 3 };
        cls.setVersion(1);
        msg = cls.setValueMessage(3);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getConfigMessage() {
        ZWaveSoundSwitchCommandClass cls = (ZWaveSoundSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SOUND_SWITCH);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 0x79, 6 };
        cls.setVersion(1);
        msg = cls.getConfigMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setConfigMessage() {
        ZWaveSoundSwitchCommandClass cls = (ZWaveSoundSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SOUND_SWITCH);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 0x79, 5, 100, 0 };
        cls.setVersion(1);
        msg = cls.setConfigMessage(100, 0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setConfigMessage_will_use_volume_0_when_changing_tone_when_this_is_the_last_known_volume() {
        ZWaveSoundSwitchCommandClass cls = (ZWaveSoundSwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SOUND_SWITCH);
        cls.setVersion(1);
        byte[] input = { 0x79, 7, 0, 2 }; // volume 0 and default tone 2
        cls.handleConfigReport(new ZWaveCommandClassPayload(input), 0);

        byte[] expectedResponseV1 = { 0x79, 5, 0, 1 };
        ZWaveCommandClassTransactionPayload msg = cls.setConfigMessage(255, 1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
