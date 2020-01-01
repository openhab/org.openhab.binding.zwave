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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.openhab.binding.zwave.internal.protocol.ZWaveAssociation;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveAssociationEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Multi Instance Association command class.
 * This allows reading and writing of node association parameters with instance values
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION")
public class ZWaveMultiAssociationCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiAssociationCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 3;

    private static final int MULTI_INSTANCE_MARKER = 0x00;

    private static final int MULTI_ASSOCIATIONCMD_SET = 1;
    private static final int MULTI_ASSOCIATIONCMD_GET = 2;
    private static final int MULTI_ASSOCIATIONCMD_REPORT = 3;
    private static final int MULTI_ASSOCIATIONCMD_REMOVE = 4;
    private static final int MULTI_ASSOCIATIONCMD_GROUPINGS_GET = 5;
    private static final int MULTI_ASSOCIATIONCMD_GROUPINGS_REPORT = 6;

    @XStreamOmitField
    private int updateAssociationsNode = 0;

    @XStreamOmitField
    private ZWaveAssociationGroup pendingAssociation = null;

    @XStreamOmitField
    private boolean initialiseDone = false;

    // This will be set when we query a node for the number of groups it supports
    private int maxGroups = 0;

    /**
     * Creates a new instance of the ZWaveMultiAssociationCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveMultiAssociationCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION;
    }

    /**
     * Processes a MULTI_ASSOCIATIONCMD_REPORT message.
     *
     * @param serialMessage
     *            the incoming message to process.
     * @param offset
     *            the offset position from which to start message processing.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = MULTI_ASSOCIATIONCMD_REPORT, name = "MULTI_ASSOCIATIONCMD_REPORT")
    public void handleMultiAssociationReport(ZWaveCommandClassPayload payload, int endpoint) {
        // Extract the group index
        int group = payload.getPayloadByte(2);
        // The max associations supported (0 if the requested group is not supported)
        int maxAssociations = payload.getPayloadByte(3);
        // Number of outstanding requests (if the group is large, it may come in multiple frames)
        int following = payload.getPayloadByte(4);

        if (maxAssociations == 0) {
            // Unsupported association group. Nothing to do!
            if (updateAssociationsNode == group) {
                logger.debug("NODE {}: All association groups acquired.", getNode().getNodeId());

                updateAssociationsNode = 0;

                // This is used for network management, so send a network event
                getController().notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssociationUpdate,
                        getNode().getNodeId(), ZWaveNetworkEvent.State.Success));
            }
            return;
        }

        logger.debug("NODE {}: association group {} has max associations {}", getNode().getNodeId(), group,
                maxAssociations);

        // Are we waiting to synchronise the start of a new group?
        if (pendingAssociation == null) {
            pendingAssociation = new ZWaveAssociationGroup(group);
        }

        if (payload.getPayloadLength() > 5) {
            logger.debug("NODE {}: Association group {} includes the following nodes:", getNode().getNodeId(), group);
            int dataLength = payload.getPayloadLength() - 5;
            int dataPointer = 0;

            // Process the root associations
            for (; dataPointer < dataLength; dataPointer++) {
                int node = payload.getPayloadByte(5 + dataPointer);
                if (node == MULTI_INSTANCE_MARKER) {
                    break;
                }
                logger.debug("NODE {}: Associated with Node {} in group {}", getNode().getNodeId(), node, group);

                // Add the node to the group
                pendingAssociation.addAssociation(new ZWaveAssociation(node));
            }

            // Process the multi instance associations
            if (dataPointer < dataLength) {
                logger.trace("NODE {}: Includes multi_instance associations", getNode().getNodeId());

                // Step over the marker
                dataPointer++;
                for (; dataPointer < dataLength; dataPointer += 2) {
                    int node = payload.getPayloadByte(5 + dataPointer);
                    int endpointId = payload.getPayloadByte(6 + dataPointer);
                    if (node == MULTI_INSTANCE_MARKER) {
                        break;
                    }
                    logger.debug("NODE {}: Associated with Node {} endpoint {} in group", getNode().getNodeId(), node,
                            endpointId, group);

                    // Add the node to the group
                    pendingAssociation.addAssociation(new ZWaveAssociation(node, endpointId));
                }
            }
        }

        // If this is the end of the group, update the list then let the listeners know
        if (following == 0) {
            // Clear the current information for this group
            ZWaveAssociationGroup associationGroup = getNode().getAssociationGroup(group);
            if (associationGroup != null) {
                // Update the group in the list
                getNode().getAssociationGroup(pendingAssociation.getIndex())
                        .setAssociations(pendingAssociation.getAssociations());
            }

            // Send an event to the users
            ZWaveAssociationEvent zEvent = new ZWaveAssociationEvent(getNode().getNodeId(), pendingAssociation);
            pendingAssociation = null;
            getController().notifyEventListeners(zEvent);
        }

        // Is this the end of the list
        if (following == 0 && group == updateAssociationsNode) {
            // This is the end of this group and the current 'get all groups' node
            // so we need to request the next group
            if (updateAssociationsNode < maxGroups) {
                updateAssociationsNode++;
                ZWaveCommandClassTransactionPayload transaction = getAssociationMessage(updateAssociationsNode);
                if (transaction != null) {
                    getController().sendData(transaction);
                }
            } else {
                logger.debug("NODE {}: All association groups acquired.", getNode().getNodeId());
                // We have reached our maxNodes, notify listeners we are done.

                updateAssociationsNode = 0;
                // This is used for network management, so send a network event
                getController().notifyEventListeners(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.AssociationUpdate,
                        getNode().getNodeId(), ZWaveNetworkEvent.State.Success));
            }
        }
    }

    /**
     * Processes a ASSOCIATIONCMD_GROUPINGSREPORT message.
     *
     * @param serialMessage
     *            the incoming message to process.
     * @param offset
     *            the offset position from which to start message processing.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = MULTI_ASSOCIATIONCMD_GROUPINGS_REPORT, name = "MULTI_ASSOCIATIONCMD_GROUPINGS_REPORT")
    public void handleMultiAssociationGroupingsReport(ZWaveCommandClassPayload payload, int endpoint) {
        maxGroups = payload.getPayloadByte(2);
        logger.debug("NODE {}: processGroupingsReport number of groups {}", getNode().getNodeId(), maxGroups);

        // Start the process to query these nodes
        updateAssociationsNode = 1;

        ZWaveCommandClassTransactionPayload transaction = getAssociationMessage(updateAssociationsNode);
        if (transaction != null) {
            getController().sendData(transaction);
        }

        initialiseDone = true;
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_SET command
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload setAssociationMessage(int group, int node, int endpoint) {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_SET", getNode().getNodeId());

        // We only use the multi-endpoint version here, even if endpoint is 0.
        // This is needed since at least in some devices using the multi-instance version
        // configures the device to send multi-instance responses.
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(group);

        // Version 2 doesn't allow endpoint to be 0
        if (getVersion() <= 2 && endpoint == 0) {
            outputData.write(node);
        } else {
            outputData.write(0);
            outputData.write(node);
            outputData.write(endpoint);
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MULTI_ASSOCIATIONCMD_SET).withPayload(outputData.toByteArray()).withPriority(TransactionPriority.Config)
                        .build();
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_REMOVE command
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload removeAssociationMessage(int group, int node, int endpoint) {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_REMOVE", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MULTI_ASSOCIATIONCMD_REMOVE).withPayload(group, 0, node, endpoint)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a ZWaveTransaction with the MULTI_ASSOCIATIONCMD_REMOVE command to remove all nodes
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload clearAssociationMessage(int group) {
        logger.debug(
                "NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_REMOVE node all, endpoint all, group {}",
                getNode().getNodeId(), group);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MULTI_ASSOCIATIONCMD_REMOVE).withPayload(group).withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_GET command
     *
     * @param group
     *            the association group to read
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getAssociationMessage(int group) {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_GET group {}",
                getNode().getNodeId(), group);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MULTI_ASSOCIATIONCMD_GET).withPayload(group).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(MULTI_ASSOCIATIONCMD_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the MULTI_ASSOCIATIONCMD_GROUPINGSGET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getGroupingsMessage() {
        logger.debug("NODE {}: Creating new message for command MULTI_ASSOCIATIONCMD_GROUPINGSGET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MULTI_ASSOCIATIONCMD_GROUPINGS_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(MULTI_ASSOCIATIONCMD_GROUPINGS_REPORT).build();
    }

    /**
     * Request all association groups.
     * This method requests the number of groups from a node, when that
     * replay is processed we request association group 1 and set flags so that
     * when the response is received the command class automatically
     * requests the next group. This continues until we reach the maximum
     * number of group the device reports to us or until the device returns
     * a group with no members.
     */
    public void getAllAssociations() {
        ZWaveCommandClassTransactionPayload transaction = getGroupingsMessage();
        if (transaction != null) {
            getController().sendData(transaction);
        }
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(getGroupingsMessage());
        }

        return result;
    }

    public int getMaxGroup() {
        return maxGroups;
    }
}
