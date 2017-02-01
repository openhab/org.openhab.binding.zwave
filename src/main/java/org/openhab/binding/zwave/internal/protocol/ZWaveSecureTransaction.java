package org.openhab.binding.zwave.internal.protocol;

public class ZWaveSecureTransaction extends ZWaveTransaction {
    private ZWaveTransaction linkedTransaction;

    public ZWaveSecureTransaction(ZWaveTransaction transaction, ZWaveMessagePayloadTransaction payload) {
        super(payload);

        linkedTransaction = transaction;
    }

    public void setLinkedTransaction(ZWaveTransaction transaction) {
        linkedTransaction = transaction;
    }

    public ZWaveTransaction getLinkedTransaction() {
        return linkedTransaction;
    }
}
