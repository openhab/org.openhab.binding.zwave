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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSensorCommandClass.SensorType;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveMultiLevelSensorConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");
    final ChannelTypeUID typeUid = new ChannelTypeUID("zwave:sensor_value");

    private ZWaveControllerHandler controller;
    private ZWaveThingChannel channel;
    private ZWaveCommandClassValueEvent event;
    private ZWaveNode node;
    private DecimalType decimalType;
    private ZWaveMultiLevelSensorCommandClass commandClass;

    @Before
    public void setup() {
        controller = mock(ZWaveControllerHandler.class);
        channel = mock(ZWaveThingChannel.class);
        event = mock(ZWaveCommandClassValueEvent.class);
        node = mock(ZWaveNode.class);
        decimalType = mock(DecimalType.class);
        commandClass = mock(ZWaveMultiLevelSensorCommandClass.class);
        when(node.resolveCommandClass(CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL, 0)).thenReturn(commandClass);
    }

    private ZWaveThingChannel createChannel(String type) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("type", type);
        return new ZWaveThingChannel(null, typeUid, uid, DataType.DecimalType,
                CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL.toString(), 0, args);
    }

    private ZWaveCommandClassValueEvent createEvent(ZWaveMultiLevelSensorCommandClass.SensorType type, int scale,
            BigDecimal value) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveMultiLevelSensorCommandClass cls = new ZWaveMultiLevelSensorCommandClass(node, controller, endpoint);

        return cls.new ZWaveMultiLevelSensorValueEvent(1, 0, type, scale, value);
    }

    @Test
    public void Event_Luminance() {
        ZWaveMultiLevelSensorConverter converter = new ZWaveMultiLevelSensorConverter(null);
        ZWaveThingChannel channel = createChannel(ZWaveMultiLevelSensorCommandClass.SensorType.LUMINANCE.toString());
        BigDecimal value = new BigDecimal("103");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveMultiLevelSensorCommandClass.SensorType.LUMINANCE, 0,
                value);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), QuantityType.class);
        assertEquals(SmartHomeUnits.LUX, ((QuantityType) state).getUnit());
        assertEquals(((QuantityType) state).toBigDecimal(), value);
    }

    @Test
    public void Event_Temperature() {
        ZWaveMultiLevelSensorConverter converter = new ZWaveMultiLevelSensorConverter(null);
        ZWaveThingChannel channel = createChannel(ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE.toString());
        BigDecimal value = new BigDecimal("21.3");

        ZWaveCommandClassValueEvent event = createEvent(ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE, 0,
                value);
        State state = converter.handleEvent(channel, event);
        assertEquals(QuantityType.class, state.getClass());
        assertEquals(SIUnits.CELSIUS, ((QuantityType<Temperature>) state).getUnit());
        assertEquals(21.3, ((QuantityType<Temperature>) state).doubleValue(), 0.01);

        event = createEvent(ZWaveMultiLevelSensorCommandClass.SensorType.TEMPERATURE, 1, value);
        state = converter.handleEvent(channel, event);
        assertEquals(QuantityType.class, state.getClass());
        assertEquals(ImperialUnits.FAHRENHEIT, ((QuantityType) state).getUnit());
        assertEquals(21.3, ((QuantityType<Temperature>) state).doubleValue(), 0.01);
    }

    @Test
    public void commandTemperature() {
        ZWaveMultiLevelSensorConverter sut = new ZWaveMultiLevelSensorConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("type", "TEMPERATURE");
        configMap.put("config_scale", "0");
        BigDecimal value = BigDecimal.valueOf(23.4);
        when(channel.getArguments()).thenReturn(configMap);
        when(decimalType.toBigDecimal()).thenReturn(value);
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("zwave:sensor_report"));
        sut.receiveCommand(channel, node, decimalType);
        verify(commandClass).getReportMessage(SensorType.TEMPERATURE, 0, value);
    }
}
