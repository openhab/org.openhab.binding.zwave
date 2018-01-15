/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SetSucNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SetSucNodeMessageClass.SUCType;

/**
 * Test cases for SetSucNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class SetSucNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponseNone = { 12, 1, 0, 0 };
        byte[] expectedResponseBasic = { 12, 0, 0, 0 };

        ZWaveSerialPayload msg;
        SetSucNodeMessageClass handler = new SetSucNodeMessageClass();

        msg = handler.doRequest(12, SUCType.BASIC);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.SetSucNodeID);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseNone));

        msg = handler.doRequest(12, SUCType.NONE);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.SetSucNodeID);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseBasic));
    }
}
