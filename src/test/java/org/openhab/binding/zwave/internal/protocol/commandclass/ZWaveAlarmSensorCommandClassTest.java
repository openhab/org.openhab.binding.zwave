/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
