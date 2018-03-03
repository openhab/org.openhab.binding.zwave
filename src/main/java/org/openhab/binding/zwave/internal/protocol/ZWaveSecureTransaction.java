/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

/**
 *
 * @author Chris Jackson
 *
 */
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
