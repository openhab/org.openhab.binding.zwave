/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveVersionCommandClass;

/**
 * Test cases for {@link ZWaveVersionCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveVersionCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getVersionMessage() {
        ZWaveVersionCommandClass cls = (ZWaveVersionCommandClass) getCommandClass(CommandClass.VERSION);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, -122, 17, 0, 0, 19 };
        cls.setVersion(1);
        msg = cls.getVersionMessage();
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveVersionCommandClass cls = (ZWaveVersionCommandClass) getCommandClass(CommandClass.VERSION);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, -122, 19, 113, 0, 0, 98 };
        cls.setVersion(1);
        msg = cls.getCommandClassVersionMessage(CommandClass.ALARM);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void processApplicationVersionReport() {
        byte[] packetData = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x08, 0x07, (byte) 0x86, 0x12, 0x03, 0x03, 0x14, 0x01, 0x04,
                (byte) 0x7C };

        ZWaveVersionCommandClass cls = (ZWaveVersionCommandClass) getCommandClass(CommandClass.VERSION);
        SerialMessage msg = new SerialMessage(packetData);
        try {
            cls.handleApplicationCommandRequest(msg, 4, 0);
            assertEquals("1.4", cls.getApplicationVersion());
        } catch (ZWaveSerialMessageException e) {
        }
    }
}
