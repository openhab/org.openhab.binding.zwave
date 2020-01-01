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
import java.util.concurrent.PriorityBlockingQueue;

import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveTransactionComparatorTest {

    // @Test
    public void test() {
        ZWaveTransactionComparator comparator = new ZWaveTransactionComparator();

        ZWaveTransaction transaction1 = new ZWaveTransaction(Mockito.mock(ZWaveMessagePayloadTransaction.class));
        ZWaveTransaction transaction2 = new ZWaveTransaction(Mockito.mock(ZWaveMessagePayloadTransaction.class));

        transaction1.setPriority(TransactionPriority.NonceResponse);
        transaction2.setPriority(TransactionPriority.RealTime);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.RealTime);
        transaction2.setPriority(TransactionPriority.Immediate);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.Immediate);
        transaction2.setPriority(TransactionPriority.High);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.High);
        transaction2.setPriority(TransactionPriority.Set);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.Set);
        transaction2.setPriority(TransactionPriority.Controller);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.Controller);
        transaction2.setPriority(TransactionPriority.Get);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.Get);
        transaction2.setPriority(TransactionPriority.Config);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.Config);
        transaction2.setPriority(TransactionPriority.Poll);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.Set);
        transaction2.setPriority(TransactionPriority.Config);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.Config);
        transaction2.setPriority(TransactionPriority.Set);
        assertTrue(comparator.compare(transaction1, transaction2) > 0);

        transaction1.setPriority(TransactionPriority.High);
        transaction2.setPriority(TransactionPriority.Get);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.High);
        transaction2.setPriority(TransactionPriority.High);
        assertTrue(comparator.compare(transaction1, transaction2) < 0);

        transaction1.setPriority(TransactionPriority.High);
        transaction2.setPriority(TransactionPriority.High);
        assertTrue(comparator.compare(transaction2, transaction1) > 0);
    }

    @Test
    public void testQueue() throws InterruptedException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        PriorityBlockingQueue<ZWaveTransaction> sendQueue = new PriorityBlockingQueue<ZWaveTransaction>(10,
                new ZWaveTransactionComparator());

        ZWaveTransaction transaction1 = getTransaction(1, TransactionPriority.Get);
        ZWaveTransaction transaction2 = getTransaction(2, TransactionPriority.Set);
        ZWaveTransaction transaction3 = getTransaction(3, TransactionPriority.Poll);

        sendQueue.clear();
        sendQueue.add(transaction1);
        sendQueue.add(transaction2);
        sendQueue.add(transaction3);
        assertEquals(transaction2, sendQueue.take());
        assertEquals(transaction1, sendQueue.take());
        assertEquals(transaction3, sendQueue.take());

        sendQueue.clear();
        sendQueue.add(transaction2);
        sendQueue.add(transaction1);
        sendQueue.add(transaction3);
        assertEquals(transaction2, sendQueue.take());
        assertEquals(transaction1, sendQueue.take());
        assertEquals(transaction3, sendQueue.take());

        sendQueue.clear();
        sendQueue.add(transaction3);
        sendQueue.add(transaction2);
        sendQueue.add(transaction1);
        assertEquals(transaction2, sendQueue.take());
        assertEquals(transaction1, sendQueue.take());
        assertEquals(transaction3, sendQueue.take());

        sendQueue.clear();
        sendQueue.add(transaction3);
        sendQueue.add(transaction1);
        sendQueue.add(transaction2);
        assertEquals(transaction2, sendQueue.take());
        assertEquals(transaction1, sendQueue.take());
        assertEquals(transaction3, sendQueue.take());
    }

    @Test
    public void testQueuePriority() throws InterruptedException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        PriorityBlockingQueue<ZWaveTransaction> sendQueue = new PriorityBlockingQueue<ZWaveTransaction>(10,
                new ZWaveTransactionComparator());

        ZWaveTransaction transaction1 = getTransaction(3018, TransactionPriority.Immediate);
        ZWaveTransaction transaction2 = getTransaction(3017, TransactionPriority.Immediate);
        ZWaveTransaction transaction3 = getTransaction(3019, TransactionPriority.Immediate);

        sendQueue.clear();
        sendQueue.add(transaction3);
        sendQueue.add(transaction1);
        sendQueue.add(transaction2);
        assertEquals(3017, sendQueue.take().getTransactionId());
        assertEquals(3018, sendQueue.take().getTransactionId());
        assertEquals(3019, sendQueue.take().getTransactionId());

        sendQueue.clear();
        sendQueue.add(transaction1);
        sendQueue.add(transaction2);
        sendQueue.add(transaction3);
        assertEquals(3017, sendQueue.take().getTransactionId());
        assertEquals(3018, sendQueue.take().getTransactionId());
        assertEquals(3019, sendQueue.take().getTransactionId());

        sendQueue.clear();
        sendQueue.add(transaction2);
        sendQueue.add(transaction1);
        sendQueue.add(transaction3);
        assertEquals(3017, sendQueue.take().getTransactionId());
        assertEquals(3018, sendQueue.take().getTransactionId());
        assertEquals(3019, sendQueue.take().getTransactionId());
    }

    private ZWaveTransaction getTransaction(long transactionId, TransactionPriority priority)
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        ZWaveTransaction transaction = new ZWaveTransaction(
                new ZWaveCommandClassTransactionPayloadBuilder(1, CommandClass.COMMAND_CLASS_BASIC, 1)
                        .withExpectedResponseCommand(1).withPriority(priority).build());

        Field f1 = transaction.getClass().getDeclaredField("transactionId");
        f1.setAccessible(true);
        f1.set(transaction, transactionId);
        return transaction;
    }
}
