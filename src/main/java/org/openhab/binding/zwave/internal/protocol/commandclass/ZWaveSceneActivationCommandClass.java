/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
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

@XStreamAlias("sceneActivationCommandClass")
public class ZWaveSceneActivationCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private final static Logger logger = LoggerFactory.getLogger(ZWaveBasicCommandClass.class);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.SCENE_ACTIVATION;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug(String.format("Received Scene Activation for Node ID = %d", this.getNode().getNodeId()));
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case SCENEACTIVATION_SET:
                logger.debug("Scene Activation Set");

                processSceneActivationSet(serialMessage, offset, endpoint);
                break;
            default:
                logger.warn(String.format("Unsupported Command %d for command class %s (0x%02X).", command,
                        this.getCommandClass().getLabel(), this.getCommandClass().getKey()));
        }
    }

    /**
     * Processes a SCENEACTIVATION_SET message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    protected void processSceneActivationSet(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        int sceneId = serialMessage.getMessagePayloadByte(offset + 1);
        int sceneTime = 0;

        // TODO: Aeon Minimote fw 1.19 sends SceneActivationSet without Time parameter - to database parm
        if (serialMessage.getMessagePayload().length > (offset + 2)) {
            sceneTime = serialMessage.getMessagePayloadByte(offset + 2);
        }

        logger.debug(String.format("Scene activation node from node %d: Scene %d, Time %d", this.getNode().getNodeId(),
                sceneId, sceneTime));

        // Ignore the time for now at least!

        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                this.getCommandClass(), sceneId);
        this.getController().notifyEventListeners(zEvent);
    }
}
