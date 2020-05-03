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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the climate control schedule command class.
 *
 * @author Chris Jackson
 * @author Max Berger
 */
@XStreamAlias("COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE")
public class ZWaveClimateControlScheduleCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveClimateControlScheduleCommandClass.class);

    private static final int SCHEDULE_CHANGED_GET = 0x04;
    private static final int SCHEDULE_CHANGED_REPORT = 0x05;
    private static final int SCHEDULE_GET = 0x02;
    private static final int SCHEDULE_OVERRIDE_GET = 0x07;
    private static final int SCHEDULE_OVERRIDE_REPORT = 0x08;
    private static final int SCHEDULE_OVERRIDE_SET = 0x06;
    private static final int SCHEDULE_REPORT = 0x03;
    private static final int SCHEDULE_SET = 0x01;

    private static final byte SCHEDULE_CHANGE_TEMPORARILY_DISABLED = 0;

    /**
     * Creates a new instance of the ZWaveClimateControlCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveClimateControlScheduleCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE;
    }

    @ZWaveResponseHandler(id = SCHEDULE_CHANGED_GET, name = "SCHEDULE_CHANGED_GET")
    public void handleScheduleChangedGet(ZWaveCommandClassPayload payload, int endpoint) {
        logger.debug("NODE {}: Answering with noop SCHEDULE_CHANGED_REPORT", getNode().getNodeId());
        getController().enqueue(getScheduleChangedReportMessage(SCHEDULE_CHANGE_TEMPORARILY_DISABLED));
    }

    @ZWaveResponseHandler(id = SCHEDULE_OVERRIDE_REPORT, name = "SCHEDULE_OVERRIDE_REPORT")
    public void handleScheduleOverrideReport(ZWaveCommandClassPayload payload, int endpoint) {
        OverrideType overrideType = OverrideType.getOverrideTypeFor((byte) (payload.getPayloadByte(2) & 0x03));
        ScheduleState scheduleState = ScheduleState.getScheduleStateFor((byte) payload.getPayloadByte(3));
        logger.debug("NODE {}: Override type: {}, ScheduleState: {}", getNode().getNodeId(), overrideType,
                scheduleState);
    }

    // Visible for Testing
    public ZWaveCommandClassTransactionPayload getScheduleChangedReportMessage(byte scheduleChangeCounter) {
        logger.debug("NODE {}: Creating new message for command SCHEDULE_CHANGED_REPORT", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SCHEDULE_CHANGED_REPORT).withPayload(scheduleChangeCounter)
                        .withPriority(ZWaveTransaction.TransactionPriority.RealTime).build();

    }

    // Visible for Testing
    public enum OverrideType {
        NO_OVERRIDE((byte) 0b00),
        TEMPORARY_OVERRIDE((byte) 0b01),
        PERMANENT_OVERRIDE((byte) 0b10),
        RESERVED((byte) 0b11);

        private final byte value;
        private static final Map<Byte, OverrideType> codeToOverrideType;

        OverrideType(byte value) {
            this.value = value;
        }

        public static OverrideType getOverrideTypeFor(byte i) {
            return codeToOverrideType.get(i);
        }

        static {
            codeToOverrideType = new HashMap<>();
            for (OverrideType o : values()) {
                codeToOverrideType.put(o.value, o);
            }
        }
    }

    // Visible for Testing
    public enum ScheduleStateState {
        SETBACK((byte) 0),
        FROST_PROTECTION((byte) 0x79),
        ENERGY_SAVING((byte) 0x7A),
        RESERVED1((byte) 0x7B),
        RESERVED2((byte) 0x7C),
        RESERVED3((byte) 0x7D),
        RESERVED4((byte) 0x7E),
        UNUSED((byte) 0x7F);

        private final byte value;
        private static final Map<Byte, ScheduleStateState> codeToScheduledStateState;

        ScheduleStateState(byte value) {
            this.value = value;
        }

        public static ScheduleStateState getScheduledStateFor(byte value) {
            if (codeToScheduledStateState.containsKey(value)) {
                return codeToScheduledStateState.get(value);
            } else {
                return SETBACK;
            }
        }

        static {
            codeToScheduledStateState = new HashMap<>();
            for (ScheduleStateState s : values()) {
                codeToScheduledStateState.put(s.value, s);
            }
        }

    }

    // Visible for Testing
    public static class ScheduleState {
        public ScheduleStateState state;
        public int setBack;

        // Visible for Testing
        public ScheduleState(ScheduleStateState _state, int _setBack) {
            this.state = _state;
            this.setBack = _setBack;
        }

        public static ScheduleState getScheduleStateFor(byte messagePayloadByte) {
            ScheduleStateState stateState = ScheduleStateState.getScheduledStateFor(messagePayloadByte);
            if (stateState.equals(ScheduleStateState.SETBACK)) {
                return new ScheduleState(ScheduleStateState.SETBACK, messagePayloadByte);
            } else {
                return new ScheduleState(stateState, 0);
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("[");
            sb.append(state);
            if (state.equals(ScheduleStateState.SETBACK)) {
                sb.append(' ').append(setBack / 10.0);
            }
            sb.append(']');
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ScheduleState that = (ScheduleState) o;
            return setBack == that.setBack && state == that.state;
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, setBack);
        }
    }

}
