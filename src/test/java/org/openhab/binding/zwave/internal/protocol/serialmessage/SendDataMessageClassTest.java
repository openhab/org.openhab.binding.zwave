/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SendDataMessageClass;

/**
 * Test cases for SendDataMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class SendDataMessageClassTest extends ZWaveCommandProcessorTest {
    @Test
    public void doRequest() {
        byte[] responseData = { 0x01, 0x07, 0x00, 0x13, 0x0E, 0x01, 0x01, 0x20, (byte) 0xC5 };

        SendDataMessageClass handler = new SendDataMessageClass();
        // SerialMessage msg = new SerialMessage(responseData);

        ProcessRequest(handler, null, responseData);
        // handler.handleRequest(zController, transaction, msg);

        // assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }
}
