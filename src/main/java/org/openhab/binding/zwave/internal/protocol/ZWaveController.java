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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionState;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiInstanceCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInitializationStateEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkStateEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeStatusEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveTransactionCompletedEvent;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeSerializer;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AddNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.AssignSucReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ControllerSetDefaultMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.DeleteReturnRouteMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.EnableSucMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetControllerCapabilitiesMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetRoutingInfoMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetSucNodeIdMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetVersionMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.IdentifyNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.IsFailedNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.MemoryGetIdMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RemoveFailedNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RemoveNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ReplaceFailedNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNetworkUpdateMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RequestNodeNeighborUpdateMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiGetCapabilitiesMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiGetInitDataMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiSetTimeoutsMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SerialApiSoftResetMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.SetSucNodeMessageClass;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveCommandProcessor;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWave controller class. Implements communication with the ZWave controller
 * stick using serial messages.
 *
 * @author Chris Jackson
 * @author Victor Belov
 * @author Brian Crosby
 */
public class ZWaveController {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveController.class);

    private static final int ZWAVE_RESPONSE_TIMEOUT = 5000;
    // private static final int INITIAL_TX_QUEUE_SIZE = 128;
    // private static final int INITIAL_RX_QUEUE_SIZE = 8;
    // private static final long WATCHDOG_TIMER_PERIOD = 10000;

    public static final int TRANSMIT_OPTION_ACK = 0x01;
    public static final int TRANSMIT_OPTION_AUTO_ROUTE = 0x04;
    private static final int TRANSMIT_OPTION_EXPLORE = 0x20;

    private final ConcurrentHashMap<Integer, ZWaveNode> zwaveNodes = new ConcurrentHashMap<Integer, ZWaveNode>();
    private final ArrayList<ZWaveEventListener> zwaveEventListeners = new ArrayList<ZWaveEventListener>();

    private int zWaveResponseTimeout = ZWAVE_RESPONSE_TIMEOUT; // TODO: Not currently used

    private String zwaveVersion = "Unknown";
    private String serialAPIVersion = "Unknown";
    private int homeId = 0;
    private int ownNodeId = 0;
    private int manufactureId = 0;
    private int deviceType = 0;
    private int deviceId = 0;
    private int zwaveLibraryType = 0;
    private int sentDataPointer = 1;
    private boolean setSUC = false;
    private ZWaveDeviceType controllerType = ZWaveDeviceType.UNKNOWN;
    private int sucID = 0;
    private boolean softReset = false;
    private boolean masterController = true;
    private int secureInclusionMode = 0;
    private String networkSecurityKey;
    private Set<SerialMessageClass> apiCapabilities = new HashSet<>();

    private int defaultWakeupPeriod = 0;

    private final ZWaveTransactionManager transactionManager = new ZWaveTransactionManager(this);

    private AtomicInteger timeOutCount = new AtomicInteger(0);

    private ZWaveIoHandler ioHandler;

    // Constructors
    public ZWaveController(ZWaveIoHandler handler) {
        this(handler, new HashMap<String, String>());
    }

    /**
     * Constructor. Creates a new instance of the ZWave controller class.
     *
     * @param handler the io handler to use for communication with the ZWave controller stick.
     * @param config a map of configuration parameters
     * @throws SerialInterfaceException
     *             when a connection error occurs.
     */
    public ZWaveController(ZWaveIoHandler handler, Map<String, String> config) {
        masterController = "true".equals(config.get("masterController"));
        setSUC = "true".equals(config.get("isSUC"));
        softReset = "true".equals(config.get("softReset"));
        secureInclusionMode = config.containsKey("secureInclusion") ? Integer.parseInt(config.get("secureInclusion"))
                : 0;
        final Integer timeout = config.containsKey("timeout") ? Integer.parseInt(config.get("timeout")) : 0;

        networkSecurityKey = config.get("networkKey");

        defaultWakeupPeriod = config.containsKey("wakeupDefaultPeriod")
                ? Integer.parseInt(config.get("wakeupDefaultPeriod")) : 0;

        logger.info("Starting ZWave controller");

        if (timeout != null && timeout >= 1500 && timeout <= 10000) {
            zWaveResponseTimeout = timeout;
        }
        logger.info("ZWave timeout is set to {}ms. Soft reset is {}.", zWaveResponseTimeout, softReset);

        ioHandler = handler;

        // We have a delay in running the initialisation sequence to allow any frames queued in the controller to be
        // received before sending the init sequence. This avoids protocol errors (CAN errors).
        Timer initTimer = new Timer();
        initTimer.schedule(new InitializeDelayTask(), 3000);
    }

    private class InitializeDelayTask extends TimerTask {
        private final Logger logger = LoggerFactory.getLogger(InitializeDelayTask.class);

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            logger.debug("Initialising network");
            initialize();
        }
    }

    // Incoming message handlers

    public void handleTransactionComplete(ZWaveTransaction currentTransaction, SerialMessage responseMessage) {
        notifyEventListeners(new ZWaveTransactionCompletedEvent(currentTransaction, responseMessage,
                currentTransaction.getTransactionState() == TransactionState.DONE ? true : false));
    }

    /**
     * Handles incoming Serial Messages. Serial messages can either be messages
     * that are a response to our own requests, or the stick asking us information.
     *
     * @param incomingMessage
     *            the incoming message to process.
     */
    public void handleIncomingMessage(ZWaveTransaction currentTransaction, SerialMessage incomingMessage) {
        logger.debug("Incoming Message: {}", incomingMessage.toString());

        try {
            switch (incomingMessage.getMessageType()) {
                case Response:
                    handleIncomingResponseMessage(currentTransaction, incomingMessage);
                    break;
                case Request:
                    handleIncomingRequestMessage(currentTransaction, incomingMessage);
                    break;
                default:
                    logger.warn("Unsupported incomingMessageType: {}", incomingMessage.getMessageType());
            }
        } catch (ZWaveSerialMessageException e) {
            logger.error("Error processing incoming message: {}", e.getMessage());
        }
    }

    /**
     * Handles an incoming request message. An incoming request message is a
     * message initiated by a node or the controller.
     *
     * @param transaction
     *            the transaction associated with this message
     * @param incomingMessage
     *            the incoming message to process.
     */
    private void handleIncomingRequestMessage(ZWaveTransaction transaction, SerialMessage incomingMessage) {
        logger.trace("Incoming Message type = REQUEST");

        ZWaveCommandProcessor processor = ZWaveCommandProcessor.getMessageDispatcher(incomingMessage.getMessageClass());
        if (processor == null) {
            logger.warn(String.format("TODO: Implement processing of Request Message = %s (0x%02X)",
                    incomingMessage.getMessageClass() == null ? "--" : incomingMessage.getMessageClass(),
                    incomingMessage.getMessageClassKey()));

            return;
        }

        try {
            processor.handleRequest(this, transaction, incomingMessage);
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Handles an incoming response message. An incoming response message is a
     * response, based one of our own requests.
     *
     * @param transaction
     *            the transaction associated with this message
     * @param incomingMessage
     *            the response message to process.
     */
    private void handleIncomingResponseMessage(ZWaveTransaction transaction, SerialMessage incomingMessage)
            throws ZWaveSerialMessageException {
        logger.trace("Incoming Message type = RESPONSE");

        ZWaveCommandProcessor processor = ZWaveCommandProcessor.getMessageDispatcher(incomingMessage.getMessageClass());
        if (processor == null) {
            logger.warn(String.format("TODO: Implement processing of Response Message = %s (0x%02X)",
                    incomingMessage.getMessageClass(), incomingMessage.getMessageClass().getKey()));

            return;
        }

        processor.handleResponse(this, transaction, incomingMessage);

        switch (incomingMessage.getMessageClass()) {
            case GetVersion:
                zwaveVersion = ((GetVersionMessageClass) processor).getVersion();
                zwaveLibraryType = ((GetVersionMessageClass) processor).getLibraryType();
                break;
            case MemoryGetId:
                ownNodeId = ((MemoryGetIdMessageClass) processor).getNodeId();
                homeId = ((MemoryGetIdMessageClass) processor).getHomeId();
                break;
            case GetSucNodeId:
                // Remember the SUC ID
                sucID = ((GetSucNodeIdMessageClass) processor).getSucNodeId();

                // If we want to be the SUC, enable it here
                if (setSUC == true && sucID == 0) {
                    // We want to be SUC
                    enqueue(new EnableSucMessageClass().doRequest(EnableSucMessageClass.SUCType.SERVER));
                    enqueue(new SetSucNodeMessageClass().doRequest(ownNodeId, SetSucNodeMessageClass.SUCType.SERVER));
                } else if (setSUC == false && sucID == ownNodeId) {
                    // We don't want to be SUC, but we currently are!
                    // Disable SERVER functionality, and set the node to 0
                    enqueue(new EnableSucMessageClass().doRequest(EnableSucMessageClass.SUCType.NONE));
                    enqueue(new SetSucNodeMessageClass().doRequest(ownNodeId, SetSucNodeMessageClass.SUCType.NONE));
                }
                enqueue(new GetControllerCapabilitiesMessageClass().doRequest());
                break;
            case GetControllerCapabilities:
                controllerType = ((GetControllerCapabilitiesMessageClass) processor).getDeviceType();
                break;
            case SerialApiGetCapabilities:
                serialAPIVersion = ((SerialApiGetCapabilitiesMessageClass) processor).getSerialAPIVersion();
                manufactureId = ((SerialApiGetCapabilitiesMessageClass) processor).getManufactureId();
                deviceId = ((SerialApiGetCapabilitiesMessageClass) processor).getDeviceId();
                deviceType = ((SerialApiGetCapabilitiesMessageClass) processor).getDeviceType();
                apiCapabilities = ((SerialApiGetCapabilitiesMessageClass) processor).getApiCapabilities();

                enqueue(new SerialApiGetInitDataMessageClass().doRequest());
                break;
            case SerialApiGetInitData:
                List<Thread> initList = new ArrayList<Thread>();
                for (Integer nodeId : ((SerialApiGetInitDataMessageClass) processor).getNodes()) {
                    initList.add(addNode(nodeId));
                }

                // Wait for all threads to complete starting initialisation before we advise the system
                new ZWaveInitWaitThread(initList).start();
                break;
            default:
                break;
        }
    }

    // Controller methods

    /**
     * Removes the node, and restarts the initialisation sequence
     *
     * @param nodeId
     */
    public void reinitialiseNode(int nodeId) {
        zwaveNodes.remove(nodeId);
        addNode(nodeId);
    }

    /**
     * Add a node to the controller
     *
     * @param nodeId
     *            the node number to add
     */
    private ZWaveInitNodeThread addNode(int nodeId) {
        ZWaveEvent zEvent = new ZWaveInitializationStateEvent(nodeId, ZWaveNodeInitStage.EMPTYNODE);
        notifyEventListeners(zEvent);

        // if (nodeId != 6) {
        // return;
        // }

        ioHandler.deviceDiscovered(nodeId);
        ZWaveInitNodeThread thread = new ZWaveInitNodeThread(this, nodeId);
        thread.setName("Node_" + nodeId + "_init");
        thread.start();

        return thread;
    }

    /**
     * Thread to wait for all nodes to complete the init thread before notifying the system that we're alive.
     * This ensures that all nodes exist in the system before application layers come online.
     *
     */
    private class ZWaveInitWaitThread extends Thread {
        List<Thread> initList;

        ZWaveInitWaitThread(List<Thread> initList) {
            this.initList = initList;
        }

        @Override
        public void run() {
            logger.debug("Starting waiting for init threads");
            for (Thread thread : initList) {
                try {
                    logger.debug("Waiting for init thread {}", thread.getName());
                    thread.join();
                    logger.debug("Init thread {} complete", thread.getName());
                } catch (InterruptedException e) {
                }
            }
            logger.debug("All init threads complete");
            notifyEventListeners(new ZWaveNetworkStateEvent(true));
        }
    }

    private class ZWaveInitNodeThread extends Thread {
        int nodeId;
        ZWaveController controller;

        ZWaveInitNodeThread(ZWaveController controller, int nodeId) {
            this.nodeId = nodeId;
            this.controller = controller;
        }

        @Override
        public void run() {
            logger.debug("NODE {}: Init node thread start", nodeId);

            // Check if the node exists
            if (zwaveNodes.get(nodeId) != null) {
                logger.warn("NODE {}: Attempting to add node that already exists", nodeId);
                return;
            }

            boolean serializedOk = false;
            ZWaveNode node = null;
            try {
                ZWaveNodeSerializer nodeSerializer = new ZWaveNodeSerializer();
                node = nodeSerializer.DeserializeNode(homeId, nodeId);
            } catch (Exception e) {
                logger.error("NODE {}: Restore from config: Error deserialising XML file. {}", nodeId, e.toString());
                node = null;
            }

            // Did the node deserialise ok?
            if (node != null) {
                // Sanity check the data from the file
                if (node.getManufacturer() == Integer.MAX_VALUE || node.getHomeId() != controller.homeId
                        || node.getNodeId() != nodeId) {
                    logger.warn("NODE {}: Restore from config: Error. Data invalid, ignoring config.", nodeId);
                    node = null;
                } else {
                    // The restore was ok, but we have some work to set up the links that aren't
                    // made as the deserialiser doesn't call the constructor
                    serializedOk = true;
                    logger.debug("NODE {}: Restore from config: Ok.", nodeId);
                    node.setRestoredFromConfigfile(controller);

                    // Set the controller and node references for all command classes
                    for (ZWaveCommandClass commandClass : node.getCommandClasses(0)) {
                        commandClass.initialise(node, controller, null);

                        // Handle event handlers
                        if (commandClass instanceof ZWaveEventListener) {
                            controller.addEventListener((ZWaveEventListener) commandClass);
                        }

                        // If this is the multi-instance class, add all command classes for the endpoints
                        if (commandClass instanceof ZWaveMultiInstanceCommandClass) {
                            for (int endpointNumber = 1; endpointNumber < node.getEndpointCount(); endpointNumber++) {
                                ZWaveEndpoint endPoint = node.getEndpoint(endpointNumber);
                                for (ZWaveCommandClass endpointCommandClass : endPoint.getCommandClasses()) {
                                    endpointCommandClass.initialise(node, controller, endPoint);

                                    // Handle event handlers
                                    if (endpointCommandClass instanceof ZWaveEventListener) {
                                        controller.addEventListener((ZWaveEventListener) endpointCommandClass);
                                    }
                                }
                            }
                        }

                        // If this is the security command class, set the key
                        if (commandClass instanceof ZWaveSecurityCommandClass) {
                            ((ZWaveSecurityCommandClass) commandClass).setNetworkKey(networkSecurityKey);
                        }
                    }
                }
            }

            // Create a new node if it wasn't deserialised ok
            if (node == null) {
                node = new ZWaveNode(controller.homeId, nodeId, controller);
            }

            if (nodeId == controller.ownNodeId) {
                // This is the controller node.
                // We already know the device type, id, manufacturer so set it here.
                // It won't be set later as we probably won't request the manufacturer specific data
                node.setDeviceId(controller.getDeviceId());
                node.setDeviceType(controller.getDeviceType());
                node.setManufacturer(controller.getManufactureId());
            }

            // Place nodes in the local ZWave Controller
            controller.zwaveNodes.putIfAbsent(nodeId, node);

            // If we loaded from file, then we need to add this to the discovery
            // since we bypass the initial discovery phases
            if (serializedOk == true) {
                ZWaveEvent zEvent = new ZWaveInitializationStateEvent(node.getNodeId(),
                        ZWaveNodeInitStage.DISCOVERY_COMPLETE);
                controller.notifyEventListeners(zEvent);
            }

            // Kick off the initialisation
            node.initialiseNode();

            logger.debug("NODE {}: Init node thread finished", nodeId);
        }
    }

    public void sendPacket(SerialMessage message) {
        ioHandler.sendPacket(message);
    }

    /**
     * Queues a message for sending on the send queue.
     * This does not wait for a response.
     *
     * @param transaction
     *            the {@link ZWaveMessagePayloadTransaction} message to enqueue.
     */
    public void enqueue(ZWaveMessagePayloadTransaction transaction) {
        // Sanity check!
        if (transaction == null) {
            logger.debug("Attempt to queue null message");
            return;
        }

        // Since this method doesn't wait for a response, we tell the transaction handler not to wait for DATA
        if (transaction instanceof ZWaveCommandClassTransactionPayload) {
            ((ZWaveCommandClassTransactionPayload) transaction).setRequiresResponse(false);
        }

        transactionManager.queueTransactionForSend(transaction);
    }

    /**
     * Queues a message for sending on the nonce send queue.
     * This does not wait for a response.
     *
     * @param transaction
     *            the {@link ZWaveMessagePayloadTransaction} message to enqueue.
     */
    public void enqueueNonce(ZWaveMessagePayloadTransaction transaction) {
        // Sanity check!
        if (transaction == null) {
            logger.debug("Attempt to queue null message");
            return;
        }

        transactionManager.queueNonceReportForSend(transaction);
    }

    /**
     * Queues a message for sending and returns the response once received.
     *
     * @param transaction
     *            the {@link ZWaveMessagePayloadTransaction} message to enqueue.
     */
    public ZWaveTransactionResponse sendTransaction(ZWaveMessagePayloadTransaction transaction) {
        return transactionManager.sendTransaction(transaction);
    }

    /**
     * Returns the size of the send queue for a specific node.
     */
    public int getSendQueueLength(int nodeId) {
        return transactionManager.getSendQueueLength(nodeId);
    }

    /**
     * Notify our own event listeners of a ZWave event.
     *
     * @param event
     *            the event to send.
     */
    public void notifyEventListeners(ZWaveEvent event) {
        logger.debug("Notifying event listeners: {}", event.getClass().getSimpleName());
        ArrayList<ZWaveEventListener> copy = new ArrayList<ZWaveEventListener>(zwaveEventListeners);
        for (ZWaveEventListener listener : copy) {
            listener.ZWaveIncomingEvent(event);
        }

        // We also need to handle the inclusion internally within the controller
        if (event instanceof ZWaveInclusionEvent) {
            ZWaveInclusionEvent incEvent = (ZWaveInclusionEvent) event;
            switch (incEvent.getEvent()) {
                case IncludeSlaveFound:
                    // When a device is found we get the IncludeSlaveFound notification.
                    // Here we need to end inclusion.
                    requestInclusionStop();
                    logger.debug("NODE {}: Including node.", incEvent.getNodeId());

                    // First make sure this isn't an existing node
                    if (getNode(incEvent.getNodeId()) != null) {
                        logger.debug("NODE {}: Newly included node already exists - not initialising.",
                                incEvent.getNodeId());
                        break;
                    }

                    // Create a new node
                    ZWaveNode newNode = new ZWaveNode(homeId, incEvent.getNodeId(), this);

                    // Add the device class
                    ZWaveDeviceClass deviceClass = newNode.getDeviceClass();
                    deviceClass.setBasicDeviceClass(incEvent.getBasic());
                    deviceClass.setGenericDeviceClass(incEvent.getGeneric());
                    deviceClass.setSpecificDeviceClass(incEvent.getSpecific());

                    // Add mandatory classes
                    newNode.addCommandClass(ZWaveCommandClass
                            .getInstance(CommandClass.COMMAND_CLASS_NO_OPERATION.getKey(), newNode, this));
                    newNode.addCommandClass(
                            ZWaveCommandClass.getInstance(CommandClass.COMMAND_CLASS_BASIC.getKey(), newNode, this));

                    // If we have the NIF as part of the inclusion, use it
                    for (CommandClass commandClass : incEvent.getCommandClasses()) {
                        // We're only interested in the security command class!
                        // We don't add other classes since the list of non-secure classes can change after inclusion
                        // so we need to request the NIF after inclusion is complete.
                        // if (commandClass != CommandClass.COMMAND_CLASS_SECURITY) {
                        // continue;
                        // }
                        ZWaveCommandClass zwaveCommandClass = ZWaveCommandClass.getInstance(commandClass.getKey(),
                                newNode, this);
                        if (zwaveCommandClass != null) {
                            logger.debug("NODE {}: Inclusion is adding command class {}.", incEvent.getNodeId(),
                                    commandClass);

                            // Add the network key to the security class
                            if (commandClass == CommandClass.COMMAND_CLASS_SECURITY) {
                                ((ZWaveSecurityCommandClass) zwaveCommandClass).setNetworkKey(networkSecurityKey);
                            }
                            newNode.addCommandClass(zwaveCommandClass);
                        }
                    }

                    newNode.setInclusionTimer();

                    // Place nodes in the local ZWave Controller
                    zwaveNodes.putIfAbsent(incEvent.getNodeId(), newNode);
                    break;

                case IncludeDone:
                    // Ignore node 0 - this just indicates inclusion finished
                    if (incEvent.getNodeId() == 0) {
                        break;
                    }

                    // End inclusion
                    stopInclusionTimer();

                    // Kick off the initialisation.
                    // Since the node is awake, we jump straight into the initialisation sequence
                    // without some of the initial stages like PING that are designed to detect if
                    // the device is responding.
                    // This is primarily designed to speed up the secure inclusion but is valid for all.
                    // TODO: There's an assumption here that the whole NIF is provided with the inclusion method
                    // -- we might want to keep an eye on this in case it's incorrect!
                    ZWaveNode node = getNode(incEvent.getNodeId());
                    if (node == null) {
                        logger.debug("NODE {}: Newly included node doesn't exist - initialising from start.",
                                incEvent.getNodeId());
                        // Add the node using addNode()
                        // This seems to happen if we exclude the node, then add it back in.
                        // We don't get the IncludeSlaveFound notification, just the IncludeDone notification.
                        addNode(incEvent.getNodeId());
                        break;
                    }

                    // If this node is already initialising, then do nothing.
                    // This might happen if a node is re-added even when we are already aware of it
                    if (node.getNodeInitStage() != ZWaveNodeInitStage.EMPTYNODE) {
                        logger.debug("NODE {}: Newly included node already initialising at {}", incEvent.getNodeId(),
                                node.getNodeInitStage());
                        break;
                    }

                    // Start initialisation...
                    // If we just included this through the IncludeSlaveFound, then we'll already know the device class
                    if (node.getDeviceClass().getBasicDeviceClass() != Basic.BASIC_TYPE_UNKNOWN) {
                        node.initialiseNode(ZWaveNodeInitStage.INCLUSION_START);
                    } else {
                        node.initialiseNode(ZWaveNodeInitStage.EMPTYNODE);
                    }
                    break;

                case ExcludeDone:
                    // Ignore node 0 - this just indicates exclusion finished
                    if (incEvent.getNodeId() == 0) {
                        break;
                    }

                    // End exclusion
                    stopInclusionTimer();

                    logger.debug("NODE {}: Excluding node.", incEvent.getNodeId());
                    // Remove the node from the controller
                    if (getNode(incEvent.getNodeId()) == null) {
                        logger.debug("NODE {}: Excluding node that doesn't exist.", incEvent.getNodeId());
                        break;
                    }
                    removeNode(incEvent.getNodeId());
                    break;
                default:
                    break;
            }
        } else if (event instanceof ZWaveNetworkEvent) {
            ZWaveNetworkEvent networkEvent = (ZWaveNetworkEvent) event;
            switch (networkEvent.getEvent()) {
                case DeleteNode:
                    if (getNode(networkEvent.getNodeId()) == null) {
                        logger.debug("NODE {}: Deleting a node that doesn't exist.", networkEvent.getNodeId());
                        break;
                    }
                    this.zwaveNodes.remove(networkEvent.getNodeId());

                    // Remove the XML file
                    ZWaveNodeSerializer nodeSerializer = new ZWaveNodeSerializer();
                    nodeSerializer.DeleteNode(homeId, event.getNodeId());
                    break;
                default:
                    break;
            }
        } else if (event instanceof ZWaveNodeStatusEvent) {
            ZWaveNodeStatusEvent statusEvent = (ZWaveNodeStatusEvent) event;
            logger.debug("NODE {}: Node Status event - Node is {}", statusEvent.getNodeId(), statusEvent.getState());

            // Get the node
            ZWaveNode node = getNode(event.getNodeId());
            if (node == null) {
                logger.debug("NODE {}: Node is unknown!", statusEvent.getNodeId());
                return;
            }

            // Handle node state changes
            switch (statusEvent.getState()) {
                case DEAD:
                    break;
                case FAILED:
                    break;
                case ALIVE:
                    break;
            }
        }
    }

    /**
     * Initializes communication with the ZWave controller stick.
     *
     */
    public void initialize() {
        enqueue(new GetVersionMessageClass().doRequest());
        enqueue(new MemoryGetIdMessageClass().doRequest());
        enqueue(new SerialApiGetCapabilitiesMessageClass().doRequest());
        enqueue(new SerialApiSetTimeoutsMessageClass().doRequest(150, 15));
        enqueue(new GetSucNodeIdMessageClass().doRequest());
    }

    /**
     * Send Identify Node message to the controller.
     *
     * @param nodeId
     *            the nodeId of the node to identify
     *
     */
    public void identifyNode(int nodeId) {
        enqueue(new IdentifyNodeMessageClass().doRequest(nodeId));
    }

    /**
     * Request the node routing information.
     *
     * @param nodeId
     *            The address of the node to update
     *
     */
    public void requestNodeRoutingInfo(int nodeId) {
        enqueue(new GetRoutingInfoMessageClass().doRequest(nodeId));
    }

    /**
     * Request the node neighbor list to be updated for the specified node. Once
     * this is complete, the requestNodeRoutingInfo will be called automatically
     * to update the data in the binding.
     *
     * @param nodeId
     *            The address of the node to update
     *
     */
    public void requestNodeNeighborUpdate(int nodeId) {
        enqueue(new RequestNodeNeighborUpdateMessageClass().doRequest(nodeId));
    }

    /**
     * Puts the controller into inclusion mode to add new nodes
     *
     * @param inclusionMode the mode to use for inclusion.
     *            <br>
     *            0=Low Power Inclusion
     *            <br>
     *            1=High Power Inclusion
     *            <br>
     *            2=Network Wide Inclusion
     *
     */
    public void requestAddNodesStart(int inclusionMode) {
        if (exclusion == true || inclusion == true) {
            logger.debug("ZWave exclusion already in progress - aborted");
            return;
        }

        logger.debug("ZWave controller start inclusion - mode {}", inclusionMode);

        // Check if the stick supports NWI - if not, revert to HPI
        if (inclusionMode == 2 && hasApiCapability(SerialMessageClass.ExploreRequestInclusion) == false) {
            inclusionMode = 1;
        }

        boolean highPower;
        boolean networkWide;
        switch (inclusionMode) {
            case 0:
                highPower = false;
                networkWide = false;
                break;
            case 1:
                highPower = true;
                networkWide = false;
                break;
            default:
                highPower = true;
                networkWide = true;
                break;
        }

        enqueue(new AddNodeMessageClass().doRequestStart(highPower, networkWide));
        inclusion = true;
        startInclusionTimer();
    }

    /**
     * Terminates the inclusion mode
     *
     */
    private void requestAddNodesStop() {
        enqueue(new AddNodeMessageClass().doRequestStop());
        logger.debug("ZWave controller end inclusion");
    }

    /**
     * Puts the controller into exclusion mode to remove new nodes
     *
     */
    public void requestRemoveNodesStart() {
        if (exclusion == true || inclusion == true) {
            logger.debug("ZWave exclusion already in progress - aborted");
            return;
        }
        enqueue(new RemoveNodeMessageClass().doRequestStart());
        exclusion = true;
        startInclusionTimer();
        logger.debug("ZWave controller start exclusion");
    }

    /**
     * Requests a network update
     *
     */
    public void requestRequestNetworkUpdate() {
        enqueue(new RequestNetworkUpdateMessageClass().doRequest());
        logger.debug("ZWave controller request network update");
    }

    /**
     * Terminates the exclusion mode
     *
     */
    private void requestRemoveNodesStop() {
        enqueue(new RemoveNodeMessageClass().doRequestStop());
        logger.debug("ZWave controller end exclusion");
    }

    /**
     * Terminates inclusion or exclusion mode - which-ever is running
     *
     */
    public void requestInclusionStop() {
        stopInclusionTimer();
    }

    // The following timer class implements a re-triggerable timer to stop the inclusion
    // mode after 30 seconds.
    private Timer timer = new Timer();
    private TimerTask timerTask = null;
    private boolean inclusion = false;
    private boolean exclusion = false;

    private class InclusionTimerTask extends TimerTask {
        @Override
        public void run() {
            logger.debug("Ending inclusion mode.");
            stopInclusionTimer();
        }
    }

    private synchronized void startInclusionTimer() {
        // Stop any existing timer
        if (timerTask != null) {
            timerTask.cancel();
        }

        // Create the timer task
        timerTask = new InclusionTimerTask();

        // Start the timer for 30 seconds
        timer.schedule(timerTask, 30000);
    }

    /**
     * Stops any pending inclusion/exclusion.
     * Resets flags, and signals to controller.
     */
    private synchronized void stopInclusionTimer() {
        logger.debug("Stopping inclusion timer.");
        if (inclusion) {
            requestAddNodesStop();
        } else if (exclusion) {
            requestRemoveNodesStop();
        } else {
            logger.debug("Neither inclusion nor exclusion was active!");
        }

        inclusion = false;
        exclusion = false;

        // Stop the timer
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /**
     * Sends a request to perform a soft reset on the controller. This will just
     * reset the controller - probably similar to a power cycle. It doesn't
     * reinitialise the network, or change the network configuration.
     * <p>
     * <b>NOTE</b>: On some (most!) sticks, this doesn't return a response. Therefore, the number of retries is set to
     * 1. <br>
     * <b>NOTE</b>: On some (most!) ZWave-Plus sticks, this can cause the stick to hang.
     *
     */
    public void requestSoftReset() {
        ZWaveSerialPayload msg = new SerialApiSoftResetMessageClass().doRequest();
        // msg.attempts = 1;
        enqueue(msg);
        logger.debug("ZWave controller soft reset");
    }

    /**
     * Sends a request to perform a hard reset on the controller.
     * This will reset the controller to its default, resetting the network completely
     *
     */
    public void requestHardReset() {
        // Clear the queues
        // If we're resetting, there's no point in queuing messages!
        // sendQueue.clear();
        // recvQueue.clear();

        // TODO: Clear RX queue as well?
        transactionManager.clearSendQueue();

        ZWaveSerialPayload msg = new ControllerSetDefaultMessageClass().doRequest();
        // msg.attempts = 1;
        enqueue(msg);

        // Clear all the nodes and we'll reinitialise
        zwaveNodes.clear();
        enqueue(new SerialApiGetInitDataMessageClass().doRequest());
        logger.debug("ZWave controller hard reset");
    }

    /**
     * Request if the node is currently marked as failed by the controller.
     *
     * @param nodeId
     *            The address of the node to check
     */
    public void requestIsFailedNode(int nodeId) {
        enqueue(new IsFailedNodeMessageClass().doRequest(nodeId));
    }

    /**
     * Removes a failed node from the network. Note that this won't remove nodes
     * that have not failed.
     *
     * @param nodeId
     *            The address of the node to remove
     */
    public void requestRemoveFailedNode(int nodeId) {
        enqueue(new RemoveFailedNodeMessageClass().doRequest(nodeId));
    }

    /**
     * Marks a node as failed
     *
     * @param nodeId
     *            The address of the node to set failed
     */
    public void requestSetFailedNode(int nodeId) {
        enqueue(new ReplaceFailedNodeMessageClass().doRequest(nodeId));
    }

    /**
     * Delete all return nodes from the specified node. This should be performed
     * before updating the routes
     *
     * @param nodeId
     */
    public void requestDeleteAllReturnRoutes(int nodeId) {
        enqueue(new DeleteReturnRouteMessageClass().doRequest(nodeId));
    }

    /**
     * Request the controller to set the return route between two nodes
     *
     * @param nodeId
     *            Source node
     * @param destinationId
     *            Destination node
     */
    public void requestAssignReturnRoute(int nodeId, int destinationId) {
        enqueue(new AssignReturnRouteMessageClass().doRequest(nodeId, destinationId));
    }

    /**
     * Request the controller to set the return route from a node to the
     * controller
     *
     * @param nodeId
     *            Source node
     */
    public void requestAssignSucReturnRoute(int nodeId) {
        enqueue(new AssignSucReturnRouteMessageClass().doRequest(nodeId));
    }

    /**
     * Transmits the {@link ZWaveCommandClassTransactionPayload} to a single ZWave Node.
     *
     * @param payload
     *            the {@link ZWaveCommandClassTransactionPayload} message to send.
     */
    public void sendData(ZWaveCommandClassTransactionPayload transaction) {
        if (transaction == null) {
            return;
        }
        enqueue(transaction);
    }

    /**
     * Add a listener for ZWave events to this controller.
     *
     * @param eventListener
     *            the event listener to add.
     */
    public void addEventListener(ZWaveEventListener eventListener) {
        synchronized (zwaveEventListeners) {
            // First, check if this listener is already registered
            if (zwaveEventListeners.contains(eventListener)) {
                logger.debug("Event Listener {} already registered", eventListener);
                return;
            }
            zwaveEventListeners.add(eventListener);
            logger.debug("Event listener added.");
        }
    }

    /**
     * Remove a listener for ZWave events to this controller.
     *
     * @param eventListener
     *            the event listener to remove.
     */
    public void removeEventListener(ZWaveEventListener eventListener) {
        synchronized (zwaveEventListeners) {
            zwaveEventListeners.remove(eventListener);
        }
    }

    /**
     * Gets the API Version of the controller.
     *
     * @return the serialAPIVersion
     */
    public String getSerialAPIVersion() {
        return serialAPIVersion;
    }

    /**
     * Gets the zWave Version of the controller.
     *
     * @return the zWaveVersion
     */
    public String getZWaveVersion() {
        return zwaveVersion;
    }

    /**
     * Gets the Manufacturer ID of the controller.
     *
     * @return the manufactureId
     */
    public int getManufactureId() {
        return manufactureId;
    }

    /**
     * Gets the device type of the controller;
     *
     * @return the deviceType
     */
    public int getDeviceType() {
        return deviceType;
    }

    /**
     * Gets the device ID of the controller.
     *
     * @return the deviceId
     */
    public int getDeviceId() {
        return deviceId;
    }

    /**
     * Gets the node ID of the controller.
     *
     * @return the deviceId
     */
    public int getOwnNodeId() {
        return ownNodeId;
    }

    /**
     * Gets the device type of the controller.
     *
     * @return the device type
     */
    public ZWaveDeviceType getControllerType() {
        return controllerType;
    }

    /**
     * Gets the networks SUC controller ID.
     *
     * @return the device id of the SUC, or 0 if none exists
     */
    public int getSucId() {
        return sucID;
    }

    /**
     * Returns true if the binding is the master controller in the network. The
     * master controller simply means that we get notifications.
     *
     * @return true if this is the master
     */
    public boolean isMasterController() {
        return masterController;
    }

    /**
     * Checks if the serial API supports the given capability.
     *
     * @param capability
     *            the capability to check
     * @return true if the controller API support the capability
     */
    public boolean hasApiCapability(SerialMessageClass capability) {
        return apiCapabilities.contains(capability);
    }

    /**
     * Returns the secure inclusion mode
     *
     * @return
     *         0 ENTRY_CONTROL
     *         1 All Devices
     */
    public int getSecureInclusionMode() {
        return secureInclusionMode;
    }

    /**
     * Gets the node object using it's node ID as key. Returns null if the node is not found
     *
     * @param nodeId
     *            the Node ID of the node to get.
     * @return node object
     */
    public ZWaveNode getNode(int nodeId) {
        return zwaveNodes.get(nodeId);
    }

    /**
     * Removes the node object using it's node ID as key.
     *
     * @param nodeId
     *            the Node ID of the node to get.
     * @return node object
     */
    public void removeNode(int nodeId) {
        ZWaveNode node = getNode(nodeId);
        if (node != null) {
            node.close();
            zwaveNodes.remove(nodeId);
        }

        // Remove the XML file
        ZWaveNodeSerializer nodeSerializer = new ZWaveNodeSerializer();
        nodeSerializer.DeleteNode(homeId, nodeId);
    }

    /**
     * Gets the node list
     *
     * @return
     */
    public Collection<ZWaveNode> getNodes() {
        return zwaveNodes.values();
    }

    /**
     * Returns the number of Time-Outs while sending.
     *
     * @return the timeoutCount
     */
    public int getTimeOutCount() {
        return timeOutCount.get();
    }

    // Nested classes and enumerations

    public void incomingPacket(SerialMessage packet) {
        // Add the packet to the receive queue
        transactionManager.processReceiveMessage(packet);
    }

    /**
     * Gets the system wide default wakeup period
     *
     * @return the wakeup period in seconds, or 0 if no default is set
     */
    public int getSystemDefaultWakeupPeriod() {
        return defaultWakeupPeriod;
    }

    public String getSecurityKey() {
        return networkSecurityKey;
    }
}
