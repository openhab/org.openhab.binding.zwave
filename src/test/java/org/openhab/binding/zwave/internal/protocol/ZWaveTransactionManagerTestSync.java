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

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionState;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionManager;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionResponse;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeInfoMessageClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;

public class ZWaveTransactionManagerTestSync extends ZWaveTransactionManagerTest {
    ZWaveTransactionResponse response1;
    ZWaveTransactionResponse response2;

    void setResponse1(ZWaveTransactionResponse response) {
        this.response1 = response;
    };

    void setResponse2(ZWaveTransactionResponse response) {
        this.response2 = response;
    };

    /**
     * Tests a single type 4 transaction - sending a command class request, and receiving the data from the device.
     */
    @Ignore
    @Test
    public void TestTransactionType4() {
        final byte[] responsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        final byte[] responsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x53, 0x00, 0x00, 0x02, (byte) 0xBA };
        final byte[] responsePacket3 = { 0x01, 0x0D, 0x00, 0x04, 0x00, 0x05, 0x07, (byte) 0x9C, 0x02, 0x05, 0x01, 0x00,
                0x00, 0x00, 0x6E };
        final byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00,
                0x02, 0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        // Test transaction
        final ZWaveCommandClassTransactionPayload payload = new ZWaveCommandClassTransactionPayloadBuilder(5,
                CommandClass.COMMAND_CLASS_SENSOR_ALARM, 1)
                        .withPayload(5, 3, CommandClass.COMMAND_CLASS_SENSOR_ALARM.getKey(), 1, 1)
                        .withExpectedResponseCommand(2).build();

        final ZWaveTransactionManager manager = getTransactionManager();

        response1 = null;
        Thread syncThread = new Thread() {
            @Override
            public void run() {
                // Send the transaction and wait for the response
                setResponse1(manager.sendTransaction(payload));
            }
        };

        Thread messageThread = new Thread() {
            @Override
            public void run() {
                // Give some time to ensure the sync thread is running
                try {
                    Thread.sleep(400);
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

        assertNotNull(response1);
        assertEquals(ZWaveTransactionResponse.State.COMPLETE, response1.getState());
    }

    /**
     * Tests a single type 4 transaction - sending a command class request, and receiving the data from the device.
     */
    @Ignore
    @Test
    public void TestTransactionType4Timeout() {
        final byte[] responsePacket1 = { 0x01, 0x04, 0x01, 0x13, 0x01, (byte) 0xE8 };
        final byte[] responsePacket2 = { 0x01, 0x07, 0x00, 0x13, 0x53, 0x00, 0x00, 0x02, (byte) 0xBA };
        final byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00,
                0x02, 0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        // Test transaction
        final ZWaveCommandClassTransactionPayload payload = new ZWaveCommandClassTransactionPayloadBuilder(5,
                CommandClass.COMMAND_CLASS_SENSOR_ALARM, 1)
                        .withPayload(5, 3, CommandClass.COMMAND_CLASS_SENSOR_ALARM.getKey(), 1, 1)
                        .withExpectedResponseCommand(2).build();

        final ZWaveTransactionManager manager = getTransactionManager();

        response1 = null;
        Thread syncThread = new Thread() {
            @Override
            public void run() {
                // Send the transaction and wait for the response
                setResponse1(manager.sendTransaction(payload));
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

        assertNotNull(response1);
        assertEquals(ZWaveTransactionResponse.State.TIMEOUT_WAITING_FOR_RESPONSE, response1.getState());
    }

    @Ignore
    @Test
    public void TestMultipleNIFRequests() {
        final byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00,
                0x02, 0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        final ZWaveTransactionManager manager = getTransactionManager();

        response1 = null;
        Thread syncThread1 = new Thread() {
            @Override
            public void run() {
                // Send the transaction and wait for the response
                setResponse1(manager.sendTransaction(new RequestNodeInfoMessageClass().doRequest(2)));
            }
        };

        response2 = null;
        Thread syncThread2 = new Thread() {
            @Override
            public void run() {
                // Send the transaction and wait for the response
                setResponse2(manager.sendTransaction(new RequestNodeInfoMessageClass().doRequest(13)));
            }
        };

        syncThread1.start();
        // Give time for the first transaction to send - to be sure we know which one started
        Mockito.verify(controller, Mockito.timeout(5000).times(1)).sendPacket(Matchers.any(SerialMessage.class));
        syncThread2.start();

        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide the response and make sure the transaction didn't complete
        SerialMessage message = new SerialMessage(new byte[] { 0x01, 0x04, 0x01, 0x60, 0x01, (byte) 0x9B });
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());
        assertEquals(1, txQueueCapture.getAllValues().size());

        // And the NIF and make sure the transaction completed
        message = new SerialMessage(new byte[] { 0x01, 0x14, 0x00, 0x49, (byte) 0x84, 0x02, 0x0E, 0x04, 0x11, 0x01,
                0x72, (byte) 0x86, 0x70, (byte) 0x85, (byte) 0x8E, 0x26, 0x27, 0x73, (byte) 0xEF, 0x20, 0x26, 0x2A });
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        // The second transaction should now send
        assertEquals(2, txQueueCapture.getAllValues().size());

        // Provide the response and make sure the transaction didn't complete
        message = new SerialMessage(new byte[] { 0x01, 0x04, 0x01, 0x60, 0x01, (byte) 0x9B });
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        // And the a NIF for the wrong node
        message = new SerialMessage(new byte[] { 0x01, 0x14, 0x00, 0x49, (byte) 0x84, 0x02, 0x0E, 0x04, 0x11, 0x01,
                0x72, (byte) 0x86, 0x70, (byte) 0x85, (byte) 0x8E, 0x26, 0x27, 0x73, (byte) 0xEF, 0x20, 0x26, 0x2A });
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());

        // And the request and make sure the transaction completed
        message = new SerialMessage(new byte[] { 0x01, 0x11, 0x00, 0x49, (byte) 0x84, 0x0D, 0x0B, 0x04, 0x10, 0x01,
                0x25, 0x20, 0x27, 0x72, (byte) 0x86, 0x32, (byte) 0x85, 0x70, 0x21 });
        manager.processReceiveMessage(message);
        assertEquals(2, transactionCompleteCapture.getAllValues().size());

        try {
            syncThread1.join(20000);
            syncThread2.join(20000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        assertNotNull(response1);
        assertEquals(ZWaveTransactionResponse.State.COMPLETE, response1.getState());
        assertNotNull(response2);
        assertEquals(ZWaveTransactionResponse.State.COMPLETE, response2.getState());
    }

}
