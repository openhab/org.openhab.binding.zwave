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
import java.util.Map;

import org.openhab.binding.zwave.internal.HexToIntegerConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWavePlusDeviceClass.ZWavePlusDeviceType;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the ZWave Plus Command command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_ZWAVEPLUS_INFO")
public class ZWavePlusCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWavePlusCommandClass.class);

    private static final byte ZWAVE_PLUS_GET = 0x01;
    private static final byte ZWAVE_PLUS_REPORT = 0x02;

    @SuppressWarnings("unused")
    private int zwPlusVersion = 0;
    @SuppressWarnings("unused")
    private int zwPlusRole = 0;
    @SuppressWarnings("unused")
    private int zwPlusNodeType = 0;
    @XStreamConverter(HexToIntegerConverter.class)
    private int zwPlusDeviceType = 0;
    @XStreamConverter(HexToIntegerConverter.class)
    private int zwPlusInstallerIcon = 0;

    @XStreamOmitField
    private boolean initialiseDone = false;
    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWavePlusCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWavePlusCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_ZWAVEPLUS_INFO;
    }

    @ZWaveResponseHandler(id = ZWAVE_PLUS_REPORT, name = "ZWAVE_PLUS_REPORT")
    public void handleZwavePlusReport(ZWaveCommandClassPayload payload, int endpoint) {
        zwPlusVersion = payload.getPayloadByte(1);
        zwPlusRole = payload.getPayloadByte(2);
        zwPlusNodeType = payload.getPayloadByte(3);
        zwPlusInstallerIcon = (payload.getPayloadByte(4) << 8) | payload.getPayloadByte(5);
        zwPlusDeviceType = (payload.getPayloadByte(6) << 8) | payload.getPayloadByte(7);

        ZWavePlusDeviceType deviceType = ZWavePlusDeviceType.getZWavePlusDeviceType(zwPlusDeviceType);
        if (deviceType != null) {
            logger.debug("NODE {}: Adding mandatory command classes for ZWavePlus device type {}",
                    getNode().getNodeId(), deviceType);

            // Add all missing mandatory plus command classes
            for (CommandClass commandClass : deviceType.getMandatoryCommandClasses()) {
                ZWaveCommandClass zwaveCommandClass = this.getNode().getCommandClass(commandClass);

                // Add the mandatory class missing, ie not set via NIF
                if (zwaveCommandClass == null) {
                    zwaveCommandClass = ZWaveCommandClass.getInstance(commandClass.getKey(), getNode(),
                            getController());
                    if (zwaveCommandClass != null) {
                        logger.debug(String.format("NODE %d: Adding command class %s (0x%02x)", getNode().getNodeId(),
                                commandClass, commandClass.getKey()));
                        getNode().addCommandClass(zwaveCommandClass);
                    }
                }
            }
        } else {
            logger.info("NODE {}: unknown ZWavePlus device type: {}", getNode().getNodeId(), zwPlusDeviceType);
        }

        initialiseDone = true;
    }

    @Override
    public Collection<ZWaveTransaction> initialize(boolean refresh) {
        ArrayList<ZWaveTransaction> result = new ArrayList<ZWaveTransaction>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(this.getValueMessage());
        }
        return result;
    }

    @Override
    public ZWaveTransaction getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command ZWAVE_PLUS_GET",
                this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), ZWAVE_PLUS_GET).withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), ZWAVE_PLUS_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Return the ZWave Plus Device Type
     *
     * @return {@link ZWavePlusDeviceType}
     */
    public ZWavePlusDeviceType getZWavePlusDeviceType() {
        ZWavePlusDeviceType deviceType = ZWavePlusDeviceType.getZWavePlusDeviceType(zwPlusDeviceType);
        if (deviceType == null) {
            deviceType = ZWavePlusDeviceType.UNKNOWN_TYPE;
        }

        return deviceType;
    }
}
