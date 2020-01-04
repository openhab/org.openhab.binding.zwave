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
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveUserCodeCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveUserCodeCommandClass.UserIdStatusType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveUserCodeCommandClass.ZWaveUserCodeValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveUserCodeCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveUserCodeCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void processUserCodeReportAscii() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x10, 0x1C, 0x0E, 0x63, 0x03, 0x01, 0x01, (byte) 0x30,
                (byte) 0x31, (byte) 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, (byte) 0x8C };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(1, events.size());
        ZWaveUserCodeValueEvent event = (ZWaveUserCodeValueEvent) events.get(0);
        assertEquals("0123456789", event.getCode());
        assertEquals(1, event.getId());
        assertEquals(UserIdStatusType.OCCUPIED, event.getStatus());
    }

    @Test
    public void processUserCodeReportAsciiShort() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x10, 0x1C, 0x08, 0x63, 0x03, 0x02, 0x01, 0x31, 0x31, 0x31, 0x33,
                (byte) 0x8A };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(1, events.size());
        ZWaveUserCodeValueEvent event = (ZWaveUserCodeValueEvent) events.get(0);
        assertEquals("1113", event.getCode());
        assertEquals(2, event.getId());
        assertEquals(UserIdStatusType.OCCUPIED, event.getStatus());
    }

    @Test
    public void processUserCodeReportHex() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x10, 0x1C, 0x0E, 0x63, 0x03, 0x01, 0x01, (byte) 0x2f,
                (byte) 0x31, (byte) 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, (byte) 0x93 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(1, events.size());
        ZWaveUserCodeValueEvent event = (ZWaveUserCodeValueEvent) events.get(0);
        assertEquals("2F 31 32 33 34 35 36 37 38 39", event.getCode());
        assertEquals(1, event.getId());
        assertEquals(UserIdStatusType.OCCUPIED, event.getStatus());
    }

    @Test
    public void processUserCodeReportAvailable() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x10, 0x1C, 0x04, 0x63, 0x03, 0x03, 0x00, (byte) 0x84 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(1, events.size());
        ZWaveUserCodeValueEvent event = (ZWaveUserCodeValueEvent) events.get(0);
        assertEquals("", event.getCode());
        assertEquals(3, event.getId());
        assertEquals(UserIdStatusType.AVAILABLE, event.getStatus());
    }

    @Test
    public void setCodeAscii() {
        ZWaveUserCodeCommandClass cls = (ZWaveUserCodeCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_USER_CODE);

        byte[] expectedResponse = { 99, 1, 1, 1, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 };

        byte[] msg = cls.setUserCode(1, "0123456789").getPayloadBuffer();
        assertTrue(Arrays.equals(msg, expectedResponse));
    }

    @Test
    public void setCodeHex() {
        ZWaveUserCodeCommandClass cls = (ZWaveUserCodeCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_USER_CODE);

        byte[] expectedResponse = { 99, 1, 1, 1, 0, 17, 34, 51, 68, 85, 102 };

        byte msg[] = cls.setUserCode(1, "00 11 22 33 44 55 66").getPayloadBuffer();
        assertTrue(Arrays.equals(msg, expectedResponse));
    }

    @Test
    public void processUserCodeReportIdUnsigned() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x10, 0x1C, 0x0E, 0x63, 0x03, (byte) 0x8C, 0x01, (byte) 0x30,
                (byte) 0x31, (byte) 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x01 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(1, events.size());
        ZWaveUserCodeValueEvent event = (ZWaveUserCodeValueEvent) events.get(0);
        assertEquals("0123456789", event.getCode());
        assertEquals(140, event.getId());
        assertEquals(UserIdStatusType.OCCUPIED, event.getStatus());
    }

}
