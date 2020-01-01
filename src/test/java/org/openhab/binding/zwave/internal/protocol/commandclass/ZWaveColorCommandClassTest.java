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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveColorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveColorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getCapabilityMessage() {
        ZWaveColorCommandClass cls = (ZWaveColorCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_SWITCH_COLOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 51, 1 };
        cls.setVersion(1);
        msg = cls.getCapabilityMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getValueMessage() {
        ZWaveColorCommandClass cls = (ZWaveColorCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_SWITCH_COLOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 51, 3, 1 };
        cls.setVersion(1);
        msg = cls.getValueMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveColorCommandClass cls = (ZWaveColorCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_SWITCH_COLOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 51, 5, 1, 3, 80 };
        cls.setVersion(1);
        msg = cls.setValueMessage(3, 80);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void processCapabilitySupportedReport() {
        byte[] packetData = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x0D, 0x04, 0x33, 0x02, 0x1C, 0x00, (byte) 0xD5 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);
        ZWaveColorValueEvent event = (ZWaveColorValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_SWITCH_COLOR);
        assertTrue(event.getColorMap().containsKey(ZWaveColorType.RED));
        assertTrue(event.getColorMap().containsKey(ZWaveColorType.GREEN));
        assertTrue(event.getColorMap().containsKey(ZWaveColorType.BLUE));
        assertFalse(event.getColorMap().containsKey(ZWaveColorType.AMBER));
        assertFalse(event.getColorMap().containsKey(ZWaveColorType.COLD_WHITE));
        assertFalse(event.getColorMap().containsKey(ZWaveColorType.WARM_WHITE));
        assertFalse(event.getColorMap().containsKey(ZWaveColorType.PURPLE));
        assertFalse(event.getColorMap().containsKey(ZWaveColorType.INDEX));

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
    }
}
