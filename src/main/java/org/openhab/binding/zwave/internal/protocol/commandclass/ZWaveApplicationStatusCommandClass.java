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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveDelayedPollEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Application Status command class.
 *
 * @author Chris Jackson
 * @author Dan Cunningham
 */
@XStreamAlias("COMMAND_CLASS_APPLICATION_STATUS")
public class ZWaveApplicationStatusCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveApplicationStatusCommandClass.class);

    @XStreamOmitField
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final int APPLICATION_BUSY = 1;
    private static final int APPLICATION_REJECTED_REQUEST = 2;

    private static final int StatusBusyTryAgainLater = 0x0;
    private static final int StatusBusyTryAgainLaterInSeconds = 0x1;
    private static final int StatusBusyQueued = 0x2;

    public static int DEFAULT_RETRY_SECONDS = 2;

    /**
     * Creates a new instance of the ZWaveApplicationStatusClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveApplicationStatusCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_APPLICATION_STATUS;
    }

    @ZWaveResponseHandler(id = APPLICATION_BUSY, name = "APPLICATION_BUSY")
    public void handleApplicationBusy(ZWaveCommandClassPayload payload, int endpoint) {
        int busyStatus = payload.getPayloadByte(2);
        int retrySeconds = DEFAULT_RETRY_SECONDS;
        switch (busyStatus) {
            case StatusBusyTryAgainLaterInSeconds:
                retrySeconds = payload.getPayloadByte(3);
                logger.debug("NODE {}: is busy and wants us to try again in {} seconds", getNode(), retrySeconds);
            case StatusBusyTryAgainLater:
                logger.debug("NODE {}: is busy and will be polled in {} seconds", getNode(), retrySeconds);
                ZWaveDelayedPollEvent event = new ZWaveDelayedPollEvent(getNode().getNodeId(), retrySeconds,
                        TimeUnit.SECONDS);
                this.getController().notifyEventListeners(event);
                break;
            case StatusBusyQueued:
                logger.debug("NODE {}: is busy and has queued the request", getNode());
                break;
            default:
                logger.debug("NODE {}: unknown busy status {} ", getNode(), busyStatus);
                break;
        }
    }

    @ZWaveResponseHandler(id = APPLICATION_REJECTED_REQUEST, name = "APPLICATION_REJECTED_REQUEST")
    public void handleApplicationRejectedRequest(ZWaveCommandClassPayload payload, int endpoint) {
        // TODO: Should we reject or retry the transaction??
    }
}
