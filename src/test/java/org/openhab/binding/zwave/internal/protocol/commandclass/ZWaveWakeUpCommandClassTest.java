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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveWakeUpCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveWakeUpCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getNoMoreInformationMessage() {
        ZWaveWakeUpCommandClass cls = (ZWaveWakeUpCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_WAKE_UP);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -124, 8 };
        cls.setVersion(1);
        msg = cls.getNoMoreInformationMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getIntervalMessage() {
        ZWaveWakeUpCommandClass cls = (ZWaveWakeUpCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_WAKE_UP);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -124, 5 };
        cls.setVersion(1);
        msg = cls.getIntervalMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setInterval() {
        ZWaveWakeUpCommandClass cls = (ZWaveWakeUpCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_WAKE_UP);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -124, 4, 0, 38, -108, 0 };
        cls.setVersion(1);
        msg = cls.setInterval(0, 9876);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getIntervalCapabilitiesMessage() {
        ZWaveWakeUpCommandClass cls = (ZWaveWakeUpCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_WAKE_UP);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -124, 9 };
        cls.setVersion(1);
        msg = cls.getIntervalCapabilitiesMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
