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
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;

/**
 * Test cases for RemoveNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class RemoveNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponseStart = { 1 };
        byte[] expectedResponseStop = { 5 };
        byte[] expectedResponseStopComplete = { 5 };

        RemoveNodeMessageClass handler = new RemoveNodeMessageClass();
        ZWaveSerialPayload msg;

        msg = handler.doRequestStart();
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.RemoveNodeFromNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStart));
        assertTrue(msg.getSerialMessage().getCallbackId() != 0);

        msg = handler.doRequestStop(false);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.RemoveNodeFromNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStop));
        assertTrue(msg.getSerialMessage().getCallbackId() != 0);

        msg = handler.doRequestStop(true);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.RemoveNodeFromNetwork);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseStopComplete));
        assertTrue(msg.getSerialMessage().getCallbackId() == 0);

    }
}
