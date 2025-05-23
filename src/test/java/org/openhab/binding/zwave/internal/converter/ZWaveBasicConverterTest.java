/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveBasicConverterTest extends ZWaveCommandClassConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");
    final ChannelTypeUID typeUid = new ChannelTypeUID("zwave:channel");

    @Test
    public void Event_Percent() {
        ZWaveBasicConverter converter = new ZWaveBasicConverter(null);
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUid, uid, DataType.PercentType,
                CommandClass.COMMAND_CLASS_BASIC.toString(), 0, new HashMap<String, String>());

        State state;
        ZWaveCommandClassValueEvent event;

        event = new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_BASIC, 100);
        state = converter.handleEvent(channel, event);
        assertEquals(state.getClass(), PercentType.class);
        assertEquals(state, new PercentType(100));

        event = new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_BASIC, 255);
        state = converter.handleEvent(channel, event);
        assertEquals(state.getClass(), PercentType.class);
        assertEquals(state, new PercentType(100));

        event = new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_BASIC, 101);
        state = converter.handleEvent(channel, event);
        assertNull(state);
    }
}
