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
import java.io.IOException;
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
 * Handles the Multi Level Sensor command class. Multi level sensors indicate their states as a value + unit. The
 * commands include the possibility to get a given value and report a value.
 *
 * @auther Chris Jackson
 * @author Ben Jones
 * @author Jan-Willem Spuij
 */
@XStreamAlias("COMMAND_CLASS_SENSOR_MULTILEVEL")
public class ZWaveMultiLevelSensorCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassDynamicState, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiLevelSensorCommandClass.class);
    private static final int MAX_SUPPORTED_VERSION = 10;

    private static final int SENSOR_MULTILEVEL_GET = 0x04;
    private static final int SENSOR_MULTILEVEL_REPORT = 0x05;

    // v5
    private static final int SENSOR_MULTILEVEL_SUPPORTED_GET_SENSOR = 0x01;
    private static final int SENSOR_MULTILEVEL_SUPPORTED_SENSOR_REPORT = 0x02;
    private static final int SENSOR_MULTILEVEL_SUPPORTED_GET_SCALE = 0x03;
    private static final int SENSOR_MULTILEVEL_SUPPORTED_SCALE_REPORT = 0x06;

    private final Map<SensorType, Sensor> sensors = new HashMap<SensorType, Sensor>();

    @XStreamOmitField
    private boolean initialiseDone = false;
    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveMultiLevelSensorCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveMultiLevelSensorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL;
    }

    @ZWaveResponseHandler(id = SENSOR_MULTILEVEL_SUPPORTED_SENSOR_REPORT, name = "SENSOR_MULTILEVEL_SUPPORTED_SENSOR_REPORT")
    public void handleSensorMultilevelSupportedSensorReport(ZWaveCommandClassPayload payload, int endpoint) {
        int payloadLength = payload.getPayloadLength();

        for (int i = 2; i < payloadLength; ++i) {
            for (int bit = 0; bit < 8; ++bit) {
                if (((payload.getPayloadByte(i)) & (1 << bit)) == 0) {
                    continue;
                }

                int index = ((i - 2) * 8) + bit + 1;
                if (index >= SensorType.values().length) {
                    continue;
                }

                // (n)th bit is set. n is the index for the sensor type enumeration.
                SensorType sensorTypeToAdd = SensorType.getSensorType(index);
                Sensor newSensor = new Sensor(sensorTypeToAdd);
                sensors.put(sensorTypeToAdd, newSensor);
                logger.debug("NODE {}: Added sensor type {} ({})", getNode().getNodeId(), sensorTypeToAdd.getLabel(),
                        index);
            }
        }

        initialiseDone = true;
    }

    @ZWaveResponseHandler(id = SENSOR_MULTILEVEL_REPORT, name = "SENSOR_MULTILEVEL_REPORT")
    public void handleSensorMultilevelReport(ZWaveCommandClassPayload payload, int endpoint) {
        int sensorTypeCode = payload.getPayloadByte(2);
        int sensorScale = (payload.getPayloadByte(3) >> 3) & 0x03;

        // Sensor type seems to be supported, add it to the list.
        Sensor sensor = getSensor(sensorTypeCode);
        if (sensor != null) {
            sensor.setInitialised();

            logger.debug("NODE {}: Sensor Type = {}({}), Scale = {}", getNode().getNodeId(),
                    sensor.getSensorType().getLabel(), sensorTypeCode, sensorScale);

            // Set the global flag. This is mainly used for version < 4
            dynamicDone = true;

            try {
                BigDecimal value = extractValue(payload, 3);

                logger.debug("NODE {}: Sensor Value = {}", getNode().getNodeId(), value);

                ZWaveMultiLevelSensorValueEvent zEvent = new ZWaveMultiLevelSensorValueEvent(getNode().getNodeId(),
                        endpoint, sensor.getSensorType(), sensorScale, value);
                getController().notifyEventListeners(zEvent);
            } catch (NumberFormatException e) {
                return;
            }
        }
    }

    private Sensor getSensor(int sensorTypeCode) {
        SensorType sensorType = SensorType.getSensorType(sensorTypeCode);
        if (sensorType == null) {
            logger.debug("NODE {}: Unknown Sensor Type = {}, ignoring report.", getNode().getNodeId(), sensorTypeCode);
            return null;
        }

        // Sensor type seems to be supported, add it to the list.
        Sensor sensor = sensors.get(sensorType);
        if (sensor == null) {
            sensor = new Sensor(sensorType);
            this.sensors.put(sensorType, sensor);
            logger.debug("NODE {}: Adding new sensor Type = {}({})", getNode().getNodeId(), sensorType.getLabel(),
                    sensorTypeCode);
        }

        return sensor;
    }

    /**
     * Gets a SerialMessage with the SENSOR_MULTI_LEVEL_GET command
     *
     * Will only return the first sensor!
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        if (this.getVersion() > 4) {
            for (Map.Entry<SensorType, Sensor> entry : sensors.entrySet()) {
                return this.getMessage(entry.getValue().getSensorType());
            }
        }

        logger.debug("NODE {}: Creating new message for command SENSOR_MULTI_LEVEL_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_MULTILEVEL_GET).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(SENSOR_MULTILEVEL_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the SENSOR_MULTILEVEL_SUPPORTED_GET_SENSOR command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getSupportedSensorMessage() {
        logger.debug("NODE {}: Creating new message for command SENSOR_MULTILEVEL_SUPPORTED_GET",
                getNode().getNodeId());
        if (getVersion() < 5) {
            return null;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_MULTILEVEL_SUPPORTED_GET_SENSOR).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(SENSOR_MULTILEVEL_SUPPORTED_SENSOR_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the SENSOR_MULTILEVEL_SUPPORTED_GET_SCALE command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getSupportedScaleMessage(SensorType sensorType) {
        logger.debug("NODE {}: Creating new message for command SENSOR_MULTILEVEL_SUPPORTED_GET_SCALE",
                getNode().getNodeId());
        if (getVersion() < 5) {
            return null;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_MULTILEVEL_SUPPORTED_GET_SCALE).withPayload(sensorType.getKey())
                        .withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(SENSOR_MULTILEVEL_SUPPORTED_SCALE_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the SENSOR_MULTILEVEL_GET command
     *
     * @param sensorType the {@link SensorType} to get the value for.
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getMessage(SensorType sensorType) {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests for MULTILEVEL_SENSOR",
                    this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command SENSOR_MULTILEVEL_GET", getNode().getNodeId());
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        // Pre v5 does not have a sensortype argument
        if (getVersion() >= 5) {
            outputData.write(sensorType.getKey());
            outputData.write(0); // first scale
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_MULTILEVEL_GET).withPayload(outputData.toByteArray()).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(SENSOR_MULTILEVEL_REPORT).build();
    }

    /**
     * Sends a sensor report
     *
     * @param sensorType
     * @param scale
     * @param value
     * @return
     */
    public ZWaveCommandClassTransactionPayload getReportMessage(SensorType sensorType, int scale, BigDecimal value) {
        logger.debug("NODE {}: Creating new message for command SENSOR_MULTILEVEL_REPORT, Set {} to {} with scale {}",
                getNode().getNodeId(), sensorType, value, scale);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(sensorType.getKey());
        try {
            byte[] encodedValue = encodeValue(value);
            encodedValue[0] |= (scale << 3) & 0x18;
            outputData.write(encodedValue);
        } catch (ArithmeticException | IOException e) {
            logger.debug("Error converting {}. ", value, e);
            return null;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_MULTILEVEL_REPORT).withPayload(outputData.toByteArray()).withPriority(TransactionPriority.Set)
                        .build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        if ((refresh == true || initialiseDone == false) && getVersion() > 4) {
            result.add(getSupportedSensorMessage());
        }

        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        // If we want to refresh, then reset the init flag on all sensors
        if (refresh == true && getVersion() > 4) {
            logger.debug("=========== Resetting init flag!");
            for (Map.Entry<SensorType, Sensor> entry : sensors.entrySet()) {
                entry.getValue().resetInitialised();
            }

            dynamicDone = false;
        }

        if (this.getVersion() > 4) {
            for (Map.Entry<SensorType, Sensor> entry : sensors.entrySet()) {
                if (entry.getValue().getInitialised() == false) {
                    logger.debug("============ Requesting {}!", entry.getValue().getSensorType());
                    result.add(getMessage(entry.getValue().getSensorType()));
                }
            }
        } else if (dynamicDone == false) {
            result.add(this.getValueMessage());
        }

        return result;
    }

    /**
     * Z-Wave SensorType enumeration. The sensor type indicates the type of sensor that is reported.
     */
    @XStreamAlias("multilevelSensorType")
    public enum SensorType {
        TEMPERATURE(1, "Temperature"),
        GENERAL(2, "General"),
        LUMINANCE(3, "Luminance"),
        POWER(4, "Power"),
        RELATIVE_HUMIDITY(5, "RelativeHumidity"),
        VELOCITY(6, "Velocity"),
        DIRECTION(7, "Direction"),
        ATMOSPHERIC_PRESSURE(8, "AtmosphericPressure"),
        BAROMETRIC_PRESSURE(9, "BarometricPressure"),
        SOLAR_RADIATION(10, "SolarRadiation"),
        DEW_POINT(11, "DewPoint"),
        RAIN_RATE(12, "RainRate"),
        TIDE_LEVEL(13, "TideLevel"),
        WEIGHT(14, "Weight"),
        VOLTAGE(15, "Voltage"),
        CURRENT(16, "Current"),
        CO2(17, "CO2"),
        AIR_FLOW(18, "AirFlow"),
        TANK_CAPACITY(19, "TankCapacity"),
        DISTANCE(20, "Distance"),
        ANGLE_POSITION(21, "AnglePosition"),
        ROTATION(22, "Rotation"),
        WATER_TEMPERATURE(23, "WaterTemperature"),
        SOIL_TEMPERATURE(24, "SoilTemperature"),
        SEISMIC_INTENSITY(25, "SeismicIntensity"),
        SEISMIC_MAGNITUDE(26, "SeismicMagnitude"),
        ULTRAVIOLET(27, "Ultraviolet"),
        ELECTRICAL_RESISTIVITY(28, "ElectricalResistivity"),
        ELECTRICAL_CONDUCTIVITY(29, "ElectricalConductivity"),
        LOUDNESS(30, "Loudness"),
        MOISTURE(31, "Moisture"),
        FREQUENCY(32, "Frequency"),
        TIME(33, "Time"),
        TARGET_TEMPERATURE(34, "Target Temperature"),
        PARTICULATE_MATTER(35, "Particulate Matter"),
        FORMALDEHYDE_LEVEL(36, "Formaldehyde Level"),
        RADON_CONCENTRATION(37, "Radon Concentration"),
        METHANE_DENSITY(38, "Methane Density"),
        VOLATILE_ORGANIC_COMPOUND(39, "Volatile Organic Compound"),
        CARBON_MONOXIDE(40, "Carbon Monoxide"),
        SOIL_HUMIDITY(41, "Soil Humidity"),
        SOIL_REACTIVITY(42, "Soil Reactivity"),
        SOIL_SALINITY(43, "Soil Salinity"),
        HEART_RATE(44, "Heart Rate"),
        BLOOD_PRESSURE(45, "Blood Pressure"),
        MUSCLE_MASS(46, "Muscle Mass"),
        FAT_MASS(47, "Fat Mass"),
        BONE_MASS(48, "Bone Mass"),
        TOTAL_BODY_WATER(49, "Total Body Water"),
        BASIC_METABOLIC_RATE(50, "Basic Metabolic Rate"),
        BODY_MASS_INDEX(51, "Body Mass Index"),
        ACCELERATION_X(52, "Accelleration X-Axis"),
        ACCELERATION_Y(53, "Accelleration Y-Axis"),
        ACCELERATION_Z(54, "Accelleration Z-Axis"),
        SMOKE_DENSITY(55, "Smoke Density"),
        WATER_FLOW(56, "Water Flow"),
        WATER_PRESURE(57, "Water Pressure"),
        RF_SIGNAL_STRENGTH(58, "RF Signal Strength"),
        PARTICULATE(59, "Particulate Matter"),
        RESPIRATORY_RATE(60, "Respiratory Rate"),
        MAX_TYPE(61, "MaxType");

        /**
         * A mapping between the integer code and its corresponding Sensor type to facilitate lookup by code.
         */
        private static Map<Integer, SensorType> codeToSensorTypeMapping;

        private int key;
        private String label;

        private SensorType(int key, String label) {
            this.key = key;
            this.label = label;
        }

        private static void initMapping() {
            codeToSensorTypeMapping = new HashMap<Integer, SensorType>();
            for (SensorType s : values()) {
                codeToSensorTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the sensor type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the sensor type.
         */
        public static SensorType getSensorType(int i) {
            if (codeToSensorTypeMapping == null) {
                initMapping();
            }

            return codeToSensorTypeMapping.get(i);
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
     * Class to hold alarm state
     *
     * @author Chris Jackson
     */
    @XStreamAlias("multilevelSensor")
    private static class Sensor {
        SensorType sensorType;
        boolean initialised = false;

        public Sensor(SensorType type) {
            sensorType = type;
        }

        public SensorType getSensorType() {
            return sensorType;
        }

        public void setInitialised() {
            initialised = true;
        }

        public void resetInitialised() {
            initialised = false;
        }

        public boolean getInitialised() {
            return initialised;
        }
    }

    /**
     * Z-Wave Multilevel Sensor Event class. Indicates that an sensor value changed.
     *
     * @author Jan-Willem Spuij
     */
    public class ZWaveMultiLevelSensorValueEvent extends ZWaveCommandClassValueEvent {

        private SensorType sensorType;
        private int scale;

        /**
         * Constructor. Creates a instance of the ZWaveMultiLevelSensorValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param sensorType the sensor type that triggered the event;
         * @param scale the scale for the event
         * @param value the value for the event.
         */
        public ZWaveMultiLevelSensorValueEvent(int nodeId, int endpoint, SensorType sensorType, int scale,
                Object value) {
            super(nodeId, endpoint, CommandClass.COMMAND_CLASS_SENSOR_MULTILEVEL, value);
            this.sensorType = sensorType;
            this.scale = scale;
        }

        /**
         * Gets the sensor type for this sensor value event.
         */
        public SensorType getSensorType() {
            return sensorType;
        }

        /**
         * Gets the scale for this event
         */
        public int getSensorScale() {
            return this.scale;
        }
    }
}
