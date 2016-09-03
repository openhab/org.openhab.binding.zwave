/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.handler;

import static org.openhab.binding.zwave.ZWaveBindingConstants.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.ZWaveBindingConstants.I18nConstant;
import org.openhab.binding.zwave.discovery.ZWaveDiscoveryService;
import org.openhab.binding.zwave.event.BindingEventDTO;
import org.openhab.binding.zwave.event.BindingEventFactory;
import org.openhab.binding.zwave.event.BindingEventType;
import org.openhab.binding.zwave.internal.ZWaveEventPublisher;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveIoHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInitializationStateEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkStateEvent;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;
import org.openhab.binding.zwave.internal.protocol.serialmessage.RemoveFailedNodeMessageClass.Report;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZWaveControllerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 */
public abstract class ZWaveControllerHandler extends BaseBridgeHandler implements ZWaveEventListener, ZWaveIoHandler {

    private Logger logger = LoggerFactory.getLogger(ZWaveControllerHandler.class);

    private ZWaveDiscoveryService discoveryService;
    private ServiceRegistration discoveryRegistration;

    private volatile ZWaveController controller;

    private Boolean isMaster;
    private Boolean isSUC;
    private String networkKey;
    private Integer secureInclusionMode;
    private Integer healTime;

    private final int SEARCHTIME_DEFAULT = 30;
    private int searchTime;

    public ZWaveControllerHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ZWave Controller.");

        Object param;
        param = getConfig().get(CONFIGURATION_MASTER);
        if (param instanceof Boolean && param != null) {
            isMaster = (Boolean) param;
        } else {
            isMaster = true;
        }

        param = getConfig().get(CONFIGURATION_SECUREINCLUSION);
        if (param instanceof BigDecimal && param != null) {
            secureInclusionMode = ((BigDecimal) param).intValue();
        } else {
            secureInclusionMode = 0;
        }

        param = getConfig().get(CONFIGURATION_INCLUSIONTIMEOUT);
        if (param instanceof BigDecimal && param != null) {
            searchTime = ((BigDecimal) param).intValue();
        } else {
            searchTime = SEARCHTIME_DEFAULT;
        }

        param = getConfig().get(CONFIGURATION_SUC);
        if (param instanceof Boolean && param != null) {
            isSUC = (Boolean) param;
        } else {
            isSUC = false;
        }

        param = getConfig().get(CONFIGURATION_NETWORKKEY);
        if (param instanceof String && param != null) {
            networkKey = (String) param;
        }

        if (networkKey.length() == 0) {
            // Create random network key
            networkKey = "";
            for (int cnt = 0; cnt < 16; cnt++) {
                int value = (int) Math.floor((Math.random() * 255));
                if (cnt != 0) {
                    networkKey += " ";
                }
                networkKey += String.format("%02X", value);
            }
            // Persist the value
            Configuration configuration = editConfiguration();
            configuration.put(ZWaveBindingConstants.CONFIGURATION_NETWORKKEY, networkKey);
            try {
                // If the thing is defined statically, then this will fail and we will never start!
                updateConfiguration(configuration);
            } catch (IllegalStateException e) {
                // Eat it for now...
            }
        }

