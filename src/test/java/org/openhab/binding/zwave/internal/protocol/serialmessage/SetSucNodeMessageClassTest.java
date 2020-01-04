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
 * Test cases for SetSucNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class SetSucNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponseNone = { 12, 1, 0, 1 };
        byte[] expectedResponseBasic = { 12, 0, 0, 0 };

        ZWaveSerialPayload msg;
        SetSucNodeMessageClass handler = new SetSucNodeMessageClass();

        msg = handler.doRequest(12, true);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.SetSucNodeID);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseNone));

        msg = handler.doRequest(12, false);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.SetSucNodeID);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseBasic));
    }
}
