/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBasicCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWavePowerLevelCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveBasicCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWavePowerLevelCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWavePowerLevelCommandClass cls = (ZWavePowerLevelCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_POWERLEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 115, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWavePowerLevelCommandClass cls = (ZWavePowerLevelCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_POWERLEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 115, 1, 1, 1 };
        cls.setVersion(1);
        msg = cls.setValueMessage(1, 1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
