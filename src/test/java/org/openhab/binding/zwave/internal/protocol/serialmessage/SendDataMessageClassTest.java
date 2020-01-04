/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
