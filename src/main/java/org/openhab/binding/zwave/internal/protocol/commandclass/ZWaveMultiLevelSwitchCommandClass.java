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
import java.util.HashMap;
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
 * Handles the Multi Level Switch command class. Multi level switches accept on / off, on or off and report their status
 * as on (0xFF) or off (0x00).
 * The commands include the possibility to set a given level, get a given level and report a level.
 * Z-Wave dimmers have a range from 0 (off) to 99 (on). 255 (0xFF) means restore to previous level. We translate 99 to
 * 100%, so it's impossible to set the level to 99%.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("multiLevelSwitchCommandClass")
public class ZWaveMultiLevelSwitchCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiLevelSwitchCommandClass.class);
    private static final int MAX_SUPPORTED_VERSION = 3;

    private static final int SWITCH_MULTILEVEL_SET = 0x01;
    private static final int SWITCH_MULTILEVEL_GET = 0x02;
    private static final int SWITCH_MULTILEVEL_REPORT = 0x03;
    private static final int SWITCH_MULTILEVEL_START_LEVEL_CHANGE = 0x04;
    private static final int SWITCH_MULTILEVEL_STOP_LEVEL_CHANGE = 0x05;
    private static final int SWITCH_MULTILEVEL_SUPPORTED_GET = 0x06;
    private static final int SWITCH_MULTILEVEL_SUPPORTED_REPORT = 0x07;

    private static final int START_LEVEL_INCREASE = 0;
    private static final int START_LEVEL_DECREASE = 0x40;
    private static final int START_LEVEL_IGNORE_LEVEL = 0x20;

    private SwitchType switchTypePrimary = null;
    private SwitchType switchTypeSecondary = null;

    @XStreamOmitField
    private boolean initialiseDone = false;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveMultiLevelSwitchCommandClass class.
     *
     * @param node       the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint   the endpoint this Command class belongs to
     */
    public ZWaveMultiLevelSwitchCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL;
    }

    @ZWaveResponseHandler(id = SWITCH_MULTILEVEL_SET, name = "SWITCH_MULTILEVEL_SET")
    public void handleSwitchMultilevelSet(ZWaveCommandClassPayload payload, int endpoint) {
        int value = payload.getPayloadByte(2);
        logger.debug("NODE {}: Switch Multi Level set, value = {}", getNode().getNodeId(), value);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), value);

        getController().notifyEventListeners(zEvent);
    }

    @ZWaveResponseHandler(id = SWITCH_MULTILEVEL_REPORT, name = "SWITCH_MULTILEVEL_REPORT")
    public void handleSwitchMultilevelReport(ZWaveCommandClassPayload payload, int endpoint) {
        int value = payload.getPayloadByte(2);
        if (getVersion() == 4) {
            // Version 4 includes a target rather than the current value.
            // This fits better with the OH concepts
            value = payload.getPayloadByte(3);
        }
        logger.debug("NODE {}: Switch Multi Level report, value = {}", getNode().getNodeId(), value);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), value);

        getController().notifyEventListeners(zEvent);

        dynamicDone = true;
    }

    @ZWaveResponseHandler(id = SWITCH_MULTILEVEL_START_LEVEL_CHANGE, name = "SWITCH_MULTILEVEL_START_LEVEL_CHANGE")
    public void handleSwitchMultilevelStartLevelChanel(ZWaveCommandClassPayload payload, int endpoint) {
        StartStopDirection direction = ((payload.getPayloadByte(2) & START_LEVEL_DECREASE) != 0)
                ? StartStopDirection.DECREASE
                : StartStopDirection.INCREASE;

        logger.debug("NODE {}: Switch Multi Level start level change, direction = {}", getNode().getNodeId(),
                direction);
        ZWaveCommandClassValueEvent zEvent = new ZWaveStartStopEvent(getNode().getNodeId(), endpoint, getCommandClass(),
                direction);

        getController().notifyEventListeners(zEvent);

        dynamicDone = true;
    }

    @ZWaveResponseHandler(id = SWITCH_MULTILEVEL_STOP_LEVEL_CHANGE, name = "SWITCH_MULTILEVEL_STOP_LEVEL_CHANGE")
    public void handleSwitchMultilevelStopLevelChanel(ZWaveCommandClassPayload payload, int endpoint) {
        logger.debug("NODE {}: Switch Multi Level stop level change", getNode().getNodeId());
        ZWaveCommandClassValueEvent zEvent = new ZWaveStartStopEvent(getNode().getNodeId(), endpoint, getCommandClass(),
                StartStopDirection.STOP);

        getController().notifyEventListeners(zEvent);

        dynamicDone = true;
    }

    @ZWaveResponseHandler(id = SWITCH_MULTILEVEL_SUPPORTED_REPORT, name = "SWITCH_MULTILEVEL_SUPPORTED_REPORT")
    public void handleSwitchMultilevelSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        int primary = payload.getPayloadByte(2) & 0x1f;
        int secondary = payload.getPayloadByte(3) & 0x1f;

        switchTypePrimary = SwitchType.getSwitchType(primary);
        switchTypeSecondary = SwitchType.getSwitchType(secondary);

        initialiseDone = true;
    }

    /**
     * Gets a SerialMessage with the SWITCH_MULTILEVEL_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SWITCH_MULTILEVEL_GET).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(SWITCH_MULTILEVEL_REPORT).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the SWITCH_MULTILEVEL_SET command
     *
     * @param the level to set. 0 is mapped to off, > 0 is mapped to on.
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload setValueMessage(int level) {
        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_SET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SWITCH_MULTILEVEL_SET).withPayload(level).withPriority(TransactionPriority.Set).build();
    }

    /**
     * Gets a SerialMessage with the SWITCH_MULTILEVEL_STOP_LEVEL_CHANGE command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload stopLevelChangeMessage() {
        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_STOP_LEVEL_CHANGE",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SWITCH_MULTILEVEL_STOP_LEVEL_CHANGE).withPriority(TransactionPriority.Set).build();
    }

    /**
     * Gets a SerialMessage with the SWITCH_MULTILEVEL_START_LEVEL_CHANGE command
     *
     * @param increase true to increase the level, false to decrease
     * @param duration sets the dimming duration
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload startLevelChangeMessage(boolean increase, int duration) {
        // TODO: This is only V2 implementation! V3 has some extra options.
        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_START_LEVEL_CHANGE",
                getNode().getNodeId());
        byte[] newPayload = { 0, 0, 0 };
        if (increase) {
            newPayload[0] = START_LEVEL_INCREASE;
        } else {
            newPayload[0] = START_LEVEL_DECREASE;
        }
        newPayload[0] |= START_LEVEL_IGNORE_LEVEL;

        newPayload[1] = 0; // Start level - ignored (for now!)
        newPayload[2] = (byte) duration;

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SWITCH_MULTILEVEL_START_LEVEL_CHANGE).withPayload(newPayload).withPriority(TransactionPriority.Set)
                        .build();
    }

    /**
     * Gets a SerialMessage with the SWITCH_MULTILEVEL_SET command
     *
     * @param the level to set. 0 is mapped to off, > 0 is mapped to on.
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getSupportedMessage() {
        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_SUPPORTED_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SWITCH_MULTILEVEL_SUPPORTED_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(SWITCH_MULTILEVEL_SUPPORTED_REPORT).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        if ((refresh == true || initialiseDone == false) && getVersion() >= 3) {
            result.add(getSupportedMessage());
        } else {
            initialiseDone = true;
        }

        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (refresh == true || dynamicDone == false) {
            result.add(getValueMessage());
        }
        return result;
    }

    /**
     * Return the primary switch type
     *
     * @return
     */
    public SwitchType getPrimarySwitchType() {
        return switchTypePrimary;
    }

    /**
     * Return the secondary switch type
     *
     * @return
     */
    public SwitchType getSecondarySwitchType() {
        return switchTypeSecondary;
    }

    /**
     * Enum to define the different switch types defined by ZWave
     *
     */
    @XStreamAlias("multilevelSwitchType")
    public enum SwitchType {
        UNDEFINED(0),
        OFF_ON(1),
        DOWN_UP(2),
        CLOSE_OPEN(3),
        CCW_CW(4),
        LEFT_RIGHT(5),
        REVERSE_FORWARD(6),
        PULL_PUSH(7);

        private final int value;
        private static Map<Integer, SwitchType> codeToTypeMapping;

        private static void initMapping() {
            codeToTypeMapping = new HashMap<Integer, SwitchType>();
            for (SwitchType s : values()) {
                codeToTypeMapping.put(s.value, s);
            }
        }

        public static SwitchType getSwitchType(int i) {
            if (codeToTypeMapping == null) {
                initMapping();
            }

            return codeToTypeMapping.get(i);
        }

        private SwitchType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum StartStopDirection {
        INCREASE,
        DECREASE,
        STOP
    }

    /**
     * Z-Wave Alarm Event class. Indicates that an alarm value changed.
     */
    public static class ZWaveStartStopEvent extends ZWaveCommandClassValueEvent {
        public StartStopDirection direction;

        public ZWaveStartStopEvent(int nodeId, int endpoint, CommandClass commandClass, StartStopDirection direction) {
            super(nodeId, endpoint, commandClass, direction);

            this.direction = direction;
        }

    }

}
