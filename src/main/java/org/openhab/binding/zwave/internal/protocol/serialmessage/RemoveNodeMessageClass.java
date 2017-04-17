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
                .build();
    }

    public ZWaveSerialPayload doRequestStop() {
        logger.debug("Ending EXCLUSION mode.");

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.RemoveNodeFromNetwork)
                .withPayload(REMOVE_NODE_STOP).build();
    }

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        if (transaction == null) {
            logger.debug("NODE {}: transaction not correlated for RemoveNodeMessageClass");
            return false;
        }

        switch (incomingMessage.getMessagePayloadByte(1)) {
            case REMOVE_NODE_STATUS_LEARN_READY:
                logger.debug("Remove Node: Learn ready.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.ExcludeStart));
                transaction.setTransactionComplete();
                break;
            case REMOVE_NODE_STATUS_NODE_FOUND:
                logger.debug("Remove Node: Node found for removal.");
                break;
            case REMOVE_NODE_STATUS_REMOVING_SLAVE:
                if (incomingMessage.getMessagePayloadByte(2) < 1 || incomingMessage.getMessagePayloadByte(2) > 232) {
                    break;
                }
                logger.debug("NODE {}: Removing slave.", incomingMessage.getMessagePayloadByte(2));
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.ExcludeSlaveFound,
                        incomingMessage.getMessagePayloadByte(2)));
                break;
            case REMOVE_NODE_STATUS_REMOVING_CONTROLLER:
                if (incomingMessage.getMessagePayloadByte(2) < 1 || incomingMessage.getMessagePayloadByte(2) > 232) {
                    break;
                }
                logger.debug("NODE {}: Removing controller.", incomingMessage.getMessagePayloadByte(2));
                zController.notifyEventListeners(new ZWaveInclusionEvent(
                        ZWaveInclusionEvent.Type.ExcludeControllerFound, incomingMessage.getMessagePayloadByte(2)));
                break;
            case REMOVE_NODE_STATUS_DONE:
                if (incomingMessage.getMessagePayloadByte(2) < 1 || incomingMessage.getMessagePayloadByte(2) > 232) {
                    break;
                }
                logger.debug("Remove Node: Done.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.ExcludeDone,
                        incomingMessage.getMessagePayloadByte(2)));
                break;
            case REMOVE_NODE_STATUS_FAILED:
                logger.debug("Remove Node: Failed.");
                zController.notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.ExcludeFail));
                break;
            default:
                logger.debug("Remove Node: Unknown request ({}).", incomingMessage.getMessagePayloadByte(1));
                break;
        }

        return true;
    }
}
