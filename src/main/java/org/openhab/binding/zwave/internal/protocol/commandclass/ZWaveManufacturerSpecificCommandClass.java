/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

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
public class ZWaveManufacturerSpecificCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveManufacturerSpecificCommandClass.class);

    private static final int MANUFACTURER_SPECIFIC_GET = 0x04;
    private static final int MANUFACTURER_SPECIFIC_REPORT = 0x05;

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
                logger.trace("Process Manufacturer Specific Report");

                int tempMan = ((serialMessage.getMessagePayloadByte(offset + 1)) << 8)
                        | (serialMessage.getMessagePayloadByte(offset + 2));
                int tempDeviceType = ((serialMessage.getMessagePayloadByte(offset + 3)) << 8)
                        | (serialMessage.getMessagePayloadByte(offset + 4));
                int tempDeviceId = ((serialMessage.getMessagePayloadByte(offset + 5)) << 8)
                        | (serialMessage.getMessagePayloadByte(offset + 6));

                this.getNode().setManufacturer(tempMan);
                this.getNode().setDeviceType(tempDeviceType);
                this.getNode().setDeviceId(tempDeviceId);

                logger.debug(String.format("NODE %d: Manufacturer ID = 0x%04x", this.getNode().getNodeId(),
                        this.getNode().getManufacturer()));
                logger.debug(String.format("NODE %d: Device Type     = 0x%04x", this.getNode().getNodeId(),
                        this.getNode().getDeviceType()));
                logger.debug(String.format("NODE %d: Device ID       = 0x%04x", this.getNode().getNodeId(),
                        this.getNode().getDeviceId()));
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        this.getNode().getNodeId(), command, this.getCommandClass().getLabel(),
                        this.getCommandClass().getKey()));
        }
    }

    /**
     * Gets a SerialMessage with the ManufacturerSpecific GET command
     *
     * @return the serial message
     */
    public ZWaveTransaction getManufacturerSpecificMessage() {
        logger.debug("NODE {}: Creating new message for command MANUFACTURER_SPECIFIC_GET", this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), MANUFACTURER_SPECIFIC_GET).withNodeId(getNode().getNodeId())
                .build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), MANUFACTURER_SPECIFIC_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

}
