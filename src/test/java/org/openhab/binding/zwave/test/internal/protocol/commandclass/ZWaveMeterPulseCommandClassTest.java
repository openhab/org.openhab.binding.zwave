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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterPulseCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveMeterPulseCommandClass}.
 *
 * @author Chris Jackson
 */
public class ZWaveMeterPulseCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void reportValue() {
        byte[] packetData = { 0x01, 0x0F, 0x00, 0x04, 0x00, 0x01, 0x07, (byte) 0x35, 0x05, 0x00, 0x01, 0x00, 0x00,
                (byte) 0xC3 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);
        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_METER_PULSE);
        assertEquals(event.getEndpoint(), 0);
        assertEquals((int) event.getValue(), 65536);
    }

    @Test
    public void getValueMessage() {
        ZWaveMeterPulseCommandClass cls = (ZWaveMeterPulseCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_METER_PULSE);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 53, 4 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
