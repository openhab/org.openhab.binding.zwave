/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

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
