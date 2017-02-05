package org.openhab.binding.zwave.internal.protocol;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public interface ZWaveMessagePayloadTransaction extends ZWaveMessagePayload {
    public int getMaxAttempts();

    public void setMaxAttempts(int maxAttempts);

    public int getDestinationNode();

    public int getTimeout();

    public SerialMessageClass getSerialMessageClass();

    public SerialMessageClass getExpectedResponseSerialMessageClass();

    public SerialMessage getSerialMessage();

    public TransactionPriority getPriority();

    public boolean requiresData();
}
