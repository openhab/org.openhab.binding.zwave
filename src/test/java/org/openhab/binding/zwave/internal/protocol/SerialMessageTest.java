/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.zwave.internal.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;

/**
 * Test cases for {@link SerialMessage}. This performs basic checks on the serial message processing to ensure packets
 * are handled correctly.
 *
 * @author Chris Jackson - Initial version
 */
public class SerialMessageTest {

    @Test
    public void TestCreate() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x01,
                (byte) 0xB8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x51 };
        byte[] packetDataFaulty = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x01,
                (byte) 0xB8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x52 };
        SerialMessage msg = new SerialMessage(packetData);

        assertEquals(msg.getMessageBuffer()[5], 44);
        assertEquals(msg.isValid, true);
        assertEquals(SerialMessageType.Request, msg.getMessageType());
        assertEquals(SerialMessageClass.ApplicationCommandHandler, msg.getMessageClass());

        try {
            assertEquals(msg.getMessagePayloadByte(5), 33);
            assertEquals(msg.getMessagePayloadByte(9), 1);
        } catch (ZWaveSerialMessageException e) {
        }

        boolean oob = false;
        try {
            msg.getMessagePayloadByte(17);
        } catch (ZWaveSerialMessageException e) {
            oob = true;
        }
        assertEquals(oob, true);

        // Make sure we correct detect a packet with a corrupt CRC
        msg = new SerialMessage(packetDataFaulty);
        assertEquals(msg.isValid, false);
    }

    @Test
    public void TestNAK() {
        byte[] packetData = { 0x15 };
        SerialMessage msg = new SerialMessage(packetData);

        assertEquals(SerialMessageType.NAK, msg.getMessageType());
    }

    @Test
    public void TestCAN() {
        byte[] packetData = { 0x18 };
        SerialMessage msg = new SerialMessage(packetData);

        assertEquals(SerialMessageType.CAN, msg.getMessageType());
    }
}
