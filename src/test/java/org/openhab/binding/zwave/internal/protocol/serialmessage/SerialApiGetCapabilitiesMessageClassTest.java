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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;

/**
 * Test cases for EnableSUCMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class SerialApiGetCapabilitiesMessageClassTest {
    @Test
    public void doRequest() {
        byte[] expectedResponse = { 1, 3, 0, 7, -5 };

        SerialMessage msg;
        SerialApiGetCapabilitiesMessageClass handler = new SerialApiGetCapabilitiesMessageClass();

        msg = handler.doRequest().getSerialMessage();
        msg.setCallbackId(1);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }
}
