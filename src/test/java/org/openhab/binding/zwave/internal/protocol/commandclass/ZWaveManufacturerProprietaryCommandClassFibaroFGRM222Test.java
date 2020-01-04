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

import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ReportType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ZWaveAlarmValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBasicCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.impl.CommandClassManufacturerProprietaryFibaroFgrm222V1;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * Test cases for {@link ZWaveBasicCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveManufacturerProprietaryCommandClassFibaroFGRM222Test extends ZWaveCommandClassTest {

    @Ignore
    @Test
    public void getValueMessage() {
        byte[] payload = CommandClassManufacturerProprietaryFibaroFgrm222V1.getFgrm222Get();

        byte[] expectedResponse = { -111, 2 };
        assertTrue(Arrays.equals(payload, expectedResponse));
    }

    @Ignore
    @Test
    public void getShutterMessage() {
        byte[] payload = CommandClassManufacturerProprietaryFibaroFgrm222V1.getFgrm222Set("SHUTTER", 0, 0);

        byte[] expectedResponse = { (byte) 0x91, 0x01, 0x0F, 0x26, 0x01, 0x01, 0x00, 0x63, 0x25, (byte) 0xB5 };
        assertTrue(Arrays.equals(payload, expectedResponse));
    }

    @Ignore
    @Test
    public void getLamellaMessage() {
        byte[] payload = CommandClassManufacturerProprietaryFibaroFgrm222V1.getFgrm222Set("LAMELLA", 0, 0);

        byte[] expectedResponse = { (byte) 0x91, 0x01, 0x0F, 0x26, 0x01, 0x01, 0x00, 0x63, 0x25, (byte) 0xB5 };
        assertTrue(Arrays.equals(payload, expectedResponse));
    }

    @Ignore
    @Test
    public void Notification_Burglar_Motion() {
        byte[] packetData = { 0x01, 0x0F, 0x00, 0x13, 0x71, 0x08, (byte) 0x91, 0x01, 0x0F, 0x26, 0x01, 0x01, 0x00, 0x01,
                0x25, (byte) 0xB3, (byte) 0xB4 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 3);

        assertEquals(events.size(), 1);

        ZWaveAlarmValueEvent event = (ZWaveAlarmValueEvent) events.get(0);

        // assertEquals(event.getNodeId(), 40);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_ALARM);
        assertEquals(event.getReportType(), ReportType.NOTIFICATION);
        assertEquals(event.getAlarmType(), ZWaveAlarmCommandClass.AlarmType.BURGLAR);
        assertEquals(event.getAlarmEvent(), 8);
        assertEquals(event.getAlarmStatus(), 0xFF);
    }
}
