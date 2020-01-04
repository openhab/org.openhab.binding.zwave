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
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class AddNodeMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(AddNodeMessageClass.class);

    private final int ADD_NODE_ANY = 1;
    private final int ADD_NODE_CONTROLLER = 2;
    private final int ADD_NODE_SLAVE = 3;
    private final int ADD_NODE_EXISTING = 4;
    private final int ADD_NODE_STOP = 5;
    private final int ADD_NODE_STOP_FAILED = 6;

    private final int ADD_NODE_STATUS_LEARN_READY = 1;
    private final int ADD_NODE_STATUS_NODE_FOUND = 2;
    private final int ADD_NODE_STATUS_ADDING_SLAVE = 3;
    private final int ADD_NODE_STATUS_ADDING_CONTROLLER = 4;
    private final int ADD_NODE_STATUS_PROTOCOL_DONE = 5;
    private final int ADD_NODE_STATUS_DONE = 6;
    private final int ADD_NODE_STATUS_FAILED = 7;

    private final int OPTION_HIGH_POWER = 0x80;
    private final int OPTION_NETWORK_WIDE = 0x40;

    private Basic basic;
    private Generic generic;
    private Specific specific;
    private List<CommandClass> commandClasses;

    public ZWaveSerialPayload doRequestStart(boolean highPower, boolean networkWide) {
        logger.debug("Setting controller into INCLUSION mode, highPower:{} networkWide:{}.", highPower, networkWide);

        byte command = ADD_NODE_ANY;
        if (highPower == true) {
            command |= OPTION_HIGH_POWER;
        }
        if (networkWide == true) {
            command |= OPTION_NETWORK_WIDE;
        }

        return new ZWaveTransactionMessageBuilder(SerialMessageClass.AddNodeToNetwork).withPayload(command)
                .withRequiresData(false).build();
    }

    public ZWaveSerialPayload doRequestStop(boolean complete) {
        logger.debug("Ending INCLUSION mode.");

        // Create the request
        ZWaveSerialPayload payload = new ZWaveTransactionMessageBuilder(SerialMessageClass.AddNodeToNetwork)
                .withPayload(ADD_NODE_STOP).withRequiresData(false).build();

        if (complete) {
            payload.setCallbackId(0);
        }
        return payload;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        switch (incomingMessage.getMessagePayloadByte(1)) {
            case ADD_NODE_STATUS_LEARN_READY:
                logger.debug("Add Node: Learn ready.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeStart));
                if (transaction != null) {
                    transaction.setTransactionComplete();
                }
                break;

            case ADD_NODE_STATUS_NODE_FOUND:
                logger.debug("Add Node: New node found.");
                break;

            case ADD_NODE_STATUS_ADDING_SLAVE:
                if (incomingMessage.getMessagePayloadByte(2) < 1 || incomingMessage.getMessagePayloadByte(2) > 232) {
                    break;
                }
                logger.debug("NODE {}: Adding slave.", incomingMessage.getMessagePayloadByte(2));
                processNodeInformation(incomingMessage);
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeSlaveFound,
                        incomingMessage.getMessagePayloadByte(2), basic, generic, specific, commandClasses));
                break;

            case ADD_NODE_STATUS_ADDING_CONTROLLER:
                if (incomingMessage.getMessagePayloadByte(2) < 1 || incomingMessage.getMessagePayloadByte(2) > 232) {
                    break;
                }
                logger.debug("NODE {}: Adding controller.", incomingMessage.getMessagePayloadByte(2));
                processNodeInformation(incomingMessage);
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeControllerFound,
                        incomingMessage.getMessagePayloadByte(2), basic, generic, specific, commandClasses));
                break;

            case ADD_NODE_STATUS_PROTOCOL_DONE:
                logger.debug("NODE {}: Add Node: Protocol done.", incomingMessage.getMessagePayloadByte(2));
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeProtocolDone,
                        incomingMessage.getMessagePayloadByte(2)));
                break;

            case ADD_NODE_STATUS_DONE:
                logger.debug("Add Node: Done.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeDone,
                        incomingMessage.getMessagePayloadByte(2)));
                break;

            case ADD_NODE_STATUS_FAILED:
                logger.debug("Add Node: Failed.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeFail));
                if (transaction != null) {
                    transaction.setTransactionCanceled();
                }
                break;

            default:
                logger.debug("Add Node: Unknown request ({}).", incomingMessage.getMessagePayloadByte(1));
                break;
        }

        return true;
    }

    private void processNodeInformation(SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int length = incomingMessage.getMessagePayloadByte(3);

        basic = Basic.getBasic(incomingMessage.getMessagePayloadByte(4));
        generic = Generic.getGeneric(incomingMessage.getMessagePayloadByte(5));
        specific = Specific.getSpecific(generic, incomingMessage.getMessagePayloadByte(6));

        commandClasses = new ArrayList<CommandClass>();

        for (int i = 7; i < length + 4; i++) {
            int data = incomingMessage.getMessagePayloadByte(i);

            CommandClass commandClass = CommandClass.getCommandClass(data);
            if (commandClass == null) {
                continue;
            }

            commandClasses.add(commandClass);
        }

    }
}
