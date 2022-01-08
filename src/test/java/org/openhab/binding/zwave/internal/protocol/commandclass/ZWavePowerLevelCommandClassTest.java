/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveBasicCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWavePowerLevelCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getValueMessage() {
        ZWavePowerLevelCommandClass cls = (ZWavePowerLevelCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_POWERLEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 115, 2 };
        cls.setVersion(1);
        msg = cls.getValueMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void setValueMessage() {
        ZWavePowerLevelCommandClass cls = (ZWavePowerLevelCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_POWERLEVEL);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { 115, 1, 1, 1 };
        cls.setVersion(1);
        msg = cls.setValueMessage(1, 1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
