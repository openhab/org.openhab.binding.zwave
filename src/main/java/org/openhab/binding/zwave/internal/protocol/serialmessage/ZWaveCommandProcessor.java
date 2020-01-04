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

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller.
 * <p>
 * This class is the base class for the serial message class. It handles the request from the application, and the
 * processing of the responses from the controller.
 * <p>
 * When a message is sent to the controller, the controller responds with a RESPONSE.
 * <p>
 * When the controller has further data, it responds with a REQUEST.
 * <p>
 * These calls map to the handleResponse and handleRequest methods which must be overridden by the individual classes.
 *
 * @author Chris Jackson
 */
public abstract class ZWaveCommandProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveCommandProcessor.class);

    private static HashMap<SerialMessage.SerialMessageClass, Class<? extends ZWaveCommandProcessor>> messageMap = null;

    public ZWaveCommandProcessor() {
    }

    /**
     * Checks if the incomingMessage is related to the transaction
     * This performs class dependent checks to see if the message is related.
     *
     * @return true if the incomingMessage is linked to the transaction
     */
    public boolean correlateTransactionResponse(ZWaveTransaction transaction, SerialMessage incomingMessage) {
        // If this is a response, check the callbackId
        if (transaction.getCallbackId() != incomingMessage.getCallbackId()) {
            logger.debug("NO callback match! ({} <> {})", transaction.getCallbackId(), incomingMessage.getCallbackId());
            return false;
        }

        logger.debug("Callback match!");
        return true;
    }

    /**
     * Method for handling the response from the controller
     *
     * @param zController the ZWave controller
     * @param lastSentMessage The original message we sent to the controller
     * @param incomingMessage The response from the controller
     * @return
     * @throws ZWaveSerialMessageException
     */
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.warn("TODO: {} unsupported RESPONSE.", incomingMessage.getMessageClass());
        return false;
    }

    /**
     * Method for handling the request from the controller
     *
     * @param zController the ZWave controller
     * @param lastSentMessage The original message we sent to the controller
     * @param incomingMessage The response from the controller
     * @return
     * @throws ZWaveSerialMessageException
     */
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.warn("TODO: {} unsupported REQUEST.", incomingMessage.getMessageClass());
        return false;
    }

    /**
     * Returns the message processor for the specified message class
     *
     * @param serialMessage The message class required to be processed
     * @return The message processor
     */
    public static ZWaveCommandProcessor getMessageDispatcher(SerialMessage.SerialMessageClass serialMessage) {
        if (messageMap == null) {
            messageMap = new HashMap<SerialMessage.SerialMessageClass, Class<? extends ZWaveCommandProcessor>>();
            messageMap.put(SerialMessage.SerialMessageClass.AddNodeToNetwork, AddNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.ApplicationCommandHandler,
                    ApplicationCommandMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.ApplicationUpdate, ApplicationUpdateMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.AssignReturnRoute, AssignReturnRouteMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.AssignSucReturnRoute,
                    AssignSucReturnRouteMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.DeleteReturnRoute, DeleteReturnRouteMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.DeleteSUCReturnRoute,
                    DeleteSucReturnRouteMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.GetRoutingInfo, GetRoutingInfoMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.GetVersion, GetVersionMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.GetSucNodeId, GetSucNodeIdMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.GetControllerCapabilities,
                    GetControllerCapabilitiesMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.IdentifyNode, IdentifyNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.MemoryGetId, MemoryGetIdMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RemoveFailedNodeID, RemoveFailedNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.IsFailedNodeID, IsFailedNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RemoveNodeFromNetwork, RemoveNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.ReplaceFailedNode, ReplaceFailedNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RequestNetworkUpdate,
                    RequestNetworkUpdateMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RequestNodeInfo, RequestNodeInfoMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.RequestNodeNeighborUpdate,
                    RequestNodeNeighborUpdateMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SendData, SendDataMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SerialApiGetCapabilities,
                    SerialApiGetCapabilitiesMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SerialApiGetInitData,
                    SerialApiGetInitDataMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SerialApiSetTimeouts,
                    SerialApiSetTimeoutsMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SerialApiSoftReset, SerialApiSoftResetMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SetSucNodeID, SetSucNodeMessageClass.class);
            messageMap.put(SerialMessage.SerialMessageClass.SetDefault, ControllerSetDefaultMessageClass.class);
        }

        Constructor<? extends ZWaveCommandProcessor> constructor;
        try {
            if (messageMap.get(serialMessage) == null) {
                logger.warn("SerialMessage class {} is not implemented!", serialMessage);
                return null;
            }
            constructor = messageMap.get(serialMessage).getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            logger.debug("Command processor error");
        }

        return null;
    }
}
