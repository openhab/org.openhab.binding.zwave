package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Tests for {@link ZWaveCrc16EncapsulationCommandClass}
 *
 * @author Chris Jackson - initial contribution
 *
 */
public class ZWaveCrc16EncapsulationCommandClassTest extends ZWaveCommandClassTest {
    @Test
    public void ProcessMessage() {
        byte[] packetData = { 0x01, 0x0d, 0x00, 0x04, 0x00, 0x07, 0x07, 0x56, 0x01, 0x20, 0x03, 0x00, (byte) 0x8c, 0x58,
                0x56 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);
        assertEquals(events.size(), 1);
        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);
        assertEquals(event.getCommandClass(), CommandClass.BASIC);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getValue(), 0);
    }
}
