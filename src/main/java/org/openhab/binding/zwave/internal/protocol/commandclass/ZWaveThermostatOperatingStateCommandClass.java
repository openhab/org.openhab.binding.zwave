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
 * Handles the Thermostat OperatingState command class.
 *
 * @author Chris Jackson
 * @author Dan Cunningham
 */
@XStreamAlias("COMMAND_CLASS_THERMOSTAT_OPERATING_STATE")
public class ZWaveThermostatOperatingStateCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveThermostatOperatingStateCommandClass.class);

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

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_THERMOSTAT_OPERATING_STATE;
    }

    @Override
    public int getMaxVersion() {
        return 2;
    }

    /**
     * Processes a THERMOSTAT_OPERATING_STATE_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = THERMOSTAT_OPERATING_STATE_REPORT, name = "THERMOSTAT_OPERATING_STATE_REPORT")
    public void handleThermostatOperatingStateReport(ZWaveCommandClassPayload payload, int endpoint) {
        int value = payload.getPayloadByte(2);

        logger.debug("NODE {}: Thermostat Operating State report value = {}", getNode().getNodeId(), value);

        OperatingStateType operatingStateType = OperatingStateType.getOperatingStateType(value);

        if (operatingStateType == null) {
            logger.error("NODE {}: Unknown Operating State Type = {}, ignoring report.", getNode().getNodeId(), value);
            return;
        }

        dynamicDone = true;

        logger.debug("NODE {}: Operating State Type = {} ({})", getNode().getNodeId(), operatingStateType.getLabel(),
                value);

        logger.debug("NODE {}: Thermostat Operating State Report value = {}", getNode().getNodeId(),
                operatingStateType.getLabel());
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), new BigDecimal(value));
        getController().notifyEventListeners(zEvent);
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

        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_OPERATING_STATE_GET",
                this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                THERMOSTAT_OPERATING_STATE_GET).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(THERMOSTAT_OPERATING_STATE_REPORT).build();
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
