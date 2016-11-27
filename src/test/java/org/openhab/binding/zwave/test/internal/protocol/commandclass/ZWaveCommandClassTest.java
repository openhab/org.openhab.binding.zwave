/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * This class provides methods to allow test for standard command classes to be run simply.
 * It provides the mocked framework to allow command classes to be processed and receives the notification events.
 * Command classes pass in the packet data, and receive the command classes notification event(s) in return which should
 * then be checked for the correct output.
 *
 * @author Chris Jackson
 *
 */
public class ZWaveCommandClassTest {
    protected ArgumentCaptor<ZWaveEvent> argument;
    protected ZWaveController mockedController;
    protected ZWaveNode mockedNode;
    protected ArgumentCaptor<ZWaveCommandClass> commandClsCaptor;
    protected Map<Integer, ZWaveAssociationGroup> associationList;

    /**
     * Helper class to create everything we need to test a command class message.
     *
     * We pass in the data, and the expected command class. This method creates the class, checks it's the right one,
     * processes the message and gets the response events.
     *
     * It expects at least one response event.
     *
     * @param packetData byte array containing the full Z-Wave frame
     * @param version commandclass version
     * @return List of ZWaveEvent(s)
     */
    protected List<ZWaveEvent> processCommandClassMessage(byte[] packetData, int version) {
        SerialMessage msg = new SerialMessage(packetData);

        // Check the packet is not corrupted and is a command class request
        assertEquals(true, msg.isValid);
        assertEquals(SerialMessageType.Request, msg.getMessageType());
        assertEquals(SerialMessageClass.ApplicationCommandHandler, msg.getMessageClass());

        // Mock the controller so we can get any events
        mockedController = Mockito.mock(ZWaveController.class);
        argument = ArgumentCaptor.forClass(ZWaveEvent.class);
        Mockito.doNothing().when(mockedController).notifyEventListeners(argument.capture());
        mockedNode = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);

        associationList = new HashMap<Integer, ZWaveAssociationGroup>();
        for (int c = 1; c <= 10; c++) {
            associationList.put(c, new ZWaveAssociationGroup(c));
        }

        Mockito.when(mockedNode.getAssociationGroup(Matchers.any(Integer.class)))
                .thenAnswer(new Answer<ZWaveAssociationGroup>() {
                    @Override
                    public ZWaveAssociationGroup answer(InvocationOnMock invocation) {
                        return associationList.get(invocation.getArguments()[0]);
                    }
                });

        commandClsCaptor = ArgumentCaptor.forClass(ZWaveCommandClass.class);
        Mockito.doNothing().when(endpoint).addCommandClass(commandClsCaptor.capture());

        Mockito.when(mockedNode.getCommandClass(Matchers.any(CommandClass.class)))
                .thenAnswer(new Answer<ZWaveCommandClass>() {
                    @Override
                    public ZWaveCommandClass answer(InvocationOnMock invocation) {
                        return ZWaveCommandClass.getInstance(((CommandClass) invocation.getArguments()[0]).getKey(),
                                mockedNode, mockedController);
                    }
                });

        try {
            // Get the command class and process the response
            ZWaveCommandClass cls = ZWaveCommandClass.getInstance(msg.getMessagePayloadByte(3), mockedNode,
                    mockedController);
            assertNotNull(cls);
            cls.setVersion(version);
            cls.handleApplicationCommandRequest(new ZWaveCommandClassPayload(msg), 0);
        } catch (ZWaveSerialMessageException e) {
            fail("Out of bounds exception processing data");
        }

        // Return all the notifications....
        return argument.getAllValues();
    }

    /**
     * Helper class to create everything we need to test a command class message.
     *
     * We pass in the data, and the expected command class. This method creates the class, checks it's the right one,
     * processes the message and gets the response events.
     *
     * It expects at least one response event.
     *
     * @param packetData byte array containing the full Z-Wave frame
     * @return List of ZWaveEvent(s)
     */
    protected List<ZWaveEvent> processCommandClassMessage(byte[] packetData) {
        return processCommandClassMessage(packetData, 1);
    }

    ZWaveCommandClass getCommandClass(CommandClass cls) {
        ZWaveDeviceClass deviceClass = new ZWaveDeviceClass(Basic.NOT_KNOWN, Generic.NOT_KNOWN, Specific.NOT_USED);

        // Mock the controller so we can get any events
        mockedController = Mockito.mock(ZWaveController.class);
        mockedNode = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        Mockito.when(mockedNode.getNodeId()).thenReturn(99);
        Mockito.when(mockedNode.getDeviceClass()).thenReturn(deviceClass);
        Mockito.when(mockedNode.getEndpoint(Matchers.anyInt())).thenReturn(endpoint);
        ZWaveDeviceClass endpointDeviceClass = new ZWaveDeviceClass(Basic.NOT_KNOWN, Generic.NOT_KNOWN,
                Specific.NOT_USED);
        Mockito.when(endpoint.getDeviceClass()).thenReturn(endpointDeviceClass);

        commandClsCaptor = ArgumentCaptor.forClass(ZWaveCommandClass.class);
        Mockito.doNothing().when(endpoint).addCommandClass(commandClsCaptor.capture());

        // Get the command class and process the response
        ZWaveCommandClass cmdCls = ZWaveCommandClass.getInstance(cls.getKey(), mockedNode, mockedController);
        assertNotNull(cls);

        return cmdCls;
    }
}
