/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Collection;

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
 * Handles the manufacturer specific command class. Class to request and report manufacturer specific information.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("COMMAND_CLASS_MANUFACTURER_SPECIFIC")
public class ZWaveManufacturerSpecificCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveManufacturerSpecificCommandClass.class);

    private static final int MANUFACTURER_SPECIFIC_GET = 0x04;
    private static final int MANUFACTURER_SPECIFIC_REPORT = 0x05;
    private static final int MANUFACTURER_SPECIFIC_DEVICE_GET = 0x06;
    private static final int MANUFACTURER_SPECIFIC_DEVICE_REPORT = 0x07;

    private static final int MANUFACTURER_TYPE_FACTORYDEFAULT = 0x00;
    private static final int MANUFACTURER_TYPE_SERIALNUMBER = 0x01;
    // private static final int MANUFACTURER_TYPE_PSEUDORANDOM = 0x02;

    // private boolean initFactoryDefault = false;
    private boolean initSerialNumber = false;

    private int deviceManufacturer = Integer.MAX_VALUE;
    private int deviceType = Integer.MAX_VALUE;
    private int deviceId = Integer.MAX_VALUE;
    private String deviceSerialNumber = null;

    /**
     * Creates a new instance of the ZwaveManufacturerSpecificCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveManufacturerSpecificCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_MANUFACTURER_SPECIFIC;
    }

    @ZWaveResponseHandler(id = MANUFACTURER_SPECIFIC_REPORT, name = "MANUFACTURER_SPECIFIC_REPORT")
    public void handleManufacturerSpecificReport(ZWaveCommandClassPayload payload, int endpoint) {
        deviceManufacturer = ((payload.getPayloadByte(2)) << 8) | (payload.getPayloadByte(3));
        deviceType = ((payload.getPayloadByte(4)) << 8) | (payload.getPayloadByte(5));
        deviceId = ((payload.getPayloadByte(6)) << 8) | (payload.getPayloadByte(7));

        getNode().setManufacturer(deviceManufacturer);
        getNode().setDeviceType(deviceType);
        getNode().setDeviceId(deviceId);

        logger.debug("NODE {}: Manufacturer ID = 0x{}", getNode().getNodeId(),
                Integer.toHexString(getNode().getManufacturer()));
        logger.debug("NODE {}: Device Type     = 0x{}", getNode().getNodeId(),
                Integer.toHexString(getNode().getDeviceType()));
        logger.debug("NODE {}: Device ID       = 0x{}", getNode().getNodeId(),
                Integer.toHexString(getNode().getDeviceId()));
    }

    @ZWaveResponseHandler(id = MANUFACTURER_SPECIFIC_DEVICE_REPORT, name = "MANUFACTURER_SPECIFIC_DEVICE_REPORT")
    public void handleManufacturerSpecificDeviceReport(ZWaveCommandClassPayload payload, int endpoint) {
        int dataType = payload.getPayloadByte(2) & 0x07;
        int dataFormat = (payload.getPayloadByte(3) & 0xe0) >> 0x05;
        int dataLength = payload.getPayloadByte(3) & 0x1f;

        StringBuilder dataBuilder = new StringBuilder(32);
        for (int cnt = 0; cnt < dataLength; cnt++) {
            if (dataFormat == 0) {
                dataBuilder.append(payload.getPayloadByte(cnt + 4));
            } else {
                dataBuilder.append(String.format("%02X", payload.getPayloadByte(cnt + 4)));
            }
        }

        String data = dataBuilder.toString();

        // if (dataType == MANUFACTURER_TYPE_FACTORYDEFAULT) {
        // initFactoryDefault = true;
        // getNode().setFactoryId(data);
        // logger.debug("NODE {}: Factory Number = {}", getNode().getNodeId(), getNode().getFactoryId());
        // }
        if (dataType == MANUFACTURER_TYPE_SERIALNUMBER) {
            deviceSerialNumber = data;
            initSerialNumber = true;
            getNode().setSerialNumber(data);
            logger.debug("NODE {}: Serial Number   = {}", getNode().getNodeId(), getNode().getSerialNumber());
        }
    }

    /**
     * Gets a SerialMessage with the ManufacturerSpecific GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getManufacturerSpecificMessage() {
        logger.debug("NODE {}: Creating new message for command MANUFACTURER_SPECIFIC_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MANUFACTURER_SPECIFIC_GET).withExpectedResponseCommand(MANUFACTURER_SPECIFIC_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the ManufacturerSpecific GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getManufacturerSpecificDeviceMessage(int type) {
        logger.debug("NODE {}: Creating new message for command MANUFACTURER_SPECIFIC_DEVICE_GET({})",
                getNode().getNodeId(), type);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MANUFACTURER_SPECIFIC_DEVICE_GET).withPayload(type)
                        .withExpectedResponseCommand(MANUFACTURER_SPECIFIC_DEVICE_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        if (getVersion() <= 1) {
            return null;
        }

        if (refresh == true) {
            // initFactoryDefault = false;
            initSerialNumber = false;
        }

        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        // if (initFactoryDefault == false) {
        // result.add(getManufacturerSpecificDeviceMessage(MANUFACTURER_TYPE_FACTORYDEFAULT));
        // }
        if (initSerialNumber == false) {
            result.add(getManufacturerSpecificDeviceMessage(MANUFACTURER_TYPE_SERIALNUMBER));
        }

        return result;
    }

    public int getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }
}
