/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class IdentifyNodeMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(IdentifyNodeMessageClass.class);

    public ZWaveSerialPayload doRequest(int nodeId) {
        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.IdentifyNode).withPayload(nodeId).build();
        // .withResponseNodeId(nodeId).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.trace("Handle Message Get Node ProtocolInfo Response");

        if (transaction == null) {
            return false;
        }
        if (transaction.getSerialMessage() == null) {
            return false;
        }
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);
        logger.debug("NODE {}: ProtocolInfo", nodeId);

        ZWaveNode node = zController.getNode(nodeId);
        if (node == null) {
            logger.warn("NODE {}: Node not found when processing ProtocolInfo");
            return false;
        }

        boolean listening = (incomingMessage.getMessagePayloadByte(0) & 0x80) != 0 ? true : false;
        boolean routing = (incomingMessage.getMessagePayloadByte(0) & 0x40) != 0 ? true : false;
        int version = (incomingMessage.getMessagePayloadByte(0) & 0x07) + 1;
        boolean frequentlyListening = (incomingMessage.getMessagePayloadByte(1) & 0x60) != 0 ? true : false;
        boolean beaming = ((incomingMessage.getMessagePayloadByte(1) & 0x10) != 0);
        boolean security = ((incomingMessage.getMessagePayloadByte(1) & 0x01) != 0);

        // TODO: How about the 100kbps option?
        int maxBaudRate = 9600;
        if ((incomingMessage.getMessagePayloadByte(0) & 0x38) == 0x10) {
            maxBaudRate = 40000;
        }

        logger.debug("NODE {}: Listening = {}", nodeId, listening);
        logger.debug("NODE {}: Routing   = {}", nodeId, routing);
        logger.debug("NODE {}: Beaming   = {}", nodeId, beaming);
        logger.debug("NODE {}: Version   = {}", nodeId, version);
        logger.debug("NODE {}: FLIRS     = {}", nodeId, frequentlyListening);
        logger.debug("NODE {}: Security  = {}", nodeId, security);
        logger.debug("NODE {}: Max Baud  = {}", nodeId, maxBaudRate);

        node.setListening(listening);
        node.setRouting(routing);
        node.setVersion(version);
        node.setFrequentlyListening(frequentlyListening);
        node.setSecurity(security);
        node.setBeaming(beaming);
        node.setMaxBaud(maxBaudRate);

        Basic basic = Basic.getBasic(incomingMessage.getMessagePayloadByte(3));
        if (basic == null) {
            logger.debug("NODE {}: Basic device class {} not found", nodeId, incomingMessage.getMessagePayloadByte(3));
            return false;
        }
        logger.debug("NODE {}: Basic    = {}", nodeId, basic.toString());

        Generic generic = Generic.getGeneric(incomingMessage.getMessagePayloadByte(4));
        if (generic == null) {
            logger.debug("NODE {}: Generic device class {} not found", nodeId,
                    incomingMessage.getMessagePayloadByte(4));
            return false;
        }
        logger.debug("NODE {}: Generic  = {}", nodeId, generic.toString());

        Specific specific = Specific.getSpecific(generic, incomingMessage.getMessagePayloadByte(5));
        if (specific == null) {
            logger.debug("NODE {}: Specific device class {} not found", nodeId,
                    incomingMessage.getMessagePayloadByte(5));
            return false;
        }
        logger.debug("NODE {}: Specific = {}", nodeId, specific.toString());

        ZWaveDeviceClass deviceClass = node.getDeviceClass();
        deviceClass.setBasicDeviceClass(basic);
        deviceClass.setGenericDeviceClass(generic);
        deviceClass.setSpecificDeviceClass(specific);

        // Add the mandatory command classes.
        // If restored the node from configuration file then
        // the classes will already exist and this will be ignored
        node.addCommandClass(
                ZWaveCommandClass.getInstance(CommandClass.COMMAND_CLASS_NO_OPERATION.getKey(), node, zController));
        node.addCommandClass(
                ZWaveCommandClass.getInstance(CommandClass.COMMAND_CLASS_BASIC.getKey(), node, zController));

        // Add mandatory command classes as specified by it's generic device class.
        // for (CommandClass commandClass : generic.getMandatoryCommandClasses()) {
        // ZWaveCommandClass zwaveCommandClass = ZWaveCommandClass.getInstance(commandClass.getKey(), node,
        // zController);
        // if (zwaveCommandClass != null) {
        // zController.getNode(nodeId).addCommandClass(zwaveCommandClass);
        // }
        // }

        // Add mandatory command classes as specified by it's specific device class.
        // for (CommandClass commandClass : specific.getMandatoryCommandClasses()) {
        // ZWaveCommandClass zwaveCommandClass = ZWaveCommandClass.getInstance(commandClass.getKey(), node,
        // zController);
        // if (zwaveCommandClass != null) {
        // node.addCommandClass(zwaveCommandClass);
        // }
        // }

        // checkTransactionComplete(transaction, incomingMessage);

        transaction.setTransactionComplete();
        return true;
    }
}
