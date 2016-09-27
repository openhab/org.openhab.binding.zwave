/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
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

}
