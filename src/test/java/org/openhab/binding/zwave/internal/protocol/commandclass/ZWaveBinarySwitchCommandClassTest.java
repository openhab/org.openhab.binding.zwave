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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveBinarySwitchCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveBinarySwitchCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveBinarySwitchCommandClass cls = (ZWaveBinarySwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_BINARY);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 37, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveBinarySwitchCommandClass cls = (ZWaveBinarySwitchCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SWITCH_BINARY);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 37, 1, -1 };
        cls.setVersion(1);
        msg = cls.setValueMessage(33);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
