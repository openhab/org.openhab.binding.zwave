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

import java.util.Comparator;

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZWaveTransactionComparator implements Comparator<ZWaveTransaction> {

    /**
     * Compares a serial message to another serial message.
     * Used by the priority queue to order messages.
     *
     * @param arg0 the first serial message to compare the other to.
     * @param arg1 the other serial message to compare the first one to.
     */
    @Override
    public int compare(ZWaveTransaction arg0, ZWaveTransaction arg1) {
        if (arg0.getPriority().equals(arg1.getPriority())) {
            return Long.compare(arg0.getTransactionId(), arg1.getTransactionId());
        }
        return arg0.getPriority().compareTo(arg1.getPriority());
    }
}
