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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Thermostat FanMode command class.
 *
 * @author Chris Jackson
 * @author Dan Cunningham
 */
@XStreamAlias("COMMAND_CLASS_THERMOSTAT_FAN_MODE")
public class ZWaveThermostatFanModeCommandClass extends ZWaveCommandClass
        implements ZWaveBasicCommands, ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveThermostatFanModeCommandClass.class);

    private static final byte THERMOSTAT_FAN_MODE_SET = 1;
    private static final byte THERMOSTAT_FAN_MODE_GET = 2;
    private static final byte THERMOSTAT_FAN_MODE_REPORT = 3;
    private static final byte THERMOSTAT_FAN_MODE_SUPPORTED_GET = 4;
    private static final byte THERMOSTAT_FAN_MODE_SUPPORTED_REPORT = 5;

    private final Set<FanModeType> fanModeTypes = new HashSet<FanModeType>();

    @XStreamOmitField
    private boolean initialiseDone = false;
    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveThermostatFanModeCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveThermostatFanModeCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_MODE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxVersion() {
        return 2;
    }

    @ZWaveResponseHandler(id = THERMOSTAT_FAN_MODE_SUPPORTED_REPORT, name = "THERMOSTAT_FAN_MODE_SUPPORTED_REPORT")
    public void handleThermostatFanModeSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        int payloadLength = payload.getPayloadLength();

        for (int i = 2; i < payloadLength; ++i) {
            int bitMask = payload.getPayloadByte(i);
            for (int bit = 0; bit < 8; ++bit) {
                if ((bitMask & (1 << bit)) == 0) {
                    continue;
                }

                int index = ((i - 2) * 8) + bit;
                if (index >= FanModeType.values().length) {
                    continue;
                }

                logger.debug("NODE {}: looking up Fan Mode type {}", this.getNode().getNodeId(), index);
                // (n)th bit is set. n is the index for the fanMode type enumeration.
                FanModeType fanModeTypeToAdd = FanModeType.getFanModeType(index);
                if (fanModeTypeToAdd != null) {
                    fanModeTypes.add(fanModeTypeToAdd);
                    logger.debug("NODE {}: Added Fan Mode type {} ({})", this.getNode().getNodeId(),
                            fanModeTypeToAdd.getLabel(), index);
                } else {
                    logger.warn("NODE {}: Uknown fan mode type {}", this.getNode().getNodeId(), index);
                }
            }
        }

        initialiseDone = true;
    }

    @ZWaveResponseHandler(id = THERMOSTAT_FAN_MODE_REPORT, name = "THERMOSTAT_FAN_MODE_REPORT")
    public void handleThermostatFanModeReport(ZWaveCommandClassPayload payload, int endpoint) {
        int value = payload.getPayloadByte(2);

        logger.debug("NODE {}: Thermostat Fan Mode report value = {}", getNode().getNodeId(), value);

        FanModeType fanModeType = FanModeType.getFanModeType(value);

        if (fanModeType == null) {
            logger.error("NODE {}: Unknown Fan Mode Type = {}, ignoring report.", getNode().getNodeId(), value);
            return;
        }

        // fanMode type seems to be supported, add it to the list.
        if (!fanModeTypes.contains(fanModeType)) {
            fanModeTypes.add(fanModeType);
        }

        dynamicDone = true;
        logger.debug("NODE {}: Thermostat Fan Mode Report value = {}", getNode().getNodeId(), fanModeType.getLabel());
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), value);
        getController().notifyEventListeners(zEvent);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ZWaveTransaction> initialize(boolean refresh) {
        ArrayList<ZWaveTransaction> result = new ArrayList<ZWaveTransaction>();
        if (refresh == true || initialiseDone == false) {
            result.add(this.getSupportedMessage());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ZWaveTransaction> getDynamicValues(boolean refresh) {
        // TODO (or question for Dan from Chris) - shouldn't this iterate through all fan types?
        ArrayList<ZWaveTransaction> result = new ArrayList<ZWaveTransaction>();
        if (refresh == true || dynamicDone == false) {
            result.add(getValueMessage());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZWaveTransaction getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_FAN_MODE_GET",
                this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), THERMOSTAT_FAN_MODE_GET).withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), THERMOSTAT_FAN_MODE_REPORT)
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
     * Gets a SerialMessage with the THERMOSTAT_FAN_MODE_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public ZWaveTransaction getSupportedMessage() {
        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_FAN_MODE_SUPPORTED_GET",
                this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), THERMOSTAT_FAN_MODE_SUPPORTED_GET)
                .withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), THERMOSTAT_FAN_MODE_SUPPORTED_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ZWaveTransaction setValueMessage(int value) {

        if (fanModeTypes.isEmpty()) {
            logger.warn("NODE {}: requesting fan mode types, set request ignored (try again later)",
                    this.getNode().getNodeId());
            return this.getSupportedMessage();
        }

        if (!fanModeTypes.contains(FanModeType.getFanModeType(value))) {
            logger.error("NODE {}: Unsupported fanMode type {}", value, this.getNode().getNodeId());

            return null;
        }

        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_FAN_MODE_SET",
                this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), THERMOSTAT_FAN_MODE_SET).withNodeId(getNode().getNodeId()).build();

        return new ZWaveTransactionBuilder(serialMessage).withPriority(TransactionPriority.Set).build();
    }

    /**
     * Z-Wave FanModeType enumeration. The fanMode type indicates the type of fanMode that is reported.
     *
     * @author Dan Cunningham
     */
    @XStreamAlias("fanModeType")
    public enum FanModeType {
        AUTO_LOW(0, "Auto Low"),
        ON_LOW(1, "On Low"),
        AUTO_HIGH(2, "Auto High"),
        ON_HIGH(3, "On High"),
        UNKNOWN_4(4, "Unknown 4"),
        UNKNOWN_5(5, "Unknown 5"),
        CIRCULATE(6, "Circulate");

        /**
         * A mapping between the integer code and its corresponding fan mode type to facilitate lookup by code.
         */
        private static Map<Integer, FanModeType> codeToFanModeTypeMapping;

        private int key;
        private String label;

        private FanModeType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToFanModeTypeMapping = new HashMap<Integer, FanModeType>();
            for (FanModeType s : values()) {
                codeToFanModeTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the fan mode type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the fan mode type. null if value doesn't exist
         */
        public static FanModeType getFanModeType(int i) {
            if (codeToFanModeTypeMapping == null) {
                initMapping();
            }
            return codeToFanModeTypeMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }
    }
}
