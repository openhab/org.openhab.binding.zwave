/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveClockCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * Test cases for {@link ZWaveClockCommandClass}.
 *
 * @author Jorg de Jong
 * @author Chris Jackson
 */
public class ZWaveClockCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void reportTime() {
        byte[] packetData = { 0x01, 0x0F, 0x00, 0x04, 0x00, 0x07, 0x06, (byte) 0x81, 0x06, -127, 4, 127, 0, -120 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_CLOCK);
        assertEquals(event.getEndpoint(), 0);
        Date date = (Date) event.getValue();
        assertNotNull(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        assertEquals(1, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(4, cal.get(Calendar.MINUTE));
        assertEquals(5, cal.get(Calendar.DAY_OF_WEEK));
    }

    @Test
    public void setTime() {
        ZWaveClockCommandClass cls = (ZWaveClockCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_CLOCK);

        byte[] expectedResponse = { 99, 4, -127, 4, -128, 0 };

        Calendar utc = Calendar.getInstance();
        utc.setTimeZone(TimeZone.getTimeZone("UTC"));
        utc.setTime(new Date(0));
        SerialMessage msg = cls.getSetMessage(utc).getSerialMessage();

        assertArrayEquals(expectedResponse, msg.getMessagePayload());
    }

    @Test
    public void getReportMessage() {
        ZWaveClockCommandClass cls = (ZWaveClockCommandClass) getCommandClass(CommandClass.COMMAND_CLASS_CLOCK);

        byte[] expectedResponse = {99, 4, -127, 6, -128, 0};

        Calendar utc = Calendar.getInstance();
        utc.setTimeZone(TimeZone.getTimeZone("UTC"));
        utc.setTime(new Date(0));
        SerialMessage msg = cls.getReportMessage(utc).getSerialMessage();

        assertArrayEquals(expectedResponse, msg.getMessagePayload());
    }
}
