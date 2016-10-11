package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionManager;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeInfoMessageClass;

public class ZWaveTransactionManagerTest_RequestNodeInfo extends ZWaveTransactionManagerTest {
    @Test
    public void TestMultipleNIFRequests() {
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

        manager.queueTransactionForSend(new RequestNodeInfoMessageClass().doRequest(2));
        // Check that this frame was sent
        assertEquals(1, txQueueCapture.getAllValues().size());
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        // Provide the response and make sure the transaction didn't complete
        message = new SerialMessage(new byte[] { 0x01, 0x04, 0x01, 0x60, 0x01, (byte) 0x9B });
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());
        assertEquals(1, txQueueCapture.getAllValues().size());

        // Throw in an unrelated packet and make sure it doesn't impact the transaction
        message = new SerialMessage(unrelatedPacket);
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());
        assertEquals(1, txQueueCapture.getAllValues().size());

        System.out.println("About to send data for 11111 ----------");

        // And the a NIF for the wrong node
        message = new SerialMessage(new byte[] { 0x01, 0x11, 0x00, 0x49, (byte) 0x84, 0x0D, 0x0B, 0x04, 0x10, 0x01,
                0x25, 0x20, 0x27, 0x72, (byte) 0x86, 0x32, (byte) 0x85, 0x70, 0x21 });
        manager.processReceiveMessage(message);
        assertEquals(0, transactionCompleteCapture.getAllValues().size());

        System.out.println("About to REAL NIF data for 22222 ----------");

        // And the NIF and make sure the transaction completed
        message = new SerialMessage(new byte[] { 0x01, 0x14, 0x00, 0x49, (byte) 0x84, 0x02, 0x0E, 0x04, 0x11, 0x01,
                0x72, (byte) 0x86, 0x70, (byte) 0x85, (byte) 0x8E, 0x26, 0x27, 0x73, (byte) 0xEF, 0x20, 0x26, 0x2A });
        manager.processReceiveMessage(message);
        assertEquals(1, transactionCompleteCapture.getAllValues().size());
    }

}
