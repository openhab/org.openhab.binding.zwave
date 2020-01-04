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

/**
 * ZWave Network state event. Used to notify the system that the network is available
 *
 * @author Chris Jackson
 */
public class ZWaveNetworkStateEvent extends ZWaveEvent {
    private boolean networkState;

    /**
     * Constructor. Creates a new instance of the ZWaveInitializationStateEvent class.
     *
     * @param nodeId the nodeId of the event. Must be set to the controller node.
     */
    public ZWaveNetworkStateEvent(boolean state) {
        super(0);
        this.networkState = state;
    }

    /**
     * Returns the current state for the network
     *
     * @return node stage
     */
    public boolean getNetworkState() {
        return networkState;
    }
}
