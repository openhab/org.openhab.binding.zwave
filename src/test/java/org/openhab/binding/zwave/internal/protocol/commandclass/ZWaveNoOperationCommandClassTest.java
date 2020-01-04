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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveNoOperationCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveNoOperationCommandClassTest}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveNoOperationCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getNoOperationMessage() {
        ZWaveNoOperationCommandClass cls = (ZWaveNoOperationCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_NO_OPERATION);

        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponse1 = { 0 };
        msg = cls.getNoOperationMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse1));
    }
}
