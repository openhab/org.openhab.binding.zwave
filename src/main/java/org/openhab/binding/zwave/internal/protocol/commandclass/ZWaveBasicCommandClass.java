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

import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
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

@XStreamAlias("COMMAND_CLASS_BASIC")
public class ZWaveBasicCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveBasicCommandClass.class);

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

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_BASIC;
    }

    /**
     * Processes a BASIC_REPORT / BASIC_SET message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = BASIC_REPORT, name = "BASIC_REPORT")
    public void handleBasicReport(ZWaveCommandClassPayload payload, int endpoint) {
        int value = payload.getPayloadByte(2);
        logger.debug("NODE {}: Basic report, value = {}", getNode().getNodeId(), value);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), value);
        getController().notifyEventListeners(zEvent);
    }

    @ZWaveResponseHandler(id = BASIC_SET, name = "BASIC_SET")
    public void handleBasicSet(ZWaveCommandClassPayload payload, int endpoint) {
        handleBasicReport(payload, endpoint);
    }

    /**
     * Gets a SerialMessage with the BASIC GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), BASIC_GET)
                .withExpectedResponseCommand(BASIC_REPORT).withPriority(TransactionPriority.Get).build();
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
    public ZWaveCommandClassTransactionPayload setValueMessage(int level) {
        logger.debug("NODE {}: Creating new message for application command BASIC_SET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), BASIC_SET)
                .withPayload(level).withPriority(TransactionPriority.Set).build();
    }

}
