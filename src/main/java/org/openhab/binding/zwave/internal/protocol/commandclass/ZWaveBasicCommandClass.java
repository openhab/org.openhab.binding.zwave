/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.Map;

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
 * Handles the Basic command class. Almost all devices support the Basic commands. This class contains a few basic
 * commands that can be used to control the basic functionality of a device.
 * The commands include the possibility to set a given level, get a given level and report a level.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */

@XStreamAlias("basicCommandClass")
public class ZWaveBasicCommandClass extends ZWaveCommandClass implements ZWaveBasicCommands {

    @XStreamOmitField
    private final static Logger logger = LoggerFactory.getLogger(ZWaveBasicCommandClass.class);

    private static final int BASIC_SET = 0x01;
    private static final int BASIC_GET = 0x02;
    private static final int BASIC_REPORT = 0x03;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveBasicCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveBasicCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.BASIC;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received Basic Request", this.getNode().getNodeId());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case BASIC_SET:
                logger.debug("NODE {}: Basic Set sent to the controller will be processed as Basic Report",
                        this.getNode().getNodeId());
                // Now, some devices report their value as a basic set. For instance the Fibaro FGK - 101 Door / Window
                // sensor.
                // Process this as if it was a value report.
                processBasicReport(serialMessage, offset, endpoint);
                break;
            case BASIC_REPORT:
                logger.trace("NODE {}: Process Basic Report", this.getNode().getNodeId());
                processBasicReport(serialMessage, offset, endpoint);
                break;
            default:
                logger.debug(String.format("Unsupported Command 0x%02X for command class %s (0x%02X).", command,
                        this.getCommandClass().getLabel(), this.getCommandClass().getKey()));
        }
    }

    /**
     * Processes a BASIC_REPORT / BASIC_SET message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    protected void processBasicReport(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        int value = serialMessage.getMessagePayloadByte(offset + 1);
        logger.debug(String.format("NODE %d: Basic report, value = 0x%02X", this.getNode().getNodeId(), value));
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                this.getCommandClass(), value);
        this.getController().notifyEventListeners(zEvent);
    }

    /**
     * Gets a SerialMessage with the BASIC GET command
     *
     * @return the serial message
     */
    @Override
    public SerialMessage getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command BASIC_GET", this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                (byte) BASIC_GET };
        result.setMessagePayload(newPayload);
        return result;
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the BASIC SET command
     *
     * @param the level to set.
     * @return the serial message
     */
    @Override
    public SerialMessage setValueMessage(int level) {
        logger.debug("NODE {}: Creating new message for application command BASIC_SET", this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 3, (byte) getCommandClass().getKey(), (byte) BASIC_SET,
                (byte) level };
        result.setMessagePayload(newPayload);
        return result;
    }

}
