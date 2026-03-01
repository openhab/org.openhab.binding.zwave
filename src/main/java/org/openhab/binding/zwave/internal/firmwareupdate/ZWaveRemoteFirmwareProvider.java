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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.internal.protocol.serialmessage.GetRfRegionMessageClass.ZWaveRegion;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.firmware.Firmware;
import org.openhab.core.thing.binding.firmware.FirmwareBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Queries the Z-Wave JS firmware update service (https://firmware.zwave-js.io)
 * for the latest stable firmware for a device, downloads it to the node's local
 * firmware folder, and exposes it to the openHAB firmware update UI.
 *
 * Uses API v4 with a {@code User-Agent} header derived from the bundle version
 * (e.g. {@code openhab/5.2.0}). No API key is required.
 *
 * Downloaded files are written to
 * {@code userdata/zwave/firmware/node-{nodeId}/} so that
 * {@link ZWaveLocalFirmwareProvider} will also serve them on subsequent calls.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveRemoteFirmwareProvider {

    private final Logger logger = LoggerFactory.getLogger(ZWaveRemoteFirmwareProvider.class);

    private static final String API_URL = "https://firmware.zwave-js.io/api/v4/updates";

    // Startup prefetch probe: query one lower firmware version so a file can be
    // staged for UI visibility when the current version has no update payload.
    private static final boolean STARTUP_PREFETCH_ALLOW_LOWER_VERSION_PROBE = true;

    private static final long STARTUP_LOOKUP_INITIAL_DELAY_SECONDS = 90;
    private static final long STARTUP_LOOKUP_INTERVAL_SECONDS = 30;
    private static final int STARTUP_LOOKUP_NOT_READY_RETRIES = 6;

    private static final ScheduledExecutorService STARTUP_LOOKUP_EXECUTOR = Executors
            .newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "zwave-remote-fw-startup");
                thread.setDaemon(true);
                return thread;
            });
    private static final ConcurrentLinkedQueue<StartupLookupRequest> STARTUP_LOOKUP_QUEUE = new ConcurrentLinkedQueue<>();
    private static final Set<ThingUID> STARTUP_LOOKUP_PENDING = ConcurrentHashMap.newKeySet();
    private static final AtomicBoolean STARTUP_LOOKUP_WORKER_SCHEDULED = new AtomicBoolean(false);

    /**
     * User-Agent built from the OSGi bundle version, e.g. "openhab/5.2.0".
     * Required by the Z-Wave JS firmware update service.
     */
    private static final String USER_AGENT;

    private static final Gson GSON = new Gson();

    static {
        Bundle bundle = FrameworkUtil.getBundle(ZWaveRemoteFirmwareProvider.class);
        if (bundle != null) {
            Version v = bundle.getVersion();
            USER_AGENT = "openhab/" + v.getMajor() + "." + v.getMinor() + "." + v.getMicro();
        } else {
            // Unit tests run outside OSGi; use a stable descriptive fallback.
            USER_AGENT = "openHab/5.2.0";
        }
    }

    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    /** ThingRegistry is used to resolve the RF region from the bridge thing. */
    private final @Nullable ThingRegistry thingRegistry;

    public ZWaveRemoteFirmwareProvider(@Nullable ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    public ZWaveRemoteFirmwareProvider() {
        this(null);
    }

    /**
     * Queues a one-time remote firmware lookup for startup prefetch so firmware
     * files become available in the UI without a manual action.
     */
    public void scheduleStartupRefresh(Thing thing) {
        if (!"zwave".equals(thing.getThingTypeUID().getBindingId())) {
            return;
        }

        ThingUID thingUID = thing.getUID();
        if (!STARTUP_LOOKUP_PENDING.add(thingUID)) {
            return;
        }

        STARTUP_LOOKUP_QUEUE.add(new StartupLookupRequest(this, thingUID, thing, STARTUP_LOOKUP_NOT_READY_RETRIES));
        scheduleStartupLookupWorker(STARTUP_LOOKUP_INITIAL_DELAY_SECONDS);
    }

    public @Nullable Set<Firmware> refreshFirmware(Thing thing) {
        return refreshFirmware(thing, false);
    }

    public @Nullable Set<Firmware> refreshFirmware(Thing thing, boolean useLowerVersionProbe) {
        if (!"zwave".equals(thing.getThingTypeUID().getBindingId())) {
            return null;
        }

        Integer nodeId = readNodeId(thing);
        if (nodeId == null) {
            return null;
        }

        String manufacturerId = readHexProperty(thing, ZWaveBindingConstants.PROPERTY_MANUFACTURER);
        String productType = readHexProperty(thing, ZWaveBindingConstants.PROPERTY_DEVICETYPE);
        String productId = readHexProperty(thing, ZWaveBindingConstants.PROPERTY_DEVICEID);
        String firmwareVersion = thing.getProperties().get(ZWaveBindingConstants.PROPERTY_VERSION);

        if (manufacturerId == null || productType == null || productId == null || firmwareVersion == null) {
            logger.debug("NODE {}: Missing device properties for remote firmware lookup", nodeId);
            return null;
        }

        String lookupFirmwareVersion = getLookupFirmwareVersion(firmwareVersion, useLowerVersionProbe);

        @Nullable
        String rfRegion = resolveRfRegion(thing);

        logger.debug("NODE {}: Querying remote firmware update service for fingerprint {}/{}/{} version {} region {}",
                nodeId, manufacturerId, productType, productId, lookupFirmwareVersion,
                rfRegion != null ? rfRegion : "(none)");

        JsonObject requestBody = buildRequestBody(manufacturerId, productType, productId, lookupFirmwareVersion,
                rfRegion);
        logger.debug("NODE {}: Sending remote firmware update request {}", nodeId, requestBody);

        JsonObject updateEntry = null;
        try {
            updateEntry = queryLatestStableUpdate(GSON.toJson(requestBody), manufacturerId, productType, productId,
                    rfRegion);
        } catch (IOException e) {
            logger.warn("NODE {}: Failed to query firmware update service: {}", nodeId, e.getMessage());
            return null;
        } catch (InterruptedException e) {
            logger.warn("NODE {}: Failed to query firmware update service: {}", nodeId, e.getMessage());
            Thread.currentThread().interrupt();
            return null;
        }

        if (updateEntry == null) {
            logger.debug("NODE {}: No firmware available from remote service", nodeId);
            return null;
        }

        return stageRemoteFirmware(thing, nodeId, updateEntry);
    }

    /**
     * Resolves the RF region from the bridge thing's properties, falling back to
     * the default locale if not available for main regions (US, EU, ANZ).
     * Returns a lowercase string suitable for the remote firmware update service.
     */
    private @Nullable String resolveRfRegion(Thing thing) {
        ThingUID bridgeUID = thing.getBridgeUID();
        if (bridgeUID != null && thingRegistry != null) {
            Thing bridgeThing = thingRegistry.get(bridgeUID);
            if (bridgeThing != null) {
                String regionProp = bridgeThing.getProperties().get(ZWaveBindingConstants.PROPERTY_RF_REGION);
                if (regionProp != null && !regionProp.isBlank() && !"unknown".equalsIgnoreCase(regionProp)) {
                    return regionProp;
                }
            }
        }

        ZWaveRegion region = ZWaveRegion.fromLocale(Locale.getDefault());
        return region.getLowerCaseName();
    }

    private static String getLookupFirmwareVersion(String firmwareVersion, boolean useLowerVersionProbe) {
        if (!useLowerVersionProbe) {
            return firmwareVersion;
        }

        String adjusted = adjustVersionForPrefetchProbe(firmwareVersion);
        LoggerFactory.getLogger(ZWaveRemoteFirmwareProvider.class)
                .debug("Using override for remote firmware lookup: {} -> {}", firmwareVersion, adjusted);
        return adjusted;
    }

    @SuppressWarnings("unused")
    private static String getLookupFirmwareVersion(String firmwareVersion) {
        return getLookupFirmwareVersion(firmwareVersion, STARTUP_PREFETCH_ALLOW_LOWER_VERSION_PROBE);
    }

    private static String adjustVersionForPrefetchProbe(String version) {
        String trimmed = version.trim();
        if (trimmed.isBlank()) {
            return trimmed;
        }

        String[] parts = trimmed.split("\\.");
        int[] values = new int[parts.length];
        boolean allNumeric = true;
        for (int i = 0; i < parts.length; i++) {
            try {
                values[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                allNumeric = false;
                break;
            }
        }

        if (!allNumeric) {
            return trimmed;
        }

        // Decrement the rightmost non-zero segment after major without crossing
        // the major boundary (e.g. keep 10.0 as 10.0, not 9.0).
        int decrementIndex = -1;
        for (int i = values.length - 1; i >= 1; i--) {
            if (values[i] > 0) {
                decrementIndex = i;
                break;
            }
        }

        if (decrementIndex < 0) {
            return trimmed;
        }
        values[decrementIndex]--;

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append('.');
            }
            builder.append(values[i]);
        }
        return builder.toString();
    }

    // Needed for unit tests
    @SuppressWarnings("unused")
    private static String buildLookupCacheKey(String thingUid, String manufacturerId, String productType,
            String productId, String firmwareVersion) {
        return String.join("|", normalizeLookupCacheToken(thingUid), normalizeLookupCacheToken(manufacturerId),
                normalizeLookupCacheToken(productType), normalizeLookupCacheToken(productId),
                normalizeLookupCacheToken(firmwareVersion));
    }

    private static String normalizeLookupCacheToken(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith("0x")) {
            normalized = normalized.substring(2);
        }
        return normalized;
    }

    private static JsonObject buildRequestBody(String manufacturerId, String productType, String productId,
            String firmwareVersion, @Nullable String region) {
        JsonObject device = new JsonObject();
        device.addProperty("manufacturerId", manufacturerId);
        device.addProperty("productType", productType);
        device.addProperty("productId", productId);
        device.addProperty("firmwareVersion", firmwareVersion);

        JsonArray devices = new JsonArray();
        devices.add(device);

        JsonObject requestBody = new JsonObject();
        requestBody.add("devices", devices);
        if (region != null && !region.isBlank()) {
            requestBody.addProperty("region", region);
        }
        return requestBody;
    }

    private @Nullable Set<Firmware> stageRemoteFirmware(Thing thing, int nodeId, JsonObject updateEntry) {
        String newVersion = updateEntry.get("version").getAsString();
        JsonArray files = updateEntry.getAsJsonArray("files");
        if (files == null || files.isEmpty()) {
            return null;
        }

        // Use firmware target 0; fall back to the first entry
        JsonObject fileEntry = findTarget(files, 0);
        if (fileEntry == null) {
            return null;
        }

        String url = fileEntry.get("url").getAsString();
        String integrity = fileEntry.get("integrity").getAsString();

        Path folder = Paths.get(OpenHAB.getUserDataFolder(), "zwave", "firmware", "node-" + nodeId);
        Path localFile = resolveLocalPath(folder, url, newVersion);

        try {
            clearNodeFirmwareFolder(folder);
            localFile = downloadAndVerify(url, integrity, folder, localFile, nodeId);

            InputStream inputStream = Files.newInputStream(localFile);
            String fileName = localFile.getFileName().toString();
            Firmware firmware = FirmwareBuilder.create(thing.getThingTypeUID(), newVersion)
                    .withDescription("Remote Z-Wave firmware: " + fileName).withInputStream(inputStream)
                    .withProperties(Map.of("zwave.firmware.file", fileName)).build();
            return Set.of(firmware);
        } catch (IOException e) {
            logger.warn("NODE {}: Failed to stage firmware file {}: {}", nodeId, localFile, e.getMessage());
            return null;
        }
    }

    private void clearNodeFirmwareFolder(Path folder) throws IOException {
        Files.createDirectories(folder);
        try (Stream<Path> files = Files.list(folder)) {
            files.filter(Files::isRegularFile).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    logger.warn("Unable to delete old firmware file {}", path, e);
                }
            });
        }
    }

    /**
     * POSTs to the v4 API and returns the best available stable, non-downgrade
     * update entry for the given device fingerprint, or {@code null} if none.
     */
    private @Nullable JsonObject queryLatestStableUpdate(String requestJson, String manufacturerId, String productType,
            String productId, @Nullable String region) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL))
                .header("Content-Type", "application/json").header("User-Agent", USER_AGENT)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson)).build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            logger.warn("Firmware update service returned HTTP {}: {}", response.statusCode(), response.body());
            return null;
        }

        JsonElement responseElement = GSON.fromJson(response.body(), JsonElement.class);
        if (responseElement == null || responseElement.isJsonNull()) {
            return null;
        }

        return findBestUpdateForDevice(responseElement, manufacturerId, productType, productId, region);
    }

    /**
     * Finds the matching device result in an API v4 response and returns its best
     * update.
     */
    private @Nullable JsonObject findBestUpdateForDevice(JsonElement responseElement, String manufacturerId,
            String productType, String productId, @Nullable String region) {
        if (responseElement.isJsonArray()) {
            JsonArray responseArray = responseElement.getAsJsonArray();
            for (JsonElement elem : responseArray) {
                JsonObject deviceResult = elem.getAsJsonObject();
                if (matchesDevice(deviceResult, manufacturerId, productType, productId)) {
                    return findBestUpdateFromDevice(deviceResult, region);
                }
            }
            return null;
        }

        if (!responseElement.isJsonObject()) {
            return null;
        }

        JsonObject responseObject = responseElement.getAsJsonObject();
        JsonArray deviceResults = responseObject.getAsJsonArray("devices");
        if (deviceResults == null) {
            JsonElement valueElement = responseObject.get("value");
            if (valueElement != null && valueElement.isJsonArray()) {
                deviceResults = valueElement.getAsJsonArray();
            }
        }

        if (deviceResults != null) {
            for (JsonElement elem : deviceResults) {
                JsonObject deviceResult = elem.getAsJsonObject();
                if (matchesDevice(deviceResult, manufacturerId, productType, productId)) {
                    JsonArray upgrades = responseObject.getAsJsonArray("upgrades");
                    if (upgrades != null && !upgrades.isEmpty()) {
                        return findBestUpdate(upgrades, region);
                    }
                    return findBestUpdateFromDevice(deviceResult, region);
                }
            }
        }

        return null;
    }

    private static boolean matchesDevice(JsonObject deviceResult, String manufacturerId, String productType,
            String productId) {
        return manufacturerId.equalsIgnoreCase(deviceResult.get("manufacturerId").getAsString())
                && productType.equalsIgnoreCase(deviceResult.get("productType").getAsString())
                && productId.equalsIgnoreCase(deviceResult.get("productId").getAsString());
    }

    private @Nullable JsonObject findBestUpdateFromDevice(JsonObject deviceResult, @Nullable String region) {
        JsonArray updates = deviceResult.getAsJsonArray("updates");
        if (updates == null || updates.isEmpty()) {
            return null;
        }
        return findBestUpdate(updates, region);
    }

    /**
     * Selects the highest-versioned stable, non-downgrade entry from the updates
     * array.
     */
    private @Nullable JsonObject findBestUpdate(JsonArray updates, @Nullable String region) {
        JsonObject bestStable = null;
        JsonObject bestFallback = null;
        for (JsonElement elem : updates) {
            JsonObject update = elem.getAsJsonObject();
            if (!isCompatibleWithRequestedRegion(update, region)) {
                continue;
            }
            JsonElement downgradeElement = update.get("downgrade");
            if (downgradeElement != null && downgradeElement.getAsBoolean()) {
                continue;
            }

            JsonElement channelElement = update.get("channel");
            String channel = channelElement == null ? null : channelElement.getAsString();
            String normalizedVersion = getVersionString(update, "normalizedVersion", "version");
            boolean isStable = "stable".equals(channel) || channel == null || channel.isBlank();
            boolean isFallback = "latest".equals(channel) || "beta".equals(channel);

            if (isStable) {
                if (bestStable == null || compareVersions(normalizedVersion,
                        getVersionString(bestStable, "normalizedVersion", "version")) > 0) {
                    bestStable = update;
                }
            } else if (isFallback) {
                if (bestFallback == null || compareVersions(normalizedVersion,
                        getVersionString(bestFallback, "normalizedVersion", "version")) > 0) {
                    bestFallback = update;
                }
            }
        }

        return bestStable != null ? bestStable : bestFallback;
    }

    private static boolean isCompatibleWithRequestedRegion(JsonObject update, @Nullable String region) {
        JsonElement regionElement = update.get("region");
        if (region == null || region.isBlank()) {
            return regionElement == null || regionElement.isJsonNull();
        }
        if (regionElement == null || regionElement.isJsonNull()) {
            return true;
        }
        return region.equalsIgnoreCase(regionElement.getAsString());
    }

    private static String getVersionString(JsonObject update, String primaryKey, String fallbackKey) {
        JsonElement primaryElement = update.get(primaryKey);
        if (primaryElement != null && !primaryElement.isJsonNull()) {
            return primaryElement.getAsString();
        }
        JsonElement fallbackElement = update.get(fallbackKey);
        return fallbackElement == null || fallbackElement.isJsonNull() ? "0" : fallbackElement.getAsString();
    }

    /**
     * Numeric semver comparison of "major.minor.patch" strings.
     * Returns positive if {@code a > b}, negative if {@code a < b}, 0 if equal.
     */
    private static int compareVersions(String a, String b) {
        int[] aParts = parseVersion(a);
        int[] bParts = parseVersion(b);
        for (int i = 0; i < 3; i++) {
            int diff = aParts[i] - bParts[i];
            if (diff != 0) {
                return diff;
            }
        }
        return 0;
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.", 3);
        int[] result = new int[3];
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            try {
                result[i] = Integer.parseInt(parts[i]);
            } catch (NumberFormatException e) {
                result[i] = 0;
            }
        }
        return result;
    }

    private static @Nullable JsonObject findTarget(JsonArray files, int target) {
        for (JsonElement elem : files) {
            JsonObject file = elem.getAsJsonObject();
            if (file.get("target").getAsInt() == target) {
                return file;
            }
        }
        // Fall back to the first file if target 0 is not listed
        return files.isEmpty() ? null : files.get(0).getAsJsonObject();
    }

    /**
     * Derives a local filename from the remote URL so the staged firmware file
     * keeps the remote name for clarity for comparing it later.
     */
    private static Path resolveLocalPath(Path folder, String url, String version) {
        String urlPath = URI.create(url).getPath();
        String remoteFileName = urlPath.substring(urlPath.lastIndexOf('/') + 1);
        if (remoteFileName.isBlank()) {
            String extension = ".bin";
            int dot = version.lastIndexOf('.');
            if (dot > 0) {
                extension = version.substring(dot);
            }
            return folder.resolve(version + extension);
        }
        return folder.resolve(remoteFileName);
    }

    private Path downloadAndVerify(String url, String integrity, Path folder, Path destination, int nodeId)
            throws IOException {
        Files.createDirectories(folder);

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("User-Agent", USER_AGENT).GET()
                .build();

        byte[] bytes;
        try {
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IOException("Firmware download returned HTTP " + response.statusCode());
            }
            bytes = response.body();

            verifyIntegrity(bytes, integrity);

            Path resolvedDestination = resolveDownloadedPath(folder, destination, response);
            Files.write(resolvedDestination, bytes);
            logger.debug("NODE {}: Downloaded firmware to {}", nodeId, resolvedDestination);
            return resolvedDestination;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Firmware download interrupted", e);
        }
    }

    private static Path resolveDownloadedPath(Path folder, Path fallbackDestination, HttpResponse<byte[]> response) {
        String fallbackFileName = fallbackDestination.getFileName().toString();

        String fileName = extractFileNameFromContentDisposition(
                response.headers().firstValue("Content-Disposition").orElse(null));
        if (fileName == null || fileName.isBlank()) {
            fileName = extractFileNameFromUri(response.uri());
        }
        if (fileName == null || fileName.isBlank()) {
            fileName = fallbackFileName;
        }

        String safeName = fileName.replace('/', '_').replace('\\', '_').trim();
        if (safeName.isBlank()) {
            safeName = fallbackFileName;
        }
        return folder.resolve(safeName);
    }

    private static @Nullable String extractFileNameFromContentDisposition(@Nullable String contentDisposition) {
        if (contentDisposition == null || contentDisposition.isBlank()) {
            return null;
        }

        String[] parts = contentDisposition.split(";");
        for (String part : parts) {
            String token = part.trim();
            if (token.regionMatches(true, 0, "filename*=", 0, 10)) {
                String value = token.substring(10).trim();
                int charsetSeparator = value.indexOf("''");
                if (charsetSeparator >= 0 && charsetSeparator + 2 < value.length()) {
                    value = value.substring(charsetSeparator + 2);
                }
                return URLDecoder.decode(stripQuotes(value), StandardCharsets.UTF_8);
            }
            if (token.regionMatches(true, 0, "filename=", 0, 9)) {
                return stripQuotes(token.substring(9).trim());
            }
        }
        return null;
    }

    private static @Nullable String extractFileNameFromUri(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return null;
        }
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        if (name.isBlank()) {
            return null;
        }
        return URLDecoder.decode(name, StandardCharsets.UTF_8);
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private void verifyIntegrity(byte[] data, String integrity) throws IOException {
        if (!integrity.startsWith("sha256:")) {
            logger.debug("Unknown integrity format '{}', skipping integrity check", integrity);
            return;
        }

        byte[] hashInput = data;
        if (isAscii(data)) {
            try {
                byte[] decoded = decodeIntelHexPayload(data);
                hashInput = decoded;
                logger.debug("Using decoded Intel HEX payload for integrity check ({} raw bytes -> {} payload bytes)",
                        data.length, decoded.length);
            } catch (IOException e) {
                // For .ota/.otz some vendors publish binary payloads despite extension.
                logger.debug("Intel HEX decode failed for integrity check, falling back to raw bytes: {}",
                        e.getMessage());
            }
        }

        String expectedHex = integrity.substring(7);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] actualBytes = digest.digest(hashInput);
            StringBuilder sb = new StringBuilder(actualBytes.length * 2);
            for (byte b : actualBytes) {
                sb.append(String.format("%02x", b));
            }
            String actualHex = sb.toString();
            if (!actualHex.equalsIgnoreCase(expectedHex)) {
                throw new IOException(
                        "Firmware integrity check failed: expected " + expectedHex + " but got " + actualHex);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 unavailable", e);
        }
    }

    private static boolean isAscii(byte[] data) {
        for (byte b : data) {
            if ((b & 0xFF) > 0x7F) {
                return false;
            }
        }
        return true;
    }

    private static byte[] decodeIntelHexPayload(byte[] data) throws IOException {
        String hexText = new String(data, StandardCharsets.US_ASCII);
        String[] lines = hexText.split("\\r?\\n");

        TreeMap<Integer, Byte> memory = new TreeMap<>();
        int baseAddress = 0;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (!line.startsWith(":")) {
                throw new IOException("Intel HEX line does not start with ':'");
            }

            String record = line.substring(1);
            if (record.length() < 10 || (record.length() % 2) != 0) {
                throw new IOException("Invalid Intel HEX record length");
            }

            int byteCount = parseHexByte(record, 0);
            int address = parseHexWord(record, 2);
            int recordType = parseHexByte(record, 6);

            int expectedLength = 10 + (byteCount * 2);
            if (record.length() != expectedLength) {
                throw new IOException("Invalid Intel HEX record size");
            }

            int dataOffset = 8;
            int checksumOffset = dataOffset + (byteCount * 2);
            byte[] recordData = new byte[byteCount];
            int sum = byteCount + ((address >> 8) & 0xFF) + (address & 0xFF) + recordType;

            for (int i = 0; i < byteCount; i++) {
                int value = parseHexByte(record, dataOffset + (i * 2));
                recordData[i] = (byte) value;
                sum += value;
            }

            int checksum = parseHexByte(record, checksumOffset);
            if (((sum + checksum) & 0xFF) != 0) {
                throw new IOException("Intel HEX checksum mismatch");
            }

            switch (recordType) {
                case 0x00:
                    int absoluteAddress = baseAddress + address;
                    for (int i = 0; i < recordData.length; i++) {
                        memory.put(absoluteAddress + i, recordData[i]);
                    }
                    break;
                case 0x01:
                    // EOF
                    break;
                case 0x02:
                    if (recordData.length != 2) {
                        throw new IOException("Invalid Intel HEX extended segment record");
                    }
                    baseAddress = (((recordData[0] & 0xFF) << 8) | (recordData[1] & 0xFF)) << 4;
                    break;
                case 0x03:
                case 0x05:
                    // Start address records are not needed for payload extraction.
                    break;
                case 0x04:
                    if (recordData.length != 2) {
                        throw new IOException("Invalid Intel HEX extended linear address record");
                    }
                    baseAddress = (((recordData[0] & 0xFF) << 8) | (recordData[1] & 0xFF)) << 16;
                    break;
                default:
                    throw new IOException("Unsupported Intel HEX record type " + recordType);
            }
        }

        if (memory.isEmpty()) {
            throw new IOException("Intel HEX contains no data records");
        }

        int endOffset = memory.lastKey() + 1;
        byte[] payload = new byte[endOffset];
        Arrays.fill(payload, (byte) 0xFF);
        for (Map.Entry<Integer, Byte> entry : memory.entrySet()) {
            payload[entry.getKey()] = entry.getValue();
        }
        return payload;
    }

    private static int parseHexByte(String hex, int offset) throws IOException {
        if (offset + 2 > hex.length()) {
            throw new IOException("Invalid Intel HEX byte offset");
        }
        try {
            return Integer.parseInt(hex.substring(offset, offset + 2), 16);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid Intel HEX byte value", e);
        }
    }

    private static int parseHexWord(String hex, int offset) throws IOException {
        if (offset + 4 > hex.length()) {
            throw new IOException("Invalid Intel HEX word offset");
        }
        try {
            return Integer.parseInt(hex.substring(offset, offset + 4), 16);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid Intel HEX word value", e);
        }
    }

    /**
     * Reads a Thing property and normalizes it to a zero-padded four-digit
     * lowercase hexadecimal string (e.g. "291" → "0123") for the remote API.
     */
    private static @Nullable String readHexProperty(Thing thing, String key) {
        String value = thing.getProperties().get(key);
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        boolean isHex = trimmed.startsWith("0x") || trimmed.startsWith("0X");
        String normalized = trimmed;
        if (isHex) {
            normalized = trimmed.substring(2);
        }

        if (isHex || !normalized.matches("[0-9]+")) {
            try {
                String parsed = String.format(Locale.ROOT, "%04x", Integer.parseInt(normalized, 16));
                return "0x" + parsed;
            } catch (NumberFormatException e) {
                return "0x" + normalized.toLowerCase(Locale.ROOT);
            }
        }

        try {
            String parsed = String.format(Locale.ROOT, "%04x", Integer.parseInt(normalized));
            return "0x" + parsed;
        } catch (NumberFormatException e) {
            return "0x" + normalized.toLowerCase(Locale.ROOT);
        }
    }

    private static @Nullable Integer readNodeId(Thing thing) {
        Object nodeId = thing.getConfiguration().get(ZWaveBindingConstants.CONFIGURATION_NODEID);
        if (nodeId instanceof Number number) {
            return number.intValue();
        }
        if (nodeId instanceof String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private static boolean hasLookupFingerprint(Thing thing) {
        return readNodeId(thing) != null && readHexProperty(thing, ZWaveBindingConstants.PROPERTY_MANUFACTURER) != null
                && readHexProperty(thing, ZWaveBindingConstants.PROPERTY_DEVICETYPE) != null
                && readHexProperty(thing, ZWaveBindingConstants.PROPERTY_DEVICEID) != null
                && thing.getProperties().get(ZWaveBindingConstants.PROPERTY_VERSION) != null;
    }

    private static void scheduleStartupLookupWorker(long delaySeconds) {
        if (!STARTUP_LOOKUP_WORKER_SCHEDULED.compareAndSet(false, true)) {
            return;
        }

        STARTUP_LOOKUP_EXECUTOR.schedule(ZWaveRemoteFirmwareProvider::runQueuedStartupLookup, delaySeconds,
                TimeUnit.SECONDS);
    }

    private static void runQueuedStartupLookup() {
        StartupLookupRequest request = STARTUP_LOOKUP_QUEUE.poll();
        if (request == null) {
            STARTUP_LOOKUP_WORKER_SCHEDULED.set(false);
            return;
        }

        ZWaveRemoteFirmwareProvider provider = request.provider();
        try {
            Thing lookupThing = request.fallbackThing();
            if (lookupThing == null) {
                STARTUP_LOOKUP_PENDING.remove(request.thingUID());
                return;
            }

            Integer nodeId = readNodeId(lookupThing);
            if (nodeId == null) {
                STARTUP_LOOKUP_PENDING.remove(request.thingUID());
                return;
            }

            if (!hasLookupFingerprint(lookupThing)) {
                if (request.retriesRemaining() > 0) {
                    STARTUP_LOOKUP_QUEUE.add(new StartupLookupRequest(provider, request.thingUID(), lookupThing,
                            request.retriesRemaining() - 1));
                    provider.logger.debug(
                            "NODE {}: Startup remote firmware lookup deferred, waiting for node properties ({} retries left)",
                            nodeId, request.retriesRemaining());
                } else {
                    STARTUP_LOOKUP_PENDING.remove(request.thingUID());
                    provider.logger.debug(
                            "NODE {}: Startup remote firmware lookup skipped, node properties remained incomplete",
                            nodeId);
                }
                return;
            }

            Set<Firmware> firmwares = provider.refreshFirmware(lookupThing, STARTUP_PREFETCH_ALLOW_LOWER_VERSION_PROBE);
            if (firmwares == null || firmwares.isEmpty()) {
                provider.logger.debug("NODE {}: Startup remote firmware lookup completed with no result", nodeId);
            } else {
                provider.logger.debug("NODE {}: Startup remote firmware lookup staged {} firmware file(s)", nodeId,
                        firmwares.size());
            }
            STARTUP_LOOKUP_PENDING.remove(request.thingUID());
        } catch (RuntimeException e) {
            provider.logger.warn("Startup remote firmware lookup failed for thing {}: {}", request.thingUID(),
                    e.getMessage());
            STARTUP_LOOKUP_PENDING.remove(request.thingUID());
        } finally {
            STARTUP_LOOKUP_WORKER_SCHEDULED.set(false);
            if (!STARTUP_LOOKUP_QUEUE.isEmpty()) {
                scheduleStartupLookupWorker(STARTUP_LOOKUP_INTERVAL_SECONDS);
            }
        }
    }

    private static record StartupLookupRequest(ZWaveRemoteFirmwareProvider provider, ThingUID thingUID,
            @Nullable Thing fallbackThing, int retriesRemaining) {
    }
}
