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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveVersionCommandClass}.
 *
 * @author Chris Jackson - Initial contribution
 */
public class ZWaveVersionCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getVersionMessage() {
        ZWaveVersionCommandClass cls = (ZWaveVersionCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_VERSION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -122, 17 };
        cls.setVersion(1);
        msg = cls.getVersionMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getCommandClassVersionMessage() {
        ZWaveVersionCommandClass cls = (ZWaveVersionCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_VERSION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -122, 19, 113 };
        cls.setVersion(1);
        msg = cls.getCommandClassVersionMessage(CommandClass.COMMAND_CLASS_ALARM);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));

        byte[] expectedVersionClassResponse = { -122, 19, -122 };
        msg = cls.getCommandClassVersionMessage(CommandClass.COMMAND_CLASS_VERSION);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedVersionClassResponse));
    }

    @Test
    public void processApplicationVersionReport() {
        byte[] packetData = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x08, 0x07, (byte) 0x86, 0x12, 0x03, 0x03, 0x14, 0x01, 0x04,
                (byte) 0x7C };

        ZWaveVersionCommandClass cls = (ZWaveVersionCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_VERSION);
        SerialMessage msg = new SerialMessage(packetData);
        try {
            ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(msg);
            cls.handleApplicationCommandRequest(payload, 0);
            assertEquals("1.4", cls.getApplicationVersion());
        } catch (ZWaveSerialMessageException e) {
        }
    }

    @Test
    public void processVersionCapabilitiesReportWithBitmask() {
        ZWaveVersionCommandClass cls = (ZWaveVersionCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_VERSION);

        try {
            Field versionMaxField = ZWaveCommandClass.class.getDeclaredField("versionMax");
            versionMaxField.setAccessible(true);
            versionMaxField.setInt(cls, 0);
            cls.initialise(mockedNode, mockedController, null);

            ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(new byte[] { (byte) 0x86, 0x16, 0x0F });
            cls.handleVersionCapabilitiesReport(payload, 0);

            Field supportsZWaveSoftwareVersionField = ZWaveVersionCommandClass.class
                    .getDeclaredField("supportsZWaveSoftwareVersion");
            supportsZWaveSoftwareVersionField.setAccessible(true);
            assertTrue((boolean) supportsZWaveSoftwareVersionField.get(cls));
            assertEquals(3, cls.getVersion());
        } catch (ReflectiveOperationException e) {
            fail(e);
        }
    }

    @Test
    public void processVersionZWaveSoftwareVersionReport() {
        ZWaveVersionCommandClass cls = (ZWaveVersionCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_VERSION);

        // getApplicationVersion() only trusts applicationVersionLong when its major.minor matches
        // the VERSION_REPORT applicationVersion, so that must be populated first.
        ZWaveCommandClassPayload versionPayload = new ZWaveCommandClassPayload(
                new byte[] { (byte) 0x86, 0x12, 0x03, 0x03, 0x14, 0x0A, 0x00 });
        cls.handleVersionReport(versionPayload, 0);

        ZWaveCommandClassPayload capabilitiesPayload = new ZWaveCommandClassPayload(
                new byte[] { (byte) 0x86, 0x16, 0x07 });
        cls.handleVersionCapabilitiesReport(capabilitiesPayload, 0);

        ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(
                new byte[] { (byte) 0x86, 0x18, 0x07, 0x0D, 0x09, 0x0A, 0x0D, 0x09, 0x01, (byte) 0x9E, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x07, 0x0D, 0x09, 0x01, (byte) 0x9E, 0x0A, 0x00, 0x01, (byte) 0xAA, (byte) 0xBB });
        cls.handleVersionZWaveSoftwareReport(payload, 0);
        assertEquals("10.0.1", cls.getApplicationVersion());
    }
}
