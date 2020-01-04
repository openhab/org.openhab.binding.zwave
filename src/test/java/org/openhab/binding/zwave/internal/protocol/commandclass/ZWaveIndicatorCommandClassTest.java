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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveIndicatorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveIndicatorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveIndicatorCommandClass cls = (ZWaveIndicatorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_INDICATOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -121, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveIndicatorCommandClass cls = (ZWaveIndicatorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_INDICATOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -121, 1, 34 };
        cls.setVersion(1);
        msg = cls.setValueMessage(34);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getSupportedIndicators() {
        ZWaveIndicatorCommandClass cls = (ZWaveIndicatorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_INDICATOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -121, 4, 0 };
        cls.setVersion(1);
        msg = cls.getSupportedIndicators(null);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void Notification_Burglar_Motion() {
        byte[] packetData = { 0x01, 0x0C, 0x00, 0x04, 0x00, 0x11, 0x06, (byte) 0x87, 0x05, 0x43, 0x44, 0x01, 0x07,
                0x63 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData, 2);

    }
}
