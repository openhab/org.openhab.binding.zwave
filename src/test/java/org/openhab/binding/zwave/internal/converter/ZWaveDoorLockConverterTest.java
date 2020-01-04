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

import java.util.HashMap;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveDoorLockCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveDoorLockConverterTest extends ZWaveCommandClassConverterTest {
    final ChannelUID uidLockDoor = new ChannelUID("zwave:node:bridge:lock_door");
    final ChannelTypeUID typeUidLockDoor = new ChannelTypeUID("zwave:lock_door");
    final ChannelUID uidSensorDoor = new ChannelUID("zwave:node:bridge:sensor_door");
    final ChannelTypeUID typeUidSensorDoor = new ChannelTypeUID("zwave:sensor_door");

    @Test
    public void Event_DoorLockState() {
        ZWaveDoorLockConverter converter = new ZWaveDoorLockConverter(null);
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUidLockDoor, uidLockDoor, DataType.OnOffType,
                CommandClass.COMMAND_CLASS_DOOR_LOCK.toString(), 0, new HashMap<String, String>());

        State state;
        ZWaveCommandClassValueEvent event;

        event = new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_DOOR_LOCK, 0,
                ZWaveDoorLockCommandClass.Type.DOOR_LOCK_STATE);
        state = converter.handleEvent(channel, event);
        assertEquals(OnOffType.class, state.getClass());
        assertEquals(state, OnOffType.OFF);

        event = new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_DOOR_LOCK, 1,
                ZWaveDoorLockCommandClass.Type.DOOR_LOCK_STATE);
        state = converter.handleEvent(channel, event);
        assertEquals(OnOffType.class, state.getClass());
        assertEquals(state, OnOffType.ON);
    }

    @Test
    public void Event_DoorLockCondition() {
        ZWaveDoorLockConverter converter = new ZWaveDoorLockConverter(null);
        ZWaveThingChannel channel = new ZWaveThingChannel(null, typeUidSensorDoor, uidSensorDoor,
                DataType.OpenClosedType, CommandClass.COMMAND_CLASS_DOOR_LOCK.toString(), 0,
                new HashMap<String, String>());

        State state;
        ZWaveCommandClassValueEvent event;

        event = new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_DOOR_LOCK, 0,
                ZWaveDoorLockCommandClass.Type.DOOR_CONDITION);
        state = converter.handleEvent(channel, event);
        assertEquals(OpenClosedType.class, state.getClass());
        assertEquals(state, OpenClosedType.OPEN);

        event = new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_DOOR_LOCK, 1,
                ZWaveDoorLockCommandClass.Type.DOOR_CONDITION);
        state = converter.handleEvent(channel, event);
        assertEquals(OpenClosedType.class, state.getClass());
        assertEquals(state, OpenClosedType.CLOSED);

        event = new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_DOOR_LOCK, 2,
                ZWaveDoorLockCommandClass.Type.DOOR_CONDITION);
        state = converter.handleEvent(channel, event);
        assertEquals(OpenClosedType.class, state.getClass());
        assertEquals(state, OpenClosedType.OPEN);

        event = new ZWaveCommandClassValueEvent(1, 0, CommandClass.COMMAND_CLASS_DOOR_LOCK, 3,
                ZWaveDoorLockCommandClass.Type.DOOR_CONDITION);
        state = converter.handleEvent(channel, event);
        assertEquals(OpenClosedType.class, state.getClass());
        assertEquals(state, OpenClosedType.CLOSED);
    }
}
