package org.openhab.binding.zwave.test.internal.protocol.serialmessage;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNetworkUpdateMessageClass;

public class RequestNetworkUpdateMessageClassTest {
    @Test
    public void doRequest() {
        RequestNetworkUpdateMessageClass handler = new RequestNetworkUpdateMessageClass();
        ZWaveSerialPayload msg = handler.doRequest();
        assertEquals(msg.getSerialMessageClass(), SerialMessageClass.RequestNetworkUpdate);
        assertNull(msg.getPayloadBuffer());
    }
}
