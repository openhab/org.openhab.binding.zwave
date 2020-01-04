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
package org.openhab.binding.zwave.handler;

import static org.openhab.binding.zwave.ZWaveBindingConstants.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveIoHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkStateEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZWaveControllerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Chris Jackson - Initial contribution
 */
public abstract class ZWaveControllerHandler extends BaseBridgeHandler implements ZWaveEventListener, ZWaveIoHandler {

    private final Logger logger = LoggerFactory.getLogger(ZWaveControllerHandler.class);

    private volatile ZWaveController controller;

    private Set<ZWaveEventListener> listeners = new HashSet<ZWaveEventListener>();

    private Boolean isMaster;
    private Integer sucNode;
    private String networkKey;
    private Integer secureInclusionMode;
    private Integer healTime;
    private Integer wakeupDefaultPeriod;

    private final int SEARCHTIME_MINIMUM = 20;
    private final int SEARCHTIME_DEFAULT = 30;
    private final int SEARCHTIME_MAXIMUM = 300;
    private int searchTime;

    private ScheduledFuture<?> healJob = null;

    public ZWaveControllerHandler(@NonNull Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing ZWave Controller {}.", getThing().getUID());

        Object param;
        param = getConfig().get(CONFIGURATION_MASTER);
        if (param instanceof Boolean) {
            isMaster = (Boolean) param;
        } else {
            isMaster = true;
        }

        param = getConfig().get(CONFIGURATION_SECUREINCLUSION);
        if (param instanceof BigDecimal) {
            secureInclusionMode = ((BigDecimal) param).intValue();
        } else {
            secureInclusionMode = 0;
        }

        param = getConfig().get(CONFIGURATION_INCLUSIONTIMEOUT);
        if (param instanceof BigDecimal) {
            searchTime = ((BigDecimal) param).intValue();
        } else {
            searchTime = SEARCHTIME_DEFAULT;
        }

        if (searchTime < SEARCHTIME_MINIMUM || searchTime > SEARCHTIME_MAXIMUM) {
            searchTime = SEARCHTIME_DEFAULT;
        }

        param = getConfig().get(CONFIGURATION_DEFAULTWAKEUPPERIOD);
        if (param instanceof BigDecimal) {
            wakeupDefaultPeriod = ((BigDecimal) param).intValue();
        } else {
            wakeupDefaultPeriod = 0;
        }

        param = getConfig().get(CONFIGURATION_SISNODE);
        if (param instanceof BigDecimal) {
            sucNode = ((BigDecimal) param).intValue();
        } else {
            sucNode = 0;
        }

        param = getConfig().get(CONFIGURATION_NETWORKKEY);
        if (param instanceof String) {
            networkKey = (String) param;
        }
        if (networkKey.length() == 0) {
            logger.debug("No network key set by user - using random value.");

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
                // Eat it...
            }
        }

        param = getConfig().get(CONFIGURATION_HEALTIME);
        if (param instanceof BigDecimal) {
            healTime = ((BigDecimal) param).intValue();
        } else {
            healTime = -1;
        }

