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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveDelayedPollEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Z-Wave protocol in a controller calls ApplicationControllerUpdate when a new node has been added or deleted from
 * the controller through the network management features. The Z-Wave protocol calls ApplicationControllerUpdate as a
 * result of using the API call ZW_RequestNodeInfo. The application can use this functionality to add/delete the node
 * information from any structures used in the Application layer. The Z-Wave protocol also calls
 * ApplicationControllerUpdate when a node information frame has been received and the protocol is not in a state where
 * it needs the node information.
 *
 * ApplicationControllerUpdate is called on the SUC each time a node is added/deleted by the primary controller.
 * ApplicationControllerUpdate is called on the SIS each time a node is added/deleted by the inclusion controller. When
 * a node request ZW_RequestNetWorkUpdate from the SUC/SIS then the ApplicationControllerUpdate is called for each node
 * change (add/delete) on the requesting node. ApplicationControllerUpdate is not called on a primary or inclusion
 * controller when a node is added/deleted. *
 *
 * @author Chris Jackson
 */
public class ApplicationUpdateMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(ApplicationUpdateMessageClass.class);

    @Override
    public boolean handleRequest(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        int nodeId;
        boolean result = true;
        UpdateState updateState = UpdateState.getUpdateState(incomingMessage.getMessagePayloadByte(0));

        switch (updateState) {
            case NODE_INFO_RECEIVED:
                // We've received a NIF, and this contains the node ID.
                nodeId = incomingMessage.getMessagePayloadByte(1);
                logger.debug("NODE {}: Application update request. Node information received. Transaction {}", nodeId,
                        transaction);

                int length = incomingMessage.getMessagePayloadByte(2);
                final ZWaveNode node = zController.getNode(nodeId);
                if (node == null) {
                    logger.debug("NODE {}: Application update request. Node not known!", nodeId);

                    // We've received a NIF from a node we don't know.
                    // This could happen if we add a new node using a different controller than OH.
                    // We handle this the same way as if included through an AddNode packet.
                    if (nodeId >= 1 && nodeId <= 232) {
                        zController
                                .notifyEventListeners(new ZWaveInclusionEvent(ZWaveInclusionState.IncludeDone, nodeId));
                    }
                    break;
                }

                // We've just received a message from a node, therefore it's ALIVE!
                node.setNodeState(ZWaveNodeState.ALIVE);
                node.resetResendCount();

                // Remember that we've received this so we can continue initialisation
                node.setApplicationUpdateReceived(true);

                // If we're finished initialisation, then we treat this like a HAIL
                if (node.getNodeInitStage() == ZWaveNodeInitStage.DONE) {
                    // If this node supports associations, then assume this should be handled through that mechanism
                    if (node.getCommandClass(CommandClass.COMMAND_CLASS_ASSOCIATION) == null) {
                        // If we receive an Application Update Request and the node is already
                        // fully initialised we assume this is a request to the controller to
                        // re-get the current node values
                        logger.debug("NODE {}: Application update request. Requesting node state.", nodeId);

                        // Send delayed poll event
                        zController
                                .notifyEventListeners(new ZWaveDelayedPollEvent(nodeId, 0, 175, TimeUnit.MILLISECONDS));
                    }
                } else {
                    List<CommandClass> nifClasses = new ArrayList<CommandClass>();

                    boolean control = false;
                    for (int i = 6; i < length + 3; i++) {
                        int data = incomingMessage.getMessagePayloadByte(i);

                        CommandClass commandClass = CommandClass.getCommandClass(data);
                        if (commandClass == null) {
                            logger.trace("NODE {}: Command class 0x{} is not known.", nodeId,
                                    Integer.toHexString(data));
                            continue;
                        }

                        // Keep a record in the node - mainly useful for the XML
                        nifClasses.add(commandClass);

                        // Check if this is the control marker
                        if (commandClass == CommandClass.COMMAND_CLASS_MARK) {
                            control = true;
                            continue;
                        }

                        // Add the new class if it doesn't exist
                        if (node.getCommandClass(commandClass) == null) {
                            ZWaveCommandClass zwaveCommandClass = ZWaveCommandClass.getInstance(data, node,
                                    zController);
                            if (zwaveCommandClass != null) {
                                logger.debug("NODE {}: Application update is adding command class {}.", nodeId,
                                        commandClass);
                                zwaveCommandClass.setControlClass(control);
                                node.addCommandClass(zwaveCommandClass);
                            }
                        }
                    }

                    node.updateNifClasses(nifClasses);
                }

                // If we have a transaction, then it's been correlated with the expected response.
                // We can therefore complete it
                if (transaction != null) {
                    transaction.setTransactionComplete();
                } else {
                    logger.debug("NODE {}: Application update - no transaction.", nodeId);
                }

                // Treat the node information frame as a wakeup
                // We have a delay here as in a number of devices the NIF isn't used as a wakeup, and the wakeup
                // notification is sent shortly after the NIF.
                // For these devices, if we treat the NIF as a wakeup, and start transmitting messages before the real
                // wakeup, then these messages will time out.
                // TODO: Prevent this running multiple times
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(250);
                        } catch (InterruptedException e) {
                            return;
                        }
                        node.setAwake(true);
                    }
                }.start();

                break;
            case NODE_INFO_REQ_FAILED:
                // The failed message doesn't contain the node number, so use the info from the request.
                if (transaction != null) {
                    nodeId = transaction.getNodeId();
                    logger.debug("NODE {}: Application update request. Node Info Request Failed.", nodeId);
                    transaction.setTransactionCanceled();
                } else {
                    logger.debug("Application update request. Node Info Request Failed (Unknown Node).");
                }

                result = false;
                break;
            default:
                logger.warn("TODO: Implement Application Update Request Handling of {} ({}).", updateState.getLabel(),
                        updateState.getKey());
                break;
        }

        return result;
    }

    /**
     * Update state enumeration. Indicates the type of application update state that was sent.
     *
     * @author Jan-Willem Spuij
     */
    public enum UpdateState {
        NODE_INFO_RECEIVED(0x84, "Node info received"),
        NODE_INFO_REQ_DONE(0x82, "Node info request done"),
        NODE_INFO_REQ_FAILED(0x81, "Node info request failed"),
        ROUTING_PENDING(0x80, "Routing pending"),
        NEW_ID_ASSIGNED(0x40, "New ID Assigned"),
        DELETE_DONE(0x20, "Delete done"),
        SUC_ID(0x10, "SUC ID");

        /**
         * A mapping between the integer code and its corresponding update state
         * class to facilitate lookup by code.
         */
        private static Map<Integer, UpdateState> codeToUpdateStateMapping;

        private int key;
        private String label;

        private UpdateState(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToUpdateStateMapping = new HashMap<Integer, UpdateState>();
            for (UpdateState s : values()) {
                codeToUpdateStateMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the update state code.
         * Returns null when there is no update state with code i.
         *
         * @param i the code to lookup
         * @return enumeration value of the update state.
         */
        public static UpdateState getUpdateState(int i) {
            if (codeToUpdateStateMapping == null) {
                initMapping();
            }

            return codeToUpdateStateMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }

    @Override
    public boolean correlateTransactionResponse(ZWaveTransaction transaction, SerialMessage incomingMessage) {
        if (transaction.getExpectedReplyClass() != incomingMessage.getMessageClass()) {
            return false;
        }

        // If the expected command class is defined, then check it
        // If the incoming node is 0, we will also correlate as this is an error to our last request
        try {
            UpdateState updateState = UpdateState.getUpdateState(incomingMessage.getMessagePayloadByte(0));

            switch (updateState) {
                case DELETE_DONE:
                case NEW_ID_ASSIGNED:
                case NODE_INFO_REQ_DONE:
                case NODE_INFO_RECEIVED:
                case ROUTING_PENDING:
                case SUC_ID:
                    // Default implementation
                    return transaction.getNodeId() == incomingMessage.getMessagePayloadByte(1);
                case NODE_INFO_REQ_FAILED:
                    // No node provided, so we correlate the transaction on the assumption that it is linked to our
                    // request.
                    return true;
                default:
                    break;
            }
        } catch (ZWaveSerialMessageException e) {
            logger.error("Error processing ApplicationUpdate {} ", incomingMessage, e);
            return false;
        }

        return true;
    }
}