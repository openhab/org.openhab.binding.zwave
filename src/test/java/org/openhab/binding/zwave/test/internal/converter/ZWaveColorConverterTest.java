/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.converter;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveColorConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveColorConverterTest extends ZWaveCommandClassConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");
    final ChannelTypeUID typeUid = new ChannelTypeUID("zwave:channel");

    private ZWaveCommandClassValueEvent createEvent(Map<ZWaveColorType, Integer> colorMap) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveColorCommandClass cls = new ZWaveColorCommandClass(node, controller, endpoint);

        return cls.new ZWaveColorValueEvent(1, 0, colorMap);
    }

    @Test
    public void Event_ColorSupported_None() {
        // This simulates the initial color_supported message which returns colors with null
        ZWaveColorConverter converter = new ZWaveColorConverter(null);
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUid, uid, DataType.HSBType,
                CommandClass.COMMAND_CLASS_SWITCH_COLOR.toString(), 0, new HashMap<String, String>());

        final Map<ZWaveColorType, Integer> colorMap = new HashMap<ZWaveColorType, Integer>();
        colorMap.put(ZWaveColorType.RED, null);
        colorMap.put(ZWaveColorType.GREEN, null);
        colorMap.put(ZWaveColorType.BLUE, null);

        ZWaveColorValueEvent event = (ZWaveColorValueEvent) createEvent(colorMap);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), HSBType.class);
        assertEquals((state), HSBType.fromRGB(0, 0, 0));
    }
}
