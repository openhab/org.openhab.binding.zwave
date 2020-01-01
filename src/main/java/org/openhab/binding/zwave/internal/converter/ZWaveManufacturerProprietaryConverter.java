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
package org.openhab.binding.zwave.internal.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.commandclass.impl.CommandClassManufacturerProprietaryFibaroFgrm222V1;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveManufacturerProprietaryConverter class.
 * <p>
 * Currently this simply handles the Fibaro FGR222
 *
 * @author Chris Jackson
 */
public class ZWaveManufacturerProprietaryConverter extends ZWaveCommandClassConverter {

    private final Logger logger = LoggerFactory.getLogger(ZWaveManufacturerProprietaryConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveManufacturerProprietaryConverter} class.
     *
     */
    public ZWaveManufacturerProprietaryConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();

        ZWaveCommandClassTransactionPayload transaction = null;
        switch (channel.getUID().getId()) {
            case "blinds_lamella":
            case "blinds_shutter":
                byte[] shutterPayload = CommandClassManufacturerProprietaryFibaroFgrm222V1.getFgrm222Get();

                transaction = new ZWaveCommandClassTransactionPayloadBuilder(node.getNodeId(), shutterPayload)
                        .withExpectedResponseCommand(CommandClassManufacturerProprietaryFibaroFgrm222V1.FGRM222_REPORT)
                        .withPriority(TransactionPriority.Get).build();
                break;
        }

        if (transaction != null) {
            messages.add(transaction);
        }
        return messages;
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        boolean configInvertPercent = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_percent"));

        int value = -1;
        ZWaveValueEvent valueEvent = (ZWaveValueEvent) event;
        switch (channel.getUID().getId()) {
            case "blinds_lamella":
                value = Integer.parseInt(valueEvent.getValue("LAMELLA_POSITION"));
                break;
            case "blinds_shutter":
                value = Integer.parseInt(valueEvent.getValue("SHUTTER_POSITION"));
                break;
        }

        // If we read greater than 99%, then change it to 100%
        // This just appears better in OH otherwise you can't get 100%!
        if (value >= 99) {
            value = 100;
        } else if (value < 0) {
            value = 0;
        }

        if (configInvertPercent) {
            value = 100 - value;
        }
        return new PercentType(value);
    }

    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {
        boolean configInvertPercent = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_percent"));

        PercentType percentType = (PercentType) command;
        int value = percentType.intValue();
        if (configInvertPercent) {
            value = 100 - value;
        }

        // zwave has a max value of 99 for percentages.
        if (value >= 100) {
            value = 99;
        } else if (value < 0) {
            value = 0;
        }

        logger.trace("NODE {}: Converted command '{}' to value {} for channel = {}, endpoint = {}.", node.getNodeId(),
                command.toString(), value, channel.getUID(), channel.getEndpoint());

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();

        ZWaveCommandClassTransactionPayload transaction = null;
        switch (channel.getUID().getId()) {
            case "blinds_lamella":
                byte[] lamellaPayload = CommandClassManufacturerProprietaryFibaroFgrm222V1
                        .getFgrm222Set("LAMELLA_POSITION", 0, value);

                transaction = new ZWaveCommandClassTransactionPayloadBuilder(node.getNodeId(), lamellaPayload)
                        .withPriority(TransactionPriority.Set).build();
                break;
            case "blinds_shutter":
                byte[] shutterPayload = CommandClassManufacturerProprietaryFibaroFgrm222V1
                        .getFgrm222Set("SHUTTER_POSITION", value, 0);

                transaction = new ZWaveCommandClassTransactionPayloadBuilder(node.getNodeId(), shutterPayload)
                        .withPriority(TransactionPriority.Set).build();
                break;
        }

        if (transaction != null) {
            messages.add(transaction);
        }
        return messages;
    }
}
