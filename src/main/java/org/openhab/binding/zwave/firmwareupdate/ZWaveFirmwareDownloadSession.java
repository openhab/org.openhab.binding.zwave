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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.firmwareupdate.ZWaveFirmwareUpdateSession.FirmwareEventType;
import org.openhab.binding.zwave.firmwareupdate.ZWaveFirmwareUpdateSession.FirmwareUpdateEvent;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveFirmwareUpdateCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeStatusEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Session that downloads firmware from a node using Firmware Update MD CC v5+.
 * Per spec 3.2.22.15.5, very few supporting nodes are capable of downloading their firmware.
 * This is a placeholder as I was not able to test
 * 
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class ZWaveFirmwareDownloadSession {
    private static final Logger logger = LoggerFactory.getLogger(ZWaveFirmwareDownloadSession.class);
    private static final int IMAGE_CHECKSUM_INITIAL = 0x1D0F;
    private static final int SESSION_TIMEOUT_SECONDS = 20;

    public enum State {
        IDLE,
        WAITING_FOR_MD_REPORT,
        WAITING_FOR_PREPARE_REPORT,
        WAITING_FOR_FRAGMENT_REPORT,
        FINALIZING,
        SUCCESS,
        FAILURE
    }

    private final ZWaveNode node;
    private final ZWaveControllerHandler controller;
    private final Path outputFolder;

    private volatile boolean active = false;
    private volatile State state = State.IDLE;
    private volatile boolean timeoutArmed = false;

    private @Nullable Metadata metadata;
    private final ByteArrayOutputStream imageData = new ByteArrayOutputStream();
    private int nextReportNumber = 1;

    public ZWaveFirmwareDownloadSession(ZWaveNode node, ZWaveControllerHandler controller, Path outputFolder) {
        this.node = node;
        this.controller = controller;
        this.outputFolder = outputFolder;
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        ZWaveFirmwareUpdateCommandClass fw = getFirmwareCc();
        if (fw == null) {
            failDownload("Firmware Update Metadata command class not supported", Integer.valueOf(-1));
            return;
        }

        int ccVersion = fw.getVersion();
        if (ccVersion < 5) {
            failDownload("Firmware download requires Firmware Update MD CC version 5 or newer", Integer.valueOf(ccVersion));
            return;
        }

        logger.info("NODE {}: Firmware download session starting", node.getNodeId());
        active = true;
        state = State.WAITING_FOR_MD_REPORT;
        timeoutArmed = false;
        metadata = null;
        imageData.reset();
        nextReportNumber = 1;

        node.setFirmwareUpdateInProgress(true);
        requestMetadata();

        if (node.isAwake()) {
            armSessionTimeout();
        }
    }

    public void abort(String reason) {
        if (!active) {
            return;
        }
        failDownload("Firmware download session aborted: " + reason, Integer.valueOf(-1));
    }

    public boolean handleEvent(Object event) {
        if (!active) {
            return false;
        }

        if (event instanceof ZWaveNodeStatusEvent nodeStatusEvent) {
            if (state == State.WAITING_FOR_MD_REPORT && nodeStatusEvent.getState() == ZWaveNodeState.AWAKE) {
                armSessionTimeout();
            }
            return false;
        }

        if (!(event instanceof FirmwareUpdateEvent firmwareEvent)) {
            return false;
        }

        FirmwareEventType type = firmwareEvent.getType();
        return switch (type) {
            case MD_REPORT -> handleMdReport(firmwareEvent);
            case UPDATE_PREPARE_REPORT -> handlePrepareReport(firmwareEvent);
            default -> false;
        };
    }

    private void armSessionTimeout() {
        if (timeoutArmed) {
            return;
        }
        timeoutArmed = true;

        CompletableFuture.runAsync(() -> {
            if (!active) {
                return;
            }

            if (state == State.WAITING_FOR_MD_REPORT || state == State.WAITING_FOR_PREPARE_REPORT
                    || state == State.WAITING_FOR_FRAGMENT_REPORT) {
                failDownload("Timed out waiting for firmware download response", Integer.valueOf(-1));
            }
        }, CompletableFuture.delayedExecutor(SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS));
    }

    private boolean handleMdReport(FirmwareUpdateEvent event) {
        if (state == State.WAITING_FOR_MD_REPORT) {
            return handleMetadataReport(event.getPayload());
        }
        if (state == State.WAITING_FOR_FRAGMENT_REPORT) {
            return handleFragmentReport(event.getPayload());
        }
        return false;
    }

    private boolean handleMetadataReport(byte[] payload) {
        Metadata parsed;
        try {
            parsed = parseMetadata(payload);
        } catch (IllegalArgumentException e) {
            failDownload("Malformed metadata report payload: " + e.getMessage(), e.getMessage());
            return true;
        }

        metadata = parsed;
        sendPrepareGet(parsed);
        state = State.WAITING_FOR_PREPARE_REPORT;

        logger.debug("NODE {}: Firmware download metadata parsed manufacturerId={}, firmwareId={}, checksum=0x{}",
                node.getNodeId(),
                parsed.manufacturerId(),
                parsed.firmwareId(),
                Integer.toHexString(parsed.checksum()));
        return true;
    }

    private boolean handlePrepareReport(FirmwareUpdateEvent event) {
        if (state != State.WAITING_FOR_PREPARE_REPORT) {
            return false;
        }

        if (event.getStatus() != 0xFF) {
            failDownload("Device rejected firmware download prepare request with status " + event.getStatus(),
                    Integer.valueOf(event.getStatus()));
            return true;
        }

        byte[] payload = event.getPayload();
        if (payload.length >= 2) {
            int reportedChecksum = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF);
            logger.debug("NODE {}: Prepare report checksum=0x{}", node.getNodeId(), Integer.toHexString(reportedChecksum));
        }

        requestFragment(nextReportNumber);
        state = State.WAITING_FOR_FRAGMENT_REPORT;
        return true;
    }

    private boolean handleFragmentReport(byte[] payload) {
        ZWaveFirmwareUpdateCommandClass fw = getFirmwareCc();
        if (fw == null) {
            failDownload("Firmware Update MD command class missing", Integer.valueOf(-1));
            return true;
        }

        if (payload.length < 4) {
            failDownload("Firmware fragment report payload too short", Integer.valueOf(payload.length));
            return true;
        }

        int reportWord = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF);
        boolean isLast = (reportWord & 0x8000) != 0;
        int reportNumber = reportWord & 0x7FFF;

        if (reportNumber != nextReportNumber) {
            failDownload("Unexpected firmware fragment report " + reportNumber + " while waiting for " + nextReportNumber,
                    Integer.valueOf(reportNumber));
            return true;
        }

        int crcBytes = fw.getVersion() >= 2 ? 2 : 0;
        int dataEnd = payload.length - crcBytes;
        if (dataEnd <= 2) {
            failDownload("Firmware fragment report has no payload data", Integer.valueOf(payload.length));
            return true;
        }

        if (crcBytes == 2) {
            int expectedCrc = ((payload[dataEnd] & 0xFF) << 8) | (payload[dataEnd + 1] & 0xFF);
            byte[] crcBuffer = Arrays.copyOfRange(payload, 0, dataEnd);
            int calculatedCrc = ZWaveFirmwareUpdateCommandClass.crc16Ccitt(
                    new byte[] {
                            (byte) CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD.getKey(),
                            (byte) ZWaveFirmwareUpdateCommandClass.FIRMWARE_UPDATE_MD_REPORT },
                    IMAGE_CHECKSUM_INITIAL);
            calculatedCrc = ZWaveFirmwareUpdateCommandClass.crc16Ccitt(crcBuffer, calculatedCrc);
            if (expectedCrc != calculatedCrc) {
                failDownload("Firmware fragment CRC mismatch", Integer.valueOf(reportNumber));
                return true;
            }
        }

        imageData.write(payload, 2, dataEnd - 2);

        if (isLast) {
            finalizeDownloadedFirmware();
            return true;
        }

        nextReportNumber++;
        requestFragment(nextReportNumber);
        return true;
    }

    private void finalizeDownloadedFirmware() {
        state = State.FINALIZING;

        try {
            Files.createDirectories(outputFolder);

            String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
            Path destination = outputFolder.resolve("download-" + node.getNodeId() + "-" + timestamp + ".bin");

            byte[] firmware = imageData.toByteArray();
            Files.write(destination, firmware);

            logger.info("NODE {}: Firmware download completed, {} bytes saved to {}", node.getNodeId(), firmware.length,
                    destination);
            publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Success, destination.toString());
            completeSuccess();
        } catch (IOException e) {
            failDownload("Failed to persist downloaded firmware", e.getMessage());
        }
    }

    private void requestMetadata() {
        ZWaveFirmwareUpdateCommandClass fw = getFirmwareCc();
        if (fw == null) {
            failDownload("Firmware Update MD command class missing", Integer.valueOf(-1));
            return;
        }

        ZWaveCommandClassTransactionPayload msg = fw.sendMDGetMessage();
        node.sendMessage(msg);
        logger.debug("NODE {}: Sent Firmware MD Get for download preflight", node.getNodeId());
    }

    private void sendPrepareGet(Metadata parsed) {
        ZWaveFirmwareUpdateCommandClass fw = getFirmwareCc();
        if (fw == null) {
            failDownload("Firmware Update MD command class missing", Integer.valueOf(-1));
            return;
        }

        byte[] payload;
        if (parsed.hardwareVersionPresent()) {
            payload = new byte[] {
                    (byte) ((parsed.manufacturerId() >> 8) & 0xFF),
                    (byte) (parsed.manufacturerId() & 0xFF),
                    (byte) ((parsed.firmwareId() >> 8) & 0xFF),
                    (byte) (parsed.firmwareId() & 0xFF),
                    0,
                    (byte) ((parsed.maxFragmentSize() >> 8) & 0xFF),
                    (byte) (parsed.maxFragmentSize() & 0xFF),
                    (byte) (parsed.hardwareVersion() & 0xFF)
            };
        } else {
            payload = new byte[] {
                    (byte) ((parsed.manufacturerId() >> 8) & 0xFF),
                    (byte) (parsed.manufacturerId() & 0xFF),
                    (byte) ((parsed.firmwareId() >> 8) & 0xFF),
                    (byte) (parsed.firmwareId() & 0xFF),
                    0,
                    (byte) ((parsed.maxFragmentSize() >> 8) & 0xFF),
                    (byte) (parsed.maxFragmentSize() & 0xFF)
            };
        }

        ZWaveCommandClassTransactionPayload msg = fw.setFirmwarePrepareGet(payload);
        node.sendMessage(msg);
        logger.debug("NODE {}: Sent Firmware Prepare Get", node.getNodeId());
    }

    private void requestFragment(int reportNumber) {
        ZWaveFirmwareUpdateCommandClass fw = getFirmwareCc();
        if (fw == null) {
            failDownload("Firmware Update MD command class missing", Integer.valueOf(-1));
            return;
        }

        ZWaveCommandClassTransactionPayload msg = fw.sendFirmwareUpdateMdGet(reportNumber, 1);
        node.sendMessage(msg);
        logger.debug("NODE {}: Requested firmware fragment {}", node.getNodeId(), reportNumber);
    }

    private @Nullable ZWaveFirmwareUpdateCommandClass getFirmwareCc() {
        return (ZWaveFirmwareUpdateCommandClass) node.getCommandClass(CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD);
    }

    private Metadata parseMetadata(byte[] payload) {
        if (payload.length < 10) {
            throw new IllegalArgumentException("payload too short for v5+ metadata (need at least 10 bytes, got "
                    + payload.length + ")");
        }

        int manufacturerId = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF);
        int firmwareId = ((payload[2] & 0xFF) << 8) | (payload[3] & 0xFF);
        int checksum = ((payload[4] & 0xFF) << 8) | (payload[5] & 0xFF);
        int maxFragmentSize = ((payload[8] & 0xFF) << 8) | (payload[9] & 0xFF);

        int additionalTargets = payload[7] & 0xFF;
        int index = 10 + (additionalTargets * 2);
        if (index > payload.length) {
            throw new IllegalArgumentException("metadata target list exceeds payload length");
        }

        int remaining = payload.length - index;
        boolean hardwareVersionPresent = remaining >= 1;
        int hardwareVersion = hardwareVersionPresent ? payload[index] & 0xFF : 0;

        return new Metadata(manufacturerId, firmwareId, checksum, maxFragmentSize, hardwareVersionPresent,
            hardwareVersion);
    }

    private void completeSuccess() {
        state = State.SUCCESS;
        active = false;
        timeoutArmed = false;
        node.setFirmwareUpdateInProgress(false);
    }

    private void fail(String reason) {
        logger.error("NODE {}: Firmware download failed: {}", node.getNodeId(), reason);
        state = State.FAILURE;
        active = false;
        timeoutArmed = false;
        node.setFirmwareUpdateInProgress(false);
    }

    private void failDownload(String reason, Object value) {
        publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State.Failure, value);
        fail(reason);
    }

    private void publishFirmwareUpdateNetworkEvent(ZWaveNetworkEvent.State eventState, Object value) {
        ZWaveNetworkEvent event = new ZWaveNetworkEvent(
                ZWaveNetworkEvent.Type.FirmwareUpdate,
                node.getNodeId(),
                eventState,
                value);

        if (controller.getController() != null) {
            controller.getController().notifyEventListeners(event);
            return;
        }

        controller.ZWaveIncomingEvent(event);
    }

    private record Metadata(int manufacturerId, int firmwareId, int checksum, int maxFragmentSize,
            boolean hardwareVersionPresent, int hardwareVersion) {
    }
}
