package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

public class ZWaveEndpointTest {
    @Test
    public void testSupportedCommandClasses() {
        ZWaveEndpoint endpoint = new ZWaveEndpoint(0);

        endpoint.addCommandClass(new ZWaveAlarmCommandClass(null, null, null));

        assertTrue(endpoint.supportsCommandClass(CommandClass.COMMAND_CLASS_ALARM));
        assertFalse(endpoint.supportsCommandClass(CommandClass.COMMAND_CLASS_ASSOCIATION));
    }
}
