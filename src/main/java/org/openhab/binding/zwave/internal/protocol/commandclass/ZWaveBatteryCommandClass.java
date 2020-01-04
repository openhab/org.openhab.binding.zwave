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
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Battery command class. Devices that support this command class can report their battery level and give
 * low battery warnings.
 * The commands include the possibility to get a given battery level and report a battery level.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("COMMAND_CLASS_BATTERY")
public class ZWaveBatteryCommandClass extends ZWaveCommandClass implements ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveBatteryCommandClass.class);

    private static final int BATTERY_GET = 0x02;
    private static final int BATTERY_REPORT = 0x03;

    private Integer batteryLevel = null;
    private Boolean batteryLow = null;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveBatteryCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveBatteryCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_BATTERY;
    }

    @ZWaveResponseHandler(id = BATTERY_REPORT, name = "BATTERY_REPORT")
    public void handleBatteryReport(ZWaveCommandClassPayload payload, int endpoint) {
        batteryLevel = payload.getPayloadByte(2);
        logger.debug("NODE {}: Battery report value = {}", getNode().getNodeId(), batteryLevel);

        // A Battery level of 255 means battery low.
        // Set battery level to 0
        if (batteryLevel == 255) {
            batteryLevel = 0;
            batteryLow = true;
            logger.debug("NODE {}: BATTERY LOW!", getNode().getNodeId());
        } else {
            batteryLow = false;
        }

        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), batteryLevel);
        getController().notifyEventListeners(zEvent);

        dynamicDone = true;
    }

    /**
     * Gets a SerialMessage with the BATTERY_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }
        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), BATTERY_GET)
                .withExpectedResponseCommand(BATTERY_REPORT).withPriority(TransactionPriority.Get).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
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

    /**
     * Returns the current battery level. If the battery level is unknown, returns null
     *
     * @return battery level
     */
    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    /**
     * Returns the current battery warning state.
     *
     * @return true if device is saying battery is low
     */
    public Boolean getBatteryLow() {
        if (batteryLow == null) {
            return false;
        }
        return batteryLow;
    }
}
