package org.openhab.binding.zwave.test.internal.protocol;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionManager;

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

}
