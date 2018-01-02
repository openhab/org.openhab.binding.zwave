/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
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
import org.openhab.binding.zwave.internal.protocol.serialmessage.ControllerSetDefaultMessageClass;

/**
 * Test cases for ControllerSetDefaultMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class ControllerSetDefaultMessageClassTest {
    @Test
    public void doRequest() {
        ZWaveSerialPayload msg;
        ControllerSetDefaultMessageClass handler = new ControllerSetDefaultMessageClass();

        msg = handler.doRequest();
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.SetDefault);
        assertNull(msg.getPayloadBuffer());
    }
}
