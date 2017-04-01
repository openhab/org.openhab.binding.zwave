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
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent.State;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class RemoveFailedNodeMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(RemoveFailedNodeMessageClass.class);

    private final int FAILED_NODE_REMOVE_STARTED = 0x00;
    private final int FAILED_NODE_NOT_PRIMARY_CONTROLLER = 0x02;
    private final int FAILED_NODE_NO_CALLBACK_FUNCTION = 0x04;
    private final int FAILED_NODE_NOT_FOUND = 0x08;
    private final int FAILED_NODE_REMOVE_PROCESS_BUSY = 0x10;
    private final int FAILED_NODE_REMOVE_FAIL = 0x20;

    private final int FAILED_NODE_OK = 0x00;
    private final int FAILED_NODE_REMOVED = 0x01;
    private final int FAILED_NODE_NOT_REMOVED = 0x02;

    public enum Report {
        FAILED_NODE_REMOVE_STARTED,
        FAILED_NODE_NOT_PRIMARY_CONTROLLER,
        FAILED_NODE_NO_CALLBACK_FUNCTION,
        FAILED_NODE_NOT_FOUND,
        FAILED_NODE_REMOVE_PROCESS_BUSY,
        FAILED_NODE_REMOVE_FAIL,
        FAILED_NODE_UNKNOWN_FAIL,

        FAILED_NODE_OK,
        FAILED_NODE_REMOVED,
        FAILED_NODE_NOT_REMOVED
    }

    public SerialMessage doRequest(int nodeId) {
        logger.debug("NODE {}: Marking node as having failed.", nodeId);

        // Queue the request
        SerialMessage newMessage = new SerialMessage(SerialMessageClass.RemoveFailedNodeID, SerialMessageType.Request,
                SerialMessageClass.RemoveFailedNodeID, SerialMessagePriority.High);
        byte[] newPayload = { (byte) nodeId, (byte) 0x01 };
        newMessage.setMessagePayload(newPayload);
        return newMessage;
    }

    @Override
    public boolean handleResponse(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.debug("Got RemoveFailedNode response.");
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        Report report = null;
        switch (incomingMessage.getMessagePayloadByte(0)) { // TODO: Should this be (&& 0x0f)?
            case FAILED_NODE_REMOVE_STARTED:
                logger.debug("NODE {}: Remove failed node successfully placed on stack.", nodeId);
                break;
            case FAILED_NODE_NOT_PRIMARY_CONTROLLER:
                logger.debug("NODE {}: Remove failed node failed as not Primary Controller for node!", nodeId);
                transactionComplete = true;
                report = Report.FAILED_NODE_NOT_PRIMARY_CONTROLLER;
                break;
            case FAILED_NODE_NO_CALLBACK_FUNCTION:
                logger.debug("NODE {}: Remove failed node failed as no callback function!", nodeId);
                transactionComplete = true;
                report = Report.FAILED_NODE_NO_CALLBACK_FUNCTION;
                break;
            case FAILED_NODE_NOT_FOUND:
                logger.debug("NODE {}: Remove failed node failed as node not found", nodeId);
                transactionComplete = true;
                report = Report.FAILED_NODE_NOT_FOUND;
                break;
            case FAILED_NODE_REMOVE_PROCESS_BUSY:
                logger.debug("NODE {}: Remove failed node failed as Controller Busy!", nodeId);
                transactionComplete = true;
                report = Report.FAILED_NODE_REMOVE_PROCESS_BUSY;
                break;
            case FAILED_NODE_REMOVE_FAIL:
                logger.debug("NODE {}: Remove failed node failed!", nodeId);
                transactionComplete = true;
                report = Report.FAILED_NODE_REMOVE_FAIL;
                break;
            default:
                logger.debug("NODE {}: Remove failed node not placed on stack due to error 0x{}.", nodeId,
                        Integer.toHexString(incomingMessage.getMessagePayloadByte(0)));
                transactionComplete = true;
                report = Report.FAILED_NODE_UNKNOWN_FAIL;
                break;
        }

        // If this is a fail, then notify now, otherwise wait for the REQuest
        if (report != null) {
            zController.notifyEventListeners(
                    new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.RemoveFailedNodeID, nodeId, State.Failure, report));
        }
        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, SerialMessage lastSentMessage,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId = lastSentMessage.getMessagePayloadByte(0);

        logger.debug("NODE {}: Got RemoveFailedNode request.", nodeId);
        ZWaveNetworkEvent.State state;
        Report report = null;
        switch (incomingMessage.getMessagePayloadByte(1)) {
            case FAILED_NODE_OK:
                logger.debug("NODE {}: Unable to remove failed node as it has not failed!", nodeId);
                transactionComplete = true;
                state = ZWaveNetworkEvent.State.Failure;
                report = Report.FAILED_NODE_OK;
                break;
            case FAILED_NODE_REMOVED:
                logger.debug("NODE {}: Successfully removed node from controller!", nodeId);
                zController.notifyEventListeners(new ZWaveNetworkEvent(Type.DeleteNode, nodeId, State.Success));
                transactionComplete = true;
                state = ZWaveNetworkEvent.State.Success;
                report = Report.FAILED_NODE_REMOVED;
                break;
            case FAILED_NODE_NOT_REMOVED:
                logger.debug("NODE {}: Unable to remove failed node!", nodeId);
                transactionComplete = true;
                state = ZWaveNetworkEvent.State.Failure;
                report = Report.FAILED_NODE_NOT_REMOVED;
                break;
            default:
                logger.debug("NODE {}: Remove failed node failed with response 0x{}.", nodeId,
                        Integer.toHexString(incomingMessage.getMessagePayloadByte(1)));
                transactionComplete = true;
                state = ZWaveNetworkEvent.State.Failure;
                report = Report.FAILED_NODE_UNKNOWN_FAIL;
                break;
        }

        zController.notifyEventListeners(
                new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.RemoveFailedNodeID, nodeId, state, report));

        return true;
    }
}
