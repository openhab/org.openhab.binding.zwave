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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Merged implementation adapted from zwave-js FirmwareUpdateMetaDataCC.
 * Integrates firmware update command class data structures, (de)serialization
 * and CRC16-CCITT helper methods.
 * 
 * @author Chris Jackson - initial contribution
 * @author Bob Eckhoff - contributions to firmware update handling and
 *         refactoring
 */
@XStreamAlias("COMMAND_CLASS_FIRMWARE_UPDATE_MD")
@NonNullByDefault
public class ZWaveFirmwareUpdateCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveFirmwareUpdateCommandClass.class);
    // private static final int MAX_SUPPORTED_VERSION = 8;

    public static final int FIRMWARE_MD_GET = 0x01;
    public static final int FIRMWARE_MD_REPORT = 0x02;
    public static final int FIRMWARE_MD_REQUEST_GET = 0x03;
    public static final int FIRMWARE_MD_REQUEST_REPORT = 0x04;
    public static final int FIRMWARE_DOWNLOAD_GET = 0x05;
    public static final int FIRMWARE_DOWNLOAD_REPORT = 0x06;
    public static final int FIRMWARE_ACTIVATION_SET = 0x07;
    public static final int FIRMWARE_ACTIVATION_REPORT = 0x08;

    private @Nullable Integer cachedManufacturerId;
    private @Nullable Integer cachedFirmwareId;
    private @Nullable Integer cachedChecksum;
    private @Nullable Integer cachedMaxFragmentSize;
    private @Nullable Integer cachedHardwareVersion;

    /**
     * Creates a new instance of the ZWaveFirmwareUpdateCommandClass class.
     *
     * @param node       the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint   the endpoint this Command class belongs to
     */
    public ZWaveFirmwareUpdateCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD;
    }

    /**
     * Create a transaction payload for Firmware Meta Data Get.
     * The message requests the supporting node to return a Firmware Meta Data
     * Report.
     */
    public ZWaveCommandClassTransactionPayload getMetaDataGetMessage() {
        logger.debug("NODE {}: Creating new message for application command FIRMWARE_MD_GET",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                FIRMWARE_MD_GET)
                .withPriority(TransactionPriority.Config)
                .withExpectedResponseCommand(FIRMWARE_MD_REPORT).build();
    }

    /**
     * Create a transaction payload for Firmware Meta Data Request Get.
     *
     * @param request the {@link RequestGet} instance containing the parameters
     *                to send. the payload bytes are generated via
     *                {@code request.toBytes()}.
     */
    public ZWaveCommandClassTransactionPayload getMetaDataRequestGetMessage(RequestGet request) {
        logger.debug("NODE {}: Creating new message for application command FIRMWARE_MD_REQUEST_GET",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                FIRMWARE_MD_REQUEST_GET)
                .withPayload(request.toBytes())
                .withPriority(TransactionPriority.Config)
                .withExpectedResponseCommand(FIRMWARE_MD_REQUEST_REPORT).build();
    }

    /**
     * Convenience overload to create a Firmware Meta Data Request Get message using
     * cached values for manufacturer and firmware id.
     */
    public ZWaveCommandClassTransactionPayload getMetaDataRequestGetMessage() {
        return getMetaDataRequestGetMessage(new RequestGet(cachedManufacturerId != null ? cachedManufacturerId : 0,
                cachedFirmwareId != null ? cachedFirmwareId : 0, cachedChecksum != null ? cachedChecksum : 0, null,
                cachedMaxFragmentSize != null ? cachedMaxFragmentSize : 32, null,
                null, null, cachedHardwareVersion != null ? cachedHardwareVersion : 1));
    }

    @ZWaveResponseHandler(id = FIRMWARE_MD_REPORT, name = "FIRMWARE_MD_REPORT")
    public void handleMetaDataReport(ZWaveCommandClassPayload payload, int endpoint) {
        try {
            MetaDataReport r = MetaDataReport.fromBytes(payload.getPayloadBuffer(), getVersion());
            StringBuilder addIds = new StringBuilder();
            for (int id : r.additionalFirmwareIDs) {
                if (addIds.length() > 0) {
                    addIds.append(", ");
                }
                addIds.append(String.format("0x%04X", id));
            }

            logger.debug("NODE {}: Received Firmware Meta Data Report", getNode().getNodeId());
            logger.debug("NODE {}: Manufacturer ID = 0x{}", getNode().getNodeId(),
                    String.format("%04X", r.manufacturerId));
            this.cachedManufacturerId = r.manufacturerId;
            logger.debug("NODE {}: Firmware ID = 0x{}", getNode().getNodeId(), String.format("%04X", r.firmwareId));
            this.cachedFirmwareId = r.firmwareId;
            logger.debug("NODE {}: Checksum = 0x{}", getNode().getNodeId(), String.format("%04X", r.checksum));
            this.cachedChecksum = r.checksum;
            logger.debug("NODE {}: Firmware upgradable = {}", getNode().getNodeId(), r.firmwareUpgradable);
            logger.debug("NODE {}: Number additional targets = {}", getNode().getNodeId(),
                    r.additionalFirmwareIDs.size());
            logger.debug("NODE {}: Additional Firmware IDs = {}", getNode().getNodeId(), addIds.toString());
            logger.debug("NODE {}: Max fragment size = {}", getNode().getNodeId(), r.maxFragmentSize);
            this.cachedMaxFragmentSize = r.maxFragmentSize;
            logger.debug("NODE {}: Hardware version = {}", getNode().getNodeId(), r.hardwareVersion);
            this.cachedHardwareVersion = r.hardwareVersion;
            logger.debug("NODE {}: Continues to function = {}", getNode().getNodeId(), r.continuesToFunction);
            logger.debug("NODE {}: Supports activation = {}", getNode().getNodeId(), r.supportsActivation);
            logger.debug("NODE {}: Supports resume = {}", getNode().getNodeId(), r.supportsResuming);
            logger.debug("NODE {}: Supports non-secure transfer = {}", getNode().getNodeId(),
                    r.supportsNonSecureTransfer);
        } catch (IllegalArgumentException e) {
            logger.debug("NODE {}: Failed to parse Firmware Meta Data Report: {}", getNode().getNodeId(),
                    e.getMessage());
        }
    }

    @ZWaveResponseHandler(id = FIRMWARE_MD_REQUEST_REPORT, name = "FIRMWARE_MD_REQUEST_REPORT")
    public void handleMetaDataRequestReport(ZWaveCommandClassPayload payload, int endpoint) {
        try {
            RequestReport r = RequestReport.fromBytes(payload.getPayloadBuffer());
            logger.debug("NODE {}: Received Firmware Meta Data Request Report", getNode().getNodeId());
            logger.debug("NODE {}: Status = {}", getNode().getNodeId(), r.status);
            logger.debug("NODE {}: Resume = {}", getNode().getNodeId(), r.resume);
            logger.debug("NODE {}: Non-secure transfer = {}", getNode().getNodeId(), r.nonSecureTransfer);
        } catch (IllegalArgumentException e) {
            logger.debug("NODE {}: Failed to parse Firmware Meta Data Request Report: {}", getNode().getNodeId(),
                    e.getMessage());
        }
    }

    @ZWaveResponseHandler(id = FIRMWARE_DOWNLOAD_GET, name = "FIRMWARE_DOWNLOAD_GET")
    public void handleFirmwareDownloadGet(ZWaveCommandClassPayload payload, int endpoint) {
        ReportGet r = ReportGet.fromBytes(payload.getPayloadBuffer());
        logger.debug("NODE {}: Received Firmware Download Get", getNode().getNodeId());
        logger.debug("NODE {}: Number of reports = {}", getNode().getNodeId(), r.numReports);
        logger.debug("NODE {}: Report number = {}", getNode().getNodeId(), r.reportNumber);
    }

    public enum FirmwareUpdateStatus {
        OK(0),
        FAIL(1);

        private final int id;

        FirmwareUpdateStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static FirmwareUpdateStatus from(int v) {
            return v == 0 ? OK : FAIL;
        }
    }

    public enum FirmwareUpdateRequestStatus {
        Error_InvalidManufacturerOrFirmwareID(0),
        Error_AuthenticationExpected(1),
        Error_FragmentSizeTooLarge(2),
        Error_NotUpgradable(3),
        Error_InvalidHardwareVersion(4),
        Error_FirmwareUpgradeInProgress(5),
        Error_BatteryLow(6),
        OK(0xff),
        INVALID(-1);

        private final int id;

        FirmwareUpdateRequestStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static FirmwareUpdateRequestStatus from(int v) {
            for (FirmwareUpdateRequestStatus s : values()) {
                if (s.id == v) {
                    return s;
                }
            }
            return INVALID;
        }
    }

    public enum FirmwareDownloadStatus {
        OK(0), FAILED(1);

        private final int id;

        FirmwareDownloadStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static FirmwareDownloadStatus from(int v) {
            return v == 0 ? OK : FAILED;
        }
    }

    public enum FirmwareUpdateActivationStatus {
        SUCCESS(0), FAILURE(1);

        private final int id;

        FirmwareUpdateActivationStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static FirmwareUpdateActivationStatus from(int v) {
            return v == 0 ? SUCCESS : FAILURE;
        }
    }

    /* CRC16-CCITT (poly 0x1021) implementation */
    public static int crc16Ccitt(byte[] data, int initial) {
        int crc = initial & 0xffff;
        for (byte b : data) {
            crc ^= (b & 0xff) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
            crc &= 0xffff;
        }
        return crc & 0xffff;
    }

    /* ---------- Metadata report (MetaDataReport) ---------- */
    public static class MetaDataReport {
        public final int manufacturerId; // used elsewhere
        public final int firmwareId;
        public final int checksum;
        public final boolean firmwareUpgradable;
        public final @Nullable Integer maxFragmentSize; // nullable
        public final List<Integer> additionalFirmwareIDs;
        public final @Nullable Integer hardwareVersion;
        public final @Nullable Boolean continuesToFunction;
        public final @Nullable Boolean supportsActivation;
        public final @Nullable Boolean supportsResuming;
        public final @Nullable Boolean supportsNonSecureTransfer;

        public MetaDataReport(
                int manufacturerId, int firmwareId, int checksum,
                boolean firmwareUpgradable, @Nullable Integer maxFragmentSize,
                @Nullable List<Integer> additionalFirmwareIDs, @Nullable Integer hardwareVersion,
                @Nullable Boolean continuesToFunction, @Nullable Boolean supportsActivation,
                @Nullable Boolean supportsResuming, @Nullable Boolean supportsNonSecureTransfer) {
            this.manufacturerId = manufacturerId;
            this.firmwareId = firmwareId;
            this.checksum = checksum;
            this.firmwareUpgradable = firmwareUpgradable;
            this.maxFragmentSize = maxFragmentSize;
            this.additionalFirmwareIDs = additionalFirmwareIDs != null ? additionalFirmwareIDs : new ArrayList<>();
            this.hardwareVersion = hardwareVersion;
            this.continuesToFunction = continuesToFunction;
            this.supportsActivation = supportsActivation;
            this.supportsResuming = supportsResuming;
            this.supportsNonSecureTransfer = supportsNonSecureTransfer;
        }

        public static MetaDataReport fromBytes(byte[] payload, int ccVersion) {
            if (payload == null) {
                throw new IllegalArgumentException("payload is null");
            }

            // Some callers provide the full payload including the Command Class and
            // command id prefix (0x7A, FIRMWARE_MD_REPORT). If present, skip these
            // two bytes so parsing aligns with the expected fields.

            if (payload.length < 8) {
                throw new IllegalArgumentException("payload too short");
            }

            ByteBuffer bb = ByteBuffer.wrap(payload, 2, payload.length - 2);
            int manufacturerId = bb.getShort() & 0xffff;
            int firmwareId = bb.getShort() & 0xffff;
            int checksum = bb.getShort() & 0xffff;
            boolean firmwareUpgradable = true;
            if (payload.length >= 9) {
                int b6 = payload[8] & 0xff;
                firmwareUpgradable = (b6 == 0xff);
            }

            Integer maxFragmentSize = null;
            List<Integer> additionalFirmwareIDs = new ArrayList<>();
            Integer hardwareVersion = null;
            Boolean continuesToFunction = null, supportsActivation = null, supportsResuming = null,
                    supportsNonSecureTransfer = null;

            if (payload.length >= 12) {
                int numAdditional = payload[9] & 0xff;
                maxFragmentSize = ((payload[10] & 0xff) << 8) | (payload[11] & 0xff);
                int expected = 12 + 2 * numAdditional;
                if (payload.length < expected) {
                    throw new IllegalArgumentException("payload too short for additional firmwares");
                }
                for (int i = 0; i < numAdditional; i++) {
                    int id = ((payload[12 + 2 * i] & 0xff) << 8) | (payload[12 + 2 * i + 1] & 0xff);
                    additionalFirmwareIDs.add(id);
                }
                int payloadIndex = 12 + 2 * numAdditional;
                if (payload.length >= payloadIndex + 1) {
                    hardwareVersion = payload[payloadIndex] & 0xff;
                    payloadIndex++;
                    if (payload.length >= payloadIndex + 1) {
                        int capabilities = payload[payloadIndex] & 0xff;
                        continuesToFunction = (capabilities & 0b1) != 0;
                        supportsActivation = (capabilities & 0b10) != 0;
                        supportsNonSecureTransfer = (capabilities & 0b100) != 0;
                        supportsResuming = (capabilities & 0b1000) != 0;
                    }
                }
            }

            return new MetaDataReport(
                    manufacturerId, firmwareId, checksum, firmwareUpgradable,
                    maxFragmentSize, additionalFirmwareIDs, hardwareVersion,
                    continuesToFunction, supportsActivation, supportsResuming,
                    supportsNonSecureTransfer);
        }

        /**
         * Serialize the report to bytes, suitable for sending as a command payload.
         * 
         * @param ccVersion
         * @return byte array containing the serialized report, without the command
         *         class or command id prefix.
         *         The caller should prepend these as needed.
         */
        public byte[] toBytes(int ccVersion) {
            int baseLen = 10 + 2 * additionalFirmwareIDs.size() + 2; // conservative
            ByteBuffer bb = ByteBuffer.allocate(baseLen);
            bb.putShort((short) manufacturerId);
            bb.putShort((short) firmwareId);
            bb.putShort((short) checksum);
            bb.put((byte) (firmwareUpgradable ? 0xff : 0x00));
            bb.put((byte) additionalFirmwareIDs.size());
            bb.putShort((short) (maxFragmentSize != null ? maxFragmentSize : 0xff));
            for (int id : additionalFirmwareIDs) {
                bb.putShort((short) id);
            }
            bb.put((byte) (hardwareVersion != null ? hardwareVersion : 0xff));
            int caps = 0;
            if (Boolean.TRUE.equals(continuesToFunction)) {
                caps |= 0b1;
            }
            if (Boolean.TRUE.equals(supportsActivation)) {
                caps |= 0b10;
            }
            if (Boolean.TRUE.equals(supportsNonSecureTransfer)) {
                caps |= 0b100;
            }
            if (Boolean.TRUE.equals(supportsResuming)) {
                caps |= 0b1000;
            }
            bb.put((byte) caps);
            return Arrays.copyOf(bb.array(), bb.position());
        }
    }

    /* ---------- ReportFragment ---------- */
    public static class ReportFragment {
        public final boolean isLast;
        public final int reportNumber;
        public final byte[] firmwareData;
        public final @Nullable Integer crc16; // nullable for v1

        public ReportFragment(boolean isLast, int reportNumber, byte[] firmwareData, @Nullable Integer crc16) {
            this.isLast = isLast;
            this.reportNumber = reportNumber;
            this.firmwareData = firmwareData;
            this.crc16 = crc16;
        }

        public static ReportFragment fromBytes(byte[] payload, int ccVersion) {
            if (payload == null || payload.length < 2) {
                throw new IllegalArgumentException("payload too short");
            }
            int word = ((payload[0] & 0xff) << 8) | (payload[1] & 0xff);
            boolean isLast = (word & 0x8000) != 0;
            int reportNumber = word & 0x7fff;
            if (ccVersion >= 2) {
                if (payload.length < 4) {
                    throw new IllegalArgumentException("payload too short for crc");
                }
                int crc = ((payload[payload.length - 2] & 0xff) << 8) | (payload[payload.length - 1] & 0xff);
                byte[] data = Arrays.copyOfRange(payload, 2, payload.length - 2);
                return new ReportFragment(isLast, reportNumber, data, crc);
            } else {
                byte[] data = Arrays.copyOfRange(payload, 2, payload.length);
                return new ReportFragment(isLast, reportNumber, data, null);
            }
        }

        public byte[] toBytes(int ccVersion, int ccId, int ccCommand) {
            int len = 2 + firmwareData.length + (ccVersion >= 2 ? 2 : 0);
            ByteBuffer bb = ByteBuffer.allocate(len);
            int word = (reportNumber & 0x7fff) | (isLast ? 0x8000 : 0);
            bb.putShort((short) word);
            bb.put(firmwareData);
            if (ccVersion >= 2) {
                byte[] commandBuffer = Arrays.copyOfRange(bb.array(), 0, bb.position());
                int crc = crc16Ccitt(new byte[] { (byte) ccId, (byte) ccCommand }, 0xffff);
                crc = crc16Ccitt(commandBuffer, crc);
                bb.putShort((short) crc);
            }
            return bb.array();
        }
    }

    /* ---------- RequestReport ---------- */
    public static class RequestReport {
        public final FirmwareUpdateRequestStatus status;
        public final @Nullable Boolean resume;
        public final @Nullable Boolean nonSecureTransfer;

        public RequestReport(FirmwareUpdateRequestStatus status, @Nullable Boolean resume,
                @Nullable Boolean nonSecureTransfer) {
            this.status = status;
            this.resume = resume;
            this.nonSecureTransfer = nonSecureTransfer;
        }

        public static RequestReport fromBytes(byte[] payload) {
            if (payload == null || payload.length < 3) {
                throw new IllegalArgumentException("payload too short");
            }
            FirmwareUpdateRequestStatus status = FirmwareUpdateRequestStatus.from(payload[2] & 0xff);
            Boolean resume = null, nonSecure = null;
            if (payload.length >= 4) {
                int flags = payload[3] & 0xff;
                resume = (flags & 0b100) != 0;
                nonSecure = (flags & 0b10) != 0;
            }
            return new RequestReport(status, resume, nonSecure);
        }

        public byte[] toBytes() {
            byte[] out = new byte[2];
            out[0] = (byte) status.getId();
            int flags = (resume != null && resume ? 0b100 : 0)
                    | (nonSecureTransfer != null && nonSecureTransfer ? 0b10 : 0);
            out[1] = (byte) flags;
            return out;
        }
    }

    /* ---------- RequestGet ---------- */
    public static class RequestGet {
        public final int manufacturerId;
        public final int firmwareId;
        public final int checksum;
        public final @Nullable Integer firmwareTarget;
        public final @Nullable Integer fragmentSize;
        public final @Nullable Boolean activation;
        public final @Nullable Integer hardwareVersion;
        public final @Nullable Boolean resume;
        public final @Nullable Boolean nonSecureTransfer;

        public RequestGet(int manufacturerId, int firmwareId, int checksum,
                @Nullable Integer firmwareTarget, @Nullable Integer fragmentSize, @Nullable Boolean activation,
                @Nullable Boolean resume, @Nullable Boolean nonSecureTransfer, @Nullable Integer hardwareVersion) {
            this.manufacturerId = manufacturerId;
            this.firmwareId = firmwareId;
            this.checksum = checksum;
            this.firmwareTarget = firmwareTarget;
            this.fragmentSize = fragmentSize;
            this.activation = activation;
            this.hardwareVersion = hardwareVersion;
            this.resume = resume;
            this.nonSecureTransfer = nonSecureTransfer;
        }

        public static RequestGet fromBytes(byte[] payload) {
            if (payload == null || payload.length < 8) {
                throw new IllegalArgumentException("payload too short");
            }
            int manufacturerId = ((payload[2] & 0xff) << 8) | (payload[3] & 0xff);
            int firmwareId = ((payload[4] & 0xff) << 8) | (payload[5] & 0xff);
            int checksum = ((payload[6] & 0xff) << 8) | (payload[7] & 0xff);
            if (payload.length < 9) {
                return new RequestGet(manufacturerId, firmwareId, checksum, null, null, null, null, null, null);
            }
            int firmwareTarget = payload[8] & 0xff;
            int fragmentSize = ((payload[10] & 0xff) << 8) | (payload[9] & 0xff);
            Boolean activation = null, nonSecure = null, resume = null;
            Integer hardwareVersion = null;
            if (payload.length >= 11) {
                int flags = payload[11] & 0xff;
                activation = (flags & 0b1) != 0;
                nonSecure = (flags & 0b10) != 0;
                resume = (flags & 0b100) != 0;
            }
            if (payload.length >= 12) {
                hardwareVersion = payload[12] & 0xff;
            }
            return new RequestGet(manufacturerId, firmwareId, checksum, firmwareTarget, fragmentSize, activation,
                    resume, nonSecure, hardwareVersion);
        }

        public byte[] toBytes() {
            ByteBuffer bb = ByteBuffer.allocate(11);
            bb.putShort((short) manufacturerId);
            bb.putShort((short) firmwareId);
            bb.putShort((short) checksum);
            bb.put((byte) (firmwareTarget != null ? firmwareTarget : 0));
            bb.putShort((short) (fragmentSize != null ? fragmentSize : 32));
            int flags = (activation != null && activation ? 0b1 : 0)
                    | (nonSecureTransfer != null && nonSecureTransfer ? 0b10 : 0)
                    | (resume != null && resume ? 0b100 : 0);
            bb.put((byte) flags);
            if (hardwareVersion != null) {
                bb.put((byte) (hardwareVersion & 0xff));
            }
            return Arrays.copyOf(bb.array(), bb.position());
        }
    }

    /* ---------- PrepareReport ---------- */
    public static class PrepareReport {
        public final FirmwareDownloadStatus status;
        public final int checksum;

        public PrepareReport(FirmwareDownloadStatus status, int checksum) {
            this.status = status;
            this.checksum = checksum;
        }

        public static PrepareReport fromBytes(byte[] payload) {
            if (payload == null || payload.length < 3) {
                throw new IllegalArgumentException("payload too short");
            }
            FirmwareDownloadStatus status = FirmwareDownloadStatus.from(payload[0] & 0xff);
            int checksum = ((payload[1] & 0xff) << 8) | (payload[2] & 0xff);
            return new PrepareReport(status, checksum);
        }

        public byte[] toBytes() {
            byte[] out = new byte[3];
            out[0] = (byte) status.getId();
            out[1] = (byte) ((checksum >> 8) & 0xff);
            out[2] = (byte) (checksum & 0xff);
            return out;
        }
    }

    /* ---------- PrepareGet ---------- */
    public static class PrepareGet {
        public final int manufacturerId;
        public final int firmwareId;
        public final int firmwareTarget;
        public final int fragmentSize;
        public final int hardwareVersion;

        public PrepareGet(int manufacturerId, int firmwareId, int firmwareTarget, int fragmentSize,
                int hardwareVersion) {
            this.manufacturerId = manufacturerId;
            this.firmwareId = firmwareId;
            this.firmwareTarget = firmwareTarget;
            this.fragmentSize = fragmentSize;
            this.hardwareVersion = hardwareVersion;
        }

        public static PrepareGet fromBytes(byte[] payload) {
            if (payload == null || payload.length < 8) {
                throw new IllegalArgumentException("payload too short");
            }
            int manufacturerId = ((payload[0] & 0xff) << 8) | (payload[1] & 0xff);
            int firmwareId = ((payload[2] & 0xff) << 8) | (payload[3] & 0xff);
            int firmwareTarget = payload[4] & 0xff;
            int fragmentSize = ((payload[5] & 0xff) << 8) | (payload[6] & 0xff);
            int hardwareVersion = payload[7] & 0xff;
            return new PrepareGet(manufacturerId, firmwareId, firmwareTarget, fragmentSize, hardwareVersion);
        }

        public byte[] toBytes() {
            ByteBuffer bb = ByteBuffer.allocate(8);
            bb.putShort((short) manufacturerId);
            bb.putShort((short) firmwareId);
            bb.put((byte) (firmwareTarget & 0xff));
            bb.putShort((short) (fragmentSize & 0xffff));
            bb.put((byte) (hardwareVersion & 0xff));
            return bb.array();
        }
    }

    /* ---------- ActivationReport ---------- */
    public static class ActivationReport {
        public final int manufacturerId;
        public final int firmwareId;
        public final int checksum;
        public final int firmwareTarget;
        public final FirmwareUpdateActivationStatus activationStatus;
        public final @Nullable Integer hardwareVersion;

        public ActivationReport(int manufacturerId, int firmwareId, int checksum, int firmwareTarget,
                FirmwareUpdateActivationStatus activationStatus, @Nullable Integer hardwareVersion) {
            this.manufacturerId = manufacturerId;
            this.firmwareId = firmwareId;
            this.checksum = checksum;
            this.firmwareTarget = firmwareTarget;
            this.activationStatus = activationStatus;
            this.hardwareVersion = hardwareVersion;
        }

        public static ActivationReport fromBytes(byte[] payload) {
            if (payload == null || payload.length < 8) {
                throw new IllegalArgumentException("payload too short");
            }
            int manufacturerId = ((payload[0] & 0xff) << 8) | (payload[1] & 0xff);
            int firmwareId = ((payload[2] & 0xff) << 8) | (payload[3] & 0xff);
            int checksum = ((payload[4] & 0xff) << 8) | (payload[5] & 0xff);
            int firmwareTarget = payload[6] & 0xff;
            FirmwareUpdateActivationStatus status = FirmwareUpdateActivationStatus.from(payload[7] & 0xff);
            Integer hw = null;
            if (payload.length >= 9) {
                hw = payload[8] & 0xff;
            }
            return new ActivationReport(manufacturerId, firmwareId, checksum, firmwareTarget, status, hw);
        }

        public byte[] toBytes() {
            ByteBuffer bb = ByteBuffer.allocate(hardwareVersion != null ? 9 : 8);
            bb.putShort((short) manufacturerId);
            bb.putShort((short) firmwareId);
            bb.putShort((short) checksum);
            bb.put((byte) (firmwareTarget & 0xff));
            bb.put((byte) activationStatus.getId());
            if (hardwareVersion != null) {
                bb.put((byte) (hardwareVersion & 0xff));
            }
            return Arrays.copyOf(bb.array(), bb.position());
        }
    }

    /* ---------- ActivationSet ---------- */
    public static class ActivationSet {
        public final int manufacturerId;
        public final int firmwareId;
        public final int checksum;
        public final int firmwareTarget;
        public final @Nullable Integer hardwareVersion;

        public ActivationSet(int manufacturerId, int firmwareId, int checksum, int firmwareTarget,
                @Nullable Integer hardwareVersion) {
            this.manufacturerId = manufacturerId;
            this.firmwareId = firmwareId;
            this.checksum = checksum;
            this.firmwareTarget = firmwareTarget;
            this.hardwareVersion = hardwareVersion;
        }

        public static ActivationSet fromBytes(byte[] payload) {
            if (payload == null || payload.length < 7) {
                throw new IllegalArgumentException("payload too short");
            }
            int manufacturerId = ((payload[0] & 0xff) << 8) | (payload[1] & 0xff);
            int firmwareId = ((payload[2] & 0xff) << 8) | (payload[3] & 0xff);
            int checksum = ((payload[4] & 0xff) << 8) | (payload[5] & 0xff);
            int firmwareTarget = payload[6] & 0xff;
            Integer hw = null;
            if (payload.length >= 8) {
                hw = payload[7] & 0xff;
            }
            return new ActivationSet(manufacturerId, firmwareId, checksum, firmwareTarget, hw);
        }

        public byte[] toBytes() {
            ByteBuffer bb = ByteBuffer.allocate(hardwareVersion != null ? 8 : 7);
            bb.putShort((short) manufacturerId);
            bb.putShort((short) firmwareId);
            bb.putShort((short) checksum);
            bb.put((byte) (firmwareTarget & 0xff));
            if (hardwareVersion != null) {
                bb.put((byte) (hardwareVersion & 0xff));
            }
            return Arrays.copyOf(bb.array(), bb.position());
        }
    }

    /* ---------- StatusReport ---------- */
    public static class StatusReport {
        public final FirmwareUpdateStatus status;
        public final @Nullable Integer waitTime; // seconds

        public StatusReport(FirmwareUpdateStatus status, @Nullable Integer waitTime) {
            this.status = status;
            this.waitTime = waitTime;
        }

        public static StatusReport fromBytes(byte[] payload) {
            if (payload == null || payload.length < 1) {
                throw new IllegalArgumentException("payload too short");
            }
            FirmwareUpdateStatus status = FirmwareUpdateStatus.from(payload[0] & 0xff);
            Integer wait = null;
            if (payload.length >= 3) {
                wait = ((payload[1] & 0xff) << 8) | (payload[2] & 0xff);
            }
            return new StatusReport(status, wait);
        }

        public byte[] toBytes() {
            byte[] out = new byte[3];
            out[0] = (byte) status.getId();
            out[1] = (byte) ((waitTime != null ? (waitTime >> 8) & 0xff : 0));
            out[2] = (byte) ((waitTime != null ? waitTime & 0xff : 0));
            return out;
        }
    }

    /* ---------- Get (report request) and MetaDataGet ---------- */
    public static class ReportGet {
        public final int numReports;
        public final int reportNumber;

        public ReportGet(int numReports, int reportNumber) {
            this.numReports = numReports;
            this.reportNumber = reportNumber;
        }

        public static ReportGet fromBytes(byte[] payload) {
            if (payload == null || payload.length < 5) {
                throw new IllegalArgumentException("payload too short");
            }
            int numReports = payload[2] & 0xff;
            int reportNumber = ((payload[3] & 0xff) << 8) | (payload[4] & 0xff);
            reportNumber = reportNumber & 0x7fff;
            return new ReportGet(numReports, reportNumber);
        }

        public byte[] toBytes() {
            ByteBuffer bb = ByteBuffer.allocate(3);
            bb.put((byte) numReports);
            bb.putShort((short) (reportNumber & 0x7fff));
            return bb.array();
        }
    }
}
