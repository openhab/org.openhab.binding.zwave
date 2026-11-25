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
package org.openhab.binding.zwave.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

public class ZWaveControllerHandlerTest {
    private static final Instant NOW = Instant.parse("2026-10-02T12:00:00Z");

    @Test
    public void testStartupRemoteFirmwareLookupIsDueWhenNoTimestampExists() {
        assertTrue(ZWaveControllerHandler.isStartupRemoteFirmwareLookupDue(null, NOW));
    }

    @Test
    public void testStartupRemoteFirmwareLookupIsNotDueBefore24Hours() {
        String lastLookup = NOW.minus(24, ChronoUnit.HOURS).plusMillis(1).toString();

        assertFalse(ZWaveControllerHandler.isStartupRemoteFirmwareLookupDue(lastLookup, NOW));
    }

    @Test
    public void testStartupRemoteFirmwareLookupIsDueAt24Hours() {
        String lastLookup = NOW.minus(24, ChronoUnit.HOURS).toString();

        assertTrue(ZWaveControllerHandler.isStartupRemoteFirmwareLookupDue(lastLookup, NOW));
    }

    @Test
    public void testStartupRemoteFirmwareLookupIsNotDueForFutureTimestamp() {
        String lastLookup = NOW.plus(1, ChronoUnit.SECONDS).toString();

        assertFalse(ZWaveControllerHandler.isStartupRemoteFirmwareLookupDue(lastLookup, NOW));
    }

    @Test
    public void testStartupRemoteFirmwareLookupIsDueForMalformedTimestamp() {
        assertTrue(ZWaveControllerHandler.isStartupRemoteFirmwareLookupDue("not-an-instant", NOW));
    }
}