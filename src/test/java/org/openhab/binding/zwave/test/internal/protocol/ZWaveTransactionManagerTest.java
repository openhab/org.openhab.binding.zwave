package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionState;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionManager;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AddNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetSucNodeIdMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetVersionMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.MemoryGetIdMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeNeighborUpdateMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiGetCapabilitiesMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiSetTimeoutsMessageClass;

public class ZWaveTransactionManagerTest {
    private ArgumentCaptor<SerialMessage> txQueueCapture;
    private ArgumentCaptor<SerialMessage> serialMessageComplete;
    private ArgumentCaptor<ZWaveTransaction> transactionCompleteCapture;
    private ZWaveController controller;

    private ZWaveTransactionManager getTransactionManagerForTimeout() {
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

    private ZWaveTransactionManager getTransactionManager() {
        ZWaveTransactionManager manager = getTransactionManagerForTimeout();

        Mockito.doNothing().when(controller).handleTransactionComplete(transactionCompleteCapture.capture(),
                serialMessageComplete.capture());

        return manager;
    }

    @Test
    public void TestSendQueue() {
        ZWaveTransactionManager manager = getTransactionManager();

        byte[] packetData = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x01,
                (byte) 0xB8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x51 };
        byte[] packetData1 = { 0x01, 0x14, 0x00, 0x04, 0x00, (byte) 0xef };
        byte[] packetData2 = { 0x01, 0x14, 0x00, 0x04, 0x01, (byte) 0xee };
        byte[] packetData3 = { 0x01, 0x14, 0x00, 0x04, 0x02, (byte) 0xed };
        byte[] packetData4 = { 0x01, 0x14, 0x00, 0x04, 0x03, (byte) 0xec };

        ZWaveTransaction transaction;
        SerialMessage serialMessage;

        // We add a transaction which gets sent immediately so the rest are queued
        serialMessage = new SerialMessage(packetData);
        transaction = new ZWaveTransactionBuilder(serialMessage).build();
        manager.queueTransactionForSend(transaction);

        // Queue must start empty or we're doomed from the start!
        assertEquals(0, manager.getSendQueueLength());

        // Add a frame and make sure the queue is 1 transaction
        serialMessage = new SerialMessage(packetData);
        transaction = new ZWaveTransactionBuilder(serialMessage).build();
        manager.queueTransactionForSend(transaction);
        assertEquals(1, manager.getSendQueueLength());

        // Add it again and make sure it is not duplicated
        serialMessage = new SerialMessage(packetData);
        transaction = new ZWaveTransactionBuilder(serialMessage).build();
        manager.queueTransactionForSend(transaction);
        assertEquals(1, manager.getSendQueueLength());

        // Clear the queue
        manager.clearSendQueue();
        assertEquals(0, manager.getSendQueueLength());

        // Add some messages with different priorities and check they are returned in the correct order
        serialMessage = new SerialMessage(packetData1);
        transaction = new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.Poll).build();
        manager.queueTransactionForSend(transaction);
        assertEquals(1, manager.getSendQueueLength());

