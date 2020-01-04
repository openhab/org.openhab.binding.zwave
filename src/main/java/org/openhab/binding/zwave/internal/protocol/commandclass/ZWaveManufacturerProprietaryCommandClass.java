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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * ZWave Manufacturer Proprietary command class.
 * <p>
 * This class delegates to separate implementation classes for each manufacturer proprietary implementation.
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_MANUFACTURER_PROPRIETARY")
public class ZWaveManufacturerProprietaryCommandClass extends ZWaveCommandClass {
    private ManufacturerProprietaryClass classType;

    /**
     * Creates a new instance of the ZWaveManufacturerProprietaryCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveManufacturerProprietaryCommandClass(ZWaveNode node, ZWaveController controller,
            ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_MANUFACTURER_PROPRIETARY;
    }

    @ZWaveResponseHandler(id = 0, name = "DEFAULT_HANDLER")
    public void handleManufacturerSpecificReport(ZWaveCommandClassPayload payload, int endpoint) {
        // If we don't have an implementation specified then just return
        if (classType == null) {
            return;
        }
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        try {
            classType = ManufacturerProprietaryClass.valueOf(options.get("className"));
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Enumeration of manufacturer proprietary command class names.
     * Format is manufacturer, device, version.
     *
     */
    private enum ManufacturerProprietaryClass {
        FIBARO_FGRM222_V1
    }

}