/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.openhab.binding.zwave.internal.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * Handles the climate control schedule command class.
 *
 * @author Chris Jackson
 * @author Max Berger
 */
@XStreamAlias("climateControlScheduleCommandClass")
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

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.CLIMATE_CONTROL_SCHEDULE;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received CLIMATE_CONTROL_SCHEDULE command V{}", this.getNode().getNodeId(),
                this.getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case SCHEDULE_CHANGED_GET:
                logger.debug("Answering with noop SCHEDULE_CHANGED_REPORT");
                getController().enqueue(getScheduleChangedReportMessage(SCHEDULE_CHANGE_TEMPORARILY_DISABLED));
                break;
            case SCHEDULE_OVERRIDE_REPORT:
                OverrideType overrideType = getOverrideTypeFor(serialMessage.getMessagePayloadByte(offset + 1) & 0x03);
                ScheduleState scheduleState = getScheduleStateFor((byte) serialMessage.getMessagePayloadByte(offset + 2));
                logger.info("NODE {} reported: Override type: {}, ScheduleState: {}", this.getNode().getNodeId(), overrideType, scheduleState);
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        this.getNode().getNodeId(), command, this.getCommandClass().getLabel(),
                        this.getCommandClass().getKey()));
        }
    }

    public ScheduleState getScheduleStateFor(byte messagePayloadByte) {
        if (messagePayloadByte == 0x79) return new ScheduleState(ScheduleStateState.FROST_PROTECTION,0);
        if (messagePayloadByte == 0x7A) return new ScheduleState(ScheduleStateState.ENERGY_SAVING, 0);
        if (messagePayloadByte >= 0x7B && messagePayloadByte <= 0x7E) return new ScheduleState(ScheduleStateState.RESERVED, 0);
        if (messagePayloadByte == 0x7F) return new ScheduleState(ScheduleStateState.UNUSED, 0);
        return new ScheduleState(ScheduleStateState.SETBACK, messagePayloadByte);
    }

    public OverrideType getOverrideTypeFor(int i) {
        switch (i) {
            case 0b00: return OverrideType.NO_OVERRIDE;
            case 0b01: return OverrideType.TEMPORARY_OVERRIDE;
            case 0b10: return OverrideType.PERMANENT_OVERRIDE;
            case 0b11: return OverrideType.RESERVED;
            default: return OverrideType.RESERVED;
        }
    }

    public SerialMessage getScheduleChangedReportMessage(byte scheduleChangeCounter) {
        logger.debug("NODE {}: Creating new message for command SCHEDULE_CHANGED_REPORT", getNode().getNodeId());

        SerialMessage result = new SerialMessage(getNode().getNodeId(), SerialMessage.SerialMessageClass.SendData,
                SerialMessage.SerialMessageType.Request, SerialMessage.SerialMessageClass.SendData, SerialMessage.SerialMessagePriority.RealTime);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(getNode().getNodeId());
        outputData.write(4);
        outputData.write(getCommandClass().getKey());
        outputData.write(SCHEDULE_CHANGED_REPORT);
        outputData.write(scheduleChangeCounter);
        result.setMessagePayload(outputData.toByteArray());
        return result;
    }

    public enum OverrideType { NO_OVERRIDE, TEMPORARY_OVERRIDE, PERMANENT_OVERRIDE, RESERVED }

    public enum ScheduleStateState { SETBACK, FROST_PROTECTION, ENERGY_SAVING, RESERVED, UNUSED }

    public static class ScheduleState {
        public ScheduleStateState state;
        public int setBack;

        public ScheduleState(ScheduleStateState _state, int _setBack) { this.state = _state; this.setBack = _setBack; }

        @Override public String toString() { return "[" + state + " " + setBack/10.0 + "]"; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScheduleState that = (ScheduleState) o;
            return setBack == that.setBack &&
                    state == that.state;
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, setBack);
        }
    }
}
