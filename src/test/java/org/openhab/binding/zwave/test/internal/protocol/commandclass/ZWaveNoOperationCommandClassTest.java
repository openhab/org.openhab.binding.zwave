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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveNoOperationCommandClass;

/**
 * Test cases for {@link ZWaveNoOperationCommandClassTest}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveNoOperationCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getNoOperationMessage() {
        ZWaveNoOperationCommandClass cls = (ZWaveNoOperationCommandClass) getCommandClass(CommandClass.NO_OPERATION);

        SerialMessage msg;

        byte[] expectedResponse1 = { 1, 8, 0, 19, 99, 1, 0, 0, 0, -122 };
        msg = cls.getNoOperationMessage().getSerialMessage();
        msg.setCallbackId(0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse1));
    }
}
