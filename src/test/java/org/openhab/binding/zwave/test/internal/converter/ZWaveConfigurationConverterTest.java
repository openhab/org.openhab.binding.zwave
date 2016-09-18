/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.converter;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveConfigurationConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveConfigurationParameter;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

public class ZWaveConfigurationConverterTest extends ZWaveCommandClassConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");

    private ZWaveThingChannel createChannel(String parameter) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("parameter", parameter);
        return new ZWaveThingChannel(null, uid, DataType.DecimalType, CommandClass.CONFIGURATION.toString(), 0, args);
    }

    private ZWaveCommandClassValueEvent createEvent(int nodeId, int id, int value, int size) {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveConfigurationCommandClass cls = new ZWaveConfigurationCommandClass(node, controller, endpoint);

        ZWaveConfigurationParameter parameter = new ZWaveConfigurationParameter(id, value, size);
        return cls.new ZWaveConfigurationParameterEvent(nodeId, parameter);
    }

    @Test
    public void handleEvent() {
        ZWaveConfigurationConverter converter = new ZWaveConfigurationConverter(null);
        ZWaveThingChannel channel = createChannel("2");

        ZWaveCommandClassValueEvent event = createEvent(1, 2, 3, 4);

        State state = converter.handleEvent(channel, event);

        assertEquals(state.getClass(), DecimalType.class);
        assertEquals(((DecimalType) state).toBigDecimal(), new BigDecimal("3"));
    }

    @Test
    public void executeRefresh() {
        byte[] expectedResponse = { 1, 10, 0, 19, 0, 3, 112, 5, 2, 0, 0, -110 };

        ZWaveConfigurationConverter converter = new ZWaveConfigurationConverter(null);
        ZWaveThingChannel channel = createChannel("2");
        ZWaveNode node = CreateMockedNode(1, null);

        List<SerialMessage> msgs = converter.executeRefresh(channel, node);

        assertEquals(1, msgs.size());
        msgs.get(0).setCallbackId(0);
        assertTrue(Arrays.equals(msgs.get(0).getMessageBuffer(), expectedResponse));
    }

    @Test
    public void receiveCommand_Decimal() {
        byte[] expectedResponse0 = { 1, 15, 0, 19, 0, 8, 112, 4, 2, 4, 0, 0, 0, 44, 0, 0, -75 };
        byte[] expectedResponse1 = { 1, 10, 0, 19, 0, 3, 112, 5, 2, 0, 0, -110 };

        ZWaveConfigurationConverter converter = new ZWaveConfigurationConverter(null);
        ZWaveThingChannel channel = createChannel("2");
        ZWaveNode node = CreateMockedNode(1, null);

        ZWaveConfigurationCommandClass configCommandClass = (ZWaveConfigurationCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.CONFIGURATION, channel.getEndpoint());

        SerialMessage report = new SerialMessage();
        report.setMessagePayload(new byte[] { 6, 2, 4, 0, 0, 0, 0 });
        try {
            configCommandClass.handleApplicationCommandRequest(report, 0, 0);
        } catch (ZWaveSerialMessageException e) {
            e.printStackTrace();
        }

        DecimalType command = new DecimalType(44);

        List<SerialMessage> msgs = converter.receiveCommand(channel, node, command);

        assertEquals(2, msgs.size());
        msgs.get(0).setCallbackId(0);
        assertTrue(Arrays.equals(msgs.get(0).getMessageBuffer(), expectedResponse0));
        msgs.get(1).setCallbackId(0);
        assertTrue(Arrays.equals(msgs.get(1).getMessageBuffer(), expectedResponse1));
    }
}
