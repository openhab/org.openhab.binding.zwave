/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
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
@XStreamAlias("ZWaveApplicationStatusClass")
public class ZWaveApplicationStatusClass extends ZWaveCommandClass {

    @XStreamOmitField
    private final Logger logger = LoggerFactory.getLogger(ZWaveApplicationStatusClass.class);

    @XStreamOmitField
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final int ApplicationStatusBusy = 0x1;
    private static final int ApplicationStatusRejected = 0x2;

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
    public ZWaveApplicationStatusClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.APPLICATION_STATUS;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Application Status message", getNode());
        int status = serialMessage.getMessagePayloadByte(offset++);
        switch (status) {
            case ApplicationStatusBusy:
                logger.trace("NODE {}: Process Application Status Busy status", getNode());
                int busyStatus = serialMessage.getMessagePayloadByte(offset++);
                int retry = DEFAULT_RETRY_SECONDS;
                switch (busyStatus) {
                    case StatusBusyTryAgainLaterInSeconds:
                        retry = serialMessage.getMessagePayloadByte(offset++);
                        logger.debug("NODE {}: is busy and wants us to try again in {} seconds", getNode(), retry);
                    case StatusBusyTryAgainLater:
                        logger.debug("NODE {}: is busy and will be polled in {} seconds", getNode(), retry);
                        ZWaveDelayedPollEvent event = new ZWaveDelayedPollEvent(getNode().getNodeId(), retry,
                                TimeUnit.SECONDS);
                        this.getController().notifyEventListeners(event);
                        break;
                    case StatusBusyQueued:
                        logger.warn("NODE {}: is busy and has queued the request", getNode());
                        break;
                    default:
                        logger.warn("NODE {}: unknown busy status {} ", getNode(), busyStatus);
                        break;
                }
                break;
            case ApplicationStatusRejected:
                logger.warn("NODE {}: has rejected the request", getNode());
                break;
            default:
                logger.warn(String.format("Unsupported status 0x%02X for command class %s (0x%02X).", status,
                        this.getCommandClass().getLabel(), this.getCommandClass().getKey()));
        }
    }
}
