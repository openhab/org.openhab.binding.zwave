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
