package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveConfigurationParameter;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiCommandCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Tests for {@link ZWaveMultiCommandCommandClass}
 *
 * @author Chris Jackson - initial contribution
 *
 */
public class ZWaveMultiCommandCommandClassTest extends ZWaveCommandClassTest {
    @Test
    public void ProcessMessage() {
        byte[] packetData = { 0x01, 0x13, 0x00, 0x04, 0x00, 0x07, 0x07, (byte) 0x8f, 0x01, 0x02, 0x05, 0x70, 0x06, 0x01,
                0x01, 0x01, 0x05, 0x70, 0x06, 0x02, 0x01, 0x02, 100 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);
        assertEquals(events.size(), 2);
        ZWaveCommandClassValueEvent event1 = (ZWaveCommandClassValueEvent) events.get(0);
        assertEquals(event1.getCommandClass(), CommandClass.CONFIGURATION);
        assertEquals(event1.getEndpoint(), 0);
        ZWaveConfigurationParameter parameter1 = (ZWaveConfigurationParameter) event1.getValue();
        assertEquals(1, (int) parameter1.getIndex());
        assertEquals(1, (int) parameter1.getSize());
        assertEquals(1, (int) parameter1.getValue());
        ZWaveCommandClassValueEvent event2 = (ZWaveCommandClassValueEvent) events.get(1);
        assertEquals(event2.getCommandClass(), CommandClass.CONFIGURATION);
        assertEquals(event2.getEndpoint(), 0);
        ZWaveConfigurationParameter parameter2 = (ZWaveConfigurationParameter) event2.getValue();
        assertEquals(2, (int) parameter2.getIndex());
        assertEquals(1, (int) parameter2.getSize());
        assertEquals(2, (int) parameter2.getValue());
    }
}
