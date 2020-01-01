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
package org.openhab.binding.zwave.internal.protocol;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveEndpointTest {
    @Test
    public void testSupportedCommandClasses() {
        ZWaveController controller = new ZWaveController(null);
        ZWaveNode node = new ZWaveNode(0, 0, controller);
        ZWaveEndpoint endpoint = new ZWaveEndpoint(0);

        ZWaveCommandClass commandClass = new ZWaveAlarmCommandClass(node, controller, endpoint);
        endpoint.addCommandClass(commandClass);

        assertTrue(endpoint.supportsCommandClass(CommandClass.COMMAND_CLASS_ALARM));
        assertFalse(endpoint.supportsCommandClass(CommandClass.COMMAND_CLASS_ASSOCIATION));
    }
}
