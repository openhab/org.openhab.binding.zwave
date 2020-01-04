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
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Matchers;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationGroupInfoCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveAssociationGroupInfoCommandClass}.
 *
 * @author Jorg de Jong - Initial version
 * @author Chris Jackson
 */
public class ZWaveAssociationGroupInfoCommandClassTest extends ZWaveCommandClassTest {

    /**
     * Test the processing of the supported messages types.
     */
    @Test
    public void applicationCommands() {
        byte[] groupName = { 0x01, 0x13, 0x00, 0x04, 0x00, 0x0A, 0x0D, 0x59, 0x02, 0x01, 0x09, 0x4C, 0x69, 0x66, 0x65,
                0x6C, 0x69, 0x6E, 0x65, 0x00, (byte) 0x94 };

        byte[] groupProfile = { 0x01, 0x10, 0x00, 0x04, 0x00, 0x0A, 0x0A, 0x59, 0x04, 0x01, 0x01, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, (byte) 0xB7 };

        byte[] groupCommands = { 0x01, 0x16, 0x00, 0x04, 0x00, 0x0A, 0x10, 0x59, 0x06, 0x01, 0x0C, 0x26, 0x03, 0x5A,
                0x01, 0x2B, 0x01, 0x31, 0x05, 0x32, 0x02, 0x71, 0x05, (byte) 0x81 };

        ZWaveAssociationGroup group = new ZWaveAssociationGroup(1);

        // setup mocks
        ZWaveNode node = mock(ZWaveNode.class);
        when(node.getAssociationGroup(1)).thenReturn(group);

        ZWaveCommandClass reset = ZWaveCommandClass
                .getInstance(CommandClass.COMMAND_CLASS_DEVICE_RESET_LOCALLY.getKey(), node, null);
        when(node.getCommandClass(Matchers.eq(CommandClass.COMMAND_CLASS_DEVICE_RESET_LOCALLY))).thenReturn(reset);

        // Our test subject
        ZWaveAssociationGroupInfoCommandClass cls = (ZWaveAssociationGroupInfoCommandClass) ZWaveCommandClass
                .getInstance(CommandClass.COMMAND_CLASS_ASSOCIATION_GRP_INFO.getKey(), node, null);

        // Let our CC process the messages
        processCommandClassMessages(cls, Arrays.asList(new SerialMessage(groupName), new SerialMessage(groupProfile),
                new SerialMessage(groupCommands)));

        // See if we got the expected results
        assertEquals("Lifeline", group.getName());
        assertEquals(Integer.valueOf(1), group.getProfile2());
        assertEquals(false, group.getCommandClasses().isEmpty());
        assertEquals(false,
                group.getCommandClasses().contains(CommandClass.COMMAND_CLASS_DEVICE_RESET_LOCALLY.getKey()));
        assertEquals(true, cls.getAutoSubscribeGroups().contains(1));
    }

    protected void processCommandClassMessages(ZWaveCommandClass cls, List<SerialMessage> msgs) {
        assertNotNull(cls);

        for (SerialMessage msg : msgs) {

            // Check the packet is not corrupted and is a command class request
            assertEquals(true, msg.isValid);
            assertEquals(SerialMessageType.Request, msg.getMessageType());
            assertEquals(SerialMessageClass.ApplicationCommandHandler, msg.getMessageClass());

            try {
                // ensure our message is for the correct command class
                assertEquals(cls.getCommandClass().getKey(), msg.getMessagePayloadByte(3));

                cls.handleApplicationCommandRequest(new ZWaveCommandClassPayload(msg), 0);
            } catch (ZWaveSerialMessageException e) {
                fail("Out of bounds exception processing data");
            }
        }
    }

    @Test
    public void getGroupNameMessage() {
        ZWaveAssociationGroupInfoCommandClass cls = (ZWaveAssociationGroupInfoCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION_GRP_INFO);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 89, 1, 1 };
        cls.setVersion(1);
        msg = cls.getGroupNameMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getCommandListMessage() {
        ZWaveAssociationGroupInfoCommandClass cls = (ZWaveAssociationGroupInfoCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION_GRP_INFO);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 89, 5, 0, 1 };
        cls.setVersion(1);
        msg = cls.getCommandListMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getInfoMessage() {
        ZWaveAssociationGroupInfoCommandClass cls = (ZWaveAssociationGroupInfoCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION_GRP_INFO);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 89, 3, 0, 1 };
        cls.setVersion(1);
        msg = cls.getInfoMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void handleAssociationGroupInfoListReport() {
        byte[] packetData = { 0x01, 0x12, 0x00, 0x04, 0x00, 0x03, 0x0C, 0x59, 0x06, 0x03, 0x04, 0x26, 0x04, 0x26, 0x05,
                0x00, 0x00, 0x00, 0x00, (byte) 0xBF };

        processCommandClassMessage(packetData, 3);

        ZWaveAssociationGroup group = mockedNode.getAssociationGroup(3);
        assertNotNull(group);

        assertEquals(1, group.getCommandClasses().size());
    }
}
