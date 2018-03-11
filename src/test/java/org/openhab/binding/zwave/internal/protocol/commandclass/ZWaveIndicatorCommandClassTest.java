/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveIndicatorCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveIndicatorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveIndicatorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveIndicatorCommandClass cls = (ZWaveIndicatorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_INDICATOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -121, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveIndicatorCommandClass cls = (ZWaveIndicatorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_INDICATOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -121, 1, 34 };
        cls.setVersion(1);
        msg = cls.setValueMessage(34);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
