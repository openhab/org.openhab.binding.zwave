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
package org.openhab.binding.zwave.discovery;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.ZWaveConfigProvider;
import org.openhab.binding.zwave.internal.ZWaveProduct;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInclusionEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveInitializationStateEvent;
import org.openhab.binding.zwave.internal.protocol.serialmessage.ZWaveInclusionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZWaveDiscoveryService} tracks ZWave devices which are associated to a controller.
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ZWaveDiscoveryService extends AbstractDiscoveryService implements ZWaveEventListener {
    private final Logger logger = LoggerFactory.getLogger(ZWaveDiscoveryService.class);

    private final String ZWAVE_NODE_LABEL = "Z-Wave Node %03d";

    private final ZWaveControllerHandler controllerHandler;

    public ZWaveDiscoveryService(ZWaveControllerHandler coordinatorHandler, int searchTime) {
        super(searchTime);
        this.controllerHandler = coordinatorHandler;
        logger.debug("Creating ZWave discovery service for {} with scan time of {}",
                controllerHandler.getThing().getUID(), searchTime);
    }

    public void activate() {
        logger.debug("ZWave discovery: Active {}", controllerHandler.getThing().getUID());
        controllerHandler.addEventListener(this);
    }

    @Override
    public void deactivate() {
        logger.debug("ZWave discovery: Deactivate {}", controllerHandler.getThing().getUID());
        controllerHandler.removeEventListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return ZWaveConfigProvider.getSupportedThingTypes();
    }

    @Override
    public void startScan() {
        logger.debug("ZWave discovery: Start {}", controllerHandler.getThing().getUID());

        // Start the search for new devices
        controllerHandler.startDeviceDiscovery();

        Runnable pollingRunnable = new Runnable() {
            @Override
            public void run() {
                // Add all existing devices
                for (ZWaveNode node : controllerHandler.getNodes()) {
                    deviceAdded(node);
                }
            }
        };

        scheduler.schedule(pollingRunnable, 2, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void abortScan() {
        logger.debug("ZWave discovery: Abort {}", controllerHandler.getThing().getUID());
        controllerHandler.stopDeviceDiscovery();
        super.abortScan();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("ZWave discovery: Stop {}", controllerHandler.getThing().getUID());
        controllerHandler.stopDeviceDiscovery();
        super.stopScan();
    }

    /**
     * Initial device discovered call.
     * This is called when the device is first found. We know very little about the device at
     * this stage - just its node number. We add this to the inbox so the users have feedback
     * that the device is included into the network.
     *
     * {@link #deviceAdded} is called once more information is known about the device to add
     * manufacturer information and other properties.
     *
     * @param nodeId the node to be added
     */
    public void deviceDiscovered(int nodeId) {
        // Don't add the controller as a thing, and only add valid nodes
        if (controllerHandler.getOwnNodeId() == nodeId || nodeId == 0 || nodeId > 232) {
            return;
        }

        ThingUID bridgeUID = controllerHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(new ThingTypeUID(ZWaveBindingConstants.ZWAVE_THING), bridgeUID,
                String.format("node%d", nodeId));

        logger.debug("NODE {}: Device discovered", nodeId);

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(ZWaveBindingConstants.PROPERTY_NODEID, Integer.toString(nodeId));
        properties.put(ZWaveBindingConstants.CONFIGURATION_NODEID, new BigDecimal(nodeId));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(String.format(ZWAVE_NODE_LABEL, nodeId)).build();

        thingDiscovered(discoveryResult);
    }

    /**
     * This is called once the node is fully discovered. At this point we know most of the information about
     * the device including manufacturer information.
     *
     * @param node the node to be added
     */
    public void deviceAdded(ZWaveNode node) {
        // Don't add the controller as a thing, and only add valid nodes
        if (controllerHandler.getOwnNodeId() == node.getNodeId() || node.getNodeId() == 0 || node.getNodeId() > 232) {
            return;
        }

        logger.debug("NODE {}: Device discovery completed", node.getNodeId());

        ThingUID bridgeUID = controllerHandler.getThing().getUID();

        // Search the database for this product information
        ZWaveProduct foundProduct = null;
        for (ZWaveProduct product : ZWaveConfigProvider.getProductIndex()) {
            if (product == null) {
                continue;
            }
            logger.trace("NODE {}: Checking {}", node.getNodeId(), product.getThingTypeUID());
            if (product.match(node) == true) {
                foundProduct = product;
                break;
            }
        }

        ThingTypeUID thingTypeUID;

        // If we didn't find the product, then add the unknown thing
        String label = String.format(ZWAVE_NODE_LABEL, node.getNodeId());
        if (foundProduct == null) {
            if (node.getManufacturer() != Integer.MAX_VALUE) {
                logger.warn("NODE {}: Device discovery could not resolve to a thingType! {}:{}:{}::{}",
                        node.getNodeId(), String.format("%04X", node.getManufacturer()),
                        String.format("%04X", node.getDeviceType()), String.format("%04X", node.getDeviceId()),
                        node.getApplicationVersion());

                label += String.format(" (%04X:%04X:%04X:%s)", node.getManufacturer(), node.getDeviceType(),
                        node.getDeviceId(), node.getApplicationVersion());
            } else {
                logger.warn("NODE {}: Device discovery could not resolve to a thingType! Manufacturer data not known.",
                        node.getNodeId());
            }
            thingTypeUID = new ThingTypeUID(ZWaveBindingConstants.ZWAVE_THING);
        } else {
            logger.debug("NODE {}: Device discovery resolved to thingType {}", node.getNodeId(),
                    foundProduct.getThingTypeUID());

            // And create the new thing
            ThingType thingType = ZWaveConfigProvider.getThingType(foundProduct.getThingTypeUID());
            label += String.format(": %s", thingType.getLabel());

            thingTypeUID = foundProduct.getThingTypeUID();
        }
        // thingTypeUID = new ThingTypeUID(ZWaveBindingConstants.ZWAVE_THING);

        // Create the thing UID
        ThingUID thingUID = new ThingUID(new ThingTypeUID(ZWaveBindingConstants.ZWAVE_THING), bridgeUID,
                String.format("node%d", node.getNodeId()));

        // Add some device properties that might be useful for the system to know
        Map<String, Object> properties = new HashMap<>(11);
        properties.put(ZWaveBindingConstants.CONFIGURATION_NODEID, new BigDecimal(node.getNodeId()));

        // Don't add the device information if we don't know it yet
        // This should also prevent it from being overwritten if it was added previously
        if (node.getManufacturer() != Integer.MAX_VALUE) {
            properties.put(ZWaveBindingConstants.PROPERTY_MANUFACTURER, Integer.toString(node.getManufacturer()));
            properties.put(ZWaveBindingConstants.PROPERTY_DEVICETYPE, Integer.toString(node.getDeviceType()));
            properties.put(ZWaveBindingConstants.PROPERTY_DEVICEID, Integer.toString(node.getDeviceId()));
        }
        properties.put(ZWaveBindingConstants.PROPERTY_VERSION, node.getApplicationVersion());

        // The following properties should always be known as they come from the controller
        properties.put(ZWaveBindingConstants.PROPERTY_CLASS_BASIC,
                node.getDeviceClass().getBasicDeviceClass().toString());
        properties.put(ZWaveBindingConstants.PROPERTY_CLASS_GENERIC,
                node.getDeviceClass().getGenericDeviceClass().toString());
        properties.put(ZWaveBindingConstants.PROPERTY_CLASS_SPECIFIC,
                node.getDeviceClass().getSpecificDeviceClass().toString());
        properties.put(ZWaveBindingConstants.PROPERTY_LISTENING, Boolean.toString(node.isListening()));
        properties.put(ZWaveBindingConstants.PROPERTY_FREQUENT, Boolean.toString(node.isFrequentlyListening()));
        properties.put(ZWaveBindingConstants.PROPERTY_BEAMING, Boolean.toString(node.isBeaming()));
        properties.put(ZWaveBindingConstants.PROPERTY_ROUTING, Boolean.toString(node.isRouting()));

        // Create the discovery result and add to the inbox
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                .withProperties(properties).withBridge(bridgeUID).withLabel(label).build();
        thingDiscovered(discoveryResult);

        return;
    }

    @Override
    public void ZWaveIncomingEvent(ZWaveEvent event) {
        if (event instanceof ZWaveInclusionEvent) {
            ZWaveInclusionEvent incEvent = (ZWaveInclusionEvent) event;
            if (incEvent.getEvent() == ZWaveInclusionState.IncludeDone && incEvent.getNodeId() != 0) {
                deviceDiscovered(event.getNodeId());
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
                    ZWaveNode node = controllerHandler.getNode(initEvent.getNodeId());
                    if (node != null) {
                        deviceAdded(node);
                    }
                default:
                    break;
            }
        }
    }
}