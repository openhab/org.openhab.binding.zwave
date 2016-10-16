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

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Indicator command class.
 * The indicator command class operates the indicator on the physical device if available.
 * This can be used to identify a device or use the indicator for special purposes.
 * Example is the Evolve LCD panel that uses the Indicator class to toggle the labels
 * displayed on the LCD. The Indicator class is also used to sync multiple panels' labels
 *
 * @author Chris Jackson
 * @author Pedro Paixao
 */
@XStreamAlias("COMMAND_CLASS_INDICATOR")
public class ZWaveIndicatorCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveSetCommands, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveIndicatorCommandClass.class);

    private static final int INDICATOR_SET = 0x01;
    private static final int INDICATOR_GET = 0x02;
    private static final int INDICATOR_REPORT = 0x03;

    private int indicator;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveIndicatorCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveIndicatorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        indicator = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_INDICATOR;
    }

    /**
     * Processes a INDICATOR_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = INDICATOR_REPORT, name = "INDICATOR_REPORT")
    public void handleIndicatorReport(ZWaveCommandClassPayload payload, int endpoint) {
        int newIndicator = payload.getPayloadByte(2);

        logger.debug("NODE {}: Indicator report, value={}", this.getNode().getNodeId(), newIndicator);

        ZWaveIndicatorCommandClassChangeEvent zEvent = new ZWaveIndicatorCommandClassChangeEvent(getNode().getNodeId(),
                endpoint, getCommandClass(), newIndicator, indicator);

        indicator = newIndicator;
        getController().notifyEventListeners(zEvent);
    }

    /**
     * Gets a SerialMessage with the INDICATOR GET command
     *
     * @return the serial message
     */
    @Override
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command INDICATOR_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), INDICATOR_GET)
                .withExpectedResponseCommand(INDICATOR_REPORT).withPriority(TransactionPriority.Get).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the INDICATOR SET command
     *
     * @param the level to set.
     * @return the serial message
     */
    @Override
    public ZWaveCommandClassTransactionPayload setValueMessage(int newIndicator) {
        logger.debug("NODE {}: Creating new message for application command INDICATOR_SET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), INDICATOR_SET)
                .withPayload(newIndicator).withPriority(TransactionPriority.Set).build();
    }

    /**
     * Get current indicator value
     *
     * @return indicator
     */
    public int getValue() {
        return indicator;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        if (refresh == true) {
            dynamicDone = false;
        }

        if (dynamicDone == true) {
            return null;
        }

        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        result.add(getValueMessage());
        return result;
    }

    public class ZWaveIndicatorCommandClassChangeEvent extends ZWaveCommandClassValueEvent {
        private int currentIndicator;

        /**
         * Constructor. Creates a new instance of the ZWaveCommandClassValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param commandClass the command class that fired the ZWaveCommandClassValueEvent;
         * @param the new indicator value for the event.
         * @param the value currently held by indicator for the event.
         */
        public ZWaveIndicatorCommandClassChangeEvent(int nodeId, int endpoint, CommandClass commandClass, int newValue,
                int oldValue) {
            super(nodeId, endpoint, commandClass, newValue);
            this.currentIndicator = oldValue;
        }

        /**
         * Gets the value for the event.
         *
         * @return the value.
         */
        public int getOldValue() {
            return currentIndicator;
        }

        public ArrayList<Integer> changes() {
            ArrayList<Integer> changesList = new ArrayList<Integer>();

            int newIndicator = ((Integer) this.getValue()).intValue();

            int changes = currentIndicator ^ newIndicator;

            for (int i = 0; i < 8; i++) {
                if ((changes & 0x01) == 0x01) {
                    changesList.add(i);
                }
                changes = changes >> 1;
            }
            return changesList;
        }

        public boolean isBitOn(int n) {
            byte newIndicator = ((Integer) this.getValue()).byteValue();
            if (((newIndicator >> (n - 1)) & 0x01) == 1) {
                return true;
            }
            return false;
        }
    }
}
