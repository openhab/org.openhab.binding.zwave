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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeStatusEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZWaveFirmwareUpdateSession} class represents an active firmware
 * update session for a Z-Wave node.
 *
 * @author Robert Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveFirmwareUpdateSession {
    private final Logger logger = LoggerFactory.getLogger(ZWaveFirmwareUpdateSession.class);
    private static final int DEFAULT_MAX_FRAGMENT_SIZE = 32;
    private static final int MAX_REPORT_NUMBER = 0x7FFF;
    private static final int MULTI_FRAGMENT_INTERFRAME_DELAY_MS = 35;
    private static final int IMAGE_CHECKSUM_INITIAL = 0x1D0F;
    private static final int MAX_DUPLICATE_GETS_FOR_SENT_REPORT = 5;
    private static final int MD_REPORT_WAIT_TIMEOUT_SECONDS = 12;

    private int startReportNumber;
    private int count;
    private final ZWaveNode node;
    private final ZWaveControllerHandler controller;
    private final byte[] firmwareBytes;
    private final int firmwareChecksum;
    private final int firmwareTarget; // Z-Wave firmware target = 0

    private volatile boolean active = false;
    private volatile State state = State.IDLE;

    private List<FirmwareFragment> fragments = List.of();
    private @Nullable FirmwareMetadata sessionMetadata;
    private int highestRequestedStartReport = -1;
    private int highestTransmittedReportNumber = 0;
    private int duplicateGetsForSentReport = 0;
    private volatile boolean mdReportTimeoutArmed = false;

    // ---------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------
    public ZWaveFirmwareUpdateSession(
            ZWaveNode node,
            ZWaveControllerHandler controller,
            byte[] firmwareBytes,
            int firmwareTarget) {
        this.node = node;
        this.controller = controller;
        this.firmwareBytes = firmwareBytes;
        this.firmwareChecksum = ZWaveFirmwareUpdateCommandClass.crc16Ccitt(firmwareBytes, IMAGE_CHECKSUM_INITIAL);
        this.firmwareTarget = firmwareTarget;
    }

    // ---------------------------------------------------------
    // Event Types
    // ---------------------------------------------------------
    public enum FirmwareEventType {
        MD_REPORT,
        UPDATE_MD_REQUEST_REPORT,
        UPDATE_MD_GET,
        UPDATE_MD_STATUS_REPORT,
        ACTIVATION_STATUS_REPORT, // optional, depending on your flow
        UPDATE_PREPARE_REPORT // Not implemented yet, but can be used to retrieve current firmware information.
    }

    // ---------------------------------------------------------
    // Session State
    // ---------------------------------------------------------
    public enum State {
        IDLE,
        WAITING_FOR_MD_REPORT,
        WAITING_FOR_UPDATE_MD_REQUEST_REPORT,
        WAITING_FOR_UPDATE_MD_GET,
        SENDING_FRAGMENTS,
        WAITING_FOR_UPDATE_MD_STATUS_REPORT,
        WAITING_FOR_ACTIVATION_STATUS_REPORT, // optional, depending on your flow
        WAITING_FOR_UPDATE_PREPARE_REPORT, // Not implemented yet, but can be used to retrieve current firmware information.
        SUCCESS,
        FAILURE
    }

    // ---------------------------------------------------------
    // Update MD Request Status
    // ---------------------------------------------------------
    public enum UpdateMdRequestStatus {
        ERROR_INVALID_MANUFACTURER_OR_FIRMWARE_ID(0x00),
        ERROR_AUTHENTICATION_EXPECTED(0x01),
        ERROR_FRAGMENT_SIZE_TOO_LARGE(0x02),
        ERROR_NOT_UPGRADABLE(0x03),
        ERROR_INVALID_HARDWARE_VERSION(0x04),
        ERROR_FIRMWARE_UPGRADE_IN_PROGRESS(0x05),
        ERROR_BATTERY_LOW(0x06),
        OK(0xFF),
        UNKNOWN(-1);

        private final int id;

        UpdateMdRequestStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static UpdateMdRequestStatus from(int v) {
            for (UpdateMdRequestStatus s : values()) {
                if (s.id == v) {
                    return s;
                }
            }
            return UNKNOWN;
        }
    }

    // ---------------------------------------------------------
    // Firmware Update Status Report Values (for UPDATE_MD_STATUS_REPORT)
    // ---------------------------------------------------------
    public enum UpdateMdStatusReport {
        ERROR_CHECKSUM(0x00),
        ERROR_TRANSMISSION_FAILED(0x01),
        ERROR_INVALID_MANUFACTURER_ID(0x02),
        ERROR_INVALID_FIRMWARE_ID(0x03),
        ERROR_INVALID_FIRMWARE_TARGET(0x04),
        ERROR_INVALID_HEADER_INFORMATION(0x05),
        ERROR_INVALID_HEADER_FORMAT(0x06),
        ERROR_INSUFFICIENT_MEMORY(0x07),
        ERROR_INVALID_HARDWARE_VERSION(0x08),
        OK_WAITING_FOR_ACTIVATION(0xFD),
        OK_NO_RESTART(0xFE),
        OK_RESTART_PENDING(0xFF),
        UNKNOWN(-1);

        private final int id;

        UpdateMdStatusReport(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static UpdateMdStatusReport from(int v) {
            for (UpdateMdStatusReport s : values()) {
                if (s.id == v) {
                    return s;
                }
            }
            return UNKNOWN;
        }
    }

    // ---------------------------------------------------------
    // Event wrapper
    // ---------------------------------------------------------
    public static class FirmwareUpdateEvent extends ZWaveEvent {
        private final FirmwareEventType type;

        private final int reportNumber;
        private final int numReports;
        private final int status;
        private final int waitTime;
        private final byte[] payload;
        private final @Nullable Boolean resume;
        private final @Nullable Boolean nonSecure;

        private FirmwareUpdateEvent(int nodeId, int endpoint, FirmwareEventType type,
                int reportNumber, int numReports, int status, int waitTime,
                byte[] payload, @Nullable Boolean resume, @Nullable Boolean nonSecure) {
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
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.MD_REPORT,
                    -1, 0, 0, 0, payload, null, null);
        }

        public static FirmwareUpdateEvent forUpdateMdRequestReport(int nodeId, int endpoint, int status,
                @Nullable Boolean resume, @Nullable Boolean nonSecure) {
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.UPDATE_MD_REQUEST_REPORT,
                    -1, 0, status, 0, new byte[0], resume, nonSecure);
        }

        public static FirmwareUpdateEvent forUpdateMdGet(int nodeId, int endpoint, int reportNumber, int numReports) {
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.UPDATE_MD_GET,
                    reportNumber, numReports, 0, 0, new byte[0], null, null);
        }

        public static FirmwareUpdateEvent forUpdateMdStatusReport(int nodeId, int endpoint, int status,
                int waitTime) {
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.UPDATE_MD_STATUS_REPORT,
                    -1, 0, status, waitTime, new byte[0], null, null);
        }

        public static FirmwareUpdateEvent forActivationStatusReport(int nodeId, int endpoint, int status) {
            return new FirmwareUpdateEvent(nodeId, endpoint, FirmwareEventType.ACTIVATION_STATUS_REPORT,
                -1, 0, status, 0, new byte[0], null, null);
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

    // ---------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------
    public void start() {
        logger.info("NODE {}: Firmware session starting", node.getNodeId());
        active = true;
        state = State.WAITING_FOR_MD_REPORT;
        highestRequestedStartReport = -1;
        highestTransmittedReportNumber = 0;
        duplicateGetsForSentReport = 0;
        mdReportTimeoutArmed = false;

        requestMetadata(); // (1)

        // Start timeout only once the node is awake, since requestMetadata may be queued for sleeping nodes.
        if (node.isAwake()) {
            scheduleMdReportTimeout();
        }
    }

    private void scheduleMdReportTimeout() {
        if (mdReportTimeoutArmed) {
            return;
        }
        mdReportTimeoutArmed = true;

        CompletableFuture.runAsync(() -> {
            if (!active || state != State.WAITING_FOR_MD_REPORT) {
                return;
            }

            logger.debug("NODE {}: Timed out waiting for Firmware MD Report", node.getNodeId());
            failFirmwareUpdate("Timed out waiting for Firmware MD Report", Integer.valueOf(-1));
        }, CompletableFuture.delayedExecutor(MD_REPORT_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    public boolean isActive() {
        return active;
    }

    public void abort(String reason) {
        if (!active) {
            return;
        }

        failFirmwareUpdate("Firmware update session aborted: " + reason, Integer.valueOf(-1));
    }

    private void completeSuccess() {
        logger.info("NODE {}: Firmware update completed", node.getNodeId());
        node.setFirmwareUpdateInProgress(false);
        state = State.SUCCESS;
        active = false;
    }

    private void fail(String reason) {
        logger.error("NODE {}: Firmware update failed: {}", node.getNodeId(), reason);
        node.setFirmwareUpdateInProgress(false);
        state = State.FAILURE;
        active = false;
    }

    private void failFirmwareUpdate(String reason, Object value) {
        publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Failure, value);
        fail(reason);
    }

    private void publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State state, Object value) {
        ZWaveNetworkEvent event = new ZWaveNetworkEvent(
                ZWaveNetworkEvent.Type.FirmwareUpdate,
                node.getNodeId(),
                state,
                value);

        if (controller.getController() != null) {
            controller.getController().notifyEventListeners(event);
            return;
        }

        // Fallback for early-session or test scenarios where the internal controller is not available.
        controller.ZWaveIncomingEvent(event);
    }

    // ---------------------------------------------------------
    // Internal Fragment
    // ---------------------------------------------------------
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

    // ---------------------------------------------------------
    // Fragment preparation
    // ---------------------------------------------------------
    private boolean prepareFragments(FirmwareMetadata metadata) {
        fragments = new ArrayList<>();

        // maxFragmentSize specifies the firmware DATA bytes per fragment only;
        // the CC/CMD/reportNum/CRC overhead is added by the serializer on top of this.
        int usable = metadata.maxFragmentSize();

        if (usable <= 0) {
            failFirmwareUpdate("Max fragment size too small for firmware update (max=" + metadata.maxFragmentSize() + ")",
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

        logger.debug("NODE {}: Prepared {} fragments (usable={} bytes each)",
                node.getNodeId(), fragments.size(), usable);
        return true;
    }

    // ---------------------------------------------------------
    // Event Routing
    // ---------------------------------------------------------
    public boolean handleEvent(Object event) {
        if (event instanceof ZWaveNodeStatusEvent nodeStatusEvent) {
            if (state == State.WAITING_FOR_MD_REPORT && nodeStatusEvent.getState() == ZWaveNodeState.AWAKE) {
                scheduleMdReportTimeout();
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
            case UPDATE_PREPARE_REPORT:
                break;
            default:
                break;
        }

        return false;
    }

    // ---------------------------------------------------------
    // Event Handlers
    // ---------------------------------------------------------
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
                node.getNodeId(),
                metadata.manufacturerId(),
                metadata.firmwareId(),
                metadata.checksum(),
                metadata.maxFragmentSize(),
                metadata.hardwareVersionPresent(),
                metadata.hardwareVersion(),
                Integer.toHexString(metadata.requestFlags()));

        this.sessionMetadata = metadata;
        node.setFirmwareUpdateInProgress(true);

        // Prepare fragments using maxFragmentSize
        if (!prepareFragments(metadata)) {
            return true;
        }

        // Build and send UPDATE_MD_REQUEST_GET
        sendFirmwareUpdateMdRequestGet(metadata);

        state = State.WAITING_FOR_UPDATE_MD_REQUEST_REPORT;
        return true;
    }

    private boolean handleUpdateMdRequestReport(FirmwareUpdateEvent event) {
        if (state != State.WAITING_FOR_UPDATE_MD_REQUEST_REPORT) {
            return false;
        }

        // Version 8, resume = devices agrees to resume a previously interrupted update,
        // nonSecure = device agrees to accept firmware without security encoding
        UpdateMdRequestStatus requestStatus = UpdateMdRequestStatus.from(event.getStatus());
        logger.debug("NODE {}: Received Update MD Request Report", node.getNodeId());
        logger.debug("NODE {}: Status={} ({}), resume={}, nonSecure={}",
                node.getNodeId(), event.getStatus(), requestStatus, event.getResume(), event.getNonSecure());

        if (requestStatus != UpdateMdRequestStatus.OK) {
            publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Failure, Integer.valueOf(event.getStatus()));
            fail("Device rejected firmware update request: " + requestStatus);
            return true;
        }

        state = State.WAITING_FOR_UPDATE_MD_GET;
        return true;
    }

    private boolean handleUpdateMdGet(FirmwareUpdateEvent event) {
        if (state != State.WAITING_FOR_UPDATE_MD_GET && state != State.SENDING_FRAGMENTS) {
            return false;
        }

        int requestedStartReport = event.getReportNumber();
        int requestedCount = event.getNumReports();
        if (requestedCount <= 0) {
            logger.debug("NODE {}: Received UPDATE_MD_GET with invalid count {} - normalizing to 1",
                    node.getNodeId(), requestedCount);
            requestedCount = 1;
        }

        logger.debug("NODE {}: Received UPDATE_MD_GET for fragment {} (count={})",
                node.getNodeId(), requestedStartReport, requestedCount);

        // Ignore stale requests that arrive after the device has already advanced.
        // Some devices/controllers can emit closely-spaced GETs that arrive out of order.
        if (highestRequestedStartReport > 0 && requestedStartReport < highestRequestedStartReport) {
            logger.debug(
                    "NODE {}: Ignoring stale UPDATE_MD_GET for fragment {} because fragment {} was already requested",
                    node.getNodeId(), requestedStartReport, highestRequestedStartReport);
            return true;
        }
        if (requestedStartReport > highestRequestedStartReport) {
            highestRequestedStartReport = requestedStartReport;
        }

        // Some nodes may queue duplicate GETs for an already-sent report when there is
        // a slight timing delay. Do not resend reports that were already transmitted.
        if (requestedStartReport <= highestTransmittedReportNumber) {
            duplicateGetsForSentReport++;
            logger.warn(
                "NODE {}: Ignoring duplicate UPDATE_MD_GET for already-transmitted fragment {} (highestTransmitted={}, duplicateCount={})",
                node.getNodeId(), requestedStartReport, highestTransmittedReportNumber, duplicateGetsForSentReport);

            if (duplicateGetsForSentReport >= MAX_DUPLICATE_GETS_FOR_SENT_REPORT) {
            failFirmwareUpdate(
                "Device repeatedly requested already-transmitted fragment " + requestedStartReport
                    + " (highestTransmitted=" + highestTransmittedReportNumber + ")",
                Integer.valueOf(requestedStartReport));
            }
            return true;
        }
        duplicateGetsForSentReport = 0;

        if (requestedStartReport < 1 || requestedStartReport > MAX_REPORT_NUMBER) {
            logger.warn("NODE {}: Received UPDATE_MD_GET with invalid start fragment {}",
                node.getNodeId(), requestedStartReport);
            return true;
        }

        if (fragments.isEmpty()) {
            fail("No fragments prepared");
            return true;
        }

        int remainingFragments = fragments.size() - requestedStartReport + 1;
        if (remainingFragments <= 0) {
            logger.warn("NODE {}: Received UPDATE_MD_GET start {} beyond available fragments {}",
                node.getNodeId(), requestedStartReport, fragments.size());
            return true;
        }

        int cappedCount = Math.min(requestedCount, remainingFragments);
        if (cappedCount != requestedCount) {
            logger.debug(
                "NODE {}: Capping UPDATE_MD_GET count from {} to {} (remaining from start={} is {})",
                node.getNodeId(), requestedCount, cappedCount, requestedStartReport, remainingFragments);
        }

        // Device is asking for the next fragment
        this.startReportNumber = requestedStartReport;
        this.count = cappedCount;
        state = State.SENDING_FRAGMENTS;
        sendNextFragment(startReportNumber, count);

        return true;
    }

    private boolean handleUpdateMdStatusReport(FirmwareUpdateEvent event) {
        if (state != State.WAITING_FOR_UPDATE_MD_STATUS_REPORT) {
            return false;
        }

        UpdateMdStatusReport updateStatus = UpdateMdStatusReport.from(event.getStatus());
        logger.debug("NODE {}: Received Status Report: {}",
                node.getNodeId(), updateStatus);

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
                failFirmwareUpdate("Device reported firmware update status: " + updateStatus,
                        Integer.valueOf(event.getStatus()));
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
                completeSuccess();
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
            failFirmwareUpdate("Device reported activation required, but Firmware Update MD CC version "
                    + ccVersion + " does not support activation command", Integer.valueOf(ccVersion));
            return true;
        }

        FirmwareMetadata metadata = sessionMetadata;
        if (metadata == null) {
            failFirmwareUpdate("Cannot send activation - metadata unavailable", Integer.valueOf(-1));
            return true;
        }

        byte[] firmwareBaseData = buildFirmwareBaseData(metadata, ccVersion);

        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node.getCommandClass(
                CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);
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

        if (event.getStatus() == 0xFF) {
            publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Success, Integer.valueOf(event.getStatus()));
            completeSuccess();
            return true;
        }

        failFirmwareUpdate("Firmware activation failed", Integer.valueOf(event.getStatus()));
        return true;
    }

    private void scheduleNopAfterWaitTime(int waitTimeSeconds) {
        if (waitTimeSeconds < 0) {
            waitTimeSeconds = 0;
        }

        final int delay = waitTimeSeconds;
        logger.debug("NODE {}: Scheduling NOP ping after {} seconds", node.getNodeId(), delay);

        CompletableFuture.runAsync(() -> {
            logger.debug("NODE {}: Sending delayed NOP ping after firmware restart wait", node.getNodeId());
            node.pingNode();
        }, CompletableFuture.delayedExecutor(delay, TimeUnit.SECONDS));
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

        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node.getCommandClass(
                CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);

        if (fw == null) {
            fail("Firmware Update MD CC missing");
            return;
        }

        // Multi-fragment request: device may ask for N fragments at once
        for (int i = 0; i < count; i++) {

            int reportNumber = startReportNumber + i;

            if (reportNumber > fragments.size()) {
                logger.warn("NODE {}: Device requested fragment {} beyond available {}",
                        node.getNodeId(), reportNumber, fragments.size());
                break;
            }

            FirmwareFragment fragment = fragments.get(reportNumber - 1);

            logger.debug("NODE {}: Sending fragment {} (last={})",
                    node.getNodeId(), fragment.getReportNumber(), fragment.isLast());

            // Convert session fragment → CC fragment
            ZWaveFirmwareUpdateCommandClass.FirmwareFragment ccFragment = new ZWaveFirmwareUpdateCommandClass.FirmwareFragment(
                    fragment.isLast(),
                    fragment.getReportNumber(),
                    fragment.getData(),
                    null
            );

            ZWaveCommandClassTransactionPayload msg = fw.sendFirmwareUpdateReport(ccFragment);

                if (logger.isDebugEnabled()) {
                    int advertisedMaxFragmentSize = sessionMetadata != null ? sessionMetadata.maxFragmentSize() : -1;
                byte[] txPayload = msg.getPayloadBuffer();
                int crcMsb = txPayload.length >= 2 ? txPayload[txPayload.length - 2] & 0xFF : -1;
                int crcLsb = txPayload.length >= 1 ? txPayload[txPayload.length - 1] & 0xFF : -1;
                logger.debug(
                        "NODE {}: Fragment TX details report={}, isLast={}, advertisedMaxDataLen={}, dataLen={}, payloadLen={}, crc=0x{}{}, payload={}",
                    node.getNodeId(),
                    fragment.getReportNumber(),
                    fragment.isLast(),
                        advertisedMaxFragmentSize >= 0 ? advertisedMaxFragmentSize : null,
                    fragment.getData().length,
                    txPayload.length,
                    crcMsb >= 0 ? String.format("%02X", crcMsb) : "??",
                    crcLsb >= 0 ? String.format("%02X", crcLsb) : "??",
                    toHex(txPayload));
                }

            node.sendMessage(msg);
            highestTransmittedReportNumber = Math.max(highestTransmittedReportNumber, fragment.getReportNumber());

            // If this was the last fragment, transition to waiting for status
            if (fragment.isLast()) {
                logger.debug("NODE {}: Last fragment sent, waiting for status report", node.getNodeId());
                state = State.WAITING_FOR_UPDATE_MD_STATUS_REPORT;
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

    // Sends the initial FIRMWARE_MD_GET to start the process
    private void requestMetadata() {
        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node.getCommandClass(
                CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);

        ZWaveCommandClassTransactionPayload msg = fw.sendMDGetMessage();
        node.sendMessage(msg);

        logger.debug("NODE {}: Sent Firmware MD Get", node.getNodeId());
    }

    // Sends the FIRMWARE_MD_REQUEST_GET with metadata from the initial report, to
    // confirm update parameters
    private void sendFirmwareUpdateMdRequestGet(FirmwareMetadata metadata) {
        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node.getCommandClass(
                CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);

        byte[] payload = buildMdRequestGet(metadata);

        ZWaveCommandClassTransactionPayload msg = fw.sendMDRequestGetMessage(payload);

        node.sendMessage(msg);

        logger.debug("NODE {}: Sent Firmware MD RequestGet", node.getNodeId());

    }

    // Parses the raw payload of the initial MD Report into structured metadata
    // for future use in creating payloads and preparing fragments.
    private FirmwareMetadata parseMetadata(byte[] payload) {
        if (payload.length < 6) {
            throw new IllegalArgumentException("payload too short (need at least 6 bytes, got " + payload.length + ")");
        }

        int manufacturerId = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF);
        int firmwareId = ((payload[2] & 0xFF) << 8) | (payload[3] & 0xFF);
        int checksum = ((payload[4] & 0xFF) << 8) | (payload[5] & 0xFF);

        // V1/V2 only provide the first 6 bytes; assume upgradable and use default
        // fragment size.
        if (payload.length == 6) {
            byte[] report3Payload = buildLegacyReport3Payload(manufacturerId, firmwareId, checksum,
                    false, false, DEFAULT_MAX_FRAGMENT_SIZE, false, 0, 0);

            return new FirmwareMetadata(
                    manufacturerId,
                    firmwareId,
                    checksum,
                    true,
                    DEFAULT_MAX_FRAGMENT_SIZE,
                    0,
                    false,
                    0,
                    false,
                    0,
                    report3Payload);
        }

        // V3+ metadata requires bytes 6..9.
        if (payload.length < 10) {
            throw new IllegalArgumentException("payload too short for v3+ metadata (need at least 10 bytes, got "
                    + payload.length + ")");
        }

        boolean upgradable = (payload[6] & 0xFF) != 0;
        int additionalTargets = payload[7] & 0xFF;
        int maxFragmentSize = ((payload[8] & 0xFF) << 8) | (payload[9] & 0xFF);

        int index = 10 + (additionalTargets * 2);
        if (index > payload.length) {
            throw new IllegalArgumentException(
                    "additional target data exceeds payload length (targets=" + additionalTargets + ", payload="
                            + payload.length + ")");
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
        Integer report2Flags = parsedVersion >= 6 && remaining >= 2 ? Integer.valueOf(payload[index + 1] & 0xFF)
            : null;

        boolean ccFunctionalityPresent = report2Flags != null && (report2Flags.intValue() & 0x01) != 0;

        int requestFlags = mapRequestFlags(report2Flags);

        byte[] report3Payload = buildLegacyReport3Payload(
                manufacturerId,
                firmwareId,
                checksum,
                parsedVersion >= 3,
                parsedVersion >= 4,
                maxFragmentSize,
                hardwareVersionPresent,
                hardwareVersion,
                requestFlags);

        return new FirmwareMetadata(
                manufacturerId,
                firmwareId,
                checksum,
                upgradable,
                maxFragmentSize,
                additionalTargets,
                hardwareVersionPresent,
                hardwareVersion,
                ccFunctionalityPresent,
                requestFlags,
                report3Payload);
    }

    private int getFirmwareUpdateMdVersion() {
        ZWaveFirmwareUpdateCommandClass fw = (ZWaveFirmwareUpdateCommandClass) node.getCommandClass(
                CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);
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

    private byte[] buildLegacyReport3Payload(int manufacturerId, int firmwareId, int checksum,
            boolean includeV3Fields,
            boolean includeReport3Flags,
            int maxFragmentSize,
            boolean hardwareVersionPresent,
            int hardwareVersion,
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

    public record FirmwareMetadata(
            int manufacturerId,
            int firmwareId,
            int checksum,
            boolean upgradable,
            int maxFragmentSize,
            int additionalTargets,
            boolean hardwareVersionPresent,
            int hardwareVersion,
            boolean ccFunctionalityPresent,
            int requestFlags,
            byte[] report3Payload) {
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

}
