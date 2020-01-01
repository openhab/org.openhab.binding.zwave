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
 * Handles the Association command class. This allows reading and writing of node association parameters
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_ASSOCIATION")
public class ZWaveAssociationCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveAssociationCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 2;

    private static final int ASSOCIATIONCMD_SET = 1;
    private static final int ASSOCIATIONCMD_GET = 2;
    private static final int ASSOCIATIONCMD_REPORT = 3;
    private static final int ASSOCIATIONCMD_REMOVE = 4;
    private static final int ASSOCIATIONCMD_GROUPINGS_GET = 5;
    private static final int ASSOCIATIONCMD_GROUPINGS_REPORT = 6;

    @XStreamOmitField
    private int updateAssociationsNode = 0;

    @XStreamOmitField
    private ZWaveAssociationGroup pendingAssociation = null;

    // This will be set when we query a node for the number of groups it supports
    private int maxGroups = 0;

    @XStreamOmitField
    private boolean initialiseDone = false;

    /**
     * Creates a new instance of the ZWaveAssociationCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveAssociationCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_ASSOCIATION;
    }

    /**
     * Processes a ASSOCIATIONCMD_REPORT message.
     *
     * @param serialMessage
     *            the incoming message to process.
     * @param offset
     *            the offset position from which to start message processing.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = ASSOCIATIONCMD_REPORT, name = "ASSOCIATIONCMD_REPORT")
    public void handleAssociationReport(ZWaveCommandClassPayload payload, int endpoint) {
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

        if (payload.getPayloadLength() > 4) {
            logger.debug("NODE {}: association group {} includes the following nodes:", getNode().getNodeId(), group);
            int numAssociations = payload.getPayloadLength() - (5);
            for (int cnt = 0; cnt < numAssociations; cnt++) {
                int node = payload.getPayloadByte(5 + cnt);
                logger.debug("Node {}", node);

                // Add the node to the group
                pendingAssociation.addAssociation(new ZWaveAssociation(node));
            }
        }

        // If this is the end of the group, update the list then let the listeners know
        if (following == 0) {
            // Update the group in the list
            getNode().getAssociationGroup(pendingAssociation.getIndex())
                    .setAssociations(pendingAssociation.getAssociations());

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
                ZWaveCommandClassTransactionPayload outputMessage = getAssociationMessage(updateAssociationsNode);
                if (outputMessage != null) {
                    getController().sendData(outputMessage);
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
    @ZWaveResponseHandler(id = ASSOCIATIONCMD_GROUPINGS_REPORT, name = "ASSOCIATIONCMD_GROUPINGS_REPORT")
    public void handleAssociationGroupingsReport(ZWaveCommandClassPayload payload, int endpoint) {
        maxGroups = payload.getPayloadByte(2);
        logger.debug("NODE {}: processGroupingsReport number of groups {}", getNode().getNodeId(), maxGroups);

        initialiseDone = true;

        // Add an association for each group if it doesn't exist
        for (int groupId = 1; groupId <= maxGroups; groupId++) {
            if (getNode().getAssociationGroup(groupId) == null) {
                ZWaveAssociationGroup group = new ZWaveAssociationGroup(groupId);
                getNode().setAssociationGroup(group);
            }
        }
    }

    /**
     * Gets a SerialMessage with the ASSOCIATIONCMD_SET command
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload setAssociationMessage(int group, int node) {
        logger.debug("NODE {}: Creating new message for application command ASSOCIATIONCMD_SET, group={}, node={}",
                getNode().getNodeId(), group, node);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                ASSOCIATIONCMD_SET).withPayload((group & 0xff), (node & 0xff)).withPriority(TransactionPriority.Config)
                        .build();
    }

    /**
     * Gets a SerialMessage with the ASSOCIATIONCMD_REMOVE command
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload removeAssociationMessage(int group, int node) {
        logger.debug("NODE {}: Creating new message for application command ASSOCIATIONCMD_REMOVE group={}, node={}",
                getNode().getNodeId(), group, node);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                ASSOCIATIONCMD_REMOVE).withPayload((group & 0xff), (node & 0xff))
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a ZWaveTransaction with the ASSOCIATIONCMD_REMOVE command
     *
     * @param group
     *            the association group
     * @param node
     *            the node to add to the specified group
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload clearAssociationMessage(int group) {
        logger.debug("NODE {}: Creating new message for application command ASSOCIATIONCMD_REMOVE group={}, node=all",
                getNode().getNodeId(), group);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                ASSOCIATIONCMD_REMOVE).withPayload((group & 0xff)).withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a ZWaveTransaction with the ASSOCIATIONCMD_GET command
     *
     * @param group
     *            the association group to read
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getAssociationMessage(int group) {
        logger.debug("NODE {}: Creating new message for application command ASSOCIATIONCMD_GET group {}",
                getNode().getNodeId(), group);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                ASSOCIATIONCMD_GET).withExpectedResponseCommand(ASSOCIATIONCMD_REPORT).withPayload(group)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the ASSOCIATIONCMD_GROUPINGSGET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getGroupingsMessage() {
        logger.debug("NODE {}: Creating new message for application command ASSOCIATIONCMD_GROUPINGSGET",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                ASSOCIATIONCMD_GROUPINGS_GET).withExpectedResponseCommand(ASSOCIATIONCMD_GROUPINGS_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Request all association groups.
     * This method requests the number of groups from a node, when that replay is processed we request association group
     * 1 and set flags so that when the response is received the command class automatically requests the next group.
     * This continues until we reach the maximum number of group the device reports to us or until the device returns a
     * group with no members.
     *
     */
    public void getAllAssociations() {
        updateAssociationsNode = 1;
        ZWaveCommandClassTransactionPayload payload = getAssociationMessage(updateAssociationsNode);
        if (payload != null) {
            getController().sendData(payload);
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
