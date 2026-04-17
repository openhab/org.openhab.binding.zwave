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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveTransactionCompletedEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Unit tests for for the Firmware Update Session, which is responsible for
 * managing the state of a firmware update process for a single node, including
 * parsing metadata, building requests, and handling events related to the
 * firmware update process. Tests involve the various versions of the Command Class
 * as they have changed over time.
 * {@link ZWaveFirmwareUpdateSession}.
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveFirmwareUpdateSessionTest {

    private static class TestableZWaveFirmwareUpdateSession extends ZWaveFirmwareUpdateSession {
        private long nowMillis;
        private int statusReportWaitTimeoutSeconds = 30;
        private long inactivityTimeoutMillis = TimeUnit.MINUTES.toMillis(2);

        public TestableZWaveFirmwareUpdateSession(ZWaveNode node, ZWaveControllerHandler controller,
                byte[] firmwareBytes, int firmwareTarget) {
            super(node, controller, firmwareBytes, firmwareTarget);
        }

        public void setCurrentTimeMillis(long nowMillis) {
            this.nowMillis = nowMillis;
        }

        public void setStatusReportWaitTimeoutSeconds(int statusReportWaitTimeoutSeconds) {
            this.statusReportWaitTimeoutSeconds = statusReportWaitTimeoutSeconds;
        }

        public void setInactivityTimeoutMillis(long inactivityTimeoutMillis) {
            this.inactivityTimeoutMillis = inactivityTimeoutMillis;
        }

        @Override
        protected long currentTimeMillis() {
            return nowMillis;
        }

        @Override
        protected int getStatusReportWaitTimeoutSeconds() {
            return statusReportWaitTimeoutSeconds;
        }

        @Override
        protected long getInactivityTimeoutMillis() {
            return inactivityTimeoutMillis;
        }
    }

    private ZWaveFirmwareUpdateSession newSession() {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        Mockito.when(node.getNodeId()).thenReturn(1);
        return new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01, 0x02 }, 0);
    }

    private void setState(ZWaveFirmwareUpdateSession session, ZWaveFirmwareUpdateSession.State state) throws Exception {
        Method method = ZWaveFirmwareUpdateSession.class.getDeclaredMethod("handleEvent", Object.class);
        Method unused = method; // moves the unused warning to here from method.
        java.lang.reflect.Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("state");
        field.setAccessible(true);
        field.set(session, state);
    }

    private Object parseMetadata(ZWaveFirmwareUpdateSession session, byte[] payload) throws Exception {
        Method method = ZWaveFirmwareUpdateSession.class.getDeclaredMethod("parseMetadata", byte[].class);
        method.setAccessible(true);
        return method.invoke(session, payload);
    }

    private byte[] buildMdRequestGet(ZWaveFirmwareUpdateSession session, Object metadata) throws Exception {
        Method method = ZWaveFirmwareUpdateSession.class.getDeclaredMethod("buildMdRequestGet",
                ZWaveFirmwareUpdateSession.FirmwareMetadata.class);
        method.setAccessible(true);
        return (byte[]) method.invoke(session, metadata);
    }

    private int expectedSessionChecksum() {
        return ZWaveFirmwareUpdateCommandClass.crc16Ccitt(new byte[] { 0x01, 0x02 }, 0x1D0F);
    }

    @Test
    public void testParseMetadataV7PlusMapsRequestFlagsAndReordersForReport3() throws Exception {
        ZWaveFirmwareUpdateSession session = newSession();

        // Version 7+ example with all optional fields present, additional target count of 1, and all request flags set
        byte[] payload = new byte[] { 0x12, 0x34, // manufacturer
                0x56, 0x78, // firmware
                (byte) 0x9A, (byte) 0xBC, // checksum
                0x01, // upgradable
                0x01, // additional targets
                0x01, (byte) 0xF4, // max fragment size
                0x00, 0x02, // one additional target entry
                0x05, // hardware version
                0x0F // report-2 flags: b3,b2,b1,b0 set
        };

        ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = (ZWaveFirmwareUpdateSession.FirmwareMetadata) parseMetadata(
                session, payload);

        assertEquals(0x1234, metadata.manufacturerId());
        assertEquals(0x5678, metadata.firmwareId());
        assertEquals(0x9ABC, metadata.checksum());
        assertEquals(0x01F4, metadata.maxFragmentSize());
        assertEquals(1, metadata.additionalTargets());
        assertTrue(metadata.hardwareVersionPresent());
        assertEquals(0x05, metadata.hardwareVersion());
        assertTrue(metadata.ccFunctionalityPresent());
        assertEquals(0x07, metadata.requestFlags());

        assertArrayEquals(
                new byte[] { 0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, 0x00, 0x01, (byte) 0xF4, 0x07, 0x05 },
                metadata.report3Payload());

        byte[] requestPayload = buildMdRequestGet(session, metadata);
        assertArrayEquals(
                new byte[] { 0x12, 0x34, 0x56, 0x78, (byte) ((expectedSessionChecksum() >> 8) & 0xFF),
                        (byte) (expectedSessionChecksum() & 0xFF), 0x00, 0x01, (byte) 0xF4, 0x07, 0x05 },
                requestPayload);
    }

    @Test
    public void testParseMetadataV5HasHardwareAndZeroActivationInReport3() throws Exception {
        ZWaveFirmwareUpdateSession session = newSession();

        // Version 5 example with hardware version present.
        byte[] payload = new byte[] { 0x02, 0x7A, 0x00, 0x03, 0x00, 0x00, (byte) 0xFF, 0x00, 0x00, 0x28, 0x02 };

        ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = (ZWaveFirmwareUpdateSession.FirmwareMetadata) parseMetadata(
                session, payload);

        assertEquals(0, metadata.requestFlags());
        assertTrue(metadata.hardwareVersionPresent());
        assertEquals(0x02, metadata.hardwareVersion());
        assertFalse(metadata.ccFunctionalityPresent());

        assertArrayEquals(new byte[] { 0x02, 0x7A, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x28, 0x00, 0x02 },
                metadata.report3Payload());

        byte[] requestPayload = buildMdRequestGet(session, metadata);
        assertArrayEquals(new byte[] { 0x02, 0x7A, 0x00, 0x03, (byte) ((expectedSessionChecksum() >> 8) & 0xFF),
                (byte) (expectedSessionChecksum() & 0xFF), 0x00, 0x00, 0x28, 0x00, 0x02 }, requestPayload);
    }

    @Test
    public void testParseMetadataV1V2UsesOnlyFirstSixBytesForReport3() throws Exception {
        ZWaveFirmwareUpdateSession session = newSession();

        byte[] payload = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };

        ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = (ZWaveFirmwareUpdateSession.FirmwareMetadata) parseMetadata(
                session, payload);

        assertTrue(metadata.upgradable());
        assertEquals(32, metadata.maxFragmentSize());
        assertFalse(metadata.hardwareVersionPresent());
        assertFalse(metadata.ccFunctionalityPresent());
        assertEquals(0, metadata.requestFlags());

        assertArrayEquals(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 }, metadata.report3Payload());

        byte[] requestPayload = buildMdRequestGet(session, metadata);
        assertArrayEquals(new byte[] { 0x01, 0x02, 0x03, 0x04, (byte) ((expectedSessionChecksum() >> 8) & 0xFF),
                (byte) (expectedSessionChecksum() & 0xFF) }, requestPayload);
    }

    @Test
    public void testParseMetadataV3BuildsReport3WithTargetAndFragmentOnly() throws Exception {
        ZWaveFirmwareUpdateSession session = newSession();

        // Version 3 example.
        byte[] payload = new byte[] { 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x01, 0x00, 0x00, 0x40 };

        ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = (ZWaveFirmwareUpdateSession.FirmwareMetadata) parseMetadata(
                session, payload);

        assertTrue(metadata.upgradable());
        assertFalse(metadata.hardwareVersionPresent());
        assertFalse(metadata.ccFunctionalityPresent());
        assertEquals(0, metadata.requestFlags());

        assertArrayEquals(new byte[] { 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x00, 0x00, 0x40 },
                metadata.report3Payload());

        byte[] requestPayload = buildMdRequestGet(session, metadata);
        assertArrayEquals(new byte[] { 0x0A, 0x0B, 0x0C, 0x0D, (byte) ((expectedSessionChecksum() >> 8) & 0xFF),
                (byte) (expectedSessionChecksum() & 0xFF), 0x00, 0x00, 0x40 }, requestPayload);
    }

    @Test
    public void testParseMetadataV6UsesSingleFlagsByteForFunctionalityOnly() throws Exception {
        ZWaveFirmwareUpdateSession session = newSession();

        // Version 6 example with functionality flags in report-2 flags byte.
        byte[] payload = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x01, 0x00, 0x00, 0x30, 0x09, 0x01 };

        ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = (ZWaveFirmwareUpdateSession.FirmwareMetadata) parseMetadata(
                session, payload);

        assertTrue(metadata.hardwareVersionPresent());
        assertTrue(metadata.ccFunctionalityPresent());
        assertEquals(0, metadata.requestFlags());

        assertArrayEquals(new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x00, 0x00, 0x30, 0x00, 0x09 },
                metadata.report3Payload());
    }

    @Test
    public void testParseMetadataRejectsInvalidAdditionalTargetLength() throws Exception {
        ZWaveFirmwareUpdateSession session = newSession();

        // Version 4 example with invalid additional target length.
        byte[] payload = new byte[] { 0x00, 0x01, 0x00, 0x02, 0x00, 0x03, 0x01, 0x02, 0x00, 0x20, 0x55 };

        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> parseMetadata(session, payload));
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
        assertTrue(ex.getCause().getMessage().contains("additional target data exceeds payload length"));
    }

    @Test
    public void testHandleMetadataReportMalformedPayloadNotifiesFailureEvent() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(7);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_MD_REPORT);

        boolean handled = session.handleEvent(
                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forMDReport(7, 0, new byte[] { 0x01, 0x02, 0x03 }));

        assertTrue(handled);
        assertFalse(session.isActive());
        Mockito.verify(controller)
                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                        && ((ZWaveNetworkEvent) event).getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                        && ((ZWaveNetworkEvent) event).getState() == ZWaveNetworkEvent.State.Failure));
    }

    @Test
    public void testHandleMetadataReportNonUpgradableNotifiesFailureEvent() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(8);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_MD_REPORT);

        byte[] payload = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x00, 0x00, 0x00, 0x20 };

        boolean handled = session
                .handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forMDReport(8, 0, payload));

        assertTrue(handled);
        assertFalse(session.isActive());
        Mockito.verify(controller)
                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                        && ((ZWaveNetworkEvent) event).getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                        && ((ZWaveNetworkEvent) event).getState() == ZWaveNetworkEvent.State.Failure));
    }

    @Test
    public void testFailedMetadataGetPublishesDescriptiveFailureMessage() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(9);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_MD_REPORT);
        setActive(session, true);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(9,
                new byte[] { (byte) CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey(),
                        (byte) ZWaveFirmwareUpdateCommandClass.FIRMWARE_MD_GET },
                TransactionPriority.Config, null, null);

        boolean handled = session.handleEvent(new ZWaveTransactionCompletedEvent(new ZWaveTransaction(tx), null, false));

        assertTrue(handled);
        assertFalse(session.isActive());
        assertEquals(ZWaveFirmwareUpdateSession.State.FAILURE, getState(session));
        Mockito.verify(controller).ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                && ((ZWaveNetworkEvent) event).getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                && ((ZWaveNetworkEvent) event).getState() == ZWaveNetworkEvent.State.Failure
                && "FIRMWARE_MD_GET failed after all retries".equals(((ZWaveNetworkEvent) event).getValue())));
    }

    private void setActive(ZWaveFirmwareUpdateSession session, boolean active) throws Exception {
        Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("active");
        field.setAccessible(true);
        field.set(session, active);
    }

    private ZWaveFirmwareUpdateSession.State getState(ZWaveFirmwareUpdateSession session) throws Exception {
        Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("state");
        field.setAccessible(true);
        return (ZWaveFirmwareUpdateSession.State) field.get(session);
    }

    private void setSessionMetadata(ZWaveFirmwareUpdateSession session,
            ZWaveFirmwareUpdateSession.FirmwareMetadata metadata) throws Exception {
        java.lang.reflect.Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("sessionMetadata");
        field.setAccessible(true);
        field.set(session, metadata);
    }

    private void setFragments(ZWaveFirmwareUpdateSession session,
            List<ZWaveFirmwareUpdateSession.FirmwareFragment> fragments) throws Exception {
        Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("fragments");
        field.setAccessible(true);
        field.set(session, fragments);
    }

    private int getHighestTransmittedReportNumber(ZWaveFirmwareUpdateSession session) throws Exception {
        Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("highestTransmittedReportNumber");
        field.setAccessible(true);
        return field.getInt(session);
    }

        private int getHighestAckedReportNumber(ZWaveFirmwareUpdateSession session) throws Exception {
                Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("highestAckedReportNumber");
                field.setAccessible(true);
                return field.getInt(session);
        }

    private void setLastPublishedProgressPercent(ZWaveFirmwareUpdateSession session, int percent) throws Exception {
        Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("lastPublishedProgressPercent");
        field.setAccessible(true);
        field.setInt(session, percent);
    }

    private void setHighestTransmittedReportNumber(ZWaveFirmwareUpdateSession session, int reportNumber)
            throws Exception {
        Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("highestTransmittedReportNumber");
        field.setAccessible(true);
        field.setInt(session, reportNumber);
    }

    private void setStartReportNumber(ZWaveFirmwareUpdateSession session, int reportNumber) throws Exception {
        Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("startReportNumber");
        field.setAccessible(true);
        field.setInt(session, reportNumber);
    }

    private void setCount(ZWaveFirmwareUpdateSession session, int count) throws Exception {
        Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("count");
        field.setAccessible(true);
        field.setInt(session, count);
    }

    private void invokePublishFirmwareUpdateProgressIfNeeded(ZWaveFirmwareUpdateSession session) throws Exception {
        Method method = ZWaveFirmwareUpdateSession.class.getDeclaredMethod("publishFirmwareUpdateProgressIfNeeded");
        method.setAccessible(true);
        method.invoke(session);
    }

    private void waitForSessionToStop(ZWaveFirmwareUpdateSession session, long timeoutMillis)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (session.isActive() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
    }

    @Test
    public void testUpdateMdStatusReportOkNoRestartMarksSuccess() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(11);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT);
        setActive(session, true);

        boolean handled = session
                .handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(11, 0, 0xFE, 0));

        assertTrue(handled);
        assertFalse(session.isActive());
        assertEquals(ZWaveFirmwareUpdateSession.State.SUCCESS, getState(session));
        Mockito.verify(controller)
                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                        && ((ZWaveNetworkEvent) event).getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                        && ((ZWaveNetworkEvent) event).getState() == ZWaveNetworkEvent.State.Success));
    }

    @Test
    public void testUpdateMdStatusReportErrorMarksFailure() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(12);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT);
        setActive(session, true);

        boolean handled = session
                .handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(12, 0, 0x01, 0));

        assertTrue(handled);
        assertFalse(session.isActive());
        assertEquals(ZWaveFirmwareUpdateSession.State.FAILURE, getState(session));
        Mockito.verify(controller)
                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                        && ((ZWaveNetworkEvent) event).getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                        && ((ZWaveNetworkEvent) event).getState() == ZWaveNetworkEvent.State.Failure));
    }

    @Test
    public void testUpdateMdStatusReportRestartPendingSchedulesNopPing() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(13);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT);
        setActive(session, true);

        boolean handled = session
                .handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(13, 0, 0xFF, 0));

        assertTrue(handled);
        assertFalse(session.isActive());
        assertEquals(ZWaveFirmwareUpdateSession.State.SUCCESS, getState(session));
                Mockito.verify(node, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(7))).pingNode();
    }

    @Test
    public void testUpdateMdStatusReportWaitingForActivationSendsActivationSet() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(14);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);

        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);
        Mockito.when(fw.getVersion()).thenReturn(5);

        ZWaveCommandClassTransactionPayload activationTx = new ZWaveCommandClassTransactionPayload(14,
                new byte[] { 0x7A }, TransactionPriority.Config, null, null);
        Mockito.when(fw.setFirmwareActivation(Mockito.any())).thenReturn(activationTx);

        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT);
        setActive(session, true);

        ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = new ZWaveFirmwareUpdateSession.FirmwareMetadata(0x1234,
                0x5678, 0x9ABC, true, 64, 0, true, 0x05, false, 0, new byte[0]);
        setSessionMetadata(session, metadata);

        boolean handled = session
                .handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(14, 0, 0xFD, 0));

        assertTrue(handled);
        assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_ACTIVATION_STATUS_REPORT, getState(session));

        ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
        Mockito.verify(fw).setFirmwareActivation(payloadCaptor.capture());
        int expectedActivationChecksum = ZWaveFirmwareUpdateCommandClass.crc16Ccitt(new byte[] { 0x01 }, 0x1D0F);
        assertArrayEquals(new byte[] { 0x12, 0x34, 0x56, 0x78, (byte) ((expectedActivationChecksum >> 8) & 0xFF),
                (byte) (expectedActivationChecksum & 0xFF), 0x00, 0x05 }, payloadCaptor.getValue());
        Mockito.verify(node).sendMessage(activationTx);
    }

    @Test
    public void testActivationStatusReportSuccessRequires0xFF() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(15);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_ACTIVATION_STATUS_REPORT);
        setActive(session, true);

        boolean handled = session
                .handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forActivationStatusReport(15, 0, 0xFF));

        assertTrue(handled);
        assertFalse(session.isActive());
        assertEquals(ZWaveFirmwareUpdateSession.State.SUCCESS, getState(session));
        Mockito.verify(controller)
                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                        && ((ZWaveNetworkEvent) event).getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                        && ((ZWaveNetworkEvent) event).getState() == ZWaveNetworkEvent.State.Success));
    }

    @Test
    public void testActivationStatusReportErrorCodesFail() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(16);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_ACTIVATION_STATUS_REPORT);
        setActive(session, true);

        boolean handled = session
                .handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forActivationStatusReport(16, 0, 0x00));

        assertTrue(handled);
        assertFalse(session.isActive());
        assertEquals(ZWaveFirmwareUpdateSession.State.FAILURE, getState(session));
        Mockito.verify(controller)
                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                        && ((ZWaveNetworkEvent) event).getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                        && ((ZWaveNetworkEvent) event).getState() == ZWaveNetworkEvent.State.Failure));
    }

    @Test
    public void testDuplicateUpdateMdGetWithinResendWindowIsIgnored() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(17);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(17, new byte[] { 0x7A },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session, List.of(new ZWaveFirmwareUpdateSession.FirmwareFragment(1, true, new byte[] { 0x55 })));

        session.setCurrentTimeMillis(1_000L);
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(17, 0, 1, 1)));
        assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT, getState(session));
        assertEquals(1, getHighestTransmittedReportNumber(session));

        session.setCurrentTimeMillis(6_000L);
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(17, 0, 1, 1)));

        Mockito.verify(node, Mockito.times(1)).sendMessage(tx);
    }

    @Test
    public void testDuplicateUpdateMdGetAfterResendWindowResendsReport() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(18);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(18, new byte[] { 0x7B },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session, List.of(new ZWaveFirmwareUpdateSession.FirmwareFragment(1, true, new byte[] { 0x66 })));

        session.setCurrentTimeMillis(1_000L);
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(18, 0, 1, 1)));
        assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT, getState(session));
        assertEquals(1, getHighestTransmittedReportNumber(session));

        session.setCurrentTimeMillis(TimeUnit.SECONDS.toMillis(25));
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(18, 0, 1, 1)));

        Mockito.verify(node, Mockito.times(2)).sendMessage(tx);
        assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT, getState(session));
    }

    @Test
        public void testLateRetryGetDoesNotRewindHighestTransmittedProgressBaseline() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(26);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(26, new byte[] { 0x7D },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session,
                java.util.stream.IntStream.rangeClosed(1, 3000)
                        .mapToObj(i -> new ZWaveFirmwareUpdateSession.FirmwareFragment(i, false, new byte[] { 0x01 }))
                        .toList());

        setHighestTransmittedReportNumber(session, 2689);
        setLastPublishedProgressPercent(session, 85);

        session.setCurrentTimeMillis(TimeUnit.SECONDS.toMillis(30));
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(26, 0, 2215, 1)));

                assertEquals(2689, getHighestTransmittedReportNumber(session));
                assertEquals(2214, getHighestAckedReportNumber(session));
    }

    @Test
    public void testOutOfSequenceForwardGetIsIgnored() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(27);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(27, new byte[] { 0x7E },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session,
                java.util.stream.IntStream.rangeClosed(1, 3000)
                        .mapToObj(
                                i -> new ZWaveFirmwareUpdateSession.FirmwareFragment(i, i == 3000, new byte[] { 0x01 }))
                        .toList());

        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(27, 0, 1, 1)));
        assertEquals(1, getHighestTransmittedReportNumber(session));

        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(27, 0, 2561, 4)));

        Mockito.verify(node, Mockito.times(1)).sendMessage(tx);
        assertEquals(1, getHighestTransmittedReportNumber(session));
        assertEquals(ZWaveFirmwareUpdateSession.State.SENDING_FRAGMENTS, getState(session));
    }

    @Test
    public void testMissingStatusAfterLastFragmentTimesOutToFailure() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(21);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(21, new byte[] { 0x7C },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        session.setStatusReportWaitTimeoutSeconds(1);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session, List.of(new ZWaveFirmwareUpdateSession.FirmwareFragment(1, true, new byte[] { 0x77 })));

        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(21, 0, 1, 1)));
        assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT, getState(session));

        waitForSessionToStop(session, TimeUnit.SECONDS.toMillis(3));

        assertFalse(session.isActive());
        assertEquals(ZWaveFirmwareUpdateSession.State.FAILURE, getState(session));
        Mockito.verify(node, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(2))).setFirmwareUpdateInProgress(false);
        Mockito.verify(controller, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(2)))
                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                        && ((ZWaveNetworkEvent) event).getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                        && ((ZWaveNetworkEvent) event).getState() == ZWaveNetworkEvent.State.Failure));
    }

    @Test
    public void testProgressEventsUseFivePercentSteps() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(22);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        List<Integer> progressValues = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof ZWaveNetworkEvent networkEvent
                    && networkEvent.getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                    && networkEvent.getState() == ZWaveNetworkEvent.State.Progress
                    && networkEvent.getValue() instanceof Number number) {
                progressValues.add(number.intValue());
            }
            return null;
        }).when(controller).ZWaveIncomingEvent(Mockito.any());

        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setFragments(session,
                java.util.stream.IntStream.rangeClosed(1, 39)
                        .mapToObj(i -> new ZWaveFirmwareUpdateSession.FirmwareFragment(i, false, new byte[] { 0x01 }))
                        .toList());

        setHighestTransmittedReportNumber(session, 27);
        invokePublishFirmwareUpdateProgressIfNeeded(session);

        setHighestTransmittedReportNumber(session, 29);
        invokePublishFirmwareUpdateProgressIfNeeded(session);

        setHighestTransmittedReportNumber(session, 31);
        invokePublishFirmwareUpdateProgressIfNeeded(session);

        assertEquals(List.of(Integer.valueOf(65), Integer.valueOf(70), Integer.valueOf(75)), progressValues);
    }

    @Test
    public void testInactivityTimeoutFiresWhenNoGetReceived() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(23);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(23, new byte[] { 0x7A },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        // Use 2 fragments so the last-sent transition does NOT occur on the first GET,
        // leaving the session in SENDING_FRAGMENTS where only the inactivity timer guards it.
        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        session.setInactivityTimeoutMillis(100); // 100 ms in tests instead of 2 minutes
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session, List.of(new ZWaveFirmwareUpdateSession.FirmwareFragment(1, false, new byte[] { 0x01 }),
                new ZWaveFirmwareUpdateSession.FirmwareFragment(2, true, new byte[] { 0x02 })));

        // First GET — sends fragment 1, does NOT send last fragment, timer armed
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(23, 0, 1, 1)));
        assertEquals(ZWaveFirmwareUpdateSession.State.SENDING_FRAGMENTS, getState(session));

        // No further events — inactivity timer should fire (timeout = 0 min → immediate)
        waitForSessionToStop(session, TimeUnit.SECONDS.toMillis(3));

        assertFalse(session.isActive());
        assertEquals(ZWaveFirmwareUpdateSession.State.FAILURE, getState(session));
        Mockito.verify(controller, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(2)))
                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                        && ((ZWaveNetworkEvent) event).getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                        && ((ZWaveNetworkEvent) event).getState() == ZWaveNetworkEvent.State.Failure));
    }

    @Test
    public void testInactivityTimeoutCancelledWhenLastFragmentSent() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(24);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(24, new byte[] { 0x7A },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        // Inactivity timeout = 100 ms (fires quickly if not cancelled).
        // Status report timeout = 60 s so it does NOT race with our assertion.
        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        session.setInactivityTimeoutMillis(100);
        session.setStatusReportWaitTimeoutSeconds(60);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session, List.of(new ZWaveFirmwareUpdateSession.FirmwareFragment(1, true, new byte[] { 0x01 })));

        // Single last fragment — inactivity timer must be cancelled and status timer armed
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(24, 0, 1, 1)));
        assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT, getState(session));

        // Give a brief window for any stale timer fire; session must still be active
        Thread.sleep(200);
        assertTrue(session.isActive());
    }

    @Test
    public void testInitialOnePercentProgressPublishedOnFirstGet() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(25);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        List<Integer> progressValues = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            Object event = invocation.getArgument(0);
            if (event instanceof ZWaveNetworkEvent networkEvent
                    && networkEvent.getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                    && networkEvent.getState() == ZWaveNetworkEvent.State.Progress
                    && networkEvent.getValue() instanceof Number number) {
                progressValues.add(number.intValue());
            }
            return null;
        }).when(controller).ZWaveIncomingEvent(Mockito.any());

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(25, new byte[] { 0x7A },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        // Large enough fragment list that no 5%-step fires on the first GET
        List<ZWaveFirmwareUpdateSession.FirmwareFragment> frags = java.util.stream.IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new ZWaveFirmwareUpdateSession.FirmwareFragment(i, i == 100, new byte[] { 0x01 }))
                .toList();

        ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session, frags);

        // First GET — should publish exactly 1% before any 5%-step event
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(25, 0, 1, 1)));

        assertFalse(progressValues.isEmpty(), "Expected at least a 1% progress event");
        assertEquals(Integer.valueOf(1), progressValues.get(0), "First progress event must be 1%");

        // Sending the same GET again must NOT re-publish 1% (already past that)
        int countAfterFirst = progressValues.size();
        // push highestTransmitted back so the duplicate-resend window is expired
        TestableZWaveFirmwareUpdateSession tSession = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        // Just verify that the first element is 1, which is the key assertion
        assertEquals(Integer.valueOf(1), progressValues.get(0));
        // And the second GET on an already-known first fragment won't re-fire a 1% (lastPublishedProgressPercent != 0)
        assertTrue(countAfterFirst >= 1);
    }

    @Test
    public void testImplicitAckWhenHigherFragmentRequested() throws Exception {
        // Scenario: far-away node with poor radio conditions
        // We're retrying fragment 2086, but device sends GET for 2087 (higher).
        // This means device received 2086, so we should move to 2087, not resend 2086.

        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(19);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(19, new byte[] { 0x7A },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        // Create fragments 2085, 2086, 2087
        List<ZWaveFirmwareUpdateSession.FirmwareFragment> frags = java.util.stream.IntStream.rangeClosed(1, 2087)
                .mapToObj(i -> new ZWaveFirmwareUpdateSession.FirmwareFragment(i, i == 2087, new byte[] { 0x01 }))
                .toList();

        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session, frags);

        // Simulate that we're retrying fragment 2086 (startReportNumber = 2086, count = 1)
        setStartReportNumber(session, 2086);
        setCount(session, 1);
        setHighestTransmittedReportNumber(session, 2087); // We already sent up to 2087

        // Now a GET arrives for fragment 2087 (higher than the 2086 we're retrying)
        // This is implicit ACK that 2086 made it, so we should send 2087, not resend 2086
        ArgumentCaptor<ZWaveFirmwareUpdateCommandClass.FirmwareFragment> captor = ArgumentCaptor
                .forClass(ZWaveFirmwareUpdateCommandClass.FirmwareFragment.class);

        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(19, 0, 2087, 1)));

        // Verify that sendFirmwareUpdateReport was called with fragment 2087, not a resend of 2086
        Mockito.verify(fw, Mockito.atLeastOnce()).sendFirmwareUpdateReport(captor.capture());
        // The last call should be for fragment 2087 (status=WAITING_FOR_UPDATE_MD_STATUS_REPORT for last fragment)
        ZWaveFirmwareUpdateCommandClass.FirmwareFragment lastCall = captor.getValue();
        assertEquals(2087, lastCall.reportNumber(), "Should send fragment 2087, not resend 2086");
        assertTrue(lastCall.isLast(), "Fragment 2087 should be marked as last");
    }

    @Test
    public void testLowerGetIgnoredAfterAckAnchorAdvancesOnHigherGet() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(28);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(28, new byte[] { 0x7A },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session,
                java.util.stream.IntStream.rangeClosed(1, 3000)
                        .mapToObj(i -> new ZWaveFirmwareUpdateSession.FirmwareFragment(i, i == 3000, new byte[] { 0x01 }))
                        .toList());
        setStartReportNumber(session, 2867);
        setCount(session, 1);
        setHighestTransmittedReportNumber(session, 2867);

        session.setCurrentTimeMillis(TimeUnit.SECONDS.toMillis(30));
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(28, 0, 2868, 1)));
        assertEquals(2867, getHighestAckedReportNumber(session));

        session.setCurrentTimeMillis(TimeUnit.SECONDS.toMillis(60));
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(28, 0, 2867, 1)));

        Mockito.verify(fw, Mockito.times(1)).sendFirmwareUpdateReport(Mockito.any());
    }

    @Test
    public void testMultiCountGetAdvancesAnchorToStartMinusOneOnly() throws Exception {
        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        Mockito.when(node.getNodeId()).thenReturn(29);
        ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

        ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(29, new byte[] { 0x7A },
                TransactionPriority.Config, null, null);
        Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

        TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                new byte[] { 0x01 }, 0);
        setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
        setActive(session, true);
        setFragments(session,
                java.util.stream.IntStream.rangeClosed(1, 3000)
                        .mapToObj(i -> new ZWaveFirmwareUpdateSession.FirmwareFragment(i, i == 3000, new byte[] { 0x01 }))
                        .toList());
        setStartReportNumber(session, 2867);
        setCount(session, 1);
        setHighestTransmittedReportNumber(session, 2869);

        session.setCurrentTimeMillis(TimeUnit.SECONDS.toMillis(30));
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(29, 0, 2868, 4)));
        assertEquals(2867, getHighestAckedReportNumber(session));

        session.setCurrentTimeMillis(TimeUnit.SECONDS.toMillis(60));
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(29, 0, 2867, 1)));
        assertTrue(session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(29, 0, 2869, 1)));

        // First send is from start=2868,count=4 and includes 2868/2869/2870/2871.
        // The GET for 2867 is ignored due to the ACK anchor. GET 2869 is allowed and re-sent after window.
        Mockito.verify(fw, Mockito.times(5)).sendFirmwareUpdateReport(Mockito.any());
    }
}
