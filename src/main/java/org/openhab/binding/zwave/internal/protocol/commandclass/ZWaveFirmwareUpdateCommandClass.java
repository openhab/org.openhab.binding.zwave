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
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwave.firmwareupdate.ZWaveFirmwareUpdateSession.FirmwareUpdateEvent;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final int MAX_SUPPORTED_VERSION = 8;
    private static final int CRC16_CCITT_INITIAL = 0x1D0F;

    public static final int FIRMWARE_MD_GET = 0x01; // To Device
    public static final int FIRMWARE_MD_REPORT = 0x02; // From Device
    public static final int FIRMWARE_UPDATE_MD_REQUEST_GET = 0x03; // To Device
    public static final int FIRMWARE_UPDATE_MD_REQUEST_REPORT = 0x04; // From Device
    public static final int FIRMWARE_UPDATE_MD_GET = 0x05; // From Device if ready to receive.
    public static final int FIRMWARE_UPDATE_MD_REPORT = 0x06; // To Device for fragment data.
    public static final int FIRMWARE_UPDATE_MD_STATUS_REPORT = 0x07; // From Device
    public static final int FIRMWARE_UPDATE_ACTIVATION_SET = 0x08; // To Device
    public static final int FIRMWARE_UPDATE_ACTIVATION_STATUS_REPORT = 0x09; // From Device
    public static final int FIRMWARE_UPDATE_PREPARE_GET = 0x0A; // To Device requesting it send current firmware -Future
    public static final int FIRMWARE_UPDATE_PREPARE_REPORT = 0x0B; // From Device current firmware to binding -Future

    /**
     * Creates a new instance of the ZWaveFirmwareUpdateCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveFirmwareUpdateCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public void initialise(@Nullable ZWaveNode node, @Nullable ZWaveController controller,
            @Nullable ZWaveEndpoint endpoint) {
        super.initialise(node, controller, endpoint);
        // versionMax is @XStreamOmitField so it is not persisted. Restore it here so that
        // setVersion() does not cap the version to 0 when called on a deserialized node.
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_FIRMWARE_UPDATE_MD;
    }

    /**
     * Create a transaction payload for Firmware Meta Data Get (1).
     * The message requests the supporting node to return a Firmware Meta Data
     * Report (2).
     */
    public ZWaveCommandClassTransactionPayload sendMDGetMessage() {
        logger.debug("NODE {}: Creating new message for application command FIRMWARE_MD_GET",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), FIRMWARE_MD_GET)
                .withPriority(TransactionPriority.Config).withExpectedResponseCommand(FIRMWARE_MD_REPORT).build();
    }

    /**
     * Create a transaction payload for Firmware Update MD Request Get (3).
     * The message requests the supporting node to return a Firmware Update MD
     * Request Report (4) indicating whether the device is ready to receive the
     * firmware data and whether this is a resume of an interrupted update.
     */
    public ZWaveCommandClassTransactionPayload sendMDRequestGetMessage(byte[] payload) {
        logger.debug("NODE {}: Creating new message for FIRMWARE_MD_REQUEST_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                FIRMWARE_UPDATE_MD_REQUEST_GET).withPayload(payload).withPriority(TransactionPriority.Config)
                .withExpectedResponseCommand(FIRMWARE_UPDATE_MD_REQUEST_REPORT).build();
    }

    /**
     * Create a transaction payload for Firmware Update MD Report (6).
     * This sends a single firmware fragment to the device.
     */
    public ZWaveCommandClassTransactionPayload sendFirmwareUpdateReport(FirmwareFragment fragment) {
        logger.debug("NODE {}: Creating FIRMWARE_UPDATE_MD_REPORT for fragment {}, isLast={}", getNode().getNodeId(),
                fragment.reportNumber, fragment.isLast);

        byte[] payload = fragment.toBytes(getVersion(), getCommandClass().getKey(), FIRMWARE_UPDATE_MD_REPORT);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                FIRMWARE_UPDATE_MD_REPORT).withPayload(payload).withPriority(TransactionPriority.Config).build();
    }

    /**
     * Create a transaction payload for Firmware Update MD Get (5).
     * This requests one or more firmware fragments from the node.
     */
    public ZWaveCommandClassTransactionPayload sendFirmwareUpdateMdGet(int reportNumber, int numberOfReports) {
        logger.debug("NODE {}: Creating new message for FIRMWARE_UPDATE_MD_GET report={}, count={}",
                getNode().getNodeId(), reportNumber, numberOfReports);

        byte[] payload = new byte[] { (byte) (numberOfReports & 0xFF), (byte) ((reportNumber >> 8) & 0xFF),
                (byte) (reportNumber & 0xFF) };

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                FIRMWARE_UPDATE_MD_GET).withPayload(payload).withPriority(TransactionPriority.Config)
                .withExpectedResponseCommand(FIRMWARE_UPDATE_MD_REPORT).build();
    }

    /**
     * Create a transaction payload for Firmware Update Activation Set (8).
     * The message initiates the activation of the new firmware after all fragments
     * have been sent.
     * The device will respond with a Firmware Update Activation Status Report (9)
     * indicating the result.
     */
    public ZWaveCommandClassTransactionPayload setFirmwareActivation(byte[] firmwareBaseData) {
        logger.debug("NODE {}: Creating new message for FIRMWARE_UPDATE_ACTIVATION_SET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                FIRMWARE_UPDATE_ACTIVATION_SET).withPayload(firmwareBaseData).withPriority(TransactionPriority.Config)
                .withExpectedResponseCommand(FIRMWARE_UPDATE_ACTIVATION_STATUS_REPORT).build();
    }

    /**
     * Create a transaction payload for Firmware Update Prepare Get (10).
     * The message requests the device to prepare to send its current firmware
     * information, which will be returned in a Firmware Update Prepare Report (11).
     * This can be used to retrieve the current firmware information before starting
     * an update.
     * Payload format:
     * manufacturerId(2), firmwareId(2), firmwareTarget(1), fragmentSize(2),
     * [hardwareVersion(1)].
     */
    public ZWaveCommandClassTransactionPayload setFirmwarePrepareGet(byte[] prepareRequestData) {
        logger.debug("NODE {}: Creating new message for FIRMWARE_UPDATE_PREPARE_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                FIRMWARE_UPDATE_PREPARE_GET).withPayload(prepareRequestData).withPriority(TransactionPriority.Config)
                .withExpectedResponseCommand(FIRMWARE_UPDATE_PREPARE_REPORT).build();
    }

    /**
     * Handle Firmware Meta Data Report (2) from device, which contains information
     * about the firmware and the device's capabilities related to firmware update.
     * The payload contains manufacturer ID, firmware ID, checksum, max fragment
     * size and optionally hardware version.
     * 
     * @param payload
     * @param endpoint
     */
    @ZWaveResponseHandler(id = FIRMWARE_MD_REPORT, name = "FIRMWARE_MD_REPORT")
    public void handleMetaDataReport(ZWaveCommandClassPayload payload, int endpoint) {
        byte[] data = payload.getPayloadBuffer();

        byte[] payloadMD = Arrays.copyOfRange(data, 2, data.length);

        // Payload is processed in the session. As the information is used for the session.
        getController()
                .notifyEventListeners(FirmwareUpdateEvent.forMDReport(getNode().getNodeId(), endpoint, payloadMD));
    }

    /**
     * Handle Firmware Update MD Request Report (4) from device, which indicates the
     * result of the firmware update request and whether the device is ready to
     * receive the firmware data.
     * The payload contains status byte and optional flags for versions.
     * 
     * @param payload
     * @param endpoint
     */
    @ZWaveResponseHandler(id = FIRMWARE_UPDATE_MD_REQUEST_REPORT, name = "FIRMWARE_UPDATE_MD_REQUEST_REPORT")
    public void handleMetaDataRequestReport(ZWaveCommandClassPayload payload, int endpoint) {
        byte[] data = payload.getPayloadBuffer();

        if (data.length < 3) {
            logger.warn("NODE {}: Firmware Update MD Request Report payload too short", getNode().getNodeId());
            getController().notifyEventListeners(
                    FirmwareUpdateEvent.forUpdateMdRequestReport(getNode().getNodeId(), endpoint, -1, null, null));
            return;
        }

        int status = data[2] & 0xFF;

        @Nullable
        Boolean resume = null;
        @Nullable
        Boolean nonSecure = null;

        if (data.length >= 4) {
            int flags = data[3] & 0xFF;
            resume = (flags & 0b100) != 0;
            nonSecure = (flags & 0b10) != 0;
        }

        getController().notifyEventListeners(FirmwareUpdateEvent.forUpdateMdRequestReport(getNode().getNodeId(),
                endpoint, status, resume, nonSecure));
    }

    /**
     * Handle Firmware Update MD Get (5) from device, which indicates that the
     * device is ready to receive the next firmware fragment. The payload contains
     * the report number and total number of reports.
     * 
     * @param payload
     * @param endpoint
     */
    @ZWaveResponseHandler(id = FIRMWARE_UPDATE_MD_GET, name = "FIRMWARE_UPDATE_MD_GET")
    public void handleFirmwareDownloadGet(ZWaveCommandClassPayload payload, int endpoint) {
        byte[] data = payload.getPayloadBuffer();

        if (data.length < 5) {
            logger.debug("NODE {}: Firmware Download Get payload too short", getNode().getNodeId());
            return;
        }

        int numReports = data[2] & 0xFF;
        int reportNumber = ((data[3] & 0xFF) << 8) | (data[4] & 0xFF);
        reportNumber &= 0x7FFF; // mask reserved bit

        logger.debug("NODE {}: Received Firmware Download Get", getNode().getNodeId());
        logger.debug("NODE {}: Number of reports = {}", getNode().getNodeId(), numReports);
        logger.debug("NODE {}: Report number = {}", getNode().getNodeId(), reportNumber);

        getController().notifyEventListeners(
                FirmwareUpdateEvent.forUpdateMdGet(getNode().getNodeId(), endpoint, reportNumber, numReports));
    }

    /**
     * Handle Firmware Update MD Report (6) from device during firmware download.
     * Payload format matches fragment data layout and is routed to the firmware session.
     *
     * @param payload command payload
     * @param endpoint endpoint id
     */
    @ZWaveResponseHandler(id = FIRMWARE_UPDATE_MD_REPORT, name = "FIRMWARE_UPDATE_MD_REPORT")
    public void handleFirmwareUpdateMdReport(ZWaveCommandClassPayload payload, int endpoint) {
        byte[] data = payload.getPayloadBuffer();

        if (data.length < 4) {
            logger.debug("NODE {}: Firmware Update MD Report payload too short", getNode().getNodeId());
            return;
        }

        byte[] fragmentPayload = Arrays.copyOfRange(data, 2, data.length);

        getController().notifyEventListeners(
                FirmwareUpdateEvent.forMDReport(getNode().getNodeId(), endpoint, fragmentPayload));
    }

    @ZWaveResponseHandler(id = FIRMWARE_UPDATE_MD_STATUS_REPORT, name = "FIRMWARE_UPDATE_MD_STATUS_REPORT")
    public void handleFirmwareUpdateMdStatusReport(ZWaveCommandClassPayload payload, int endpoint) {
        byte[] data = payload.getPayloadBuffer();

        if (data.length < 3) {
            logger.debug("NODE {}: Firmware Update MD Status Report payload too short", getNode().getNodeId());
            return;
        }

        int status = data[2] & 0xFF;
        int waitTime = 0;
        if (getVersion() >= 3 && data.length >= 5) {
            waitTime = ((data[3] & 0xFF) << 8) | (data[4] & 0xFF);
        }

        logger.debug("NODE {}: Received Firmware Update MD Status Report: status={}, waitTime={}",
                getNode().getNodeId(), status, waitTime);

        getController().notifyEventListeners(
                FirmwareUpdateEvent.forUpdateMdStatusReport(getNode().getNodeId(), endpoint, status, waitTime));
    }

    /**
     * Handle Firmware Update Activation Status Report (9) from device, which
     * indicates the result of the activation attempt.
     * The payload contains manufacturer ID, firmware ID, checksum, target,
     * activation status and optionally hardware version.
     * 
     * @param payload
     * @param endpoint
     */
    @ZWaveResponseHandler(id = FIRMWARE_UPDATE_ACTIVATION_STATUS_REPORT, name = "FIRMWARE_UPDATE_ACTIVATION_STATUS_REPORT")
    public void handleFirmwareActivationStatusReport(ZWaveCommandClassPayload payload, int endpoint) {
        byte[] data = payload.getPayloadBuffer();

        if (data.length < 11) {
            logger.debug("NODE {}: Firmware Activation Status Report payload too short", getNode().getNodeId());
            return;
        }
        // Skip CC + command (2 bytes)
        ByteBuffer bb = ByteBuffer.wrap(data, 2, data.length - 2);

        int manufacturerId = bb.getShort() & 0xFFFF;
        int firmwareId = bb.getShort() & 0xFFFF;
        int checksum = bb.getShort() & 0xFFFF;
        int firmwareTarget = bb.get() & 0xFF;
        FirmwareUpdateActivationStatus status = FirmwareUpdateActivationStatus.from(bb.get() & 0xFF);
        Integer hardwareVersion = null;
        if (bb.hasRemaining()) {
            hardwareVersion = Integer.valueOf(bb.get() & 0xFF);
        }

        logger.debug(
                "NODE {}: Received Firmware Activation Status Report: manufacturerId=0x{}, firmwareId=0x{}, checksum=0x{}, target={}, status={}, hwVersion={}",
                getNode().getNodeId(), Integer.toHexString(manufacturerId), Integer.toHexString(firmwareId),
                Integer.toHexString(checksum), firmwareTarget, status, hardwareVersion);

        getController().notifyEventListeners(
                FirmwareUpdateEvent.forActivationStatusReport(getNode().getNodeId(), endpoint, status.getId()));
    }

    /**
     * Handle Firmware Update Prepare Report (11) from device, which contains the
     * current firmware information of the device. The payload contains checksum and
     * status. Not implemented and used for now, but can be used to retrieve current firmware.
     * 
     * @param payload
     * @param endpoint
     */
    @ZWaveResponseHandler(id = FIRMWARE_UPDATE_PREPARE_REPORT, name = "FIRMWARE_UPDATE_PREPARE_REPORT")
    public void handleFirmwarePrepareReport(ZWaveCommandClassPayload payload, int endpoint) {
        byte[] data = payload.getPayloadBuffer();

        if (data.length < 5) {
            logger.debug("NODE {}: Firmware Prepare Report payload too short", getNode().getNodeId());
            return;
        }
        // Skip CC + command (2 bytes)
        ByteBuffer bb = ByteBuffer.wrap(data, 2, data.length - 2);

        FirmwareDownloadStatus status = FirmwareDownloadStatus.from(bb.get() & 0xFF);
        int checksum = bb.getShort() & 0xFFFF;

        logger.debug("NODE {}: Received Firmware Prepare Report: checksum=0x{}, status={}", getNode().getNodeId(),
                Integer.toHexString(checksum), status);

        getController().notifyEventListeners(
                FirmwareUpdateEvent.forUpdatePrepareReport(getNode().getNodeId(), endpoint, status.getId(), checksum));
    }

    public enum FirmwareDownloadStatus {
        INVALID_PAYLOAD(0x00),
        EXPECTED_AUTHORIZATION_EVENT(0x01),
        FRAGMENT_SIZE_EXCEEDED(0x02),
        FIRMWARE_TARGET_NOT_DOWNLOADABLE(0x03),
        INVALID_HARDWARE_VERSION(0x04),
        SUCCESS(0xFF),
        UNKNOWN(-1);

        private final int id;

        FirmwareDownloadStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static FirmwareDownloadStatus from(int v) {
            for (FirmwareDownloadStatus status : values()) {
                if (status.id == v) {
                    return status;
                }
            }
            return UNKNOWN;
        }
    }

    public enum FirmwareUpdateActivationStatus {
        INVALID_PAYLOAD(0x00),
        ERROR_ACTIVATING_FIRMWARE(0x01),
        SUCCESS(0xFF),
        UNKNOWN(-1);

        private final int id;

        FirmwareUpdateActivationStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public static FirmwareUpdateActivationStatus from(int v) {
            for (FirmwareUpdateActivationStatus status : values()) {
                if (status.id == v) {
                    return status;
                }
            }
            return UNKNOWN;
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

    /** Data structure representing a firmware fragment to be sent to the device. */
    public record FirmwareFragment(boolean isLast, int reportNumber, byte[] firmwareData, @Nullable Integer crc16) {
        public byte[] toBytes(int ccVersion, int ccId, int ccCommand) {
            int len = 2 + firmwareData.length + (ccVersion >= 2 ? 2 : 0);
            ByteBuffer bb = ByteBuffer.allocate(len);

            int word = (reportNumber & 0x7fff) | (isLast ? 0x8000 : 0);
            bb.putShort((short) word);
            bb.put(firmwareData);

            if (ccVersion >= 2) {
                int crc;
                if (crc16 != null) {
                    crc = crc16.intValue() & 0xFFFF;
                } else {
                    byte[] commandBuffer = Arrays.copyOfRange(bb.array(), 0, bb.position());
                    crc = crc16Ccitt(new byte[] { (byte) ccId, (byte) ccCommand }, CRC16_CCITT_INITIAL);
                    crc = crc16Ccitt(commandBuffer, crc);
                }
                bb.putShort((short) crc);
            }

            return bb.array();
        }
    }
}