        // We must set the state
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, ZWaveBindingConstants.OFFLINE_CTLR_OFFLINE);
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
        config.put("sucNode", sucNode.toString());
        config.put("secureInclusion", secureInclusionMode.toString());
        config.put("networkKey", networkKey);
        config.put("wakeupDefaultPeriod", wakeupDefaultPeriod.toString());

        // TODO: Handle soft reset?
        controller = new ZWaveController(this, config);
        controller.addEventListener(this);

        // Add any listeners that were registered before the manager was registered
        synchronized (listeners) {
            for (ZWaveEventListener listener : listeners) {
                controller.addEventListener(listener);
            }
        }

        initializeHeal();

    }

    /**
     * Common stopping point for all ZWave controllers.
     * Called by bridges when their interfaces are unavailable.
     */
    protected void stopNetwork() {
        logger.debug("Stopping ZWave network");
        if (healJob != null) {
            healJob.cancel(true);
            healJob = null;
        }

        ZWaveController controller = this.controller;
        if (controller != null) {
            this.controller = null;
            synchronized (listeners) {
                for (ZWaveEventListener listener : listeners) {
                    controller.removeEventListener(listener);
                }
            }
            controller.removeEventListener(this);
            controller.shutdown();
        }
        logger.debug("ZWave network stopped");
    }

    private void initializeHeal() {
        if (healJob != null) {
            healJob.cancel(true);
            healJob = null;
        }

        if (healTime >= 0 && healTime <= 23) {
            Runnable healRunnable = new Runnable() {
                @Override
                public void run() {
                    if (controller == null) {
                        return;
                    }
                    logger.debug("Starting network mesh heal for controller {}.", getThing().getUID());
                    for (ZWaveNode node : controller.getNodes()) {
                        logger.debug("Starting network mesh heal for controller {}.", getThing().getUID());
                        node.healNode();
                    }
                }
            };

            Calendar cal = Calendar.getInstance();
            int hours = healTime - cal.get(Calendar.HOUR_OF_DAY);
            if (hours < 0) {
                hours += 24;
            }

            logger.debug("Scheduling network mesh heal for {} hours time.", hours);

            healJob = scheduler.scheduleAtFixedRate(healRunnable, hours, 24, TimeUnit.HOURS);
        }
    }

    @Override
    public void dispose() {
        if (healJob != null) {
            healJob.cancel(true);
            healJob = null;
        }

        ZWaveController controller = this.controller;
        if (controller != null) {
            this.controller = null;
            synchronized (listeners) {
                for (ZWaveEventListener listener : listeners) {
                    controller.removeEventListener(listener);
                }
            }
            controller.removeEventListener(this);
            controller.shutdown();
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
            if (value == null) {
                continue;
            }

            String[] cfg = configurationParameter.getKey().split("_");
            if ("controller".equals(cfg[0])) {
                if (controller == null) {
                    logger.debug("Trying to send controller command, but controller is not initialised");
                    continue;
                }

                if (cfg[1].equals("softreset") && value instanceof Boolean && ((Boolean) value) == true) {
                    controller.requestSoftReset();
                    value = false;
                } else if (cfg[1].equals("hardreset") && value instanceof Boolean && ((Boolean) value) == true) {
                    controller.requestHardReset();
                    value = false;
                } else if (cfg[1].equals("exclude") && value instanceof Boolean && ((Boolean) value) == true) {
                    controller.requestRemoveNodesStart();
                    value = false;
                } else if (cfg[1].equals("sync") && value instanceof Boolean && ((Boolean) value) == true) {
                    controller.requestRequestNetworkUpdate();
                    value = false;
                } else if (cfg[1].equals("suc") && value instanceof Boolean) {
                    // TODO: Do we need to set this immediately
                } else if (cfg[1].equals("inclusiontimeout") && value instanceof BigDecimal) {
                    reinitialise = true;
                }
            }
            if ("security".equals(cfg[0])) {
                if (cfg[1].equals("networkkey")) {
                    // Format the key here so it's presented nicely and consistently to the user!
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

                    reinitialise = true;
                }
            }

            if ("port".equals(cfg[0])) {
                reinitialise = true;
            }

            if ("heal".equals(cfg[0])) {
                healTime = ((BigDecimal) value).intValue();
                initializeHeal();
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
    }

    public void startDeviceDiscovery() {
        if (controller == null) {
            return;
        }

        int inclusionMode = 2;
        Object param = getConfig().get(CONFIGURATION_INCLUSION_MODE);
        if (param instanceof BigDecimal) {
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

    private void updateControllerProperties() {
        Configuration configuration = editConfiguration();
        configuration.put(ZWaveBindingConstants.CONFIGURATION_SISNODE, controller.getSucId());
        try {
            // If the thing is defined statically, then this will fail and we will never start!
            updateConfiguration(configuration);
        } catch (IllegalStateException e) {
            // Eat it...
        }
    }

    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {
        if (event instanceof ZWaveNetworkStateEvent) {
            logger.debug("Controller: Incoming Network State Event {}",
                    ((ZWaveNetworkStateEvent) event).getNetworkState());
            if (((ZWaveNetworkStateEvent) event).getNetworkState() == true) {
                updateStatus(ThingStatus.ONLINE);
                updateControllerProperties();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        ZWaveBindingConstants.OFFLINE_CTLR_OFFLINE);
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

                default:
                    break;
            }
        }
    }

    protected void incomingMessage(SerialMessage serialMessage) {
        if (controller == null) {
            return;
        }
        controller.incomingPacket(serialMessage);
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

    /**
     * Transmits the {@link ZWaveCommandClassTransactionPayload} to a Node.
     * This will not wait for the transaction response.
     *
     * @param transaction the {@link ZWaveCommandClassTransactionPayload} message to send.
     */
    public void sendData(ZWaveCommandClassTransactionPayload transaction) {
        if (controller == null) {
            return;
        }
        controller.sendData(transaction);
    }

    public boolean addEventListener(ZWaveEventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }

        if (controller == null) {
            logger.info("Attempting to add listener when controller is null");
            return false;
        }
        controller.addEventListener(listener);
        return true;
    }

    public boolean removeEventListener(ZWaveEventListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }

        if (controller == null) {
            return false;
        }
        controller.removeEventListener(listener);
        return true;
    }

    /**
     * Gets the default wakeup period configured for this network
     *
     * @return the default wakeup, or null if not set
     */
    public Integer getDefaultWakeupPeriod() {
        return wakeupDefaultPeriod;
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
        controller.requestSetFailedNode(nodeId);
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
            logger.debug("NODE {}: Can't be found!", nodeId);
            return false;
        }

        node.healNode();

        return true;
    }

    public boolean isControllerMaster() {
        return isMaster;
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

    /**
     * Gets the home ID associated with the controller.
     *
     * @return the home ID
     */
    public int getHomeId() {
        return controller.getHomeId();
    }
}
