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
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test cases for {@link ZWaveAlarmSensorCommandClass}.
 *
 * @author Chris Jackson - Initial version
 */
public class ZWaveAlarmSensorCommandClassTest extends ZWaveCommandClassTest {

    @Test
    public void getSupportedMessage() {
        ZWaveAlarmSensorCommandClass cls = (ZWaveAlarmSensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_ALARM);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -100, 3 };
        cls.setVersion(1);
        msg = cls.getSupportedMessage();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }

    @Test
    public void getMessage() {
        ZWaveAlarmSensorCommandClass cls = (ZWaveAlarmSensorCommandClass) getCommandClass(
                CommandClass.COMMAND_CLASS_SENSOR_ALARM);
        ZWaveCommandClassTransactionPayload msg;

        byte[] expectedResponseV1 = { -100, 1, 6 };
        cls.setVersion(1);
        msg = cls.getMessage(AlarmType.ACCESS_CONTROL);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponseV1));
    }
}
