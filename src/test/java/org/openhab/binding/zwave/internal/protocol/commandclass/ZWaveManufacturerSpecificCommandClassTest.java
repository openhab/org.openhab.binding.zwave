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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveManufacturerSpecificCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

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
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 114, 4 };
        cls.setVersion(1);
        msg = cls.getManufacturerSpecificMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getManufacturerSpecificDeviceMessage() {
        ZWaveManufacturerSpecificCommandClass cls = (ZWaveManufacturerSpecificCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MANUFACTURER_SPECIFIC);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseSerialNumber = { 114, 6, 1 };
        cls.setVersion(1);
        msg = cls.getManufacturerSpecificDeviceMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseSerialNumber));
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
