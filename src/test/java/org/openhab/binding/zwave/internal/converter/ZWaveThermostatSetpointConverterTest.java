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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatSetpointCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatSetpointCommandClass.Setpoint;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveThermostatSetpointCommandClass.SetpointType;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ZWaveThermostatSetpointConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");
    final ChannelTypeUID typeUid = new ChannelTypeUID("zwave:channel");

    private ZWaveThermostatSetpointCommandClass getCommandClass(ZWaveNode node) {
        ZWaveThermostatSetpointCommandClass cls = new ZWaveThermostatSetpointCommandClass(node, null, null);

        Map<SetpointType, Setpoint> setpoints = new HashMap<>();
        setpoints.put(SetpointType.HEATING, cls.new Setpoint(SetpointType.HEATING));

        try {
            Field f = ZWaveThermostatSetpointCommandClass.class.getDeclaredField("setpoints");
            f.setAccessible(true);
            f.set(cls, setpoints);
        } catch (NoSuchFieldException x) {
            x.printStackTrace();
        } catch (IllegalArgumentException x) {
            x.printStackTrace();
        } catch (IllegalAccessException x) {
            x.printStackTrace();
        }

        return cls;
    }

    private ZWaveThingChannel createChannel() {
        Map<String, String> args = new HashMap<String, String>();
        args.put("type", "HEATING");
        return new ZWaveThingChannel(null, typeUid, uid, DataType.DecimalType,
                CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT.toString(), 0, args);
    }

    // @Test
    public void test() {
        // Setup mocks
        ZWaveThermostatSetpointConverter converter = new ZWaveThermostatSetpointConverter(null);
        ZWaveThingChannel channel = createChannel();
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveThermostatSetpointCommandClass cls = getCommandClass(node);
        when(node.resolveCommandClass(Matchers.eq(CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT), Matchers.anyInt()))
                .thenReturn(cls);

        when(node.encapsulate(Matchers.any(), Matchers.any())).thenAnswer(i -> i.getArguments()[0]);

        Mockito.when(node.encapsulate(Matchers.any(), Matchers.any()))
                .thenAnswer(new Answer<ZWaveCommandClassTransactionPayload>() {
                    @Override
                    public ZWaveCommandClassTransactionPayload answer(InvocationOnMock invocation) throws Throwable {
                        Object[] args = invocation.getArguments();
                        return (ZWaveCommandClassTransactionPayload) args[0];
                    }
                });

        // when(cls.getCommandClass()).thenReturn(CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT);

        // new QuantityType(2.3, ImperialUnits.FAHRENHEIT);
        // the actual call
        List<ZWaveCommandClassTransactionPayload> result = converter.receiveCommand(channel, node,
                new DecimalType(2.3));

        // verify the results
        assertNotNull(result);
        assertEquals(result.size(), 1);
        // verify(node, times(1)).encapsulate(Matchers.any(ZWaveCommandClassTransactionPayload.class),
        // Matchers.anyInt());
        verify(cls, times(1)).getValueMessage();
    }
}
