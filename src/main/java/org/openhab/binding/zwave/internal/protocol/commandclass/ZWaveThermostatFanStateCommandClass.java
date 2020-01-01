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
 * Handles the Thermostat FanState command class.
 *
 * @author Chris Jackson
 * @author Dan Cunningham
 */
@XStreamAlias("COMMAND_CLASS_THERMOSTAT_FAN_STATE")
public class ZWaveThermostatFanStateCommandClass extends ZWaveCommandClass implements ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveThermostatFanStateCommandClass.class);

    private static final byte THERMOSTAT_FAN_STATE_GET = 0x2;
    private static final byte THERMOSTAT_FAN_STATE_REPORT = 0x3;

    private final Set<FanStateType> fanStateTypes = new HashSet<FanStateType>();

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveThermostatFanStateCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveThermostatFanStateCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_STATE;
    }

    @Override
    public int getMaxVersion() {
        return 2;
    }

    /**
     * Processes a THERMOSTAT_FAN_STATE_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = THERMOSTAT_FAN_STATE_REPORT, name = "THERMOSTAT_FAN_STATE_REPORT")
    public void handleThermostatFanStateReport(ZWaveCommandClassPayload payload, int endpoint) {
        int value = payload.getPayloadByte(2);

        logger.debug("NODE {}: Thermostat fan state report value = {}", this.getNode().getNodeId(), value);

        FanStateType fanStateType = FanStateType.getFanStateType(value);

        if (fanStateType == null) {
            logger.error("NODE {}: Unknown fan state Type = {}, ignoring report.", this.getNode().getNodeId(), value);
            return;
        }

        // fanState type seems to be supported, add it to the list.
        if (!fanStateTypes.contains(fanStateType)) {
            fanStateTypes.add(fanStateType);
        }

        dynamicDone = true;

        logger.debug("NODE {}: Thermostat fan state  Report value = {}", this.getNode().getNodeId(),
                fanStateType.getLabel());
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                this.getCommandClass(), value);
        this.getController().notifyEventListeners(zEvent);
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        // TODO (or question for Dan from Chris) - shouldn't this iterate through all fan modes?
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

        logger.debug("NODE {}: Creating new message for application command THERMOSTAT_FAN_STATE_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                THERMOSTAT_FAN_STATE_GET).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(THERMOSTAT_FAN_STATE_REPORT).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Z-Wave FanStateType enumeration. The fanState type indicates the type of fanState that is reported.
     *
     * @author Dan Cunningham
     */
    @XStreamAlias("fanStateType")
    public enum FanStateType {

        IDLE(0, "Idle"),
        RUNNING(1, "Running"),
        RUNNING_HIGH(2, "Running High"),
        State_3(3, "State 3"),
        State_4(4, "State 4"),
        State_5(5, "State 5"),
        State_6(6, "State 6"),
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
         * A mapping between the integer code and its corresponding fanState type to facilitate lookup by code.
         */
        private static Map<Integer, FanStateType> codeToFanStateTypeMapping;

        private int key;
        private String label;

        private FanStateType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToFanStateTypeMapping = new HashMap<Integer, FanStateType>();
            for (FanStateType s : values()) {
                codeToFanStateTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the fanState type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the fanState type. null if code doesn't exist
         */
        public static FanStateType getFanStateType(int i) {
            if (codeToFanStateTypeMapping == null) {
                initMapping();
            }
            return codeToFanStateTypeMapping.get(i);
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
