/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Collection;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the clock command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_CENTRAL_SCENE")
public class ZWaveCentralSceneCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveCentralSceneCommandClass.class);

    private static final int CENTRAL_SCENE_SUPPORTED_GET = 1;
    private static final int CENTRAL_SCENE_SUPPORTED_REPORT = 2;
    private static final int CENTRAL_SCENE_NOTIFICATION = 3;
    private static final int CENTRAL_SCENE_CONFIGURATION_SET = 4;
    private static final int CENTRAL_SCENE_CONFIGURATION_GET = 5;
    private static final int CENTRAL_SCENE_CONFIGURATION_REPORT = 6;

    @XStreamOmitField
    private boolean initialiseDone = false;

    private int sceneCount = 0;

    /**
     * Creates a new instance of the ZWaveCentralSceneCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveCentralSceneCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_CENTRAL_SCENE;
    }

    @ZWaveResponseHandler(id = CENTRAL_SCENE_NOTIFICATION, name = "CENTRAL_SCENE_NOTIFICATION")
    public void handleCentralSceneNotification(ZWaveCommandClassPayload payload, int endpoint) {
        // offset+1 is an incrementing number
        int sceneId = payload.getPayloadByte(4);
        int time = payload.getPayloadByte(4);
        if (time > 127) {
            // Values of 128 and above are in minutes (128 = 1 minute)
            time = (time - 127) * 60;
        }
        logger.debug("NODE {}: Received scene {} at time {}", getNode().getNodeId(), sceneId, time);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), sceneId);
        getController().notifyEventListeners(zEvent);
    }

    @ZWaveResponseHandler(id = CENTRAL_SCENE_SUPPORTED_REPORT, name = "CENTRAL_SCENE_SUPPORTED_REPORT")
    public void handleCentralSceneSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        sceneCount = payload.getPayloadByte(2);
        logger.debug("NODE {}: Supports {} scenes", getNode().getNodeId(), sceneCount);
        initialiseDone = true;
    }

    @Override
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command SCENE_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                CENTRAL_SCENE_SUPPORTED_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(CENTRAL_SCENE_NOTIFICATION).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        if (initialiseDone == false) {
            result.add(getValueMessage());
        }

        return result;
    }
}
