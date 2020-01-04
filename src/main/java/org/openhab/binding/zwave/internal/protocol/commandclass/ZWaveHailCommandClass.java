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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.concurrent.TimeUnit;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveDelayedPollEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Hail command class. Some devices handle state changes by 'hailing' the controller and asking it to
 * request the device values
 *
 * @author Chris Jackson
 * @author Ben Jones
 */
@XStreamAlias("COMMAND_CLASS_HAIL")
public class ZWaveHailCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveHailCommandClass.class);

    private static final int HAIL = 1;

    /**
     * Creates a new instance of the ZWaveHailCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveHailCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_HAIL;
    }

    @ZWaveResponseHandler(id = HAIL, name = "HAIL")
    public void handleDoorLockReport(ZWaveCommandClassPayload payload, int endpoint) {
        // We only re-request dynamic values for nodes that are completely initialized.
        if (getNode().getNodeState() != ZWaveNodeState.ALIVE || getNode().isInitializationComplete() == false) {
            return;
        }

        // Send delayed poll event
        getController()
                .notifyEventListeners(new ZWaveDelayedPollEvent(getNode().getNodeId(), 0, 75, TimeUnit.MILLISECONDS));
    }
}
