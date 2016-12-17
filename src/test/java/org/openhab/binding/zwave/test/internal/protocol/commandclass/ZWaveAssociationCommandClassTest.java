/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol.commandclass;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveAlarmSensorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveAssociationCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getAssociationMessage() {
        ZWaveAssociationCommandClass cls = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -123, 2, 1 };
        cls.setVersion(1);
        msg = cls.getAssociationMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getGroupingsMessage() {
        ZWaveAssociationCommandClass cls = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -123, 5 };
        cls.setVersion(1);
        msg = cls.getGroupingsMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void removeAssociationMessage() {
        ZWaveAssociationCommandClass cls = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -123, 4, 1, 1 };
        cls.setVersion(1);
        msg = cls.removeAssociationMessage(1, 1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void clearAssociationMessage() {
        ZWaveAssociationCommandClass cls = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -123, 4, 1 };
        cls.setVersion(1);
        msg = cls.clearAssociationMessage(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setAssociationMessage() {
        ZWaveAssociationCommandClass cls = (ZWaveAssociationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_ASSOCIATION);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -123, 1, 1, 1 };
        cls.setVersion(1);
        msg = cls.setAssociationMessage(1, 1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
