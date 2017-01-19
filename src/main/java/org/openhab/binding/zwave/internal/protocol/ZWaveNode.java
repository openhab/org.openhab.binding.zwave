/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.zwave.internal.HexToIntegerConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiInstanceCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveVersionCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeStatusEvent;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStageAdvancer;
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

    private final ZWaveDeviceClass deviceClass;
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

    private String deviceFactoryId;
    private String deviceSerialId;

    private boolean listening; // i.e. sleeping
    private boolean frequentlyListening;
    private boolean routing;
    @SuppressWarnings("unused")
    private boolean security;
    private boolean beaming;
    @SuppressWarnings("unused")
    private int maxBaudRate;

    // Keep the NIF - just used for information and debug in the XML
    @SuppressWarnings("unused")
    private List<CommandClass> nodeInformationFrame = null;

    private Map<CommandClass, ZWaveCommandClass> supportedCommandClasses = new HashMap<CommandClass, ZWaveCommandClass>();
    private final Set<CommandClass> securedCommandClasses = new HashSet<CommandClass>();

    // Stores the list of association groups
    private Map<Integer, ZWaveAssociationGroup> associationGroups = new HashMap<Integer, ZWaveAssociationGroup>();

    private List<Integer> nodeNeighbors = new ArrayList<Integer>();
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

    /**
     * Constructor. Creates a new instance of the ZWaveNode class.
     *
     * @param homeId the home ID to use.
     * @param nodeId the node ID to use.
     * @param controller the wave controller instance
     */
    public ZWaveNode(int homeId, int nodeId, ZWaveController controller) {
        nodeState = ZWaveNodeState.INITIALIZING;
        this.homeId = homeId;
        this.nodeId = nodeId;
        this.controller = controller;
        this.nodeInitStageAdvancer = new ZWaveNodeInitStageAdvancer(this, controller);
        this.deviceClass = new ZWaveDeviceClass(Basic.NOT_KNOWN, Generic.NOT_KNOWN, Specific.NOT_USED);
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
        nodeState = ZWaveNodeState.INITIALIZING;

        this.controller = controller;

        // Create the initialisation advancer and tell it we've loaded from file
        nodeInitStageAdvancer = new ZWaveNodeInitStageAdvancer(this, controller);
        nodeInitStageAdvancer.setRestoredFromConfigfile();
        nodeInitStageAdvancer.startInitialisation(ZWaveNodeInitStage.EMPTYNODE);
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

        switch (state) {
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

        ZWaveEvent zEvent = new ZWaveNodeStatusEvent(this.getNodeId(), state);
        controller.notifyEventListeners(zEvent);

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
        return nodeInitStageAdvancer.getCurrentStage();
    }

    /**
     * Gets the initialization state
     *
     * @return true if initialization has been completed
     */
    public boolean isInitializationComplete() {
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
                .getCommandClass(CommandClass.VERSION);
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
     * Resets the resend counter and possibly resets the
     * node stage to DONE when previous initialization was
     * complete.
     * Note that if the node is DEAD, then the nodeStage stays DEAD
     */
    public void resetResendCount() {
        resendCount = 0;
        if (nodeInitStageAdvancer.isInitializationComplete() && isDead() == false) {
            nodeInitStageAdvancer.startInitialisation(ZWaveNodeInitStage.DONE);
        }
    }

    /**
     * Returns the device class of the node.
     *
     * @return the deviceClass
     */
    public ZWaveDeviceClass getDeviceClass() {
        return deviceClass;
    }

    /**
     * Returns the Command classes this node implements.
     *
     * @return the command classes.
     */
    public Collection<ZWaveCommandClass> getCommandClasses() {
        return supportedCommandClasses.values();
    }

    /**
     * Returns a commandClass object this node implements.
     * Returns null if command class is not supported by this node.
     *
     * @param commandClass The command class to get.
     * @return the command class.
     */
    public ZWaveCommandClass getCommandClass(CommandClass commandClass) {
        return supportedCommandClasses.get(commandClass);
    }

    /**
     * Returns whether a node supports this command class.
     *
     * @param commandClass the command class to check
     * @return true if the command class is supported, false otherwise.
     */
    public boolean supportsCommandClass(CommandClass commandClass) {
        return supportedCommandClasses.containsKey(commandClass);
    }

    /**
     * Adds a command class to the list of supported command classes by this node.
     * Does nothing if command class is already added.
     *
     * @param commandClass the command class instance to add.
     */
    public void addCommandClass(ZWaveCommandClass commandClass) {
        CommandClass key = commandClass.getCommandClass();

        if (!supportedCommandClasses.containsKey(key)) {
            logger.debug("NODE {}: Adding command class {} to the list of supported command classes.", nodeId,
                    commandClass.getCommandClass().getLabel());
            supportedCommandClasses.put(key, commandClass);

            if (commandClass instanceof ZWaveEventListener) {
                this.controller.addEventListener((ZWaveEventListener) commandClass);
            }
        }
    }

    /**
     * Removes a command class from the node.
     * This is used to remove classes that a node may report it supports
     * but it doesn't respond to.
     *
     * @param commandClass The command class key
     */
    public void removeCommandClass(CommandClass commandClass) {
        supportedCommandClasses.remove(commandClass);
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

        if (endpointId == 0) {
            return getCommandClass(commandClass);
        }

        ZWaveMultiInstanceCommandClass multiInstanceCommandClass = (ZWaveMultiInstanceCommandClass) supportedCommandClasses
                .get(CommandClass.MULTI_INSTANCE);
        if (multiInstanceCommandClass == null) {
            return null;
        } else if (multiInstanceCommandClass.getVersion() == 2) {
            ZWaveEndpoint endpoint = multiInstanceCommandClass.getEndpoint(endpointId);

            if (endpoint != null) {
                ZWaveCommandClass result = endpoint.getCommandClass(commandClass);
                if (result != null) {
                    return result;
                }
            }
        } else if (multiInstanceCommandClass.getVersion() == 1) {
            ZWaveCommandClass result = getCommandClass(commandClass);
            if (result != null && endpointId <= result.getInstances()) {
                return result;
            }
        } else {
            logger.warn("NODE {}: Unsupported multi instance command version: {}.", nodeId,
                    multiInstanceCommandClass.getVersion());
        }

        return null;
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
     * Encapsulates a serial message for sending to a
     * multi-instance instance/ multi-channel endpoint on
     * a node.
     *
     * @param serialMessage the serial message to encapsulate
     * @param commandClass the command class used to generate the message.
     * @param endpointId the instance / endpoint to encapsulate the message for
     * @param node the destination node.
     * @return SerialMessage on success, null on failure.
     */
    public SerialMessage encapsulate(SerialMessage serialMessage, ZWaveCommandClass commandClass, int endpointId) {
        ZWaveMultiInstanceCommandClass multiInstanceCommandClass;

        if (serialMessage == null) {
            return null;
        }

        // no encapsulation necessary.
        if (endpointId == 0) {
            return serialMessage;
        }

        multiInstanceCommandClass = (ZWaveMultiInstanceCommandClass) this.getCommandClass(CommandClass.MULTI_INSTANCE);

        if (multiInstanceCommandClass != null) {
            logger.debug("NODE {}: Encapsulating message, instance / endpoint {}", this.getNodeId(), endpointId);
            switch (multiInstanceCommandClass.getVersion()) {
                case 2:
                    if (commandClass.getEndpoint() != null) {
                        serialMessage = multiInstanceCommandClass.getMultiChannelEncapMessage(serialMessage,
                                commandClass.getEndpoint());
                        return serialMessage;
                    }
                    break;
                case 1:
                default:
                    if (commandClass.getInstances() >= endpointId) {
                        serialMessage = multiInstanceCommandClass.getMultiInstanceEncapMessage(serialMessage,
                                endpointId);
                        return serialMessage;
                    }
                    break;
            }
        }

        logger.warn("NODE {}: Encapsulating message, instance / endpoint {} failed, will discard message.",
                this.getNodeId(), endpointId);
        return null;
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
        ZWaveWakeUpCommandClass wakeupCmdClass = (ZWaveWakeUpCommandClass) getCommandClass(CommandClass.WAKE_UP);
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
        return String.format("Node %d. Manufacturer %04X, Type %04X, Id %04X", nodeId, manufacturer, deviceType,
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

    /**
     * Invoked by {@link ZWaveSecurityCommandClass} when a
     * {@link ZWaveSecurityCommandClass#SECURITY_SUPPORTED_REPORT} is received.
     *
     * @param data the class id for each class which must be encrypted in transmission
     */
    public void setSecuredClasses(byte[] data) {
        logger.debug("NODE {}:  Setting secured command classes for node with {}", this.getNodeId(),
                SerialMessage.bb2hex(data));
        boolean afterMark = false;
        securedCommandClasses.clear(); // reset the existing list
        for (final byte aByte : data) {
            // TODO: DB support extended commandClass format by checking for 0xF1 - 0xFF
            if (ZWaveSecurityCommandClass.bytesAreEqual(aByte, ZWaveSecurityCommandClass.COMMAND_CLASS_MARK)) {
                /**
                 * Marks the end of the list of supported command classes. The remaining classes are those
                 * that can be controlled by the device. These classes are created without values.
                 * Messages received cause notification events instead.
                 */
                afterMark = true;
                continue;
            }

            // Check if this is a commandClass that is already registered with the node
            final CommandClass commandClass = CommandClass.getCommandClass((aByte & 0xFF));
            if (commandClass == null) {
                // Not supported by OpenHab
                logger.error(
                        "NODE {}: setSecuredClasses requested secure "
                                + "class NOT supported by OpenHab: {}   afterMark={}",
                        this.getNodeId(), commandClass, afterMark);
            } else {
                // Sometimes security will be transmitted as a secure class, but it
                // can't be set that way since it's the one doing the encryption work So ignore that.
                if (commandClass == CommandClass.SECURITY) {
                    continue;
                } else if (afterMark) {
                    // Nothing to do, we don't track devices that control other devices
                    logger.info("NODE {}: is after mark for commandClass {}", this.getNodeId(), commandClass);
                    break;
                } else {
                    if (!this.supportsCommandClass(commandClass)) {
                        logger.info(
                                "NODE {}: Adding secured command class to supported that wasn't in original list {}",
                                this.getNodeId(), commandClass.getLabel());
                        final ZWaveCommandClass classInstance = ZWaveCommandClass.getInstance((aByte & 0xFF), this,
                                controller);
                        if (classInstance != null) {
                            addCommandClass(classInstance);
                        }
                    }
                    securedCommandClasses.add(commandClass);
                    logger.info("NODE {}: (Secured) {}", this.getNodeId(), commandClass.getLabel());
                }
            }
        }
        if (logger.isInfoEnabled()) {
            // show which classes are still insecure after the update
            final StringBuilder buf = new StringBuilder(
                    "NODE " + this.getNodeId() + ": After update, INSECURE command classes are: ");
            for (final ZWaveCommandClass zwCommandClass : this.getCommandClasses()) {
                if (!securedCommandClasses.contains(zwCommandClass.getCommandClass())) {
                    buf.append(zwCommandClass.getCommandClass() + ", ");
                }
            }
            logger.info(buf.toString().substring(0, buf.toString().length() - 1));
        }
    }

    public boolean doesMessageRequireSecurityEncapsulation(SerialMessage serialMessage) {
        boolean result = false;
        if (serialMessage.getMessageClass() != SerialMessageClass.SendData) {
            result = false;
        } else if (!supportedCommandClasses.containsKey(CommandClass.SECURITY)) {
            // Does this node support security at all?
            result = false;
        } else {
            int commandClassCode;
            try {
                commandClassCode = (byte) serialMessage.getMessagePayloadByte(2) & 0xFF;
            } catch (ZWaveSerialMessageException e) {
                logger.error("NODE {}: Exception processing message. Treating as INSECURE %s", getNodeId(),
                        e.getMessage());
                return false;
            }
            final CommandClass commandClassOfMessage = CommandClass.getCommandClass(commandClassCode);
            if (commandClassOfMessage == null) {
                // not sure how we would ever get here
                logger.warn(String.format("NODE %d: CommandClass not found for 0x%02X so treating as INSECURE %s",
                        getNodeId(), commandClassCode, serialMessage));
                result = false;
            } else if (CommandClass.SECURITY == commandClassOfMessage) {
                // CommandClass.SECURITY is a special case because only some commands get encrypted
                try {
                    final Byte messageCode = Byte.valueOf((byte) (serialMessage.getMessagePayloadByte(3) & 0xFF));
                    result = ZWaveSecurityCommandClass.doesCommandRequireSecurityEncapsulation(messageCode);
                } catch (ZWaveSerialMessageException e) {
                    logger.error("NODE {}: Exception processing message. Treating as INSECURE %s", getNodeId(),
                            e.getMessage());
                    return false;
                }
            } else if (commandClassOfMessage == CommandClass.NO_OPERATION) { // TODO: DB
                // On controller startup, PING seems to fail whenever it's encrypted, so don't
                // TODO: DB try again
                result = false;
            } else {
                result = securedCommandClasses.contains(commandClassOfMessage);
                if (!result) {
                    // Certain messages must always be sent securely per the Z-Wave spec
                    if (commandClassOfMessage == CommandClass.DOOR_LOCK
                            || commandClassOfMessage == CommandClass.USER_CODE) { // TODO: DB what else?
                        logger.warn("NODE {}: CommandClass {} is not marked as secure but should be, forcing secure",
                                getNodeId(), commandClassOfMessage);
                        result = true;
                    }
                }
            }
            if (result) {
                logger.trace("NODE {}: Message {} requires security encapsulation", getNodeId(), serialMessage);
            }
        }
        return result;
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

    public SerialMessage getAssociation(int group) {
        ZWaveMultiAssociationCommandClass multiAssociationCommandClass = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.MULTI_INSTANCE_ASSOCIATION);
        if (multiAssociationCommandClass != null) {
            return multiAssociationCommandClass.getAssociationMessage(group);
        }

        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.ASSOCIATION);
        if (associationCommandClass != null) {
            return associationCommandClass.getAssociationMessage(group);
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
     * @param endpoint the endpoint required to send the reports
     * @param groupId the group to be set
     * @param nodeId the node to be set to report to (receive)
     * @param endpointId the endpoint to be set to report to (receive)
     * @return
     */
    public SerialMessage setAssociation(ZWaveEndpoint endpoint, int groupId, int nodeId, int endpointId) {
        ZWaveMultiAssociationCommandClass multiAssociationCommandClass = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.MULTI_INSTANCE_ASSOCIATION);
        if (multiAssociationCommandClass != null) {
            ZWaveAssociationGroup group = getAssociationGroup(groupId);
            // Associations to endpoint 0 are only allowed if version is greater than 3
            // Here we check that this is a lifeline group
            if ((endpointId == 0 && multiAssociationCommandClass.getVersion() >= 3 && group != null
                    && group.getProfile2() != null && group.getProfile2() == 1) || endpointId != 0) {
                return multiAssociationCommandClass.setAssociationMessage(groupId, nodeId, endpointId);
            }
        }

        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.ASSOCIATION);
        if (associationCommandClass != null) {
            return associationCommandClass.setAssociationMessage(groupId, nodeId);
        }

        return null;
    }

    public SerialMessage removeAssociation(Integer groupId, int nodeId, int endpointId) {
        ZWaveMultiAssociationCommandClass multiAssociationCommandClass = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.MULTI_INSTANCE_ASSOCIATION);
        if (multiAssociationCommandClass != null) {
            return multiAssociationCommandClass.removeAssociationMessage(groupId, nodeId, endpointId);
        }

        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.ASSOCIATION);
        if (associationCommandClass != null) {
            return associationCommandClass.removeAssociationMessage(groupId, nodeId);
        }

        return null;
    }

    public SerialMessage clearAssociation(Integer groupId) {
        ZWaveMultiAssociationCommandClass multiAssociationCommandClass = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.MULTI_INSTANCE_ASSOCIATION);
        if (multiAssociationCommandClass != null) {
            return multiAssociationCommandClass.clearAssociationMessage(groupId);
        }

        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.ASSOCIATION);
        if (associationCommandClass != null) {
            return associationCommandClass.clearAssociationMessage(groupId);
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
}
