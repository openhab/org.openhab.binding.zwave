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

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles scene activation messages
 *
 * @author Chris Jackson
 */

@XStreamAlias("COMMAND_CLASS_SCENE_ACTIVATION")
public class ZWaveSceneActivationCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveSceneActivationCommandClass.class);

    private static final int SCENEACTIVATION_SET = 0x01;

    /**
     * Creates a new instance of the ZWaveSceneActivationCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveSceneActivationCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_SCENE_ACTIVATION;
    }

    /**
     * Processes a SCENEACTIVATION_SET message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = SCENEACTIVATION_SET, name = "SCENEACTIVATION_SET")
    public void handleProtectionReport(ZWaveCommandClassPayload payload, int endpoint) {
        int sceneId = payload.getPayloadByte(2);
        int sceneTime = 0;

        // TODO: Aeon Minimote fw 1.19 sends SceneActivationSet without Time parameter - to database parm
        if (payload.getPayloadLength() > 3) {
            sceneTime = payload.getPayloadByte(3);
        }

        logger.debug("NODE {}: Scene activation: Scene {}, Time {}", getNode().getNodeId(), sceneId, sceneTime);

        // Ignore the time for now at least!

        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), sceneId);
        getController().notifyEventListeners(zEvent);
    }
}
