/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.concurrent.TimeUnit;

/**
 * ZWaveEvent to notify listeners that a node needs to be polled
 * for its value at some future time.
 * 
 * @author Dan Cunningham
 *
 */
public class ZWaveDelayedPollEvent extends ZWaveEvent {

    private long delay;
    private TimeUnit unit;

    public ZWaveDelayedPollEvent(int nodeId, int endpoint, long delay, TimeUnit unit) {
        super(nodeId, endpoint);
        this.delay = delay;
        this.unit = unit;
    }

    public ZWaveDelayedPollEvent(int nodeId, long delay, TimeUnit unit) {
        super(nodeId);
        this.delay = delay;
        this.unit = unit;
    }

    public long getDelay() {
        return delay;
    }

    public TimeUnit getUnit() {
        return unit;
    }
}
