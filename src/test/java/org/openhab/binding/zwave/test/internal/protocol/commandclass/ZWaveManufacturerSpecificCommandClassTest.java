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
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveManufacturerSpecificCommandClass;

/**
 * Test cases for {@link ZWaveManufacturerSpecificCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveManufacturerSpecificCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveManufacturerSpecificCommandClass cls = (ZWaveManufacturerSpecificCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MANUFACTURER_SPECIFIC);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 114, 4, 0, 0, -14 };
        cls.setVersion(1);
        msg = cls.getManufacturerSpecificMessage().getSerialMessage();
        msg.setCallbackId(0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getManufacturerSpecificDeviceMessage() {
        ZWaveManufacturerSpecificCommandClass cls = (ZWaveManufacturerSpecificCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MANUFACTURER_SPECIFIC);
        SerialMessage msg;

        byte[] expectedResponseSerialNumber = { 1, 10, 0, 19, 99, 3, 114, 6, 1, 0, 0, -13 };
        cls.setVersion(1);
        msg = cls.getManufacturerSpecificDeviceMessage(1).getSerialMessage();
        msg.setCallbackId(0);

        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseSerialNumber));
    }

    @Test
    public void processManufacturerReport() {
        byte[] packetData = { 0x01, 0x0E, 0x00, 0x04, 0x00, 0x08, 0x08, 0x72, 0x05, 0x01, 0x0F, 0x02, 0x00, 0x01, 0x04,
                (byte) 0x8B };

        ZWaveManufacturerSpecificCommandClass cls = (ZWaveManufacturerSpecificCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MANUFACTURER_SPECIFIC);
        SerialMessage msg = new SerialMessage(packetData);
        try {
            ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(msg);
            cls.handleApplicationCommandRequest(payload, 0);
            assertEquals(0x010f, cls.getDeviceManufacturer());
            assertEquals(0x0200, cls.getDeviceType());
            assertEquals(0x0104, cls.getDeviceId());
        } catch (ZWaveSerialMessageException e) {
        }
    }

}
