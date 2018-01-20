/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.event;

/**
 * Network event signals that a network function has completed.
 * This is used to notify higher layers of network functions so they can be handled by (for example) a network heal
 * process.
 *
 * @author Chris Jackson
 */
public class ZWaveNetworkEvent extends ZWaveEvent {
    Type type;
    State state;
    Object value;

    /**
     * Constructor. Creates a new instance of the ZWaveNetworkEvent class.
     *
     * @param type the type of event (generally the serial command)
     * @param nodeId the nodeId of the event or 0 for a network wide event
     * @param state
     */
    public ZWaveNetworkEvent(Type type, int nodeId, State state) {
        super(nodeId);

        this.type = type;
        this.state = state;
    }

    public ZWaveNetworkEvent(Type type, int nodeId, State state, Object value) {
        super(nodeId);

        this.type = type;
        this.state = state;
        this.value = value;
    }

    public Type getEvent() {
        return type;
    }

    public State getState() {
        return state;
    }

    public Object getValue() {
        return value;
    }

    public enum Type {
        AssignSucReturnRoute,
        AssignReturnRoute,
        DeleteSucReturnRoute,
        DeleteReturnRoute,
        NodeNeighborUpdate,
        NodeRoutingInfo,
        AssociationUpdate,
        DeleteNode,
        FailedNode,
        RequestNetworkUpdate,
        RemoveFailedNodeID,
        ReplaceFailedNode
    }

    public enum State {
        Success,
        Failure
    }
}
