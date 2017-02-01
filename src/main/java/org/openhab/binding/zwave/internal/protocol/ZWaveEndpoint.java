/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * ZWaveEndpoint class. Represents an endpoint in case of a Multi-channel node.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("endPoint")
public class ZWaveEndpoint {

    private final ZWaveDeviceClass deviceClass;
    private final int endpointId;

    private Set<CommandClass> secureCommandClasses = new HashSet<CommandClass>();
    private Map<CommandClass, ZWaveCommandClass> supportedCommandClasses = new HashMap<CommandClass, ZWaveCommandClass>();

    /**
     * Constructor. Creates a new instance of the ZWaveEndpoint class.
     *
     * @param node the parent node of this endpoint.
     * @param endpointId the endpoint ID.
     */
    public ZWaveEndpoint(int endpointId) {
        this.endpointId = endpointId;
        this.deviceClass = new ZWaveDeviceClass(Basic.BASIC_TYPE_UNKNOWN, Generic.GENERIC_TYPE_NOT_USED,
                Specific.SPECIFIC_TYPE_NOT_USED);
    }

    /**
     * Gets the endpoint ID
     *
     * @return endpointId the endpointId
     */
    public int getEndpointId() {
        return endpointId;
    }

    /**
     * Gets the Command classes this endpoint implements.
     *
     * @return the command classes.
     */
    public Collection<ZWaveCommandClass> getCommandClasses() {
        return supportedCommandClasses.values();
    }

    /**
     * Gets a commandClass object this endpoint implements. Returns null if this endpoint does not support this command
     * class.
     *
     * @param commandClass
     *            The command class to get.
     * @return the command class.
     */
    public ZWaveCommandClass getCommandClass(CommandClass commandClass) {
        return supportedCommandClasses.get(commandClass);
    }

    /**
     * Adds a command class to the list of supported command classes by this endpoint. Does nothing if command class is
     * already added.
     *
     * @param commandClass the command class instance to add.
     */
    public void addCommandClass(ZWaveCommandClass commandClass) {
        CommandClass key = commandClass.getCommandClass();

        if (!supportedCommandClasses.containsKey(key)) {
            supportedCommandClasses.put(key, commandClass);
        }
    }

    /**
     * Adds a secure command class to the list of supported command classes by this endpoint. Does nothing if command
     * class is already added.
     *
     * @param commandClass the command class instance to add.
     */
    public void addSecureCommandClass(CommandClass commandClass) {
        secureCommandClasses.add(commandClass);
    }

    /**
     * Checks if a commandClass is supported in secure mode by this endpoint.
     *
     * @param commandClass
     *            The command class to get.
     * @return true if the command class is supported in secure mode.
     */
    public boolean supportsSecureCommandClass(CommandClass commandClass) {
        return secureCommandClasses.contains(commandClass);
    }

    /**
     * Removes a command class from the node.
     * This is used to remove classes that a node may report it supports
     * but it doesn't respond to.
     *
     * @param commandClass The command class key
     */
    public void removeCommandClass(CommandClass commandClass) {
        supportedCommandClasses.remove(commandClass);
    }

    /**
     * Returns the device class for this endpoint.
     *
     * @return the deviceClass
     */
    public ZWaveDeviceClass getDeviceClass() {
        return deviceClass;
    }

    public Set<CommandClass> getSecureCommandClasses() {
        return secureCommandClasses;
    }
}
