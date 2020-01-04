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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
 * Handles the Thermostat Mode command class.
 *
 * @author Chris Jackson
 * @author Dan Cunningham
 */
@XStreamAlias("COMMAND_CLASS_THERMOSTAT_MODE")
public class ZWaveThermostatModeCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveThermostatModeCommandClass.class);

    private static final byte THERMOSTAT_MODE_SET = 0x1;
    private static final byte THERMOSTAT_MODE_GET = 0x2;
    private static final byte THERMOSTAT_MODE_REPORT = 0x3;
    private static final byte THERMOSTAT_MODE_SUPPORTED_GET = 0x4;
    private static final byte THERMOSTAT_MODE_SUPPORTED_REPORT = 0x5;

    private final Set<ModeType> modeTypes = new HashSet<ModeType>();

    @XStreamOmitField
    private boolean initialiseDone = false;
    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveThermostatModeCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveThermostatModeCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_THERMOSTAT_MODE;
    }

    @Override
    public int getMaxVersion() {
        return 2;
    }

    @ZWaveResponseHandler(id = THERMOSTAT_MODE_SUPPORTED_REPORT, name = "THERMOSTAT_MODE_SUPPORTED_REPORT")
    public void handleThermostatFanStateReport(ZWaveCommandClassPayload payload, int endpoint) {
        int payloadLength = payload.getPayloadLength();

        for (int i = 2; i < payloadLength; ++i) {
            int bitMask = payload.getPayloadByte(i);
            for (int bit = 0; bit < 8; ++bit) {
                if ((bitMask & (1 << bit)) == 0) {
                    continue;
                }

                int index = ((i - 2) * 8) + bit;

                // (n)th bit is set. n is the index for the mode type enumeration.
                ModeType modeTypeToAdd = ModeType.getModeType(index);
                if (modeTypeToAdd != null) {
                    this.modeTypes.add(modeTypeToAdd);
                    logger.debug("NODE {}: Added mode type {} ({})", this.getNode().getNodeId(),
                            modeTypeToAdd.getLabel(), index);
                } else {
                    logger.warn("NODE {}: Unknown mode type {}", this.getNode().getNodeId(), index);
                }
            }
        }

        initialiseDone = true;
    }

    /**
     * Processes a THERMOSTAT_MODE_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = THERMOSTAT_MODE_REPORT, name = "THERMOSTAT_MODE_REPORT")
    public void handleThermostatModeReport(ZWaveCommandClassPayload payload, int endpoint) {
        int value = payload.getPayloadByte(2);

        logger.debug("NODE {}: Thermostat Mode report, value = {}", this.getNode().getNodeId(), value);

        ModeType modeType = ModeType.getModeType(value);

        if (modeType == null) {
            logger.error("NODE {}: Unknown Mode Type = {}, ignoring report.", this.getNode().getNodeId(), value);
            return;
        }

        // mode type seems to be supported, add it to the list.
        if (!modeTypes.contains(modeType)) {
            modeTypes.add(modeType);
        }

        dynamicDone = true;

        logger.debug("NODE {}: Thermostat Mode Report, value = {}", this.getNode().getNodeId(), modeType.getLabel());
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                this.getCommandClass(), value);
        this.getController().notifyEventListeners(zEvent);
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (refresh == true || initialiseDone == false) {
            result.add(this.getSupportedMessage());
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

    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_MODE_GET",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                THERMOSTAT_MODE_GET).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(THERMOSTAT_MODE_REPORT).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the THERMOSTAT_MODE_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public ZWaveCommandClassTransactionPayload getSupportedMessage() {
        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_MODE_SUPPORTED_GET",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                THERMOSTAT_MODE_SUPPORTED_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(THERMOSTAT_MODE_SUPPORTED_REPORT).build();
    }

    public ZWaveCommandClassTransactionPayload setValueMessage(int value) {
        logger.debug("NODE {}: setValueMessage {}, modeType empty {}", getNode().getNodeId(), value,
                modeTypes.isEmpty());

        // if we do not have any mode types yet, get them
        if (modeTypes.isEmpty()) {
            logger.warn("NODE {}: requesting mode types, set request ignored (try again later)", getNode().getNodeId());
            return getSupportedMessage();
        }

        if (!modeTypes.contains(ModeType.getModeType(value))) {
            logger.error("NODE {}: Unsupported mode type {}", getNode().getNodeId(), value);
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_MODE_SET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                THERMOSTAT_MODE_SET).withPayload(value).withPriority(TransactionPriority.Set).build();
    }

    /**
     * Z-Wave ModeType enumeration. The mode type indicates the type of mode that is reported.
     *
     * @author Dan Cunningham
     */
    @XStreamAlias("modeType")
    public enum ModeType {

        OFF(0, "Off"),
        HEAT(1, "Heat"),
        COOL(2, "Cool"),
        AUTO(3, "Auto"),
        AUX_HEAT(4, "Aux Heat"),
        RESUME(5, "Resume"),
        FAN_ONLY(6, "Fan Only"),
        FURNANCE(7, "Furnace"),
        DRY_AIR(8, "Dry Air"),
        MOIST_AIR(9, "Moist Air"),
        AUTO_CHANGEOVER(10, "Auto Changeover"),
        HEAT_ECON(11, "Heat Econ"),
        COOL_ECON(12, "Cool Econ"),
        AWAY(13, "Away"),
        FULL_POWER(15, "Full Power"),
        MANUAL(31, "Manual");

        /**
         * A mapping between the integer code and its corresponding mode type to facilitate lookup by code.
         */
        private static Map<Integer, ModeType> codeToModeTypeMapping;

        private int key;
        private String label;

        private ModeType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToModeTypeMapping = new HashMap<Integer, ModeType>();
            for (ModeType s : values()) {
                codeToModeTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the mode type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the mode type.
         */
        public static ModeType getModeType(int i) {
            if (codeToModeTypeMapping == null) {
                initMapping();
            }
            return codeToModeTypeMapping.get(i);
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
