package org.openhab.binding.zwave.internal.protocol;

import static org.junit.Assert.*;

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

    @Test
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
    public void testQueue() throws InterruptedException {
        PriorityBlockingQueue<ZWaveTransaction> sendQueue = new PriorityBlockingQueue<ZWaveTransaction>(10,
                new ZWaveTransactionComparator());

        ZWaveTransaction transaction1 = new ZWaveTransaction(
                new ZWaveCommandClassTransactionPayloadBuilder(1, CommandClass.COMMAND_CLASS_BASIC, 1)
                        .withExpectedResponseCommand(1).withPriority(TransactionPriority.Get).build());
        ZWaveTransaction transaction2 = new ZWaveTransaction(
                new ZWaveCommandClassTransactionPayloadBuilder(1, CommandClass.COMMAND_CLASS_BASIC, 1)
                        .withExpectedResponseCommand(1).withPriority(TransactionPriority.Set).build());
        ZWaveTransaction transaction3 = new ZWaveTransaction(
                new ZWaveCommandClassTransactionPayloadBuilder(1, CommandClass.COMMAND_CLASS_BASIC, 1)
                        .withExpectedResponseCommand(1).withPriority(TransactionPriority.Poll).build());

        sendQueue.clear();
        sendQueue.add(transaction1);
        sendQueue.add(transaction2);
        sendQueue.add(transaction3);
        assertEquals(transaction2, sendQueue.take());

        sendQueue.clear();
        sendQueue.add(transaction2);
        sendQueue.add(transaction1);
        sendQueue.add(transaction3);
        assertEquals(transaction2, sendQueue.take());

        sendQueue.clear();
        sendQueue.add(transaction3);
        sendQueue.add(transaction2);
        sendQueue.add(transaction1);
        assertEquals(transaction2, sendQueue.take());

        sendQueue.clear();
        sendQueue.add(transaction3);
        sendQueue.add(transaction1);
        sendQueue.add(transaction2);
        assertEquals(transaction2, sendQueue.take());
    }
}
