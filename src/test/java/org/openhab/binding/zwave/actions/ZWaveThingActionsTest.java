/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwave.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveThingHandler;
import org.openhab.core.thing.Thing;

/**
 * Tests for {@link ZWaveThingActions}.
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveThingActionsTest {

    private static class TestThingHandler extends ZWaveThingHandler {
        private final String response;

        private TestThingHandler(Thing thing, String response) {
            super(thing);
            this.response = response;
        }

        @Override
        protected void validateConfigurationParameters(java.util.Map<String, Object> configurationParameters) {
        }

        @Override
        public String checkRemoteFirmware() {
            return response;
        }
    }

    @Test
    public void testCheckRemoteFirmwareDelegatesToHandler() {
        Thing thing = Mockito.mock(Thing.class);
        ZWaveThingHandler handler = new TestThingHandler(thing, "Remote firmware lookup started");

        ZWaveThingActions actions = new ZWaveThingActions();
        actions.setThingHandler(handler);

        assertEquals("Remote firmware lookup started", actions.checkRemoteFirmware());
    }
}
