/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.openhab.binding.zwave.internal.protocol.serialmessage.RemoveFailedNodeMessageClass;

/**
 * Test cases for RemoveFailedNodeMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class RemoveFailedNodeMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 12, 1 };

        RemoveFailedNodeMessageClass handler = new RemoveFailedNodeMessageClass();
        ZWaveSerialPayload msg = handler.doRequest(12);
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.RemoveFailedNodeID);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }
}
