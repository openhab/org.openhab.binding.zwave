/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveTimeParametersCommandClass}.
 *
 * @author Jorg de Jong
 */
public class ZWaveTimeParametersCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void reportTimeOffset() {
        byte[] packetData = { 0x01, 0x0F, 0x00, 0x04, 0x00, 0x07, 0x09, (byte) 0x8B, 0x03, 0x07, (byte) 0xE0, 0x04,
                0x17, 0x0B, 0x29, 0x18, (byte) 0xBC };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_TIME_PARAMETERS);
        assertEquals(event.getEndpoint(), 0);
        Date date = (Date) event.getValue();
        assertNotNull(date);
        // reported time is in the past.
        assertEquals(true, date.before(new Date()));
    }

    @Test
    public void setTime() {
        ZWaveTimeParametersCommandClass cls = (ZWaveTimeParametersCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_TIME_PARAMETERS);

        byte[] expectedResponse = { 99, 9, -117, 1, 7, -78, 1, 1, 0, 0, 0 };

        Calendar utc = Calendar.getInstance();
        utc.setTimeZone(TimeZone.getTimeZone("UTC"));
        utc.setTime(new Date(0));
        SerialMessage msg = cls.getSetMessage(utc).getSerialMessage();

        assertTrue(Arrays.equals(msg.getMessagePayload(), expectedResponse));
    }
}
