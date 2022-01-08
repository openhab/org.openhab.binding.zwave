/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;

/**
 * Node status event is used to signal if a node is alive or dead
 *
 * @author Chris Jackson
 */
public class ZWaveNodeStatusEvent extends ZWaveEvent {
    ZWaveNodeState state;
    ZWaveNodeInitStage stage = null;

    /**
     * Constructor. Creates a new instance of the ZWaveNetworkEvent class.
     *
     * @param nodeId the nodeId of the event.
     */
    public ZWaveNodeStatusEvent(int nodeId, ZWaveNodeState state) {
        super(nodeId);

        this.state = state;
    }

    public ZWaveNodeStatusEvent(int nodeId, ZWaveNodeState state, ZWaveNodeInitStage stage) {
        super(nodeId);

        this.state = state;
        this.stage = stage;
    }

    public ZWaveNodeState getState() {
        return state;
    }

    public ZWaveNodeInitStage getStage() {
        return stage;
    }
}
