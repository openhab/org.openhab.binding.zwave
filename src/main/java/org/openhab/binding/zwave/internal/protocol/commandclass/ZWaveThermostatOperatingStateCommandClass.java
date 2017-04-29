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
 * Handles the Thermostat OperatingState command class.
 *
 * @author Chris Jackson
 * @author Dan Cunningham
 */
@XStreamAlias("thermostatOperatingStateCommandClass")
public class ZWaveThermostatOperatingStateCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private final static Logger logger = LoggerFactory.getLogger(ZWaveThermostatOperatingStateCommandClass.class);

    private static final byte THERMOSTAT_OPERATING_STATE_GET = 0x2;
    private static final byte THERMOSTAT_OPERATING_STATE_REPORT = 0x3;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveThermostatOperatingStateCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveThermostatOperatingStateCommandClass(ZWaveNode node, ZWaveController controller,
            ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.THERMOSTAT_OPERATING_STATE;
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
        logger.debug("NODE {}: Received Thermostat Operating State Request", this.getNode().getNodeId());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case THERMOSTAT_OPERATING_STATE_REPORT:
                logger.trace("NODE {}: Process Thermostat Operating State Report", this.getNode().getNodeId());
                processThermostatOperatingStateReport(serialMessage, offset, endpoint);
                break;
            default:
                logger.warn("NODE {}: Unsupported Command {} for command class {} ({}).", this.getNode().getNodeId(),
                        command, this.getCommandClass().getLabel(), this.getCommandClass().getKey());
        }
    }

    /**
     * Processes a THERMOSTAT_OPERATING_STATE_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    protected void processThermostatOperatingStateReport(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {

        int value = serialMessage.getMessagePayloadByte(offset + 1);

        logger.debug("NODE {}: Thermostat Operating State report value = {}", this.getNode().getNodeId(), value);

        OperatingStateType operatingStateType = OperatingStateType.getOperatingStateType(value);

        if (operatingStateType == null) {
            logger.debug("NODE {}: Unknown Operating State Type = {}, ignoring report.", this.getNode().getNodeId(),
                    value);
            return;
        }

        dynamicDone = true;

        logger.debug("NODE {}: Operating State Type = {} ({})", this.getNode().getNodeId(),
                operatingStateType.getLabel(), value);

        logger.debug("NODE {}: Thermostat Operating State Report value = {}", this.getNode().getNodeId(),
                operatingStateType.getLabel());
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                this.getCommandClass(), new BigDecimal(value));
        this.getController().notifyEventListeners(zEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        if (refresh == true || dynamicDone == false) {
            result.add(getValueMessage());
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

        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_OPERATING_STATE_GET",
                this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] payload = { (byte) this.getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                THERMOSTAT_OPERATING_STATE_GET };
        result.setMessagePayload(payload);
        return result;
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Z-Wave OperatingStateType enumeration. The operating state type indicates the type of operating state that is
     * reported from the thermostat.
     *
     * @author Dan Cunningham
     */
    @XStreamAlias("operatingStateType")
    public enum OperatingStateType {
        IDLE(0, "Idle"),
        HEATING(1, "Heating"),
        COOLING(2, "Cooling"),
        FAN_ONLY(3, "Fan Only"),
        PENDING_HEAT(4, "Pending Heat"),
        PENDING_COOL(5, "Pending Cool"),
        VENT(6, "Vent / Economizer"),
        State_7(7, "State 7"), // Undefined states. May be used in the future.
        State_8(8, "State 8"),
        State_9(9, "State 9"),
        State_10(10, "State 10"),
        State_11(11, "State 11"),
        State_12(12, "State 12"),
        State_13(12, "State 13"),
        State_14(14, "State 14"),
        State_15(15, "State 15");

        /**
         * A mapping between the integer code and its corresponding operating state type to facilitate lookup by code.
         */
        private static Map<Integer, OperatingStateType> codeToOperatingStateTypeMapping;

        private int key;
        private String label;

        private OperatingStateType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToOperatingStateTypeMapping = new HashMap<Integer, OperatingStateType>();
            for (OperatingStateType s : values()) {
                codeToOperatingStateTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the operating state type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the operatingState type.
         */
        public static OperatingStateType getOperatingStateType(int i) {
            if (codeToOperatingStateTypeMapping == null) {
                initMapping();
            }
            return codeToOperatingStateTypeMapping.get(i);
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
