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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.serialmessage.DeleteSucReturnRouteMessageClass;

/**
 * Test cases for DeleteSucReturnRouteMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class DeleteSucReturnRouteMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 12 };

        DeleteSucReturnRouteMessageClass handler = new DeleteSucReturnRouteMessageClass();
        ZWaveSerialPayload msg = handler.doRequest(12);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }
}
