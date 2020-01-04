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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBasicCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatFanModeCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveBasicCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveThermostatFanModeCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveThermostatFanModeCommandClass cls = (ZWaveThermostatFanModeCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_MODE);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 68, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveThermostatFanModeCommandClass cls = (ZWaveThermostatFanModeCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_MODE);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 68, 4 };
        cls.setVersion(1);
        msg = cls.setValueMessage(34);
        // byte[] x = msg.getMessageBuffer();
        // assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedMessage() {
        ZWaveThermostatFanModeCommandClass cls = (ZWaveThermostatFanModeCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_MODE);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 68, 4 };
        cls.setVersion(1);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
