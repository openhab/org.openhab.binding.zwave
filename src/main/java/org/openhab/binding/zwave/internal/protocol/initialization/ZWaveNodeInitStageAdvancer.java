/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.initialization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.internal.ZWaveConfigProvider;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociation;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveMessagePayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionResponse;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionResponse.State;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClassDynamicState;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClassInitialization;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveManufacturerSpecificCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiInstanceCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveNoOperationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveVersionCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInitializationStateEvent;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignSucReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.DeleteReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.DeleteSucReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetRoutingInfoMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.IdentifyNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.IsFailedNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeInfoMessageClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveNodeStageAdvancer class. Advances the node stage, thereby controlling
 * the initialization of a node.
 * <p>
 * Node initialisation is handled solely within the NodeStageAdvancer. It is not based on time - it waits for the
 * transactions to complete. Time cannot be used since with larger networks, it may take a long time for the
 * initialisation. This is especially true if there are battery nodes since the PING phase, used to detect if a node is
 * active, will time-out for battery devices. A timeout takes 5 seconds, and if there are retries active, this may be
 * extended to 15 seconds. For a network with 8 battery nodes, this could mean a delay of 2 minutes!
 * <p>
 * We use the 'listening' flag to prioritise the initialisation of nodes. Rather than kicking off all nodes at the same
 * time and have battery nodes timing out and delaying the initialisation of mains nodes, we try and initialise nodes
 * that are listening first. This is checked after the protocol information is received, and non-listening nodes are
 * held at a WAIT state until the transmit queue drops below 2 frames when they are allowed to proceed to PING.
 * <p>
 * The NodeStageAdvancer registers an event listener during the initialisation of a node. This allows it to be notified
 * when each transaction is complete, and we can process this accordingly. The event listener is removed when we stop
 * initialising to reduce processor loading.
 * <p>
 * Command classes are responsible for building lists of messages needed to initialise themselves. The command class
 * also needs to keep track of responses so it knows if initialisation of this stage is complete. Other than that, the
 * command class does not have any input into the initialisation phase, and the sequencing of events - this is all
 * handled here in the node advancer class.
 * <p>
 * For each stage, the advancer builds a list of all messages that need to be sent to the node. Since the initialisation
 * phase is an intense period, with a lot of messages on the network, we try and ensure that only 1 packet is
 * outstanding to any node at once to avoid filling up the main transmit queue which could impact on the performance of
 * other nodes.
 * <p>
 * Each time we receive an ACK for a message, the node advancer gets called, and we see if this is an ACK for a message
 * that's part of the initialisation. If it is, the message gets removed from the list.
 * <p>
 * Each time we receive a command message, the node advancer gets called. This is called after the command class has
 * been updated, so at this stage we know if the stage can be completed.
 * <p>
 * Two checks are performed to allow a node stage to advance. Firstly, we make sure we've sent all the messages required
 * for this phase. Sending the messages however doesn't guarantee that we get a response, so we then run through the
 * stage again to make sure that the command class really is initialised. If the second run queues no messages, then we
 * can reliably assume this stage is completed. If we've missed anything, then we continue until there are no messages
 * to send.
 * <p>
 * If a node is DEAD (or FAILED) then we still try to initialise. No HEAL is performed on initialising nodes, so we need
 * to do enough here to find out if the node comes back to life.
 * <p>
 * A 'is node failed' check is performed at the beginning of the init process. This asks the controller if it thinks the
 * node is dead - if it is, then we treat the node as dead until it comes back to life.
 * <p>
 * A DEAD node will use a backoff to reduce the traffic. We start sending data reasonably quickly, but if it fails, then
 * we reduce the retry timer by a factor of 2 until BACKOFF_TIMER_MAX is reached.
 *
 * @author Chris Jackson
 */
