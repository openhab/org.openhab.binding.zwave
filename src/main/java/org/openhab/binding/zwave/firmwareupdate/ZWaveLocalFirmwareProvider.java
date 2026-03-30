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
package org.openhab.binding.zwave.firmwareupdate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.firmware.Firmware;
import org.openhab.core.thing.binding.firmware.FirmwareBuilder;
import org.openhab.core.thing.firmware.FirmwareProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposes local Z-Wave firmware files to the openHAB firmware UI.
 *
 * This provider keeps the current Z-Wave storage model intact by sourcing firmware
 * metadata and bytes from userdata/zwave/firmware/node-{nodeId}.
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@Component(service = FirmwareProvider.class)
@NonNullByDefault
public class ZWaveLocalFirmwareProvider implements FirmwareProvider {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveLocalFirmwareProvider.class);

    private static final Set<String> SUPPORTED_FIRMWARE_EXTENSIONS = Set.of(".bin", ".hex", ".ota", ".otz", ".gbl",
            ".zip", ".exe", ".ex_");

    /** Matches patterns like V02R40, v2r40, V1_06, V10_0 and extracts major/minor revision numbers. */
    private static final Pattern VERSION_PATTERN = Pattern.compile("[Vv](\\d+)[Rr_](\\d+)");

    @Override
    public @Nullable Firmware getFirmware(Thing thing, String version) {
        return getFirmware(thing, version, null);
    }

    @Override
    public @Nullable Firmware getFirmware(Thing thing, String version, @Nullable Locale locale) {
        Set<Firmware> firmwares = getFirmwares(thing, locale);
        if (firmwares == null) {
            return null;
        }

        Optional<Firmware> matchingFirmware = firmwares.stream()
                .filter(firmware -> firmware.getVersion().equals(version)).findFirst();
        if (matchingFirmware.isPresent()) {
            return matchingFirmware.get();
        }
        return null;
    }

    @Override
    public @Nullable Set<Firmware> getFirmwares(Thing thing) {
        return getFirmwares(thing, null);
    }

    @Override
    public @Nullable Set<Firmware> getFirmwares(Thing thing, @Nullable Locale locale) {
        if (!"zwave".equals(thing.getThingTypeUID().getBindingId())) {
            return null;
        }

        Integer nodeId = readNodeId(thing);
        if (nodeId == null) {
            return null;
        }

        Path folder = Paths.get(OpenHAB.getUserDataFolder(), "zwave", "firmware", "node-" + nodeId);
        if (!Files.isDirectory(folder)) {
            return null;
        }

        try (Stream<Path> files = Files.list(folder)) {
            Optional<Path> localFirmware = files.filter(Files::isRegularFile)
                    .filter(ZWaveLocalFirmwareProvider::isSupportedFirmwareFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .findFirst();

            if (localFirmware.isEmpty()) {
                return null;
            }

            Firmware firmware = toFirmware(thing, localFirmware.get());
            if (firmware == null) {
                return null;
            }
            return Set.of(firmware);
        } catch (IOException e) {
            logger.warn("Cannot list local Z-Wave firmware files from {}", folder, e);
            return null;
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

    private static boolean isSupportedFirmwareFile(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return SUPPORTED_FIRMWARE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    private @Nullable Firmware toFirmware(Thing thing, Path file) {
        String fileName = file.getFileName().toString();
        String version = extractVersion(fileName);

        try {
            InputStream inputStream = Files.newInputStream(file);
            return FirmwareBuilder.create(thing.getThingTypeUID(), version)
                    .withDescription("Local Z-Wave firmware file: " + fileName)
                    .withInputStream(inputStream)
                    .withProperties(Map.of("zwave.firmware.file", fileName)).build();
        } catch (IOException e) {
            logger.warn("Cannot open local Z-Wave firmware file {}", file, e);
            return null;
        }
    }

    /**
     * Extracts a numeric version from a firmware filename.
     * Converts manufacturer patterns like "ZEN73_V02R40.gbl" → "2.40" so that
     * openHAB Core can compare it numerically against the device's current firmware version.
     * Falls back to the bare filename (no extension) when no V##R## pattern is found.
     */
    private static String extractVersion(String fileName) {
        Matcher matcher = VERSION_PATTERN.matcher(fileName);
        if (matcher.find()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            return major + "." + minor;
        }
        return stripExtension(fileName);
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0) {
            return fileName;
        }
        return fileName.substring(0, dot);
    }
}
