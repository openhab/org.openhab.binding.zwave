package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionState;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionManager;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionResponse;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

public class ZWaveTransactionManagerTestSync extends ZWaveTransactionManagerTest {
    ZWaveTransactionResponse response;

    void setResponse(ZWaveTransactionResponse response) {
        this.response = response;
    };

    /**
     * Tests a single type 4 transaction - sending a command class request, and receiving the data from the device.
     */
    @Test
    public void TestTransactionType4() {
        final byte[] responsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        final byte[] responsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x53, 0x00, 0x00, 0x02, (byte) 0xBA };
        final byte[] responsePacket3 = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x05, 0x07, (byte) 0x9C, 0x02, 0x05, 0x01, 0x00,
                0x00, 0x00, 0x6E };
        final byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00,
                0x02, 0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        // Test transaction
        SerialMessage message = new ZWaveSendDataMessageBuilder()
                .withCommandClass(CommandClass.COMMAND_CLASS_SENSOR_ALARM, 1).withNodeId(5)
                .withPayload(5, 3, CommandClass.COMMAND_CLASS_SENSOR_ALARM.getKey(), 1, 1).build();

        final ZWaveTransaction transaction = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(CommandClass.COMMAND_CLASS_SENSOR_ALARM, 2).build();
        transaction.getSerialMessage().setCallbackId(83);

        final ZWaveTransactionManager manager = getTransactionManager();

        response = null;
        Thread syncThread = new Thread() {
            @Override
            public void run() {
                // Send the transaction and wait for the response
                setResponse(manager.SendTransaction(transaction));
                System.out.println("Done....................................");
            }
        };

        Thread messageThread = new Thread() {
            @Override
            public void run() {
                // Give some time to ensure the sync thread is running
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Check that this frame was sent
                // assertEquals(1, txQueueCapture.getAllValues().size());
                // assertEquals(0, transactionCompleteCapture.getAllValues().size());

                // Provide the response and make sure the transaction didn't complete
                SerialMessage message = new SerialMessage(responsePacket1);
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
        };

        try {
            syncThread.start();
            messageThread.start();
            syncThread.join(20000);
            messageThread.join(100);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        assertNotNull(response);
        assertEquals(ZWaveTransactionResponse.State.COMPLETE, response.getState());
    }

    /**
     * Tests a single type 4 transaction - sending a command class request, and receiving the data from the device.
     */
    @Test
    public void TestTransactionType4Timeout() {
        final byte[] responsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        final byte[] responsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x53, 0x00, 0x00, 0x02, (byte) 0xBA };
        final byte[] responsePacket3 = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x05, 0x07, (byte) 0x9C, 0x02, 0x05, 0x01, 0x00,
                0x00, 0x00, 0x6E };
        final byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00,
                0x02, 0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        // Test transaction
        SerialMessage message = new ZWaveSendDataMessageBuilder()
                .withCommandClass(CommandClass.COMMAND_CLASS_SENSOR_ALARM, 1).withNodeId(5)
                .withPayload(5, 3, CommandClass.COMMAND_CLASS_SENSOR_ALARM.getKey(), 1, 1).build();

        final ZWaveTransaction transaction = new ZWaveTransactionBuilder(message)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(CommandClass.COMMAND_CLASS_SENSOR_ALARM, 2).build();
        transaction.getSerialMessage().setCallbackId(83);

        final ZWaveTransactionManager manager = getTransactionManager();

        response = null;
        Thread syncThread = new Thread() {
            @Override
            public void run() {
                // Send the transaction and wait for the response
                setResponse(manager.SendTransaction(transaction));
                System.out.println("Done....................................");
            }
        };

        Thread messageThread = new Thread() {
            @Override
            public void run() {
                // Give some time to ensure the sync thread is running
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Check that this frame was sent
                // assertEquals(1, txQueueCapture.getAllValues().size());
                // assertEquals(0, transactionCompleteCapture.getAllValues().size());

                // Provide the response and make sure the transaction didn't complete
                SerialMessage message = new SerialMessage(responsePacket1);
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
            }
        };

        try {
            syncThread.start();
            messageThread.start();
            syncThread.join(20000);
            messageThread.join(100);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        assertNotNull(response);
        assertEquals(ZWaveTransactionResponse.State.TIMEOUT_WAITING_FOR_RESPONSE, response.getState());
    }

}
