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

import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
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
@XStreamAlias("COMMAND_CLASS_NO_OPERATION")
public class ZWaveNoOperationCommandClass extends ZWaveCommandClass {
    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveNoOperationCommandClass.class);

    /**
     * Creates a new instance of the ZWaveNoOperationCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveNoOperationCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);

        // We don't want to request the version since some nodes won't respond
        // There's also no point as this class must be implemented and there's only 1 version
        setVersion(1);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_NO_OPERATION;
    }

    /**
     * Gets a SerialMessage with the No Operation command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getNoOperationMessage() {
        logger.debug("NODE {}: Creating new message for command NO_OPERATION_PING", getNode().getNodeId());

        // return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass())
        // .withPriority(TransactionPriority.Poll).build();

        return new ZWaveCommandClassTransactionPayload(getNode().getNodeId(),
                new byte[] { (byte) getCommandClass().getKey() }, TransactionPriority.Poll, null, 0);
    }
}
