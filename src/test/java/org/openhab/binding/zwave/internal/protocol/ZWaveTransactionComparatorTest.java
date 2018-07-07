package org.openhab.binding.zwave.internal.protocol;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;

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
}
