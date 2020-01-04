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
 * Handles the Switch All command class. Sends all on or all off commands to device
 *
 * @author Chris Jackson
 * @author Pedro Paixao
 */
@XStreamAlias(value = "COMMAND_CLASS_SWITCH_ALL")
public class ZWaveSwitchAllCommandClass extends ZWaveCommandClass implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveSwitchAllCommandClass.class);

    private static final int SWITCH_ALL_SET = 1;
    private static final int SWITCH_ALL_GET = 2;
    private static final int SWITCH_ALL_REPORT = 3;
    private static final int SWITCH_ALL_ON = 4;
    private static final int SWITCH_ALL_OFF = 5;

    public enum SwitchAllMode {
        SWITCH_ALL_EXCLUDED(0x00, "not included in either All On or All Off groups"),
        SWITCH_ALL_INCLUDE_ON_ONLY(0x01, "device included in All On group"),
        SWITCH_ALL_INCLUDE_OFF_ONLY(0x02, "device included in All Off group"),
        SWITCH_ALL_INCLUDE_ON_OFF(0xFF, "device included in All On and All Off group");

        int mode;
        String description;

        SwitchAllMode(int mode) {
            this.mode = mode;
            this.description = "";
        }

        SwitchAllMode(int mode, String description) {
            this.mode = mode;
            this.description = description;
        }

        public int getMode() {
            return mode;
        }

        public String getDescription() {
            return description;
        }

        public static SwitchAllMode fromInteger(int m) {
            switch (m) {
                case 0x00:
                    return SWITCH_ALL_EXCLUDED;
                case 0x01:
                    return SWITCH_ALL_INCLUDE_ON_ONLY;
                case 0x02:
                    return SWITCH_ALL_INCLUDE_OFF_ONLY;
                case 0xFF:
                    return SWITCH_ALL_INCLUDE_ON_OFF;
            }

            return null;
        }
    }

    @XStreamOmitField
    private boolean initialiseDone = false;

    private boolean isGetSupported = true;
    private SwitchAllMode mode;

    /**
     * Creates a new instance of the ZWaveSwitchAllCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveSwitchAllCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public ZWaveCommandClass.CommandClass getCommandClass() {
        return ZWaveCommandClass.CommandClass.COMMAND_CLASS_SWITCH_ALL;
    }

    @ZWaveResponseHandler(id = SWITCH_ALL_REPORT, name = "SWITCH_ALL_REPORT")
    public void handleSwitchAllReport(ZWaveCommandClassPayload payload, int endpoint) {
        int m = payload.getPayloadByte(2);
        mode = SwitchAllMode.fromInteger(m);

        if (mode != null) {
            logger.debug("NODE {}: Switch All report, {}.", getNode().getNodeId(), mode.getDescription());
        } else {
            logger.debug("NODE {}: Switch All unsupported mode.", getNode().getNodeId());
            return;
        }

        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), new Integer(m));
        getController().notifyEventListeners(zEvent);

        initialiseDone = true;
    }

    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (!isGetSupported) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command SWITCH_ALL_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), SWITCH_ALL_GET)
                .withPriority(TransactionPriority.Config).withExpectedResponseCommand(SWITCH_ALL_REPORT).build();
    }

    /**
     * Create a new SwitchAll set message
     *
     * @param newMode
     *            as (0x00 - Exclude, 0x01 Only All On, 0x02 Only All Off, 0xFF
     *            Both All on and All off)
     * @return SerialMessage
     */
    public ZWaveCommandClassTransactionPayload setValueMessage(int newMode) {
        mode = SwitchAllMode.fromInteger(newMode);

        if (mode != null) {
            logger.debug("NODE {}: Switch All report, {}.", getNode().getNodeId(), mode.getDescription());
        } else {
            logger.debug("NODE {}: Switch All unsupported mode.", getNode().getNodeId());
            return null;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), SWITCH_ALL_SET)
                .withPayload(newMode).withPriority(TransactionPriority.Config).build();
    }

    /**
     * Create the All On message
     *
     * @return
     */
    public ZWaveCommandClassTransactionPayload allOnMessage() {
        logger.debug("NODE {}: Switch All - Creating All On message.", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), SWITCH_ALL_ON)
                .withPriority(TransactionPriority.Set).build();
    }

    /**
     * Create the All Off message
     *
     * @return
     */
    public ZWaveCommandClassTransactionPayload allOffMessage() {
        logger.debug("NODE {}: Switch All - Creating All Off message.", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), SWITCH_ALL_OFF)
                .withPriority(TransactionPriority.Set).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * get the Switch All mode
     *
     * @return the mode
     */
    public SwitchAllMode getMode() {
        return mode;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        // If we're already initialized, then don't do it again unless we're
        // refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(getValueMessage());
        }

        return result;
    }
}
