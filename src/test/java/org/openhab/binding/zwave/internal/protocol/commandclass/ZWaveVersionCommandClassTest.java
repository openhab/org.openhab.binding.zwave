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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveVersionCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveVersionCommandClass}.
 *
 * @author Chris Jackson - Initial version
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
}
