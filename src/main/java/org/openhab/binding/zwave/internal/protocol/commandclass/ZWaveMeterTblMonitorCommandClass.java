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
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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
 * Handles the Meter Tbl Monitor Command command class.
 *
 * @author Jorg de Jong
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_METER_TBL_MONITOR")
public class ZWaveMeterTblMonitorCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveMeterTblMonitorCommandClass.class);

    private static final byte METER_TBL_TABLE_ID_GET = 3;
    private static final byte METER_TBL_TABLE_ID_REPORT = 4;
    private static final byte METER_TBL_TABLE_CAPABILITY_GET = 5;
    private static final byte METER_TBL_REPORT = 6;
    private static final byte METER_TBL_CURRENT_DATA_GET = 12;
    private static final byte METER_TBL_CURRENT_DATA_REPORT = 13;

    // unsuported private static final byte METER_TBL_STATUS_REPORT = 11;
    // unsuported private static final byte METER_TBL_STATUS_DATE_GET = 10;
    // unsuported private static final byte METER_TBL_STATUS_DEPTH_GET = 9;
    // unsuported private static final byte METER_TBL_STATUS_SUPPORTED_GET = 7;
    // unsuported private static final byte METER_TBL_STATUS_SUPPORTED_REPORT = 8;
    // unsuported private static final byte METER_TBL_HISTORICAL_DATA_GET = 14;
    // unsuported private static final byte METER_TBL_HISTORICAL_DATA_REPORT = 15;
    // unsuported private static final byte METER_TBL_TABLE_POINT_ADM_NO_GET = 1;
    // unsuported private static final byte METER_TBL_TABLE_POINT_ADM_NO_REPORT = 2;

    @XStreamOmitField
    private boolean initialiseDone = false;
    @XStreamOmitField
    private boolean dynamicDone = false;

    private MeterTblMonitorType meterType;
    private String tableName;
    private int rateType;
    private int dataset;

    /**
     * Creates a new instance of the ZWaveMeterTblMonitorCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveMeterTblMonitorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_METER_TBL_MONITOR;
    }

    @ZWaveResponseHandler(id = METER_TBL_TABLE_ID_REPORT, name = "METER_TBL_TABLE_ID_REPORT")
    public void handleTableIdReport(ZWaveCommandClassPayload payload, int endpoint) {
        int numBytes = payload.getPayloadByte(2) & 0x1F;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Check for null terminations - ignore anything after the first null
        for (int c = 0; c < numBytes; c++) {
            if (payload.getPayloadByte(c + 3) > 32 && payload.getPayloadByte(c + 3) < 127) {
                baos.write((byte) (payload.getPayloadByte(c + 3)));
            }
        }
        String name;

        try {
            name = new String(baos.toByteArray(), "ASCII");
        } catch (UnsupportedEncodingException e) {
            name = "unsupported";
        }
        tableName = name;
        logger.debug("NODE {}: Table name: {}", getNode().getNodeId(), name);
    }

    @ZWaveResponseHandler(id = METER_TBL_REPORT, name = "METER_TBL_REPORT")
    public void handleTableCurrentDataReport(ZWaveCommandClassPayload payload, int endpoint) {
        int meterType = payload.getPayloadByte(2) & 0x3F;
        int rateType = (payload.getPayloadByte(2) & 0xC0) >> 6;
        int properties = payload.getPayloadByte(3);
        int datasetSupported = extractValue(payload.getPayloadBuffer(), 4, 3);
        int datasetSupportedHistory = extractValue(payload.getPayloadBuffer(), 7, 3);
        int dataSupportedHistory = extractValue(payload.getPayloadBuffer(), 10, 3);

        logger.debug("NODE {}: meterType              : {} {}", getNode().getNodeId(), meterType,
                MeterTblMonitorType.getMeterType(meterType));
        logger.debug("NODE {}: rateType               : {}", getNode().getNodeId(), rateType);
        logger.debug("NODE {}: properties             : {}", getNode().getNodeId(), properties);
        logger.debug("NODE {}: datasetSupported       : {}", getNode().getNodeId(), datasetSupported);
        logger.debug("NODE {}: datasetSupportedHistory: {}", getNode().getNodeId(), datasetSupportedHistory);
        logger.debug("NODE {}: dataSupportedHistory   : {}", getNode().getNodeId(), dataSupportedHistory);

        this.meterType = MeterTblMonitorType.getMeterType(meterType);
        this.rateType = rateType;
        this.dataset = datasetSupported;

        initialiseDone = true;
    }

    @ZWaveResponseHandler(id = METER_TBL_CURRENT_DATA_REPORT, name = "METER_TBL_CURRENT_DATA_REPORT")
    public void handleTableDataReport(ZWaveCommandClassPayload payload, int endpoint) {
        int numReports = payload.getPayloadByte(2);
        int rateType = payload.getPayloadByte(3) & 0x03;
        boolean operatingStatus = (payload.getPayloadByte(3) & 0x80) > 0;

        int dataset = extractValue(payload.getPayloadBuffer(), 4, 3);
        int year = extractValue(payload.getPayloadBuffer(), 7, 2);
        int month = payload.getPayloadByte(9);
        int day = payload.getPayloadByte(10);
        int hour = payload.getPayloadByte(11);
        int minutes = payload.getPayloadByte(12);
        int seconds = payload.getPayloadByte(13);

        int scaleIndex = payload.getPayloadByte(14) & 0x1F;
        int precision = (payload.getPayloadByte(14) & 0xE0) >> 5;

        int valueRaw = extractValue(payload.getPayloadBuffer(), 15, 4);

        logger.trace("NODE {}: numReports:{}", getNode().getNodeId(), numReports);
        logger.trace("NODE {}: rateType  :{}", getNode().getNodeId(), rateType);
        logger.trace("NODE {}: operating :{}", getNode().getNodeId(), operatingStatus);
        logger.trace("NODE {}: dataset   :{}", getNode().getNodeId(), dataset);
        logger.trace("NODE {}: time      :{}-{}-{} {}:{}:{}", this.getNode().getNodeId(), year, month, day, hour,
                minutes, seconds);

        logger.trace("NODE {}: scale     :{}", getNode().getNodeId(), scaleIndex);
        logger.trace("NODE {}: presision :{}", getNode().getNodeId(), precision);
        logger.trace("NODE {}: value     :{}", getNode().getNodeId(), valueRaw);

        MeterTblMonitorScale scale = MeterTblMonitorScale.getMeterScale(meterType, scaleIndex);
        if (scale == null) {
            logger.warn("NODE {}: Invalid meter scale {}", getNode().getNodeId(), scaleIndex);
            return;
        }

        try {
            BigDecimal value = new BigDecimal(valueRaw / Math.pow(10, precision)).setScale(precision,
                    BigDecimal.ROUND_HALF_UP);
            logger.debug("NODE {}: Meter Tbl Monitor: Type={}, Scale={}({}), Value={}, Dataset={}",
                    getNode().getNodeId(), meterType.getLabel(), scale.getUnit(), scale.getScale(), value, dataset);

            ZWaveMeterTblMonitorValueEvent zEvent = new ZWaveMeterTblMonitorValueEvent(getNode().getNodeId(), endpoint,
                    meterType, scale, value);
            getController().notifyEventListeners(zEvent);
        } catch (NumberFormatException e) {
            logger.debug("NODE {}: Meter Tbl Monitor Value Error {}", getNode().getNodeId(), e);
            return;
        }

        dynamicDone = true;
    }

    /**
     * Gets a SerialMessage with the METER_TBL_CURRENT_DATA_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getCurrentData(int dataset) {
        logger.debug("NODE {}: Creating new message for application command METER_TBL_CURRENT_DATA_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                METER_TBL_CURRENT_DATA_GET).withPayload(dataset >> 16, dataset >> 8, dataset)
                        .withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(METER_TBL_CURRENT_DATA_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the METER_TBL_TABLE_CAPABILITY_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getCapabilityGet() {
        logger.debug("NODE {}: Creating new message for application command METER_TBL_TABLE_CAPABILITY_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                METER_TBL_TABLE_CAPABILITY_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(METER_TBL_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the METER_TBL_TABLE_ID_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getTableIDGet() {
        logger.debug("NODE {}: Creating new message for application command METER_TBL_TABLE_ID_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                METER_TBL_TABLE_ID_GET).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(METER_TBL_TABLE_ID_REPORT).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(getCapabilityGet());
            result.add(getTableIDGet());
        }
        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        if (refresh == true || dynamicDone == false) {
            result.add(getCurrentData(dataset));
        }

        return result;
    }

    /**
     * Z-Wave MeterTblMonitorType enumeration. The meter type indicates the type of meter that is reported.
     *
     */
    public enum MeterTblMonitorType {
        UNKNOWN(0, "Unknown"),
        ELECTRIC(1, "Electric"),
        GAS(2, "Gas"),
        WATER(3, "Water"),
        TWIN_ELECTRIC(4, "Twin Electric");

        /**
         * A mapping between the integer code and its corresponding Meter type
         * to facilitate lookup by code.
         */
        private static Map<Integer, MeterTblMonitorType> codeToMeterTypeMapping;

        private int key;
        private String label;

        private MeterTblMonitorType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToMeterTypeMapping = new HashMap<Integer, MeterTblMonitorType>();
            for (MeterTblMonitorType s : values()) {
                codeToMeterTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the meter type code. Returns null if the
         * code does not exist.
         *
         * @param i
         *            the code to lookup
         * @return enumeration value of the meter type.
         */
        public static MeterTblMonitorType getMeterType(int i) {
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
     *
     */
    @XStreamAlias("meterTblMonitorScale")
    public enum MeterTblMonitorScale {
        E_KWh(0, MeterTblMonitorType.ELECTRIC, "kWh", "Energy"),
        E_KVAh(1, MeterTblMonitorType.ELECTRIC, "kVAh", "Energy"),
        E_W(2, MeterTblMonitorType.ELECTRIC, "W", "Power"),
        E_Pulses(3, MeterTblMonitorType.ELECTRIC, "Pulses", "Count"),
        E_V(4, MeterTblMonitorType.ELECTRIC, "V", "Voltage"),
        E_A(5, MeterTblMonitorType.ELECTRIC, "A", "Current"),
        E_Power_Factor(6, MeterTblMonitorType.ELECTRIC, "Power Factor", "Power Factor"),
        G_Cubic_Meters(0, MeterTblMonitorType.GAS, "Cubic Meters", "Volume"),
        G_Cubic_Feet(1, MeterTblMonitorType.GAS, "Cubic Feet", "Volume"),
        G_Pulses(3, MeterTblMonitorType.GAS, "Pulses", "Count"),
        W_Cubic_Meters(0, MeterTblMonitorType.WATER, "Cubic Meters", "Volume"),
        W_Cubic_Feet(1, MeterTblMonitorType.WATER, "Cubic Feet", "Volume"),
        W_Gallons(2, MeterTblMonitorType.WATER, "US gallons", "Volume"),
        W_Pulses(3, MeterTblMonitorType.WATER, "Pulses", "Count"),
        TE_KWh(0, MeterTblMonitorType.TWIN_ELECTRIC, "kWh", "Energy"),
        TE_KVAh(1, MeterTblMonitorType.TWIN_ELECTRIC, "kVAh", "Energy"),
        TE_W(2, MeterTblMonitorType.TWIN_ELECTRIC, "W", "Power"),
        TE_Pulses(3, MeterTblMonitorType.TWIN_ELECTRIC, "Pulses", "Count"),
        TE_V(4, MeterTblMonitorType.TWIN_ELECTRIC, "V", "Voltage"),
        TE_A(5, MeterTblMonitorType.TWIN_ELECTRIC, "A", "Current");

        private final int scale;
        private final MeterTblMonitorType meterType;
        private final String unit;
        private final String label;

        /**
         * A mapping between the integer code, Meter type and its corresponding Meter scale
         * to facilitate lookup by code.
         */
        private static Map<MeterTblMonitorType, Map<Integer, MeterTblMonitorScale>> codeToMeterScaleMapping;

        /**
         * A mapping between the name,and its corresponding Meter scale to facilitate lookup by enumeration name.
         */
        private static Map<String, MeterTblMonitorScale> nameToMeterScaleMapping;

        /**
         * Constructor. Creates a new enumeration value.
         *
         * @param scale the scale number
         * @param meterType the meter type
         * @param unit the unit
         * @param label the label.
         */
        private MeterTblMonitorScale(int scale, MeterTblMonitorType meterType, String unit, String label) {
            this.scale = scale;
            this.meterType = meterType;
            this.unit = unit;
            this.label = label;
        }

        private static void initMapping() {
            codeToMeterScaleMapping = new HashMap<MeterTblMonitorType, Map<Integer, MeterTblMonitorScale>>();
            nameToMeterScaleMapping = new HashMap<String, MeterTblMonitorScale>();
            for (MeterTblMonitorScale s : values()) {
                if (!codeToMeterScaleMapping.containsKey(s.getMeterType())) {
                    codeToMeterScaleMapping.put(s.getMeterType(), new HashMap<Integer, MeterTblMonitorScale>());
                }
                codeToMeterScaleMapping.get(s.getMeterType()).put(s.getScale(), s);
                nameToMeterScaleMapping.put(s.name().toLowerCase(), s);
            }
        }

        /**
         * Lookup function based on the meter type and code. Returns null if the code does not exist.
         *
         * @param meterType the meter type to use to lookup the scale
         * @param i the code to lookup
         * @return enumeration value of the meter scale.
         */
        public static MeterTblMonitorScale getMeterScale(MeterTblMonitorType meterType, int i) {
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
        public static MeterTblMonitorScale getMeterScale(String name) {
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
        protected MeterTblMonitorType getMeterType() {
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
     * Z-Wave Meter tbl monitor value Event class. Indicates that a meter value changed.
     *
     */
    public class ZWaveMeterTblMonitorValueEvent extends ZWaveCommandClassValueEvent {

        private MeterTblMonitorType meterType;
        private MeterTblMonitorScale meterScale;

        /**
         * Constructor. Creates a instance of the ZWaveMeterTblMonitorValueEvent class.
         *
         * @param nodeId
         *            the nodeId of the event
         * @param endpoint
         *            the endpoint of the event.
         * @param meterType
         *            the meter type that triggered the event;
         * @param meterType
         *            the meter scale for the event;
         * @param value
         *            the value for the event.
         */
        public ZWaveMeterTblMonitorValueEvent(int nodeId, int endpoint, MeterTblMonitorType meterType,
                MeterTblMonitorScale meterScale, Object value) {
            super(nodeId, endpoint, CommandClass.COMMAND_CLASS_METER_TBL_MONITOR, value);
            this.meterType = meterType;
            this.meterScale = meterScale;
        }

        /**
         * Gets the meter type for this meter tbl monitor value event.
         *
         * @return the meter type for this meter tbl monitor value event.
         */
        public MeterTblMonitorType getMeterType() {
            return meterType;
        }

        /**
         * Gets the meter scale for this meter tbl monitor value event.
         *
         * @return the meter scale for this meter tbl monitor value event.
         */
        public MeterTblMonitorScale getMeterScale() {
            return meterScale;
        }

    }
}
