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
package org.openhab.binding.zwave.internal.protocol;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.internal.HexToIntegerConverter;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCRC16EncapsulationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiCommandCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiInstanceCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveVersionCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.impl.CommandClassSecurityV1;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeStatusEvent;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStageAdvancer;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Z-Wave node class. Represents a node in the Z-Wave network.
 *
 * @author Chris Jackson
 * @author Brian Crosby
 */
@XStreamAlias("node")
public class ZWaveNode {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveNode.class);

    @XStreamOmitField
    private ZWaveController controller;
    @XStreamOmitField
    private ZWaveNodeInitStageAdvancer nodeInitStageAdvancer;
    @XStreamOmitField
    private ZWaveNodeState nodeState;

    @XStreamConverter(HexToIntegerConverter.class)
    private int homeId = Integer.MAX_VALUE;
    private int nodeId = Integer.MAX_VALUE;
    private int version = Integer.MAX_VALUE;

    @XStreamConverter(HexToIntegerConverter.class)
    private int manufacturer = Integer.MAX_VALUE;
    @XStreamConverter(HexToIntegerConverter.class)
    private int deviceId = Integer.MAX_VALUE;
    @XStreamConverter(HexToIntegerConverter.class)
    private int deviceType = Integer.MAX_VALUE;

    // private String deviceFactoryId;
    private String deviceSerialId;

    private boolean listening; // i.e. sleeping
    private boolean frequentlyListening;
    private boolean routing;
    @SuppressWarnings("unused")
    private boolean security;
    private boolean beaming;
    @SuppressWarnings("unused")
    private int maxBaudRate;

    @XStreamOmitField
    private Timer timer = null;
    @XStreamOmitField
    private TimerTask timerTask = null;
    @XStreamOmitField
    private boolean awake = false;

    // Half the period to wait before telling a sleeping node to sleep again
    private final int sleepDelay = 1000;

    // Keep the NIF - just used for information and debug in the XML
    @SuppressWarnings("unused")
    private List<CommandClass> nodeInformationFrame = null;

    // Stores the list of association groups
    private final Map<Integer, ZWaveAssociationGroup> associationGroups = new ConcurrentHashMap<Integer, ZWaveAssociationGroup>();

    // Endpoint
    private final Map<Integer, ZWaveEndpoint> endpoints = new ConcurrentHashMap<Integer, ZWaveEndpoint>();

    private final List<Integer> nodeNeighbors = new ArrayList<Integer>();
    private Date lastSent = null;
    private Date lastReceived = null;

    @XStreamOmitField
    private boolean applicationUpdateReceived = false;

    @XStreamOmitField
    private int resendCount = 0;

    @XStreamOmitField
    private int receiveCount = 0;
    @XStreamOmitField
    private int sendCount = 0;
    @XStreamOmitField
    private int deadCount = 0;
    @XStreamOmitField
    private Date deadTime;
    @XStreamOmitField
    private int retryCount = 0;

    @XStreamOmitField
    Long inclusionTimer = null;

    /**
     * Constructor. Creates a new instance of the ZWaveNode class.
     *
     * @param homeId the home ID to use.
     * @param nodeId the node ID to use.
     * @param controller the wave controller instance
     */
    public ZWaveNode(int homeId, int nodeId, ZWaveController controller) {
        nodeState = ZWaveNodeState.ALIVE; // TODO: ??? INITIALIZING;
        this.homeId = homeId;
        this.nodeId = nodeId;
        this.controller = controller;
        this.nodeInitStageAdvancer = new ZWaveNodeInitStageAdvancer(this, controller);

        ZWaveEndpoint endpoint0 = new ZWaveEndpoint(0);
        endpoints.put(0, endpoint0);
    }

    /**
     * Closes the node and stops any running processes
     */
    public void close() {
        if (nodeInitStageAdvancer != null) {
            nodeInitStageAdvancer.stopInitialisation();
            nodeInitStageAdvancer = null;
        }
    }

    /**
     * Configures the node after it's been restored from file.
     * NOTE: XStream doesn't run any default constructor. So, any initialisation
     * made in a constructor, or statically, won't be performed!!!
     * Set defaults here if it's important!!!
     *
     * @param controller the wave controller instance
     */
    public void setRestoredFromConfigfile(ZWaveController controller) {
        nodeState = ZWaveNodeState.ALIVE;

        this.controller = controller;

        // Create the initialisation advancer and tell it we've loaded from file
        nodeInitStageAdvancer = new ZWaveNodeInitStageAdvancer(this, controller);
        nodeInitStageAdvancer.setRestoredFromConfigfile();
    }

    /**
     * Gets the node ID.
     *
     * @return the node id
     */
    public int getNodeId() {
        return nodeId;
    }

    /**
     * Gets whether the node is listening.
     *
     * @return boolean indicating whether the node is listening or not.
     */
    public boolean isListening() {
        return listening;
    }

    /**
     * Sets whether the node is listening.
     *
     * @param listening
     */
    public void setListening(boolean listening) {
        this.listening = listening;
    }

    /**
     * Gets whether the node is frequently listening.
     * Frequently listening is responding to a beam signal. Apart from
     * increased latency, nothing else is noticeable from the serial api
     * side.
     *
     * @return boolean indicating whether the node is frequently
     *         listening or not.
     */
    public boolean isFrequentlyListening() {
        return frequentlyListening;
    }

    /**
     * Sets whether the node is frequently listening.
     * Frequently listening is responding to a beam signal. Apart from
     * increased latency, nothing else is noticeable from the serial api
     * side.
     *
     * @param frequentlyListening indicating whether the node is frequently
     *            listening or not.
     */
    public void setFrequentlyListening(boolean frequentlyListening) {
        this.frequentlyListening = frequentlyListening;
    }

    /**
     * Gets whether the node is dead.
     *
     * @return
     */
    public boolean isDead() {
        if (nodeState == ZWaveNodeState.DEAD || nodeState == ZWaveNodeState.FAILED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the node state and alert the listeners that the state has changed
     *
     * @param state the new {@link ZWaveNodeState}
     */
    public void setNodeState(ZWaveNodeState state) {
        // Make sure we only handle real state changes
        if (state == nodeState) {
            return;
        }

        // Notify the listeners
        ZWaveEvent zEvent = new ZWaveNodeStatusEvent(getNodeId(), state);
        controller.notifyEventListeners(zEvent);

        switch (state) {
            case AWAKE:
                state = ZWaveNodeState.ALIVE;
                break;

            case ASLEEP:
                state = ZWaveNodeState.ALIVE;
                break;

            case ALIVE:
                logger.debug("NODE {}: Node is ALIVE. Init stage is {}.", nodeId, getNodeInitStage().toString());

                // Reset the resend counter
                resendCount = 0;
                break;

            case DEAD:
                // If the node is failed, then we don't allow transitions to DEAD
                // The only valid state change from FAILED is to ALIVE
                if (nodeState == ZWaveNodeState.FAILED) {
                    return;
                }
            case FAILED:
                deadCount++;
                deadTime = Calendar.getInstance().getTime();
                logger.debug("NODE {}: Node is DEAD.", nodeId);
                break;

            case INITIALIZING:
                break;
        }

        controller.notifyEventListeners(new ZWaveNodeStatusEvent(getNodeId(), state));
        nodeState = state;
    }

    /**
     * Gets the home ID
     *
     * @return the homeId
     */
    public Integer getHomeId() {
        return homeId;
    }

    /**
     * Gets the manufacturer of the node.
     *
     * @return the manufacturer. If not set Integer.MAX_VALUE is returned.
     */
    public int getManufacturer() {
        return manufacturer;
    }

    /**
     * Sets the manufacturer of the node.
     *
     * @param tempMan the manufacturer to set
     */
    public void setManufacturer(int manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * Gets the device id of the node.
     *
     * @return the deviceId. If not set Integer.MAX_VALUE is returned.
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device id of the node.
     *
     * @param deviceId the device to set
     */
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Gets the device type of the node.
     *
     * @return the deviceType. If not set Integer.MAX_VALUE is returned.
     */
    public int getDeviceType() {
        return deviceType;
    }

    /**
     * Sets the device type of the node.
     *
     * @param deviceType the deviceType to set
     */
    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * Get the date/time the node was last updated (ie a frame was received from it).
     *
     * @return the lastUpdated time
     */
    public Date getLastReceived() {
        return lastReceived;
    }

    /**
     * Get the date/time we last sent a frame to the node.
     *
     * @return the lastSent
     */
    public Date getLastSent() {
        return lastSent;
    }

    /**
     * Gets the node state.
     *
     * @return the nodeState
     */
    public ZWaveNodeState getNodeState() {
        return nodeState;
    }

    /**
     * Gets the node stage.
     *
     * @return the nodeStage
     */
    public ZWaveNodeInitStage getNodeInitStage() {
        if (nodeInitStageAdvancer == null) {
            return ZWaveNodeInitStage.EMPTYNODE;
        }
        return nodeInitStageAdvancer.getCurrentStage();
    }

    /**
     * Gets the initialization state
     *
     * @return true if initialization has been completed
     */
    public boolean isInitializationComplete() {
        if (nodeInitStageAdvancer == null) {
            return false;
        }
        return nodeInitStageAdvancer.isInitializationComplete();
    }

    /**
     * Sets the node stage.
     *
     * @param nodeStage the nodeStage to set
     */
    public void setNodeStage(ZWaveNodeInitStage nodeStage) {
        nodeInitStageAdvancer.startInitialisation(nodeStage);
    }

    /**
     * Gets the node version
     *
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the node version.
     *
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Gets the node application firmware version
     *
     * @return the version
     */
    public String getApplicationVersion() {
        ZWaveVersionCommandClass versionCmdClass = (ZWaveVersionCommandClass) this
                .getCommandClass(CommandClass.COMMAND_CLASS_VERSION);
        if (versionCmdClass == null) {
            return "0.0";
        }

        String appVersion = versionCmdClass.getApplicationVersion();
        if (appVersion == null) {
            logger.trace("NODE {}: App version requested but version is unknown", this.getNodeId());
            return "0.0";
        }

        return appVersion;
    }

    /**
     * Gets whether the node is routing messages.
     *
     * @return the routing
     */
    public boolean isRouting() {
        return routing;
    }

    /**
     * Sets whether the node is routing messages.
     *
     * @param routing the routing to set
     */
    public void setRouting(boolean routing) {
        this.routing = routing;
    }

    /**
     * Gets the time stamp the node was last queried.
     *
     * @return the queryStageTimeStamp
     */
    public Date getQueryStageTimeStamp() {
        return nodeInitStageAdvancer.getQueryStageTimeStamp();
    }

    /**
     * Increments the resend counter.
     * On three increments the node stage is set to DEAD and no
     * more messages will be sent.
     * This is only used for SendData messages.
     */
    public void incrementResendCount() {
        if (++resendCount >= 3) {
            setNodeState(ZWaveNodeState.DEAD);
        }
        retryCount++;
    }

    /**
     * Resets the resend counter and possibly resets the node stage to DONE when previous initialization was complete.
     * Note that if the node is DEAD, then the nodeStage stays DEAD
     */
    public void resetResendCount() {
        resendCount = 0;
        if (nodeInitStageAdvancer.isInitializationComplete() == true && isDead() == false) {
            logger.debug("NODE {}: resetResendCount initComplete={} isDead={}", nodeId,
                    nodeInitStageAdvancer.isInitializationComplete(), isDead());
        }
    }

    public ZWaveEndpoint getEndpoint(int endpoint) {
        return endpoints.get(endpoint);
    }

    public ZWaveEndpoint addEndpoint(int endpointNumber) {
        if (endpoints.containsKey(endpointNumber)) {
            logger.debug("NODE {}: Endpoint {} already exists", nodeId, endpointNumber);
            return endpoints.get(endpointNumber);
        }
        ZWaveEndpoint endpoint = new ZWaveEndpoint(endpointNumber);
        endpoints.put(endpointNumber, endpoint);

        logger.debug("NODE {}: Endpoint {} added", nodeId, endpointNumber);

        return endpoint;
    }

    public int getEndpointCount() {
        return endpoints.size();
    }

    /**
     * Returns the device class of the node.
     *
     * @return the deviceClass
     */
    public ZWaveDeviceClass getDeviceClass() {
        return endpoints.get(0).getDeviceClass();
    }

    /**
     * Returns the Command classes this node implements.
     *
     * @return the command classes.
     */
    public Collection<ZWaveCommandClass> getCommandClasses(int endpoint) {
        return endpoints.get(endpoint).getCommandClasses();
    }

    /**
     * Returns a commandClass object this node implements.
     * Returns null if command class is not supported by this node.
     *
     * @param commandClass The command class to get.
     * @return the command class.
     */
    public ZWaveCommandClass getCommandClass(CommandClass commandClass) {
        return endpoints.get(0).getCommandClass(commandClass);
    }

    /**
     * Returns whether a node supports this command class.
     *
     * @param commandClass the command class to check
     * @return true if the command class is supported, false otherwise.
     */
    public boolean supportsCommandClass(CommandClass commandClass) {
        return endpoints.get(0).supportsCommandClass(commandClass);
    }

    /**
     * Adds a command class to the list of supported command classes by this node.
     * Does nothing if command class is already added.
     *
     * @param commandClass the command class instance to add.
     */
    public void addCommandClass(ZWaveCommandClass commandClass) {
        if (commandClass == null) {
            logger.debug("NODE {}: Attempt to add NULL command class", nodeId);
            return;
        }

        if (!endpoints.get(0).getCommandClasses().contains(commandClass)) {
            logger.debug("NODE {}: Adding command class {} to the list of supported command classes.", nodeId,
                    commandClass.getCommandClass());
            endpoints.get(0).addCommandClass(commandClass);

            // Register an event listener for this class if it is listening for them
            if (commandClass instanceof ZWaveEventListener) {
                controller.addEventListener((ZWaveEventListener) commandClass);
            }
        }
    }

    /**
     * Gets the command class from the endpoint if it exists. IF the class does not exist within the endpoint, it is
     * added.
     *
     * @param endpoint the {@link ZWaveEndpoint}
     * @param commandClass the {@link CommandClass}
     * @return the {@link ZWaveCommandClass} or null if the class is not supported
     */
    public ZWaveCommandClass getOrAddCommandClass(ZWaveEndpoint endpoint, CommandClass commandClass) {
        ZWaveCommandClass zwaveCommandClass = endpoint.getCommandClass(commandClass);

        // Apparently, this endpoint supports a command class that we did not learn about during initialization.
        // Let's add it now then to support handling this message.
        if (zwaveCommandClass != null) {
            return zwaveCommandClass;
        }

        logger.debug("NODE {}: Command class {} not found, trying to add it.", getNodeId(), commandClass,
                commandClass.getKey());

        zwaveCommandClass = ZWaveCommandClass.getInstance(commandClass.getKey(), this, controller);
        if (zwaveCommandClass == null) {
            // We got an unsupported command class, leave zwaveCommandClass as null
            logger.debug("NODE {}: Unsupported Z-Wave command class {}", getNodeId(), commandClass);
            return null;
        }

        logger.debug("NODE {}: Adding command class {} to endpoint {}", getNodeId(), commandClass,
                endpoint.getEndpointId());
        endpoint.addCommandClass(zwaveCommandClass);
        return zwaveCommandClass;
    }

    /**
     * Removes a command class from the node.
     * This is used to remove classes that a node may report it supports
     * but it doesn't respond to.
     *
     * @param commandClass The command class key
     */
    public void removeCommandClass(CommandClass commandClass) {
        endpoints.get(0).removeCommandClass(commandClass);
    }

    /**
     * Resolves a command class for this node. First endpoint is checked.
     * If endpoint == 0 or (endpoint != 1 and version of the multi instance
     * command == 1) then return a supported command class on the node itself.
     * If endpoint != 1 and version of the multi instance command == 2 then
     * first try command classes of endpoints. If not found the return a
     * supported command class on the node itself.
     * Returns null if a command class is not found.
     *
     * @param commandClass The command class to resolve.
     * @param endpointId the endpoint / instance to resolve this command class for.
     * @return the command class.
     */
    public ZWaveCommandClass resolveCommandClass(CommandClass commandClass, int endpointId) {
        if (commandClass == null) {
            return null;
        }

        if (endpoints.get(endpointId) == null) {
            return null;
        }
        // TODO: Note that this doesn't support multi-instance - only multi-channel!
        return endpoints.get(endpointId).getCommandClass(commandClass);
    }

    /**
     * Initialise the node
     */
    public void initialiseNode() {
        nodeInitStageAdvancer.startInitialisation();
    }

    public void initialiseNode(ZWaveNodeInitStage startStage) {
        nodeInitStageAdvancer.startInitialisation(startStage);
    }

    /**
     * Heal the node
     */
    public void healNode() {
        if (nodeInitStageAdvancer.isInitializationComplete() == false) {
            logger.debug("NODE {}: Can not start heal as initialisation is not complete ({}).", getNodeId(),
                    nodeInitStageAdvancer.getCurrentStage());
            return;
        }

        logger.debug("NODE {}: Starting network mesh heal.", getNodeId());
        nodeInitStageAdvancer.startInitialisation(ZWaveNodeInitStage.HEAL_START);
    }

    /**
     * Return a list with the nodes neighbors
     *
     * @return list of node IDs
     */
    public List<Integer> getNeighbors() {
        return nodeNeighbors;
    }

    /**
     * Clear the neighbor list
     */
    public void clearNeighbors() {
        nodeNeighbors.clear();
    }

    /**
     * Updates a nodes routing information
     * Generation of routes uses associations
     *
     * @param nodeId
     */
    public Set<Integer> getRoutingList() {
        logger.debug("NODE {}: Generate return routes list", nodeId);

        // Create a list of nodes this device is configured to talk to
        Set<Integer> routedNodes = new HashSet<Integer>();

        // Only update routes if this is a routing node
        if (isRouting() == false) {
            logger.debug("NODE {}: Node is not a routing node. No routes can be set.", nodeId);
            return Collections.emptySet();
        }

        // Get the number of association groups reported by this node
        int groups = associationGroups.size();
        if (groups != 0) {
            // Loop through each association group and add the node ID to the list
            for (int group = 1; group <= groups; group++) {
                if (associationGroups.get(group) == null) {
                    continue;
                }
                for (ZWaveAssociation associationNode : associationGroups.get(group).getAssociations()) {
                    routedNodes.add(associationNode.getNode());
                }
            }
        }

        // Add the wakeup destination node to the list for battery devices
        ZWaveWakeUpCommandClass wakeupCmdClass = (ZWaveWakeUpCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_WAKE_UP);
        if (wakeupCmdClass != null) {
            Integer wakeupNodeId = wakeupCmdClass.getTargetNodeId();
            routedNodes.add(wakeupNodeId);
        }

        // Are there any nodes to which we need to set routes?
        if (routedNodes.size() == 0) {
            logger.debug("NODE {}: No return routes required.", nodeId);
            return Collections.emptySet();
        }

        return routedNodes;
    }

    /**
     * Add a node ID to the neighbor list
     *
     * @param nodeId the node to add
     */
    public void addNeighbor(Integer nodeId) {
        nodeNeighbors.add(nodeId);
    }

    /**
     * Gets the number of times the node has been determined as DEAD
     *
     * @return dead count
     */
    public int getDeadCount() {
        return deadCount;
    }

    /**
     * Gets the number of times the node has been determined as DEAD
     *
     * @return dead count
     */
    public Date getDeadTime() {
        return deadTime;
    }

    /**
     * Gets the number of packets that have been resent to the node
     *
     * @return retry count
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Increments the sent packet counter and records the last sent time
     * This is simply used for statistical purposes to assess the health
     * of a node.
     */
    public void incrementSendCount() {
        sendCount++;
        lastSent = Calendar.getInstance().getTime();
    }

    /**
     * Increments the received packet counter and records the last received time
     * This is simply used for statistical purposes to assess the health
     * of a node.
     */
    public void incrementReceiveCount() {
        receiveCount++;
        lastReceived = Calendar.getInstance().getTime();
    }

    /**
     * Gets the number of packets sent to the node
     *
     * @return send count
     */
    public int getSendCount() {
        return sendCount;
    }

    /**
     * Gets the applicationUpdateReceived flag.
     * This is set to indicate that we have received the required information from the device
     *
     * @return true if information received
     */
    public boolean getApplicationUpdateReceived() {
        return applicationUpdateReceived;
    }

    /**
     * Sets the applicationUpdateReceived flag.
     * This is set to indicate that we have received the required information from the device
     *
     * @param received true if received
     */
    public void setApplicationUpdateReceived(boolean received) {
        applicationUpdateReceived = received;
    }

    @Override
    public String toString() {
        return String.format("Node %d: Manufacturer=%04X, Type=%04X, Id=%04X", nodeId, manufacturer, deviceType,
                deviceId);
    }

    /**
     * Updates the list of classes in the NIF.
     *
     * @param nif
     */
    public void updateNifClasses(List<CommandClass> nif) {
        nodeInformationFrame = nif;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }

    public boolean isSecure() {
        return endpoints.get(0).getSecureCommandClasses().size() != 0;
    }

    /**
     * Gets whether the node supports beaming
     *
     * @return true if the node supports beaming
     */
    public boolean isBeaming() {
        return beaming;
    }

    /**
     * Sets whether the node supports beaming.
     *
     * @param beaming true if beaming is supported
     */
    public void setBeaming(boolean beaming) {
        this.beaming = beaming;
    }

    public void setMaxBaud(int maxBaudRate) {
        this.maxBaudRate = maxBaudRate;
    }

    public boolean doesMessageRequireSecurityEncapsulation(int endpoint, ZWaveCommandClassPayload payload) {
        // Does this node support security at all?
        if (endpoints.get(0).getCommandClass(CommandClass.COMMAND_CLASS_SECURITY) == null) {
            logger.debug("NODE {}: SECURITY not supported", nodeId);
            return false;
        }

        final CommandClass commandClass = CommandClass.getCommandClass(payload.getCommandClassId());

        if (CommandClass.COMMAND_CLASS_SECURITY == commandClass) {
            logger.debug("NODE {}: SECURITY check internal", nodeId);
            // CommandClass.SECURITY is a special case because only some commands get encrypted
            return ZWaveSecurityCommandClass.doesCommandRequireSecurityEncapsulation(payload.getCommandClassCommand());
        }

        // PING should not be encrypted
        if (commandClass == CommandClass.COMMAND_CLASS_NO_OPERATION) {
            logger.debug("NODE {}: SECURITY doesn't encrypt PING", nodeId);
            return false;
        }

        // Does this endpoint support this class secure
        if (endpoints.get(endpoint).supportsSecureCommandClass(commandClass)) {
            logger.debug("NODE {}: SECURITY required on {}", nodeId, commandClass);
            return true;
        }

        logger.debug("NODE {}: SECURITY NOT required on {}", nodeId, commandClass);
        return false;
    }

    /**
     * Get an association group
     *
     * @param group
     * @return
     */
    public ZWaveAssociationGroup getAssociationGroup(int group) {
        return associationGroups.get(group);
    }

    /**
     * Set an association group
     *
     * @param group
     */
    public void setAssociationGroup(ZWaveAssociationGroup group) {
        associationGroups.put(group.getIndex(), group);
    }

    /**
     * Return a list of all association groups
     *
     * @return
     */
    public Map<Integer, ZWaveAssociationGroup> getAssociationGroups() {
        return associationGroups;
    }

    /**
     * Gets an association group
     *
     * @param groupId the group id
     * @return the {@link ZWaveCommandClassTransactionPayload}
     */
    public ZWaveCommandClassTransactionPayload getAssociation(int groupId) {
        if (endpoints.size() > 1) {
            ZWaveMultiAssociationCommandClass multiAssociationCommandClass = (ZWaveMultiAssociationCommandClass) getCommandClass(
                    CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
            if (multiAssociationCommandClass != null && groupId <= multiAssociationCommandClass.getMaxGroup()) {
                return multiAssociationCommandClass.getAssociationMessage(groupId);
            }
        }

        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION);
        if (associationCommandClass != null && groupId <= associationCommandClass.getMaxGroup()) {
            return associationCommandClass.getAssociationMessage(groupId);
        } else {
            logger.debug("NODE {}: Unable to get association group {}. Association={}", groupId,
                    associationCommandClass.getMaxGroup());
        }

        return null;
    }

    /**
     * Sets an association.
     * This method chooses the appropriate association command class to use for the device
     * and the endpoint.
     *
     * The ZWave spec requires that source and destination endpoints can't be 0, therefore
     * if the device endpoint is the root node, and the receive endpoint is 0, we use the
     * single instance command class, otherwise we use the multi instance class if it exists.
     *
     * @param groupId the group to be set
     * @param member the {@link ZWaveAssociation} to be set to report to (receive)
     * @return {@link ZWaveCommandClassTransactionPayload}
     */
    public ZWaveCommandClassTransactionPayload setAssociation(int groupId, ZWaveAssociation member) {
        if (endpoints.size() > 1) {
            ZWaveMultiAssociationCommandClass multiAssociationCommandClass = (ZWaveMultiAssociationCommandClass) getCommandClass(
                    CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
            if (multiAssociationCommandClass != null && groupId <= multiAssociationCommandClass.getMaxGroup()) {
                return multiAssociationCommandClass.setAssociationMessage(groupId, member.getNode(),
                        member.getEndpoint() == null ? 0 : member.getEndpoint());
            }
        }

        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION);
        if (associationCommandClass != null && groupId <= associationCommandClass.getMaxGroup()) {
            return associationCommandClass.setAssociationMessage(groupId, member.getNode());
        } else {
            logger.debug("NODE {}: Unable to set association group {}. Association={}", groupId,
                    associationCommandClass.getMaxGroup());
        }

        return null;
    }

    /**
     * Removes an association.
     * This method chooses the appropriate association command class to use for the device
     * and the endpoint.
     *
     * @param groupId the group to be set
     * @param member the {@link ZWaveAssociation} to be set to report to (receive)
     * @return {@link ZWaveCommandClassTransactionPayload}
     */
    public ZWaveCommandClassTransactionPayload removeAssociation(Integer groupId, ZWaveAssociation member) {
        if (endpoints.size() > 1) {
            ZWaveMultiAssociationCommandClass multiAssociationCommandClass = (ZWaveMultiAssociationCommandClass) getCommandClass(
                    CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
            if (multiAssociationCommandClass != null && groupId <= multiAssociationCommandClass.getMaxGroup()) {
                return multiAssociationCommandClass.removeAssociationMessage(groupId, member.getNode(),
                        member.getEndpoint() == null ? 0 : member.getEndpoint());
            }
        }

        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION);
        if (associationCommandClass != null && groupId <= associationCommandClass.getMaxGroup()) {
            return associationCommandClass.removeAssociationMessage(groupId, member.getNode());
        } else {
            logger.debug("NODE {}: Unable to remove association group {}. Association={}", groupId,
                    associationCommandClass.getMaxGroup());
        }

        return null;
    }

    /**
     * Clears an association group of all associations.
     *
     * @param groupId the group to be set
     * @return {@link ZWaveCommandClassTransactionPayload}
     */
    public ZWaveCommandClassTransactionPayload clearAssociation(Integer groupId) {
        if (endpoints.size() > 1) {
            ZWaveMultiAssociationCommandClass multiAssociationCommandClass = (ZWaveMultiAssociationCommandClass) getCommandClass(
                    CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
            if (multiAssociationCommandClass != null && groupId <= multiAssociationCommandClass.getMaxGroup()) {
                return multiAssociationCommandClass.clearAssociationMessage(groupId);
            }
        }

        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION);
        if (associationCommandClass != null && groupId <= associationCommandClass.getMaxGroup()) {
            return associationCommandClass.clearAssociationMessage(groupId);
        } else {
            logger.debug("NODE {}: Unable to clear association group {}. Association={}", groupId,
                    associationCommandClass.getMaxGroup());
        }

        return null;
    }

    // public void setFactoryId(String deviceFactoryId) {
    // this.deviceFactoryId = deviceFactoryId;
    // }

    // public String getFactoryId() {
    // return deviceFactoryId;
    // }

    public void setSerialNumber(String deviceSerialId) {
        this.deviceSerialId = deviceSerialId;
    }

    public String getSerialNumber() {
        return deviceSerialId;
    }

    /**
     * Encapsulates a serial message for sending to a multi-instance instance/ multi-channel endpoint on a node.
     *
     * A number of Z-Wave encapsulation Command Classes exist, they MUST be applied in the following order:
     * <ol>
     * <li>Any one of the following combinations:
     * <ol>
     * <li>Transport Service followed by Security
     * <li>Transport Service
     * <li>Security
     * <li>CRC16
     * </ol>
     * <li>Multi Channel
     * <li>Supervision
     * <li>Multi Command
     * <li>Schedule
     * <li>Command Class (payload), e.g. Basic Get
     * </ol>
     * Note: The Transport Service and CRC16 Command Classes are mutually exclusive as well as Security and CRC16.
     *
     * Security encapsulation is performed in the transaction manager since it needs to manage the NONCE and
     * encapsulation needs to be done at the time the message is sent.
     *
     * @param transaction the {@link ZWaveCommandClassTransactionPayload} to encapsulate
     * @param commandClass the command class used to generate the message.
     * @param endpointId the instance / endpoint to encapsulate the message for
     * @return SerialMessage on success, null on failure.
     */
    public ZWaveCommandClassTransactionPayload encapsulate(ZWaveCommandClassTransactionPayload transaction,
            int endpointId) {
        ZWaveMultiInstanceCommandClass multiInstanceCommandClass;
        logger.trace("NODE {}: Encapsulating message, endpoint {}", getNodeId(), endpointId);

        if (transaction == null) {
            return null;
        }

        // Encapsulation the COMMAND_CLASS_SCHEDULE class

        // Encapsulation the COMMAND_CLASS_MULTI_CMD class

        // Encapsulation the COMMAND_CLASS_SUPERVISION class

        // Encapsulation the COMMAND_CLASS_MULTI_CHANNEL class
        if (endpointId != 0) {
            multiInstanceCommandClass = (ZWaveMultiInstanceCommandClass) getCommandClass(
                    CommandClass.COMMAND_CLASS_MULTI_CHANNEL);

            if (multiInstanceCommandClass == null) {
                logger.warn("NODE {}: Encapsulating message, instance / endpoint {} failed, will discard message.",
                        getNodeId(), endpointId);
                return null;
            }

            logger.debug("NODE {}: Encapsulating message, instance / endpoint {}", getNodeId(), endpointId);
            switch (multiInstanceCommandClass.getVersion()) {
                case 1:
                    transaction = multiInstanceCommandClass.getMultiInstanceEncapMessage(transaction, endpointId);
                    break;
                default:
                case 2:
                    transaction = multiInstanceCommandClass.getMultiChannelEncapMessage(transaction, endpointId);
                    break;
            }
        }

        // Check if we need to secure this message
        if (doesMessageRequireSecurityEncapsulation(0, transaction)) {
            logger.debug("NODE {}: Command Class {} is required to be secured", nodeId,
                    CommandClass.getCommandClass(transaction.getCommandClassId()));

            transaction.setRequiresSecurity();
        } else {
            logger.debug("NODE {}: Command Class {} is NOT required to be secured", nodeId,
                    CommandClass.getCommandClass(transaction.getCommandClassId()));
            // Encapsulation the COMMAND_CLASS_CRC16 class if we don't utilise security
        }

        return transaction;
    }

    /**
     * Decapsulates a serial message for sending to a multi-instance instance/ multi-channel endpoint on a node.
     * <p>
     * Any command classes that are not already known (eg through the NIF or manually added through the database)
     * are ignored. This is done to avoid adding random classes if corrupt frames are received.
     * <p>
     * A number of Z-Wave encapsulation Command Classes exist, they MUST be applied in the following order:
     * <ol>
     * <li>Any one of the following combinations:
     * <ol>
     * <li>Transport Service followed by Security
     * <li>Transport Service
     * <li>Security
     * <li>CRC16
     * </ol>
     * <li>Multi Channel
     * <li>Supervision
     * <li>Multi Command
     * <li>Schedule
     * <li>Command Class (payload), e.g. Basic Get
     * </ol>
     * Note: The Transport Service and CRC16 Command Classes are mutually exclusive as well as Security and CRC16.
     *
     * @param transaction the {@link ZWaveCommandClassPayload} to process
     * @param commandClass the command class used to generate the message.
     * @param endpointId the instance / endpoint to encapsulate the message for
     * @return list of raw commands that were processed on success, null on failure.
     */
    public List<ZWaveCommandClassPayload> processCommand(ZWaveCommandClassPayload payload) {
        // Sanity check incoming message
        if (payload == null || payload.getPayloadLength() == 0) {
            return null;
        }

        // We've just received a message from a node, therefore it's ALIVE!
        setNodeState(ZWaveNodeState.ALIVE);

        resetResendCount();
        incrementReceiveCount();

        boolean securityDecapOk = false;

        if (payload.getCommandClassId() == CommandClass.COMMAND_CLASS_TRANSPORT_SERVICE.getKey()) {
            logger.debug("NODE {}: Decapsulating COMMAND_CLASS_TRANSPORT_SERVICE", getNodeId());

        } else if (payload.getCommandClassId() == CommandClass.COMMAND_CLASS_SECURITY.getKey()
                && (payload.getCommandClassCommand() == CommandClassSecurityV1.SECURITY_MESSAGE_ENCAPSULATION || payload
                        .getCommandClassCommand() == CommandClassSecurityV1.SECURITY_MESSAGE_ENCAPSULATION_NONCE_GET)) {
            logger.debug("NODE {}: Decapsulating COMMAND_CLASS_SECURITY", getNodeId());

            if (endpoints.get(0) == null) {
                logger.debug("NODE {}: No endpoint 0!", getNodeId());
                return null;
            }

            ZWaveSecurityCommandClass securityCommandClass = (ZWaveSecurityCommandClass) endpoints.get(0)
                    .getCommandClass(CommandClass.COMMAND_CLASS_SECURITY);
            if (securityCommandClass == null) {
                logger.debug("NODE {}: COMMAND_CLASS_SECURITY not found in endpoint 0", getNodeId());

                securityCommandClass = (ZWaveSecurityCommandClass) ZWaveCommandClass
                        .getInstance(CommandClass.COMMAND_CLASS_SECURITY.getKey(), this, controller);
                if (securityCommandClass != null) {
                    logger.debug("NODE {}: Adding COMMAND_CLASS_SECURITY", nodeId);
                    securityCommandClass.setNetworkKey(controller.getSecurityKey());
                    addCommandClass(securityCommandClass);
                } else {
                    logger.debug("NODE {}: Unable to instantiate COMMAND_CLASS_SECURITY", nodeId);
                    return null;
                }
            }

            byte[] cleartextData = securityCommandClass.getSecurityMessageDecapsulation(payload.getPayloadBuffer());
            if (cleartextData == null) {
                return null;
            }

            payload = new ZWaveCommandClassPayload(cleartextData);

            securityDecapOk = true;
        } else if (payload.getCommandClassId() == CommandClass.COMMAND_CLASS_CRC_16_ENCAP.getKey()
                && payload.getCommandClassCommand() == 1) {
            logger.debug("NODE {}: Decapsulating COMMAND_CLASS_CRC_16_ENCAP", getNodeId());

            if (endpoints.get(0) == null) {
                logger.debug("NODE {}: No endpoint 0!", getNodeId());
                return null;
            }
            ZWaveCRC16EncapsulationCommandClass crcCommandClass = (ZWaveCRC16EncapsulationCommandClass) getOrAddCommandClass(
                    endpoints.get(0), CommandClass.COMMAND_CLASS_CRC_16_ENCAP);
            if (crcCommandClass == null) {
                logger.debug("NODE {}: COMMAND_CLASS_CRC_16_ENCAP not found", getNodeId());
                return null;
            }

            payload = crcCommandClass.handleCrcEncap(payload);
            if (payload == null) {
                // CRC Failed
                return null;
            }
        }

        int endpointNumber = 0;

        if (payload.getCommandClassId() == CommandClass.COMMAND_CLASS_MULTI_CHANNEL.getKey()
                && (payload.getCommandClassCommand() == 6 || payload.getCommandClassCommand() == 13)) {
            logger.debug("NODE {}: Decapsulating COMMAND_CLASS_MULTI_CHANNEL", getNodeId());

            if (endpoints.get(0) == null) {
                logger.debug("NODE {}: No endpoint 0!", getNodeId());
                return null;
            }
            ZWaveMultiInstanceCommandClass multichannelCommandClass = (ZWaveMultiInstanceCommandClass) getOrAddCommandClass(
                    endpoints.get(0), CommandClass.COMMAND_CLASS_MULTI_CHANNEL);
            if (multichannelCommandClass == null) {
                logger.debug("NODE {}: COMMAND_CLASS_MULTI_CHANNEL not found", getNodeId());
                return null;
            }

            // Check that the length is long enough for the encapsulated command to be included
            if (payload.getCommandClassCommand() == 6 && payload.getPayloadLength() > 4) {
                // MULTI_INSTANCE_ENCAP
                endpointNumber = payload.getPayloadByte(2);

                payload = new ZWaveCommandClassPayload(payload, 3);
            } else if (payload.getCommandClassCommand() == 13 && payload.getPayloadLength() > 5) {
                // MULTI_CHANNEL_ENCAP
                endpointNumber = multichannelCommandClass.getSourceEndpoint(payload);
                payload = new ZWaveCommandClassPayload(payload, 4);
            } else {
                logger.debug("NODE {}: COMMAND_CLASS_MULTI_CHANNEL corrupted payload {}", getNodeId(),
                        SerialMessage.bb2hex(payload.getPayloadBuffer()));
                return null;
            }
        }

        if (payload.getCommandClassId() == CommandClass.COMMAND_CLASS_SUPERVISION.getKey()) {
            logger.debug("NODE {}: Decapsulating COMMAND_CLASS_SUPERVISION", getNodeId());
        }

        List<ZWaveCommandClassPayload> commands = new ArrayList<ZWaveCommandClassPayload>();

        if (payload.getCommandClassId() == CommandClass.COMMAND_CLASS_MULTI_CMD.getKey()) {
            logger.debug("NODE {}: Decapsulating COMMAND_CLASS_MULTI_CMD", getNodeId());

            if (endpoints.get(0) == null) {
                logger.debug("NODE {}: No endpoint 0!", getNodeId());
                return null;
            }
            ZWaveMultiCommandCommandClass multicommandCommandClass = (ZWaveMultiCommandCommandClass) getOrAddCommandClass(
                    endpoints.get(0), CommandClass.COMMAND_CLASS_MULTI_CMD);
            if (multicommandCommandClass == null) {
                logger.debug("NODE {}: COMMAND_CLASS_MULTI_CMD not found", getNodeId());
                return null;
            }

            commands.addAll(multicommandCommandClass.handleMultiCommandEncap(payload));
        } else {
            commands.add(payload);
        }

        ZWaveEndpoint endpoint = getEndpoint(endpointNumber);
        if (endpoint == null) {
            logger.debug("NODE {}: No endpoint {}!", getNodeId(), endpointNumber);
            return null;
        }
        for (ZWaveCommandClassPayload command : commands) {
            // Check for WAKEUP_NOTIFICATION
            if (payload.getCommandClassId() == CommandClass.COMMAND_CLASS_WAKE_UP.getKey()
                    && payload.getCommandClassCommand() == 0x07) {
                setAwake(true);
                continue;
            }

            CommandClass commandClass = CommandClass.getCommandClass(command.getCommandClassId());
            if (commandClass == null) {
                logger.debug("NODE {}: Unknown command class 0x{}", getNodeId(),
                        Integer.toHexString(payload.getCommandClassId()));
                continue;
            }

            logger.debug("NODE {}: Incoming command class {}, endpoint {}", getNodeId(), commandClass,
                    endpoint.getEndpointId());
            ZWaveCommandClass zwaveCommandClass = getOrAddCommandClass(endpoint, commandClass);
            if (zwaveCommandClass == null) {
                continue;
            }

            if (securityDecapOk == false
                    && doesMessageRequireSecurityEncapsulation(endpoint.getEndpointId(), command)) {
                // Should have been security encapsulation but wasn't!
                logger.debug(
                        "NODE {}: Command Class {} was required to be security encapsulated but it wasn't! Message dropped.",
                        nodeId, zwaveCommandClass.getCommandClass());

                return Collections.emptyList();
            }

            try {
                zwaveCommandClass.handleApplicationCommandRequest(command, endpoint.getEndpointId());
            } catch (ZWaveSerialMessageException e) {
                logger.error("Exception processing frame", e);
            }
        }

        // Return the list of commands we've processed
        return commands;
    }

    public void sendMessage(ZWaveCommandClassTransactionPayload payload) {
        controller.sendData(encapsulate(payload, 0));
    }

    public @Nullable ZWaveTransactionResponse sendTransaction(ZWaveCommandClassTransactionPayload payload,
            int endpoint) {
        return controller.sendTransaction(encapsulate(payload, endpoint));
    }

    /**
     * Returns the number of nanoseconds since the device was included, or Long.MAX_VALUE if the device has not recently
     * been included.
     *
     * @return number of nano seconds since inclusion completed
     */
    public long getInclusionTimer() {
        if (inclusionTimer == null) {
            return Long.MAX_VALUE;
        }

        return System.nanoTime() - inclusionTimer;
    }

    public void setInclusionTimer() {
        inclusionTimer = System.nanoTime();
    }

    /**
     * Sets the device as awake if the device is normally not listening.
     *
     * @param awake boolean true if the device is currently awake
     */
    public void setAwake(boolean awake) {
        // Don't do anything if this node is listening
        if (listening == true || frequentlyListening == true) {
            logger.trace("NODE {}: Node is listening - ignore wakeup", getNodeId());
            return;
        }

        // Create the timer if this is our first call
        if (timer == null) {
            controller.kickQueue();
            logger.trace("NODE {}: Creating wakeup timer", getNodeId());
            timer = new Timer();
        }

        // Start the timer
        if (!this.awake) {
            // We're awake
            logger.debug("NODE {}: Is awake with {} messages in the queue", getNodeId(),
                    controller.getSendQueueLength(getNodeId()));

            setSleepTimer();

            // Notify application
            ZWaveEvent event = new ZWaveNodeStatusEvent(getNodeId(), ZWaveNodeState.AWAKE);
            controller.notifyEventListeners(event);
        } else if (!awake) {
            resetSleepTimer();
        }
        this.awake = awake;
    }

    /**
     * Checks if the device is able to receive messages
     * If this device is always listening, then it will always return true, otherwise it will return true if the device
     * is awake.
     *
     * @return true if the node can receive a message
     */
    public boolean isAwake() {
        logger.trace("NODE {}: listening == {}, frequentlyListening == {}, awake == {}", getNodeId(), listening,
                frequentlyListening, awake);
        return (listening == true || frequentlyListening == true || awake == true);
    }

    /**
     * The following timer implements a re-triggerable timer. The timer is triggered when there are no more messages to
     * be sent in the wake-up queue. When the timer times out it will send the 'Go To Sleep' message to the node.
     * The timer just provides some time for anything further to be sent as a result of any processing.
     */
    private class WakeupTimerTask extends TimerTask {
        // Two cycles through the loop are required to send a device to sleep
        private boolean triggered;
        private final ZWaveWakeUpCommandClass wakeUpCommandClass;

        WakeupTimerTask() {
            logger.trace("NODE {}: Creating WakeupTimerTask", getNodeId());
            wakeUpCommandClass = (ZWaveWakeUpCommandClass) getEndpoint(0)
                    .getCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_WAKE_UP);
            if (wakeUpCommandClass == null) {
                logger.debug("NODE {}: COMMAND_CLASS_WAKE_UP not found - setting AWAKE", getNodeId());
                awake = true;
            }

            triggered = false;
        }

        @Override
        public void run() {
            if (isAwake() == false) {
                logger.trace("NODE {}: WakeupTimerTask Already asleep", getNodeId());
                return;
            }

            logger.debug("NODE {}: WakeupTimerTask {} Messages waiting, state {}", getNodeId(),
                    controller.getSendQueueLength(getNodeId()), getNodeInitStage());
            if (triggered == false) {
                logger.trace("NODE {}: WakeupTimerTask First iteration", getNodeId());
                triggered = true;
                return;
            }

            // Tell the device to go back to sleep.
            logger.debug("NODE {}: No more messages, go back to sleep", getNodeId());
            if (wakeUpCommandClass != null) {
                ZWaveTransactionResponse response = sendTransaction(wakeUpCommandClass.getNoMoreInformationMessage(),
                        0);
                logger.debug("NODE {}: Went to sleep {}", getNodeId(), response.getState());
            }
            // When this transaction completes, assume we're asleep
            setAwake(false);

            // Stop the timer
            resetSleepTimer();
        }
    }

    private synchronized void setSleepTimer() {
        // Stop any existing timer
        resetSleepTimer();

        // Create the timer task
        timerTask = new WakeupTimerTask();

        int timerDelay;

        // Start the timer
        // If the initialisation is complete, then use a short delay,
        // Otherwise use a longer delay...
        if (isInitializationComplete()) {
            timerDelay = sleepDelay;
        } else {
            timerDelay = 5000;
        }
        logger.debug("NODE {}: Start sleep timer at {}ms", getNodeId(), timerDelay);

        timer.schedule(timerTask, timerDelay / 2, timerDelay / 2);
    }

    private synchronized void resetSleepTimer() {
        // Stop any existing timer
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = null;
    }
}
