/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.serialmessage;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiSoftResetMessageClass;

/**
 * Test cases for SerialApiGetInitDataMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class SerialApiSoftResetMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 1, 3, 0, 8, -12 };

        SerialMessage msg;
        SerialApiSoftResetMessageClass handler = new SerialApiSoftResetMessageClass();

        msg = handler.doRequest().getSerialMessage();
        msg.setCallbackId(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }
}
