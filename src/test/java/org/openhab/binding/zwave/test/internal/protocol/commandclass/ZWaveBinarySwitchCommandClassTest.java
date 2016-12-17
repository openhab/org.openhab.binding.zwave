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
