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
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveColorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveColorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getCapabilityMessage() {
        ZWaveColorCommandClass cls = (ZWaveColorCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_SWITCH_COLOR);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 9, 0, 19, 99, 2, 51, 1, 0, 0, -74 };
        cls.setVersion(1);
        msg = cls.getCapabilityMessage().getSerialMessage();
        msg.setCallbackId(0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void getValueMessage() {
        ZWaveColorCommandClass cls = (ZWaveColorCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_SWITCH_COLOR);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 10, 0, 19, 99, 3, 51, 3, 1, 0, 0, -73 };
        cls.setVersion(1);
        msg = cls.getValueMessage(1).getSerialMessage();
        msg.setCallbackId(0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveColorCommandClass cls = (ZWaveColorCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_SWITCH_COLOR);
        SerialMessage msg;

        byte[] expectedResponseV1 = { 1, 12, 0, 19, 99, 5, 51, 5, 1, 3, 80, 0, 0, -30 };
        cls.setVersion(1);
        msg = cls.setValueMessage(3, 80).getSerialMessage();
        msg.setCallbackId(0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponseV1));
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
