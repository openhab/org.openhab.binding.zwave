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
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Unit tests for for the Firmware Update Session, which is responsible for
 * managing the state of a firmware update process for a single node, including
 * parsing metadata, building requests, and handling events related to the
 * firmware update process. These tests focus on the parsing of metadata from
 * the device, building of request payloads, and handling of status reports.
 * {@link ZWaveFirmwareUpdateSession}.
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveFirmwareUpdateSessionTest {

        private static class TestableZWaveFirmwareUpdateSession extends ZWaveFirmwareUpdateSession {
                private long nowMillis;
                private int statusReportWaitTimeoutSeconds = 30;

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

                @Override
                protected long currentTimeMillis() {
                        return nowMillis;
                }

                @Override
                protected int getStatusReportWaitTimeoutSeconds() {
                        return statusReportWaitTimeoutSeconds;
                }
        }

        private ZWaveFirmwareUpdateSession newSession() {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
                Mockito.when(node.getNodeId()).thenReturn(1);
                return new ZWaveFirmwareUpdateSession(node, controller, new byte[] { 0x01, 0x02 }, 0);
        }

        private void setState(ZWaveFirmwareUpdateSession session, ZWaveFirmwareUpdateSession.State state)
                        throws Exception {
                Method method = ZWaveFirmwareUpdateSession.class.getDeclaredMethod("handleEvent", Object.class);
                Method unused = method;
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

                byte[] payload = new byte[] {
                                0x12, 0x34, // manufacturer
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

                assertArrayEquals(new byte[] {
                                0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC,
                                0x00, 0x01, (byte) 0xF4,
                                0x07,
                                0x05
                }, metadata.report3Payload());

                byte[] requestPayload = buildMdRequestGet(session, metadata);
                assertArrayEquals(new byte[] {
                                0x12, 0x34, 0x56, 0x78,
                                (byte) ((expectedSessionChecksum() >> 8) & 0xFF),
                                (byte) (expectedSessionChecksum() & 0xFF),
                                0x00, 0x01, (byte) 0xF4,
                                0x07,
                                0x05
                }, requestPayload);
        }

        @Test
        public void testParseMetadataV5HasHardwareAndZeroActivationInReport3() throws Exception {
                ZWaveFirmwareUpdateSession session = newSession();

                byte[] payload = new byte[] {
                                0x02, 0x7A,
                                0x00, 0x03,
                                0x00, 0x00,
                                (byte) 0xFF,
                                0x00,
                                0x00, 0x28,
                                0x02
                };

                ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = (ZWaveFirmwareUpdateSession.FirmwareMetadata) parseMetadata(
                                session, payload);

                assertEquals(0, metadata.requestFlags());
                assertTrue(metadata.hardwareVersionPresent());
                assertEquals(0x02, metadata.hardwareVersion());
                assertFalse(metadata.ccFunctionalityPresent());

                assertArrayEquals(new byte[] {
                                0x02, 0x7A, 0x00, 0x03, 0x00, 0x00,
                                0x00, 0x00, 0x28,
                                0x00,
                                0x02
                }, metadata.report3Payload());

                byte[] requestPayload = buildMdRequestGet(session, metadata);
                assertArrayEquals(new byte[] {
                                0x02, 0x7A, 0x00, 0x03,
                                (byte) ((expectedSessionChecksum() >> 8) & 0xFF),
                                (byte) (expectedSessionChecksum() & 0xFF),
                                0x00, 0x00, 0x28,
                                0x00,
                                0x02
                }, requestPayload);
        }

        @Test
        public void testParseMetadataV1V2UsesOnlyFirstSixBytesForReport3() throws Exception {
                ZWaveFirmwareUpdateSession session = newSession();

                byte[] payload = new byte[] {
                                0x01, 0x02,
                                0x03, 0x04,
                                0x05, 0x06
                };

                ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = (ZWaveFirmwareUpdateSession.FirmwareMetadata) parseMetadata(
                                session, payload);

                assertTrue(metadata.upgradable());
                assertEquals(32, metadata.maxFragmentSize());
                assertFalse(metadata.hardwareVersionPresent());
                assertFalse(metadata.ccFunctionalityPresent());
                assertEquals(0, metadata.requestFlags());

                assertArrayEquals(new byte[] {
                                0x01, 0x02, 0x03, 0x04, 0x05, 0x06
                }, metadata.report3Payload());

                byte[] requestPayload = buildMdRequestGet(session, metadata);
                assertArrayEquals(new byte[] {
                                0x01, 0x02, 0x03, 0x04,
                                (byte) ((expectedSessionChecksum() >> 8) & 0xFF),
                                (byte) (expectedSessionChecksum() & 0xFF)
                }, requestPayload);
        }

        @Test
        public void testParseMetadataV3BuildsReport3WithTargetAndFragmentOnly() throws Exception {
                ZWaveFirmwareUpdateSession session = newSession();

                byte[] payload = new byte[] {
                                0x0A, 0x0B,
                                0x0C, 0x0D,
                                0x0E, 0x0F,
                                0x01,
                                0x00,
                                0x00, 0x40
                };

                ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = (ZWaveFirmwareUpdateSession.FirmwareMetadata) parseMetadata(
                                session, payload);

                assertTrue(metadata.upgradable());
                assertFalse(metadata.hardwareVersionPresent());
                assertFalse(metadata.ccFunctionalityPresent());
                assertEquals(0, metadata.requestFlags());

                assertArrayEquals(new byte[] {
                                0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F,
                                0x00, 0x00, 0x40
                }, metadata.report3Payload());

                byte[] requestPayload = buildMdRequestGet(session, metadata);
                assertArrayEquals(new byte[] {
                                0x0A, 0x0B, 0x0C, 0x0D,
                                (byte) ((expectedSessionChecksum() >> 8) & 0xFF),
                                (byte) (expectedSessionChecksum() & 0xFF),
                                0x00, 0x00, 0x40
                }, requestPayload);
        }

        @Test
        public void testParseMetadataV6UsesSingleFlagsByteForFunctionalityOnly() throws Exception {
                ZWaveFirmwareUpdateSession session = newSession();

                byte[] payload = new byte[] {
                                0x01, 0x02,
                                0x03, 0x04,
                                0x05, 0x06,
                                0x01,
                                0x00,
                                0x00, 0x30,
                                0x09,
                                0x01 // v6 flags byte: functionality only
                };

                ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = (ZWaveFirmwareUpdateSession.FirmwareMetadata) parseMetadata(
                                session, payload);

                assertTrue(metadata.hardwareVersionPresent());
                assertTrue(metadata.ccFunctionalityPresent());
                assertEquals(0, metadata.requestFlags());

                assertArrayEquals(new byte[] {
                                0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                                0x00, 0x00, 0x30,
                                0x00,
                                0x09
                }, metadata.report3Payload());
        }

        @Test
        public void testParseMetadataRejectsInvalidAdditionalTargetLength() throws Exception {
                ZWaveFirmwareUpdateSession session = newSession();

                byte[] payload = new byte[] {
                                0x00, 0x01,
                                0x00, 0x02,
                                0x00, 0x03,
                                0x01,
                                0x02,
                                0x00, 0x20,
                                0x55
                };

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
                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_MD_REPORT);

                boolean handled = session.handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forMDReport(7, 0,
                                new byte[] { 0x01, 0x02, 0x03 }));

                assertTrue(handled);
                assertFalse(session.isActive());
                Mockito.verify(controller)
                                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                                                && ((ZWaveNetworkEvent) event)
                                                                .getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                                                && ((ZWaveNetworkEvent) event)
                                                                .getState() == ZWaveNetworkEvent.State.Failure));
        }

        @Test
        public void testHandleMetadataReportNonUpgradableNotifiesFailureEvent() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(8);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_MD_REPORT);

                byte[] payload = new byte[] {
                                0x01, 0x02,
                                0x03, 0x04,
                                0x05, 0x06,
                                0x00,
                                0x00,
                                0x00, 0x20
                };

                boolean handled = session
                                .handleEvent(ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forMDReport(8, 0, payload));

                assertTrue(handled);
                assertFalse(session.isActive());
                Mockito.verify(controller)
                                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                                                && ((ZWaveNetworkEvent) event)
                                                                .getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                                                && ((ZWaveNetworkEvent) event)
                                                                .getState() == ZWaveNetworkEvent.State.Failure));
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
                        ZWaveFirmwareUpdateSession.FirmwareMetadata metadata)
                        throws Exception {
                java.lang.reflect.Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("sessionMetadata");
                field.setAccessible(true);
                field.set(session, metadata);
        }

        private void setFragments(ZWaveFirmwareUpdateSession session, List<ZWaveFirmwareUpdateSession.FirmwareFragment> fragments)
                        throws Exception {
                Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("fragments");
                field.setAccessible(true);
                field.set(session, fragments);
        }

        private int getHighestTransmittedReportNumber(ZWaveFirmwareUpdateSession session) throws Exception {
                Field field = ZWaveFirmwareUpdateSession.class.getDeclaredField("highestTransmittedReportNumber");
                field.setAccessible(true);
                return field.getInt(session);
        }

        private void waitForSessionToStop(ZWaveFirmwareUpdateSession session, long timeoutMillis) throws InterruptedException {
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

                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT);
                setActive(session, true);

                boolean handled = session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(11, 0, 0xFE, 0));

                assertTrue(handled);
                assertFalse(session.isActive());
                assertEquals(ZWaveFirmwareUpdateSession.State.SUCCESS, getState(session));
                Mockito.verify(controller)
                                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                                                && ((ZWaveNetworkEvent) event)
                                                                .getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                                                && ((ZWaveNetworkEvent) event)
                                                                .getState() == ZWaveNetworkEvent.State.Success));
        }

        @Test
        public void testUpdateMdStatusReportErrorMarksFailure() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(12);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT);
                setActive(session, true);

                boolean handled = session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(12, 0, 0x01, 0));

                assertTrue(handled);
                assertFalse(session.isActive());
                assertEquals(ZWaveFirmwareUpdateSession.State.FAILURE, getState(session));
                Mockito.verify(controller)
                                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                                                && ((ZWaveNetworkEvent) event)
                                                                .getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                                                && ((ZWaveNetworkEvent) event)
                                                                .getState() == ZWaveNetworkEvent.State.Failure));
        }

        @Test
        public void testUpdateMdStatusReportErrorDuringSendingFragmentsMarksFailure() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(19);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.SENDING_FRAGMENTS);
                setActive(session, true);

                boolean handled = session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(19, 0, 0x01, 0));

                assertTrue(handled);
                assertFalse(session.isActive());
                assertEquals(ZWaveFirmwareUpdateSession.State.FAILURE, getState(session));
                Mockito.verify(controller)
                                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                                                && ((ZWaveNetworkEvent) event)
                                                                .getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                                                && ((ZWaveNetworkEvent) event)
                                                                .getState() == ZWaveNetworkEvent.State.Failure));
        }

        @Test
        public void testUpdateMdStatusReportOkNoRestartDuringWaitingForUpdateMdGetMarksSuccess() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(20);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
                setActive(session, true);

                boolean handled = session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(20, 0, 0xFE, 0));

                assertTrue(handled);
                assertFalse(session.isActive());
                assertEquals(ZWaveFirmwareUpdateSession.State.SUCCESS, getState(session));
                Mockito.verify(controller)
                                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                                                && ((ZWaveNetworkEvent) event)
                                                                .getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                                                && ((ZWaveNetworkEvent) event)
                                                                .getState() == ZWaveNetworkEvent.State.Success));
        }

        @Test
        public void testUpdateMdStatusReportRestartPendingSchedulesNopPing() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(13);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT);
                setActive(session, true);

                boolean handled = session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(13, 0, 0xFF, 0));

                assertTrue(handled);
                assertFalse(session.isActive());
                assertEquals(ZWaveFirmwareUpdateSession.State.SUCCESS, getState(session));
                Mockito.verify(node, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(1))).pingNode();
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

                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT);
                setActive(session, true);

                ZWaveFirmwareUpdateSession.FirmwareMetadata metadata = new ZWaveFirmwareUpdateSession.FirmwareMetadata(
                                0x1234, 0x5678, 0x9ABC,
                                true, 64, 0,
                                true, 0x05,
                                false, 0,
                                new byte[0]);
                setSessionMetadata(session, metadata);

                boolean handled = session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdStatusReport(14, 0, 0xFD, 0));

                assertTrue(handled);
                assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_ACTIVATION_STATUS_REPORT, getState(session));

                ArgumentCaptor<byte[]> payloadCaptor = ArgumentCaptor.forClass(byte[].class);
                Mockito.verify(fw).setFirmwareActivation(payloadCaptor.capture());
                int expectedActivationChecksum = ZWaveFirmwareUpdateCommandClass.crc16Ccitt(new byte[] { 0x01 },
                                0x1D0F);
                assertArrayEquals(new byte[] {
                                0x12, 0x34, 0x56, 0x78,
                                (byte) ((expectedActivationChecksum >> 8) & 0xFF),
                                (byte) (expectedActivationChecksum & 0xFF),
                                0x00, 0x05
                }, payloadCaptor.getValue());
                Mockito.verify(node).sendMessage(activationTx);
        }

        @Test
        public void testActivationStatusReportSuccessRequires0xFF() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(15);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_ACTIVATION_STATUS_REPORT);
                setActive(session, true);

                boolean handled = session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forActivationStatusReport(15, 0, 0xFF));

                assertTrue(handled);
                assertFalse(session.isActive());
                assertEquals(ZWaveFirmwareUpdateSession.State.SUCCESS, getState(session));
                Mockito.verify(controller)
                                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                                                && ((ZWaveNetworkEvent) event)
                                                                .getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                                                && ((ZWaveNetworkEvent) event)
                                                                .getState() == ZWaveNetworkEvent.State.Success));
        }

        @Test
        public void testActivationStatusReportErrorCodesFail() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(16);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);

                ZWaveFirmwareUpdateSession session = new ZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_ACTIVATION_STATUS_REPORT);
                setActive(session, true);

                boolean handled = session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forActivationStatusReport(16, 0, 0x00));

                assertTrue(handled);
                assertFalse(session.isActive());
                assertEquals(ZWaveFirmwareUpdateSession.State.FAILURE, getState(session));
                Mockito.verify(controller)
                                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                                                && ((ZWaveNetworkEvent) event)
                                                                .getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                                                && ((ZWaveNetworkEvent) event)
                                                                .getState() == ZWaveNetworkEvent.State.Failure));
        }

        @Test
        public void testDuplicateUpdateMdGetWithinResendWindowIsIgnored() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(17);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
                ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
                Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

                ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(17,
                                new byte[] { 0x7A }, TransactionPriority.Config, null, null);
                Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

                TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
                setActive(session, true);
                setFragments(session, List.of(new ZWaveFirmwareUpdateSession.FirmwareFragment(1, true,
                                new byte[] { 0x55 })));

                session.setCurrentTimeMillis(1_000L);
                assertTrue(session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(17, 0, 1, 1)));
                assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT, getState(session));
                assertEquals(1, getHighestTransmittedReportNumber(session));

                session.setCurrentTimeMillis(6_000L);
                assertTrue(session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(17, 0, 1, 1)));

                Mockito.verify(node, Mockito.times(1)).sendMessage(tx);
        }

        @Test
        public void testDuplicateUpdateMdGetAfterResendWindowResendsReport() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(18);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
                ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
                Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

                ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(18,
                                new byte[] { 0x7B }, TransactionPriority.Config, null, null);
                Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

                TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
                setActive(session, true);
                setFragments(session, List.of(new ZWaveFirmwareUpdateSession.FirmwareFragment(1, true,
                                new byte[] { 0x66 })));

                session.setCurrentTimeMillis(1_000L);
                assertTrue(session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(18, 0, 1, 1)));
                assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT, getState(session));
                assertEquals(1, getHighestTransmittedReportNumber(session));

                session.setCurrentTimeMillis(TimeUnit.SECONDS.toMillis(25));
                assertTrue(session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(18, 0, 1, 1)));

                Mockito.verify(node, Mockito.times(2)).sendMessage(tx);
                assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT, getState(session));
        }

        @Test
        public void testMissingStatusAfterLastFragmentTimesOutToFailure() throws Exception {
                ZWaveNode node = Mockito.mock(ZWaveNode.class);
                Mockito.when(node.getNodeId()).thenReturn(21);
                ZWaveControllerHandler controller = Mockito.mock(ZWaveControllerHandler.class);
                ZWaveFirmwareUpdateCommandClass fw = Mockito.mock(ZWaveFirmwareUpdateCommandClass.class);
                Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD)).thenReturn(fw);

                ZWaveCommandClassTransactionPayload tx = new ZWaveCommandClassTransactionPayload(21,
                                new byte[] { 0x7C }, TransactionPriority.Config, null, null);
                Mockito.when(fw.sendFirmwareUpdateReport(Mockito.any())).thenReturn(tx);

                TestableZWaveFirmwareUpdateSession session = new TestableZWaveFirmwareUpdateSession(node, controller,
                                new byte[] { 0x01 }, 0);
                session.setStatusReportWaitTimeoutSeconds(1);
                setState(session, ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_GET);
                setActive(session, true);
                setFragments(session, List.of(new ZWaveFirmwareUpdateSession.FirmwareFragment(1, true,
                                new byte[] { 0x77 })));

                assertTrue(session.handleEvent(
                                ZWaveFirmwareUpdateSession.FirmwareUpdateEvent.forUpdateMdGet(21, 0, 1, 1)));
                assertEquals(ZWaveFirmwareUpdateSession.State.WAITING_FOR_UPDATE_MD_STATUS_REPORT, getState(session));

                waitForSessionToStop(session, TimeUnit.SECONDS.toMillis(3));

                assertFalse(session.isActive());
                assertEquals(ZWaveFirmwareUpdateSession.State.FAILURE, getState(session));
                Mockito.verify(node, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(2)))
                                .setFirmwareUpdateInProgress(false);
                Mockito.verify(controller, Mockito.timeout((int) TimeUnit.SECONDS.toMillis(2)))
                                .ZWaveIncomingEvent(Mockito.argThat(event -> event instanceof ZWaveNetworkEvent
                                                && ((ZWaveNetworkEvent) event)
                                                                .getEvent() == ZWaveNetworkEvent.Type.FirmwareUpdate
                                                && ((ZWaveNetworkEvent) event)
                                                                .getState() == ZWaveNetworkEvent.State.Failure));
        }
}
