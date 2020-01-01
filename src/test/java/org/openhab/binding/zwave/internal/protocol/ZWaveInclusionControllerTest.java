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
package org.openhab.binding.zwave.internal.protocol;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveInclusionState;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveInclusionControllerTest {
    @Test
    public void testInclusion() {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ArgumentCaptor<ZWaveMessagePayloadTransaction> txCapture = ArgumentCaptor
                .forClass(ZWaveMessagePayloadTransaction.class);
        Mockito.doNothing().when(controller).enqueue(txCapture.capture());

        ArgumentCaptor<ZWaveEventListener> listenerCapture = ArgumentCaptor.forClass(ZWaveEventListener.class);

        ArgumentCaptor<ZWaveNode> nodeCapture = ArgumentCaptor.forClass(ZWaveNode.class);
        Mockito.doNothing().when(controller).includeNode(nodeCapture.capture());

        ZWaveInclusionController inclusionController = new ZWaveInclusionController(controller, "");
        assertEquals(ZWaveInclusionState.Unknown, inclusionController.getState());

        ZWaveMessagePayloadTransaction txFrame;
        inclusionController.startInclusion(true, true);
        assertEquals(ZWaveInclusionState.IncludeSent, inclusionController.getState());
        Mockito.verify(controller, Mockito.times(1)).addEventListener(listenerCapture.capture());

        // Make sure if call startInclusion again, it doesn't send another request.
        inclusionController.startInclusion(true, true);
        assertEquals(ZWaveInclusionState.IncludeSent, inclusionController.getState());
        Mockito.verify(controller, Mockito.times(1)).addEventListener(listenerCapture.capture());

        assertEquals(1, txCapture.getAllValues().size());
        txFrame = txCapture.getAllValues().get(0);
        assertTrue(Arrays.equals(new byte[] { -63 }, txFrame.getPayloadBuffer()));

        inclusionController.ZWaveIncomingEvent(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeStart));
        assertEquals(ZWaveInclusionState.IncludeStart, inclusionController.getState());

        assertEquals(1, txCapture.getAllValues().size());

        List<CommandClass> commandClasses = new ArrayList<CommandClass>();
        commandClasses.add(CommandClass.COMMAND_CLASS_ALARM);
        commandClasses.add(CommandClass.COMMAND_CLASS_ASSOCIATION);
        ZWaveInclusionEvent event = new ZWaveInclusionEvent(ZWaveInclusionState.IncludeSlaveFound, 23,
                Basic.BASIC_TYPE_ROUTING_SLAVE, Generic.GENERIC_TYPE_ENTRY_CONTROL, Specific.SPECIFIC_TYPE_NOT_USED,
                commandClasses);
        inclusionController.ZWaveIncomingEvent(event);
        assertEquals(ZWaveInclusionState.IncludeSlaveFound, inclusionController.getState());

        assertEquals(1, txCapture.getAllValues().size());

        inclusionController.ZWaveIncomingEvent(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeProtocolDone));
        assertEquals(ZWaveInclusionState.IncludeProtocolDone, inclusionController.getState());

        assertEquals(1, nodeCapture.getAllValues().size());
        ZWaveNode node = nodeCapture.getAllValues().get(0);
        assertEquals(23, node.getNodeId());

        assertEquals(2, txCapture.getAllValues().size());
        txFrame = txCapture.getAllValues().get(1);
        assertTrue(Arrays.equals(new byte[] { 5 }, txFrame.getPayloadBuffer()));
        assertTrue(txFrame.getSerialMessage().getCallbackId() != 0);

        inclusionController.ZWaveIncomingEvent(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeDone));
        assertEquals(ZWaveInclusionState.IncludeDone, inclusionController.getState());

        assertEquals(3, txCapture.getAllValues().size());
        txFrame = txCapture.getAllValues().get(2);
        assertTrue(Arrays.equals(new byte[] { 5 }, txFrame.getPayloadBuffer()));
        assertTrue(txFrame.getSerialMessage().getCallbackId() == 0);

        Mockito.verify(controller, Mockito.times(1)).includeDone();
        Mockito.verify(controller, Mockito.times(1)).removeEventListener(listenerCapture.capture());
    }

    @Test
    public void testExclusion() {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ArgumentCaptor<ZWaveMessagePayloadTransaction> txCapture = ArgumentCaptor
                .forClass(ZWaveMessagePayloadTransaction.class);
        Mockito.doNothing().when(controller).enqueue(txCapture.capture());

        ArgumentCaptor<ZWaveEventListener> listenerCapture = ArgumentCaptor.forClass(ZWaveEventListener.class);

        ArgumentCaptor<ZWaveNode> nodeCapture = ArgumentCaptor.forClass(ZWaveNode.class);
        Mockito.doNothing().when(controller).includeNode(nodeCapture.capture());

        ZWaveInclusionController inclusionController = new ZWaveInclusionController(controller, "");
        assertEquals(ZWaveInclusionState.Unknown, inclusionController.getState());

        ZWaveMessagePayloadTransaction txFrame;
        inclusionController.startExclusion();
        assertEquals(ZWaveInclusionState.ExcludeSent, inclusionController.getState());

        Mockito.verify(controller, Mockito.times(1)).addEventListener(listenerCapture.capture());

        assertEquals(1, txCapture.getAllValues().size());
        txFrame = txCapture.getAllValues().get(0);
        assertTrue(Arrays.equals(new byte[] { 1 }, txFrame.getPayloadBuffer()));

        inclusionController.ZWaveIncomingEvent(new ZWaveInclusionEvent(ZWaveInclusionState.ExcludeStart));
        assertEquals(ZWaveInclusionState.ExcludeStart, inclusionController.getState());

        assertEquals(1, txCapture.getAllValues().size());

        inclusionController.ZWaveIncomingEvent(new ZWaveInclusionEvent(ZWaveInclusionState.ExcludeSlaveFound));
        assertEquals(ZWaveInclusionState.ExcludeSlaveFound, inclusionController.getState());

        assertEquals(1, txCapture.getAllValues().size());

        inclusionController.ZWaveIncomingEvent(new ZWaveInclusionEvent(ZWaveInclusionState.ExcludeDone));
        assertEquals(ZWaveInclusionState.ExcludeDone, inclusionController.getState());

        assertEquals(2, txCapture.getAllValues().size());
        txFrame = txCapture.getAllValues().get(1);
        assertTrue(Arrays.equals(new byte[] { 5 }, txFrame.getPayloadBuffer()));
        assertTrue(txFrame.getSerialMessage().getCallbackId() == 0);

        Mockito.verify(controller, Mockito.times(1)).includeDone();
        Mockito.verify(controller, Mockito.times(1)).removeEventListener(listenerCapture.capture());
    }

    @Test
    public void testCancelAfterNodeFound() {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ArgumentCaptor<ZWaveMessagePayloadTransaction> txCapture = ArgumentCaptor
                .forClass(ZWaveMessagePayloadTransaction.class);
        Mockito.doNothing().when(controller).enqueue(txCapture.capture());

        ArgumentCaptor<ZWaveEventListener> listenerCapture = ArgumentCaptor.forClass(ZWaveEventListener.class);

        ArgumentCaptor<ZWaveNode> nodeCapture = ArgumentCaptor.forClass(ZWaveNode.class);
        Mockito.doNothing().when(controller).includeNode(nodeCapture.capture());

        ZWaveInclusionController inclusionController = new ZWaveInclusionController(controller, "");
        assertEquals(ZWaveInclusionState.Unknown, inclusionController.getState());

        ZWaveMessagePayloadTransaction txFrame;
        inclusionController.startInclusion(true, true);
        assertEquals(ZWaveInclusionState.IncludeSent, inclusionController.getState());

        Mockito.verify(controller, Mockito.times(1)).addEventListener(listenerCapture.capture());

        assertEquals(1, txCapture.getAllValues().size());
        txFrame = txCapture.getAllValues().get(0);
        assertTrue(Arrays.equals(new byte[] { -63 }, txFrame.getPayloadBuffer()));

        inclusionController.ZWaveIncomingEvent(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeStart));
        assertEquals(ZWaveInclusionState.IncludeStart, inclusionController.getState());

        assertEquals(1, txCapture.getAllValues().size());

        List<CommandClass> commandClasses = new ArrayList<CommandClass>();
        commandClasses.add(CommandClass.COMMAND_CLASS_ALARM);
        commandClasses.add(CommandClass.COMMAND_CLASS_ASSOCIATION);
        ZWaveInclusionEvent event = new ZWaveInclusionEvent(ZWaveInclusionState.IncludeSlaveFound, 23,
                Basic.BASIC_TYPE_ROUTING_SLAVE, Generic.GENERIC_TYPE_ENTRY_CONTROL, Specific.SPECIFIC_TYPE_NOT_USED,
                commandClasses);
        inclusionController.ZWaveIncomingEvent(event);
        assertEquals(ZWaveInclusionState.IncludeSlaveFound, inclusionController.getState());

        // Verify that sending stop here does nothing
        inclusionController.stop();
        assertEquals(ZWaveInclusionState.IncludeSlaveFound, inclusionController.getState());
    }

    @Test
    public void testCancelBeforeNodeFound() {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ArgumentCaptor<ZWaveMessagePayloadTransaction> txCapture = ArgumentCaptor
                .forClass(ZWaveMessagePayloadTransaction.class);
        Mockito.doNothing().when(controller).enqueue(txCapture.capture());

        ArgumentCaptor<ZWaveEventListener> listenerCapture = ArgumentCaptor.forClass(ZWaveEventListener.class);

        ArgumentCaptor<ZWaveNode> nodeCapture = ArgumentCaptor.forClass(ZWaveNode.class);
        Mockito.doNothing().when(controller).includeNode(nodeCapture.capture());

        ZWaveInclusionController inclusionController = new ZWaveInclusionController(controller, "");
        assertEquals(ZWaveInclusionState.Unknown, inclusionController.getState());

        ZWaveMessagePayloadTransaction txFrame;
        inclusionController.startInclusion(true, true);
        assertEquals(ZWaveInclusionState.IncludeSent, inclusionController.getState());

        Mockito.verify(controller, Mockito.times(1)).addEventListener(listenerCapture.capture());

        assertEquals(1, txCapture.getAllValues().size());
        txFrame = txCapture.getAllValues().get(0);
        assertTrue(Arrays.equals(new byte[] { -63 }, txFrame.getPayloadBuffer()));

        inclusionController.ZWaveIncomingEvent(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeStart));
        assertEquals(ZWaveInclusionState.IncludeStart, inclusionController.getState());

        // Verify that sending stop here results in stop...
        inclusionController.stop();
        assertEquals(ZWaveInclusionState.IncludeDone, inclusionController.getState());

        assertEquals(2, txCapture.getAllValues().size());
        txFrame = txCapture.getAllValues().get(1);
        assertTrue(Arrays.equals(new byte[] { 5 }, txFrame.getPayloadBuffer()));
        assertTrue(txFrame.getSerialMessage().getCallbackId() != 0);
    }

}
