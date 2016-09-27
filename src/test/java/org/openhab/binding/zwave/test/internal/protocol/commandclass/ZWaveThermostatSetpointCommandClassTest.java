/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatSetpointCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatSetpointCommandClass.SetpointType;

/**
 * Test cases for {@link ZWaveThermostatSetpointCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveThermostatSetpointCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveThermostatSetpointCommandClass cls = (ZWaveThermostatSetpointCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 67, 4, 0, 0, -61 };
        cls.setVersion(1);
        msg = cls.getValueMessage().getSerialMessage();
        msg.setCallbackId(0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setMessage() {
        ZWaveThermostatSetpointCommandClass cls = (ZWaveThermostatSetpointCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT);
        SerialMessage msg;

        byte[] expectedResponse = { 1, 13, 0, 19, 99, 6, 67, 1, 2, 34, 0, -31, 0, 0, 7 };
        cls.setVersion(1);
        msg = cls.setMessage(0, SetpointType.COOLING, new BigDecimal(22.5)).getSerialMessage();
        msg.setCallbackId(0);
        byte[] x = msg.getMessageBuffer();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveThermostatSetpointCommandClass cls = (ZWaveThermostatSetpointCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 67, 4, 0, 0, -61 };
        cls.setVersion(1);
        msg = cls.getSupportedMessage().getSerialMessage();
        msg.setCallbackId(0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }
}
