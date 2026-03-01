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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.core.thing.Thing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Unit tests for remote firmware lookup selection logic.
 *
 * Uses Zooz ZEN73 identifiers from thing metadata as concrete examples:
 * - manufacturerId 027A
 * - manufacturerRef 7000:A003
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveRemoteFirmwareProviderTest {

    private JsonObject invokeFindBestUpdateForDevice(JsonElement response, String manufacturerId, String productType,
            String productId, @Nullable String region) throws Exception {
        Method method = ZWaveRemoteFirmwareProvider.class.getDeclaredMethod("findBestUpdateForDevice",
                JsonElement.class, String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        ZWaveRemoteFirmwareProvider provider = new ZWaveRemoteFirmwareProvider();
        return (JsonObject) method.invoke(provider, response, manufacturerId, productType, productId, region);
    }

    private String invokeReadHexProperty(Thing thing, String key) throws Exception {
        Method method = ZWaveRemoteFirmwareProvider.class.getDeclaredMethod("readHexProperty", Thing.class,
                String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, thing, key);
    }

    private String invokeBuildLookupCacheKey(String thingUid, String manufacturerId, String productType,
            String productId, String firmwareVersion) throws Exception {
        Method method = ZWaveRemoteFirmwareProvider.class.getDeclaredMethod("buildLookupCacheKey", String.class,
                String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, thingUid, manufacturerId, productType, productId, firmwareVersion);
    }

    private String invokeResolveLocalPath(String version, String url) throws Exception {
        Method method = ZWaveRemoteFirmwareProvider.class.getDeclaredMethod("resolveLocalPath", Path.class,
                String.class, String.class);
        method.setAccessible(true);
        return ((Path) method.invoke(null, Path.of("/tmp/zwave/firmware"), url, version)).getFileName().toString();
    }

    private String invokeGetStartupPrefetchLookupFirmwareVersion(String firmwareVersion) throws Exception {
        Method method = ZWaveRemoteFirmwareProvider.class.getDeclaredMethod("getLookupFirmwareVersion", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, firmwareVersion);
    }

    private JsonObject invokeBuildRequestBody(String manufacturerId, String productType, String productId,
            String firmwareVersion, @Nullable String region) throws Exception {
        Method method = ZWaveRemoteFirmwareProvider.class.getDeclaredMethod("buildRequestBody", String.class,
                String.class, String.class, String.class, String.class);
        method.setAccessible(true);
        return (JsonObject) method.invoke(null, manufacturerId, productType, productId, firmwareVersion, region);
    }

    private String invokeResolveRfRegion(Thing thing) throws Exception {
        Method method = ZWaveRemoteFirmwareProvider.class.getDeclaredMethod("resolveRfRegion", Thing.class);
        method.setAccessible(true);
        return (String) method.invoke(new ZWaveRemoteFirmwareProvider(), thing);
    }

    @Test
    public void testFindBestUpdateForZen73ChoosesHighestStableNonDowngrade() throws Exception {
        JsonArray response = new JsonArray();

        JsonObject otherDevice = new JsonObject();
        otherDevice.addProperty("manufacturerId", "0x9999");
        otherDevice.addProperty("productType", "0x0001");
        otherDevice.addProperty("productId", "0x0002");
        JsonArray otherUpdates = new JsonArray();
        JsonObject otherUpdate = new JsonObject();
        otherUpdate.addProperty("version", "9.9");
        otherUpdate.addProperty("normalizedVersion", "9.9.0");
        otherUpdate.addProperty("channel", "stable");
        otherUpdate.addProperty("downgrade", false);
        otherUpdate.add("files", new JsonArray());
        otherUpdates.add(otherUpdate);
        otherDevice.add("updates", otherUpdates);
        response.add(otherDevice);

        JsonObject zen73Device = new JsonObject();
        zen73Device.addProperty("manufacturerId", "0x027A");
        zen73Device.addProperty("productType", "0x7000");
        zen73Device.addProperty("productId", "0xA003");
        JsonArray zen73Updates = new JsonArray();

        JsonObject zen73StableOld = new JsonObject();
        zen73StableOld.addProperty("version", "2.10");
        zen73StableOld.addProperty("normalizedVersion", "2.10.0");
        zen73StableOld.addProperty("channel", "stable");
        zen73StableOld.addProperty("downgrade", false);
        zen73StableOld.add("files", new JsonArray());
        zen73Updates.add(zen73StableOld);

        JsonObject zen73BetaNew = new JsonObject();
        zen73BetaNew.addProperty("version", "2.12");
        zen73BetaNew.addProperty("normalizedVersion", "2.12.0-beta");
        zen73BetaNew.addProperty("channel", "beta");
        zen73BetaNew.addProperty("downgrade", false);
        zen73BetaNew.add("files", new JsonArray());
        zen73Updates.add(zen73BetaNew);

        JsonObject zen73StableBest = new JsonObject();
        zen73StableBest.addProperty("version", "2.11");
        zen73StableBest.addProperty("normalizedVersion", "2.11.0");
        zen73StableBest.addProperty("channel", "stable");
        zen73StableBest.addProperty("downgrade", false);
        zen73StableBest.add("files", new JsonArray());
        zen73Updates.add(zen73StableBest);

        JsonObject zen73Downgrade = new JsonObject();
        zen73Downgrade.addProperty("version", "2.09");
        zen73Downgrade.addProperty("normalizedVersion", "2.9.0");
        zen73Downgrade.addProperty("channel", "stable");
        zen73Downgrade.addProperty("downgrade", true);
        zen73Downgrade.add("files", new JsonArray());
        zen73Updates.add(zen73Downgrade);

        zen73Device.add("updates", zen73Updates);
        response.add(zen73Device);

        JsonObject selected = invokeFindBestUpdateForDevice(response, "0x027A", "0x7000", "0xA003", null);

        assertNotNull(selected);
        assertEquals("2.11", selected.get("version").getAsString());
        assertEquals("stable", selected.get("channel").getAsString());
    }

    @Test
    public void testBuildLookupCacheKeyUsesStableFingerprint() throws Exception {
        String key1 = invokeBuildLookupCacheKey("thing:zwave:node12", "0x027a", "0x7000", "0xa003", "2.40");
        String key2 = invokeBuildLookupCacheKey("thing:zwave:node12", "0x027a", "0x7000", "0xa003", "2.40");
        String key3 = invokeBuildLookupCacheKey("thing:zwave:node12", "0x027a", "0x7000", "0xa004", "2.40");

        assertEquals(key1, key2);
        assertNotEquals(key1, key3);
    }

    @Test
    public void testFindBestUpdateFallsBackToHighestNonDowngradeUpdate() throws Exception {
        JsonArray response = new JsonArray();
        JsonObject device = new JsonObject();
        device.addProperty("manufacturerId", "0x027a");
        device.addProperty("productType", "0x7000");
        device.addProperty("productId", "0xa003");

        JsonArray updates = new JsonArray();
        JsonObject latestUpdate = new JsonObject();
        latestUpdate.addProperty("version", "2.4");
        latestUpdate.addProperty("normalizedVersion", "2.4.0");
        latestUpdate.addProperty("channel", "latest");
        latestUpdate.addProperty("downgrade", false);
        latestUpdate.add("files", new JsonArray());
        updates.add(latestUpdate);

        device.add("updates", updates);
        response.add(device);

        JsonObject selected = invokeFindBestUpdateForDevice(response, "0x027a", "0x7000", "0xa003", null);

        assertNotNull(selected);
        assertEquals("2.4", selected.get("version").getAsString());
    }

    @Test
    public void testFindBestUpdateForDeviceSupportsTopLevelUpgradesResponse() throws Exception {
        JsonObject response = new JsonObject();
        JsonArray devices = new JsonArray();
        JsonObject device = new JsonObject();
        device.addProperty("manufacturerId", "0x027a");
        device.addProperty("productType", "0x0004");
        device.addProperty("productId", "0x0369");
        devices.add(device);
        response.add("devices", devices);

        JsonArray upgrades = new JsonArray();
        JsonObject latestUpgrade = new JsonObject();
        latestUpgrade.addProperty("version", "1.30.0");
        latestUpgrade.addProperty("url", "https://example.com/firmware/ZSE50_V01R30_US.gbl");
        latestUpgrade.addProperty("integrity", "sha256:abc123");
        upgrades.add(latestUpgrade);

        JsonObject olderUpgrade = new JsonObject();
        olderUpgrade.addProperty("version", "1.20.0");
        olderUpgrade.addProperty("url", "https://example.com/firmware/ZSE50_V01R20.gbl");
        olderUpgrade.addProperty("integrity", "sha256:def456");
        upgrades.add(olderUpgrade);
        response.add("upgrades", upgrades);

        JsonObject selected = invokeFindBestUpdateForDevice(response, "0x027a", "0x0004", "0x0369", null);

        assertNotNull(selected);
        assertEquals("1.30.0", selected.get("version").getAsString());
    }

    @Test
    public void testBuildRequestBodyIncludesRegionWhenKnown() throws Exception {
        JsonObject body = invokeBuildRequestBody("0x027a", "0x0004", "0x0369", "1.20.0", "europe");

        assertEquals("1.20.0",
                body.get("devices").getAsJsonArray().get(0).getAsJsonObject().get("firmwareVersion").getAsString());
        assertEquals("europe", body.get("region").getAsString());
    }

    @Test
    public void testFindBestUpdateForDeviceFiltersRegionSpecificUpdatesWhenRegionSpecified() throws Exception {
        JsonArray response = new JsonArray();
        JsonObject device = new JsonObject();
        device.addProperty("manufacturerId", "0x027a");
        device.addProperty("productType", "0x0004");
        device.addProperty("productId", "0x0369");

        JsonArray updates = new JsonArray();
        JsonObject regionSpecific = new JsonObject();
        regionSpecific.addProperty("version", "1.30.0");
        regionSpecific.addProperty("normalizedVersion", "1.30.0");
        regionSpecific.addProperty("channel", "stable");
        regionSpecific.addProperty("downgrade", false);
        regionSpecific.addProperty("region", "europe");
        regionSpecific.add("files", new JsonArray());
        updates.add(regionSpecific);

        JsonObject regionAgnostic = new JsonObject();
        regionAgnostic.addProperty("version", "1.29.0");
        regionAgnostic.addProperty("normalizedVersion", "1.29.0");
        regionAgnostic.addProperty("channel", "stable");
        regionAgnostic.addProperty("downgrade", false);
        regionAgnostic.add("files", new JsonArray());
        updates.add(regionAgnostic);

        device.add("updates", updates);
        response.add(device);

        JsonObject selected = invokeFindBestUpdateForDevice(response, "0x027a", "0x0004", "0x0369", "europe");

        assertNotNull(selected);
        assertEquals("1.30.0", selected.get("version").getAsString());
    }

    @Test
    public void testFindBestUpdateForDeviceOnlyUsesAgnosticUpdatesWhenNoRegionRequested() throws Exception {
        JsonArray response = new JsonArray();
        JsonObject device = new JsonObject();
        device.addProperty("manufacturerId", "0x027a");
        device.addProperty("productType", "0x0004");
        device.addProperty("productId", "0x0369");

        JsonArray updates = new JsonArray();
        JsonObject regionSpecific = new JsonObject();
        regionSpecific.addProperty("version", "1.30.0");
        regionSpecific.addProperty("normalizedVersion", "1.30.0");
        regionSpecific.addProperty("channel", "stable");
        regionSpecific.addProperty("downgrade", false);
        regionSpecific.addProperty("region", "europe");
        regionSpecific.add("files", new JsonArray());
        updates.add(regionSpecific);

        JsonObject regionAgnostic = new JsonObject();
        regionAgnostic.addProperty("version", "1.29.0");
        regionAgnostic.addProperty("normalizedVersion", "1.29.0");
        regionAgnostic.addProperty("channel", "stable");
        regionAgnostic.addProperty("downgrade", false);
        regionAgnostic.add("files", new JsonArray());
        updates.add(regionAgnostic);

        device.add("updates", updates);
        response.add(device);

        JsonObject selected = invokeFindBestUpdateForDevice(response, "0x027a", "0x0004", "0x0369", null);

        assertNotNull(selected);
        assertEquals("1.29.0", selected.get("version").getAsString());
    }

    @Test
    public void testStartupPrefetchLookupFirmwareVersionDecrementsMinorVersionForProbe() throws Exception {
        assertEquals("2.39", invokeGetStartupPrefetchLookupFirmwareVersion("2.40"));
        assertEquals("1.19", invokeGetStartupPrefetchLookupFirmwareVersion("1.20"));
        assertEquals("1.19.0", invokeGetStartupPrefetchLookupFirmwareVersion("1.20.0"));
    }

    @Test
    public void testResolveRfRegionFallsBackToDefaultLocale() throws Exception {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            Thing thing = Mockito.mock(Thing.class);
            Mockito.when(thing.getBridgeUID()).thenReturn(null);
            assertEquals("usa", invokeResolveRfRegion(thing));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void testResolveLocalPathUsesRemoteFilename() throws Exception {
        String fileName = invokeResolveLocalPath("2.40", "https://example.com/firmware/ZEN73_V02R40.gbl");
        assertEquals("ZEN73_V02R40.gbl", fileName);
    }

    @Test
    public void testReadHexPropertyFormatsZen73DecimalThingProperties() throws Exception {
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getProperties()).thenReturn(Map.of(ZWaveBindingConstants.PROPERTY_MANUFACTURER, "634",
                ZWaveBindingConstants.PROPERTY_DEVICETYPE, "28672", ZWaveBindingConstants.PROPERTY_DEVICEID, "40963"));

        assertEquals("0x027a", invokeReadHexProperty(thing, ZWaveBindingConstants.PROPERTY_MANUFACTURER));
        assertEquals("0x7000", invokeReadHexProperty(thing, ZWaveBindingConstants.PROPERTY_DEVICETYPE));
        assertEquals("0xa003", invokeReadHexProperty(thing, ZWaveBindingConstants.PROPERTY_DEVICEID));
    }

    @Test
    public void testReadHexPropertyNormalizesExistingHexThingProperties() throws Exception {
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getProperties())
                .thenReturn(Map.of(ZWaveBindingConstants.PROPERTY_MANUFACTURER, "0x027A",
                        ZWaveBindingConstants.PROPERTY_DEVICETYPE, "0x7000", ZWaveBindingConstants.PROPERTY_DEVICEID,
                        "0xA003"));

        assertEquals("0x027a", invokeReadHexProperty(thing, ZWaveBindingConstants.PROPERTY_MANUFACTURER));
        assertEquals("0x7000", invokeReadHexProperty(thing, ZWaveBindingConstants.PROPERTY_DEVICETYPE));
        assertEquals("0xa003", invokeReadHexProperty(thing, ZWaveBindingConstants.PROPERTY_DEVICEID));
    }
}
