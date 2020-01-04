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

import static org.junit.Assert.assertEquals;

import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveMessagePayloadTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveCommandProcessor;

public class ZWaveCommandProcessorTest {

    void ProcessResponse(ZWaveCommandProcessor handler, byte[] packetData) {
        SerialMessage msg = new SerialMessage(packetData);
        ZWaveMessagePayloadTransaction payload = new ZWaveSerialPayload(msg.getMessageBuffer());
        assertEquals(true, msg.isValid);
        assertEquals(SerialMessageType.Response, msg.getMessageType());

        // Mock the controller so we can get any events
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        // argument = ArgumentCaptor.forClass(ZWaveEvent.class);
        // Mockito.doNothing().when(controller).notifyEventListeners(argument.capture());

        ZWaveTransaction transaction = new ZWaveTransaction(payload);

        try {
            handler.handleResponse(controller, transaction, msg);
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void ProcessRequest(ZWaveCommandProcessor handler, ZWaveTransaction transaction, byte[] packetData) {
        SerialMessage msg = new SerialMessage(packetData);
        assertEquals(true, msg.isValid);
        assertEquals(SerialMessageType.Request, msg.getMessageType());

        // Mock the controller so we can get any events
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        // argument = ArgumentCaptor.forClass(ZWaveEvent.class);
        // Mockito.doNothing().when(controller).notifyEventListeners(argument.capture());

        try {
            handler.handleRequest(controller, transaction, msg);
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
