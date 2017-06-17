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
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBatteryCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveBatteryCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveBatteryCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveBatteryCommandClass cls = (ZWaveBatteryCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_BATTERY);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -128, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void handleReport() {
        byte[] packetData = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x49, 0x04, 0x71, 0x05, 0x00, 0x00, (byte) 0xC8 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(CommandClass.COMMAND_CLASS_BATTERY, event.getCommandClass());
        assertEquals(0x00, event.getValue());
    }

}
