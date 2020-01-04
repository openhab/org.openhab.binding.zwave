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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveMeterConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.MeterScale;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMeterCommandClass.MeterType;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveMeterConverterTest extends ZWaveCommandClassConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");
    final ChannelTypeUID typeUid = new ChannelTypeUID("zwave:channel");

    private ZWaveThingChannel createChannel(String type) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("type", type);
        return new ZWaveThingChannel(null, typeUid, uid, DataType.DecimalType,
                CommandClass.COMMAND_CLASS_METER.toString(), 0, args);
    }

    private ZWaveCommandClassValueEvent createEvent(MeterType type, MeterScale scale, BigDecimal value) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveMeterCommandClass cls = new ZWaveMeterCommandClass(node, controller, endpoint);

        return cls.new ZWaveMeterValueEvent(1, 0, type, scale, value);
    }

    @Test
    public void Event_Electric() {
        ZWaveMeterConverter converter = new ZWaveMeterConverter(null);
        ZWaveThingChannel channel = createChannel(MeterScale.E_KWh.toString());
        BigDecimal value = new BigDecimal("3.3");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveMeterCommandClass.MeterType.ELECTRIC,
                ZWaveMeterCommandClass.MeterScale.E_KWh, value);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(((DecimalType) state).toBigDecimal(), value);
    }

    @Test
    public void Reset() {
        Map<String, String> args = new HashMap<String, String>();
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUid, uid, DataType.OnOffType,
                CommandClass.COMMAND_CLASS_METER.toString(), 0, args);
        ZWaveMeterConverter converter = new ZWaveMeterConverter(null);

        Map<String, String> options = new HashMap<String, String>();
        options.put("meterCanReset", "true");
        ZWaveNode node = CreateMockedNode(2, options);

        // Refresh won't return anything for meter channel
        List<ZWaveCommandClassTransactionPayload> msgs = converter.executeRefresh(channel, node);
        assertNull(msgs);

        ZWaveCommandClassValueEvent event = createEvent(ZWaveMeterCommandClass.MeterType.ELECTRIC,
                ZWaveMeterCommandClass.MeterScale.E_KWh, new BigDecimal("3.3"));

        // Event won't update state for meter channel
        State state = converter.handleEvent(channel, event);
        assertNull(state);

        msgs = converter.receiveCommand(channel, node, OnOffType.ON);
        assertNotNull(msgs);
        assertEquals(1, msgs.size());

        byte[] expectedResponse = { 50, 5 };
        ZWaveCommandClassTransactionPayload msg = msgs.get(0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }
}
