/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBasicCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatModeCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveBasicCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveThermostatModeCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveThermostatModeCommandClass cls = (ZWaveThermostatModeCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_THERMOSTAT_MODE);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 64, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveThermostatModeCommandClass cls = (ZWaveThermostatModeCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_THERMOSTAT_MODE);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 64, 4 };
        cls.setVersion(1);
        // msg = cls.setValueMessage(34);
        // byte[] x = msg.getMessageBuffer();
        // assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveThermostatModeCommandClass cls = (ZWaveThermostatModeCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_THERMOSTAT_MODE);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 64, 4 };
        cls.setVersion(1);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
