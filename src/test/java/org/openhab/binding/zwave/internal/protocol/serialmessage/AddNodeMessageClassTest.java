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
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;

/**
 * Test cases for AddNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class AddNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponseStartLocal = { 1, };
        byte[] expectedResponseStartHigh = { -127 };
        byte[] expectedResponseStartNetwork = { -63 };
        byte[] expectedResponseStop = { 5 };
        byte[] expectedResponseStopComplete = { 5 };

        ZWaveSerialPayload msg;
        AddNodeMessageClass handler = new AddNodeMessageClass();

        msg = handler.doRequestStart(false, false);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStartLocal));
        assertTrue(msg.getSerialMessage().getCallbackId() != 0);

        msg = handler.doRequestStart(true, false);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStartHigh));
        assertTrue(msg.getSerialMessage().getCallbackId() != 0);

        msg = handler.doRequestStart(true, true);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStartNetwork));
        assertTrue(msg.getSerialMessage().getCallbackId() != 0);

        msg = handler.doRequestStop(false);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStop));
        assertTrue(msg.getSerialMessage().getCallbackId() != 0);

        msg = handler.doRequestStop(true);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.AddNodeToNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStopComplete));
        assertTrue(msg.getSerialMessage().getCallbackId() == 0);
    }

    @Test
    public void rxNodeAdd() {
        SerialMessage incomingMessage = new SerialMessage(
                new byte[] { 0x01, 0x07, 0x00, 0x4A, 0x01, 0x06, 0x11, 0x00, (byte) 0xA4 });

        System.out.println(incomingMessage);

        ZWaveSerialPayload payload = new AddNodeMessageClass().doRequestStop(false);
        payload.setCallbackId(1);
        ZWaveTransaction transaction = new ZWaveTransaction(payload);
        AddNodeMessageClass response = new AddNodeMessageClass();
        try {
            response.handleRequest(Mockito.mock(ZWaveController.class), transaction, incomingMessage);
        } catch (ZWaveSerialMessageException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

}
