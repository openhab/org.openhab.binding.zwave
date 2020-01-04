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

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent.State;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class ReplaceFailedNodeMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(ReplaceFailedNodeMessageClass.class);

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

    public ZWaveSerialPayload doRequest(int nodeId) {
        logger.debug("NODE {}: Marking node as having failed.", nodeId);

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.ReplaceFailedNode).withPayload(nodeId).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        if (transaction == null) {
            logger.debug("NODE {}: transaction not correlated for ReplaceFailedNodeMessageClass");
            return false;
        }
        logger.debug("Got ReplaceFailedNode response.");
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        Report report = null;
        switch (incomingMessage.getMessagePayloadByte(0)) {
            case FAILED_NODE_REMOVE_STARTED:
                // The replacing process started and now the new node must emit its node information frame to start the
                // assign process.
                logger.debug("NODE {}: Replace failed node successfully placed on stack.", nodeId);
                break;
            case FAILED_NODE_NOT_PRIMARY_CONTROLLER:
                // The replacing process was aborted because the controller is not a primary/inclusion/SIS controller.
                logger.error("NODE {}: Replace failed node failed as not Primary Controller for node!", nodeId);
                transaction.setTransactionCanceled();
                report = Report.FAILED_NODE_NOT_PRIMARY_CONTROLLER;
                break;
            case FAILED_NODE_NO_CALLBACK_FUNCTION:
                // The replacing process was aborted because no call back function is used.
                logger.error("NODE {}: Replace failed node failed as no callback function!", nodeId);
                transaction.setTransactionCanceled();
                report = Report.FAILED_NODE_NO_CALLBACK_FUNCTION;
                break;
            case FAILED_NODE_NOT_FOUND:
                // The replacing process aborted because the node was found, thereby not a failing node.
                logger.error("NODE {}: Replace failed node failed as node is functioning!", nodeId);
                transaction.setTransactionCanceled();
                report = Report.FAILED_NODE_NOT_FOUND;
                break;
            case FAILED_NODE_REMOVE_PROCESS_BUSY:
                // The replacing process is busy.
                logger.error("NODE {}: Replace failed node failed as Controller Busy!", nodeId);
                transaction.setTransactionCanceled();
                report = Report.FAILED_NODE_REMOVE_PROCESS_BUSY;
                break;
            case FAILED_NODE_REMOVE_FAIL:
                // The replacing process could not be started because of transmitter busy.
                logger.error("NODE {}: Replace failed node failed!", nodeId);
                transaction.setTransactionCanceled();
                report = Report.FAILED_NODE_REMOVE_FAIL;
                break;
            default:
                logger.error("NODE {}: Replace failed node not placed on stack due to error 0x{}.", nodeId,
                        Integer.toHexString(incomingMessage.getMessagePayloadByte(0)));

                transaction.setTransactionCanceled();
                report = Report.FAILED_NODE_UNKNOWN_FAIL;
                break;
        }

        // If this is a fail, then notify now, otherwise wait for the REQuest
        if (report != null) {
            zController.notifyEventListeners(
                    new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.ReplaceFailedNode, nodeId, State.Failure, report));
        }
        return true;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        if (transaction == null) {
            logger.debug("NODE {}: transaction not correlated for ReplaceFailedNodeMessageClass");
            return false;
        }
        int nodeId = transaction.getSerialMessage().getMessagePayloadByte(0);

        logger.debug("NODE {}: Got ReplaceFailedNode request.", nodeId);
        ZWaveNetworkEvent.State state;
        Report report = null;
        switch (incomingMessage.getMessagePayloadByte(1)) {// TODO: Should this be (&& 0x0f)?
            case FAILED_NODE_OK:
                // The node is working properly (removed from the failed nodes list). Replace process is stopped.
                logger.error("NODE {}: Unable to remove failed node as it is not a failed node!", nodeId);
                state = ZWaveNetworkEvent.State.Failure;
                report = Report.FAILED_NODE_OK;
                break;
            case FAILED_NODE_REMOVED:
                logger.debug("NODE {}: Successfully removed node from controller database!", nodeId);
                state = ZWaveNetworkEvent.State.Failure;
                report = Report.FAILED_NODE_REMOVED;
                break;
            case FAILED_NODE_NOT_REMOVED:
                logger.error("NODE {}: Unable to remove failed node!", nodeId);
                state = ZWaveNetworkEvent.State.Failure;
                report = Report.FAILED_NODE_NOT_REMOVED;
                break;
            default:
                logger.error("NODE {}: Replace failed node returned with response 0x{}.", nodeId,
                        Integer.toHexString(incomingMessage.getMessagePayloadByte(1)));
                state = ZWaveNetworkEvent.State.Failure;
                report = Report.FAILED_NODE_UNKNOWN_FAIL;
                break;
        }

        zController.notifyEventListeners(
                new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.ReplaceFailedNode, nodeId, state, report));

        return true;
    }
}
