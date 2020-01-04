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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveColorCommandClass.ZWaveColorValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

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

    @Test
    public void convert() {
        ZWaveColorConverter converter = new ZWaveColorConverter(null);
        Map<String, String> args = new HashMap<>();
        args.put("colorMode", "RGB");
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUid, uid, DataType.HSBType,
                CommandClass.COMMAND_CLASS_SWITCH_COLOR.toString(), 0, args);

        ZWaveNode node = CreateMockedNode(1);
        List<ZWaveCommandClassTransactionPayload> transactions = converter.receiveCommand(channel, node,
                new HSBType("0,100,100"));
        assertEquals(1, transactions.size());
        ZWaveCommandClassTransactionPayload transaction = transactions.get(0);

        assertTrue(Arrays.equals(new byte[] { 51, 5, 3, 2, -1, 3, 0, 4, 0 }, transaction.getPayloadBuffer()));
    }
}
