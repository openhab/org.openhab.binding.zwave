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
        byte[] packetData = { 0x01, 0x13, 0x00, 0x04, 0x00, 0x07, 0x0F, (byte) 0x8f, 0x01, 0x02, 0x05, 0x70, 0x06, 0x01,
                0x01, 0x01, 0x05, 0x70, 0x06, 0x02, 0x01, 0x02, 108 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);
        assertEquals(events.size(), 2);
        ZWaveCommandClassValueEvent event1 = (ZWaveCommandClassValueEvent) events.get(0);
        assertEquals(event1.getCommandClass(), CommandClass.COMMAND_CLASS_CONFIGURATION);
        assertEquals(event1.getEndpoint(), 0);
        ZWaveConfigurationParameter parameter1 = (ZWaveConfigurationParameter) event1.getValue();
        assertEquals(1, (int) parameter1.getIndex());
        assertEquals(1, (int) parameter1.getSize());
        assertEquals(1, (int) parameter1.getValue());
        ZWaveCommandClassValueEvent event2 = (ZWaveCommandClassValueEvent) events.get(1);
        assertEquals(event2.getCommandClass(), CommandClass.COMMAND_CLASS_CONFIGURATION);
        assertEquals(event2.getEndpoint(), 0);
        ZWaveConfigurationParameter parameter2 = (ZWaveConfigurationParameter) event2.getValue();
        assertEquals(2, (int) parameter2.getIndex());
        assertEquals(1, (int) parameter2.getSize());
        assertEquals(2, (int) parameter2.getValue());
    }
}
