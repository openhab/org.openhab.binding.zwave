/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        ZWaveValueEvent valueEvent = (ZWaveValueEvent) event;
        State state = null;

        switch (channel.getUID().getId()) {
            case "blinds_lamella":
                state = new PercentType(Integer.parseInt(valueEvent.getValue("LAMELLA_POSITION")));
                break;
            case "blinds_shutter":
                state = new PercentType(Integer.parseInt(valueEvent.getValue("SHUTTER_POSITION")));
                break;
        }

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ZWaveCommandClassTransactionPayload> receiveCommand(ZWaveThingChannel channel, ZWaveNode node,
            Command command) {

        List<ZWaveCommandClassTransactionPayload> messages = new ArrayList<ZWaveCommandClassTransactionPayload>();

        ZWaveCommandClassTransactionPayload transaction = null;
        switch (channel.getUID().getId()) {
            case "blinds_lamella":
                byte[] lamellaPayload = CommandClassManufacturerProprietaryFibaroFgrm222V1
                        .getFgrm222Set("LAMELLA_POSITION", 0, ((PercentType) command).intValue());

                transaction = new ZWaveCommandClassTransactionPayloadBuilder(node.getNodeId(), lamellaPayload)
                        .withPriority(TransactionPriority.Set).build();
                break;
            case "blinds_shutter":
                byte[] shutterPayload = CommandClassManufacturerProprietaryFibaroFgrm222V1
                        .getFgrm222Set("SHUTTER_POSITION", ((PercentType) command).intValue(), 0);

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
