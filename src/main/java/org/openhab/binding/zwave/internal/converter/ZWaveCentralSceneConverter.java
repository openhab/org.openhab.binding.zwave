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
package org.openhab.binding.zwave.internal.converter;

import java.math.BigDecimal;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

/**
 * ZWaveSceneConverter class. Converters between binding items and the Z-Wave API for scene controllers.
 *
 * @author Chris Jackson
 */
public class ZWaveCentralSceneConverter extends ZWaveCommandClassConverter {
    /**
     * Constructor. Creates a new instance of the {@link ZWaveConverterBase} class.
     *
     */
    public ZWaveCentralSceneConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        return new DecimalType((BigDecimal) event.getValue());
    }
}