public class ZWaveNodeInitStageAdvancer {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveNodeInitStageAdvancer.class);

    private static final ZWaveNodeSerializer nodeSerializer = new ZWaveNodeSerializer();

    private ZWaveNode node;
    private ZWaveController controller;
    private boolean restoredFromConfigfile = false;

    ThingType thingType = null;

    private Date queryStageTimeStamp;
    private ZWaveNodeInitStage currentStage = ZWaveNodeInitStage.EMPTYNODE;

    /**
     * Used only by {@link ZWaveNodeInitStage#SECURITY_REPORT}
     */
    private ZWaveTransaction securityLastSentMessage;

    /**
     * Constructor. Creates a new instance of the ZWaveNodeStageAdvancer class.
     *
     * @param node
     *            the node this advancer belongs to.
     * @param controller
     *            the controller to use
     */
    public ZWaveNodeInitStageAdvancer(ZWaveNode node, ZWaveController controller) {
        this.node = node;
        this.controller = controller;
    }

    /**
     * Starts the initialisation from the beginning.
     */
    public void startInitialisation() {
        startInitialisation(ZWaveNodeInitStage.EMPTYNODE);
    }

    /**
     * Start the initialisation from a specific stage
     *
     * @param startStage
     */
    public void startInitialisation(ZWaveNodeInitStage startStage) {
        // Reset the state variables
        currentStage = startStage;

        if (startStage == ZWaveNodeInitStage.DONE) {
            return;
        }
        logger.debug("NODE {}: Starting initialisation from {}", node.getNodeId(), startStage);

        queryStageTimeStamp = Calendar.getInstance().getTime();

        Thread thread = new Thread() {
            @Override
            public void run() {
                doInitialStages();
                if (currentStage == ZWaveNodeInitStage.DONE) {
                    return;
                }

                // If restored from a config file, jump to the dynamic node stage.
                if (isRestoredFromConfigfile()) {
                    logger.debug("NODE {}: Node advancer: Restored from file - skipping static initialisation",
                            node.getNodeId());
                    currentStage = ZWaveNodeInitStage.SESSION_START;
                } else {
                    doStaticStages();
                }

                doDynamicStages();
            }
        };

        thread.start();
    }

    private void processTransactions(ZWaveMessagePayload zWaveCommandClassTransactionPayload) {
        if (zWaveCommandClassTransactionPayload == null) {
            return;
        }

        Random rn = new Random();
        int backoff = 50;
        ZWaveTransactionResponse response;
        do {
            response = controller.SendTransaction(zWaveCommandClassTransactionPayload);

            // Increase the backoff up to 1800 seconds (approx!)
            if (backoff < 900000) {
                backoff += backoff + rn.nextInt(1000);
            }

            try {
                Thread.sleep(backoff);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } while (response == null || response.getState() != State.COMPLETE);

        logger.debug("NODE {}: Node Init transaction completed with response {}", response.getState());
    }

    /**
     * Move all the messages in a collection to the queue
     *
     * @param msgs
     *            the message collection
     */
    private void processTransactions(Collection<ZWaveCommandClassTransactionPayload> transactions) {
        if (transactions == null) {
            return;
        }
        for (ZWaveCommandClassTransactionPayload transaction : transactions) {
            processTransactions(transaction);
        }
    }

    /**
     * Move all the messages in a collection to the queue and encapsulates them
     *
     * @param msgs
     *            the message collection
     * @param the
     *            command class to encapsulate
     * @param endpointId
     *            the endpoint number
     */
    private void processTransactions(Collection<ZWaveCommandClassTransactionPayload> transactions,
            ZWaveCommandClass commandClass, int endpointId) {
        if (transactions == null) {
            return;
        }
        for (ZWaveCommandClassTransactionPayload transaction : transactions) {
            processTransactions(node.encapsulate(transaction, commandClass, endpointId));
        }
    }

    private void doInitialStages() {
        setCurrentStage(ZWaveNodeInitStage.IDENTIFY_NODE);
        logger.debug("NODE {}: Node advancer: Initialisation starting", node.getNodeId());

        // Get the device information from the controller
        processTransactions(new IdentifyNodeMessageClass().doRequest(node.getNodeId()));

        setCurrentStage(ZWaveNodeInitStage.INIT_NEIGHBORS);

        logger.debug("NODE {}: Node advancer: INIT_NEIGHBORS - send RoutingInfo", node.getNodeId());

        processTransactions(new GetRoutingInfoMessageClass().doRequest(node.getNodeId()));

        setCurrentStage(ZWaveNodeInitStage.FAILED_CHECK);
        // It seems that PC_CONTROLLERs don't respond to a lot of requests, so let's
        // just assume their OK!
        // If this is a controller, we're done
        if (node.getDeviceClass().getSpecificDeviceClass() == Specific.PC_CONTROLLER) {
            logger.debug("NODE {}: Node advancer: FAILED_CHECK - Controller - terminating initialisation",
                    node.getNodeId());
            setCurrentStage(ZWaveNodeInitStage.DONE);
            return;
        }

        processTransactions(new IsFailedNodeMessageClass().doRequest(node.getNodeId()));

        setCurrentStage(ZWaveNodeInitStage.PING);
        ZWaveNoOperationCommandClass noOpCommandClass = (ZWaveNoOperationCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_NO_OPERATION);
        if (noOpCommandClass == null) {
            return;
        }

        ZWaveTransaction msg = noOpCommandClass.getNoOperationMessage();
        if (msg == null) {
            return;
        }
        // We only send out a single PING - no retries at controller
        // level! This is to try and reduce network congestion during
        // initialisation.
        // For battery devices, the PING will time-out. This takes 5
        // seconds and if there are retries, it will be 15 seconds!
        // This will block the network for a considerable time if there
        // are a lot of battery devices (eg. 2 minutes for 8 battery devices!).
        msg.setAttemptsRemaining(1);
        processTransactions(msg);
    }

    private void doStaticStages() {
        setCurrentStage(ZWaveNodeInitStage.IDENTIFY_NODE);
        processTransactions(new IdentifyNodeMessageClass().doRequest(node.getNodeId()));

        setCurrentStage(ZWaveNodeInitStage.DETAILS);
        processTransactions(new RequestNodeInfoMessageClass().doRequest(node.getNodeId()));

        setCurrentStage(ZWaveNodeInitStage.MANUFACTURER);
        // Try and get the manufacturerSpecific command class.
        ZWaveManufacturerSpecificCommandClass manufacturerSpecific = (ZWaveManufacturerSpecificCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_MANUFACTURER_SPECIFIC);

        if (manufacturerSpecific != null) {
            // If this node implements the Manufacturer Specific command
            // class, we use it to get manufacturer info.
            logger.debug("NODE {}: Node advancer: MANUFACTURER - send ManufacturerSpecific", node.getNodeId());
            processTransactions(manufacturerSpecific.getManufacturerSpecificMessage());
        }

        setCurrentStage(ZWaveNodeInitStage.APP_VERSION);
        ZWaveVersionCommandClass versionCommandClass = (ZWaveVersionCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_VERSION);

        if (versionCommandClass == null) {
            logger.debug("NODE {}: Node advancer: APP_VERSION - VERSION not supported", node.getNodeId());
        } else {
            // Request the version report for this node
            logger.debug("NODE {}: Node advancer: APP_VERSION - send VersionMessage", node.getNodeId());

            processTransactions(versionCommandClass.getVersionMessage());

            setCurrentStage(ZWaveNodeInitStage.VERSION);
            thingType = ZWaveConfigProvider.getThingType(node);
            if (thingType == null) {
                logger.debug("NODE {}: Node advancer: VERSION - thing is null!", node.getNodeId());
            }

            // Loop through all command classes, requesting their version
            // using the Version command class
            for (ZWaveCommandClass zwaveVersionClass : node.getCommandClasses()) {
                logger.debug("NODE {}: Node advancer: VERSION - checking {}, version is {}", node.getNodeId(),
                        zwaveVersionClass.getCommandClass(), zwaveVersionClass.getVersion());

                // See if we want to force the version of this command class
                if (thingType != null) {
                    Map<String, String> properties = thingType.getProperties();
                    for (Map.Entry<String, String> entry : properties.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();

                        String cmds[] = key.split(":");
                        if ("commandClass".equals(cmds[0]) == false) {
                            continue;
                        }
                        String args[] = value.split("=");

                        if ("setVersion".equals(args[0])) {
                            if (zwaveVersionClass.getCommandClass().equals(cmds[1])) {
                                logger.debug("NODE {}: Node advancer: VERSION - Set {} to Version {}", node.getNodeId(),
                                        CommandClass.getCommandClass(cmds[1]), args[1]);

                                // TODO: This ignores endpoint
                                try {
                                    zwaveVersionClass.setVersion(Integer.parseInt(args[1]));
                                } catch (NumberFormatException e) {
                                    logger.error("NODE {}: Node advancer: VERSION - number format exception {}",
                                            args[1]);
                                }
                            }
                        }
                    }
                }

                if (versionCommandClass != null && zwaveVersionClass.getVersion() == 0) {
                    logger.debug("NODE {}: Node advancer: VERSION - queued   {}", node.getNodeId(),
                            zwaveVersionClass.getCommandClass());

                    processTransactions(versionCommandClass.checkVersion(zwaveVersionClass));
                } else if (zwaveVersionClass.getVersion() == 0) {
                    logger.debug("NODE {}: Node advancer: VERSION - VERSION default to 1", node.getNodeId());
                    zwaveVersionClass.setVersion(1);
                }
            }
        }

        setCurrentStage(ZWaveNodeInitStage.ENDPOINTS);
        // Try and get the multi instance / channel command class.
        ZWaveMultiInstanceCommandClass multiInstance = (ZWaveMultiInstanceCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_MULTI_CHANNEL);

        if (multiInstance != null) {
            logger.debug("NODE {}: Node advancer: ENDPOINTS - MultiInstance is supported", node.getNodeId());
            processTransactions(multiInstance.initEndpoints(true));
        } else {
            logger.debug("NODE {}: Node advancer: ENDPOINTS - MultiInstance not supported.", node.getNodeId());
            // Set all classes to 1 instance.
            for (ZWaveCommandClass commandClass : node.getCommandClasses()) {
                commandClass.setInstances(1);
            }
        }
        setCurrentStage(ZWaveNodeInitStage.UPDATE_DATABASE);
        // This stage reads information from the database to allow us to modify the configuration
        logger.debug("NODE {}: Node advancer: UPDATE_DATABASE", node.getNodeId());

        thingType = ZWaveConfigProvider.getThingType(node);
        if (thingType == null) {
            logger.debug("NODE {}: Node advancer: UPDATE_DATABASE - thing is null!", node.getNodeId());
        } else {

            // We now should know all the command classes, so run through the database and set any options
            Map<String, String> properties = thingType.getProperties();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                String cmds[] = key.split(":");
                if ("commandClass".equals(cmds[0]) == false) {
                    continue;
                }

                String options[] = value.split(",");

                Map<String, String> optionMap = new HashMap<String, String>(1);
                for (String option : options) {
                    String args[] = option.split("=");
                    if (args.length == 2) {
                        optionMap.put(args[0], args[1]);
                    } else {
                        optionMap.put(args[0], "");
                    }
                }

                if (optionMap.containsKey("ccRemove")) {
                    // If we want to remove the class, then remove it!

                    // TODO: This will only remove the root nodes and ignores endpoint
                    // TODO: Do we need to search into multi_instance?
                    node.removeCommandClass(CommandClass.getCommandClass(cmds[1]));
                    logger.debug("NODE {}: Node advancer: UPDATE_DATABASE - removing {}", node.getNodeId(),
                            CommandClass.getCommandClass(optionMap.get("ccRemove")));
                    continue;
                }

                // Get the command class
                int endpoint = cmds.length == 2 ? 0 : Integer.parseInt(cmds[2]);
                ZWaveCommandClass zwaveClass = node.resolveCommandClass(CommandClass.getCommandClass(cmds[1]),
                        endpoint);

                // If we found the command class, then set its options
                if (zwaveClass != null) {
                    zwaveClass.setOptions(optionMap);
                    continue;
                }

                // Command class isn't found! Do we want to add it?
                // TODO: Does this need to account for multiple endpoints!?!
                if (optionMap.containsKey("ccAdd")) {
                    ZWaveCommandClass commandClass = ZWaveCommandClass.getInstance(
                            CommandClass.getCommandClass(optionMap.get("ccAdd")).getKey(), node, controller);
                    if (commandClass != null) {
                        logger.debug("NODE {}: Node advancer: UPDATE_DATABASE - adding {}", node.getNodeId(),
                                CommandClass.getCommandClass(optionMap.get("ccAdd")));
                        node.addCommandClass(commandClass);
                    }
                }
            }
        }

        setCurrentStage(ZWaveNodeInitStage.STATIC_VALUES);
        // Loop through all classes looking for static initialisation
        for (ZWaveCommandClass zwaveStaticClass : node.getCommandClasses()) {
            logger.debug("NODE {}: Node advancer: STATIC_VALUES - checking {}", node.getNodeId(),
                    zwaveStaticClass.getCommandClass());
            if (zwaveStaticClass instanceof ZWaveCommandClassInitialization) {
                logger.debug("NODE {}: Node advancer: STATIC_VALUES - found    {}", node.getNodeId(),
                        zwaveStaticClass.getCommandClass());
                ZWaveCommandClassInitialization zcci = (ZWaveCommandClassInitialization) zwaveStaticClass;
                int instances = zwaveStaticClass.getInstances();
                logger.debug("NODE {}: Found {} instances of {}", node.getNodeId(), instances,
                        zwaveStaticClass.getCommandClass());
                if (instances == 1) {
                    processTransactions(zcci.initialize(true));
                } else {
                    for (int i = 1; i <= instances; i++) {
                        processTransactions(zcci.initialize(true), zwaveStaticClass, i);
                    }
                }
            } else if (zwaveStaticClass instanceof ZWaveMultiInstanceCommandClass) {
                ZWaveMultiInstanceCommandClass multiInstanceCommandClass = (ZWaveMultiInstanceCommandClass) zwaveStaticClass;
                for (ZWaveEndpoint endpoint : multiInstanceCommandClass.getEndpoints()) {
                    for (ZWaveCommandClass endpointCommandClass : endpoint.getCommandClasses()) {
                        logger.debug("NODE {}: Node advancer: STATIC_VALUES - checking {} for endpoint {}",
                                node.getNodeId(), endpointCommandClass.getCommandClass(), endpoint.getEndpointId());
                        if (endpointCommandClass instanceof ZWaveCommandClassInitialization) {
                            logger.debug("NODE {}: Node advancer: STATIC_VALUES - found    {}", node.getNodeId(),
                                    endpointCommandClass.getCommandClass());
                            ZWaveCommandClassInitialization zcci2 = (ZWaveCommandClassInitialization) endpointCommandClass;
                            processTransactions(zcci2.initialize(true), endpointCommandClass, endpoint.getEndpointId());
                        }
                    }
                }
            }
        }

        setCurrentStage(ZWaveNodeInitStage.ASSOCIATIONS);
        // Do we support associations
        ZWaveMultiAssociationCommandClass multiAssociationCommandClass = (ZWaveMultiAssociationCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
        ZWaveAssociationCommandClass associationCommandClass = (ZWaveAssociationCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_ASSOCIATION);
        if (multiAssociationCommandClass != null || associationCommandClass != null) {

            thingType = ZWaveConfigProvider.getThingType(node);
            if (thingType == null) {
                logger.debug("NODE {}: Node advancer: ASSOCIATIONS - thing is null!", node.getNodeId());
            } else {

                ConfigDescription config = ZWaveConfigProvider.getThingTypeConfig(thingType);
                if (config == null) {
                    logger.debug("NODE {}: Node advancer: ASSOCIATIONS - no configuration!", node.getNodeId());
                } else {
                    for (ConfigDescriptionParameter parm : config.getParameters()) {
                        String[] cfg = parm.getName().split("_");
                        if ("group".equals(cfg[0])) {
                            int group = Integer.parseInt(cfg[1]);
                            logger.debug("NODE {}: Node advancer: ASSOCIATIONS request group {}", node.getNodeId(),
                                    group);
                            processTransactions(node.getAssociation(group));
                        }
                    }
                }
            }
        }

        setCurrentStage(ZWaveNodeInitStage.SET_WAKEUP);
        ZWaveWakeUpCommandClass wakeupCommandClass = (ZWaveWakeUpCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_WAKE_UP);

        // This stage sets the wakeup class if we're the master controller
        // It sets the node to point to us, and the time is left along
        if (controller.isMasterController() == true && wakeupCommandClass != null) {

            if (wakeupCommandClass.getTargetNodeId() == controller.getOwnNodeId()) {
                logger.debug("NODE {}: Node advancer: SET_WAKEUP - TargetNode is set to controller", node.getNodeId());
            } else {

                int value = controller.getSystemDefaultWakeupPeriod();
                if (wakeupCommandClass.getInterval() == 0 && value != 0) {
                    logger.debug("NODE {}: Node advancer: SET_WAKEUP - Interval is currently 0. Set to {}",
                            node.getNodeId(), value);
                } else {
                    value = wakeupCommandClass.getInterval();
                }

                logger.debug("NODE {}: Node advancer: SET_WAKEUP - Set wakeup node to controller ({}), period {}",
                        node.getNodeId(), controller.getOwnNodeId(), value);

                // Set the wake-up interval, and request an update
                processTransactions(wakeupCommandClass.setInterval(value));
                processTransactions(wakeupCommandClass.getIntervalMessage());
            }
        }

        setCurrentStage(ZWaveNodeInitStage.SET_ASSOCIATION);
        if (controller.isMasterController() == true) {

            // TODO: This should support MULTI_ASSOCIATION - when required

            ZWaveAssociationCommandClass associationCls = (ZWaveAssociationCommandClass) node
                    .getCommandClass(CommandClass.COMMAND_CLASS_ASSOCIATION);
            if (associationCls == null) {
                logger.debug("NODE {}: Node advancer: SET_ASSOCIATION - ASSOCIATION class not supported",
                        node.getNodeId());
            } else {
                thingType = ZWaveConfigProvider.getThingType(node);
                if (thingType == null) {
                    logger.debug("NODE {}: Node advancer: SET_ASSOCIATION - thing is null!", node.getNodeId());
                } else {
                    String associations = thingType.getProperties()
                            .get(ZWaveBindingConstants.PROPERTY_XML_ASSOCIATIONS);
                    if (associations == null || associations.length() == 0) {
                        logger.debug("NODE {}: Node advancer: SET_ASSOCIATION - no default associations",
                                node.getNodeId());
                    } else {
                        String defaultGroups[] = associations.split(",");
                        for (int c = 0; c < defaultGroups.length; c++) {
                            int groupId = Integer.parseInt(defaultGroups[c]);
                            ZWaveAssociation association = new ZWaveAssociation(controller.getOwnNodeId());

                            // We should know about all groups at this stage.
                            // If we don't know about the group, then assume it doesn't exist
                            if (node.getAssociationGroup(groupId) == null) {
                                continue;
                            }

                            // Check if we're already a member
                            if (node.getAssociationGroup(groupId).getAssociations().contains(association)) {
                                logger.debug("NODE {}: Node advancer: SET_ASSOCIATION - ASSOCIATION set for group {}",
                                        node.getNodeId(), groupId);
                            } else {
                                logger.debug("NODE {}: Node advancer: SET_ASSOCIATION - Adding ASSOCIATION to group {}",
                                        node.getNodeId(), groupId);
                                // Set the association, and request the update so we confirm if it's set
                                processTransactions(node.setAssociation(null, groupId, controller.getOwnNodeId(), 0));
                                processTransactions(node.getAssociation(groupId));
                            }
                        }
                    }
                }
            }
        }

        setCurrentStage(ZWaveNodeInitStage.DELETE_SUC_ROUTES);

        // Only delete the route if this is not the controller and there is an SUC in the network
        if (node.getNodeId() != controller.getOwnNodeId() && controller.getSucId() != 0) {
            // Update the route to the controller
            logger.debug("NODE {}: Node advancer is deleting SUC return route.", node.getNodeId());
            processTransactions(new DeleteSucReturnRouteMessageClass().doRequest(node.getNodeId()));
        }

        setCurrentStage(ZWaveNodeInitStage.SUC_ROUTE);
        // Only set the route if this is not the controller and there is an SUC in the network
        if (node.getNodeId() != controller.getOwnNodeId() && controller.getSucId() != 0) {
            // Update the route to the controller
            logger.debug("NODE {}: Node advancer is setting SUC route.", node.getNodeId());
            processTransactions(new AssignSucReturnRouteMessageClass().doRequest(node.getNodeId()));
        }

        setCurrentStage(ZWaveNodeInitStage.GET_CONFIGURATION);
        ZWaveConfigurationCommandClass configurationCommandClass = (ZWaveConfigurationCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_CONFIGURATION);

        // If the node doesn't support configuration class, then we better let people know!
        if (configurationCommandClass == null) {
            logger.debug("NODE {}: Node advancer: GET_CONFIGURATION - CONFIGURATION class not supported",
                    node.getNodeId());
        } else {
            thingType = ZWaveConfigProvider.getThingType(node);
            if (thingType == null) {
                logger.debug("NODE {}: Node advancer: GET_CONFIGURATION - thing is null!", node.getNodeId());
            } else {
                ConfigDescription cfgConfig = ZWaveConfigProvider.getThingTypeConfig(thingType);
                if (cfgConfig == null) {
                    logger.debug("NODE {}: Node advancer: GET_CONFIGURATION - no configuration!", node.getNodeId());
                } else {
                    // Due to subparameters, we keep track of what we've sent to avoid sending duplicate requests
                    ArrayList<Integer> paramSent = new ArrayList<Integer>();
                    for (ConfigDescriptionParameter parm : cfgConfig.getParameters()) {
                        String[] cfg = parm.getName().split("_");
                        if ("config".equals(cfg[0])) {
                            logger.debug("NODE {}: Node advancer: GET_CONFIGURATION - checking {} - config",
                                    node.getNodeId(), parm.getName());
                            int index = Integer.parseInt(cfg[1]);
                            int size = Integer.parseInt(cfg[2]);

                            // Some parameters don't return anything, so don't request them!
                            if (Arrays.asList(cfg).contains("wo")) {
                                logger.debug("NODE {}: Node advancer: GET_CONFIGURATION - checking {} - wo",
                                        node.getNodeId(), parm.getName());

                                configurationCommandClass.setParameterWriteOnly(index, size, true);
                                continue;
                            }

                            // See if we've already sent this param
                            if (paramSent.contains(index)) {
                                continue;
                            }
                            paramSent.add(index);

                            // If this is the first time around the loop
                            // or we don't have a value for this parameter
                            // then request it!
                            logger.debug("NODE {}: Node advancer: GET_CONFIGURATION - checking {} - index {}",
                                    node.getNodeId(), parm.getName(), index);

                            if (configurationCommandClass.getParameter(index) == null) {
                                processTransactions(configurationCommandClass.getConfigMessage(index));
                            }
                        }
                    }
                }
            }
        }
    }

    void doDynamicStages() {
        setCurrentStage(ZWaveNodeInitStage.DYNAMIC_VALUES);
        // Update all dynamic information from command classes
        for (ZWaveCommandClass zwaveDynamicClass : node.getCommandClasses()) {
            logger.debug("NODE {}: Node advancer: DYNAMIC_VALUES - checking {}", node.getNodeId(),
                    zwaveDynamicClass.getCommandClass());
            if (zwaveDynamicClass instanceof ZWaveCommandClassDynamicState) {
                logger.debug("NODE {}: Node advancer: DYNAMIC_VALUES - found    {}", node.getNodeId(),
                        zwaveDynamicClass.getCommandClass());
                ZWaveCommandClassDynamicState zdds = (ZWaveCommandClassDynamicState) zwaveDynamicClass;
                int instances = zwaveDynamicClass.getInstances();
                logger.debug("NODE {}: Found {} instances of {}", node.getNodeId(), instances,
                        zwaveDynamicClass.getCommandClass());
                if (instances == 1) {
                    processTransactions(zdds.getDynamicValues(true));
                } else {
                    for (int i = 1; i <= instances; i++) {
                        processTransactions(zdds.getDynamicValues(true), zwaveDynamicClass, i);
                    }
                }
            } else if (zwaveDynamicClass instanceof ZWaveMultiInstanceCommandClass) {
                ZWaveMultiInstanceCommandClass multiInstanceCommandClass = (ZWaveMultiInstanceCommandClass) zwaveDynamicClass;
                for (ZWaveEndpoint endpoint : multiInstanceCommandClass.getEndpoints()) {
                    for (ZWaveCommandClass endpointCommandClass : endpoint.getCommandClasses()) {
                        logger.debug("NODE {}: Node advancer: DYNAMIC_VALUES - checking {} for endpoint {}",
                                node.getNodeId(), endpointCommandClass.getCommandClass(), endpoint.getEndpointId());
                        if (endpointCommandClass instanceof ZWaveCommandClassDynamicState) {
                            logger.debug("NODE {}: Node advancer: DYNAMIC_VALUES - found    {}", node.getNodeId(),
                                    endpointCommandClass.getCommandClass());
                            ZWaveCommandClassDynamicState zdds2 = (ZWaveCommandClassDynamicState) endpointCommandClass;
                            processTransactions(zdds2.getDynamicValues(true), endpointCommandClass,
                                    endpoint.getEndpointId());
                        }
                    }
                }
            }
        }

        setCurrentStage(ZWaveNodeInitStage.DELETE_ROUTES);
        if (node.getRoutingList().size() != 0) {
            // Delete all the return routes for the node
            logger.debug("NODE {}: Node advancer is deleting return routes.", node.getNodeId());
            processTransactions(new DeleteReturnRouteMessageClass().doRequest(node.getNodeId()));
        }

        setCurrentStage(ZWaveNodeInitStage.RETURN_ROUTES);
        for (Integer route : node.getRoutingList()) {
            // Loop through all the nodes and set the return route
            logger.debug("NODE {}: Adding return route to {}", node.getNodeId(), route);
            processTransactions(new AssignReturnRouteMessageClass().doRequest(node.getNodeId(), route));
        }

        setCurrentStage(ZWaveNodeInitStage.NEIGHBORS);
        logger.debug("NODE {}: Node advancer: NEIGHBORS - get RoutingInfo", node.getNodeId());
        processTransactions(new GetRoutingInfoMessageClass().doRequest(node.getNodeId()));

        logger.debug("NODE {}: Node advancer: Initialisation complete!", node.getNodeId());

        // Notify everyone!
        setCurrentStage(ZWaveNodeInitStage.DONE);
    }

    /**
     * Gets the current node stage
     *
     * @return current node stage
     */
    public ZWaveNodeInitStage getCurrentStage() {
        return currentStage;
    }

    /**
     * Sets the current node stage
     */
    private void setCurrentStage(ZWaveNodeInitStage newStage) {
        currentStage = newStage;

        // Remember the time so we can handle retries and keep users informed
        queryStageTimeStamp = Calendar.getInstance().getTime();

        logger.debug("NODE {}: Node advancer - advancing to {}", node.getNodeId(), newStage);

        ZWaveEvent zEvent = new ZWaveInitializationStateEvent(node.getNodeId(), newStage);
        controller.notifyEventListeners(zEvent);

        switch (currentStage) {
            case DISCOVERY_COMPLETE:
            case STATIC_END:
            case DYNAMIC_END:
            case DONE:
                nodeSerializer.SerializeNode(node);
                break;
            default:
                break;
        }
    }

    /**
     * Sets the time stamp the node was last queried.
     *
     * @param queryStageTimeStamp
     *            the queryStageTimeStamp to set
     */
    public Date getQueryStageTimeStamp() {
        return queryStageTimeStamp;
    }

    /**
     * Returns whether the initialization process has completed.
     *
     * @return true if initialization has completed. False otherwise.
     */
    public boolean isInitializationComplete() {
        return (currentStage == ZWaveNodeInitStage.DONE);
    }

    /**
     * Returns whether the node was restored from a config file.
     *
     * @return the restoredFromConfigfile
     */
    public boolean isRestoredFromConfigfile() {
        return restoredFromConfigfile;
    }

    /**
     * Sets the flag to indicate that this node was restored from file
     */
    public void setRestoredFromConfigfile() {
        restoredFromConfigfile = true;
    }
}
