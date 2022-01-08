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

import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * ZWave Command Class event. This event is fired when a command class receives a value from the node. The event can be
 * subclasses to add additional information to the event.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveCommandClassValueEvent extends ZWaveEvent {

    private final CommandClass commandClass;
    private final Object value;
    private final Object type;

    /**
     * Constructor. Creates a new instance of the ZWaveCommandClassValueEvent class.
     *
     * @param nodeId the nodeId of the event
     * @param endpoint the endpoint of the event.
     * @param commandClass the command class that fired the ZWaveCommandClassValueEvent;
     * @param value the value for the event.
     */
    public ZWaveCommandClassValueEvent(int nodeId, int endpoint, CommandClass commandClass, Object value) {
        super(nodeId, endpoint);

        this.commandClass = commandClass;
        this.value = value;
        this.type = null;
    }

    /**
     * Constructor. Creates a new instance of the ZWaveCommandClassValueEvent class.
     *
     * @param nodeId the nodeId of the event
     * @param endpoint the endpoint of the event.
     * @param commandClass the command class that fired the ZWaveCommandClassValueEvent;
     * @param value the value for the event.
     * @param type the type of event (normally an enum that provides information on the event type)
     */
    public ZWaveCommandClassValueEvent(int nodeId, int endpoint, CommandClass commandClass, Object value, Object type) {
        super(nodeId, endpoint);

        this.commandClass = commandClass;
        this.value = value;
        this.type = type;
    }

    /**
     * Gets the command class that fired the ZWaveCommandClassValueEvent;
     *
     * @return the command class.
     */
    public CommandClass getCommandClass() {
        return commandClass;
    }

    /**
     * Gets the value for the event.
     *
     * @return the value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets the event type
     *
     */
    public Object getType() {
        return type;
    }
}
