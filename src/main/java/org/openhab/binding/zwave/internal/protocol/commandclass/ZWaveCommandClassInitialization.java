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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.Collection;

import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Interface that command classes can implement to implement initialization.
 * For instance to support getting static values from a node, or handle dependencies on other command classes.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public interface ZWaveCommandClassInitialization {
    /**
     * Initializes a command class. During initialization some queries might need to be performed. These queries need to
     * be completed to be able to proceed to the next node phase. The queries are returned so that the node can handle
     * processing and counting to proceed to the next node phase.
     *
     * @param refresh if true will request all initialised even if the class is already initialised
     * @return the messages with the queries for initialization.
     */
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh);
}
