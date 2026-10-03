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
package org.openhab.binding.zwave.internal.firmwareupdate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.core.thing.Thing;

/**
 * Unit tests for Z-Wave local firmware version extraction from filenames.
 *
 * Uses Zooz ZEN73 naming as an example device pattern:
 * - manufacturerId 027A
 * - model ZEN73
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveLocalFirmwareProviderTest {

    private String extractVersion(String fileName) throws Exception {
        Method method = ZWaveLocalFirmwareProvider.class.getDeclaredMethod("extractVersion", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, fileName);
    }

    private String currentFirmwareVersion(Thing thing) throws Exception {
        Method method = ZWaveLocalFirmwareProvider.class.getDeclaredMethod("getCurrentFirmwareVersion", Thing.class);
        method.setAccessible(true);
        return (String) method.invoke(null, thing);
    }

    private String padVersionToMatch(String version, String currentVersion) throws Exception {
        Method method = ZWaveLocalFirmwareProvider.class.getDeclaredMethod("padVersionToMatch", String.class,
                String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, version, currentVersion);
    }

    private boolean isVersionPatternMatchingEnabled() throws Exception {
        Field field = ZWaveLocalFirmwareProvider.class.getDeclaredField("VERSION_PATTERN_MATCHING_ENABLED");
        field.setAccessible(true);
        return field.getBoolean(null);
    }

    private boolean isVersionTestModeEnabled() throws Exception {
        Field field = ZWaveLocalFirmwareProvider.class.getDeclaredField("VERSION_TEST_MODE_ENABLED");
        field.setAccessible(true);
        return field.getBoolean(null);
    }

    @Test
    public void testExtractVersionForZoozZen73StyleFilename() throws Exception {
        assertEquals("2.40", extractVersion("ZEN73_V02R40.gbl"));
    }

    @Test
    public void testVersionPatternMatchingIsEnabledByDefault() throws Exception {
        assertEquals(true, isVersionPatternMatchingEnabled());
    }

    @Test
    public void testVersionTestModeIsDisabledByDefault() throws Exception {
        assertEquals(false, isVersionTestModeEnabled());
    }

    @Test
    public void testExtractVersionFallsBackToBaseFilename() throws Exception {
        assertEquals("ZEN73_firmware_candidate", extractVersion("ZEN73_firmware_candidate.gbl"));
    }

    @Test
    public void testLocalFirmwareVersionUsesFullCurrentVersionForPadding() throws Exception {
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getProperties()).thenReturn(
                Map.of(Thing.PROPERTY_FIRMWARE_VERSION, "9.8.1", ZWaveBindingConstants.PROPERTY_VERSION, "9.0"));

        assertEquals("9.8.1", currentFirmwareVersion(thing));
        assertEquals("9.8.1", padVersionToMatch("9.8", currentFirmwareVersion(thing)));
    }

    @Test
    public void testLocalFirmwareVersionFallsBackToDiscoveryVersion() throws Exception {
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getProperties()).thenReturn(Map.of(ZWaveBindingConstants.PROPERTY_VERSION, "9.0"));

        assertEquals("9.0", currentFirmwareVersion(thing));
    }
}