        serialMessage = new SerialMessage(packetData2);
        transaction = new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.Get).build();
        manager.queueTransactionForSend(transaction);
        assertEquals(2, manager.getSendQueueLength());

        serialMessage = new SerialMessage(packetData3);
        transaction = new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.Immediate).build();
        manager.queueTransactionForSend(transaction);
        assertEquals(3, manager.getSendQueueLength());

        serialMessage = new SerialMessage(packetData4);
        transaction = new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.Set).build();
        manager.queueTransactionForSend(transaction);
        assertEquals(4, manager.getSendQueueLength());

        // Check that the messages are queued in the correct order
        assertEquals(TransactionPriority.Immediate, manager.getTransactionToSend().getPriority());
        assertEquals(TransactionPriority.Set, manager.getTransactionToSend().getPriority());
        assertEquals(TransactionPriority.Get, manager.getTransactionToSend().getPriority());
        assertEquals(TransactionPriority.Poll, manager.getTransactionToSend().getPriority());
    }

    @Test
    public void TestTimeout1() {
        ZWaveTransactionManager manager = getTransactionManagerForTimeout();

        // Test transaction requiring a RESponse - uses SerialApiSetTimeouts
        ZWaveTransaction transaction = new SerialApiSetTimeoutsMessageClass().doRequest(150, 15);
        transaction.setAttemptsRemaining(1);

        long start = System.currentTimeMillis();
        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Wait for the timeout
        Mockito.verify(controller, Mockito.timeout(5000))
                .handleTransactionComplete(transactionCompleteCapture.capture(), serialMessageComplete.capture());

        // Mockito.verify(controller, Mockito.timeout(5000))
        // .handleTransactionComplete(transactionCompleteCapture.capture(), serialMessageComplete.capture());

        long duration = System.currentTimeMillis() - start;

        // Check that this transaction completed
        // Note - only test the minimum time in case the execution gets delayed and its a lot longer than expected.
        // It shouldn't be shorter!
        assertTrue(duration > 490);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.CANCELLED, transactionCompleteCapture.getValue().getTransactionState());
    }

    @Test
    public void TestTimeout1Retry() {
        ZWaveTransactionManager manager = getTransactionManagerForTimeout();

        // Test transaction requiring a RESponse - uses SerialApiSetTimeouts
        ZWaveTransaction transaction = new SerialApiSetTimeoutsMessageClass().doRequest(150, 15);
        transaction.setAttemptsRemaining(3);

        long start = System.currentTimeMillis();
        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Wait for the timeout - this should be 3xTimer1 (1500ms total)
        Mockito.verify(controller, Mockito.timeout(15000))
                .handleTransactionComplete(transactionCompleteCapture.capture(), serialMessageComplete.capture());

        long duration = System.currentTimeMillis() - start;

        // Check that this transaction completed and we have 3 messages sent.
        // Note - only test the minimum time in case the execution gets delayed and its a lot longer than expected.
        // It shouldn't be shorter!
        assertTrue(duration > 1500);
        assertEquals(3, txQueueCapture.getAllValues().size());
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.CANCELLED, transactionCompleteCapture.getValue().getTransactionState());
    }

    @Test
    public void TestTimeout1RetryIncorrectPacketReceived() {
        byte[] invalidPacket = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x05, 0x04, 0x73, 0x03, 0x00, 0x00, (byte) 0x80 };

        ZWaveTransactionManager manager = getTransactionManagerForTimeout();

        // Test transaction requiring a RESponse - uses SerialApiSetTimeouts
        ZWaveTransaction transaction = new SerialApiSetTimeoutsMessageClass().doRequest(150, 15);
        transaction.setAttemptsRemaining(3);

        long start = System.currentTimeMillis();
        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Add a response not related to this transaction
        SerialMessage message = new SerialMessage(invalidPacket);
        manager.processReceiveMessage(message);

        // Make sure the transaction didn't complete
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Wait for the timeout - this should be 3xTimer1 (1500ms total)
        Mockito.verify(controller, Mockito.timeout(15000))
                .handleTransactionComplete(transactionCompleteCapture.capture(), serialMessageComplete.capture());

        long duration = System.currentTimeMillis() - start;

        // Check that this transaction completed and we have 3 messages sent.
        // Note - only test the minimum time in case the execution gets delayed and its a lot longer than expected.
        // It shouldn't be shorter!
        assertTrue(duration > 1500);
        assertEquals(3, txQueueCapture.getAllValues().size());
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.CANCELLED, transactionCompleteCapture.getValue().getTransactionState());
    }

    @Test
    public void TestTimeout2() {
        ZWaveTransactionManager manager = getTransactionManagerForTimeout();

        // Test transaction requiring a REQuest
        ZWaveTransaction transaction = new AddNodeMessageClass().doRequestStart(true, true);
        transaction.setAttemptsRemaining(1);

        long start = System.currentTimeMillis();
        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Wait for the timeout
        Mockito.verify(controller, Mockito.timeout(5000))
                .handleTransactionComplete(transactionCompleteCapture.capture(), serialMessageComplete.capture());

        long duration = System.currentTimeMillis() - start;

        // Check that this transaction completed
        // Note - only test the minimum time in case the execution gets delayed and its a lot longer than expected.
        // It shouldn't be shorter!
        assertTrue(duration > 2490);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.CANCELLED, transactionCompleteCapture.getValue().getTransactionState());
    }

    @Test
    public void TestTimeout3() {
        byte[] responsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        byte[] responsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x53, 0x00, 0x00, 0x02, (byte) 0xBA };

        ZWaveTransactionManager manager = getTransactionManagerForTimeout();

        // Test transaction
        SerialMessage message = new ZWaveSendDataMessageBuilder().withCommandClass(CommandClass.SENSOR_ALARM, 1)
                .withNodeId(5).withPayload(5, 3, CommandClass.SENSOR_ALARM.getKey(), 1, 1).build();

        ZWaveTransaction transaction = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(CommandClass.SENSOR_ALARM, 2).build();
        transaction.setAttemptsRemaining(1);
        transaction.getSerialMessage().setCallbackId(83);

        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Provide the response and make sure the transaction didn't complete
        message = new SerialMessage(responsePacket1);
        manager.processReceiveMessage(message);

        long start = System.currentTimeMillis();

        // And the request and make sure the transaction didn't complete
        message = new SerialMessage(responsePacket2);
        manager.processReceiveMessage(message);

        // Wait for the timeout
        Mockito.verify(controller, Mockito.timeout(5000))
                .handleTransactionComplete(transactionCompleteCapture.capture(), serialMessageComplete.capture());

        long duration = System.currentTimeMillis() - start;

        // Check that this transaction completed
        // Note - only test the minimum time in case the execution gets delayed and its a lot longer than expected.
        // It shouldn't be shorter!
        assertTrue(duration > 2490);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.CANCELLED, transactionCompleteCapture.getValue().getTransactionState());
    }

    @Test
    public void TestTransactionType1() {
        SerialMessage message;
        byte[] responsePacket1 = { 0x01, 0x05, 0x01, 0x06, (byte) 0x96, 0x0F, 0x64 };

        // Test transaction with just a RESponse - uses SerialApiSetTimeouts
        ZWaveTransaction transaction = new SerialApiSetTimeoutsMessageClass().doRequest(150, 15);

        ZWaveTransactionManager manager = getTransactionManager();
        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        message = new SerialMessage(responsePacket1);

        // Provide the response
        manager.processReceiveMessage(message);

        // Check that this transaction completed
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.DONE, transactionCompleteCapture.getValue().getTransactionState());
    }

    @Test
    public void TestTransactionType1IncorrectPacketReceived() {
        SerialMessage message;
        byte[] invalidPacket = { 0x01, 0x0A, 0x00, 0x04, 0x00, 0x05, 0x04, 0x73, 0x03, 0x00, 0x00, (byte) 0x80 };
        byte[] responsePacket = { 0x01, 0x05, 0x01, 0x06, (byte) 0x96, 0x0F, 0x64 };

        // Test transaction with just a RESponse - uses SerialApiSetTimeouts
        ZWaveTransaction transaction = new SerialApiSetTimeoutsMessageClass().doRequest(150, 15);

        ZWaveTransactionManager manager = getTransactionManager();
        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Add a response not related to this transaction
        message = new SerialMessage(invalidPacket);
        manager.processReceiveMessage(message);

        // Make sure the transaction didn't complete
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide the response
        message = new SerialMessage(responsePacket);
        manager.processReceiveMessage(message);

        // Check that this transaction completed
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.DONE, transactionCompleteCapture.getValue().getTransactionState());
    }

    @Test
    public void TestTransactionType2() {
        SerialMessage message;
        byte[] responsePacket1 = { 0x01, 0x07, 0x00, 0x4A, 0x01, 0x01, 0x00, 0x00, (byte) 0xB2 };

        // Test transaction with just a REQuest - uses AddNodeMessage
        ZWaveTransaction transaction = new AddNodeMessageClass().doRequestStart(true, true);
        transaction.getSerialMessage().setCallbackId(1);

        ZWaveTransactionManager manager = getTransactionManager();
        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Provide the response
        message = new SerialMessage(responsePacket1);
        manager.processReceiveMessage(message);

        // Check that the transaction completed
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.DONE, transactionCompleteCapture.getValue().getTransactionState());
    }

    @Test
    public void TestTransactionType2Multi() {
        SerialMessage message;
        byte[] responsePacket1 = { 0x01, 0x05, 0x00, 0x48, 0x52, 0x21, (byte) 0xC1 };
        byte[] responsePacket2 = { 0x01, 0x05, 0x00, 0x48, 0x52, 0x22, (byte) 0xC2 };
        byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x02,
                0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        // Test transaction with just a REQuest - uses RequestNodeNeighborUpdate
        ZWaveTransaction transaction = new RequestNodeNeighborUpdateMessageClass().doRequest(12);
        transaction.getSerialMessage().setCallbackId(82);

        ZWaveTransactionManager manager = getTransactionManager();
        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Provide unrelated packet
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);

        // Provide response 1
        message = new SerialMessage(responsePacket1);
        manager.processReceiveMessage(message);

        // Check that the transaction has not completed
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide unrelated packet
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);

        // Check that the transaction has not completed
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide response 2
        message = new SerialMessage(responsePacket2);
        manager.processReceiveMessage(message);

        // Check that the transaction completed
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.DONE, transactionCompleteCapture.getValue().getTransactionState());
    }

    @Test
    public void TestTransactionType3() {
        SerialMessage message;
        byte[] responsePacket1 = { 0x01, 0x04, 0x01, 0x46, 0x01, (byte) 0xBD };
        byte[] responsePacket2 = { 0x01, 0x05, 0x00, 0x46, 0x01, 0x56, 0x00, (byte) 0xEB };

        // Test transaction with just a REQuest - uses RemoveNodeMessage
        ZWaveTransaction transaction = new AssignReturnRouteMessageClass().doRequest(5, 1);
        transaction.getSerialMessage().setCallbackId(1);

        ZWaveTransactionManager manager = getTransactionManager();
        manager.queueTransactionForSend(transaction);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide the request
        message = new SerialMessage(responsePacket1);
        manager.processReceiveMessage(message);

        // Make sure this didn't complete the transaction
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // And the response
        message = new SerialMessage(responsePacket2);
        manager.processReceiveMessage(message);

        // Check that this transaction completed
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.DONE, transactionCompleteCapture.getValue().getTransactionState());
    }

    /**
     * Tests a single type 4 transaction - sending a command class request, and receiving the data from the device.
     */
    @Test
    public void TestTransactionType4() {
        byte[] responsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        byte[] responsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x53, 0x00, 0x00, 0x02, (byte) 0xBA };
        byte[] responsePacket3 = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x05, 0x07, (byte) 0x9C, 0x02, 0x05, 0x01, 0x00, 0x00,
                0x00, 0x6E };
        byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x02,
                0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        // Test transaction
        SerialMessage message = new ZWaveSendDataMessageBuilder().withCommandClass(CommandClass.SENSOR_ALARM, 1)
                .withNodeId(5).withPayload(5, 3, CommandClass.SENSOR_ALARM.getKey(), 1, 1).build();

        ZWaveTransaction transaction = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(CommandClass.SENSOR_ALARM, 2).build();
        transaction.getSerialMessage().setCallbackId(83);

        ZWaveTransactionManager manager = getTransactionManager();
        manager.queueTransactionForSend(transaction);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide the response and make sure the transaction didn't complete
        message = new SerialMessage(responsePacket1);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // And the request and make sure the transaction didn't complete
        message = new SerialMessage(responsePacket2);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // And finally provide the data
        message = new SerialMessage(responsePacket3);
        manager.processReceiveMessage(message);

        // Check that the transaction completed
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.DONE, transactionCompleteCapture.getValue().getTransactionState());
    }

    /**
     * Tests multiple outstanding transactions to different nodes
     * Two transactions are queued to different nodes. The first should be sent immediately, and the second should be
     * sent after the response is received to the first transaction.
     */
    @Test
    public void TestTransactionType4Multi() {
        byte[] t1ResponsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        byte[] t1ResponsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x53, 0x00, 0x00, 0x02, (byte) 0xBA };
        byte[] t1ResponsePacket3 = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x05, 0x07, (byte) 0x9C, 0x02, 0x05, 0x01, 0x00,
                0x00, 0x00, 0x6E };
        byte[] t2ResponsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        byte[] t2ResponsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x08, 0x00, 0x00, 0x02, (byte) 0xE1 };
        byte[] t2ResponsePacket3 = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x02, 0x0E, 0x32, 0x02, 0x21, 0x74, 0x00, 0x00, 0x0E,
                (byte) 0xE7, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x6F };
        byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x02,
                0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        ZWaveTransactionManager manager = getTransactionManager();
        System.out.println("------------------------------------------------------------------------");

        // Start transaction 1
        SerialMessage message = new ZWaveSendDataMessageBuilder().withCommandClass(CommandClass.SENSOR_ALARM, 1)
                .withNodeId(5).withPayload(5, 3, CommandClass.SENSOR_ALARM.getKey(), 1, 1).build();
        ZWaveTransaction transaction1 = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(CommandClass.SENSOR_ALARM, 2).build();
        transaction1.getSerialMessage().setCallbackId(83);

        manager.queueTransactionForSend(transaction1);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Start transaction 2
        message = new ZWaveSendDataMessageBuilder().withCommandClass(CommandClass.METER, 1).withNodeId(2)
                .withPayload(0x10).build();
        ZWaveTransaction transaction2 = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(CommandClass.METER, 2).build();
        transaction2.getSerialMessage().setCallbackId(8);

        manager.queueTransactionForSend(transaction2);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Check that the first frame has been sent
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide the response and make sure the transaction didn't complete
        message = new SerialMessage(t1ResponsePacket1);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // And the request and make sure the transaction didn't complete
        message = new SerialMessage(t1ResponsePacket2);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // The second transaction should be sent now
        assertEquals(2, txQueueCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // And finally provide the data
        message = new SerialMessage(t1ResponsePacket3);
        manager.processReceiveMessage(message);

        // Check that transaction 1 completed
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(83, transactionCompleteCapture.getValue().getCallbackId());
        assertEquals(TransactionState.DONE, transactionCompleteCapture.getValue().getTransactionState());

        // And feed in all the second transaction packets
        message = new SerialMessage(t2ResponsePacket1);
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        message = new SerialMessage(t2ResponsePacket2);
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        System.out.println("     CHECK------------------------------------------------------------------------");

        message = new SerialMessage(t2ResponsePacket3);
        manager.processReceiveMessage(message);

        System.out.println("------------------------------------------------------------------------");
        assertEquals(2, transactionCompleteCapture.getAllValues().size());
    }

    /**
     * Tests multiple outstanding transactions to the same node
     * Two transactions are queued to the same node. The first should be sent immediately, and the second should be
     * sent after the data response is received to the first transaction.
     * We use the controller as the node here.
     */
    @Ignore
    @Test
    public void TestTransactionType4MultiSameNode() {
        byte[] t1ResponsePacket1 = { 0x01, 0x05, 0x00, 0x48, 0x48, 0x21, (byte) 0xDB };
        byte[] t1ResponsePacket2 = { 0x01, 0x05, 0x00, 0x48, 0x48, 0x22, (byte) 0xD8 };
        byte[] t2ResponsePacket1 = { 0x01, 0x05, 0x00, 0x48, 0x56, 0x21, (byte) 0xC5 };
        byte[] t2ResponsePacket2 = { 0x01, 0x05, 0x00, 0x48, 0x56, 0x22, (byte) 0xC6 };
        byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x02,
                0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        ZWaveTransactionManager manager = getTransactionManager();
        System.out.println("------------------------------------------------------------------------");

        // Start transaction 1
        SerialMessage message = new ZWaveMessageBuilder(SerialMessageClass.RequestNodeNeighborUpdate).withPayload(2)
                .build();

        ZWaveTransaction transaction1 = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.RequestNodeNeighborUpdate)
                .withPriority(TransactionPriority.High).build();
        transaction1.getSerialMessage().setCallbackId(72);

        manager.queueTransactionForSend(transaction1);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Start transaction 2
        message = new ZWaveMessageBuilder(SerialMessageClass.RequestNodeNeighborUpdate).withPayload(5).build();

        ZWaveTransaction transaction2 = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.RequestNodeNeighborUpdate)
                .withPriority(TransactionPriority.High).build();
        transaction2.getSerialMessage().setCallbackId(86);

        manager.queueTransactionForSend(transaction2);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Check that the first frame has been sent
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide the response
        message = new SerialMessage(t1ResponsePacket1);
        manager.processReceiveMessage(message);

        // Check that only the first frame has been sent and make sure the transaction didn't complete
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);

        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // And the request
        message = new SerialMessage(t1ResponsePacket2);
        manager.processReceiveMessage(message);

        // Check that transaction 1 completed
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(0x48, transactionCompleteCapture.getValue().getCallbackId());
        assertEquals(TransactionState.DONE, transactionCompleteCapture.getValue().getTransactionState());

        // The second transaction should now have been sent
        assertEquals(2, txQueueCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        // And feed in all the second transaction packets
        message = new SerialMessage(t2ResponsePacket1);
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        message = new SerialMessage(t2ResponsePacket2);
        manager.processReceiveMessage(message);

        assertEquals(2, transactionCompleteCapture.getAllValues().size());
    }

    @Test
    public void TestThreadLocking() {
        // This sends multiple messages from different threads to make sure only a single message is sent
        // Really, this is a horrible test as it's all about timing and it probably should be removed...
        // Leave it here for now though - it does do its job - occasionally!

        // Test transaction with just a RESponse - uses SerialApiSetTimeouts
        final ZWaveTransactionManager manager = getTransactionManager();

        int threadCnt = 10;
        List<Thread> threads = new ArrayList<Thread>();
        for (int x = 0; x < threadCnt; x++) {
            final int c = x;
            threads.add(new Thread() {
                @Override
                public void run() {
                    ZWaveTransaction transaction = new SerialApiSetTimeoutsMessageClass().doRequest(c, 1);
                    manager.queueTransactionForSend(transaction);
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }

        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check that only one frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(threadCnt - 1, manager.getSendQueueLength());
    }

    /**
     * Tests the binding initialisation sequence
     */
    @Test
    public void TestInitialization() {
        byte[] responsePacket1 = { 0x01, 0x10, 0x01, 0x15, 0x5A, 0x2D, 0x57, 0x61, 0x76, 0x65, 0x20, 0x33, 0x2E, 0x39,
                0x35, 0x00, 0x01, (byte) 0x99 };
        byte[] responsePacket2 = { 0x01, 0x08, 0x01, 0x20, (byte) 0xCB, 0x16, 0x2E, (byte) 0xDE, 0x01, (byte) 0xFA };

        SerialMessage message;
        ZWaveTransactionManager manager = getTransactionManager();
        System.out.println("------------------------------------------------------------------------");

        manager.queueTransactionForSend(new GetVersionMessageClass().doRequest());
        manager.queueTransactionForSend(new MemoryGetIdMessageClass().doRequest());
        manager.queueTransactionForSend(new SerialApiGetCapabilitiesMessageClass().doRequest());
        manager.queueTransactionForSend(new SerialApiSetTimeoutsMessageClass().doRequest(150, 15));
        manager.queueTransactionForSend(new GetSucNodeIdMessageClass().doRequest());

        // Make sure only 1 frame is transmitted so far and no transactions are complete yet
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Send the response to the version request
        message = new SerialMessage(responsePacket1);
        manager.processReceiveMessage(message);

        // Make sure a second frame is transmitted and 1 transactions is complete
        assertEquals(2, txQueueCapture.getAllValues().size());
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        // Send the response to the memory get request
        message = new SerialMessage(responsePacket2);
        manager.processReceiveMessage(message);

        // Make sure a third frame is transmitted and 2 transactions are complete
        assertEquals(3, txQueueCapture.getAllValues().size());
        assertEquals(2, transactionCompleteCapture.getAllValues().size());
    }

    /**
     * Tests a specific failure sequence
     * A METER request is sent. Once the ACK is received a PING is sent to a different node
     * and the METER reading is received between responses
     */
    @Test
    public void TestPingFailure1() {
        byte[] responsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        byte[] responsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x0A, 0x00, 0x00, 0x09, (byte) 0xE8 };
        byte[] responsePacket3 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        byte[] responsePacket4 = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x0D, 0x0E, 0x32, 0x02, 0x21, 0x44, 0x00, 0x00, 0x00,
                (byte) 0xAF, 0x09, (byte) 0xF7, 0x00, 0x00, 0x00, (byte) 0xAF, 0x47 };
        byte[] responsePacket5 = { 0x01, 0x07, 0x00, 0x13, 0x08, 0x00, 0x00, 0x04, (byte) 0xE7 };

        ZWaveTransactionManager manager = getTransactionManager();
        System.out.println("TestPingFailure ------------------------------------------------------------------------");

        // Queue transaction 1 (METER)
        SerialMessage message = new ZWaveSendDataMessageBuilder().withCommandClass(CommandClass.METER, 1).withNodeId(13)
                .withPayload(0x00).build();
        ZWaveTransaction transaction1 = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(CommandClass.METER, 2).build();
        transaction1.getSerialMessage().setCallbackId(10);

        // Queue transaction 2 (PING)
        message = new ZWaveSendDataMessageBuilder().withCommandClass(CommandClass.NO_OPERATION, 1).withNodeId(4)
                .build();
        ZWaveTransaction transaction2 = new ZWaveTransactionBuilder(message).build();
        transaction2.getSerialMessage().setCallbackId(8);

        manager.queueTransactionForSend(transaction1);
        manager.queueTransactionForSend(transaction2);

        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Send the response to the METER
        message = new SerialMessage(responsePacket1);
        manager.processReceiveMessage(message);

        // Send the request to the METER
        message = new SerialMessage(responsePacket2);
        manager.processReceiveMessage(message);

        // Make sure the transaction didn't complete
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // PING should now have been sent
        assertEquals(2, txQueueCapture.getAllValues().size());

        // Send the response to the PING
        message = new SerialMessage(responsePacket3);
        manager.processReceiveMessage(message);

        // Send the METER packet
        message = new SerialMessage(responsePacket4);
        manager.processReceiveMessage(message);

        // Make sure the transaction completes
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        // Send the request to the PING
        message = new SerialMessage(responsePacket5);
        manager.processReceiveMessage(message);

        // Make sure the transaction completes
        assertEquals(2, transactionCompleteCapture.getAllValues().size());
    }

    @Test
    public void TestSendDataTransactionNakWithoutRequest() {
        SerialMessage message;
        byte[] responsePacket1 = { 0x01, 0x07, 0x00, 0x13, 0x0E, 0x01, 0x01, 0x20, (byte) 0xC5 };

        ZWaveTransactionManager manager = getTransactionManager();

        message = new SerialMessage(responsePacket1);

        // Provide the response
        manager.processReceiveMessage(message);

        // Check that no transaction completed
        assertEquals(0, transactionCompleteCapture.getAllValues().size());
    }

    @Test
    public void TestTimeoutRestarts() {
        ZWaveTransactionManager manager = getTransactionManagerForTimeout();

        // Test transaction requiring a RESponse - uses SerialApiSetTimeouts
        ZWaveTransaction transaction = new SerialApiSetTimeoutsMessageClass().doRequest(150, 15);
        transaction.setAttemptsRemaining(3);

        long start = System.currentTimeMillis();
        manager.queueTransactionForSend(transaction);

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Wait for the timeout - this should be 3xTimer1 (1500ms total)
        Mockito.verify(controller, Mockito.timeout(15000))
                .handleTransactionComplete(transactionCompleteCapture.capture(), serialMessageComplete.capture());

        long duration = System.currentTimeMillis() - start;

        // Check that this transaction completed and we have 3 messages sent.
        // Note - only test the minimum time in case the execution gets delayed and its a lot longer than expected.
        // It shouldn't be shorter!
        assertTrue(duration > 1500);
        assertEquals(3, txQueueCapture.getAllValues().size());
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.CANCELLED, transactionCompleteCapture.getValue().getTransactionState());
    }

    /**
     * Tests multiple outstanding transactions to different nodes
     * Two transactions are queued to different nodes. The first should be sent immediately, and the second should be
     * sent after the response is received to the first transaction.
     * We don't send the responses for the second, and the retry timer should pick it up.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void TestTransactionType4MultiTimeout() {
        byte[] t1ResponsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        byte[] t1ResponsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x53, 0x00, 0x00, 0x02, (byte) 0xBA };
        byte[] t1ResponsePacket3 = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x05, 0x07, (byte) 0x9C, 0x02, 0x05, 0x01, 0x00,
                0x00, 0x00, 0x6E };
        byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00, 0x02,
                0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        ZWaveTransactionManager manager = getTransactionManagerForTimeout();
        System.out.println("------------------------------------------------------------------------");

        // Start transaction 1
        SerialMessage message = new ZWaveSendDataMessageBuilder().withCommandClass(CommandClass.SENSOR_ALARM, 1)
                .withNodeId(5).withPayload(5, 3, CommandClass.SENSOR_ALARM.getKey(), 1, 1).build();
        ZWaveTransaction transaction1 = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(CommandClass.SENSOR_ALARM, 2).build();
        transaction1.getSerialMessage().setCallbackId(83);
        transaction1.setAttemptsRemaining(3);

        manager.queueTransactionForSend(transaction1);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Start transaction 2
        message = new ZWaveSendDataMessageBuilder().withCommandClass(CommandClass.METER, 1).withNodeId(2)
                .withPayload(0x10).build();
        ZWaveTransaction transaction2 = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(CommandClass.METER, 2).build();
        transaction2.getSerialMessage().setCallbackId(8);
        transaction2.setAttemptsRemaining(3);

        manager.queueTransactionForSend(transaction2);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Check that the first frame has been sent
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide the response and make sure the transaction didn't complete
        message = new SerialMessage(t1ResponsePacket1);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Timer to check retry.
        long start = System.currentTimeMillis();

        // And the request and make sure the transaction didn't complete
        message = new SerialMessage(t1ResponsePacket2);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // The second transaction should be sent now
        assertEquals(2, txQueueCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // And finally provide the data for the first transaction
        message = new SerialMessage(t1ResponsePacket3);
        manager.processReceiveMessage(message);

        // Mockito.verify(controller, Mockito.timeout(15000).times(1))
        // .handleTransactionComplete(transactionCompleteCapture.capture(), serialMessageComplete.capture());

        // Check that transaction 1 completed
        // assertEquals(1, transactionCompleteCapture.getAllValues().size());
        // assertEquals(83, transactionCompleteCapture.getValue().getCallbackId());
        // assertEquals(TransactionState.DONE, transactionCompleteCapture.getValue().getTransactionState());

        //
        // serialMessageComplete = ArgumentCaptor.forClass(SerialMessage.class);
        // transactionCompleteCapture = ArgumentCaptor.forClass(ZWaveTransaction.class);

        System.out.println("------------------------------------------------------------------------");

        // Wait for the timeout - this should be 3xTimer1 (1500ms total)
        Mockito.verify(controller, Mockito.timeout(15000).times(2))
                .handleTransactionComplete(transactionCompleteCapture.capture(), serialMessageComplete.capture());

        System.out.println("------------------------------------------------------------------------");
        System.out.println("callbackid=" + transactionCompleteCapture.getValue().getCallbackId());

        long duration = System.currentTimeMillis() - start;

        // Check that this transaction completed and we have 3 messages sent.
        // Note - only test the minimum time in case the execution gets delayed and its a lot longer than expected.
        // It shouldn't be shorter!
        assertEquals(4, txQueueCapture.getAllValues().size());
        assertEquals(2, transactionCompleteCapture.getAllValues().size());
        assertEquals(TransactionState.DONE, transactionCompleteCapture.getAllValues().get(0).getTransactionState());
        assertEquals(TransactionState.CANCELLED,
                transactionCompleteCapture.getAllValues().get(1).getTransactionState());
        assertTrue(duration > 1500);
        assertTrue(duration < 5000);
    }

}
