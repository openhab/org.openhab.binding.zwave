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

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link FirmwareFile} class, which is responsible for
 * representing a firmware file and providing utilities to detect and extract
 * firmware data from various vendor formats (BIN, HEX, GBL, Aeotec EXE, ZIP).
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class FirmwareFileTest {

    @Test
    public void testExtractBin() throws Exception {
        byte[] raw = new byte[] { 0x11, 0x22, 0x33 };

        FirmwareFile file = FirmwareFile.extractFirmware("firmware.bin", raw);

        assertArrayEquals(raw, file.data);
        assertNull(file.firmwareTarget);
    }

    @Test
    public void testExtractHex() throws Exception {
        String hex = ":020000040000FA\n" + // extended linear address = 0
                ":10000000000102030405060708090A0B0C0D0E0F78\n" + ":00000001FF\n";

        byte[] raw = hex.getBytes(StandardCharsets.US_ASCII);

        FirmwareFile file = FirmwareFile.extractFirmware("firmware.hex", raw);

        // Expect 16 bytes from 0x0000 to 0x000F
        assertEquals(16, file.data.length);

        for (int i = 0; i < 16; i++) {
            assertEquals((byte) i, file.data[i]);
        }
    }

    @Test
    public void testExtractGbl() throws Exception {
        byte[] raw = new byte[] { (byte) 0xEB, 0x17, (byte) 0xA6, 0x03, // Gecko magic
                0x11, 0x22, 0x33 };

        FirmwareFile file = FirmwareFile.extractFirmware("firmware.gbl", raw);

        assertArrayEquals(raw, file.data);
    }

    @Test
    public void testExtractAeotecExe() throws Exception {
        // Fake EXE layout:
        // [MZ][padding...][firmware][start][length]
        byte[] firmware = new byte[] { 0x55, 0x66, 0x77 };

        byte[] exe = new byte[64];
        exe[0] = 0x4D; // 'M'
        exe[1] = 0x5A; // 'Z'

        int firmwareStart = 16;
        System.arraycopy(firmware, 0, exe, firmwareStart, firmware.length);

        // Write start/length at end
        ByteBuffer buf = ByteBuffer.wrap(exe).order(ByteOrder.BIG_ENDIAN);
        buf.putInt(exe.length - 8, firmwareStart);
        buf.putInt(exe.length - 4, firmware.length);

        FirmwareFile file = FirmwareFile.extractFirmware("firmware.exe", exe);

        assertArrayEquals(firmware, file.data);
    }

    @Test
    public void testExtractZipWithBin() throws Exception {
        byte[] inner = new byte[] { 0x01, 0x02, 0x03 };

        // Build ZIP in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        ZipEntry entry = new ZipEntry("firmware.bin");
        zos.putNextEntry(entry);
        zos.write(inner);
        zos.closeEntry();
        zos.close();

        byte[] zipBytes = baos.toByteArray();

        FirmwareFile file = FirmwareFile.extractFirmware("firmware.zip", zipBytes);

        assertArrayEquals(inner, file.data);
    }

    @Test
    public void testExtractZipWithHex() throws Exception {
        String hex = ":020000040000FA\n" + ":0400000001020304F2\n" + ":00000001FF\n";

        byte[] hexBytes = hex.getBytes(StandardCharsets.US_ASCII);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        zos.putNextEntry(new ZipEntry("firmware.hex"));
        zos.write(hexBytes);
        zos.closeEntry();
        zos.close();

        FirmwareFile file = FirmwareFile.extractFirmware("firmware.zip", baos.toByteArray());

        assertEquals(4, file.data.length);
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, file.data);
    }

    @Test
    public void testDetectFormatInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            FirmwareFile.detectFormat("firmware.xyz", new byte[] { 0x00 });
        });
    }

    @Test
    public void testInvalidGblMagic() {
        byte[] raw = new byte[] { 0x00, 0x11, 0x22, 0x33 };

        assertThrows(IllegalArgumentException.class, () -> {
            FirmwareFile.extractFirmware("firmware.gbl", raw);
        });
    }

    @Test
    public void testInvalidAeotecExeHeader() {
        byte[] raw = new byte[32]; // no MZ header

        assertThrows(IllegalArgumentException.class, () -> {
            FirmwareFile.extractFirmware("firmware.exe", raw);
        });
    }

    @Test
    public void testZipNoValidFirmware() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        zos.putNextEntry(new ZipEntry("readme.txt"));
        zos.write("hello".getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
        zos.close();

        assertThrows(IllegalArgumentException.class, () -> {
            FirmwareFile.extractFirmware("firmware.zip", baos.toByteArray());
        });
    }
}
