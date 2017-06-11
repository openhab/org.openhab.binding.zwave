/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.event;

import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * ZWave value event. This event is fired when a command class receives a value from the node.
 *
 * @author Chris Jackson
 */
public class ZWaveValueEvent extends ZWaveCommandClassValueEvent {
    private Map<String, String> values;

    /**
     * Constructor. Creates a new instance of the ZWaveCommandClassValueEvent class.
     *
     * @param nodeId the nodeId of the event
     * @param endpoint the endpoint of the event.
     * @param commandClass the command class that fired the event;
     * @param values the value for the event as Map<String, String>.
     */
    public ZWaveValueEvent(int nodeId, int endpoint, CommandClass commandClass, Map<String, String> values) {
        super(nodeId, endpoint, commandClass, values);

        this.values = values;
    }

    /**
     * Gets the value for the event.
     *
     * @param key the key for the requested value
     * @return the value.
     */
    public String getValue(String key) {
        return values.get(key);
    }

}
