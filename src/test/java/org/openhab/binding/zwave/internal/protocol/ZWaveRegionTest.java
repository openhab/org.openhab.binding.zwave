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
package org.openhab.binding.zwave.internal.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetRfRegionMessageClass.ZWaveRegion;

/**
 * Test for {@link ZWaveRegion}
 *
 * @author Bob Eckhoff - Initial contribution
 *
 */
@NonNullByDefault
public class ZWaveRegionTest {
    @Test
    public void mapsKnownCodes() {
        assertEquals(ZWaveRegion.EU, ZWaveRegion.fromCode(0x00));
        assertEquals(ZWaveRegion.US, ZWaveRegion.fromCode(0x01));
        assertEquals(ZWaveRegion.ANZ, ZWaveRegion.fromCode(0x02));
        assertEquals(ZWaveRegion.BR, ZWaveRegion.fromCode(0x0B));
    }

    @Test
    public void exposesLowerCaseRepositoryValues() {
        assertEquals("europe", ZWaveRegion.EU.getLowerCaseName());
        assertEquals("usa", ZWaveRegion.US.getLowerCaseName());
        assertEquals("australia/new zealand", ZWaveRegion.ANZ.getLowerCaseName());
        assertEquals("hong kong", ZWaveRegion.HK.getLowerCaseName());
    }

    @Test
    public void fallsBackToUnknownForUnsupportedCodes() {
        assertEquals(ZWaveRegion.UNKNOWN, ZWaveRegion.fromCode(0x7F));
    }

    @Test
    public void doesNotExposeRepositoryNameForUnknownRegion() {
        assertEquals(null, ZWaveRegion.UNKNOWN.getLowerCaseName());
    }

    @Test
    public void derivesKnownRegionsFromUnambiguousLocale() {
        assertEquals(ZWaveRegion.US, ZWaveRegion.fromLocale(Locale.US));
        assertEquals(ZWaveRegion.US, ZWaveRegion.fromLocale(Locale.CANADA));
        assertEquals(ZWaveRegion.US, ZWaveRegion.fromLocale(Locale.forLanguageTag("es-MX")));
        assertEquals(ZWaveRegion.ANZ, ZWaveRegion.fromLocale(Locale.forLanguageTag("en-AU")));
        assertEquals(ZWaveRegion.ANZ, ZWaveRegion.fromLocale(Locale.forLanguageTag("en-NZ")));
        assertEquals(ZWaveRegion.EU, ZWaveRegion.fromLocale(Locale.GERMANY));
        assertEquals(ZWaveRegion.EU, ZWaveRegion.fromLocale(Locale.UK));
    }

    @Test
    public void leavesAmbiguousOrUnsupportedLocalesUnknown() {
        assertEquals(ZWaveRegion.UNKNOWN, ZWaveRegion.fromLocale(Locale.ENGLISH));
        assertEquals(ZWaveRegion.UNKNOWN, ZWaveRegion.fromLocale(Locale.JAPAN));
    }
}
