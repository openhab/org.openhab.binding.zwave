/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.ZWaveConfigProvider;
import org.openhab.binding.zwave.internal.ZWaveProduct;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZWaveDiscoveryService} tracks ZWave devices which are associated to a controller.
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ZWaveDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(ZWaveDiscoveryService.class);

    private final static int SEARCH_TIME = 30;

    private ZWaveControllerHandler controllerHandler;
    private DiscoveryServiceCallback discoveryServiceCallback;

    public ZWaveDiscoveryService(ZWaveControllerHandler coordinatorHandler) {
        super(SEARCH_TIME);
        this.controllerHandler = coordinatorHandler;
    }

    public void activate() {
        logger.debug("Activating ZWave discovery service for {}", controllerHandler.getThing().getUID());

        // Listen for device events
        // coordinatorHandler.addDeviceListener(this);

        // startScan();
    }

    @Override
    public void deactivate() {
        logger.debug("Deactivating ZWave discovery service for {}", controllerHandler.getThing().getUID());

        // Remove the listener
        // coordinatorHandler.removeDeviceListener(this);
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return ZWaveConfigProvider.getSupportedThingTypes();
    }

    @Override
    public void startScan() {
        logger.debug("Starting ZWave inclusion scan for {}", controllerHandler.getThing().getUID());

        // Add all existing devices
        for (ZWaveNode node : controllerHandler.getNodes()) {
            // if (node.getManufacturer() == Integer.MAX_VALUE) {
            // deviceDiscovered(node.getNodeId());
            // } else {
            deviceAdded(node);
            // }
        }

        // Start the search for new devices
        controllerHandler.startDeviceDiscovery();
    }

    @Override
    public synchronized void abortScan() {
        controllerHandler.stopDeviceDiscovery();
        super.abortScan();
    }

    @Override
    protected synchronized void stopScan() {
        controllerHandler.stopDeviceDiscovery();
        super.stopScan();
    }

    private ThingUID getThingUID(ZWaveNode node) {
        ThingUID bridgeUID = controllerHandler.getThing().getUID();

        logger.debug("NODE {}: Scanning for things to match {}", node.getNodeId(), node.toString());

        ThingTypeUID thingTypeUID = null;
        for (ZWaveProduct product : ZWaveConfigProvider.getProductIndex()) {
            logger.debug("Scanning {}", product.toString());
            if (product.match(node) == true) {
                thingTypeUID = product.getThingTypeUID();
                break;
            }
        }
        logger.debug("Found {}", thingTypeUID);

        if (thingTypeUID == null) {
            logger.warn("NODE {}: Could note be resolved to a thingType! {}:{}:{}::{}", node.getNodeId(),
                    String.format("%04X", node.getManufacturer()), String.format("%04X", node.getDeviceType()),
                    String.format("%04X", node.getDeviceId()), node.getVersion());
            return null;
        }

        // Our ThingType UID is based on the device type
        // ThingTypeUID thingTypeUID = new ThingTypeUID(ZWaveBindingConstants.BINDING_ID, thingID);

        if (getSupportedThingTypes().contains(thingTypeUID)) {
            String thingId = "node" + node.getNodeId();
            return new ThingUID(thingTypeUID, bridgeUID, thingId);
        } else {
            logger.warn("NODE {}: Thing type {} is not supported", node.getNodeId(), thingTypeUID);

            return null;
        }
    }

    public void deviceDiscovered(int nodeId) {
        // Don't add the controller as a thing, and only add valid nodes
        if (controllerHandler.getOwnNodeId() == nodeId || nodeId == 0 || nodeId > 232) {
            return;
        }

        ThingUID bridgeUID = controllerHandler.getThing().getUID();
        ThingUID thingUID = new ThingUID(new ThingTypeUID(ZWaveBindingConstants.ZWAVE_THING), bridgeUID,
                String.format("node%d", nodeId));

        if (discoveryServiceCallback != null && discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
            // Ignore this node as we already know about it
            logger.debug("NODE {}: Device already known.", nodeId);

            return;
        }

        logger.debug("NODE {}: Device discovered", nodeId);

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(ZWaveBindingConstants.PROPERTY_NODEID, Integer.toString(nodeId));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID)
                .withProperties(properties).withLabel(String.format("Node %d", nodeId)).build();

        thingDiscovered(discoveryResult);
    }

    public void deviceAdded(ZWaveNode node) {
        // Don't add the controller as a thing, and only add valid nodes
        if (controllerHandler.getOwnNodeId() == node.getNodeId() || node.getNodeId() == 0 || node.getNodeId() > 232) {
            return;
        }

        logger.debug("NODE {}: Device discovery completed", node.getNodeId());

        ThingUID bridgeUID = controllerHandler.getThing().getUID();

        ZWaveProduct foundProduct = null;
        for (ZWaveProduct product : ZWaveConfigProvider.getProductIndex()) {
            if (product == null) {
                continue;
            }
            logger.debug("NODE {}: Checking {}", node.getNodeId(), product.getThingTypeUID());
            if (product.match(node) == true) {
                foundProduct = product;
                break;
            }
        }

        ThingUID thingUID = new ThingUID(new ThingTypeUID(ZWaveBindingConstants.ZWAVE_THING), bridgeUID,
                String.format("node%d", node.getNodeId()));
        Map<String, Object> properties = new HashMap<>(11);
        if (discoveryServiceCallback != null && discoveryServiceCallback.getExistingDiscoveryResult(thingUID) != null) {
            logger.debug("NODE {}: Device already known - properties will be updated.", node.getNodeId());

            properties = discoveryServiceCallback.getExistingDiscoveryResult(thingUID).getProperties();
        }

        // If we didn't find the product, then add the unknown thing
        String label = String.format("Z-Wave Node %d", node.getNodeId());
        if (foundProduct == null) {
            logger.warn("NODE {}: Device discovery could not resolve to a thingType! {}:{}:{}::{}", node.getNodeId(),
                    String.format("%04X", node.getManufacturer()), String.format("%04X", node.getDeviceType()),
                    String.format("%04X", node.getDeviceId()), node.getApplicationVersion());

            if (node.getManufacturer() != Integer.MAX_VALUE) {
                label += String.format(" (%04X:%04X:%04X:%s)", node.getManufacturer(), node.getDeviceType(),
                        node.getDeviceId(), node.getApplicationVersion());
            }
        } else {
            logger.debug("NODE {}: Device discovery resolved to thingType {}", node.getNodeId(),
                    foundProduct.getThingTypeUID());

            // And create the new thing
            ThingType thingType = ZWaveConfigProvider.getThingType(foundProduct.getThingTypeUID());
            label += String.format(": %s", thingType.getLabel());
        }

        // Add some device properties that might be useful for the system to know
        properties.put(ZWaveBindingConstants.PROPERTY_NODEID, Integer.toString(node.getNodeId()));

        // Don't add the device information if we don't know it yet
        // This should also prevent it from being overwritten if it was added previously
        if (node.getManufacturer() != Integer.MAX_VALUE) {
            properties.put(ZWaveBindingConstants.PROPERTY_MANUFACTURER, Integer.toString(node.getManufacturer()));
            properties.put(ZWaveBindingConstants.PROPERTY_DEVICETYPE, Integer.toString(node.getDeviceType()));
            properties.put(ZWaveBindingConstants.PROPERTY_DEVICEID, Integer.toString(node.getDeviceId()));
        }
        properties.put(ZWaveBindingConstants.PROPERTY_VERSION, node.getApplicationVersion());

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

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                .withBridge(bridgeUID).withLabel(label).build();

        thingDiscovered(discoveryResult);

        return;
    }

    public void deviceRemoved(ZWaveNode node) {
        ThingUID thingUID = getThingUID(node);

        if (thingUID != null) {
            thingRemoved(thingUID);
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
    }
}