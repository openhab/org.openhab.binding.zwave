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

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionManager;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeInfoMessageClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;

public class ZWaveTransactionManagerTest_RequestNodeInfo extends ZWaveTransactionManagerTest {
    @Ignore
    @Test
    public void TestMultipleNIFRequests() {
        final byte[] unrelatedPacket = { 0x01, 0x14, 0x00, 0x04, 0x00, 0x2C, 0x0E, 0x32, 0x02, 0x21, 0x34, 0x00, 0x00,
                0x02, 0x6D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x87 };

        // Test transaction
        ZWaveCommandClassTransactionPayload payload = new ZWaveCommandClassTransactionPayloadBuilder(5,
                CommandClass.COMMAND_CLASS_SENSOR_ALARM, 1)
                        .withPayload(5, 3, CommandClass.COMMAND_CLASS_SENSOR_ALARM.getKey(), 1, 1)
                        .withExpectedResponseCommand(2).build();

        final ZWaveTransactionManager manager = getTransactionManager();

        manager.queueTransactionForSend(new RequestNodeInfoMessageClass().doRequest(2));
        manager.queueTransactionForSend(payload);

        // Check that this frame was sent
        assertEquals(0, transactionCompleteCapture.getAllValues().size());
        assertEquals(1, txQueueCapture.getAllValues().size());

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

        // And the a NIF for the wrong node
        message = new SerialMessage(new byte[] { 0x01, 0x11, 0x00, 0x49, (byte) 0x84, 0x0D, 0x0B, 0x04, 0x10, 0x01,
                0x25, 0x20, 0x27, 0x72, (byte) 0x86, 0x32, (byte) 0x85, 0x70, 0x21 });
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());
        assertEquals(1, txQueueCapture.getAllValues().size());

        // And the NIF and make sure the transaction completed
        message = new SerialMessage(new byte[] { 0x01, 0x14, 0x00, 0x49, (byte) 0x84, 0x02, 0x0E, 0x04, 0x11, 0x01,
                0x72, (byte) 0x86, 0x70, (byte) 0x85, (byte) 0x8E, 0x26, 0x27, 0x73, (byte) 0xEF, 0x20, 0x26, 0x2A });
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
        assertEquals(2, txQueueCapture.getAllValues().size());
    }

}
