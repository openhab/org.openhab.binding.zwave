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

import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.LocalProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.RfProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.Type;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveProtectionCommandClass}.
 *
 * @author Jorg de Jong
 * @author Chris Jackson
 */
public class ZWaveProtectionCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void reportProtection() {
        byte[] packetData = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x0A, 0x04, 0x75, 0x03, 0x00, 0x00, (byte) 0x89 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 2);

        assertEquals(events.size(), 2);

        ZWaveCommandClassValueEvent localEvent = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(localEvent.getCommandClass(), CommandClass.COMMAND_CLASS_PROTECTION);
        assertEquals(localEvent.getEndpoint(), 0);
        assertEquals(localEvent.getType(), Type.PROTECTION_LOCAL);
        assertEquals(localEvent.getValue(), LocalProtectionType.UNPROTECTED);

        ZWaveCommandClassValueEvent rfEvent = (ZWaveCommandClassValueEvent) events.get(1);

        assertEquals(rfEvent.getCommandClass(), CommandClass.COMMAND_CLASS_PROTECTION);
        assertEquals(rfEvent.getEndpoint(), 0);
        assertEquals(rfEvent.getType(), Type.PROTECTION_RF);
        assertEquals(rfEvent.getValue(), RfProtectionType.UNPROTECTED);
    }

    @Ignore
    @Test
    public void reportProtectionSupported() {
        byte[] packetData = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x0A, 0x04, 0x75, 0x05, 0x00, 0x05, 0x00, 0x03, 0x00,
                (byte) 0x8E };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 2);

        assertEquals(events.size(), 2);

        ZWaveCommandClassValueEvent localEvent = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(localEvent.getCommandClass(), CommandClass.COMMAND_CLASS_PROTECTION);
        assertEquals(localEvent.getEndpoint(), 0);
        assertEquals(localEvent.getType(), Type.PROTECTION_LOCAL);
        assertEquals(localEvent.getValue(), LocalProtectionType.UNPROTECTED);

        ZWaveCommandClassValueEvent rfEvent = (ZWaveCommandClassValueEvent) events.get(1);

        assertEquals(rfEvent.getCommandClass(), CommandClass.COMMAND_CLASS_PROTECTION);
        assertEquals(rfEvent.getEndpoint(), 0);
        assertEquals(rfEvent.getType(), Type.PROTECTION_RF);
        assertEquals(rfEvent.getValue(), RfProtectionType.UNPROTECTED);
    }

    @Test
    public void setProtectionV1() {
        ZWaveProtectionCommandClass cls = (ZWaveProtectionCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_PROTECTION);
        cls.setVersion(1);

        byte[] expectedResponse = { 99, 3, 117, 1, 1 };
        SerialMessage msg = cls.setValueMessage(LocalProtectionType.SEQUENCE, RfProtectionType.NORFRESPONSE)
                .getSerialMessage();

        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse));
    }

    @Test
    public void setProtectionV2() {
        ZWaveProtectionCommandClass cls = (ZWaveProtectionCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_PROTECTION);
        cls.setVersion(2);

        byte[] expectedResponse = { 99, 4, 117, 1, 1, 2 };
        SerialMessage msg = cls.setValueMessage(LocalProtectionType.SEQUENCE, RfProtectionType.NORFRESPONSE)
                .getSerialMessage();

        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse));
    }

    @Test
    public void setLocalProtectionV2_withRfDefault() {
        ZWaveProtectionCommandClass cls = (ZWaveProtectionCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_PROTECTION);
        cls.setVersion(2);
        setDefaultProtectionTypes(cls, LocalProtectionType.UNPROTECTED, RfProtectionType.NORFCONTROL);

        byte[] expectedResponse = { 99, 4, 117, 1, 1, 1 };
        SerialMessage msg = cls.setValueMessage(LocalProtectionType.SEQUENCE, null).getSerialMessage();

        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse));
    }

    @Test
    public void setRfProtectionV2_withLocalDefault() {
        ZWaveProtectionCommandClass cls = (ZWaveProtectionCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_PROTECTION);
        cls.setVersion(2);
        setDefaultProtectionTypes(cls, LocalProtectionType.PROTECTED, RfProtectionType.UNPROTECTED);

        byte[] expectedResponse = { 99, 4, 117, 1, 2, 1 };
        SerialMessage msg = cls.setValueMessage(null, RfProtectionType.NORFCONTROL).getSerialMessage();

        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse));
    }

    private void setDefaultProtectionTypes(ZWaveProtectionCommandClass cls, LocalProtectionType localType,
            RfProtectionType rfType) {
        ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(
                new byte[] { 75, 3, (byte) localType.ordinal(), (byte) rfType.ordinal() });
        cls.handleProtectionReport(payload, 1);
    }

}
