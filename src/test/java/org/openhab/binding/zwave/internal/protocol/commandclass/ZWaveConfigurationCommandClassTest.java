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
import org.openhab.binding.zwave.internal.protocol.ZWaveConfigurationParameter;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveConfigurationCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveConfigurationCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveConfigurationCommandClass cls = (ZWaveConfigurationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_CONFIGURATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 112, 5, 12 };
        cls.setVersion(1);
        msg = cls.getConfigMessage(12);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getValueMessage_WriteOnly() {
        ZWaveConfigurationCommandClass cls = (ZWaveConfigurationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_CONFIGURATION);
        cls.setVersion(1);

        // Make sure we can't read
        cls.setParameterWriteOnly(12, 2, true);
        assertNull(cls.getConfigMessage(12));

        // But we should generate a write request
        ZWaveConfigurationParameter parameter = cls.getParameter(12);
        assertNotNull(cls.setConfigMessage(parameter));
    }

    @Test
    public void getValueMessage_ReadOnly() {
        ZWaveConfigurationCommandClass cls = (ZWaveConfigurationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_CONFIGURATION);
        cls.setVersion(1);

        // Make sure we can read
        cls.setParameterReadOnly(12, true);
        assertNotNull(cls.getConfigMessage(12));

        // But we shouldn't generate a write request
        ZWaveConfigurationParameter parameter = cls.getParameter(12);
        assertNull(cls.setConfigMessage(parameter));
    }

    @Test
    public void setValueMessage() {
        ZWaveConfigurationCommandClass cls = (ZWaveConfigurationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_CONFIGURATION);
        ZWaveCommandClassTransactionPayload msg;

        ZWaveConfigurationParameter parameter = new ZWaveConfigurationParameter(12, 34, 4);

        byte[] expectedResponseV1 = { 112, 4, 12, 4, 0, 0, 0, 34 };
        cls.setVersion(1);
        msg = cls.setConfigMessage(parameter);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
