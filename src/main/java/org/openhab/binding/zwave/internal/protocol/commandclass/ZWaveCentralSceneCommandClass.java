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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
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
public class ZWaveCentralSceneCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveCentralSceneCommandClass.class);

    private static final int CENTRAL_SCENE_SUPPORTED_GET = 1;
    private static final int CENTRAL_SCENE_SUPPORTED_REPORT = 2;
    private static final int CENTRAL_SCENE_NOTIFICATION = 3;
    private static final int CENTRAL_SCENE_CONFIGURATION_SET = 4;
    private static final int CENTRAL_SCENE_CONFIGURATION_GET = 5;
    private static final int CENTRAL_SCENE_CONFIGURATION_REPORT = 6;

    private static final int MAX_SUPPORTED_VERSION = 3;

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
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_CENTRAL_SCENE;
    }

    @ZWaveResponseHandler(id = CENTRAL_SCENE_NOTIFICATION, name = "CENTRAL_SCENE_NOTIFICATION")
    public void handleCentralSceneNotification(ZWaveCommandClassPayload payload, int endpoint) {
        // offset+1 is an incrementing number

        int key = payload.getPayloadByte(3) & 0x07;
        int sceneId = payload.getPayloadByte(4);

        if (getVersion() >= 3) {
            // Slow refresh bit
        }

        String keyMeaning;
        switch (key) {
            case 0:
                keyMeaning = "Single Press";
                break;
            case 1:
                keyMeaning = "Key Released";
                break;
            case 2:
                keyMeaning = "Key Held Down";
                break;
            case 3:
                keyMeaning = "Single Press 2 times";
                break;
            case 4:
                keyMeaning = "Single Press 3 times";
                break;
            case 5:
                keyMeaning = "Single Press 4 times";
                break;
            case 6:
                keyMeaning = "Single Press 5 times";
                break;
            default:
                keyMeaning = "Unknown";
                break;
        }

        logger.debug("NODE {}: Received scene {} at key {} [{}]", getNode().getNodeId(), sceneId, key, keyMeaning);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), new BigDecimal(String.format("%d.%d", sceneId, key)));
        getController().notifyEventListeners(zEvent);
    }

    @ZWaveResponseHandler(id = CENTRAL_SCENE_SUPPORTED_REPORT, name = "CENTRAL_SCENE_SUPPORTED_REPORT")
    public void handleCentralSceneSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        sceneCount = payload.getPayloadByte(2);
        logger.debug("NODE {}: Supports {} scenes", getNode().getNodeId(), sceneCount);
        initialiseDone = true;
    }

    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command SCENE_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                CENTRAL_SCENE_SUPPORTED_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(CENTRAL_SCENE_SUPPORTED_REPORT).build();
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
