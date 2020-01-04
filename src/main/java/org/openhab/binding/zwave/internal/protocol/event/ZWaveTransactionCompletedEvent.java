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
package org.openhab.binding.zwave.internal.protocol.event;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;

/**
 * ZWave Transaction Completed Event. Indicated that a transaction (a sequence of messages with an expected reply) has
 * completed.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveTransactionCompletedEvent extends ZWaveEvent {

    private final ZWaveTransaction transaction;
    private final SerialMessage responseMessage;
    private final boolean state;

    /**
     * Constructor. Creates a new instance of the ZWaveTransactionCompletedEvent class.
     * The 'state' flag is provided by the message handler when the message is processed and its value is defined by the
     * message class.
     *
     * @param currentTransaction the original {@link SerialMessage} that has been completed.
     * @param state a flag indicating success / failure of the transaction processing
     */
    public ZWaveTransactionCompletedEvent(ZWaveTransaction transaction, SerialMessage responseMessage, boolean state) {
        super(transaction.getNodeId());

        this.transaction = transaction;
        this.responseMessage = responseMessage;
        this.state = state;
    }

    /**
     * Gets the original {@link SerialMessage} that has been completed.
     *
     * @return the original message.
     */
    public ZWaveTransaction getCompletedTransaction() {
        return transaction;
    }

    /**
     * Gets the message used to complete the transaction
     *
     * @return
     */
    public SerialMessage getResponseMessage() {
        return responseMessage;
    }

    /**
     * Returns the processing state of this transaction
     *
     * @return
     */
    public boolean getState() {
        return state;
    }
}
