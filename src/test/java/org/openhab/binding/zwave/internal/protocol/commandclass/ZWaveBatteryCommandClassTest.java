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

        byte[] expectedResponseV1 = { (byte) 0x80, 0x02 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void handleReport() {
        byte[] packetData = { 0x01, 0x09, 0x00, 0x04, 0x00, 0x05, 0x03, (byte) 0x80, 0x03, 0x6C, 0x1B };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(CommandClass.COMMAND_CLASS_BATTERY, event.getCommandClass());
        assertEquals(108, event.getValue());
    }

}
