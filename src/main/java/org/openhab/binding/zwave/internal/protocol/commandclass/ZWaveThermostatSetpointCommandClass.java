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

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
 * Handles the Thermostat Setpoint command class.
 *
 * @author Chris Jackson
 * @author Matthew Bowman
 * @author Jan-Willem Spuij
 * @author Dave Hock
 */
@XStreamAlias("COMMAND_CLASS_THERMOSTAT_SETPOINT")
public class ZWaveThermostatSetpointCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveThermostatSetpointCommandClass.class);

    private static final byte THERMOSTAT_SETPOINT_SET = 0x1;
    private static final byte THERMOSTAT_SETPOINT_GET = 0x2;
    private static final byte THERMOSTAT_SETPOINT_REPORT = 0x3;
    private static final byte THERMOSTAT_SETPOINT_SUPPORTED_GET = 0x4;
    private static final byte THERMOSTAT_SETPOINT_SUPPORTED_REPORT = 0x5;

    // Some Thermostats (Honywell) will not report on all the setpoints they claim to support.
    // If we try this many times for the initial dynamic queries, give up.
    private static final int MAX_DYNAMIC_TRIES = 5;

    private final Map<SetpointType, Setpoint> setpoints = new HashMap<SetpointType, Setpoint>();

    @XStreamOmitField
    private boolean initialiseDone = false;
    @XStreamOmitField
    private final boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveThermostatSetpointCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveThermostatSetpointCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT;
    }

    @Override
    public int getMaxVersion() {
        return 2;
    }

    @ZWaveResponseHandler(id = THERMOSTAT_SETPOINT_SUPPORTED_REPORT, name = "THERMOSTAT_SETPOINT_SUPPORTED_REPORT")
    public void handleThermostatSetpointSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        int payloadLength = payload.getPayloadLength();

        for (int i = 2; i < payloadLength; ++i) {
            int bitMask = payload.getPayloadByte(i);
            for (int bit = 0; bit < 8; ++bit) {
                if ((bitMask & (1 << bit)) == 0) {
                    continue;
                }

                int index = ((i - 2) * 8) + bit;
                if (index >= SetpointType.values().length) {
                    continue;
                }

                // (n)th bit is set. n is the index for the setpoint type enumeration.
                SetpointType setpointTypeToAdd = SetpointType.getSetpointType(index);
                if (setpointTypeToAdd != null) {
                    Setpoint newSetpoint = new Setpoint(setpointTypeToAdd);
                    setpoints.put(setpointTypeToAdd, newSetpoint);
                    logger.debug("NODE {}: Added mode type {} ({})", this.getNode().getNodeId(),
                            setpointTypeToAdd.getLabel(), index);
                } else {
                    logger.warn("NODE {}: Unknown mode type {}", this.getNode().getNodeId(), index);
                }
            }
        }

        initialiseDone = true;
    }

    /**
     * Processes a THERMOSTAT_SETPOINT_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = THERMOSTAT_SETPOINT_REPORT, name = "THERMOSTAT_SETPOINT_REPORT")
    public void handleThermostatSetpointReport(ZWaveCommandClassPayload payload, int endpoint) {

        int setpointTypeCode = payload.getPayloadByte(2);
        int scale = (payload.getPayloadByte(3) >> 3) & 0x03;

        try {
            BigDecimal value = extractValue(payload, 3);

            logger.debug("NODE {}: Thermostat Setpoint report Scale = {}", this.getNode().getNodeId(), scale);
            logger.debug("NODE {}: Thermostat Setpoint Value = {}", this.getNode().getNodeId(), value);

            SetpointType setpointType = SetpointType.getSetpointType(setpointTypeCode);

            if (setpointType == null) {
                logger.debug("NODE {}: Unknown Setpoint Type = {}, ignoring report.", this.getNode().getNodeId(),
                        setpointTypeCode);
                return;
            }

            // Setpoint type seems to be supported, add it to the list.
            Setpoint setpoint = setpoints.get(setpointType);
            if (setpoint == null) {
                setpoint = new Setpoint(setpointType);
                setpoints.put(setpointType, setpoint);
            }
            setpoint.setInitialised();

            logger.debug("NODE {}: Thermostat Setpoint Report, Type {} ({}), value = {}", this.getNode().getNodeId(),
                    setpointType.getLabel(), setpointTypeCode, value.toPlainString());
            ZWaveThermostatSetpointValueEvent zEvent = new ZWaveThermostatSetpointValueEvent(this.getNode().getNodeId(),
                    endpoint, setpointType, scale, value);
            this.getController().notifyEventListeners(zEvent);
        } catch (NumberFormatException e) {
            return;
        }
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (refresh == true || initialiseDone == false) {
            result.add(getSupportedMessage());
        }
        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        for (Map.Entry<SetpointType, Setpoint> entry : setpoints.entrySet()) {
            if (refresh == true || entry.getValue().getInitialised() == false) {
                ZWaveCommandClassTransactionPayload mesg = getMessage(entry.getValue().getSetpointType());
                entry.getValue().incrementInitCount();
                if (mesg == null) {
                    logger.warn("NODE {}: Ignoring null setpointType in setpointTypes", getNode().getNodeId());
                } else {
                    result.add(mesg);
                }
            }
        }
        return result;
    }

    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        for (Map.Entry<SetpointType, Setpoint> entry : this.setpoints.entrySet()) {
            return getMessage(entry.getValue().getSetpointType());
        }

        // In case there are no supported setpoint types, get them.
        return getSupportedMessage();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the THERMOSTAT_SETPOINT_GET command
     *
     * @param setpointType the setpoint type to get
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getMessage(SetpointType setpointType) {
        if (setpointType == null) {
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_SETPOINT_GET ({})",
                this.getNode().getNodeId(), setpointType.getLabel());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                THERMOSTAT_SETPOINT_GET).withPayload(setpointType.getKey()).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(THERMOSTAT_SETPOINT_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the THERMOSTAT_SETPOINT_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public ZWaveCommandClassTransactionPayload getSupportedMessage() {
        logger.debug("NODE {}: Creating new message for command THERMOSTAT_SETPOINT_SUPPORTED_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                THERMOSTAT_SETPOINT_SUPPORTED_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(THERMOSTAT_SETPOINT_SUPPORTED_REPORT).build();
    }

    public ZWaveCommandClassTransactionPayload setValueMessage(int value) {
        return setMessage(0, new BigDecimal(value));
    }

    /**
     * Gets a SerialMessage with the THERMOSTAT_SETPOINT_SET command
     *
     * @param setpoint the setpoint to set.
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload setMessage(int scale, BigDecimal setpoint) {
        for (Map.Entry<SetpointType, Setpoint> entry : setpoints.entrySet()) {
            return setMessage(scale, entry.getValue().getSetpointType(), setpoint);
        }

        // in case there are no supported setpoint types, get them.
        return getSupportedMessage();
    }

    /**
     * Gets a SerialMessage with the THERMOSTAT_SETPOINT_SET command
     *
     * @param scale the scale (DegC or DegF)
     * @param setpointType the setpoint type to set
     * @param setpoint the setpoint to set.
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload setMessage(int scale, SetpointType setpointType, BigDecimal setpoint) {
        logger.debug("NODE {}: Creating new message for command THERMOSTAT_SETPOINT_SET", getNode().getNodeId());

        try {
            byte[] encodedValue = encodeValue(setpoint);
            ByteArrayOutputStream outputData = new ByteArrayOutputStream();
            outputData.write(setpointType.getKey());
            outputData.write(encodedValue[0] + (scale << 3));
            for (int c = 1; c < encodedValue.length; c++) {
                outputData.write(encodedValue[c]);
            }

            return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                    THERMOSTAT_SETPOINT_SET).withPayload(outputData.toByteArray()).withPriority(TransactionPriority.Set)
                            .build();
        } catch (ArithmeticException e) {
            logger.error(
                    "NODE {}: Got an arithmetic exception converting value {} to a valid Z-Wave value. Ignoring THERMOSTAT_SETPOINT_SET message.",
                    getNode().getNodeId(), setpoint);
            return null;
        }
    }

    /**
     * Z-Wave SetpointType enumeration. The setpoint type indicates the type of setpoint that is reported.
     */
    @XStreamAlias("setpointType")
    public enum SetpointType {
        HEATING(1, "Heating"),
        COOLING(2, "Cooling"),
        FURNACE(7, "Furnace"),
        DRY_AIR(8, "Dry Air"),
        MOIST_AIR(9, "Moist Air"),
        AUTO_CHANGEOVER(10, "Auto Changeover"),
        HEATING_ECON(11, "Heating Economical"),
        COOLING_ECON(12, "Cooling Economical"),
        AWAY_HEATING(13, "Away Heating"),
        AWAY_COOLING(14, "Away Cooling"),
        FULL_POWER(15, "Full Power");

        /**
         * A mapping between the integer code and its corresponding setpoint type
         * to facilitate lookup by code.
         */
        private static Map<Integer, SetpointType> codeToSetpointTypeMapping;

        /**
         * A mapping between the name,and its corresponding SetpointType to facilitate lookup by enumeration name.
         */
        private static Map<String, SetpointType> nameToSetpointTypeMapping;

        private int key;
        private String label;

        private SetpointType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToSetpointTypeMapping = new HashMap<Integer, SetpointType>();
            nameToSetpointTypeMapping = new HashMap<>();
            for (SetpointType s : values()) {
                codeToSetpointTypeMapping.put(s.key, s);
                nameToSetpointTypeMapping.put(s.name().toLowerCase(), s);
            }
        }

        /**
         * Lookup function based on the setpoint type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the setpoint type.
         */
        public static SetpointType getSetpointType(int i) {
            if (codeToSetpointTypeMapping == null) {
                initMapping();
            }
            return codeToSetpointTypeMapping.get(i);
        }

        /**
         * Lookup function based on the name. Returns null if the name does not exist.
         *
         * @param name the name to lookup
         * @return enumeration value of the setpoint type.
         */
        public static SetpointType getSetpointType(String name) {
            if (nameToSetpointTypeMapping == null) {
                initMapping();
            }

            return nameToSetpointTypeMapping.get(name.toLowerCase());
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

    /**
     * Class to hold setpoint state
     *
     * @author Chris Jackson
     */
    @XStreamAlias("setpoint")
    public class Setpoint {
        SetpointType setpointType;
        boolean initialised = false;
        int initCount = 0;

        public Setpoint(SetpointType type) {
            setpointType = type;
        }

        public SetpointType getSetpointType() {
            return setpointType;
        }

        public void setInitialised() {
            initialised = true;
            initCount = 0;
        }

        public boolean getInitialised() {
            return initialised;
        }

        public void incrementInitCount() {
            initCount++;
            if (initCount >= MAX_DYNAMIC_TRIES) {
                setInitialised();
                logger.warn("Reached max tries to init the setpont {}, this will be our last attempt ",
                        setpointType.getLabel());
            }
        }
    }

    /**
     * Z-Wave Thermostat Setpoint Event class. Indicates that a setpoint value changed.
     *
     * @author Jan-Willem Spuij
     */
    public class ZWaveThermostatSetpointValueEvent extends ZWaveCommandClassValueEvent {

        private final SetpointType setpointType;
        private final int scale;

        /**
         * Constructor. Creates a instance of the ZWaveThermostatSetpointValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param setpointType the setpoint type that triggered the event;
         * @param value the value for the event.
         */
        private ZWaveThermostatSetpointValueEvent(int nodeId, int endpoint, SetpointType setpointType, int scale,
                Object value) {
            super(nodeId, endpoint, CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT, value);
            this.setpointType = setpointType;
            this.scale = scale;
        }

        /**
         * Gets the alarm type for this alarm sensor value event.
         */
        public SetpointType getSetpointType() {
            return setpointType;
        }

        /**
         * Gets the scale for this event
         */
        public int getScale() {
            return this.scale;
        }
    }
}
