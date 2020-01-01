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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
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
        try {

            SerialMessage msg = new SerialMessage(packetData);

            // Check the packet is not corrupted and is a command class request
            assertEquals(true, msg.isValid);
            assertEquals(SerialMessageType.Request, msg.getMessageType());
            assertEquals(SerialMessageClass.ApplicationCommandHandler, msg.getMessageClass());

            // Mock the controller so we can get any events
            mockedController = Mockito.mock(ZWaveController.class);
            argument = ArgumentCaptor.forClass(ZWaveEvent.class);
            Mockito.doNothing().when(mockedController).notifyEventListeners(argument.capture());
            mockedNode = new ZWaveNode(0, 0, mockedController);
            ZWaveEndpoint mockedEndpoint0 = Mockito.mock(ZWaveEndpoint.class);
            ZWaveEndpoint mockedEndpoint1 = Mockito.mock(ZWaveEndpoint.class);
            ZWaveEndpoint mockedEndpoint2 = Mockito.mock(ZWaveEndpoint.class);
            ZWaveEndpoint mockedEndpoint3 = Mockito.mock(ZWaveEndpoint.class);

            associationList = new HashMap<Integer, ZWaveAssociationGroup>();
            for (int c = 1; c <= 10; c++) {
                mockedNode.setAssociationGroup(new ZWaveAssociationGroup(c));
            }

            Field endpointsField = ZWaveNode.class.getDeclaredField("endpoints");
            endpointsField.setAccessible(true);
            Map<Integer, ZWaveEndpoint> endpoints = (Map<Integer, ZWaveEndpoint>) endpointsField.get(mockedNode);
            endpoints.put(0, mockedEndpoint0);
            endpoints.put(1, mockedEndpoint1);
            endpoints.put(2, mockedEndpoint2);
            endpoints.put(3, mockedEndpoint3);

            Mockito.when(mockedEndpoint0.getEndpointId()).thenReturn(0);
            Mockito.when(mockedEndpoint1.getEndpointId()).thenReturn(1);
            Mockito.when(mockedEndpoint2.getEndpointId()).thenReturn(2);
            Mockito.when(mockedEndpoint3.getEndpointId()).thenReturn(3);

            // Mockito.when(mockedNode.getAssociationGroup(Matchers.any(Integer.class)))
            // .thenAnswer(new Answer<ZWaveAssociationGroup>() {
            // @Override
            // public ZWaveAssociationGroup answer(InvocationOnMock invocation) {
            // return associationList.get(invocation.getArguments()[0]);
            // }
            // });

            commandClsCaptor = ArgumentCaptor.forClass(ZWaveCommandClass.class);
            Mockito.doNothing().when(mockedEndpoint0).addCommandClass(commandClsCaptor.capture());
            Mockito.doNothing().when(mockedEndpoint1).addCommandClass(commandClsCaptor.capture());
            Mockito.doNothing().when(mockedEndpoint2).addCommandClass(commandClsCaptor.capture());
            Mockito.doNothing().when(mockedEndpoint3).addCommandClass(commandClsCaptor.capture());

            // Get the command class and process the response
            final ZWaveCommandClass cls = ZWaveCommandClass.getInstance(msg.getMessagePayloadByte(3), mockedNode,
                    mockedController);
            assertNotNull(cls);
            cls.setVersion(version);

            Mockito.when(mockedEndpoint0.getCommandClass(Matchers.any(CommandClass.class)))
                    .thenAnswer(new Answer<ZWaveCommandClass>() {
                        @Override
                        public ZWaveCommandClass answer(InvocationOnMock invocation) {
                            if (((CommandClass) invocation.getArguments()[0]).getKey() == cls.getCommandClass()
                                    .getKey()) {
                                return cls;
                            }
                            return ZWaveCommandClass.getInstance(((CommandClass) invocation.getArguments()[0]).getKey(),
                                    mockedNode, mockedController);
                        }
                    });
            Mockito.when(mockedEndpoint1.getCommandClass(Matchers.any(CommandClass.class)))
                    .thenAnswer(new Answer<ZWaveCommandClass>() {
                        @Override
                        public ZWaveCommandClass answer(InvocationOnMock invocation) {
                            if (((CommandClass) invocation.getArguments()[0]).getKey() == cls.getCommandClass()
                                    .getKey()) {
                                return cls;
                            }
                            return ZWaveCommandClass.getInstance(((CommandClass) invocation.getArguments()[0]).getKey(),
                                    mockedNode, mockedController);
                        }
                    });
            Mockito.when(mockedEndpoint2.getCommandClass(Matchers.any(CommandClass.class)))
                    .thenAnswer(new Answer<ZWaveCommandClass>() {
                        @Override
                        public ZWaveCommandClass answer(InvocationOnMock invocation) {
                            if (((CommandClass) invocation.getArguments()[0]).getKey() == cls.getCommandClass()
                                    .getKey()) {
                                return cls;
                            }
                            return ZWaveCommandClass.getInstance(((CommandClass) invocation.getArguments()[0]).getKey(),
                                    mockedNode, mockedController);
                        }
                    });
            Mockito.when(mockedEndpoint3.getCommandClass(Matchers.any(CommandClass.class)))
                    .thenAnswer(new Answer<ZWaveCommandClass>() {
                        @Override
                        public ZWaveCommandClass answer(InvocationOnMock invocation) {
                            if (((CommandClass) invocation.getArguments()[0]).getKey() == cls.getCommandClass()
                                    .getKey()) {
                                return cls;
                            }
                            return ZWaveCommandClass.getInstance(((CommandClass) invocation.getArguments()[0]).getKey(),
                                    mockedNode, mockedController);
                        }
                    });

            // cls.handleApplicationCommandRequest(new ZWaveCommandClassPayload(msg), 0);
            mockedNode.processCommand(new ZWaveCommandClassPayload(msg));
        } catch (ZWaveSerialMessageException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                | SecurityException e) {
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

    protected ZWaveCommandClass getCommandClass(CommandClass cls) {
        ZWaveDeviceClass deviceClass = new ZWaveDeviceClass(Basic.BASIC_TYPE_UNKNOWN, Generic.GENERIC_TYPE_NOT_USED,
                Specific.SPECIFIC_TYPE_NOT_USED);

        // Mock the controller so we can get any events
        mockedController = Mockito.mock(ZWaveController.class);
        mockedNode = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        Mockito.when(mockedNode.getNodeId()).thenReturn(99);
        Mockito.when(mockedNode.getDeviceClass()).thenReturn(deviceClass);
        Mockito.when(mockedNode.getEndpoint(Matchers.anyInt())).thenReturn(endpoint);
        ZWaveDeviceClass endpointDeviceClass = new ZWaveDeviceClass(Basic.BASIC_TYPE_UNKNOWN,
                Generic.GENERIC_TYPE_NOT_USED, Specific.SPECIFIC_TYPE_NOT_USED);
        Mockito.when(endpoint.getDeviceClass()).thenReturn(endpointDeviceClass);

        commandClsCaptor = ArgumentCaptor.forClass(ZWaveCommandClass.class);
        Mockito.doNothing().when(endpoint).addCommandClass(commandClsCaptor.capture());

        // Get the command class and process the response
        ZWaveCommandClass cmdCls = ZWaveCommandClass.getInstance(cls.getKey(), mockedNode, mockedController);
        assertNotNull(cls);

        return cmdCls;
    }
}
