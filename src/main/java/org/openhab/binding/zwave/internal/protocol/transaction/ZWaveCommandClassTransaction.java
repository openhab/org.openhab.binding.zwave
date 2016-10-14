package org.openhab.binding.zwave.internal.protocol.transaction;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;

public class ZWaveCommandClassTransaction extends ZWaveTransaction {
    private int nodeId;
    private ZWaveCommandClassTransactionPayload payload;

    public ZWaveCommandClassTransaction(ZWaveCommandClassTransactionPayload payload) {
        super(SerialMessageClass.SendData, payload.getPriority(), 3);

        this.payload = payload;
    }

}
