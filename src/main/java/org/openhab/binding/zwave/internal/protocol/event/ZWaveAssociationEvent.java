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

import java.util.List;

import org.openhab.binding.zwave.internal.protocol.ZWaveAssociation;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * ZWave association group received event.
 * Send from the association members to the binding
 * Note that multiple events can be required to build up the full list.
 *
 * @author Chris Jackson
 */
public class ZWaveAssociationEvent extends ZWaveCommandClassValueEvent {

    private ZWaveAssociationGroup group;

    /**
     * Constructor. Creates a new instance of the ZWaveAssociationEvent
     * class.
     *
     * @param nodeId the nodeId of the event. Must be set to the controller node.
     */
    public ZWaveAssociationEvent(int nodeId, ZWaveAssociationGroup group) {
        super(nodeId, 0, CommandClass.COMMAND_CLASS_ASSOCIATION, 0);

        this.group = group;
    }

    public int getGroupId() {
        return group.getIndex();
    }

    public List<ZWaveAssociation> getGroupMembers() {
        return group.getAssociations();
    }
}