        // We must set the state
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                ZWaveBindingConstants.getI18nConstant(ZWaveBindingConstants.OFFLINE_CTLR_OFFLINE));
    }

    /**
     * Common initialisation point for all ZWave controllers.
     * Called by bridges after they have initialised their interfaces.
     *
     */
    protected void initializeNetwork() {
        logger.debug("Initialising ZWave controller");

        // Create config parameters
        Map<String, String> config = new HashMap<String, String>();
        config.put("masterController", isMaster.toString());
        config.put("isSUC", isSUC ? "true" : "false");
        config.put("secureInclusion", secureInclusionMode.toString());
        config.put("networkKey", networkKey);

        // MAJOR BODGE
        // The security class uses a static member to set the key so for now
        // lets do the same, but it needs to be moved into the network initialisation
        // so different networks can have different keys
        if (networkKey.length() > 0) {
            ZWaveSecurityCommandClass.setRealNetworkKey(networkKey);
        }

        // TODO: Handle soft reset better!
        controller = new ZWaveController(this, config);
        controller.addEventListener(this);

        // if (aliveCheckPeriod != null) {
        // networkMonitor.setPollPeriod(aliveCheckPeriod);
        // }
        // if (softReset != false) {
        // this.networkMonitor.resetOnError(softReset);
        // }

        // The config service needs to know the controller and the network monitor...
        // this.zConfigurationService = new ZWaveConfiguration(this.zController, this.networkMonitor);
        // zController.addEventListener(this.zConfigurationService);

        // Start the discovery service
        discoveryService = new ZWaveDiscoveryService(this, searchTime);
        discoveryService.activate();

        // And register it as an OSGi service
        discoveryRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
    }

    @Override
    public void dispose() {
        // Remove the discovery service
        if (discoveryService != null) {
            discoveryService.deactivate();
        }

        if (discoveryRegistration != null) {
            discoveryRegistration.unregister();
        }

        // if (this.converterHandler != null) {
        // this.converterHandler = null;
        // }

        ZWaveController controller = this.controller;
        if (controller != null) {
            this.controller = null;
            controller.removeEventListener(this);
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        logger.debug("Controller Configuration update received");

        // Perform checking on the configuration
        validateConfigurationParameters(configurationParameters);

        boolean reinitialise = false;

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            Object value = configurationParameter.getValue();
            logger.debug("Controller Configuration update {} to {}", configurationParameter.getKey(), value);
            String[] cfg = configurationParameter.getKey().split("_");
            if ("controller".equals(cfg[0])) {
                if (controller == null) {
                    logger.warn("Trying to send controller command, but controller is not initialised");
                    continue;
                }

                if (cfg[1].equals("softreset") && value instanceof BigDecimal
                        && ((BigDecimal) value).intValue() == ZWaveBindingConstants.ACTION_CHECK_VALUE) {
                    controller.requestSoftReset();

                    value = new BigDecimal(0);
                } else if (cfg[1].equals("hardreset") && value instanceof BigDecimal
                        && ((BigDecimal) value).intValue() == ZWaveBindingConstants.ACTION_CHECK_VALUE) {
                    controller.requestHardReset();

                    value = new BigDecimal(0);
                } else if (cfg[1].equals("exclude") && value instanceof BigDecimal
                        && ((BigDecimal) value).intValue() == ZWaveBindingConstants.ACTION_CHECK_VALUE) {
                    controller.requestRemoveNodesStart();

                    value = new BigDecimal(0);
                } else if (cfg[1].equals("sync") && value instanceof BigDecimal
                        && ((BigDecimal) value).intValue() == ZWaveBindingConstants.ACTION_CHECK_VALUE) {
                    controller.requestRequestNetworkUpdate();

                    value = new BigDecimal(0);
                } else if (cfg[1].equals("suc") && value instanceof Boolean) {
                    // TODO: Do we need to set this immediately
                } else if (cfg[1].equals("inclusiontimeout") && value instanceof BigDecimal) {
                    reinitialise = true;
                }
            }
            if ("security".equals(cfg[0])) {
                if (cfg[1].equals("networkkey")) {
                    // Format the key here so it's presented nicely and consistently to the user!
                    if (value != null) {
                        String hexString = (String) value;
                        hexString = hexString.replace("0x", "");
                        hexString = hexString.replace(",", "");
                        hexString = hexString.replace(" ", "");
                        hexString = hexString.toUpperCase();
                        if ((hexString.length() % 2) != 0) {
                            hexString += "0";
                        }

                        int arrayLength = (int) Math.ceil(((hexString.length() / 2)));
                        String[] result = new String[arrayLength];

                        int j = 0;
                        StringBuilder builder = new StringBuilder();
                        int lastIndex = result.length - 1;
                        for (int i = 0; i < lastIndex; i++) {
                            builder.append(hexString.substring(j, j + 2) + " ");
                            j += 2;
                        }
                        builder.append(hexString.substring(j));
                        value = builder.toString();

                        ZWaveSecurityCommandClass.setRealNetworkKey((String) value);
                    }
                }
            }

            if ("port".equals(cfg[0])) {
                reinitialise = true;
            }

            configuration.put(configurationParameter.getKey(), value);
        }

        // Persist changes
        updateConfiguration(configuration);

        if (reinitialise == true) {
            dispose();
            initialize();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if(channelUID.getId().equals(CHANNEL_1)) {
        // TODO: handle command
        // }
    }

    public void startDeviceDiscovery() {
        if (controller == null) {
            return;
        }

        int inclusionMode = 2;
        Object param = getConfig().get(CONFIGURATION_INCLUSION_MODE);
        if (param instanceof BigDecimal && param != null) {
            inclusionMode = ((BigDecimal) param).intValue();
        }

        controller.requestAddNodesStart(inclusionMode);
    }

    public void stopDeviceDiscovery() {
        if (controller == null) {
            return;
        }
        controller.requestInclusionStop();
    }

    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {
        // If this event requires us to let the users know something, then we create a notification
        I18nConstant eventKey = null;
        BindingEventType eventState = null;
        String eventEntity = null;
        String eventId = null;
        Object eventArgs = null;

        if (event instanceof ZWaveNetworkStateEvent) {
            logger.debug("Controller: Incoming Network State Event {}",
                    ((ZWaveNetworkStateEvent) event).getNetworkState());
            if (((ZWaveNetworkStateEvent) event).getNetworkState() == true) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        ZWaveBindingConstants.getI18nConstant(ZWaveBindingConstants.OFFLINE_CTLR_OFFLINE));
            }
        }

        if (event instanceof ZWaveNetworkEvent) {
            ZWaveNetworkEvent networkEvent = (ZWaveNetworkEvent) event;

            switch (networkEvent.getEvent()) {
                case NodeRoutingInfo:
                    if (networkEvent.getNodeId() == getOwnNodeId()) {
                        updateNeighbours();
                    }
                    break;
                case RemoveFailedNodeID:
                    eventEntity = "network"; // ??
                    eventArgs = new Integer(networkEvent.getNodeId());
                    eventId = ((Report) networkEvent.getValue()).toString();
                    switch ((Report) networkEvent.getValue()) {
                        case FAILED_NODE_NOT_FOUND:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NOTFOUND;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_NOT_PRIMARY_CONTROLLER:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NOTCTLR;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_NOT_REMOVED:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NOTREMOVED;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_NO_CALLBACK_FUNCTION:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NOCALLBACK;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_OK:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NODEOK;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_REMOVED:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_REMOVED;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_REMOVE_FAIL:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_FAILED;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_REMOVE_PROCESS_BUSY:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_BUSY;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_UNKNOWN_FAIL:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_UNKNOWN;
                            eventState = BindingEventType.WARNING;
                            break;
                        default:
                            break;
                    }
                    break;
                case ReplaceFailedNode:
                    eventEntity = "network"; // ??
                    eventArgs = new Integer(networkEvent.getNodeId());
                    eventId = ((Report) networkEvent.getValue()).toString();
                    switch ((Report) networkEvent.getValue()) {
                        case FAILED_NODE_NOT_FOUND:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NOTFOUND;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_NOT_PRIMARY_CONTROLLER:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NOTCTLR;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_NOT_REMOVED:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NOTREMOVED;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_NO_CALLBACK_FUNCTION:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NOCALLBACK;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_OK:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_NODEOK;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_REMOVED:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_REMOVED;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_REMOVE_FAIL:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_FAILED;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_REMOVE_PROCESS_BUSY:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_BUSY;
                            eventState = BindingEventType.WARNING;
                            break;
                        case FAILED_NODE_UNKNOWN_FAIL:
                            eventKey = ZWaveBindingConstants.EVENT_REMOVEFAILED_UNKNOWN;
                            eventState = BindingEventType.WARNING;
                            break;
                        default:
                            break;
                    }
                    break;
                case RequestNetworkUpdate:
                    eventEntity = "network";

                    switch ((int) networkEvent.getValue()) {
                        case 0: // ZW_SUC_UPDATE_DONE
                            eventId = "ZW_SUC_UPDATE_DONE";
                            eventKey = ZWaveBindingConstants.EVENT_NETWORKUPDATE_DONE;
                            eventState = BindingEventType.SUCCESS;
                            break;
                        case 1: // ZW_SUC_UPDATE_ABORT
                            eventId = "ZW_SUC_UPDATE_ABORT";
                            eventKey = ZWaveBindingConstants.EVENT_NETWORKUPDATE_ABORT;
                            eventState = BindingEventType.WARNING;
                            break;
                        case 2: // ZW_SUC_UPDATE_WAIT
                            eventId = "ZW_SUC_UPDATE_WAIT";
                            eventKey = ZWaveBindingConstants.EVENT_NETWORKUPDATE_WAIT;
                            eventState = BindingEventType.WARNING;
                            break;
                        case 3: // ZW_SUC_UPDATE_DISABLED
                            eventId = "ZW_SUC_UPDATE_DISABLED";
                            eventKey = ZWaveBindingConstants.EVENT_NETWORKUPDATE_DISABLED;
                            eventState = BindingEventType.WARNING;
                            break;
                        case 4: // ZW_SUC_UPDATE_OVERFLOW
                            eventId = "ZW_SUC_UPDATE_OVERFLOW";
                            eventKey = ZWaveBindingConstants.EVENT_NETWORKUPDATE_OVERFLOW;
                            eventState = BindingEventType.WARNING;
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

        // Handle node discover inclusion events
        if (event instanceof ZWaveInclusionEvent) {
            ZWaveInclusionEvent incEvent = (ZWaveInclusionEvent) event;

            eventEntity = "network";
            eventId = incEvent.getEvent().toString();
            switch (incEvent.getEvent()) {
                case IncludeStart:
                    eventKey = ZWaveBindingConstants.EVENT_INCLUSION_STARTED;
                    eventState = BindingEventType.SUCCESS;
                    break;
                case IncludeFail:
                    eventKey = ZWaveBindingConstants.EVENT_INCLUSION_FAILED;
                    eventState = BindingEventType.WARNING;
                    break;
                case IncludeDone:
                    // Ignore node 0 - this just indicates inclusion is finished
                    if (incEvent.getNodeId() != 0) {
                        discoveryService.deviceDiscovered(event.getNodeId());
                    }
                    eventKey = ZWaveBindingConstants.EVENT_INCLUSION_COMPLETED;
                    eventState = BindingEventType.SUCCESS;
                    break;
                case ExcludeStart:
                    eventKey = ZWaveBindingConstants.EVENT_EXCLUSION_STARTED;
                    eventState = BindingEventType.SUCCESS;
                    break;
                case ExcludeFail:
                    eventKey = ZWaveBindingConstants.EVENT_EXCLUSION_FAILED;
                    eventState = BindingEventType.WARNING;
                    break;
                case ExcludeDone:
                    eventKey = ZWaveBindingConstants.EVENT_EXCLUSION_COMPLETED;
                    eventState = BindingEventType.SUCCESS;
                    break;
                case ExcludeControllerFound:
                case ExcludeSlaveFound:
                case IncludeControllerFound:
                case IncludeSlaveFound:
                    break;
            }
        }

        if (event instanceof ZWaveInitializationStateEvent) {
            ZWaveInitializationStateEvent initEvent = (ZWaveInitializationStateEvent) event;
            switch (initEvent.getStage()) {
                case DISCOVERY_COMPLETE:
                    // At this point we know enough information about the device to advise the discovery
                    // service that there's a new thing.
                    // We need to do this here as we needed to know the device information such as manufacturer,
                    // type, id and version
                    ZWaveNode node = controller.getNode(initEvent.getNodeId());
                    if (node != null) {
                        deviceAdded(node);
                    }
                default:
                    break;
            }
        }

        if (eventKey != null) {
            EventPublisher ep = ZWaveEventPublisher.getEventPublisher();
            if (ep != null) {
                BindingEventDTO dto = new BindingEventDTO(eventState,
                        ZWaveBindingConstants.getI18nConstant(eventKey, eventArgs));
                Event notification = BindingEventFactory.createBindingEvent(ZWaveBindingConstants.BINDING_ID,
                        eventEntity, eventId, dto);
                ep.post(notification);
            }
        }
    }

    protected void incomingMessage(SerialMessage serialMessage) {
        if (controller == null) {
            return;
        }
        controller.incomingPacket(serialMessage);
    }

    @Override
    public void deviceDiscovered(int nodeId) {
        if (discoveryService == null) {
            return;
        }
        // discoveryService.deviceDiscovered(nodeId);
    }

    public void deviceAdded(ZWaveNode node) {
        if (discoveryService == null) {
            return;
        }
        // ThingUID newThing =
        discoveryService.deviceAdded(node);
        // if (newThing == null) {
        // return;
        // }

        // ThingType thingType = ZWaveConfigProvider.getThingType(newThing.getThingTypeUID());

        // thingType.getProperties()
    }

    public int getOwnNodeId() {
        if (controller == null) {
            return 0;
        }
        return controller.getOwnNodeId();
    }

    public ZWaveNode getNode(int node) {
        if (controller == null) {
            return null;
        }

        return controller.getNode(node);
    }

    public Collection<ZWaveNode> getNodes() {
        if (controller == null) {
            return null;
        }
        return controller.getNodes();
    }

    public void sendData(SerialMessage message) {
        if (controller == null) {
            return;
        }
        controller.sendData(message);
    }

    public boolean addEventListener(ZWaveThingHandler zWaveThingHandler) {
        if (controller == null) {
            return false;
        }
        controller.addEventListener(zWaveThingHandler);
        return true;
    }

    public boolean removeEventListener(ZWaveThingHandler zWaveThingHandler) {
        if (controller == null) {
            return false;
        }
        controller.removeEventListener(zWaveThingHandler);
        return true;
    }

    public UID getUID() {
        return thing.getUID();
    }

    public void removeFailedNode(int nodeId) {
        if (controller == null) {
            return;
        }
        controller.requestRemoveFailedNode(nodeId);
    }

    public void checkNodeFailed(int nodeId) {
        if (controller == null) {
            return;
        }
        controller.requestIsFailedNode(nodeId);
    }

    public void replaceFailedNode(int nodeId) {
        if (controller == null) {
            return;
        }
        controller.requestRemoveFailedNode(nodeId);
    }

    public void reinitialiseNode(int nodeId) {
        if (controller == null) {
            return;
        }
        controller.reinitialiseNode(nodeId);
    }

    public boolean healNode(int nodeId) {
        if (controller == null) {
            return false;
        }
        ZWaveNode node = controller.getNode(nodeId);
        if (node == null) {
            logger.error("NODE {}: Can't be found!", nodeId);
            return false;
        }

        // Only set the HEAL stage if the node is in DONE state
        if (node.getNodeInitStage() != ZWaveNodeInitStage.DONE) {
            logger.debug("NODE {}: Can't start heal when device initialisation is not complete", nodeId);
            return false;
        }

        node.setNodeStage(ZWaveNodeInitStage.HEAL_START);
        return true;
    }

    private void updateNeighbours() {
        if (controller == null) {
            return;
        }

        ZWaveNode node = getNode(getOwnNodeId());
        if (node == null) {
            return;
        }

        String neighbours = "";
        for (Integer neighbour : node.getNeighbors()) {
            if (neighbours.length() != 0) {
                neighbours += ',';
            }
            neighbours += neighbour;
        }
        getThing().setProperty(ZWaveBindingConstants.PROPERTY_NEIGHBOURS, neighbours);
        getThing().setProperty(ZWaveBindingConstants.PROPERTY_NODEID, Integer.toString(getOwnNodeId()));
    }
}
