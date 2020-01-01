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
import java.util.concurrent.ConcurrentHashMap;

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
 * Handles Barrier Operator Command Class for Items like Garage Door Opener etc...
 *
 * @author Chris Jackson
 * @author sankala
 *
 */
@XStreamAlias("COMMAND_CLASS_BARRIER_OPERATOR")
public class ZWaveBarrierOperatorCommandClass extends ZWaveCommandClass implements ZWaveCommandClassDynamicState {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveBarrierOperatorCommandClass.class);

    public static final int BARRIER_OPERATOR_SET = 1;
    public static final int BARRIER_OPERATOR_GET = 2;
    public static final int BARRIER_OPERATOR_REPORT = 3;
    public static final int BARRIER_OPERATOR_SIGNAL_SUPPORTED_GET = 4;
    public static final int BARRIER_OPERATOR_SIGNAL_SUPPORTED_REPORT = 5;
    public static final int BARRIER_OPERATOR_SIGNAL_SET = 6;
    public static final int BARRIER_OPERATOR_SIGNAL_GET = 7;
    public static final int BARRIER_OPERATOR_SIGNAL_REPORT = 8;

    @XStreamOmitField
    private boolean dynamicDone = false;

    public ZWaveBarrierOperatorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_BARRIER_OPERATOR;
    }

    @ZWaveResponseHandler(id = BARRIER_OPERATOR_REPORT, name = "BARRIER_OPERATOR_REPORT")
    public void handleBarrierOperatorReport(ZWaveCommandClassPayload payload, int endpoint) {
        logger.trace("Process Barrier Operator Report");
        int value = payload.getPayloadByte(2);
        logger.debug("NODE {}: Barrier Operator report, value = {}", getNode().getNodeId(), value);

        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), BarrierOperatorStateType.getBarrierOperatorStateType(value));

        getController().notifyEventListeners(zEvent);
    }

    public ZWaveCommandClassTransactionPayload setValueMessage(int value) {
        logger.debug("NODE {}: Creating new message for application command BARRIER_OPERATOR_SET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                BARRIER_OPERATOR_SET).withPayload(value > 0 ? 0xFF : 0x00).withPriority(TransactionPriority.Set)
                        .build();
    }

    public ZWaveCommandClassTransactionPayload getValueMessage() {
        logger.debug("NODE {}: Creating new message for command BARRIER_OPERATOR_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                BARRIER_OPERATOR_GET).withExpectedResponseCommand(BARRIER_OPERATOR_REPORT)
                        .withPriority(TransactionPriority.Get).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        if (refresh == true || dynamicDone == false) {
            return null;
        }

        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        result.add(getValueMessage());
        return result;
    }

    @XStreamAlias("barrierOperatorState")
    public static enum BarrierOperatorStateType {
        STATE_CLOSED(0x00, "Closed"),
        STATE_CLOSING(0xFC, "Closing"),
        STATE_OPENED(0xFF, "Open"),
        STATE_OPENING(0xFE, "Opening"),
        STATE_STOPPED(0xFD, "Stopped");

        private static Map<Integer, BarrierOperatorStateType> codeToBarrierOperatorStateTypeMapping;

        private int key;
        private String label;

        private static void initMapping() {
            codeToBarrierOperatorStateTypeMapping = new ConcurrentHashMap<Integer, ZWaveBarrierOperatorCommandClass.BarrierOperatorStateType>();
            for (BarrierOperatorStateType s : values()) {
                codeToBarrierOperatorStateTypeMapping.put(s.key, s);
            }
        }

        public static BarrierOperatorStateType getBarrierOperatorStateType(int i) {
            if (codeToBarrierOperatorStateTypeMapping == null) {
                initMapping();
            }
            BarrierOperatorStateType barrierOperatorStateType = codeToBarrierOperatorStateTypeMapping.get(i);
            // If the state is stopped, then the value indicates what is the percentage of opening. So it will not
            // directly yield us the "StateStopped" . We have to force it to StateStopped.
            if (barrierOperatorStateType == null && (i < 99 && i > 0)) {
                barrierOperatorStateType = STATE_STOPPED;
            }

            return barrierOperatorStateType;
        }

        private BarrierOperatorStateType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        public static Map<Integer, BarrierOperatorStateType> getCodeToBarrierOperatorStateTypeMapping() {
            return codeToBarrierOperatorStateTypeMapping;
        }

        public int getKey() {
            return key;
        }

        public int getValue() {
            return key;
        }

        public String getLabel() {
            return label;
        }
    }
}
