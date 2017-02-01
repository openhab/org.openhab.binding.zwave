/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveUserCodeCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveUserCodeCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveUserCodeCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void processUserCodeReport() {
        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x10, 0x1C, 0x0E, 0x63, 0x03, 0x00, 0x00, (byte) 0x8F,
                (byte) 0xD8, (byte) 0xC3, 0x0D, 0x01, 0x20, 0x02, (byte) 0x80, 0x00, 0x00, (byte) 0xB7 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);
    }
}
