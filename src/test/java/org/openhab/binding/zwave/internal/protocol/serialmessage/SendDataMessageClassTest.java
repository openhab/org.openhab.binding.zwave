package org.openhab.binding.zwave.internal.protocol.serialmessage;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SendDataMessageClass;

/**
 * Test cases for SendDataMessageClass message.
 * This takes some example packets, processes them, and checks that the processing is correct.
 *
 * @author Chris Jackson
 *
 */
public class SendDataMessageClassTest extends ZWaveCommandProcessorTest {
    @Test
    public void doRequest() {
        byte[] responseData = { 0x01, 0x07, 0x00, 0x13, 0x0E, 0x01, 0x01, 0x20, (byte) 0xC5 };

        SendDataMessageClass handler = new SendDataMessageClass();
        // SerialMessage msg = new SerialMessage(responseData);

        ProcessRequest(handler, null, responseData);
        // handler.handleRequest(zController, transaction, msg);

        // assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }
}
