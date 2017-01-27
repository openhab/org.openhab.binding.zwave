/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
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
@XStreamAlias("thermostatSetpointCommandClass")
public class ZWaveThermostatSetpointCommandClass extends ZWaveCommandClass
        implements ZWaveBasicCommands, ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

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
    private boolean dynamicDone = false;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.THERMOSTAT_SETPOINT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxVersion() {
        return 2;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received Thermostat Setpoint Request", this.getNode().getNodeId());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case THERMOSTAT_SETPOINT_SUPPORTED_REPORT:
                logger.debug("NODE {}: Process Thermostat Supported Setpoint Report", this.getNode().getNodeId());

                int payloadLength = serialMessage.getMessagePayload().length;

                for (int i = offset + 1; i < payloadLength; ++i) {
                    int bitMask = serialMessage.getMessagePayloadByte(i);
                    for (int bit = 0; bit < 8; ++bit) {
                        if ((bitMask & (1 << bit)) == 0) {
                            continue;
                        }

                        int index = ((i - (offset + 1)) * 8) + bit;
                        if (index >= SetpointType.values().length) {
                            continue;
                        }

                        // (n)th bit is set. n is the index for the setpoint type enumeration.
                        SetpointType setpointTypeToAdd = SetpointType.getSetpointType(index);
                        if (setpointTypeToAdd != null) {
                            Setpoint newSetpoint = new Setpoint(setpointTypeToAdd);
                            this.setpoints.put(setpointTypeToAdd, newSetpoint);
                            logger.debug("NODE {}: Added mode type {} ({})", this.getNode().getNodeId(),
                                    setpointTypeToAdd.getLabel(), index);
                        } else {
                            logger.warn("NODE {}: Unknown mode type {}", this.getNode().getNodeId(), index);
                        }
                    }
                }

                initialiseDone = true;
                break;
            case THERMOSTAT_SETPOINT_REPORT:
                processThermostatSetpointReport(serialMessage, offset, endpoint);
                break;
            default:
                logger.warn("NODE {}: Unsupported Command {} for command class {} ({}).", this.getNode().getNodeId(),
                        command, this.getCommandClass().getLabel(), this.getCommandClass().getKey());
        }
    }

    /**
     * Processes a THERMOSTAT_SETPOINT_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    protected void processThermostatSetpointReport(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {

        int setpointTypeCode = serialMessage.getMessagePayloadByte(offset + 1);
        int scale = (serialMessage.getMessagePayloadByte(offset + 2) >> 3) & 0x03;

        try {
            BigDecimal value = extractValue(serialMessage.getMessagePayload(), offset + 2);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        if (refresh == true || initialiseDone == false) {
            result.add(this.getSupportedMessage());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        for (Map.Entry<SetpointType, Setpoint> entry : this.setpoints.entrySet()) {
            if (refresh == true || entry.getValue().getInitialised() == false) {
                SerialMessage mesg = getMessage(entry.getValue().getSetpointType());
                entry.getValue().incrementInitCount();
                if (mesg == null) {
                    logger.warn("NODE {}: Ignoring null setpointType in setpointTypes", this.getNode().getNodeId());
                } else {
                    result.add(mesg);
                }
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerialMessage getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        for (Map.Entry<SetpointType, Setpoint> entry : this.setpoints.entrySet()) {
            return getMessage(entry.getValue().getSetpointType());
        }

        // In case there are no supported setpoint types, get them.
        return this.getSupportedMessage();
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
    public SerialMessage getMessage(SetpointType setpointType) {
        if (setpointType == null) {
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_SETPOINT_GET ({})",
                this.getNode().getNodeId(), setpointType.getLabel());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] payload = { (byte) this.getNode().getNodeId(), 3, (byte) getCommandClass().getKey(),
                THERMOSTAT_SETPOINT_GET, (byte) setpointType.getKey() };
        result.setMessagePayload(payload);
        return result;
    }

    /**
     * Gets a SerialMessage with the THERMOSTAT_SETPOINT_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public SerialMessage getSupportedMessage() {
        logger.debug("NODE {}: Creating new message for command THERMOSTAT_SETPOINT_SUPPORTED_GET",
                this.getNode().getNodeId());

        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Config);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                THERMOSTAT_SETPOINT_SUPPORTED_GET };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SerialMessage setValueMessage(int value) {
        return setMessage(0, new BigDecimal(value));
    }

    /**
     * Gets a SerialMessage with the THERMOSTAT_SETPOINT_SET command
     *
     * @param setpoint the setpoint to set.
     * @return the serial message
     */
    public SerialMessage setMessage(int scale, BigDecimal setpoint) {
        for (Map.Entry<SetpointType, Setpoint> entry : this.setpoints.entrySet()) {
            return setMessage(scale, entry.getValue().getSetpointType(), setpoint);
        }

        // in case there are no supported setpoint types, get them.
        return this.getSupportedMessage();
    }

    /**
     * Gets a SerialMessage with the THERMOSTAT_SETPOINT_SET command
     *
     * @param scale the scale (DegC or DegF)
     * @param setpointType the setpoint type to set
     * @param setpoint the setpoint to set.
     * @return the serial message
     */
    public SerialMessage setMessage(int scale, SetpointType setpointType, BigDecimal setpoint) {
        logger.debug("NODE {}: Creating new message for command THERMOSTAT_SETPOINT_SET", this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);

        try {
            byte[] encodedValue = encodeValue(setpoint);

            byte[] payload = ArrayUtils.addAll(
                    new byte[] { (byte) this.getNode().getNodeId(), (byte) (3 + encodedValue.length),
                            (byte) getCommandClass().getKey(), THERMOSTAT_SETPOINT_SET, (byte) setpointType.getKey() },
                    encodedValue);
            // Add the scale
            payload[5] += (byte) (scale << 3);

            result.setMessagePayload(payload);
            return result;
        } catch (ArithmeticException e) {
            logger.error(
                    "NODE {}: Got an arithmetic exception converting value {} to a valid Z-Wave value. Ignoring THERMOSTAT_SETPOINT_SET message.",
                    this.getNode().getNodeId(), setpoint);
            return null;
        }
    }

    /**
     * Z-Wave SetpointType enumeration. The setpoint type indicates the type of setpoint that is reported.
     *
     * @author Matthew Bowman
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
        AWAY_HEATING(13, "Away Heating");

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
    private class Setpoint {
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

        private SetpointType setpointType;
        private int scale;

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
            super(nodeId, endpoint, CommandClass.THERMOSTAT_SETPOINT, value);
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
