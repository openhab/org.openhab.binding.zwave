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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;;

public class ZWaveTransactionManagerTest {
    protected ArgumentCaptor<SerialMessage> txQueueCapture;
    protected ArgumentCaptor<SerialMessage> serialMessageComplete;
    protected ArgumentCaptor<ZWaveTransaction> transactionCompleteCapture;
    protected ZWaveController controller;

    protected ZWaveTransactionManager getTransactionManagerForTimeout() {
        // Mock the controller so we can get any events
        controller = Mockito.mock(ZWaveController.class);

        txQueueCapture = ArgumentCaptor.forClass(SerialMessage.class);
        Mockito.doNothing().when(controller).sendPacket(txQueueCapture.capture());

        serialMessageComplete = ArgumentCaptor.forClass(SerialMessage.class);
        transactionCompleteCapture = ArgumentCaptor.forClass(ZWaveTransaction.class);

        // ZWaveNode node = Mockito.mock(ZWaveNode.class);
        // ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);

        return new ZWaveTransactionManager(controller);
    }

    protected ZWaveTransactionManager getTransactionManager() {
        ZWaveTransactionManager manager = getTransactionManagerForTimeout();

        Mockito.doNothing().when(controller).handleTransactionComplete(transactionCompleteCapture.capture(),
                serialMessageComplete.capture());

        return manager;
    }

    @Test
    public void queueTransactionForSend()
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.isAwake()).thenReturn(true);
        Mockito.when(controller.getNode(Mockito.anyInt())).thenReturn(node);
        ZWaveTransactionManager manager = new ZWaveTransactionManager(controller);

        Field holdoffDelay = manager.getClass().getDeclaredField("holdoffDelay");
        holdoffDelay.setAccessible(true);
        Calendar holdoff = Calendar.getInstance();
        holdoff.setTimeInMillis(System.currentTimeMillis() + 999999);
        holdoffDelay.set(manager, holdoff);

        Field holdoffActive = manager.getClass().getDeclaredField("holdoffActive");
        holdoffActive.setAccessible(true);
        holdoffActive.set(manager, new AtomicBoolean(true));

        ZWaveCommandClassTransactionPayload payload1 = new ZWaveCommandClassTransactionPayloadBuilder(1,
                org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC,
                1).withExpectedResponseCommand(2).withPriority(TransactionPriority.Get).build();

        manager.queueTransactionForSend(payload1);
        assertEquals(1, manager.getSendQueueLength(1));

        ZWaveCommandClassTransactionPayload payload2 = new ZWaveCommandClassTransactionPayloadBuilder(1,
                org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC,
                1).withExpectedResponseCommand(2).withPriority(TransactionPriority.Get).build();
        manager.queueTransactionForSend(payload2);
        assertEquals(1, manager.getSendQueueLength(1));

        ZWaveCommandClassTransactionPayload payload3 = new ZWaveCommandClassTransactionPayloadBuilder(2,
                org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC,
                1).withExpectedResponseCommand(2).withPriority(TransactionPriority.Get).build();
        manager.queueTransactionForSend(payload3);
        assertEquals(1, manager.getSendQueueLength(1));
        assertEquals(1, manager.getSendQueueLength(2));
    }

    @Test
    public void queueTransactionForSendMulti() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, InterruptedException {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.isAwake()).thenReturn(true);
        Mockito.when(controller.getNode(Mockito.anyInt())).thenReturn(node);
        ZWaveTransactionManager manager = new ZWaveTransactionManager(controller);

        // Set holdoff to prevent messages being sent
        Field holdoffDelay = manager.getClass().getDeclaredField("holdoffDelay");
        holdoffDelay.setAccessible(true);
        Calendar holdoff = Calendar.getInstance();
        holdoff.setTimeInMillis(System.currentTimeMillis() + 999999);
        holdoffDelay.set(manager, holdoff);
        Field holdoffActive = manager.getClass().getDeclaredField("holdoffActive");
        holdoffActive.setAccessible(true);
        holdoffActive.set(manager, new AtomicBoolean(true));

        ExecutorService es = Executors.newCachedThreadPool();
        for (int i = 0; i < 18; i++) {
            es.execute(new test(manager, i));
        }
        es.shutdown();
        assertTrue(es.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(1, manager.getSendQueueLength(1));
        assertEquals(1, manager.getSendQueueLength(2));
        assertEquals(1, manager.getSendQueueLength(3));
        assertEquals(1, manager.getSendQueueLength(4));
        assertEquals(1, manager.getSendQueueLength(5));
        assertEquals(1, manager.getSendQueueLength(6));
        assertEquals(1, manager.getSendQueueLength(7));
        assertEquals(1, manager.getSendQueueLength(8));
        assertEquals(1, manager.getSendQueueLength(9));
        assertEquals(1, manager.getSendQueueLength(10));
    }

    @Test
    public void getMessageFromQueue() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node1 = Mockito.mock(ZWaveNode.class);
        Mockito.when(node1.isAwake()).thenReturn(true);
        Mockito.when(controller.getNode(1)).thenReturn(node1);
        Mockito.when(node1.isAwake()).thenReturn(false);
        Mockito.when(node1.isListening()).thenReturn(true);
        ZWaveNode node2 = Mockito.mock(ZWaveNode.class);
        Mockito.when(node2.isAwake()).thenReturn(true);
        Mockito.when(controller.getNode(2)).thenReturn(node2);
        Mockito.when(node2.isAwake()).thenReturn(true);
        Mockito.when(node2.isListening()).thenReturn(true);
        ZWaveTransactionManager manager = new ZWaveTransactionManager(controller);

        PriorityBlockingQueue<ZWaveTransaction> queue = new PriorityBlockingQueue<>(10,
                new ZWaveTransactionComparator());

        queue.add(new ZWaveTransaction(new ZWaveCommandClassTransactionPayloadBuilder(1,
                org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC,
                1).withPriority(TransactionPriority.Get).build()));
        queue.add(new ZWaveTransaction(new ZWaveCommandClassTransactionPayloadBuilder(1,
                org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC,
                2).withPriority(TransactionPriority.Set).build()));
        queue.add(new ZWaveTransaction(new ZWaveCommandClassTransactionPayloadBuilder(2,
                org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC,
                1).withPriority(TransactionPriority.Get).build()));
        queue.add(new ZWaveTransaction(new ZWaveCommandClassTransactionPayloadBuilder(2,
                org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC,
                2).withPriority(TransactionPriority.Set).build()));

        Method method = manager.getClass().getDeclaredMethod("getMessageFromQueue", PriorityBlockingQueue.class);
        method.setAccessible(true);

        ZWaveTransaction transaction = (ZWaveTransaction) method.invoke(manager, queue);
        assertNotNull(transaction);
        assertEquals(TransactionPriority.Set, transaction.getPriority());

        transaction = (ZWaveTransaction) method.invoke(manager, queue);
        assertNotNull(transaction);
        assertEquals(TransactionPriority.Get, transaction.getPriority());

        transaction = (ZWaveTransaction) method.invoke(manager, queue);
        assertNull(transaction);
    }

    class test implements Runnable {
        ZWaveTransactionManager manager;
        int node;

        test(ZWaveTransactionManager manager, int node) {
            this.manager = manager;
            this.node = node;
        }

        @Override
        public void run() {
            ZWaveCommandClassTransactionPayload payload1 = new ZWaveCommandClassTransactionPayloadBuilder(node,
                    org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.COMMAND_CLASS_BASIC,
                    1).withExpectedResponseCommand(2).withPriority(TransactionPriority.Get).build();

            manager.queueTransactionForSend(payload1);
        }
    }
}
