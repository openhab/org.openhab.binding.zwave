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
package org.openhab.binding.zwave.internal.converter;

import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

/**
 * ZWaveSceneConverter class. Converters between binding items and the Z-Wave API for scene controllers.
 *
 * @author Chris Jackson
 */
public class ZWaveSceneActivationConverter extends ZWaveCommandClassConverter {

    /**
     * Constructor. Creates a new instance of the {@link ZWaveConverterBase} class.
     *
     */
    public ZWaveSceneActivationConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        return new DecimalType((Integer) event.getValue());
    }
}
