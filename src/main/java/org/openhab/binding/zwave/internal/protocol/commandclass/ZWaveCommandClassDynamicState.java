/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.Collection;

import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Interface that command classes can implement to implement retrieval of dynamic state information.
 * For instance to support getting dynamic values from a node.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public interface ZWaveCommandClassDynamicState {
    /**
     * Gets the dynamic state information from the node. Returns queries that fetch dynamic state information. These
     * queries need to be completed to be able to proceed to the next node phase. The queries are returned so that the
     * node can handle processing to proceed to the next node phase.
     *
     * @param refresh if true will request all dynamic states even if they are already initialised
     * @return the messages with the queries for dynamic values.
     */
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh);
}
