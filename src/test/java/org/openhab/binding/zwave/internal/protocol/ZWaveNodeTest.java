/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwave.internal.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiInstanceCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSupervisionCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveDelayedPollEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test for {@link ZWaveNode}
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ZWaveNodeTest {
    @Test
    public void setAssociation() {
        ZWaveController controller = null;
        ZWaveEndpoint endpoint = null;
        ZWaveNode node = new ZWaveNode(1, 2, controller);
        node.addCommandClass(new ZWaveAssociationCommandClass(node, controller, endpoint));
        node.addCommandClass(new ZWaveMultiAssociationCommandClass(node, controller, endpoint));

        ZWaveCommandClassTransactionPayload msg;
        byte[] expectedResponse;

        // Setting device endpoint null and receive endpoint 0 should use single instance when only 1 endpoint in the
        // node
        expectedResponse = new byte[] { -123, 1, 0, 5 };
        msg = node.setAssociation(0, new ZWaveAssociation(5, 0));
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 0 should use single instance when only 1 endpoint in the
        // node
        expectedResponse = new byte[] { -123, 1, 0, 5 };
        msg = node.setAssociation(0, new ZWaveAssociation(5, 1));
        byte[] a = msg.getPayloadBuffer();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 0 should use multi instance when more than 1 endpoint
        expectedResponse = new byte[] { -114, 1, 0, 5 };
        node.addEndpoint(1);
        msg = node.setAssociation(0, new ZWaveAssociation(5, 0));
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 1 should use multi instance
        expectedResponse = new byte[] { -114, 1, 0, 0, 5, 1 };
        msg = node.setAssociation(0, new ZWaveAssociation(5, 1));
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 0 should use single instance
        expectedResponse = new byte[] { -114, 1, 0, 5 };
        msg = node.setAssociation(0, new ZWaveAssociation(5));
        byte[] x = msg.getPayloadBuffer();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }

    private List<ZWaveCommandClassPayload> processCommand(byte[] data) {
        ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(data);

        ZWaveController controller = null;
        ZWaveEndpoint endpoint = null;
        ZWaveNode node = new ZWaveNode(1, 2, controller);
        node.addCommandClass(new ZWaveMultiInstanceCommandClass(node, controller, endpoint));

        return node.processCommand(payload);
    }

    @Test
    public void testMultiChannelShortFrame() {
        List<ZWaveCommandClassPayload> response = processCommand(new byte[] { 0x60, 0x0D, 0x01 });

        assertNull(response);
    }

    @Test
    public void testSupervisionSuccessReplaysSwitchSetAsValueEvent() {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = new ZWaveNode(1, 2, controller);

        ArgumentCaptor<ZWaveEvent> eventCaptor = ArgumentCaptor.forClass(ZWaveEvent.class);
        Mockito.doNothing().when(controller).notifyEventListeners(eventCaptor.capture());

        ZWaveBinarySwitchCommandClass switchClass = new ZWaveBinarySwitchCommandClass(node, controller, null);
        ZWaveSupervisionCommandClass supervisionClass = new ZWaveSupervisionCommandClass(node, controller, null);
        node.addCommandClass(switchClass);
        node.addCommandClass(supervisionClass);

        ZWaveCommandClassTransactionPayload setTransaction = switchClass.setValueMessage(0xFF);
        ZWaveCommandClassTransactionPayload supervisedTransaction = node.encapsulate(setTransaction, 0);
        assertNotNull(supervisedTransaction);

        int sessionId = supervisedTransaction.getPayloadByte(2) & 0x3F;
        byte[] supervisionSuccess = new byte[] { 0x6C, 0x02, (byte) (sessionId & 0x3F), (byte) 0xFF, 0x00 };

        List<ZWaveCommandClassPayload> response = node.processCommand(new ZWaveCommandClassPayload(supervisionSuccess));
        assertNotNull(response);

        List<ZWaveEvent> events = eventCaptor.getAllValues();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ZWaveCommandClassValueEvent);

        ZWaveCommandClassValueEvent valueEvent = (ZWaveCommandClassValueEvent) events.get(0);
        assertEquals(CommandClass.COMMAND_CLASS_SWITCH_BINARY, valueEvent.getCommandClass());
        assertEquals(0xFF, valueEvent.getValue());
    }

    @Test
    public void testSupervisionWorkingAndMoreUpdatesDoNotReplaySwitchSet() {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = new ZWaveNode(1, 2, controller);

        ArgumentCaptor<ZWaveEvent> eventCaptor = ArgumentCaptor.forClass(ZWaveEvent.class);
        Mockito.doNothing().when(controller).notifyEventListeners(eventCaptor.capture());

        ZWaveBinarySwitchCommandClass switchClass = new ZWaveBinarySwitchCommandClass(node, controller, null);
        ZWaveSupervisionCommandClass supervisionClass = new ZWaveSupervisionCommandClass(node, controller, null);
        node.addCommandClass(switchClass);
        node.addCommandClass(supervisionClass);

        ZWaveCommandClassTransactionPayload setTransaction = switchClass.setValueMessage(0xFF);
        ZWaveCommandClassTransactionPayload supervisedTransaction = node.encapsulate(setTransaction, 0);
        assertNotNull(supervisedTransaction);

        int sessionId = supervisedTransaction.getPayloadByte(2) & 0x3F;

        byte[] supervisionWorking = new byte[] { 0x6C, 0x02, (byte) (sessionId & 0x3F), 0x01, 0x00 };
        List<ZWaveCommandClassPayload> responseWorking = node.processCommand(new ZWaveCommandClassPayload(supervisionWorking));
        assertNotNull(responseWorking);
        assertTrue(eventCaptor.getAllValues().isEmpty());

        byte[] supervisionMoreUpdates = new byte[] { 0x6C, 0x02, (byte) (0x40 | (sessionId & 0x3F)), (byte) 0xFF,
                0x00 };
        List<ZWaveCommandClassPayload> responseMoreUpdates = node
                .processCommand(new ZWaveCommandClassPayload(supervisionMoreUpdates));
        assertNotNull(responseMoreUpdates);
        assertTrue(eventCaptor.getAllValues().isEmpty());
    }

    @Test
    public void testSupervisionTerminalFailTriggersDelayedPollFallback() {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = new ZWaveNode(1, 2, controller);

        ArgumentCaptor<ZWaveEvent> eventCaptor = ArgumentCaptor.forClass(ZWaveEvent.class);
        Mockito.doNothing().when(controller).notifyEventListeners(eventCaptor.capture());

        ZWaveBinarySwitchCommandClass switchClass = new ZWaveBinarySwitchCommandClass(node, controller, null);
        ZWaveSupervisionCommandClass supervisionClass = new ZWaveSupervisionCommandClass(node, controller, null);
        node.addCommandClass(switchClass);
        node.addCommandClass(supervisionClass);

        ZWaveCommandClassTransactionPayload setTransaction = switchClass.setValueMessage(0xFF);
        ZWaveCommandClassTransactionPayload supervisedTransaction = node.encapsulate(setTransaction, 0);
        assertNotNull(supervisedTransaction);

        int sessionId = supervisedTransaction.getPayloadByte(2) & 0x3F;
        byte[] supervisionFail = new byte[] { 0x6C, 0x02, (byte) (sessionId & 0x3F), 0x02, 0x00 };

        List<ZWaveCommandClassPayload> response = node.processCommand(new ZWaveCommandClassPayload(supervisionFail));
        assertNotNull(response);

        List<ZWaveEvent> events = eventCaptor.getAllValues();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ZWaveDelayedPollEvent);
    }

    @Test
    public void testSupervisionWorkingDurationExtendsPendingExpiry() throws Exception {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = new ZWaveNode(1, 2, controller);

        ZWaveBinarySwitchCommandClass switchClass = new ZWaveBinarySwitchCommandClass(node, controller, null);
        ZWaveSupervisionCommandClass supervisionClass = new ZWaveSupervisionCommandClass(node, controller, null);
        node.addCommandClass(switchClass);
        node.addCommandClass(supervisionClass);

        ZWaveCommandClassTransactionPayload setTransaction = switchClass.setValueMessage(0xFF);
        ZWaveCommandClassTransactionPayload supervisedTransaction = node.encapsulate(setTransaction, 0);
        assertNotNull(supervisedTransaction);

        int sessionId = supervisedTransaction.getPayloadByte(2) & 0x3F;

        Field pendingField = ZWaveNode.class.getDeclaredField("pendingSupervisionCommands");
        pendingField.setAccessible(true);
        Map<Integer, Object> pendingMap = (Map<Integer, Object>) pendingField.get(node);
        Object pending = pendingMap.get(sessionId);
        assertNotNull(pending);

        Field expiresField = pending.getClass().getDeclaredField("expiresAtMillis");
        expiresField.setAccessible(true);
        long expiresBefore = (long) expiresField.get(pending);

        // 0x81 means about 2 minutes in Z-Wave duration format.
        byte[] supervisionWorking = new byte[] { 0x6C, 0x02, (byte) (sessionId & 0x3F), 0x01, (byte) 0x81 };
        List<ZWaveCommandClassPayload> response = node.processCommand(new ZWaveCommandClassPayload(supervisionWorking));
        assertNotNull(response);

        long expiresAfter = (long) expiresField.get(pending);
        assertTrue(expiresAfter > expiresBefore);
    }
}
