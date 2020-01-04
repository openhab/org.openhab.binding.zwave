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

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveManufacturerProprietaryConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveManufacturerProprietaryFibaroFgrm222ConverterTest extends ZWaveCommandClassConverterTest {
    final ChannelUID shutterUid = new ChannelUID("zwave:node:bridge:blinds_shutter");
    final ChannelTypeUID typeUid = new ChannelTypeUID("zwave:blinds_shutter");

    @Test
    public void Event_Fgrm222_Lamella() {
        ZWaveManufacturerProprietaryConverter converter = new ZWaveManufacturerProprietaryConverter(null);

        ChannelUID uid = new ChannelUID("zwave:node:bridge:blinds_lamella");
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUid, uid, DataType.PercentType,
                CommandClass.COMMAND_CLASS_MANUFACTURER_PROPRIETARY.toString(), 0, new HashMap<String, String>());

        State state;
        ZWaveValueEvent event;

        Map<String, String> values = new HashMap<String, String>();
        values.put("LAMELLA_POSITION", "62");
        values.put("SHUTTER_POSITION", "12");
        event = new ZWaveValueEvent(1, 0, CommandClass.COMMAND_CLASS_MANUFACTURER_PROPRIETARY, values);
        state = converter.handleEvent(channel, event);
        assertEquals(state.getClass(), PercentType.class);
        assertEquals(state, new PercentType(62));
    }

    @Test
    public void Event_Fgrm222_Shutter() {
        ZWaveManufacturerProprietaryConverter converter = new ZWaveManufacturerProprietaryConverter(null);

        ChannelUID uid = new ChannelUID("zwave:node:bridge:blinds_shutter");
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUid, uid, DataType.PercentType,
                CommandClass.COMMAND_CLASS_MANUFACTURER_PROPRIETARY.toString(), 0, new HashMap<String, String>());

        State state;
        ZWaveValueEvent event;

        Map<String, String> values = new HashMap<String, String>();
        values.put("LAMELLA_POSITION", "62");
        values.put("SHUTTER_POSITION", "12");
        event = new ZWaveValueEvent(1, 0, CommandClass.COMMAND_CLASS_MANUFACTURER_PROPRIETARY, values);
        state = converter.handleEvent(channel, event);
        assertEquals(state.getClass(), PercentType.class);
        assertEquals(state, new PercentType(12));
    }

    @Test
    public void Set_Fgrm222_Lamella() {
        ZWaveManufacturerProprietaryConverter converter = new ZWaveManufacturerProprietaryConverter(null);

        ChannelUID uid = new ChannelUID("zwave:node:bridge:blinds_lamella");
        ChannelTypeUID typeUid = new ChannelTypeUID("zwave:blinds_lamella");
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUid, uid, DataType.PercentType,
                CommandClass.COMMAND_CLASS_MANUFACTURER_PROPRIETARY.toString(), 0, new HashMap<String, String>());

        ZWaveNode node = CreateMockedNode(1);

        PercentType command = new PercentType(48);
        List<ZWaveCommandClassTransactionPayload> msgs = converter.receiveCommand(channel, node, command);

        assertNotNull(msgs);
        assertEquals(1, msgs.size());

        byte[] expectedResponse = { -111, 1, 15, 38, 1, 1, 0, 48 };
        ZWaveCommandClassTransactionPayload msg = msgs.get(0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }

    @Test
    public void Set_Fgrm222_Shutter() {
        ZWaveManufacturerProprietaryConverter converter = new ZWaveManufacturerProprietaryConverter(null);

        ChannelUID uid = new ChannelUID("zwave:node:bridge:blinds_shutter");
        ChannelTypeUID typeUid = new ChannelTypeUID("zwave:blinds_shutter");
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUid, uid, DataType.PercentType,
                CommandClass.COMMAND_CLASS_MANUFACTURER_PROPRIETARY.toString(), 0, new HashMap<String, String>());

        ZWaveNode node = CreateMockedNode(1);

        PercentType command = new PercentType(48);
        List<ZWaveCommandClassTransactionPayload> msgs = converter.receiveCommand(channel, node, command);

        assertNotNull(msgs);
        assertEquals(1, msgs.size());

        byte[] expectedResponse = { -111, 1, 15, 38, 1, 2, 48, 0 };
        ZWaveCommandClassTransactionPayload msg = msgs.get(0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }
}
