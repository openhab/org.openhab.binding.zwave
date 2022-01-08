/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;

/**
 * Test cases for ReplaceFailedNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class ReplaceFailedNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 12 };

        ReplaceFailedNodeMessageClass handler = new ReplaceFailedNodeMessageClass();
        ZWaveSerialPayload msg = handler.doRequest(12);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.ReplaceFailedNode);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }
}
