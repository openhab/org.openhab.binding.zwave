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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
 * Handles the Binary Sensor command class. Binary sensors indicate there status or event as on (0xFF) or off (0x00).
 * The commands include the possibility to get a given value and report a value.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 * @author Jorg de Jong
 */
@XStreamAlias("COMMAND_CLASS_SENSOR_BINARY")
public class ZWaveBinarySensorCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassDynamicState, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveBinarySensorCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 2;

    private static final int SENSOR_BINARY_GET = 2;
    private static final int SENSOR_BINARY_REPORT = 3;

    // version 2
    private static final int SENSOR_BINARY_SUPPORTED_SENSOR_GET = 1;
    private static final int SENSOR_BINARY_SUPPORTED_SENSOR_REPORT = 4;

    @XStreamOmitField
    private boolean initialiseDone = false;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    private Set<SensorType> types = new HashSet<>();

    /**
     * Creates a new instance of the ZWaveBinarySensorCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveBinarySensorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_SENSOR_BINARY;
    }

    @ZWaveResponseHandler(id = SENSOR_BINARY_REPORT, name = "SENSOR_BINARY_REPORT")
    public void handleBinarySensorReport(ZWaveCommandClassPayload payload, int endpoint) {
        int value = payload.getPayloadByte(2);

        SensorType sensorType = SensorType.UNKNOWN;
        if (getVersion() > 1 && payload.getPayloadLength() > 3) {
            logger.debug("Processing Sensor Type {}", payload.getPayloadByte(3));
            // For V2, we have the sensor type after the value
            sensorType = SensorType.getSensorType(payload.getPayloadByte(3));
            logger.debug("Sensor Type is {}", sensorType);
            if (sensorType == null) {
                sensorType = SensorType.UNKNOWN;
            }
        }

        logger.debug("NODE {}: Sensor Binary report, type={}, value={}", getNode().getNodeId(), sensorType.getLabel(),
                value);

        ZWaveBinarySensorValueEvent zEvent = new ZWaveBinarySensorValueEvent(getNode().getNodeId(), endpoint,
                sensorType, value);
        getController().notifyEventListeners(zEvent);

        dynamicDone = true;
    }

    @ZWaveResponseHandler(id = SENSOR_BINARY_SUPPORTED_SENSOR_REPORT, name = "SENSOR_BINARY_SUPPORTED_SENSOR_REPORT")
    public void handleSensorBinarySupportedSensorReport(ZWaveCommandClassPayload payload, int endpoint) {
        int numBytes = payload.getPayloadLength() - 2;

        for (int i = 0; i < numBytes; ++i) {
            for (int bit = 0; bit < 8; ++bit) {
                if (((payload.getPayloadByte(i + 2)) & (1 << bit)) == 0) {
                    continue;
                }

                int index = (i << 3) + bit;
                if (index >= SensorType.values().length) {
                    continue;
                }

                // (n)th bit is set. n is the index for the sensor type enumeration.
                // sensor type seems to be supported, add it to the list if it's not already there.
                types.add(SensorType.getSensorType(index));
                logger.debug("NODE {}: found binary sensor {}", getNode().getNodeId(), SensorType.getSensorType(index));
            }
        }

        initialiseDone = true;
    }

    /**
     * Gets a SerialMessage with the SENSOR_BINARY_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command SENSOR_BINARY_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_BINARY_GET).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(SENSOR_BINARY_REPORT).build();
    }

    public ZWaveCommandClassTransactionPayload getValueMessage(SensorType type) {
        if (getVersion() == 1) {
            logger.debug("NODE {}: Node doesn't support SENSOR_BINARY_GET with SensorType", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command SENSOR_BINARY_GET for {}",
                getNode().getNodeId(), type);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_BINARY_GET).withPayload(type.getKey()).withPriority(TransactionPriority.Get)
                        .withExpectedResponseCommand(SENSOR_BINARY_REPORT).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the SENSOR_BINARY_SUPPORTEDSENSOR_GET command
     *
     * @return the serial message, or null if the supported command is not supported.
     */
    public ZWaveCommandClassTransactionPayload getSupportedMessage() {
        if (getVersion() == 1) {
            logger.debug("NODE {}: SENSOR_BINARY_SUPPORTEDSENSOR_GET not supported for V1", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for command SENSOR_BINARY_SUPPORTEDSENSOR_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                SENSOR_BINARY_SUPPORTED_SENSOR_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(SENSOR_BINARY_SUPPORTED_SENSOR_REPORT).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (refresh == true || initialiseDone == false) {
            if (getVersion() > 1) {
                result.add(getSupportedMessage());
            }
        }
        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        if (refresh == true || dynamicDone == false) {
            if (getVersion() == 1) {
                result.add(getValueMessage());
            } else {
                for (SensorType type : types) {
                    result.add(getValueMessage(type));
                }
            }
        }

        return result;
    }

    /**
     * Return the supported binary sensor types as reported by the device.
     *
     * @return the supported sensor types
     */
    public Set<SensorType> getSupportedTypes() {
        return Collections.unmodifiableSet(types.stream().collect(Collectors.toSet()));
    }

    /**
     * Z-Wave SensorType enumeration. The sensor type indicates the type of sensor that is reported.
     *
     * @author Chris Jackson
     */
    @XStreamAlias("binarySensorType")
    public enum SensorType {
        UNKNOWN(0x00, "Unknown"),
        GENERAL(0x01, "General Purpose"),
        SMOKE(0x02, "Smoke"),
        CO(0x03, "Carbon Monoxide"),
        CO2(0x04, "Carbon Dioxide"),
        HEAT(0x05, "Heat"),
        WATER(0x06, "Water"),
        FREEZE(0x07, "Freeze"),
        TAMPER(0x08, "Tamper"),
        AUX(0x09, "Aux"),
        DOORWINDOW(0x0a, "Door/Window"),
        TILT(0x0b, "Tilt"),
        MOTION(0x0c, "Motion"),
        GLASSBREAK(0x0d, "Glass Break");

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
     * Z-Wave Binary Sensor Event class. Indicates that an sensor value changed.
     *
     * @author Chris Jackson
     */
    public class ZWaveBinarySensorValueEvent extends ZWaveCommandClassValueEvent {

        private SensorType sensorType;

        /**
         * Constructor. Creates a instance of the ZWaveBinarySensorValueEvent class.
         *
         * @param nodeId the nodeId of the event
         * @param endpoint the endpoint of the event.
         * @param sensorType the sensor type that triggered the event;
         * @param value the value for the event.
         */
        private ZWaveBinarySensorValueEvent(int nodeId, int endpoint, SensorType sensorType, Object value) {
            super(nodeId, endpoint, CommandClass.COMMAND_CLASS_SENSOR_BINARY, value);
            this.sensorType = sensorType;
        }

        /**
         * Gets the alarm type for this alarm sensor value event.
         */
        public SensorType getSensorType() {
            return sensorType;
        }

        @Override
        public Integer getValue() {
            return (Integer) super.getValue();
        }
    }
}
