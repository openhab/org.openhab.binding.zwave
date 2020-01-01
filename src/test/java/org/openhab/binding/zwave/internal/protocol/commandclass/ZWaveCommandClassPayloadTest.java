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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;

public class ZWaveCommandClassPayloadTest {
    @Test
    public void TestConstructors() {
        ZWaveCommandClassPayload payload1 = new ZWaveCommandClassPayload(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        assertEquals(10, payload1.getPayloadLength());
        assertEquals(1, payload1.getCommandClassId());
        assertEquals(2, payload1.getCommandClassCommand());
        assertEquals(3, payload1.getPayloadByte(2));
        assertTrue(Arrays.equals(payload1.getPayloadBuffer(), new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }));

        ZWaveCommandClassPayload payload2 = new ZWaveCommandClassPayload(payload1, 2);
        assertEquals(8, payload2.getPayloadLength());
        assertEquals(3, payload2.getCommandClassId());
        assertEquals(4, payload2.getCommandClassCommand());
        assertEquals(5, payload2.getPayloadByte(2));
        assertTrue(Arrays.equals(payload2.getPayloadBuffer(), new byte[] { 3, 4, 5, 6, 7, 8, 9, 10 }));

        ZWaveCommandClassPayload payload3 = new ZWaveCommandClassPayload(payload1, 2, 8);
        assertEquals(6, payload3.getPayloadLength());
        assertEquals(3, payload3.getCommandClassId());
        assertEquals(4, payload3.getCommandClassCommand());
        assertEquals(5, payload3.getPayloadByte(2));
        assertTrue(Arrays.equals(payload3.getPayloadBuffer(), new byte[] { 3, 4, 5, 6, 7, 8 }));

        byte[] packetData = { 0x01, 0x0F, 0x00, 0x04, 0x00, 0x07, 0x06, (byte) 0x81, 0x06, -127, 4, 127, 0, -120 };
        SerialMessage serialMsg = new SerialMessage(packetData);
        ZWaveCommandClassPayload payload4;
        try {
            payload4 = new ZWaveCommandClassPayload(serialMsg);
            assertEquals(6, payload4.getPayloadLength());
            assertEquals(129, payload4.getCommandClassId());
            assertEquals(6, payload4.getCommandClassCommand());
            assertEquals(4, payload4.getPayloadByte(3));
            assertTrue(Arrays.equals(payload4.getPayloadBuffer(), new byte[] { -127, 6, -127, 4, 127, 0 }));
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
