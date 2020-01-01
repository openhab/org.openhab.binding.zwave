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

import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;

/**
 * ZWave Network initialization state event.
 *
 * @author Chris Jackson
 */
public class ZWaveInitializationStateEvent extends ZWaveEvent {
    private ZWaveNodeInitStage stage;

    /**
     * Constructor. Creates a new instance of the ZWaveInitializationStateEvent class.
     *
     * @param nodeId the nodeId of the event. Must be set to the controller node.
     */
    public ZWaveInitializationStateEvent(int nodeId, ZWaveNodeInitStage stage) {
        super(nodeId);
        this.stage = stage;
    }

    /**
     * Returns the current initialisation stage for the node
     *
     * @return node stage
     */
    public ZWaveNodeInitStage getStage() {
        return stage;
    }
}
