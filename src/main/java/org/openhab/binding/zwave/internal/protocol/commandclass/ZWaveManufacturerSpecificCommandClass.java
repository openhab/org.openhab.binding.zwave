/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Collection;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
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
@XStreamAlias("manufacturerSpecificCommandClass")
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

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.MANUFACTURER_SPECIFIC;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {

        logger.debug("NODE {}: Received Manufacture Specific Information", this.getNode().getNodeId());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case MANUFACTURER_SPECIFIC_REPORT:
                int tempMan = ((serialMessage.getMessagePayloadByte(offset + 1)) << 8)
                        | (serialMessage.getMessagePayloadByte(offset + 2));
                int tempDeviceType = ((serialMessage.getMessagePayloadByte(offset + 3)) << 8)
                        | (serialMessage.getMessagePayloadByte(offset + 4));
                int tempDeviceId = ((serialMessage.getMessagePayloadByte(offset + 5)) << 8)
                        | (serialMessage.getMessagePayloadByte(offset + 6));

                getNode().setManufacturer(tempMan);
                getNode().setDeviceType(tempDeviceType);
                getNode().setDeviceId(tempDeviceId);

                logger.debug(String.format("NODE %d: Manufacturer ID = 0x%04x", getNode().getNodeId(),
                        getNode().getManufacturer()));
                logger.debug(String.format("NODE %d: Device Type     = 0x%04x", getNode().getNodeId(),
                        getNode().getDeviceType()));
                logger.debug(String.format("NODE %d: Device ID       = 0x%04x", getNode().getNodeId(),
                        getNode().getDeviceId()));
                break;

            case MANUFACTURER_SPECIFIC_DEVICE_REPORT:
                int dataType = serialMessage.getMessagePayloadByte(offset + 1) & 0x07;
                int dataFormat = (serialMessage.getMessagePayloadByte(offset + 2) & 0xe0) >> 0x05;
                int dataLength = serialMessage.getMessagePayloadByte(offset + 2) & 0x1f;

                StringBuilder dataBuilder = new StringBuilder(32);
                for (int cnt = 0; cnt < dataLength; cnt++) {
                    if (dataFormat == 0) {
                        dataBuilder.append(serialMessage.getMessagePayloadByte(offset + cnt + 3));
                    } else {
                        dataBuilder
                                .append(String.format("%02X", serialMessage.getMessagePayloadByte(offset + cnt + 3)));
                    }
                }

                String data = dataBuilder.toString();

                // if (dataType == MANUFACTURER_TYPE_FACTORYDEFAULT) {
                // initFactoryDefault = true;
                // getNode().setFactoryId(data);
                // logger.debug("NODE {}: Factory Number = {}", getNode().getNodeId(), getNode().getFactoryId());
                // }
                if (dataType == MANUFACTURER_TYPE_SERIALNUMBER) {
                    initSerialNumber = true;
                    getNode().setSerialNumber(data);
                    logger.debug("NODE {}: Serial Number   = {}", getNode().getNodeId(), getNode().getSerialNumber());
                }
                break;

            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        getNode().getNodeId(), command, getCommandClass().getLabel(), getCommandClass().getKey()));
        }
    }

    /**
     * Gets a SerialMessage with the ManufacturerSpecific GET command
     *
     * @return the serial message
     */
    public ZWaveTransaction getManufacturerSpecificMessage() {
        logger.debug("NODE {}: Creating new message for command MANUFACTURER_SPECIFIC_GET", getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), MANUFACTURER_SPECIFIC_GET).withNodeId(getNode().getNodeId())
                .build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), MANUFACTURER_SPECIFIC_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the ManufacturerSpecific GET command
     *
     * @return the serial message
     */
    public ZWaveTransaction getManufacturerSpecificDeviceMessage(int type) {
        logger.debug("NODE {}: Creating new message for command MANUFACTURER_SPECIFIC_DEVICE_GET({})",
                getNode().getNodeId(), type);

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), MANUFACTURER_SPECIFIC_DEVICE_GET).withNodeId(getNode().getNodeId())
                .withPayload(type).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), MANUFACTURER_SPECIFIC_DEVICE_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

    @Override
    public Collection<ZWaveTransaction> initialize(boolean refresh) {
        if (getVersion() == 1) {
            return null;
        }

        if (refresh == true) {
            // initFactoryDefault = false;
            initSerialNumber = false;
        }

        ArrayList<ZWaveTransaction> result = new ArrayList<ZWaveTransaction>();
        // if (initFactoryDefault == false) {
        // result.add(getManufacturerSpecificDeviceMessage(MANUFACTURER_TYPE_FACTORYDEFAULT));
        // }
        if (initSerialNumber == false) {
            result.add(getManufacturerSpecificDeviceMessage(MANUFACTURER_TYPE_SERIALNUMBER));
        }

        return result;
    }

}
