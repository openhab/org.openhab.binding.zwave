/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;

public class RequestNetworkUpdateMessageClassTest {
    @Test
    public void doRequest() {
        RequestNetworkUpdateMessageClass handler = new RequestNetworkUpdateMessageClass();
        ZWaveSerialPayload msg = handler.doRequest();
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.RequestNetworkUpdate);
        assertNull(msg.getPayloadBuffer());
    }
}
