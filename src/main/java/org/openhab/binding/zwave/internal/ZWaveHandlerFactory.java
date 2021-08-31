/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.zwave.internal;

import static org.openhab.binding.zwave.ZWaveBindingConstants.CONTROLLER_SERIAL;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.discovery.ZWaveDiscoveryService;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveSerialHandler;
import org.openhab.binding.zwave.handler.ZWaveThingHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZWaveHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Chris Jackson - Initial contribution
 */
@Component(service = ThingHandlerFactory.class)
public class ZWaveHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(BaseThingHandlerFactory.class);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (thingTypeUID.equals(ZWaveBindingConstants.ZWAVE_THING_UID)) {
            return true;
        }

        return ZWaveBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        logger.debug("Creating thing {}", thing.getUID());

        ZWaveControllerHandler controller = null;

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Handle controllers here
        if (thingTypeUID.equals(CONTROLLER_SERIAL)) {
            controller = new ZWaveSerialHandler((Bridge) thing, serialPortManager);
        }

        if (controller != null) {
            ZWaveDiscoveryService discoveryService = new ZWaveDiscoveryService(controller, 60);
            discoveryService.activate();

            discoveryServiceRegs.put(controller.getThing().getUID(), bundleContext.registerService(
                    DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));

            return controller;
        }

        // Everything else gets handled in a single handler
        return new ZWaveThingHandler(thing);
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof ZWaveControllerHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                ZWaveDiscoveryService service = (ZWaveDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                if (service != null) {
                    service.deactivate();
                }
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

}
