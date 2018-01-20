/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
