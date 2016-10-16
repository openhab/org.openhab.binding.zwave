package org.openhab.binding.zwave.internal.protocol.transaction;

public class ZWaveCommandClassTransaction {
    private int nodeId;
    private ZWaveCommandClassTransactionPayload payload;

    public ZWaveCommandClassTransaction(ZWaveCommandClassTransactionPayload payload) {
        // super(SerialMessageClass.SendData, payload.getPriority(), 3);

        this.payload = payload;
    }

}
