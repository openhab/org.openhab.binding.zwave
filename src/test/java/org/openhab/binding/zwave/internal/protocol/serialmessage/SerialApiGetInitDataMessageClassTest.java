/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;

/**
 * Test cases for SerialApiGetInitDataMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class SerialApiGetInitDataMessageClassTest extends ZWaveCommandProcessorTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 1, 3, 0, 2, -2 };

        SerialMessage msg;
        SerialApiGetInitDataMessageClass handler = new SerialApiGetInitDataMessageClass();

        msg = handler.doRequest().getSerialMessage();
        msg.setCallbackId(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }

    @Test
    public void testIncomingRequest() {
        byte[] packetData = { 0x01, 0x25, 0x01, 0x02, 0x05, 0x00, 0x1D, (byte) 0xFB, 0x3F, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00 };

        SerialApiGetInitDataMessageClass handler = new SerialApiGetInitDataMessageClass();
        ProcessResponse(handler, packetData);

        assertEquals(13, handler.getNodes().size());
    }
}
