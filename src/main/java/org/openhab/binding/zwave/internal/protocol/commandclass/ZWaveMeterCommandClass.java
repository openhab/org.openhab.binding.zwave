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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Handles the Meter command class. The Meter Command Class is intended for all kinds of meters that report measured
 * quantities, such as gas, electricity and water meters
 *
 * @author Chris Jackson
 * @author Ben Jones
 * @author Jan-Willem Spuij
 */
@XStreamAlias("COMMAND_CLASS_METER")
public class ZWaveMeterCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveMeterCommandClass.class);
    private static final int MAX_SUPPORTED_VERSION = 3;

    // Version 1
    private static final int METER_GET = 1;
    private static final int METER_REPORT = 2;

    // Version 2 and 3
    private static final int METER_SUPPORTED_GET = 3;
    private static final int METER_SUPPORTED_REPORT = 4;
    private static final int METER_RESET = 5;

    private MeterType meterType = null;
    private final Set<MeterScale> meterScales = new HashSet<MeterScale>();

    private volatile boolean canReset = false;

    @XStreamOmitField
    private boolean initialiseDone = false;
    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;
    private boolean isSupportRequestSupported = true;

    /**
     * Creates a new instance of the ZWaveMeterCommandClass class.
     *
     * @param node
     *                       the node this command class belongs to
     * @param controller
     *                       the controller to use
     * @param endpoint
     *                       the endpoint this Command class belongs to
     */
    public ZWaveMeterCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_METER;
    }

    @ZWaveResponseHandler(id = METER_REPORT, name = "METER_REPORT")
    public void handleMeterReport(ZWaveCommandClassPayload payload, int endpoint) {
        // Sanity check
        if (payload.getPayloadLength() < 4) {
            logger.warn("NODE {}: Meter Report: Buffer too short: length={}, required={}", getNode().getNodeId(),
                    payload.getPayloadLength(), 4);
            return;
        }

        int meterTypeIndex = payload.getPayloadByte(2) & 0x1F;
        if (meterTypeIndex >= MeterType.values().length) {
            logger.warn("NODE {}: Invalid meter type {}", getNode().getNodeId(), meterTypeIndex);
            return;
        }

        int meterRateType = (payload.getPayloadByte(2) & 0x60) >> 5;

        meterType = MeterType.getMeterType(meterTypeIndex);
        int scaleIndex = (payload.getPayloadByte(3) & 0x18) >> 0x03;

        // TODO: Does it matter about version? V1 and V2 require this to be 0 anyway.
        if (getVersion() >= 3) {
            // In version 3, an extra scale bit is stored in the meter type byte.
            scaleIndex |= ((payload.getPayloadByte(2) & 0x80) >> 0x05);
        }

        MeterScale scale = MeterScale.getMeterScale(meterType, scaleIndex);
        if (scale == null) {
            logger.warn("NODE {}: Invalid meter scale {}", getNode().getNodeId(), scaleIndex);
            return;
        }

        // add scale to the list of supported scales.
        if (!meterScales.contains(scale)) {
            meterScales.add(scale);
        }

        try {
            BigDecimal value = extractValue(payload, 3);
            logger.debug("NODE {}: Meter: Type={}({}), Scale={}({}), Value={}", getNode().getNodeId(),
                    meterType.getLabel(), meterTypeIndex, scale.getUnit(), scale.getScale(), value);

            ZWaveMeterValueEvent event = new ZWaveMeterValueEvent(getNode().getNodeId(), endpoint, meterType, scale,
                    value);
            getController().notifyEventListeners(event);
        } catch (NumberFormatException e) {
            logger.error("NODE {}: Meter Value Error. {}", getNode().getNodeId(), e.getMessage());
            return;
        }

        dynamicDone = true;
    }

    @ZWaveResponseHandler(id = METER_SUPPORTED_REPORT, name = "METER_SUPPORTED_REPORT")
    public void handleMeterSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        canReset = (payload.getPayloadByte(2) & 0x80) != 0;
        int meterTypeIndex = payload.getPayloadByte(2) & 0x1F;
        long supportedScales = payload.getPayloadByte(3) & 0x7F;
        boolean mst = (payload.getPayloadByte(3) & 0x80) != 0;

        // only 4 scales are supported in version 2 of the command.
        if (getVersion() == 2) {
            supportedScales &= 0x0F;
        }

        if (mst) {
            int numberOfScales = payload.getPayloadByte(4);
            for (int cnt = 0; cnt < numberOfScales; cnt++) {
                supportedScales += payload.getPayloadByte(5 + cnt) << ((cnt + 1) * 8);
            }
        }

        if (meterTypeIndex >= MeterType.values().length) {
            logger.warn("NODE {}: Invalid meter type {}", getNode().getNodeId(), meterTypeIndex);
            return;
        }

        meterType = MeterType.getMeterType(meterTypeIndex);
        logger.debug("NODE {}: Identified meter type {}({})", getNode().getNodeId(), meterType.getLabel(),
                meterTypeIndex);

        for (int i = 0; i < 16; ++i) {
            // scale is supported
            if ((supportedScales & (1 << i)) == (1 << i)) {
                MeterScale scale = MeterScale.getMeterScale(meterType, i);

                if (scale == null) {
                    logger.warn("NODE {}: Invalid meter scale {}", getNode().getNodeId(), i);
                    continue;
                }

                logger.debug("NODE {}: Meter Scale = {}({})", getNode().getNodeId(), scale.getUnit(), scale.getScale());

                // add scale to the list of supported scales.
                if (!meterScales.contains(scale)) {
                    meterScales.add(scale);
                }
            }
        }

        initialiseDone = true;
    }

    /**
     * Gets a SerialMessage with the METER_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command METER_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), METER_GET)
                .withPriority(TransactionPriority.Get).withExpectedResponseCommand(METER_REPORT).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        if (options.containsKey("meterCanReset")) {
            canReset = "true".equals(options.get("meterCanReset"));
        }

        if (options.containsKey("meterType") && options.containsKey("meterScale")) {
            meterType = MeterType.valueOf(options.get("meterType"));
            logger.debug("NODE {}: Set meter type {}", getNode().getNodeId(), meterType.getLabel());

            List<String> scaleList = Arrays.asList(options.get("meterScale").split(";"));
            for (String name : scaleList) {
                MeterScale scale = MeterScale.valueOf(name);
                logger.debug("NODE {}: Meter Scale {} = {}", getNode().getNodeId(), meterType.getLabel(),
                        scale.getUnit());

                // Add scale to the list of supported scales.
                if (!meterScales.contains(scale)) {
                    meterScales.add(scale);
                }
            }

            isSupportRequestSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the METER_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getMessage(MeterScale meterScale) {
        logger.debug("NODE {}: Creating new message for application command METER_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), METER_GET)
                .withPayload(meterScale.getScale() << 3).withPriority(TransactionPriority.Get)
                .withExpectedResponseCommand(METER_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the METER_SUPPORTED_GET command
     *
     * @return the serial message, or null if the supported command is not
     *         supported.
     */
    public ZWaveCommandClassTransactionPayload getSupportedMessage() {
        logger.debug("NODE {}: Creating new message for application command METER_SUPPORTED_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                METER_SUPPORTED_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(METER_SUPPORTED_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the METER_RESET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getResetMessage() {
        // ignore the reset if the version is less than one or meter is not resetable
        if (getVersion() == 1 || !canReset) {
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command METER_RESET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), METER_RESET)
                .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Initializes the meter command class. Requests the supported meter types.
     */
    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (isSupportRequestSupported == true && (refresh == true || initialiseDone == false) && getVersion() > 1) {
            result.add(getSupportedMessage());
        }
        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        if (refresh == true || dynamicDone == false) {
            switch (getVersion()) {
                case 1:
                    result.add(getValueMessage());
                    break;
                case 2:
                case 3:
                    for (MeterScale scale : meterScales) {
                        result.add(getMessage(scale));
                    }
                    break;
            }
        }

        return result;
    }

    /**
     * Z-Wave MeterType enumeration. The meter type indicates the type of meter that is reported.
     *
     */
    public enum MeterType {
        UNKNOWN(0, "Unknown"),
        ELECTRIC(1, "Electric"),
        GAS(2, "Gas"),
        WATER(3, "Water"),
        HEATING(4, "Heating"),
        COOLING(5, "Cooling");

        /**
         * A mapping between the integer code and its corresponding Meter type
         * to facilitate lookup by code.
         */
        private static Map<Integer, MeterType> codeToMeterTypeMapping;

        private int key;
        private String label;

        private MeterType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToMeterTypeMapping = new HashMap<Integer, MeterType>();
            for (MeterType s : values()) {
                codeToMeterTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the meter type code. Returns null if the
         * code does not exist.
         *
         * @param i
         *              the code to lookup
         * @return enumeration value of the meter type.
         */
        public static MeterType getMeterType(int i) {
            if (codeToMeterTypeMapping == null) {
                initMapping();
            }

            return codeToMeterTypeMapping.get(i);
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
     * Z-Wave MeterScale enumeration. The meter scale indicates the meter scale that is reported.
     */
    @XStreamAlias("meterScale")
    public enum MeterScale {
        E_KWh(0, MeterType.ELECTRIC, "kWh", "Energy"),
        E_KVAh(1, MeterType.ELECTRIC, "kVAh", "Energy"),
        E_W(2, MeterType.ELECTRIC, "W", "Power"),
        E_Pulses(3, MeterType.ELECTRIC, "Pulses", "Count"),
        E_V(4, MeterType.ELECTRIC, "V", "Voltage"),
        E_A(5, MeterType.ELECTRIC, "A", "Current"),
        E_Power_Factor(6, MeterType.ELECTRIC, "Power Factor", "Power Factor"),
        E_KVAR(7, MeterType.ELECTRIC, "", ""),
        E_KVARH(8, MeterType.ELECTRIC, "", ""),
        G_Cubic_Meters(0, MeterType.GAS, "Cubic Meters", "Volume"),
        G_Cubic_Feet(1, MeterType.GAS, "Cubic Feet", "Volume"),
        G_Pulses(3, MeterType.GAS, "Pulses", "Count"),
        W_Cubic_Meters(0, MeterType.WATER, "Cubic Meters", "Volume"),
        W_Cubic_Feet(1, MeterType.WATER, "Cubic Feet", "Volume"),
        W_Gallons(2, MeterType.WATER, "US gallons", "Volume"),
        W_Pulses(3, MeterType.WATER, "Pulses", "Count"),
        HEATING_KWH(3, MeterType.HEATING, "kWh", "Energy"),
        COOLING_KWH(3, MeterType.COOLING, "kWh", "Energy");

        private final int scale;
        private final MeterType meterType;
        private final String unit;
        private final String label;

        /**
         * A mapping between the integer code, Meter type and its corresponding Meter scale
         * to facilitate lookup by code.
         */
        private static Map<MeterType, Map<Integer, MeterScale>> codeToMeterScaleMapping;

        /**
         * A mapping between the name,and its corresponding Meter scale to facilitate lookup by enumeration name.
         */
        private static Map<String, MeterScale> nameToMeterScaleMapping;

        /**
         * Constructor. Creates a new enumeration value.
         *
         * @param scale     the scale number
         * @param meterType the meter type
         * @param unit      the unit
         * @param label     the label.
         */
        private MeterScale(int scale, MeterType meterType, String unit, String label) {
            this.scale = scale;
            this.meterType = meterType;
            this.unit = unit;
            this.label = label;
        }

        private static void initMapping() {
            codeToMeterScaleMapping = new HashMap<MeterType, Map<Integer, MeterScale>>();
            nameToMeterScaleMapping = new HashMap<String, MeterScale>();
            for (MeterScale s : values()) {
                if (!codeToMeterScaleMapping.containsKey(s.getMeterType())) {
                    codeToMeterScaleMapping.put(s.getMeterType(), new HashMap<Integer, MeterScale>());
                }
                codeToMeterScaleMapping.get(s.getMeterType()).put(s.getScale(), s);
                nameToMeterScaleMapping.put(s.name().toLowerCase(), s);
            }
        }

        /**
         * Lookup function based on the meter type and code. Returns null if the code does not exist.
         *
         * @param meterType the meter type to use to lookup the scale
         * @param i         the code to lookup
         * @return enumeration value of the meter scale.
         */
        public static MeterScale getMeterScale(MeterType meterType, int i) {
            if (codeToMeterScaleMapping == null) {
                initMapping();
            }
            if (!codeToMeterScaleMapping.containsKey(meterType)) {
                return null;
            }

            return codeToMeterScaleMapping.get(meterType).get(i);
        }

        /**
         * Lookup function based on the name. Returns null if the name does not exist.
         *
         * @param name the name to lookup
         * @return enumeration value of the meter scale.
         */
        public static MeterScale getMeterScale(String name) {
            if (nameToMeterScaleMapping == null) {
                initMapping();
            }

            return nameToMeterScaleMapping.get(name.toLowerCase());
        }

        /**
         * Returns the scale code.
         *
         * @return the scale code.
         */
        protected int getScale() {
            return scale;
        }

        /**
         * Returns the meter type.
         *
         * @return the meterType
         */
        protected MeterType getMeterType() {
            return meterType;
        }

        /**
         * Returns the unit as string.
         *
         * @return the unit
         */
        protected String getUnit() {
            return unit;
        }

        /**
         * Returns the label (category).
         *
         * @return the label
         */
        protected String getLabel() {
            return label;
        }

    }

    /**
     * Z-Wave Meter value Event class. Indicates that a meter value changed.
     */
    public class ZWaveMeterValueEvent extends ZWaveCommandClassValueEvent {

        private final MeterType meterType;
        private final MeterScale meterScale;

        /**
         * Constructor. Creates a instance of the ZWaveMeterValueEvent class.
         *
         * @param nodeId
         *                      the nodeId of the event
         * @param endpoint
         *                      the endpoint of the event.
         * @param meterType
         *                      the meter type that triggered the event;
         * @param meterType
         *                      the meter scale for the event;
         * @param value
         *                      the value for the event.
         */
        public ZWaveMeterValueEvent(int nodeId, int endpoint, MeterType meterType, MeterScale meterScale,
                Object value) {
            super(nodeId, endpoint, CommandClass.COMMAND_CLASS_METER, value);
            this.meterType = meterType;
            this.meterScale = meterScale;
        }

        /**
         * Gets the meter type for this meter value event.
         *
         * @return the meter type for this meter value event.
         */
        public MeterType getMeterType() {
            return meterType;
        }

        /**
         * Gets the meter scale for this meter value event.
         *
         * @return the meter scale for this meter value event.
         */
        public MeterScale getMeterScale() {
            return meterScale;
        }

    }
}
