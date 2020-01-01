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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveAssociationEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveMultiAssociationCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveMultiAssociationCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getAssociationMessage() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -114, 2, 1 };
        cls.setVersion(1);
        msg = cls.getAssociationMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getGroupingsMessage() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -114, 5 };
        cls.setVersion(1);
        msg = cls.getGroupingsMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void removeAssociationMessage() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponse1 = { -114, 4, 1, 0, 2, 0 };
        byte[] expectedResponse2 = { -114, 4, 1, 0, 2, 3 };

        cls.setVersion(1);
        msg = cls.removeAssociationMessage(1, 2, 0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse1));

        msg = cls.removeAssociationMessage(1, 2, 3);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse2));
    }

    @Test
    public void clearAssociationMessage() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -114, 4, 1 };
        cls.setVersion(1);
        msg = cls.clearAssociationMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setAssociationMessageV2() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        // Version 2 doesn't allow endpoint 0 to be set
        byte[] expectedResponse2 = { -114, 1, 1, 2 };

        cls.setVersion(1);
        msg = cls.setAssociationMessage(1, 2, 0);

        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse2));
    }

    @Test
    public void setAssociationMessageV3() {
        ZWaveMultiAssociationCommandClass cls = (ZWaveMultiAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponse1 = { -114, 1, 1, 2 };
        byte[] expectedResponse2 = { -114, 1, 1, 0, 2, 3 };

        cls.setVersion(1);
        msg = cls.setAssociationMessage(1, 2, 0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse1));

        msg = cls.setAssociationMessage(1, 2, 3);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse2));
    }

    @Test
    public void AssociationReport() {
        byte[] packetData = { 0x01, 0x0E, 0x00, 0x04, 0x00, 0x03, 0x08, (byte) 0x8E, 0x03, 0x02, 0x10, 0x00, 0x00, 0x01,
                0x01, 0x61 };

        List<ZWaveEvent> events = processCommandClassMessage(packetData);

        assertEquals(events.size(), 1);

        ZWaveAssociationEvent event = (ZWaveAssociationEvent) events.get(0);

        assertEquals(event.getCommandClass(), CommandClass.COMMAND_CLASS_ASSOCIATION);
        // assertEquals(event.getNodeId(), 3);
        assertEquals(event.getEndpoint(), 0);
        assertEquals(event.getGroupId(), 2);
        assertEquals(event.getGroupMembers().size(), 1);
        assertEquals(event.getGroupMembers().iterator().next().getNode(), 1);
        assertEquals(event.getGroupMembers().iterator().next().getEndpoint(), Integer.valueOf(1));
    }
}
