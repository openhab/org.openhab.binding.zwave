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
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
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
        implements ZWaveBasicCommands, ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

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
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveMultiLevelSwitchCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.SWITCH_MULTILEVEL;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received SWITCH_MULTILEVEL Command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case SWITCH_MULTILEVEL_SET:
                logger.debug("NODE {}: Switch Multi Level SET", getNode().getNodeId());
            case SWITCH_MULTILEVEL_REPORT:
                int value = serialMessage.getMessagePayloadByte(offset + 1);
                logger.debug("NODE {}: Switch Multi Level report, value = {}", getNode().getNodeId(), value);
                ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                        getCommandClass(), value);

                getController().notifyEventListeners(zEvent);

                dynamicDone = true;
                break;
            case SWITCH_MULTILEVEL_SUPPORTED_REPORT:
                int primary = serialMessage.getMessagePayloadByte(offset + 1) & 0x1f;
                int secondary = serialMessage.getMessagePayloadByte(offset + 1) & 0x1f;

                switchTypePrimary = SwitchType.getSwitchType(primary);
                switchTypeSecondary = SwitchType.getSwitchType(secondary);

                initialiseDone = true;
                break;
            default:
                logger.warn(String.format("Unsupported Command %d for command class %s (0x%02X).", command,
                        getCommandClass().getLabel(), getCommandClass().getKey()));
        }
    }

    /**
     * Gets a SerialMessage with the SWITCH_MULTILEVEL_GET command
     *
     * @return the serial message
     */
    @Override
    public ZWaveTransaction getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_GET", this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), SWITCH_MULTILEVEL_GET).withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), SWITCH_MULTILEVEL_REPORT)
                .withPriority(TransactionPriority.Get).build();
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
    @Override
    public ZWaveTransaction setValueMessage(int level) {
        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_SET", this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), SWITCH_MULTILEVEL_SET).withNodeId(getNode().getNodeId())
                .withPayload(level).build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.Set).build();
    }

    /**
     * Gets a SerialMessage with the SWITCH_MULTILEVEL_STOP_LEVEL_CHANGE command
     *
     * @return the serial message
     */
    public ZWaveTransaction stopLevelChangeMessage() {
        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_STOP_LEVEL_CHANGE",
                this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), SWITCH_MULTILEVEL_STOP_LEVEL_CHANGE)
                .withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.Set).build();
    }

    /**
     * Gets a SerialMessage with the SWITCH_MULTILEVEL_START_LEVEL_CHANGE command
     *
     * @param increase true to increase the level, false to decrease
     * @param duration sets the dimming duration
     * @return the serial message
     */
    public ZWaveTransaction startLevelChangeMessage(boolean increase, int duration) {
        // TODO: This is only V2 implementation! V3 has some extra options.
        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_START_LEVEL_CHANGE",
                getNode().getNodeId());
        byte[] newPayload = { 0, 0, 0 };
        if (increase) {
            newPayload[0] = 32;
        } else {
            newPayload[0] = 96;
        }
        newPayload[1] = 0; // Start level - ignored (for now!)
        newPayload[2] = (byte) duration;

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), SWITCH_MULTILEVEL_START_LEVEL_CHANGE)
                .withNodeId(getNode().getNodeId()).withPayload(newPayload).build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.Set).build();
    }

    /**
     * Gets a SerialMessage with the SWITCH_MULTILEVEL_SET command
     *
     * @param the level to set. 0 is mapped to off, > 0 is mapped to on.
     * @return the serial message
     */
    public ZWaveTransaction getSupportedMessage() {
        logger.debug("NODE {}: Creating new message for command SWITCH_MULTILEVEL_SUPPORTED_GET",
                getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), SWITCH_MULTILEVEL_SUPPORTED_GET).withNodeId(getNode().getNodeId())
                .build();
        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), SWITCH_MULTILEVEL_SUPPORTED_REPORT)
                .withPriority(TransactionPriority.Get).build();

        // SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
        // SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);
        // byte[] newPayload = { (byte) getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
        // (byte) SWITCH_MULTILEVEL_SUPPORTED_GET };
        // result.setMessagePayload(newPayload);
        // return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ZWaveTransaction> initialize(boolean refresh) {
        ArrayList<ZWaveTransaction> result = new ArrayList<ZWaveTransaction>();

        if ((refresh == true || initialiseDone == false) && getVersion() >= 3) {
            result.add(getSupportedMessage());
        } else {
            initialiseDone = true;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ZWaveTransaction> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveTransaction> result = new ArrayList<ZWaveTransaction>();
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
}
