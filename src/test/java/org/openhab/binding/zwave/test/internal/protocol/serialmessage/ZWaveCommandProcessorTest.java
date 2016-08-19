/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.serialmessage;

import static org.junit.Assert.assertEquals;

import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveCommandProcessor;

public class ZWaveCommandProcessorTest {

    void ProcessResponse(ZWaveCommandProcessor handler, byte[] packetData) {
        SerialMessage msg = new SerialMessage(packetData);
        assertEquals(true, msg.isValid);
        assertEquals(SerialMessageType.Response, msg.getMessageType());

        // Mock the controller so we can get any events
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        // argument = ArgumentCaptor.forClass(ZWaveEvent.class);
        // Mockito.doNothing().when(controller).notifyEventListeners(argument.capture());

        ZWaveTransaction transaction = new ZWaveTransactionBuilder(msg).build();

        try {
            handler.handleResponse(controller, transaction, msg);
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
