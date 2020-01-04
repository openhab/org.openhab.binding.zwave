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
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class RemoveNodeMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(RemoveNodeMessageClass.class);

    private final int REMOVE_NODE_ANY = 1;
    private final int REMOVE_NODE_CONTROLLER = 2;
    private final int REMOVE_NODE_SLAVE = 3;
    private final int REMOVE_NODE_STOP = 5;

    private final int REMOVE_NODE_STATUS_LEARN_READY = 1;
    private final int REMOVE_NODE_STATUS_NODE_FOUND = 2;
    private final int REMOVE_NODE_STATUS_REMOVING_SLAVE = 3;
    private final int REMOVE_NODE_STATUS_REMOVING_CONTROLLER = 4;
    private final int REMOVE_NODE_STATUS_DONE = 6;
    private final int REMOVE_NODE_STATUS_FAILED = 7;

    public ZWaveSerialPayload doRequestStart() {
        logger.debug("Setting controller into EXCLUSION mode.");

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.RemoveNodeFromNetwork).withPayload(REMOVE_NODE_ANY)
                .withRequiresData(false).build();
    }

    public ZWaveSerialPayload doRequestStop(boolean complete) {
        logger.debug("Ending EXCLUSION mode.");

        ZWaveSerialPayload payload = new ZWaveTransactionMessageBuilder(SerialMessageClass.RemoveNodeFromNetwork)
                .withPayload(REMOVE_NODE_STOP).withRequiresData(false).build();

        if (complete) {
            payload.setCallbackId(0);
        }
        return payload;
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {

        switch (incomingMessage.getMessagePayloadByte(1)) {
            case REMOVE_NODE_STATUS_LEARN_READY:
                logger.debug("Remove Node: Learn ready.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.ExcludeStart));
                if (transaction != null) {
                    transaction.setTransactionComplete();
                }
                break;
            case REMOVE_NODE_STATUS_NODE_FOUND:
                logger.debug("Remove Node: Node found for removal.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.ExcludeNodeFound));
                break;
            case REMOVE_NODE_STATUS_REMOVING_SLAVE:
                if (incomingMessage.getMessagePayloadByte(2) < 0 || incomingMessage.getMessagePayloadByte(2) > 232) {
                    break;
                }
                logger.debug("NODE {}: Removing slave.", incomingMessage.getMessagePayloadByte(2));
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.ExcludeSlaveFound,
                        incomingMessage.getMessagePayloadByte(2)));
                break;
            case REMOVE_NODE_STATUS_REMOVING_CONTROLLER:
                if (incomingMessage.getMessagePayloadByte(2) < 0 || incomingMessage.getMessagePayloadByte(2) > 232) {
                    break;
                }
                logger.debug("NODE {}: Removing controller.", incomingMessage.getMessagePayloadByte(2));
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.ExcludeControllerFound,
                        incomingMessage.getMessagePayloadByte(2)));
                break;
            case REMOVE_NODE_STATUS_DONE:
                if (incomingMessage.getMessagePayloadByte(2) < 0 || incomingMessage.getMessagePayloadByte(2) > 232) {
                    break;
                }
                logger.debug("Remove Node: Done.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.ExcludeDone,
                        incomingMessage.getMessagePayloadByte(2)));
                break;
            case REMOVE_NODE_STATUS_FAILED:
                logger.debug("Remove Node: Failed.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.ExcludeFail));
                break;
            default:
                logger.debug("Remove Node: Unknown request ({}).", incomingMessage.getMessagePayloadByte(1));
                break;
        }

        return true;
    }
}
