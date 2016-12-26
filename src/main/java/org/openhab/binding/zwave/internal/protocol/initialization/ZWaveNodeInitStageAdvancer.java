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
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveMessagePayloadTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveVersionCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInitializationStateEvent;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignSucReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.DeleteReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.DeleteSucReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetRoutingInfoMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.IdentifyNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.IsFailedNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeInfoMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeNeighborUpdateMessageClass;
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

    private Thread initialisationThread;

    private final long INCLUSION_TIMER = 10000000000L;

    private boolean initRunning = true;

    ThingType thingType = null;

    private Date queryStageTimeStamp;
    private ZWaveNodeInitStage currentStage = ZWaveNodeInitStage.EMPTYNODE;

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
    public void startInitialisation(final ZWaveNodeInitStage startStage) {
        // Reset the state variables
        currentStage = startStage;

        if (startStage == ZWaveNodeInitStage.DONE) {
            return;
        }
        logger.debug("NODE {}: Starting initialisation from {}", node.getNodeId(), startStage);

        queryStageTimeStamp = Calendar.getInstance().getTime();

        initialisationThread = new Thread() {
            @Override
            public void run() {
                if (node.getInclusionTimer() < INCLUSION_TIMER) {
                    logger.debug("NODE {}: Node advancer: Node just included ({})", node.getNodeId(),
                            node.getInclusionTimer());
                    doInitialInclusionStages();
                } else if (currentStage == ZWaveNodeInitStage.HEAL_START) {
                    doHealStages();
                    setCurrentStage(ZWaveNodeInitStage.DONE);
                    return;
                } else {
                    doInitialStages();
                }

                if (currentStage == ZWaveNodeInitStage.DONE) {
                    return;
                }

                // If restored from a config file, jump to the dynamic node stage.
                if (isRestoredFromConfigfile()) {
                    logger.debug("NODE {}: Node advancer: Restored from file - skipping static initialisation",
                            node.getNodeId());
                    currentStage = ZWaveNodeInitStage.SESSION_START;
                }
                if (currentStage.ordinal() <= ZWaveNodeInitStage.INCLUSION_START.ordinal()) {
                    doSecureStages();
                }
                if (currentStage.ordinal() <= ZWaveNodeInitStage.STATIC_VALUES.ordinal()) {
                    doStaticStages();
                }
                setCurrentStage(ZWaveNodeInitStage.STATIC_END);
                if (currentStage.ordinal() <= ZWaveNodeInitStage.DYNAMIC_VALUES.ordinal()) {
                    doDynamicStages();
                }
                setCurrentStage(ZWaveNodeInitStage.DYNAMIC_END);

                doHealStages();

                setCurrentStage(ZWaveNodeInitStage.DONE);
            }
        };

        initialisationThread.start();
    }

    /**
     * Cancels the initialisation and frees resources
     */
    public void stopInitialisation() {
        initRunning = false;
    }

    private boolean processTransaction(ZWaveMessagePayloadTransaction transaction) {
        return processTransaction(transaction, 0, 10);
    }

    private boolean processTransaction(ZWaveMessagePayloadTransaction transaction, long timeout, int retries) {
        if (transaction == null) {
            return false;
        }

        // Remember the start time
        long timerStart = System.nanoTime();

        // Use a random backoff so all nodes aren't synced.
        Random rand = new Random();
        int backoff = 250;
        int retryCount = 0;
        ZWaveTransactionResponse response = null;
        do {
            if (timeout > 0 && System.nanoTime() - timerStart > timeout) {
                logger.debug("NODE {}: timed out after {} / {}", node.getNodeId(), System.nanoTime() - timerStart,
                        timeout);
                return false;
            }

            if (transaction instanceof ZWaveCommandClassTransactionPayload) {
                logger.debug("NODE {}: ZWaveCommandClassTransactionPayload - send to node", node.getNodeId());
                response = node.sendTransaction((ZWaveCommandClassTransactionPayload) transaction, 0);
            } else {
                response = controller.sendTransaction(transaction);
            }

            logger.debug("NODE {}: Node Init response ({}) {}", node.getNodeId(), retryCount, response.getState());
            if (response != null && response.getState() == State.COMPLETE) {
                break;
            }

            if (response != null && response.getState() == State.TIMEOUT_WAITING_FOR_DATA) {
                logger.debug("NODE {}: No data from device, but it was ACK'd. Possibly not supported? (Try {})",
                        node.getNodeId(), retryCount);
                retryCount++;

                if (retries != 0 && retryCount >= retries) {
                    logger.debug("NODE {}: Node Init transaction retries exceeded", node.getNodeId());
                    return false;
                }
            }

            // If we specify a timeout, then don't sleep!
            if (timeout == 0) {
                // Increase the backoff up to 1800 seconds (approx!)
                if (backoff < 900000) {
                    backoff += backoff + rand.nextInt(1000);
                }

                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        } while (initRunning);

        if (response == null) {
            logger.debug("NODE {}: Node Init transaction completed with response null", node.getNodeId());
            return false;
        }

        logger.debug("NODE {}: Node Init transaction completed with response {}", node.getNodeId(),
                response.getState());

        return true;
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
            processTransaction(transaction);
            if (initRunning == false) {
                return;
            }
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
            processTransaction(node.encapsulate(transaction, commandClass, endpointId));
        }
    }

    private void doInitialStages() {
        setCurrentStage(ZWaveNodeInitStage.IDENTIFY_NODE);
        logger.debug("NODE {}: Node advancer: Initialisation starting", node.getNodeId());

        // Get the device information from the controller
        processTransaction(new IdentifyNodeMessageClass().doRequest(node.getNodeId()));
        if (initRunning == false) {
            return;
        }

        setCurrentStage(ZWaveNodeInitStage.INIT_NEIGHBORS);

        logger.debug("NODE {}: Node advancer: INIT_NEIGHBORS - send RoutingInfo", node.getNodeId());

        processTransaction(new GetRoutingInfoMessageClass().doRequest(node.getNodeId()));
        if (initRunning == false) {
            return;
        }

        // Controllers aren't designed to allow communication with their node.
        // If this is a controller, we're done
        if (node.getDeviceClass().getSpecificDeviceClass() == Specific.SPECIFIC_TYPE_PC_CONTROLLER) {
            logger.debug("NODE {}: Node advancer: FAILED_CHECK - Controller - terminating initialisation",
                    node.getNodeId());
            setCurrentStage(ZWaveNodeInitStage.DONE);
            return;
        }

        setCurrentStage(ZWaveNodeInitStage.FAILED_CHECK);
        do {
            processTransaction(new IsFailedNodeMessageClass().doRequest(node.getNodeId()));
            if (initRunning == false) {
                return;
            }

            // If the node is dead, sleep for 30 seconds then try again
            if (node.isDead()) {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    break;
                }
            } else {
                break;
            }
        } while (true);

        // setCurrentStage(ZWaveNodeInitStage.PING);
        // ZWaveNoOperationCommandClass noOpCommandClass = (ZWaveNoOperationCommandClass) node
        // .getCommandClass(CommandClass.COMMAND_CLASS_NO_OPERATION);
        // if (noOpCommandClass != null) {
        // ZWaveCommandClassTransactionPayload msg = noOpCommandClass.getNoOperationMessage();
        // if (msg == null) {
        // return;
        // }

        // We only send out a single PING - no retries at controller level!
        // This is to try and reduce network congestion during initialisation.
        // For battery devices, the PING will time-out. This takes up to 5 seconds and if there are retries,
        // it will be 15 seconds!
        // This will block the network for a considerable time if there are a lot of battery devices
        // (eg. 2 minutes for 8 battery devices!).
        // msg.setMaxAttempts(1);
        // processTransaction(msg);
        // }

        setCurrentStage(ZWaveNodeInitStage.REQUEST_NIF);
        processTransaction(new RequestNodeInfoMessageClass().doRequest(node.getNodeId()));
        if (initRunning == false) {
            return;
        }
    }

    private void doInitialInclusionStages() {
        setCurrentStage(ZWaveNodeInitStage.IDENTIFY_NODE);
        logger.debug("NODE {}: Node advancer: Initialisation starting from inclusion", node.getNodeId());

        // Get the device information from the controller
        processTransaction(new IdentifyNodeMessageClass().doRequest(node.getNodeId()));
        if (initRunning == false) {
            return;
        }
    }

    private void doSecureStages() {
        setCurrentStage(ZWaveNodeInitStage.SECURITY_REPORT);

        // Does this node support security
        ZWaveSecurityCommandClass securityCommandClass = (ZWaveSecurityCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_SECURITY);
        if (securityCommandClass == null) {
            logger.debug("NODE {}: SECURE command class not supported", node.getNodeId());
            return;
        }

        // Add the network key to the security class
        securityCommandClass.setNetworkKey(controller.getSecurityKey());

        // Check if we want to perform a secure inclusion...
        boolean doSecureInclusion = false;
        switch (controller.getSecureInclusionMode()) {
            default:
            case 0:
                // Only ENTRY_CONTROL
                if (node.getDeviceClass().getGenericDeviceClass() == Generic.GENERIC_TYPE_ENTRY_CONTROL) {
                    doSecureInclusion = true;
                }
                break;
            case 1:
                // All devices
                doSecureInclusion = true;
                break;
            case 2:
                // No secure inclusion
                break;
        }

        if (doSecureInclusion == false) {
            logger.debug("NODE {}: Skipping secure inclusion", node.getNodeId());
            return;
        }

        // Check if this node was just included (within the last 10 seconds or so)
        if (node.getInclusionTimer() < INCLUSION_TIMER) {
            logger.debug("NODE {}: Performing secure inclusion.", node.getNodeId());

            // Get the scheme used for the remote
            logger.debug("NODE {}: SECURITY_INC State=GET_SCHEME", node.getNodeId());
            if (processTransaction(securityCommandClass.getSecuritySchemeGetMessage(), INCLUSION_TIMER, 3) == false) {
                // Notify that secure inclusion failed
                controller.notifyEventListeners(
                        new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.SecureIncludeFailed, node.getNodeId()));
                logger.error("NODE {}: SECURITY_INC State=FAILED", node.getNodeId());

                return;
            }
            if (initRunning == false) {
                return;
            }

            // Set the key
            logger.debug("NODE {}: SECURITY_INC State=SET_KEY", node.getNodeId());
            if (processTransaction(securityCommandClass.getSetSecurityKeyMessage(), INCLUSION_TIMER, 3) == true) {
                // Notify that secure inclusion completed ok
                controller.notifyEventListeners(
                        new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.SecureIncludeComplete, node.getNodeId()));
                logger.error("NODE {}: SECURITY_INC State=COMPLETE", node.getNodeId());
            } else {
                // Notify that secure inclusion failed
                controller.notifyEventListeners(
                        new ZWaveInclusionEvent(ZWaveInclusionEvent.Type.SecureIncludeFailed, node.getNodeId()));
                logger.error("NODE {}: SECURITY_INC State=FAILED", node.getNodeId());

                return;
            }
            if (initRunning == false) {
                return;
            }
        }

        // Do a NONCE request to see if the node responds.
        // We do three tries - if it doesn't respond, and we get the ACK from the device, then we assume the node wasn't
        // securely included
        logger.debug("NODE {}: SECURITY_INC State=SECURE_PING", node.getNodeId());
        if (processTransaction(securityCommandClass.getSecurityNonceGet(), 0, 3) == false) {
            logger.error("NODE {}: SECURITY_INC State=FAILED", node.getNodeId());
            return;
        }

        if (initRunning == false) {
            return;
        }

        // Get the secure classes.
        // Even if we didn't just complete secure inclusion, request the secure supported
        // If we have lost the XML, and have previously securely included, then this will allow the device to be used
        logger.debug("NODE {}: SECURITY_INC State=GET_SECURE_SUPPORTED", node.getNodeId());
        processTransaction(securityCommandClass.getSecurityCommandsSupportedMessage());
        if (initRunning == false) {
            return;
        }
    }

    private void doStaticStages() {
        setCurrentStage(ZWaveNodeInitStage.MANUFACTURER);
        // Try and get the manufacturerSpecific command class.
        ZWaveManufacturerSpecificCommandClass manufacturerSpecific = (ZWaveManufacturerSpecificCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_MANUFACTURER_SPECIFIC);

        if (manufacturerSpecific != null) {
            // If this node implements the Manufacturer Specific command
            // class, we use it to get manufacturer info.
            logger.debug("NODE {}: Node advancer: MANUFACTURER - send ManufacturerSpecific", node.getNodeId());
            processTransaction(manufacturerSpecific.getManufacturerSpecificMessage());
            if (initRunning == false) {
                return;
            }
        }

        setCurrentStage(ZWaveNodeInitStage.APP_VERSION);
        ZWaveVersionCommandClass versionCommandClass = (ZWaveVersionCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_VERSION);

        if (versionCommandClass == null) {
            logger.debug("NODE {}: Node advancer: APP_VERSION - VERSION not supported", node.getNodeId());
        } else {
            // Request the version report for this node
            logger.debug("NODE {}: Node advancer: APP_VERSION - send VersionMessage", node.getNodeId());

            processTransaction(versionCommandClass.getVersionMessage());
            if (initRunning == false) {
                return;
            }

            setCurrentStage(ZWaveNodeInitStage.VERSION);
            thingType = ZWaveConfigProvider.getThingType(node);
            if (thingType == null) {
                logger.debug("NODE {}: Node advancer: VERSION - thing is null!", node.getNodeId());
            }

            // Loop through all command classes, requesting their version
            // using the Version command class

            // We use a new list here so since command classes can be removed in the VERSION class
            Collection<ZWaveCommandClass> classes = new ArrayList<ZWaveCommandClass>(node.getCommandClasses(0));
            for (ZWaveCommandClass zwaveVersionClass : classes) {
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

                    processTransaction(versionCommandClass.checkVersion(zwaveVersionClass));
                    if (initRunning == false) {
                        return;
                    }
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
            boolean first = true;
            do {
                logger.debug("NODE {}: MultiInstance init first={}", node.getNodeId(), first);
                ArrayList<ZWaveCommandClassTransactionPayload> multiInstanceMessages = multiInstance
                        .initEndpoints(first);
                logger.debug("NODE {}: MultiInstance init returned {}", node.getNodeId(), multiInstanceMessages.size());
                if (multiInstanceMessages.isEmpty()) {
                    break;
                }
                processTransactions(multiInstanceMessages);
                if (initRunning == false) {
                    return;
                }
                first = false;
            } while (true);
        } else {
            logger.debug("NODE {}: Node advancer: ENDPOINTS - MultiInstance not supported.", node.getNodeId());
            // Set all classes to 1 instance.
            for (ZWaveCommandClass commandClass : node.getCommandClasses(0)) {
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
                    node.removeCommandClass(CommandClass.getCommandClass(cmds[1]));
                    logger.debug("NODE {}: Node advancer: UPDATE_DATABASE - removing {}", node.getNodeId(),
                            CommandClass.getCommandClass(optionMap.get("ccRemove")));
                    continue;
                }

                // Get the command class
                int endpoint = cmds.length == 2 ? 0 : Integer.parseInt(cmds[2]);
                if (node.getEndpoint(endpoint) != null) {
                    ZWaveCommandClass zwaveClass = node.getEndpoint(endpoint)
                            .getCommandClass(CommandClass.getCommandClass(cmds[1]));

                    // If we found the command class, then set its options
                    if (zwaveClass != null) {
                        zwaveClass.setOptions(optionMap);
                        continue;
                    }
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
        for (ZWaveCommandClass zwaveStaticClass : node.getCommandClasses(0)) {
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
                        if (initRunning == false) {
                            return;
                        }
                    }
                }
            } else if (zwaveStaticClass instanceof ZWaveMultiInstanceCommandClass) {
                for (int endpointNumber = 1; endpointNumber < node.getEndpointCount(); endpointNumber++) {
                    ZWaveEndpoint endpoint = node.getEndpoint(endpointNumber);
                    for (ZWaveCommandClass endpointCommandClass : endpoint.getCommandClasses()) {
                        logger.debug("NODE {}: Node advancer: STATIC_VALUES - checking {} for endpoint {}",
                                node.getNodeId(), endpointCommandClass.getCommandClass(), endpoint.getEndpointId());
                        if (endpointCommandClass instanceof ZWaveCommandClassInitialization) {
                            logger.debug("NODE {}: Node advancer: STATIC_VALUES - found    {}", node.getNodeId(),
                                    endpointCommandClass.getCommandClass());
                            ZWaveCommandClassInitialization zcci2 = (ZWaveCommandClassInitialization) endpointCommandClass;
                            processTransactions(zcci2.initialize(true), endpointCommandClass, endpoint.getEndpointId());
                            if (initRunning == false) {
                                return;
                            }
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
                            processTransaction(node.getAssociation(group));
                            if (initRunning == false) {
                                return;
                            }
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
                processTransaction(wakeupCommandClass.setInterval(value));
                if (initRunning == false) {
                    return;
                }
                processTransaction(wakeupCommandClass.getIntervalMessage());
                if (initRunning == false) {
                    return;
                }
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
                                processTransaction(node.setAssociation(null, groupId, controller.getOwnNodeId(), 0));
                                if (initRunning == false) {
                                    return;
                                }
                                processTransaction(node.getAssociation(groupId));
                                if (initRunning == false) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
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
                                processTransaction(configurationCommandClass.getConfigMessage(index));
                                if (initRunning == false) {
                                    return;
                                }
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
        for (int endpointId = 0; endpointId < node.getEndpointCount(); endpointId++) {
            for (ZWaveCommandClass zwaveDynamicClass : node.getCommandClasses(endpointId)) {
                if (endpointId == 0) {
                    logger.debug("NODE {}: Node advancer: DYNAMIC_VALUES - checking {}", node.getNodeId(),
                            zwaveDynamicClass.getCommandClass());
                } else {
                    logger.debug("NODE {}: Node advancer: DYNAMIC_VALUES - checking {} for endpoint {}",
                            node.getNodeId(), zwaveDynamicClass.getCommandClass(), endpointId);
                }
                if (!(zwaveDynamicClass instanceof ZWaveCommandClassDynamicState)) {
                    continue;
                }

                ZWaveCommandClassDynamicState zdds = (ZWaveCommandClassDynamicState) zwaveDynamicClass;
                int instances = zwaveDynamicClass.getInstances();
                logger.debug("NODE {}: Found {} instances of {}", node.getNodeId(), instances,
                        zwaveDynamicClass.getCommandClass());
                if (instances == 1) {
                    processTransactions(zdds.getDynamicValues(true));
                } else {
                    for (int i = 1; i <= instances; i++) {
                        processTransactions(zdds.getDynamicValues(true), zwaveDynamicClass, i);
                        if (initRunning == false) {
                            return;
                        }
                    }
                }
            }
        }

        logger.debug("NODE {}: Node advancer: Initialisation complete!", node.getNodeId());
    }

    void doHealStages() {
        setCurrentStage(ZWaveNodeInitStage.HEAL_START);
        setCurrentStage(ZWaveNodeInitStage.UPDATE_NEIGHBORS);
        logger.debug("NODE {}: Node advancer: UPDATE_NEIGHBORS - updating neighbor list", node.getNodeId());
        processTransaction(new RequestNodeNeighborUpdateMessageClass().doRequest(node.getNodeId()));
        if (initRunning == false) {
            return;
        }

        setCurrentStage(ZWaveNodeInitStage.GET_NEIGHBORS);
        logger.debug("NODE {}: Node advancer: GET_NEIGHBORS - get RoutingInfo", node.getNodeId());
        processTransaction(new GetRoutingInfoMessageClass().doRequest(node.getNodeId()));
        if (initRunning == false) {
            return;
        }

        setCurrentStage(ZWaveNodeInitStage.DELETE_SUC_ROUTES);
        // Only delete the route if this is not the controller and there is an SUC in the network
        if (node.getNodeId() != controller.getOwnNodeId() && controller.getSucId() != 0) {
            // Update the route to the controller
            logger.debug("NODE {}: Node advancer is deleting SUC return route.", node.getNodeId());
            processTransaction(new DeleteSucReturnRouteMessageClass().doRequest(node.getNodeId()));
            if (initRunning == false) {
                return;
            }
        }

        setCurrentStage(ZWaveNodeInitStage.SUC_ROUTE);
        // Only set the route if this is not the controller and there is an SUC in the network
        if (node.getNodeId() != controller.getOwnNodeId() && controller.getSucId() != 0) {
            // Update the route to the controller
            logger.debug("NODE {}: Node advancer is setting SUC route.", node.getNodeId());
            processTransaction(new AssignSucReturnRouteMessageClass().doRequest(node.getNodeId()));
        }

        setCurrentStage(ZWaveNodeInitStage.DELETE_ROUTES);
        if (node.getRoutingList().size() != 0) {
            // Delete all the return routes for the node
            logger.debug("NODE {}: Node advancer is deleting return routes.", node.getNodeId());
            processTransaction(new DeleteReturnRouteMessageClass().doRequest(node.getNodeId()));
            if (initRunning == false) {
                return;
            }
        }

        setCurrentStage(ZWaveNodeInitStage.RETURN_ROUTES);
        for (Integer route : node.getRoutingList()) {
            // Loop through all the nodes and set the return route
            logger.debug("NODE {}: Adding return route to {}", node.getNodeId(), route);
            processTransaction(new AssignReturnRouteMessageClass().doRequest(node.getNodeId(), route));
            if (initRunning == false) {
                return;
            }
        }
        setCurrentStage(ZWaveNodeInitStage.HEAL_END);
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
            case HEAL_END:
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
