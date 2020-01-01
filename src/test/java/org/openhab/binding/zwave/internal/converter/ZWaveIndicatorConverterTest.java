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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveIndicatorCommandClass.IndicatorProperty;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveIndicatorCommandClass.IndicatorType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveIndicatorCommandClass.ZWaveIndicator;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveIndicatorConverterTest extends ZWaveCommandClassConverterTest {
    private ZWaveThingChannel createChannel(String channelType, DataType dataType, String type) {
        ChannelUID uid = new ChannelUID("zwave:node:bridge:" + channelType);
        ChannelTypeUID typeUid = new ChannelTypeUID("zwave:" + channelType);

        Map<String, String> args = new HashMap<String, String>();
        if (type != null) {
            args.put("type", type);
        }

        return new ZWaveThingChannel(null, typeUid, uid, dataType, CommandClass.COMMAND_CLASS_INDICATOR.toString(), 0,
                args);
    }

    private ZWaveCommandClassValueEvent createEvent(IndicatorType type, IndicatorProperty property, Integer value) {
        List<ZWaveIndicator> indicators = new ArrayList<ZWaveIndicator>();
        ZWaveIndicator indicator = new ZWaveIndicator();
        indicator.type = type;
        indicator.property = property;
        indicator.value = value;
        indicators.add(indicator);

        return new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_INDICATOR, indicators);
    }

    @Test
    public void indicatorEventMultiLevel() {
        ZWaveIndicatorConverter converter = new ZWaveIndicatorConverter(null);
        ZWaveThingChannel channel = createChannel("indicator_level", DataType.DecimalType,
                IndicatorType.BUTTON1_INDICATION.toString());

        ZWaveCommandClassValueEvent event = createEvent(IndicatorType.BUTTON1_INDICATION,
                IndicatorProperty.ON_OFF_CYCLES, 0);
        State state = converter.handleEvent(channel, event);
        assertNull(state);

        event = createEvent(IndicatorType.BUTTON1_INDICATION, IndicatorProperty.MULTILEVEL, 42);
        state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), PercentType.class);
        assertEquals(state, new DecimalType(42));
    }

    @Test
    public void indicatorSetMultiLevelV1() {
        ZWaveThingChannel channel = createChannel("indicator_level", DataType.DecimalType,
                IndicatorType.BUTTON1_INDICATION.toString());

        Map<String, String> options = new HashMap<String, String>();
        ZWaveNode node = CreateMockedNode(1, options);

        ZWaveIndicatorConverter converter = new ZWaveIndicatorConverter(null);
        List<ZWaveCommandClassTransactionPayload> transactions = converter.receiveCommand(channel, node,
                new DecimalType(42));

        assertEquals(1, transactions.size());
        ZWaveCommandClassTransactionPayload transaction = transactions.get(0);

        assertTrue(Arrays.equals(new byte[] { -121, 1, 42 }, transaction.getPayloadBuffer()));
    }

    @Test
    public void indicatorSetMultiLevelV2() {
        ZWaveThingChannel channel = createChannel("indicator_level", DataType.DecimalType,
                IndicatorType.BUTTON1_INDICATION.toString());

        Map<String, String> options = new HashMap<String, String>();
        ZWaveNode node = CreateMockedNode(2, options);

        ZWaveIndicatorConverter converter = new ZWaveIndicatorConverter(null);
        List<ZWaveCommandClassTransactionPayload> transactions = converter.receiveCommand(channel, node,
                new DecimalType(42));

        assertEquals(1, transactions.size());
        ZWaveCommandClassTransactionPayload transaction = transactions.get(0);

        byte[] x = transaction.getPayloadBuffer();
        assertTrue(Arrays.equals(new byte[] { -121, 1, 0, 1, 67, 1, 42 }, transaction.getPayloadBuffer()));
    }
}
