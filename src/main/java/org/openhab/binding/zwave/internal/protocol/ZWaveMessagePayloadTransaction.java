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
