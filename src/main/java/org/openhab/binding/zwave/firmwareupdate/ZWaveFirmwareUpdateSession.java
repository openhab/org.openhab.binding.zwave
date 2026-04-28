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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass.FirmwareUpdateActivationStatus;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveVersionCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass.FirmwareUpdateMdRequestStatus;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass.FirmwareUpdateMdStatusReport;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveTransactionCompletedEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZWaveFirmwareUpdateSession} class represents an active firmware update session for a Z-Wave node.
 * Handles the state and the steps of the Z-Wave firmware update process, including managing firmware fragments,
 * tracking progress, handling events, and applying timeout/retry behavior for robustness on noisy networks.
 *
 * @author Robert Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveFirmwareUpdateSession {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveFirmwareUpdateSession.class);
    private static final int DEFAULT_MAX_FRAGMENT_SIZE = 32; // Version 1 & 2 do not send this information.
    private static final int MAX_REPORT_NUMBER = 0x7FFF;
    private static final int MULTI_FRAGMENT_INTERFRAME_DELAY_MS = 35; // Per Spec
    private static final int IMAGE_CHECKSUM_INITIAL = 0x1D0F;
    private static final int STATUS_REPORT_WAIT_TIMEOUT_SECONDS = 30;
    private static final int PROGRESS_EVENT_STEP_PERCENT = 5;
    private static final long DUPLICATE_GET_RESEND_DELAY_MS = TimeUnit.SECONDS.toMillis(10);

    private int startReportNumber;
    private int count;
    private final ZWaveNode node;
    private final ZWaveControllerHandler controller;
    private final byte[] firmwareBytes;
    private final int firmwareChecksum;
    private final int firmwareTarget;

    private volatile boolean active = false;
    private volatile State state = State.IDLE;

    private List<FirmwareFragment> fragments = List.of();
    private @Nullable FirmwareMetadata sessionMetadata;
    private int highestAckedReportNumber = 0;
    private volatile int highestTransmittedReportNumber = 0;
    private int duplicateGetsForSentReport = 0;
    private int lastPublishedProgressPercent = 0;
    private final AtomicInteger statusReportTimeoutGeneration = new AtomicInteger(0);
    private final Map<Integer, Long> reportLastSentTimes = new ConcurrentHashMap<>();

    /**
     * Create a new firmware update session for the given node, controller, and firmware image.
     *
     * @param node the Z-Wave node for which the firmware update session is created
     * @param controller the Z-Wave controller handler
     * @param firmwareBytes the firmware image bytes
     * @param firmwareTarget the firmware target (0 = Z-Wave firmware, other values not supported)
     */
    public ZWaveFirmwareUpdateSession(ZWaveNode node, ZWaveControllerHandler controller, byte[] firmwareBytes,
            int firmwareTarget) {
        this.node = node;
        this.controller = controller;
        this.firmwareBytes = firmwareBytes;
        this.firmwareChecksum = ZWaveFirmwareUpdateCommandClass.crc16Ccitt(firmwareBytes, IMAGE_CHECKSUM_INITIAL);
        this.firmwareTarget = firmwareTarget;
    }

    /**
     * Start the firmware update session by requesting metadata from the device.
     * The session then progresses through its state machine as events are received.
     */
    public void start() {
        logger.info("NODE {}: Firmware session starting", node.getNodeId());
        active = true;
        state = State.WAITING_FOR_MD_REPORT;
        invalidateStatusReportTimeout();
        highestAckedReportNumber = 0;
        highestTransmittedReportNumber = 0;
        duplicateGetsForSentReport = 0;
        lastPublishedProgressPercent = 0;
        reportLastSentTimes.clear();

        requestMetadata(); // (1) Start the process by requesting devicemetadata.
        // Will be queued for battery devices. Not active update until the device wakes up.
    }

    public boolean isActive() {
        return active;
    }

    public State getState() {
        return state;
    }

    public void abort(String reason) {
        if (!active) {
            return;
        }

        failFirmwareUpdate("Firmware update session aborted: " + reason);
    }

    // Sends or queues the initial FIRMWARE_MD_GET to start the process
    private void requestMetadata() {
        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);

        ZWaveCommandClassTransactionPayload msg = fw.sendMDGetMessage();
        node.sendMessage(msg);

        logger.debug("NODE {}: Sent Firmware MD Get", node.getNodeId());
    }

    /**
     * Firmware update events used to progress through the firmware update process.
     * These are diverted from events normally received by the Z-Wave Thing Handler.
     */
    public static class FirmwareUpdateEvent extends ZWaveEvent {
        private final FirmwareEventType type;

        private final int reportNumber;
        private final int numReports;
        private final int status;
        private final int waitTime;
        private final byte[] payload;
        private final @Nullable Boolean resume;
        private final @Nullable Boolean nonSecure;

        private FirmwareUpdateEvent(int nodeId, int endpoint, FirmwareEventType type, int reportNumber, int numReports,
                int status, int waitTime, byte[] payload, @Nullable Boolean resume, @Nullable Boolean nonSecure) {
            super(nodeId, endpoint);
            this.type = type;
            this.reportNumber = reportNumber;
            this.numReports = numReports;
            this.status = status;
            this.waitTime = waitTime;
            this.payload = payload;
            this.resume = resume;
            this.nonSecure = nonSecure;
        }

        public static FirmwareUpdateEvent forMDReport(int nodeId, int endpoint, byte[] payload) {
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.MD_REPORT, -1, 0, 0, 0, payload, null,
                    null);
        }

        public static FirmwareUpdateEvent forUpdateMdRequestReport(int nodeId, int endpoint, int status,
                @Nullable Boolean resume, @Nullable Boolean nonSecure) {
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.UPDATE_MD_REQUEST_REPORT, -1, 0, status,
                    0, new byte[0], resume, nonSecure);
        }

        public static FirmwareUpdateEvent forUpdateMdGet(int nodeId, int endpoint, int reportNumber, int numReports) {
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.UPDATE_MD_GET, reportNumber, numReports,
                    0, 0, new byte[0], null, null);
        }

        public static FirmwareUpdateEvent forUpdateMdStatusReport(int nodeId, int endpoint, int status, int waitTime) {
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.UPDATE_MD_STATUS_REPORT, -1, 0, status,
                    waitTime, new byte[0], null, null);
        }

        public static FirmwareUpdateEvent forActivationStatusReport(int nodeId, int endpoint, int status) {
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.ACTIVATION_STATUS_REPORT, -1, 0, status,
                    0, new byte[0], null, null);
        }

        public FirmwareEventType getType() {
            return type;
        }

        public int getReportNumber() {
            return reportNumber;
        }

        public int getNumReports() {
            return numReports;
        }

        public byte[] getPayload() {
            return payload;
        }

        public @Nullable Boolean getResume() {
            return resume;
        }

        public @Nullable Boolean getNonSecure() {
            return nonSecure;
        }

        public int getStatus() {
            return status;
        }

        public int getWaitTime() {
            return waitTime;
        }
    }

    /**
     * Handle firmware update events, including failed transaction completions and firmware-specific reports.
     * Routes each event to the appropriate logic based on the current session state and event type.
     *
     * @param event firmware update related event, either {@link ZWaveTransactionCompletedEvent} or
     *            {@link FirmwareUpdateEvent}
     * @return true if the event was handled as part of the firmware update session, false otherwise
     */
    public boolean handleEvent(Object event) {
        if (event instanceof ZWaveTransactionCompletedEvent tcEvent) {
            // Handle failed Z-Wave transaction completion (!tcEvent)
            if (!tcEvent.getState() && tcEvent.getNodeId() == node.getNodeId()) {
                ZWaveTransaction completedTransaction = tcEvent.getCompletedTransaction();
                if (!isFirmwareUpdateTransaction(completedTransaction)) {
                    return false;
                }

                int txCommand = getFirmwareUpdateTransactionCommand(completedTransaction);

                if (state == State.WAITING_FOR_MD_REPORT
                        && txCommand == ZWaveFirmwareUpdateCommandClass.FIRMWARE_MD_GET) {
                    logger.debug("NODE {}: FIRMWARE_MD_GET transaction failed after all retries", node.getNodeId());
                    failFirmwareUpdate("FIRMWARE_MD_GET failed after all retries");
                    return true;
                }

                if (state == State.WAITING_FOR_UPDATE_MD_REQUEST_REPORT
                        && txCommand == ZWaveFirmwareUpdateCommandClass.FIRMWARE_UPDATE_MD_REQUEST_GET) {
                    logger.debug("NODE {}: FIRMWARE_UPDATE_MD_REQUEST_GET transaction failed after all retries",
                            node.getNodeId());
                    failFirmwareUpdate("FIRMWARE_UPDATE_MD_REQUEST_GET failed after all retries");
                    return true;
                }

                // If a firmware fragment transaction failed while sending fragments, attempt to
                // resend when outside the duplicate resend delay window.
                if ((state == State.SENDING_FRAGMENTS || state == State.WAITING_FOR_UPDATE_MD_GET)
                        && txCommand == ZWaveFirmwareUpdateCommandClass.FIRMWARE_UPDATE_MD_REPORT) {
                    int resendStart = Math.max(1, startReportNumber);
                    int resendCount = Math.max(1, count);
                    Long lastSentTime = reportLastSentTimes.get(resendStart);
                    long elapsedMillis = lastSentTime != null ? currentTimeMillis() - lastSentTime.longValue()
                            : Long.MAX_VALUE;

                    if (lastSentTime != null && elapsedMillis < DUPLICATE_GET_RESEND_DELAY_MS) {
                        logger.debug(
                                "NODE {}: Firmware fragment transaction failed/cancelled for fragment {}, but it was sent {}ms ago (<{}ms); skipping immediate requeue and waiting for next UPDATE_MD_GET",
                                node.getNodeId(), resendStart, elapsedMillis, DUPLICATE_GET_RESEND_DELAY_MS);
                        state = State.WAITING_FOR_UPDATE_MD_GET;
                        return true;
                    }

                    logger.debug(
                            "NODE {}: Firmware fragment transaction failed/cancelled; retrying fragment send from {} (count={})",
                            node.getNodeId(), resendStart, resendCount);
                    state = State.SENDING_FRAGMENTS;
                    sendNextFragment(resendStart, resendCount);
                    return true;
                }
            }
            return false;
        }

        if (!(event instanceof FirmwareUpdateEvent fwEvent)) {
            return false;
        }

        switch (fwEvent.getType()) {
            case MD_REPORT:
                return handleMetadataReport(fwEvent);

            case UPDATE_MD_REQUEST_REPORT:
                return handleUpdateMdRequestReport(fwEvent);

            case UPDATE_MD_GET:
                return handleUpdateMdGet(fwEvent);

            case UPDATE_MD_STATUS_REPORT:
                return handleUpdateMdStatusReport(fwEvent);

            case ACTIVATION_STATUS_REPORT:
                return handleActivationStatusReport(fwEvent);
            default:
                break;
        }

        return false;
    }

    private boolean isFirmwareUpdateTransaction(ZWaveTransaction transaction) {
        byte[] txPayload = transaction.getPayloadBuffer();
        return txPayload.length >= 2 && (txPayload[0] & 0xFF) == CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey();
    }

    private int getFirmwareUpdateTransactionCommand(ZWaveTransaction transaction) {
        byte[] txPayload = transaction.getPayloadBuffer();
        if (txPayload.length < 2) {
            return -1;
        }
        return txPayload[1] & 0xFF;
    }

    /**
     * Handle Metadata Report (2), the first report received after metadata is requested.
     * Parses metadata, prepares firmware fragments, and sends UPDATE_MD_REQUEST_GET (3) to continue.
     *
     * @param event the firmware update event containing the metadata report
     * @return true if the event was handled, false otherwise
     */
    private boolean handleMetadataReport(FirmwareUpdateEvent event) {
        if (state != State.WAITING_FOR_MD_REPORT) {
            return false;
        }

        logger.debug("NODE {}: Received Metadata Report", node.getNodeId());

        FirmwareMetadata metadata;
        try {
            // Parse metadata from the raw payload
            metadata = parseMetadata(event.getPayload());
        } catch (IllegalArgumentException e) {
            failFirmwareUpdate("Malformed metadata report payload: " + e.getMessage(), e.getMessage());
            return true;
        }

        if (event.getPayload().length >= 10 && !metadata.upgradable()) {
            failFirmwareUpdate("Metadata report indicates firmware is not upgradable", Integer.valueOf(0));
            return true;
        }

        logger.debug(
                "NODE {}: Metadata parsed: manufacturerId={}, firmwareId={}, checksum={}, maxFragmentSize={}, hwPresent={}, hwVersion={}, mappedFlags=0x{}",
                node.getNodeId(), metadata.manufacturerId(), metadata.firmwareId(), metadata.checksum(),
                metadata.maxFragmentSize(), metadata.hardwareVersionPresent(), metadata.hardwareVersion(),
                Integer.toHexString(metadata.requestFlags()));

        this.sessionMetadata = metadata;
        // Disable sleep on battery devices while the firmware update is active.
        node.setFirmwareUpdateInProgress(true);

        // Prepare fragments using maxFragmentSize
        if (!prepareFragments(metadata)) {
            return true;
        }

        // Build and send UPDATE_MD_REQUEST_GET (3)
        sendFirmwareUpdateMdRequestGet(metadata);

        state = State.WAITING_FOR_UPDATE_MD_REQUEST_REPORT;
        return true;
    }

    /**
     * Parse the raw metadata-report payload into a structured {@link FirmwareMetadata} object.
     * Handles legacy V1/V2 reports (6 bytes) and V3+ reports (10+ bytes), extracting manufacturer ID,
     * firmware ID, checksum, maximum fragment size, hardware version, and optional feature flags.
     * This parsed data is reused later when constructing request payloads.
     *
     * @param payload the raw payload from the MD Report
     * @return a {@link FirmwareMetadata} object containing the parsed metadata
     * @throws IllegalArgumentException if the payload is malformed or too short
     */
    private FirmwareMetadata parseMetadata(byte[] payload) {
        if (payload.length < 6) {
            throw new IllegalArgumentException("payload too short (need at least 6 bytes, got " + payload.length + ")");
        }

        int manufacturerId = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF);
        int firmwareId = ((payload[2] & 0xFF) << 8) | (payload[3] & 0xFF);
        int checksum = ((payload[4] & 0xFF) << 8) | (payload[5] & 0xFF);

        // V1/V2 only provide the first 6 bytes; assume upgradable and use default
        // max fragment size.
        if (payload.length == 6) {
            byte[] report3Payload = buildLegacyReport3Payload(manufacturerId, firmwareId, checksum, false, false,
                    DEFAULT_MAX_FRAGMENT_SIZE, false, 0, 0);

            return new FirmwareMetadata(manufacturerId, firmwareId, checksum, true, DEFAULT_MAX_FRAGMENT_SIZE, 0, false,
                    0, false, 0, report3Payload);
        }

        // V3+ metadata requires bytes 6..9.
        if (payload.length < 10) {
            throw new IllegalArgumentException(
                    "payload too short for v3+ metadata (need at least 10 bytes, got " + payload.length + ")");
        }

        boolean upgradable = (payload[6] & 0xFF) != 0;
        int additionalTargets = payload[7] & 0xFF;
        int maxFragmentSize = ((payload[8] & 0xFF) << 8) | (payload[9] & 0xFF);

        int index = 10 + (additionalTargets * 2);
        if (index > payload.length) {
            throw new IllegalArgumentException("additional target data exceeds payload length (targets="
                    + additionalTargets + ", payload=" + payload.length + ")");
        }

        int remaining = payload.length - index;
        int parsedVersion;
        if (remaining <= 0) {
            parsedVersion = 3;
        } else if (remaining == 1) {
            parsedVersion = 5;
        } else if (remaining == 2) {
            parsedVersion = 6;
        } else {
            parsedVersion = 7;
        }

        boolean hardwareVersionPresent = parsedVersion >= 5 && remaining >= 1;
        int hardwareVersion = hardwareVersionPresent ? payload[index] & 0xFF : 0;

        // V6+: one report-2 flags byte follows hardware version:
        // bit0=functionality, bit1=activation, bit2=non-secure, bit3=resume.
        Integer report2Flags = parsedVersion >= 6 && remaining >= 2 ? Integer.valueOf(payload[index + 1] & 0xFF) : null;

        boolean ccFunctionalityPresent = report2Flags != null && (report2Flags.intValue() & 0x01) != 0;

        int requestFlags = mapRequestFlags(report2Flags);

        byte[] report3Payload = buildLegacyReport3Payload(manufacturerId, firmwareId, checksum, parsedVersion >= 3,
                parsedVersion >= 4, maxFragmentSize, hardwareVersionPresent, hardwareVersion, requestFlags);

        return new FirmwareMetadata(manufacturerId, firmwareId, checksum, upgradable, maxFragmentSize,
                additionalTargets, hardwareVersionPresent, hardwareVersion, ccFunctionalityPresent, requestFlags,
                report3Payload);
    }

    private int getFirmwareUpdateMdVersion() {
        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);
        return fw != null ? fw.getVersion() : -1;
    }

    private int mapRequestFlags(@Nullable Integer report2Flags) {
        if (report2Flags == null) {
            return 0;
        }

        int source = report2Flags.intValue();
        int requestFlags = 0;

        // source bit3 -> request bit2 (resume)
        if ((source & 0x08) != 0) {
            requestFlags |= 0x04;
        }
        // source bit2 -> request bit1 (non-secure)
        if ((source & 0x04) != 0) {
            requestFlags |= 0x02;
        }
        // source bit1 -> request bit0 (activation required)
        if ((source & 0x02) != 0) {
            requestFlags |= 0x01;
        }

        return requestFlags;
    }

    private byte[] buildLegacyReport3Payload(int manufacturerId, int firmwareId, int checksum, boolean includeV3Fields,
            boolean includeReport3Flags, int maxFragmentSize, boolean hardwareVersionPresent, int hardwareVersion,
            int requestFlags) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write((manufacturerId >> 8) & 0xFF);
        out.write(manufacturerId & 0xFF);

        out.write((firmwareId >> 8) & 0xFF);
        out.write(firmwareId & 0xFF);

        out.write((checksum >> 8) & 0xFF);
        out.write(checksum & 0xFF);

        if (includeV3Fields) {
            // V3+: firmware target (always 0) + max fragment size.
            out.write(firmwareTarget & 0xFF);
            out.write((maxFragmentSize >> 8) & 0xFF);
            out.write(maxFragmentSize & 0xFF);

            // V4+: report3 includes flags byte before hardware version.
            if (includeReport3Flags) {
                out.write(requestFlags & 0xFF);
            }

            // V5+: hardware version follows report3 flags byte.
            if (hardwareVersionPresent) {
                out.write(hardwareVersion & 0xFF);
            }
        }
        return out.toByteArray();
    }

    public record FirmwareMetadata(int manufacturerId, int firmwareId, int checksum, boolean upgradable,
            int maxFragmentSize, int additionalTargets, boolean hardwareVersionPresent, int hardwareVersion,
            boolean ccFunctionalityPresent, int requestFlags, byte[] report3Payload) {
    }

    private byte[] buildFirmwareBaseData(FirmwareMetadata metadata, int ccVersion) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write((metadata.manufacturerId() >> 8) & 0xFF);
        out.write(metadata.manufacturerId() & 0xFF);

        out.write((metadata.firmwareId() >> 8) & 0xFF);
        out.write(metadata.firmwareId() & 0xFF);

        out.write((firmwareChecksum >> 8) & 0xFF);
        out.write(firmwareChecksum & 0xFF);

        out.write(firmwareTarget & 0xFF);

        if (ccVersion >= 5 && metadata.hardwareVersionPresent()) {
            out.write(metadata.hardwareVersion() & 0xFF);
        }

        return out.toByteArray();
    }

    private byte[] buildMdRequestGet(FirmwareMetadata md) {
        byte[] payload = Arrays.copyOf(md.report3Payload(), md.report3Payload().length);
        if (payload.length >= 6) {
            payload[4] = (byte) ((firmwareChecksum >> 8) & 0xFF);
            payload[5] = (byte) (firmwareChecksum & 0xFF);
        }
        return payload;
    }

    /**
     * Prepares the firmware fragments for transmission based on the metadata.
     * Each fragment contains a portion of the firmware data along with its report
     * number and a flag indicating if it is the last fragment.
     *
     * @param metadata the firmware metadata containing information about the
     *            firmware update
     * @return true if fragments were successfully prepared, false otherwise
     */
    private boolean prepareFragments(FirmwareMetadata metadata) {
        fragments = new ArrayList<>();

        // maxFragmentSize specifies the firmware DATA bytes per fragment only;
        // the CC/CMD/reportNum/CRC overhead is added by the serializer on top of this.
        int usable = metadata.maxFragmentSize();

        if (usable <= 0) {
            failFirmwareUpdate(
                    "Max fragment size too small for firmware update (max=" + metadata.maxFragmentSize() + ")",
                    Integer.valueOf(metadata.maxFragmentSize()));
            return false;
        }

        int offset = 0;
        int reportNumber = 1;

        while (offset < firmwareBytes.length) {
            if (reportNumber > MAX_REPORT_NUMBER) {
                failFirmwareUpdate("Firmware requires more than " + MAX_REPORT_NUMBER + " reports",
                        Integer.valueOf(MAX_REPORT_NUMBER));
                fragments = List.of();
                return false;
            }

            int remaining = firmwareBytes.length - offset;
            int chunkSize = Math.min(usable, remaining);

            byte[] chunk = Arrays.copyOfRange(firmwareBytes, offset, offset + chunkSize);

            boolean isLast = (offset + chunkSize) >= firmwareBytes.length;

            fragments.add(new FirmwareFragment(reportNumber, isLast, chunk));

            offset += chunkSize;
            reportNumber++;
        }

        logger.debug("NODE {}: Prepared {} fragments (usable={} bytes each)", node.getNodeId(), fragments.size(),
                usable);
        return true;
    }

    /**
     * Represents a fragment of the firmware being transmitted.
     * Each fragment contains a portion of the firmware data along with its report
     * number and a flag indicating if it is the last fragment.
     */
    public static class FirmwareFragment {
        private final int reportNumber;
        private final boolean isLast;
        private final byte[] data;

        public FirmwareFragment(int reportNumber, boolean isLast, byte[] data) {
            this.reportNumber = reportNumber;
            this.isLast = isLast;
            this.data = data;
        }

        public int getReportNumber() {
            return reportNumber;
        }

        public boolean isLast() {
            return isLast;
        }

        public byte[] getData() {
            return data;
        }
    }

    // Sends the FIRMWARE_MD_REQUEST_GET with metadata from the initial report, to
    // confirm update parameters
    private void sendFirmwareUpdateMdRequestGet(FirmwareMetadata metadata) {
        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);

        byte[] payload = buildMdRequestGet(metadata);

        ZWaveCommandClassTransactionPayload msg = fw.sendMDRequestGetMessage(payload);

        node.sendMessage(msg);

        logger.debug("NODE {}: Sent Firmware MD RequestGet", node.getNodeId());
    }

    /**
     * Handle Update MD Request Report (4) from the device.
     * This indicates whether the device accepts the parameters from the earlier MD Request Get.
     * If accepted, the device should next send UPDATE_MD_GET (5) frames.
     *
     * @param event the Update MD Request Report
     * @return true if the event was handled in the current state, false otherwise
     */
    private boolean handleUpdateMdRequestReport(FirmwareUpdateEvent event) {
        if (state != State.WAITING_FOR_UPDATE_MD_REQUEST_REPORT) {
            return false;
        }

        // Version 8, resume = devices agrees to resume a previously interrupted update,
        // nonSecure = device agrees to accept firmware without security encoding
        FirmwareUpdateMdRequestStatus requestStatus = FirmwareUpdateMdRequestStatus.from(event.getStatus());
        logger.debug("NODE {}: Received Update MD Request Report", node.getNodeId());
        logger.debug("NODE {}: Status={} ({}), resume={}, nonSecure={}", node.getNodeId(), event.getStatus(),
                requestStatus, event.getResume(), event.getNonSecure());

        if (requestStatus != FirmwareUpdateMdRequestStatus.OK) {
            failFirmwareUpdate("Device rejected firmware update request: " + requestStatus, requestStatus.name());
            return true;
        }

        state = State.WAITING_FOR_UPDATE_MD_GET;
        return true;
    }

    /**
     * Handle UPDATE_MD_GET from the device.
     * Includes safeguards for out-of-sequence requests, duplicate report requests, implicit acknowledgments,
     * and retry behavior under slow multi-hop paths and transient communication failures.
     *
     * @param event the firmware update event representing UPDATE_MD_GET
     * @return true if the event was handled successfully, false otherwise
     */
    private boolean handleUpdateMdGet(FirmwareUpdateEvent event) {
        if (state != State.WAITING_FOR_UPDATE_MD_GET && state != State.SENDING_FRAGMENTS
                && state != State.WAITING_FOR_UPDATE_MD_STATUS_REPORT
                && state != State.WAITING_FOR_UPDATE_MD_REQUEST_REPORT) {
            return false;
        }
        // Accept UPDATE_MD_GET as an implicit OK in case of missing
        // UPDATE_MD_REQUEST_REPORT.
        if (state == State.WAITING_FOR_UPDATE_MD_REQUEST_REPORT) {
            logger.debug(
                    "NODE {}: Received UPDATE_MD_GET before UPDATE_MD_REQUEST_REPORT; treating as implicit request acceptance",
                    node.getNodeId());
        }

        int requestedStartReport = event.getReportNumber();
        int requestedCount = event.getNumReports();
        if (requestedCount <= 0) {
            logger.debug("NODE {}: Received UPDATE_MD_GET with invalid count {} - normalizing to 1", node.getNodeId(),
                    requestedCount);
            requestedCount = 1;
        }

        if (requestedStartReport < 1 || requestedStartReport > MAX_REPORT_NUMBER) {
            logger.warn("NODE {}: Received UPDATE_MD_GET with invalid start fragment {}", node.getNodeId(),
                    requestedStartReport);
            return true;
        }

        logger.debug("NODE {}: Received UPDATE_MD_GET for fragment {} (count={})", node.getNodeId(),
                requestedStartReport, requestedCount);

        // Ignore out-of-sequence forward jumps.
        if (isOutOfSequenceForwardRequest(requestedStartReport)) {
            return true;
        }

        // A GET for fragment N implicitly ACKs everything below N. Advance the ACK anchor
        // accordingly, then ignore any GET at or below the new anchor — the device already
        // confirmed receipt of those fragments by previously requesting something higher.
        int impliedAckReport = Math.min(highestTransmittedReportNumber, requestedStartReport - 1);
        if (impliedAckReport > highestAckedReportNumber) {
            if (requestedStartReport == highestTransmittedReportNumber + 1
                    && highestTransmittedReportNumber == startReportNumber) {
                logger.debug(
                        "NODE {}: Advancing ACK anchor from {} to {} based on sequential UPDATE_MD_GET start {} after completed fragment {} transmission",
                        node.getNodeId(), highestAckedReportNumber, impliedAckReport, requestedStartReport,
                        highestTransmittedReportNumber);
            } else {
                logger.debug("NODE {}: Advancing ACK anchor from {} to {} based on UPDATE_MD_GET start {}",
                        node.getNodeId(), highestAckedReportNumber, impliedAckReport, requestedStartReport);
            }
            highestAckedReportNumber = impliedAckReport;
        }

        if (requestedStartReport <= highestAckedReportNumber) {
            logger.debug("NODE {}: Ignoring UPDATE_MD_GET for already ACKed fragment {} (ackAnchor={})",
                    node.getNodeId(), requestedStartReport, highestAckedReportNumber);
            return true;
        }

        // If the device requests a fragment higher than the one that is currently
        // sent, advance to that higher fragment for the next transmission.
        // Some nodes send duplicate GETs for an already-sent report.
        // Ignore these near-term, but allow a late
        // retry so the device can recover if the fragment was truly missed.
        if (requestedStartReport > startReportNumber && startReportNumber > 0) {
            if (requestedStartReport == highestTransmittedReportNumber + 1
                    && highestTransmittedReportNumber == startReportNumber) {
                logger.debug(
                        "NODE {}: Received sequential UPDATE_MD_GET for fragment {} after fragment {} transmission completion; continuing with requested fragment",
                        node.getNodeId(), requestedStartReport, startReportNumber);
            } else {
                logger.debug(
                        "NODE {}: Received UPDATE_MD_GET for fragment {} while trying fragment {}; treating this as implicit ACK of fragment {} and continuing with the higher fragment",
                        node.getNodeId(), requestedStartReport, startReportNumber, startReportNumber);
            }
            duplicateGetsForSentReport = 0;
        } else if (requestedStartReport <= highestTransmittedReportNumber) {
            Long lastSentTime = reportLastSentTimes.get(requestedStartReport);
            long elapsedMillis = lastSentTime != null ? currentTimeMillis() - lastSentTime.longValue() : Long.MAX_VALUE;

            if (lastSentTime != null && elapsedMillis < DUPLICATE_GET_RESEND_DELAY_MS) {
                duplicateGetsForSentReport++;
                logger.debug(
                        "NODE {}: Ignoring duplicate UPDATE_MD_GET for already-transmitted fragment {} (highestTransmitted={}, duplicateCount={}, elapsedMs={})",
                        node.getNodeId(), requestedStartReport, highestTransmittedReportNumber,
                        duplicateGetsForSentReport, elapsedMillis);
                return true;
            }

            logger.debug(
                    "NODE {}: Re-sending previously transmitted fragment {} after duplicate UPDATE_MD_GET (elapsedMs={}, resendWindowMs={})",
                    node.getNodeId(), requestedStartReport,
                    lastSentTime != null ? Long.valueOf(elapsedMillis) : "unknown", DUPLICATE_GET_RESEND_DELAY_MS);

            // Consume the resend window immediately so another closely-spaced GET
            // does not trigger a second resend before transmission bookkeeping updates.
            reportLastSentTimes.put(requestedStartReport, currentTimeMillis());
            duplicateGetsForSentReport = 0;
        }
        duplicateGetsForSentReport = 0;

        if (fragments.isEmpty()) {
            fail("No fragments prepared");
            return true;
        }

        int remainingFragments = fragments.size() - requestedStartReport + 1;
        if (remainingFragments <= 0) {
            logger.warn("NODE {}: Received UPDATE_MD_GET start {} beyond available fragments {}", node.getNodeId(),
                    requestedStartReport, fragments.size());
            return true;
        }

        int cappedCount = Math.min(requestedCount, remainingFragments);
        if (cappedCount != requestedCount) {
            logger.debug("NODE {}: Capping UPDATE_MD_GET count from {} to {} (remaining from start={} is {})",
                    node.getNodeId(), requestedCount, cappedCount, requestedStartReport, remainingFragments);
        }

        // Device is asking for the next fragment.
        this.startReportNumber = requestedStartReport;
        this.count = cappedCount;
        state = State.SENDING_FRAGMENTS;
        sendNextFragment(startReportNumber, count);

        return true;
    }

    private boolean isOutOfSequenceForwardRequest(int requestedStartReport) {
        if (requestedStartReport <= 1) {
            return false;
        }

        if (highestTransmittedReportNumber <= 0) {
            logger.warn(
                    "NODE {}: Ignoring out-of-sequence UPDATE_MD_GET for fragment {} before fragment 1 was transmitted",
                    node.getNodeId(), requestedStartReport);
            return true;
        }

        int nextExpectedReport = highestTransmittedReportNumber + 1;
        if (requestedStartReport > nextExpectedReport) {
            logger.warn(
                    "NODE {}: Ignoring out-of-sequence UPDATE_MD_GET for fragment {} (highestTransmitted={}, nextExpected={})",
                    node.getNodeId(), requestedStartReport, highestTransmittedReportNumber, nextExpectedReport);
            return true;
        }

        return false;
    }

    /**
     * Sends one or more fragments in response to UPDATE_MD_GET.
     * Supports:
     * - Single fragment requests
     * - Repeated fragment requests
     * - Multi-fragment requests (with required 35ms delay)
     */
    private void sendNextFragment(int startReportNumber, int count) {
        if (fragments == null || fragments.isEmpty()) {
            fail("No fragments prepared");
            return;
        }

        if (count <= 0) {
            logger.warn("NODE {}: Invalid fragment count request: {}", node.getNodeId(), count);
            return;
        }

        // Defensive bounds check
        if (startReportNumber < 1 || startReportNumber > fragments.size()) {
            logger.warn("NODE {}: Invalid fragment request: {}", node.getNodeId(), startReportNumber);
            return;
        }

        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);

        if (fw == null) {
            fail("Firmware Update MD CC missing");
            return;
        }

        // Multi-fragment request: device may ask for N fragments at once
        for (int i = 0; i < count; i++) {

            int reportNumber = startReportNumber + i;

            if (reportNumber > fragments.size()) {
                logger.warn("NODE {}: Device requested fragment {} beyond available {}", node.getNodeId(), reportNumber,
                        fragments.size());
                break;
            }

            FirmwareFragment fragment = fragments.get(reportNumber - 1);

            logger.debug("NODE {}: Sending fragment {} (last={})", node.getNodeId(), fragment.getReportNumber(),
                    fragment.isLast());

            // Convert session fragment → CC fragment
            ZWaveFirmwareUpdateCommandClass.FirmwareFragment ccFragment = new ZWaveFirmwareUpdateCommandClass.FirmwareFragment(
                    fragment.isLast(), fragment.getReportNumber(), fragment.getData(), null);

            ZWaveCommandClassTransactionPayload msg = fw.sendFirmwareUpdateReport(ccFragment);

            if (logger.isTraceEnabled()) {
                int advertisedMaxFragmentSize = sessionMetadata != null ? sessionMetadata.maxFragmentSize() : -1;
                byte[] txPayload = msg.getPayloadBuffer();
                int crcMsb = txPayload.length >= 2 ? txPayload[txPayload.length - 2] & 0xFF : -1;
                int crcLsb = txPayload.length >= 1 ? txPayload[txPayload.length - 1] & 0xFF : -1;
                logger.trace(
                        "NODE {}: Fragment TX details report={}, isLast={}, advertisedMaxDataLen={}, dataLen={}, payloadLen={}, crc=0x{}{}, payload={}",
                        node.getNodeId(), fragment.getReportNumber(), fragment.isLast(),
                        advertisedMaxFragmentSize >= 0 ? advertisedMaxFragmentSize : null, fragment.getData().length,
                        txPayload.length, crcMsb >= 0 ? String.format("%02X", crcMsb) : "??",
                        crcLsb >= 0 ? String.format("%02X", crcLsb) : "??", toHex(txPayload));
            }

            node.sendMessage(msg);
            long sentAtMillis = currentTimeMillis();
            reportLastSentTimes.put(fragment.getReportNumber(), sentAtMillis);
            highestTransmittedReportNumber = Math.max(highestTransmittedReportNumber, fragment.getReportNumber());
            publishFirmwareUpdateProgressIfNeeded();

            // If this was the last fragment, transition to waiting for status
            if (fragment.isLast()) {
                logger.debug("NODE {}: Last fragment sent, waiting for status report", node.getNodeId());
                state = State.WAITING_FOR_UPDATE_MD_STATUS_REPORT;
                scheduleStatusReportTimeout();
                return;
            }

            // Required delay when multiple fragments are requested
            if (count > 1 && i < count - 1) {
                try {
                    Thread.sleep(MULTI_FRAGMENT_INTERFRAME_DELAY_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Normal case: after sending a fragment, remain in SENDING_FRAGMENTS
        state = State.SENDING_FRAGMENTS;
    }

    private void scheduleStatusReportTimeout() {
        int generation = statusReportTimeoutGeneration.incrementAndGet();

        CompletableFuture.runAsync(() -> {
            if (!active) {
                return;
            }
            if (statusReportTimeoutGeneration.get() != generation) {
                return;
            }
            if (state != State.WAITING_FOR_UPDATE_MD_STATUS_REPORT) {
                return;
            }

            logger.warn("NODE {}: Timed out waiting for Firmware Update MD Status Report", node.getNodeId());
            failFirmwareUpdate("Timed out waiting for Firmware Update MD Status Report");
        }, CompletableFuture.delayedExecutor(getStatusReportWaitTimeoutSeconds(), TimeUnit.SECONDS));
    }

    protected int getStatusReportWaitTimeoutSeconds() {
        return STATUS_REPORT_WAIT_TIMEOUT_SECONDS;
    }

    private void invalidateStatusReportTimeout() {
        statusReportTimeoutGeneration.incrementAndGet();
    }

    private boolean handleUpdateMdStatusReport(FirmwareUpdateEvent event) {
        // A status report is only valid after the last fragment has been sent and
        // we are explicitly waiting for the device's final update status.
        // Any out-of-sequence status report is treated as a protocol failure.
        if (state != State.WAITING_FOR_UPDATE_MD_STATUS_REPORT) {
            if (!active) {
                return false;
            }
            logger.warn("NODE {}: Received unexpected UPDATE_MD_STATUS_REPORT in state {} - treating as protocol error",
                    node.getNodeId(), state);
        }

        // Any status report means the waiting timer is no longer authoritative.
        invalidateStatusReportTimeout();

        FirmwareUpdateMdStatusReport updateStatus = FirmwareUpdateMdStatusReport.from(event.getStatus());
        logger.debug("NODE {}: Received Status Report: {}", node.getNodeId(), updateStatus);

        switch (updateStatus) {
            case ERROR_CHECKSUM:
            case ERROR_TRANSMISSION_FAILED:
            case ERROR_INVALID_MANUFACTURER_ID:
            case ERROR_INVALID_FIRMWARE_ID:
            case ERROR_INVALID_FIRMWARE_TARGET:
            case ERROR_INVALID_HEADER_INFORMATION:
            case ERROR_INVALID_HEADER_FORMAT:
            case ERROR_INSUFFICIENT_MEMORY:
            case ERROR_INVALID_HARDWARE_VERSION:
            case UNKNOWN:
                failFirmwareUpdate("Device reported firmware update status: " + updateStatus, updateStatus.name());
                return true;

            case OK_WAITING_FOR_ACTIVATION:
                return handleWaitingForActivationStatus();

            case OK_NO_RESTART:
                publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Success, Integer.valueOf(event.getStatus()));
                completeSuccess();
                return true;

            case OK_RESTART_PENDING:
                scheduleNopAfterWaitTime(event.getWaitTime());
                publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Success, Integer.valueOf(event.getStatus()));
                completeSuccess(false);
                return true;

            default:
                failFirmwareUpdate("Unhandled firmware update status: " + updateStatus,
                        Integer.valueOf(event.getStatus()));
                return true;
        }
    }

    private boolean handleWaitingForActivationStatus() {
        int ccVersion = getFirmwareUpdateMdVersion();
        if (ccVersion < 4) {
            failFirmwareUpdate("Device reported activation required, but Firmware Update MD CC version " + ccVersion
                    + " does not support activation command", Integer.valueOf(ccVersion));
            return true;
        }

        FirmwareMetadata metadata = sessionMetadata;
        if (metadata == null) {
            failFirmwareUpdate("Cannot send activation - metadata unavailable", Integer.valueOf(-1));
            return true;
        }

        byte[] firmwareBaseData = buildFirmwareBaseData(metadata, ccVersion);

        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);
        if (fw == null) {
            failFirmwareUpdate("Firmware Update MD CC missing", Integer.valueOf(-1));
            return true;
        }

        ZWaveCommandClassTransactionPayload msg = fw.setFirmwareActivation(firmwareBaseData);
        node.sendMessage(msg);
        state = State.WAITING_FOR_ACTIVATION_STATUS_REPORT;

        logger.debug("NODE {}: Sent Firmware Update Activation Set", node.getNodeId());
        return true;
    }

    private boolean handleActivationStatusReport(FirmwareUpdateEvent event) {
        if (state != State.WAITING_FOR_ACTIVATION_STATUS_REPORT) {
            return false;
        }

        FirmwareUpdateActivationStatus activationStatus = FirmwareUpdateActivationStatus.from(event.getStatus());
        logger.debug("NODE {}: Received Activation Status Report: {}", node.getNodeId(), activationStatus);

        switch (activationStatus) {
            case SUCCESS:
                publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Success, Integer.valueOf(event.getStatus()));
                completeSuccess();
                return true;

            case ERROR_ACTIVATING_FIRMWARE:
            case INVALID_PAYLOAD:
            case UNKNOWN:
            default:
                failFirmwareUpdate("Firmware activation failed: " + activationStatus, activationStatus.name());
                return true;
        }
    }

    private void scheduleNopAfterWaitTime(int waitTimeSeconds) {
        if (waitTimeSeconds < 2) {
            waitTimeSeconds = 2;
        }

        final int delay = waitTimeSeconds;
        logger.debug("NODE {}: Scheduling NOP ping after {} seconds", node.getNodeId(), delay);

        CompletableFuture.runAsync(() -> {
            logger.debug("NODE {}: Sending delayed NOP ping after firmware restart wait", node.getNodeId());
            try {
                node.pingNode();
                scheduleVersionRefresh();
            } finally {
                // Keep the firmware-update awake hold active until the delayed post-restart
                // follow-up has been queued/sent.
                node.setFirmwareUpdateInProgress(false);
            }
        }, CompletableFuture.delayedExecutor(delay, TimeUnit.SECONDS));
    }

    private void scheduleVersionRefresh() {
        ZWaveVersionCommandClass versionCC = (ZWaveVersionCommandClass) node
                .getCommandClass(CommandClass.COMMAND_CLASS_VERSION);
        if (versionCC != null) {
            logger.debug("NODE {}: Requesting firmware version refresh after update", node.getNodeId());
            ZWaveCommandClassTransactionPayload msg = versionCC.getVersionMessage();
            node.sendMessage(msg);
        } else {
            logger.warn("NODE {}: Version command class not available for version refresh", node.getNodeId());
        }
    }

    private void completeSuccess() {
        completeSuccess(true);
    }

    private void completeSuccess(boolean releaseFirmwareUpdateHold) {
        logger.info("NODE {}: Firmware update completed", node.getNodeId());
        invalidateStatusReportTimeout();
        if (releaseFirmwareUpdateHold) {
            node.setFirmwareUpdateInProgress(false);
        }
        state = State.SUCCESS;
        active = false;
    }

    private void fail(String reason) {
        logger.error("NODE {}: Firmware update failed: {}", node.getNodeId(), reason);
        invalidateStatusReportTimeout();
        node.setFirmwareUpdateInProgress(false);
        state = State.FAILURE;
        active = false;
    }

    private void failFirmwareUpdate(String reason, Object value) {
        publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Failure, value);
        fail(reason);
    }

    private void failFirmwareUpdate(String reason) {
        failFirmwareUpdate(reason, reason);
    }

    private void publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State state, Object value) {
        ZWaveNetworkEvent event = new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.FirmwareUpdate, node.getNodeId(), state,
                value);

        if (controller.getController() != null) {
            controller.getController().notifyEventListeners(event);
            return;
        }

        // Fallback for early-session or test scenarios where the internal controller is not available.
        controller.ZWaveIncomingEvent(event);
    }

    private void publishFirmwareUpdateProgressIfNeeded() {
        if (fragments.isEmpty()) {
            return;
        }

        int steppedPercentComplete = getSteppedTransferProgressPercent();

        // Keep 100% reserved for terminal success status event.
        if (steppedPercentComplete >= 100) {
            steppedPercentComplete = 95;
        }

        if (steppedPercentComplete <= 0
                || steppedPercentComplete < lastPublishedProgressPercent + PROGRESS_EVENT_STEP_PERCENT) {
            return;
        }

        lastPublishedProgressPercent = steppedPercentComplete;
        publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Progress, Integer.valueOf(steppedPercentComplete));
    }

    private int getSteppedTransferProgressPercent() {
        int percentComplete = getCurrentTransferProgressPercent();
        return (percentComplete / PROGRESS_EVENT_STEP_PERCENT) * PROGRESS_EVENT_STEP_PERCENT;
    }

    /**
     * Returns the current transfer progress based on sent fragments.
     * This can be used by higher layers to restore UI status after transient
     * communication drops.
     *
     * @return progress percentage in range 0..99 while transfer is active
     */
    public int getCurrentTransferProgressPercent() {
        if (fragments.isEmpty()) {
            return 0;
        }

        int transmitted = Math.min(highestTransmittedReportNumber, fragments.size());
        int percentComplete = (transmitted * 100) / fragments.size();

        // Keep 100% reserved for terminal success status event.
        return Math.min(percentComplete, 99);
    }

    private String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 3);
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(String.format("%02X", data[i] & 0xFF));
        }
        return sb.toString();
    }

    protected long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Firmware update event types, used to route events from the Z-Wave protocol
     * layer into the session logic.
     */
    public enum FirmwareEventType {
        MD_REPORT,
        UPDATE_MD_REQUEST_REPORT,
        UPDATE_MD_GET,
        UPDATE_MD_STATUS_REPORT,
        ACTIVATION_STATUS_REPORT // optional, depending on your flow
    }

    /**
     * Firmware update session states, used to track the progress of a firmware
     * update for a node.
     */
    public enum State {
        IDLE,
        WAITING_FOR_MD_REPORT,
        WAITING_FOR_UPDATE_MD_REQUEST_REPORT,
        WAITING_FOR_UPDATE_MD_GET,
        SENDING_FRAGMENTS,
        WAITING_FOR_UPDATE_MD_STATUS_REPORT,
        WAITING_FOR_ACTIVATION_STATUS_REPORT, // optional, depending on your flow
        SUCCESS,
        FAILURE
    }
}
