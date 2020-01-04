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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
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
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveMultiInstanceCommandClassTest}.
 *
 * Since this is an encapsulation class, we try not to test all the 'end-to-end' cases, but more the process of
 * encapsulation and decapsulation.
 *
 * However, this class does provide the mock framework to process a multi-instance message, and handle the inner
 * classes, returning the command class notification events.
 *
 * Some end-to-end cases are run though.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMultiInstanceCommandClassTest extends ZWaveCommandClassTest {

    protected List<ZWaveEvent> processMessage(byte[] packetData) {
        SerialMessage msg = new SerialMessage(packetData);

        // Check the packet is not corrupted and is a command class request
        assertEquals(true, msg.isValid);
        assertEquals(SerialMessageType.Request, msg.getMessageType());
        assertEquals(SerialMessageClass.ApplicationCommandHandler, msg.getMessageClass());

        try {
            // This method only handles MULTI_INSTANCE
            assertEquals(msg.getMessagePayloadByte(3), CommandClass.COMMAND_CLASS_MULTI_CHANNEL.getKey());

            // Mock the controller so we can get any events
            final ZWaveController mockedController = Mockito.mock(ZWaveController.class);
            argument = ArgumentCaptor.forClass(ZWaveEvent.class);
            Mockito.doNothing().when(mockedController).notifyEventListeners(argument.capture());
            final ZWaveNode node = new ZWaveNode(0, 0, mockedController);// Mockito.mock(ZWaveNode.class);

            // Get the command class and process the response
            ZWaveMultiInstanceCommandClass cls = (ZWaveMultiInstanceCommandClass) ZWaveCommandClass
                    .getInstance(CommandClass.COMMAND_CLASS_MULTI_CHANNEL.getKey(), node, mockedController);
            assertNotNull(cls);

            // Create an endpoint to capture the requests so we can return the command classes
            ZWaveEndpoint mockedEndpoint = Mockito.mock(ZWaveEndpoint.class);

            Field endpointsField;

            endpointsField = ZWaveNode.class.getDeclaredField("endpoints");

            endpointsField.setAccessible(true);
            Map<Integer, ZWaveEndpoint> endpoints = (Map<Integer, ZWaveEndpoint>) endpointsField.get(node);
            endpoints.put(0, mockedEndpoint);
            endpoints.put(1, mockedEndpoint);
            endpoints.put(2, mockedEndpoint);
            endpoints.put(3, mockedEndpoint);

            // Mockito.when(node.getEndpoint(Matchers.anyInt())).thenReturn(endpoint);
            Mockito.when(mockedEndpoint.getCommandClass(Matchers.any(CommandClass.class)))
                    .thenAnswer(new Answer<ZWaveCommandClass>() {
                        @Override
                        public ZWaveCommandClass answer(InvocationOnMock invocation) {
                            return ZWaveCommandClass.getInstance(((CommandClass) invocation.getArguments()[0]).getKey(),
                                    node, mockedController);
                        }
                    });

            // cls.handleApplicationCommandRequest(new ZWaveCommandClassPayload(msg), 0);
            node.processCommand(new ZWaveCommandClassPayload(msg));

            // cls.handleApplicationCommandRequest(new ZWaveCommandClassPayload(msg), 4);
        } catch (ZWaveSerialMessageException | NoSuchFieldException | SecurityException | IllegalArgumentException
                | IllegalAccessException e) {
            fail("Out of bounds exception processing data");
        }

        // Check that we received a response
        assertNotNull(argument.getAllValues());
        assertNotEquals(argument.getAllValues().size(), 0);

        // Return all the notifications....
        return argument.getAllValues();
    }

    @Test
    public void MultiInstance_MultiChannelMessageReceive() {
        byte[] packetData = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x07, 0x07, 0x60, 0x0D, 0x02, 0x02, 0x25, 0x03, (byte) 0xFF,
                0x42 };

        List<ZWaveEvent> events = processMessage(packetData);
        assertEquals(events.size(), 1);
        ZWaveCommandClassValueEvent event = (ZWaveCommandClassValueEvent) events.get(0);

        assertEquals(CommandClass.COMMAND_CLASS_SWITCH_BINARY, event.getCommandClass());
        // assertEquals(event.getNodeId(), 44);
        // assertEquals(2, event.getEndpoint());
        assertEquals(new Integer("255"), event.getValue());
    }

    @Test
    public void getMultiChannelCapabilityGetMessage() {
        ZWaveMultiInstanceCommandClass cls = (ZWaveMultiInstanceCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 96, 9, 1 };
        cls.setVersion(1);

        msg = cls.getMultiChannelCapabilityGetMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getMultiChannelEndpointGetMessage() {
        ZWaveMultiInstanceCommandClass cls = (ZWaveMultiInstanceCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 96, 7 };
        cls.setVersion(1);

        msg = cls.getMultiChannelEndpointGetMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void handleMultiChannelCapabilityReport() {
        byte[] packetData = { 0x60, 0x0A, 0x01, 0x10, 0x01, 0x25 };

        ZWaveMultiInstanceCommandClass cls = (ZWaveMultiInstanceCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL);

        ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(packetData);
        try {
            cls.handleApplicationCommandRequest(payload, 0);
        } catch (ZWaveSerialMessageException e) {
        }

        ZWaveDeviceClass endpointDeviceClass = mockedNode.getEndpoint(2).getDeviceClass();
        assertEquals(Basic.BASIC_TYPE_UNKNOWN, endpointDeviceClass.getBasicDeviceClass());
        assertEquals(Generic.GENERIC_TYPE_SWITCH_BINARY, endpointDeviceClass.getGenericDeviceClass());
        assertEquals(1, commandClsCaptor.getAllValues().size());
        assertEquals(Specific.SPECIFIC_TYPE_POWER_SWITCH_BINARY, endpointDeviceClass.getSpecificDeviceClass());
        assertNotNull(commandClsCaptor.getValue());
        assertEquals(CommandClass.COMMAND_CLASS_SWITCH_BINARY, commandClsCaptor.getValue().getCommandClass());
    }

    @Test
    public void handleMultiChannelCapabilityReport2() {
        byte[] packetData = { 0x60, 0x0A, 0x02, 0x31, 0x01, 0x32, 0x59, 0x5E, (byte) 0x85, (byte) 0x8E };

        ZWaveMultiInstanceCommandClass cls = (ZWaveMultiInstanceCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL);

        ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(packetData);
        try {
            cls.handleApplicationCommandRequest(payload, 0);
        } catch (ZWaveSerialMessageException e) {
        }

        ZWaveDeviceClass endpointDeviceClass = mockedNode.getEndpoint(2).getDeviceClass();
        assertEquals(Basic.BASIC_TYPE_UNKNOWN, endpointDeviceClass.getBasicDeviceClass());
        assertEquals(Generic.GENERIC_TYPE_METER, endpointDeviceClass.getGenericDeviceClass());
        assertEquals(Specific.SPECIFIC_TYPE_SIMPLE_METER, endpointDeviceClass.getSpecificDeviceClass());
        assertNotNull(commandClsCaptor.getValue());
        assertEquals(5, commandClsCaptor.getAllValues().size());
        assertEquals(CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION,
                commandClsCaptor.getValue().getCommandClass());
    }
}
