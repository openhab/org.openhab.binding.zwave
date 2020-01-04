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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBarrierOperatorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveBarrierOperatorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveBarrierOperatorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWaveBarrierOperatorCommandClass cls = (ZWaveBarrierOperatorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_BARRIER_OPERATOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 102, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWaveBarrierOperatorCommandClass cls = (ZWaveBarrierOperatorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_BARRIER_OPERATOR);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 102, 1, -1 };
        cls.setVersion(1);
        msg = cls.setValueMessage(77);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

}
