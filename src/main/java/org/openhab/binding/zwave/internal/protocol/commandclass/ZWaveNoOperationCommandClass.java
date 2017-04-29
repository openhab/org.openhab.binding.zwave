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
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the no operation command class. The No Operation command class is used to check if a node is reachable by
 * sending a serial message without a command to the specified node. This can for instance be used to check that a node
 * is non-responding.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("noOperationCommandClass")
public class ZWaveNoOperationCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private final static Logger logger = LoggerFactory.getLogger(ZWaveNoOperationCommandClass.class);

    /**
     * Creates a new instance of the ZWaveNoOperationCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveNoOperationCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);

        // We don't want to request the version since some nodes won't respond and there's little point anyway!
        setVersion(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.NO_OPERATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint) {
        logger.debug("NODE {}: Received NO_OPERATION command", getNode().getNodeId());
    }

    /**
     * Gets a SerialMessage with the No Operation command
     *
     * @return the serial message
     */
    public SerialMessage getNoOperationMessage() {
        logger.debug("NODE {}: Creating new message for command No Operation", this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Poll);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 1, (byte) getCommandClass().getKey() };
        result.setMessagePayload(newPayload);
        return result;
    }
}
