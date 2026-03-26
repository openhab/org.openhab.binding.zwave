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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a firmware file and provides utilities to detect and extract
 * firmware data from various vendor formats (BIN, HEX, GBL, Aeotec EXE, ZIP).
 * This class is self-contained and does not rely on external libraries.
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public final class FirmwareFile {

    public final byte[] data;
    public final @Nullable Integer firmwareTarget;

    public FirmwareFile(byte[] data, @Nullable Integer firmwareTarget) {
        this.data = data;
        this.firmwareTarget = firmwareTarget;
    }

    // -------------------------------------------------------------------------
    // Supported formats
    // -------------------------------------------------------------------------
    public enum FirmwareFileFormat {
        BIN,
        HEX,
        OTA,
        OTZ,
        GBL, // Gecko bootloader
        AEOTEC, // Aeotec EXE/EX_
        ZIP
    }

    // -------------------------------------------------------------------------
    // Format detection
    // -------------------------------------------------------------------------
    public static FirmwareFileFormat detectFormat(String filename, byte[] rawData) {
        String lower = filename.toLowerCase();

        if (lower.endsWith(".bin")) {
            return FirmwareFileFormat.BIN;

        } else if (lower.endsWith(".gbl")) {
            if (rawData.length >= 4) {
                int magic = ((rawData[0] & 0xff) << 24) | ((rawData[1] & 0xff) << 16) | ((rawData[2] & 0xff) << 8)
                        | (rawData[3] & 0xff);
                if (magic == 0xEB17A603) {
                    return FirmwareFileFormat.GBL;
                }
            }
            throw new IllegalArgumentException("Invalid Gecko GBL firmware file");

        } else if (lower.endsWith(".exe") || lower.endsWith(".ex_")) {
            byte[] marker = "Zensys.ZWave".getBytes(StandardCharsets.UTF_8);
            if (indexOf(rawData, marker) >= 0) {
                return FirmwareFileFormat.AEOTEC;
            }
            // Must start with MZ and be large enough to contain footer
            if (rawData.length >= 12 && rawData[0] == 'M' && rawData[1] == 'Z') {
                return FirmwareFileFormat.AEOTEC;
            }

            throw new IllegalArgumentException("Unsupported EXE firmware file");

        } else if (lower.endsWith(".hex") || lower.endsWith(".ota") || lower.endsWith(".otz")) {
            return FirmwareFileFormat.HEX;

        } else if (lower.endsWith(".zip")) {
            return FirmwareFileFormat.ZIP;
        }

        throw new IllegalArgumentException("Unsupported firmware format: " + filename);
    }

    // -------------------------------------------------------------------------
    // Extraction entry point
    // -------------------------------------------------------------------------
    public static FirmwareFile extractFirmware(String filename, byte[] rawData) throws IOException {
        FirmwareFileFormat format = detectFormat(filename, rawData);

        switch (format) {
            case BIN:
            case GBL:
                return extractBinary(rawData);

            case HEX:
            case OTA:
            case OTZ:
                return extractHex(rawData);

            case AEOTEC:
                return extractAeotec(rawData);

            case ZIP:
                Optional<FirmwareFileContainer> container = tryUnzipFirmwareFile(rawData);
                if (container.isEmpty()) {
                    throw new IllegalArgumentException("ZIP does not contain a valid firmware file");
                }
                FirmwareFileContainer inner = container.get();
                return extractFirmware(inner.filename, inner.rawData);

            default:
                throw new IllegalArgumentException("Unsupported firmware format: " + format);
        }
    }

    // -------------------------------------------------------------------------
    // BIN / GBL extraction
    // -------------------------------------------------------------------------
    public static FirmwareFile extractBinary(byte[] data) {
        return new FirmwareFile(data, null);
    }

    // -------------------------------------------------------------------------
    // HEX extraction (Intel HEX)
    // -------------------------------------------------------------------------
    public static FirmwareFile extractHex(byte[] asciiBytes) {
        List<HexRecord> records = HexParser.parse(asciiBytes);

        int maxAddress = records.stream().mapToInt(r -> r.address + r.data.length).max().orElse(0);

        byte[] image = new byte[maxAddress];
        Arrays.fill(image, (byte) 0xFF);

        for (HexRecord r : records) {
            System.arraycopy(r.data, 0, image, r.address, r.data.length);
        }

        return new FirmwareFile(image, null);
    }

    // -------------------------------------------------------------------------
    // Aeotec EXE extraction
    // -------------------------------------------------------------------------
    public static FirmwareFile extractAeotec(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        if ((buf.getShort(0) & 0xffff) != 0x4D5A) {
            throw new IllegalArgumentException("Not a valid Aeotec updater (no MZ header)");
        }

        int firmwareStart = buf.getInt(data.length - 8);
        int firmwareLength = buf.getInt(data.length - 4);

        if (firmwareStart < 0 || firmwareLength <= 0 || firmwareStart + firmwareLength > data.length) {
            throw new IllegalArgumentException("Invalid firmware offsets in Aeotec EXE");
        }

        byte[] firmwareData = Arrays.copyOfRange(data, firmwareStart, firmwareStart + firmwareLength);

        return new FirmwareFile(firmwareData, null);
    }

    // -------------------------------------------------------------------------
    // ZIP extraction
    // -------------------------------------------------------------------------
    private static Optional<FirmwareFileContainer> tryUnzipFirmwareFile(byte[] zipBytes) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName().toLowerCase();

                if (name.endsWith(".bin") || name.endsWith(".hex") || name.endsWith(".ota") || name.endsWith(".otz")
                        || name.endsWith(".gbl") || name.endsWith(".exe") || name.endsWith(".ex_")) {
                    byte[] data = zis.readAllBytes();
                    FirmwareFileFormat format = detectFormat(name, data);
                    return Optional.of(new FirmwareFileContainer(name, format, data));
                }
            }
        }
        return Optional.empty();
    }

    private static final class FirmwareFileContainer {
        final String filename;
        final FirmwareFileFormat format;
        final byte[] rawData;

        FirmwareFileContainer(String filename, FirmwareFileFormat format, byte[] rawData) {
            this.filename = filename;
            this.format = format;
            this.rawData = rawData;
        }
    }

    // -------------------------------------------------------------------------
    // Intel HEX parser (minimal)
    // -------------------------------------------------------------------------
    private static final class HexRecord {
        final int address;
        final byte[] data;

        HexRecord(int address, byte[] data) {
            this.address = address;
            this.data = data;
        }
    }

    private static final class HexParser {

        public static List<HexRecord> parse(byte[] asciiBytes) {
            String text = new String(asciiBytes, StandardCharsets.US_ASCII).replace("\r", ""); // normalize CRLF → LF

            String[] lines = text.split("\n");
            List<HexRecord> records = new ArrayList<>();

            int upperAddress = 0;

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (!line.startsWith(":")) {
                    throw new IllegalArgumentException("Invalid HEX line (missing colon): " + line);
                }

                // Minimum length: ":" + LL + AAAA + TT + CC = 11 chars
                if (line.length() < 11) {
                    throw new IllegalArgumentException("HEX line too short: " + line);
                }

                int byteCount = parseByte(line, 1);
                int address = parseWord(line, 3);
                int recordType = parseByte(line, 7);

                int dataStart = 9;
                int dataEnd = dataStart + (byteCount * 2);

                // Check that the line contains enough characters for data + checksum
                if (line.length() < dataEnd + 2) {
                    throw new IllegalArgumentException("HEX line too short for declared byte count: " + line);
                }

                // Validate checksum
                int sum = 0;
                for (int i = 1; i < dataEnd; i += 2) {
                    sum += parseByte(line, i);
                }
                int checksum = parseByte(line, dataEnd);
                if (((sum + checksum) & 0xFF) != 0) {
                    throw new IllegalArgumentException("Invalid checksum in HEX line: " + line);
                }

                switch (recordType) {
                    case 0x00: { // Data record
                        byte[] data = new byte[byteCount];
                        for (int i = 0; i < byteCount; i++) {
                            int pos = dataStart + (i * 2);
                            data[i] = (byte) Integer.parseInt(line.substring(pos, pos + 2), 16);
                        }
                        records.add(new HexRecord(upperAddress + address, data));
                        break;
                    }
                    case 0x01: // EOF
                        return records;

                    case 0x04: { // Extended linear address
                        upperAddress = parseWord(line, dataStart) << 16;
                        break;
                    }
                    default:
                        // Other record types ignored
                        break;
                }
            }

            return records;
        }

        private static int parseByte(String line, int pos) {
            return Integer.parseInt(line.substring(pos, pos + 2), 16) & 0xFF;
        }

        private static int parseWord(String line, int pos) {
            return Integer.parseInt(line.substring(pos, pos + 4), 16) & 0xFFFF;
        }
    }

    // -------------------------------------------------------------------------
    // Byte-array search helper
    // -------------------------------------------------------------------------
    private static int indexOf(byte[] data, byte[] pattern) {
        outer: for (int i = 0; i <= data.length - pattern.length; i++) {
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public String getVersion() {
        // This is a placeholder. In a real implementation, you would extract the version
        // from the firmware data or metadata if available.
        return "Unknown Version";
    }

    public String getManufacturerName() {
        // This is a placeholder. In a real implementation, you would extract the manufacturer
        // name from the firmware data or metadata if available.
        return "Unknown Manufacturer";
    }
}
