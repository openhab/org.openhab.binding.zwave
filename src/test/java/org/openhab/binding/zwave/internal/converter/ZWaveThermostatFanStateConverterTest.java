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
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveThermostatFanStateConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatFanStateCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveThermostatFanStateConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");
    final ChannelTypeUID typeUid = new ChannelTypeUID("zwave:channel");

    private ZWaveThingChannel createChannel() {
        Map<String, String> args = new HashMap<String, String>();
        return new ZWaveThingChannel(null, typeUid, uid, DataType.DecimalType,
                CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_STATE.toString(), 0, args);
    }

    @Test
    public void FanState() {
        ZWaveThermostatFanStateConverter converter = new ZWaveThermostatFanStateConverter(null);
        ZWaveThingChannel channel = createChannel();
        Integer value = new Integer(3);

        ZWaveCommandClassValueEvent event = new ZWaveCommandClassValueEvent(0, 0,
                CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_STATE, value);
        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(((DecimalType) state).intValue(), value.intValue());
    }

    @Test
    public void refresh() {
        // Setup mocks
        ZWaveThermostatFanStateConverter converter = new ZWaveThermostatFanStateConverter(null);
        ZWaveThingChannel channel = createChannel();
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveThermostatFanStateCommandClass cls = mock(ZWaveThermostatFanStateCommandClass.class);
        when(node.resolveCommandClass(Matchers.eq(CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_STATE), Matchers.anyInt()))
                .thenReturn(cls);
        when(cls.getCommandClass()).thenReturn(CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_STATE);

        // the actual call
        List<ZWaveCommandClassTransactionPayload> result = converter.executeRefresh(channel, node);

        // verify the results
        assertNotNull(result);
        assertEquals(result.size(), 1);
        // verify(node, times(1)).encapsulate(Matchers.any(ZWaveCommandClassTransactionPayload.class),
        // Matchers.anyInt());
        verify(cls, times(1)).getValueMessage();
    }
}
