/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.serialmessage;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignReturnRouteMessageClass;

/**
 * Test cases for AssignReturnRouteMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class AssignReturnRouteMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 12, 44 };

        AssignReturnRouteMessageClass handler = new AssignReturnRouteMessageClass();
        ZWaveSerialPayload msg = handler.doRequest(12, 44);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AssignReturnRoute);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }
}
