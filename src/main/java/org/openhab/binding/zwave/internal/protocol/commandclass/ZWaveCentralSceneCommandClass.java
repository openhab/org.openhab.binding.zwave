/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
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
 * Handles the clock command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("centralSceneCommandClass")
public class ZWaveCentralSceneCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveCentralSceneCommandClass.class);

    private static final int SCENE_GET = 1;
    private static final int CENTRAL_SCENE_SUPPORTED_REPORT = 2;
    private static final int CENTRAL_SCENE_NOTIFICATION = 3;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.CENTRAL_SCENE;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received CENTRAL_SCENE command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case CENTRAL_SCENE_NOTIFICATION:
                // offset+1 is an incrementing number
                int key = serialMessage.getMessagePayloadByte(offset + 2) & 0x07;
                int sceneId = serialMessage.getMessagePayloadByte(offset + 3);

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

                logger.debug("NODE {}: Received scene {} at key {} [{}]", getNode().getNodeId(), sceneId, key,
                        keyMeaning);
                ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                        getCommandClass(), new BigDecimal(String.format("%d.%d", sceneId, key)));
                this.getController().notifyEventListeners(zEvent);
                break;
            case CENTRAL_SCENE_SUPPORTED_REPORT:
                sceneCount = serialMessage.getMessagePayloadByte(offset + 1);
                logger.debug("NODE {}: Supports {} scenes", this.getNode().getNodeId(), sceneCount);
                initialiseDone = true;
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        this.getNode().getNodeId(), command, this.getCommandClass().getLabel(),
                        this.getCommandClass().getKey()));
        }
    }

    @Override
    public SerialMessage getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command SCENE_GET", this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                (byte) SCENE_GET };
        result.setMessagePayload(newPayload);
        return result;
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        if (initialiseDone == false) {
            result.add(getValueMessage());
        }

        return result;
    }

}
