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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveInclusionState;

/**
 * This event signals a node being included or excluded into the network.
 *
 * @author Chris Jackson
 */
public class ZWaveInclusionEvent extends ZWaveEvent {
    private ZWaveInclusionState type;
    private final Date includedAt;
    List<CommandClass> commandClasses = new ArrayList<CommandClass>();
    Basic basic = Basic.BASIC_TYPE_UNKNOWN;
    Generic generic = Generic.GENERIC_TYPE_NOT_USED;
    Specific specific = Specific.SPECIFIC_TYPE_NOT_USED;

    /**
     * Constructor. Creates a new instance of the ZWaveInclusionEvent
     * class.
     *
     * @param nodeId the nodeId of the event.
     */
    public ZWaveInclusionEvent(ZWaveInclusionState type) {
        super(255);
        this.includedAt = new Date();
        this.type = type;
    }

    public ZWaveInclusionEvent(ZWaveInclusionState type, int nodeId) {
        super(nodeId);
        this.includedAt = new Date();
        this.type = type;
    }

    public ZWaveInclusionEvent(ZWaveInclusionState type, int nodeId, Basic basic, Generic generic, Specific specific,
            List<CommandClass> commandClasses) {
        super(nodeId);
        this.includedAt = new Date();
        this.type = type;
        this.basic = basic;
        this.generic = generic;
        this.specific = specific;
        this.commandClasses.addAll(commandClasses);
    }

    public Date getIncludedAt() {
        return includedAt;
    }

    public ZWaveInclusionState getEvent() {
        return type;
    }

    public Basic getBasic() {
        return basic;
    }

    public Generic getGeneric() {
        return generic;
    }

    public Specific getSpecific() {
        return specific;
    }

    public List<CommandClass> getCommandClasses() {
        return commandClasses;
    }
}
