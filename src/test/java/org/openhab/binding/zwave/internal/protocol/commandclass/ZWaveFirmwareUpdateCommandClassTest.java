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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 * Unit tests for {@link ZWaveFirmwareUpdateCommandClass} helper methods.
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveFirmwareUpdateCommandClassTest {

        private static final org.openhab.binding.zwave.internal.protocol.ZWaveController mockedController =
            Mockito.mock(org.openhab.binding.zwave.internal.protocol.ZWaveController.class);
        private static final ZWaveNode sharedNode = new ZWaveNode(0, 0, mockedController);
        private static final ZWaveEndpoint sharedEndpoint = new ZWaveEndpoint(0);
        private static final ZWaveFirmwareUpdateCommandClass sharedCls = new ZWaveFirmwareUpdateCommandClass(
            sharedNode, mockedController, sharedEndpoint);
    @Test
    public void testGetMetaDataGetMessagePayload() {
        // create instance with dummy node/controller/endpoint; node id 0 is fine for logging
        // ZWaveFirmwareUpdateCommandClass cls = new ZWaveFirmwareUpdateCommandClass(null, null, null);
        //        new ZWaveNode(0, 0, null),
        //        null, null);

        ZWaveCommandClassTransactionPayload msg = sharedCls.getMetaDataGetMessage();
        assertNotNull(msg);

        byte[] expected = new byte[] { (byte) CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey(), (byte) 0x01 };
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expected));
        assertEquals(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD, msg.getExpectedResponseCommandClass());
        assertEquals((Integer) 0x02, msg.getExpectedResponseCommandClassCommand());
    }

    @Test
    public void testHandleMetaDataReport() {
        // sample payload includes the command class and command id prefix
        byte[] raw = new byte[] { 0x7A, 0x02, 0x02, 0x7A, 0x00, 0x03, 0x00, 0x00, (byte) 0xFF, 0x00, 0x00, 0x28, 0x02, (byte) 0xD0, 0x01 };
        int endpoint = 0;

        // the handler itself only logs; exercise the parser directly so we can verify values
        ZWaveFirmwareUpdateCommandClass.MetaDataReport report =
                ZWaveFirmwareUpdateCommandClass.MetaDataReport.fromBytes(raw, 1);

        assertEquals(0x027A, report.manufacturerId);
        assertEquals(0x0003, report.firmwareId);
        assertEquals(0x0000, report.checksum);
        assertTrue(report.firmwareUpgradable);
        assertEquals((Integer) 0x0028, report.maxFragmentSize);
        assertNotNull(report.additionalFirmwareIDs);
        assertTrue(report.additionalFirmwareIDs.isEmpty());
        assertEquals((Integer) 0x02, report.hardwareVersion);

        // ensure the public handler can be invoked without exception using the shared instance
        sharedCls.handleMetaDataReport(new ZWaveCommandClassPayload(raw), endpoint);

        // round‑trip the report and re‑parse to ensure serialization is consistent
        byte[] serialized = report.toBytes(1);
        ZWaveFirmwareUpdateCommandClass.MetaDataReport report2 =
                ZWaveFirmwareUpdateCommandClass.MetaDataReport.fromBytes(
                        concatPrefix((byte) CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey(),
                                (byte) ZWaveFirmwareUpdateCommandClass.FIRMWARE_MD_REPORT, serialized),
                        1);
        assertEquals(report.manufacturerId, report2.manufacturerId);
        assertEquals(report.firmwareId, report2.firmwareId);
        assertEquals(report.checksum, report2.checksum);
        assertEquals(report.firmwareUpgradable, report2.firmwareUpgradable);
        assertEquals(report.maxFragmentSize, report2.maxFragmentSize);
        assertEquals(report.additionalFirmwareIDs, report2.additionalFirmwareIDs);
        assertEquals(report.hardwareVersion, report2.hardwareVersion);
        assertEquals(report.continuesToFunction, report2.continuesToFunction);
        assertEquals(report.supportsActivation, report2.supportsActivation);
        assertEquals(report.supportsResuming, report2.supportsResuming);
        assertEquals(report.supportsNonSecureTransfer, report2.supportsNonSecureTransfer);
    }

    @Test
    public void testGetMetaDataRequestGetMessagePayload() {
        // custom request - verify payload includes serialized request bytes
        ZWaveFirmwareUpdateCommandClass.RequestGet req = new ZWaveFirmwareUpdateCommandClass.RequestGet(
            0x027A, 0x0003, 0x0000, 0, 0x0028, null, null, null, 2);
        ZWaveCommandClassTransactionPayload msg2 = sharedCls.getMetaDataRequestGetMessage(req);
        assertNotNull(msg2);
        byte[] built = req.toBytes();
        byte[] payload = msg2.getPayloadBuffer();
        assertEquals(2 + built.length, payload.length);
        // check prefix bytes
        assertEquals((byte) CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey(), payload[0]);
        assertEquals((byte) 0x03, payload[1]);
        // check request body
        assertTrue(Arrays.equals(built, Arrays.copyOfRange(payload, 2, payload.length)));
        }

    /**
     * Helper to prepend the command class and command id bytes to a payload.
     */
    private static byte[] concatPrefix(byte cls, byte cmd, byte[] data) {
        byte[] result = new byte[2 + data.length];
        result[0] = cls;
        result[1] = cmd;
        System.arraycopy(data, 0, result, 2, data.length);
        return result;
    }
}
