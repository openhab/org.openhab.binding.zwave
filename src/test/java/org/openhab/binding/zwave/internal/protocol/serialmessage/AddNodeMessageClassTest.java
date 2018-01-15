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
import org.openhab.binding.zwave.internal.protocol.serialmessage.AddNodeMessageClass;

/**
 * Test cases for AddNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class AddNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponseStartLocal = { 1, 1 };
        byte[] expectedResponseStartHigh = { -127, 1 };
        byte[] expectedResponseStartNetwork = { -63, 1 };
        byte[] expectedResponseStop = { 5, 1 };
        byte[] expectedResponseStopComplete = { 5, 0 };

        ZWaveSerialPayload msg;
        AddNodeMessageClass handler = new AddNodeMessageClass();

        msg = handler.doRequestStart(false, false);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStartLocal));

        msg = handler.doRequestStart(true, false);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStartHigh));

        msg = handler.doRequestStart(true, true);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStartNetwork));

        msg = handler.doRequestStop(false);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStop));

        msg = handler.doRequestStop(true);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStopComplete));
    }
}
