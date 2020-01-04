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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.converter.ZWaveConfigurationConverter;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveConfigurationParameter;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveConfigurationCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveConfigurationConverterTest extends ZWaveCommandClassConverterTest {
    final ChannelUID uid = new ChannelUID("zwave:node:bridge:channel");
    final ChannelTypeUID typeUid = new ChannelTypeUID("zwave:channel");

    private ZWaveThingChannel createChannel(String parameter) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("parameter", parameter);
        return new ZWaveThingChannel(null, typeUid, uid, DataType.DecimalType,
                CommandClass.COMMAND_CLASS_CONFIGURATION.toString(), 0, args);
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
        byte[] expectedResponse = { 112, 5, 2 };

        ZWaveConfigurationConverter converter = new ZWaveConfigurationConverter(null);
        ZWaveThingChannel channel = createChannel("2");
        ZWaveNode node = CreateMockedNode(1, null);

        List<ZWaveCommandClassTransactionPayload> msgs = converter.executeRefresh(channel, node);

        assertEquals(1, msgs.size());
        ZWaveCommandClassTransactionPayload msg = msgs.get(0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }

    @Test
    public void receiveCommand_Decimal() {
        byte[] expectedResponse0 = { 112, 4, 2, 4, 0, 0, 0, 44 };
        byte[] expectedResponse1 = { 112, 5, 2 };

        ZWaveConfigurationConverter converter = new ZWaveConfigurationConverter(null);
        ZWaveThingChannel channel = createChannel("2");
        ZWaveNode node = CreateMockedNode(1, null);

        ZWaveConfigurationCommandClass configCommandClass = (ZWaveConfigurationCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_CONFIGURATION, channel.getEndpoint());

        // This report is required to add the parameter to the command class
        // Without it the converter will fail
        ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(
                new byte[] { (byte) CommandClass.COMMAND_CLASS_CONFIGURATION.getKey(), 6, 2, 4, 0, 0, 0, 0 });
        configCommandClass.handleConfigurationReport(payload, 0);

        DecimalType command = new DecimalType(44);

        List<ZWaveCommandClassTransactionPayload> msgs = converter.receiveCommand(channel, node, command);

        assertEquals(2, msgs.size());
        ZWaveCommandClassTransactionPayload msg = msgs.get(0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse0));
        msg = msgs.get(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse1));
    }
}
