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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass.StartStopDirection;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass.ZWaveStartStopEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveMultiLevelSwitchConverterTest {

    private ZWaveControllerHandler controller;
    private ZWaveThingChannel channel;
    private ZWaveCommandClassValueEvent event;
    private ZWaveNode node;
    private PercentType percentType;
    private ZWaveMultiLevelSwitchCommandClass commandClass;

    private ZWaveThingChannel createChannel(String channelType, DataType dataType) {
        ChannelUID uid = new ChannelUID("zwave:node:bridge:" + channelType);
        ChannelTypeUID typeUid = new ChannelTypeUID("zwave:" + channelType);

        Map<String, String> args = new HashMap<String, String>();

        return new ZWaveThingChannel(null, typeUid, uid, dataType, CommandClass.COMMAND_CLASS_MULTI_CMD.toString(), 0,
                args);
    }

    @Before
    public void setup() {
        controller = mock(ZWaveControllerHandler.class);
        channel = mock(ZWaveThingChannel.class);
        event = mock(ZWaveCommandClassValueEvent.class);
        node = mock(ZWaveNode.class);
        percentType = mock(PercentType.class);
        commandClass = mock(ZWaveMultiLevelSwitchCommandClass.class);
    }

    @Test
    public void handleEvent_PercentType0invertPercentFalse_returnPercentType0() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(0);
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new PercentType(0), state);
    }

    @Test
    public void handleEvent_PercentType99invertPercentFalse_returnPercentType100() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(99);
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new PercentType(100), state);
    }

    @Test
    public void handleEvent_PercentType99invertPercentTrue_returnPercentType0() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(99);
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new PercentType(0), state);
    }

    @Test
    public void handleEvent_PercentType0invertPercentTrue_returnPercentType100() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(0);
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new PercentType(100), state);
    }

    @Test
    public void handleEvent_PercentType1invertPercentTrue_returnPercentType99() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(1);
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new PercentType(99), state);
    }

    @Test
    public void handleEvent_OnOffType0invertFalse_returnOnOffTypeOff() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_control", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(0);
        when(channel.getDataType()).thenReturn(DataType.OnOffType);
        State state = sut.handleEvent(channel, event);
        assertEquals(OnOffType.OFF, state);
    }

    @Test
    public void handleEvent_OnOffType1invertFalse_returnOnOffTypeOn() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_control", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(1);
        when(channel.getDataType()).thenReturn(DataType.OnOffType);
        State state = sut.handleEvent(channel, event);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void handleEvent_OnOffType0invertTrue_returnOnOffTypeOn() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_control", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(0);
        when(channel.getDataType()).thenReturn(DataType.OnOffType);
        State state = sut.handleEvent(channel, event);
        assertEquals(OnOffType.ON, state);
    }

    @Test
    public void handleEvent_OnOffType1invertTrue_returnOnOffTypeOff() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_control", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(event.getValue()).thenReturn(1);
        when(channel.getDataType()).thenReturn(DataType.OnOffType);
        State state = sut.handleEvent(channel, event);
        assertEquals(OnOffType.OFF, state);
    }

    @Test
    public void receiveCommand_PercentType0invertPercentFalse_setValue0() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(0);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(0);
    }

    @Test
    public void receiveCommand_PercentType0invertPercentTrue_setValue99() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(0);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(99);
    }

    @Test
    public void receiveCommand_PercentType80invertPercentFalse_setValue80() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(80);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(80);
    }

    @Test
    public void receiveCommand_PercentType80invertPercentTrue_setValue20() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "true");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(80);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(20);
    }

    @Test
    public void receiveCommand_PercentType100invertPercentFalse_setValue99() throws Exception {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        setupReceiveCommand();
        Map<String, String> configMap = new HashMap<>();
        configMap.put("config_invert_percent", "false");
        when(channel.getArguments()).thenReturn(configMap);
        when(percentType.intValue()).thenReturn(100);
        sut.receiveCommand(channel, node, percentType);
        verify(commandClass).setValueMessage(99);
    }

    private void setupReceiveCommand() {
        when(channel.getDataType()).thenReturn(DataType.PercentType);
        when(channel.getEndpoint()).thenReturn(1);
        when(node.resolveCommandClass(CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL, channel.getEndpoint()))
                .thenReturn(commandClass);
        when(node.encapsulate(any(ZWaveCommandClassTransactionPayload.class), anyInt()))
                .thenReturn(new ZWaveCommandClassTransactionPayload(0, null, null, null, 0));
    }

    @Test
    public void handleEvent_Decrease() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        event = new ZWaveStartStopEvent(0, 0, null, StartStopDirection.DECREASE);

        channel = createChannel("switch_startstop", DataType.StringType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new StringType("{\"direction\":\"DECREASE\"}"), state);
    }

    @Test
    public void handleEvent_Increase() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        event = new ZWaveStartStopEvent(0, 0, null, StartStopDirection.INCREASE);

        channel = createChannel("switch_startstop", DataType.StringType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new StringType("{\"direction\":\"INCREASE\"}"), state);
    }

    @Test
    public void handleEvent_Stop() {
        ZWaveMultiLevelSwitchConverter sut = new ZWaveMultiLevelSwitchConverter(controller);
        event = new ZWaveStartStopEvent(0, 0, null, StartStopDirection.STOP);

        channel = createChannel("switch_startstop", DataType.StringType);
        State state = sut.handleEvent(channel, event);
        assertEquals(new StringType("{\"direction\":\"STOP\"}"), state);
    }

}
