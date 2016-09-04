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
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * Test cases for {@link ZWaveColorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveColorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getCapabilityMessage() {
        ZWaveColorCommandClass cls = (ZWaveColorCommandClass) getCommandClass(CommandClass.COLOR);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 51, 1, 0, 0, -74 };
        cls.setVersion(1);
        msg = cls.getCapabilityMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getValueMessage() {
        ZWaveColorCommandClass cls = (ZWaveColorCommandClass) getCommandClass(CommandClass.COLOR);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, 51, 3, 1, 0, 0, -73 };
        cls.setVersion(1);
        msg = cls.getValueMessage(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveColorCommandClass cls = (ZWaveColorCommandClass) getCommandClass(CommandClass.COLOR);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 11, 0, 19, 99, 4, 51, 5, 3, 80, 0, 0, -27 };
        cls.setVersion(1);
        msg = cls.setValueMessage(1, 80);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

}
