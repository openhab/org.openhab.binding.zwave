/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNetworkUpdateMessageClass;

public class RequestNetworkUpdateMessageClassTest {
    @Test
    public void doRequest() {
        RequestNetworkUpdateMessageClass handler = new RequestNetworkUpdateMessageClass();
        ZWaveSerialPayload msg = handler.doRequest();
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.RequestNetworkUpdate);
        assertNull(msg.getPayloadBuffer());
    }
}
