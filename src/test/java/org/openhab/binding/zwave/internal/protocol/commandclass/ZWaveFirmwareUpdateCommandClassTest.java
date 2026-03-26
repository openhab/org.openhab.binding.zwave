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
import static org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass.crc16Ccitt;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.firmwareupdate.ZWaveFirmwareUpdateSession.FirmwareUpdateEvent;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass.FirmwareFragment;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Unit tests for {@link ZWaveFirmwareUpdateCommandClass} helper methods.
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveFirmwareUpdateCommandClassTest {

    private static final ZWaveController MOCKEDCONTROLLER = Mockito.mock(ZWaveController.class);

    private static final ZWaveNode SHARED_NODE = new ZWaveNode(0, 0, MOCKEDCONTROLLER);

    private static final ZWaveEndpoint SHARED_ENDPOINT = new ZWaveEndpoint(0);

    private static final ZWaveFirmwareUpdateCommandClass SHARED_CLS = new ZWaveFirmwareUpdateCommandClass(SHARED_NODE,
            MOCKEDCONTROLLER, SHARED_ENDPOINT);

    @Test
    public void testGetMetaDataGetMessagePayload() {
        ZWaveCommandClassTransactionPayload msg = SHARED_CLS.sendMDGetMessage();
        assertNotNull(msg);

        byte[] expected = new byte[] { (byte) CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey(),
                (byte) ZWaveFirmwareUpdateCommandClass.FIRMWARE_MD_GET };

        assertArrayEquals(expected, msg.getPayloadBuffer());
        assertEquals(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD, msg.getExpectedResponseCommandClass());
        assertEquals((Integer) ZWaveFirmwareUpdateCommandClass.FIRMWARE_MD_REPORT,
                msg.getExpectedResponseCommandClassCommand());
    }

    @Test
    public void testFirmwareFragmentV1() {
        // Synthetic data
        boolean isLast = false;
        int reportNumber = 5;
        byte[] firmwareData = new byte[] { 0x11, 0x22, 0x33, 0x44 };

        FirmwareFragment fragment = new FirmwareFragment(isLast, reportNumber, firmwareData, null);

        byte[] actual = fragment.toBytes(1, 0x7A, 0x06); // ccVersion=1, ccId/ccCommand ignored for v1

        // Expected:
        // Header word = 0x0005 (isLast=0, reportNumber=5)
        // Data = 11 22 33 44
        byte[] expected = new byte[] { 0x00, 0x05, // header
                0x11, 0x22, 0x33, 0x44 };

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testFirmwareFragmentV2() {
        boolean isLast = true;
        int reportNumber = 3;
        byte[] firmwareData = new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC };

        FirmwareFragment fragment = new FirmwareFragment(isLast, reportNumber, firmwareData, null);

        int ccVersion = 2;
        int ccId = 0x7A; // Firmware Update CC
        int ccCommand = 0x06; // Fragment command

        byte[] actual = fragment.toBytes(ccVersion, ccId, ccCommand);

        // Compute expected CRC using the same helper
        // Header word = 0x8003 (isLast=1, reportNumber=3)
        byte[] headerAndData = new byte[] { (byte) 0x80, 0x03, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC };

        int crc = crc16Ccitt(new byte[] { (byte) ccId, (byte) ccCommand }, 0x1D0F);
        crc = crc16Ccitt(headerAndData, crc);

        byte[] expected = ByteBuffer.allocate(headerAndData.length + 2).put(headerAndData).putShort((short) crc)
                .array();

        assertArrayEquals(expected, actual);
    }

    @Test
    public void testFirmwareUpdateMdGetMessagePayloadAndExpectedResponse() {
        ZWaveCommandClassTransactionPayload msg = SHARED_CLS.sendFirmwareUpdateMdGet(0x1234, 0x03);
        assertNotNull(msg);

        byte[] expected = new byte[] { (byte) CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey(),
                (byte) ZWaveFirmwareUpdateCommandClass.FIRMWARE_UPDATE_MD_GET, 0x03, 0x12, 0x34 };

        assertArrayEquals(expected, msg.getPayloadBuffer());
        assertEquals(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD, msg.getExpectedResponseCommandClass());
        assertEquals((Integer) ZWaveFirmwareUpdateCommandClass.FIRMWARE_UPDATE_MD_REPORT,
                msg.getExpectedResponseCommandClassCommand());
    }

    @Test
    public void testHandleFirmwareUpdateMdReportPublishesFragmentEvent() {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = new ZWaveNode(0, 7, controller);
        ZWaveEndpoint endpoint = new ZWaveEndpoint(0);
        ZWaveFirmwareUpdateCommandClass cls = new ZWaveFirmwareUpdateCommandClass(node, controller, endpoint);

        byte[] frame = new byte[] { (byte) CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey(),
                (byte) ZWaveFirmwareUpdateCommandClass.FIRMWARE_UPDATE_MD_REPORT, (byte) 0x80, 0x01, 0x11, 0x22 };

        cls.handleFirmwareUpdateMdReport(new ZWaveCommandClassPayload(frame), 0);

        ArgumentCaptor<ZWaveEvent> eventCaptor = ArgumentCaptor.forClass(ZWaveEvent.class);
        Mockito.verify(controller, Mockito.times(1)).notifyEventListeners(eventCaptor.capture());

        assertTrue(eventCaptor.getValue() instanceof FirmwareUpdateEvent);
        FirmwareUpdateEvent event = (FirmwareUpdateEvent) eventCaptor.getValue();
        assertEquals(FirmwareUpdateEvent.forMDReport(7, 0, new byte[0]).getType(), event.getType());
        assertArrayEquals(new byte[] { (byte) 0x80, 0x01, 0x11, 0x22 }, event.getPayload());
    }

    @Test
    public void testHandleFirmwarePrepareReportPublishesPrepareEvent() {
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveNode node = new ZWaveNode(0, 7, controller);
        ZWaveEndpoint endpoint = new ZWaveEndpoint(0);
        ZWaveFirmwareUpdateCommandClass cls = new ZWaveFirmwareUpdateCommandClass(node, controller, endpoint);

        byte[] frame = new byte[] { (byte) CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey(),
                (byte) ZWaveFirmwareUpdateCommandClass.FIRMWARE_UPDATE_PREPARE_REPORT,
                (byte) ZWaveFirmwareUpdateCommandClass.FirmwareDownloadStatus.SUCCESS.getId(), 0x12, 0x34 };

        cls.handleFirmwarePrepareReport(new ZWaveCommandClassPayload(frame), 0);

        ArgumentCaptor<ZWaveEvent> eventCaptor = ArgumentCaptor.forClass(ZWaveEvent.class);
        Mockito.verify(controller, Mockito.times(1)).notifyEventListeners(eventCaptor.capture());

        assertTrue(eventCaptor.getValue() instanceof FirmwareUpdateEvent);
        FirmwareUpdateEvent event = (FirmwareUpdateEvent) eventCaptor.getValue();
        assertEquals(FirmwareUpdateEvent.forUpdatePrepareReport(7, 0, 0, 0).getType(), event.getType());
        assertEquals(ZWaveFirmwareUpdateCommandClass.FirmwareDownloadStatus.SUCCESS.getId(), event.getStatus());
        assertArrayEquals(new byte[] { 0x12, 0x34 }, event.getPayload());
    }
}
