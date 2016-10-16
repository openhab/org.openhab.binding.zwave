package org.openhab.binding.zwave.internal.protocol;

public interface ZWaveMessagePayloadTransaction extends ZWaveMessagePayload {
    public int getMaxAttempts();

    public int getDestinationNode();

    public SerialMessage getSerialMessage();
}
