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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
 * Handles the Indicator command class.
 * The indicator command class operates the indicator on the physical device if available.
 * This can be used to identify a device or use the indicator for special purposes.
 * Example is the Evolve LCD panel that uses the Indicator class to toggle the labels
 * displayed on the LCD. The Indicator class is also used to sync multiple panels' labels
 *
 * @author Chris Jackson
 * @author Pedro Paixao
 */
@XStreamAlias("COMMAND_CLASS_INDICATOR")
public class ZWaveIndicatorCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassDynamicState, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveIndicatorCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 3;

    private static final int INDICATOR_SET = 0x01;
    private static final int INDICATOR_GET = 0x02;
    private static final int INDICATOR_REPORT = 0x03;
    private static final int INDICATOR_SUPPORTED_GET = 0x04;
    private static final int INDICATOR_SUPPORTED_REPORT = 0x05;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    private boolean supportedIndicatorsInitialised = false;

    List<ZWaveIndicator> supportedIndicators = new ArrayList<ZWaveIndicator>();

    /**
     * Creates a new instance of the ZWaveIndicatorCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveIndicatorCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_INDICATOR;
    }

    /**
     * Processes a INDICATOR_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = INDICATOR_REPORT, name = "INDICATOR_REPORT")
    public void handleIndicatorReport(ZWaveCommandClassPayload payload, int endpoint) {
        List<ZWaveIndicator> indicators = new ArrayList<ZWaveIndicator>();
        ZWaveIndicator indicator = new ZWaveIndicator();
        indicator.type = IndicatorType.NA;
        indicator.property = null;
        indicator.value = payload.getPayloadByte(2);
        indicators.add(indicator);

        if (getVersion() >= 2) {
            int indicatorCnt = payload.getPayloadByte(3) & 0x1F;
            int offset = 3;
            for (int cnt = 0; cnt < indicatorCnt; cnt++) {
                indicator = new ZWaveIndicator();
                indicator.type = IndicatorType.getIndicatorType(payload.getPayloadByte(offset++));
                indicator.property = IndicatorProperty.getIndicatorProperty(payload.getPayloadByte(offset++));
                indicator.value = payload.getPayloadByte(offset++);
                indicators.add(indicator);
            }
        }

        logger.debug("NODE {}: Indicator report={}", getNode().getNodeId(), indicators);

        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), indicators);

        getController().notifyEventListeners(zEvent);
    }

    /**
     * Processes a INDICATOR_SUPPORTED_REPORT message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = INDICATOR_SUPPORTED_REPORT, name = "INDICATOR_SUPPORTED_REPORT")
    public void handleIndicatorSupportedReport(ZWaveCommandClassPayload payload, int endpoint) {
        IndicatorType type = IndicatorType.getIndicatorType(payload.getPayloadByte(2));
        int properties = payload.getPayloadByte(4) & 0x1F;

        for (int cnt = 0; cnt < properties; cnt++) {
            ZWaveIndicator indicator = new ZWaveIndicator();
            indicator.type = type;
            indicator.property = IndicatorProperty.getIndicatorProperty(payload.getPayloadByte(5 + cnt));

            supportedIndicators.add(indicator);
        }

        if (payload.getPayloadByte(3) == 0) {
            logger.debug("NODE {}: Indicator supported initialisation complete", getNode().getNodeId());
            supportedIndicatorsInitialised = true;
        } else {
            ZWaveCommandClassTransactionPayload outputMessage = getSupportedIndicators(
                    IndicatorType.getIndicatorType(payload.getPayloadByte(3)));
            if (outputMessage != null) {
                getNode().sendMessage(getNode().encapsulate(outputMessage, getEndpoint().getEndpointId()));
            }
        }
    }

    /**
     * Gets a SerialMessage with the INDICATOR_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command INDICATOR_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), INDICATOR_GET)
                .withExpectedResponseCommand(INDICATOR_REPORT).withPriority(TransactionPriority.Get).build();
    }

    /**
     * Gets a SerialMessage with the INDICATOR_SUPPORTED_GET command
     *
     * @param type the indicator to get the list of supported properties. Set to null to get the list of indicator types
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getSupportedIndicators(IndicatorType type) {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command INDICATOR_SUPPORTED_GET for {}",
                getNode().getNodeId(), type);

        int value = 0;
        if (type != null) {
            value = type.getKey();
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                INDICATOR_SUPPORTED_GET).withPayload(value).withExpectedResponseCommand(INDICATOR_SUPPORTED_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * Gets a SerialMessage with the INDICATOR SET command (V1)
     *
     * @param the level to set.
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload setValueMessage(int newIndicator) {
        logger.debug("NODE {}: Creating new message for application command INDICATOR_SET {}", getNode().getNodeId(),
                newIndicator);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), INDICATOR_SET)
                .withPayload(newIndicator).withPriority(TransactionPriority.Set).build();
    }

    /**
     * Gets a SerialMessage with the INDICATOR SET command (V2 / V3)
     *
     * @param the level to set.
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload setValueMessage(List<ZWaveIndicator> indicators) {
        logger.debug("NODE {}: Creating new message for application command INDICATOR_SET {}", getNode().getNodeId(),
                indicators);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(0);
        outputData.write(indicators.size());

        for (ZWaveIndicator indicator : indicators) {
            outputData.write(indicator.type.getKey());
            outputData.write(indicator.property.getKey());
            outputData.write(indicator.value);
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), INDICATOR_SET)
                .withPayload(outputData.toByteArray()).withPriority(TransactionPriority.Set).build();
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        if (refresh == true) {
            dynamicDone = false;
        }

        if (dynamicDone == true) {
            return null;
        }

        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();
        result.add(getValueMessage());
        return result;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        if (refresh == true) {
            supportedIndicatorsInitialised = false;
        }

        if (getVersion() >= 2) {
            supportedIndicators.clear();
            if (supportedIndicatorsInitialised == false) {
                result.add(getSupportedIndicators(null));
            }
        } else {
            supportedIndicatorsInitialised = true;
        }
        return result;
    }

    @XStreamAlias("indicatorType")
    public enum IndicatorType {
        NA(0x00),
        ARMED(0x01),
        NOT_ARMED(0x02),
        READY(0x03),
        FAULT(0x04),
        BUSY(0x05),
        ENTER_ID(0x06),
        ENTER_PIN(0x07),
        OK(0x08),
        NOT_OK(0x09),
        ZONE1_ARMED(0x20),
        ZONE2_ARMED(0x21),
        ZONE3_ARMED(0x22),
        ZONE4_ARMED(0x23),
        ZONE5_ARMED(0x24),
        ZONE6_ARMED(0x25),
        LCD_BACKLIGHT(0x30),
        BUTTON_BACKLIGHT_LETTERS(0x40),
        BUTTON_BACKLIGHT_DIGITS(0x41),
        BUTTON_BACKLIGHT_COMMAND(0x42),
        BUTTON1_INDICATION(0x43),
        BUTTON2_INDICATION(0x44),
        BUTTON3_INDICATION(0x45),
        BUTTON4_INDICATION(0x46),
        BUTTON5_INDICATION(0x47),
        BUTTON6_INDICATION(0x48),
        BUTTON7_INDICATION(0x49),
        BUTTON8_INDICATION(0x4A),
        BUTTON9_INDICATION(0x4B),
        BUTTON10_INDICATION(0x4C),
        BUTTON11_INDICATION(0x4D),
        BUTTON12_INDICATION(0x4E),
        NODE_IDENTIFY(0x50),
        BUZZER(0xF0);

        /**
         * A mapping between the integer code and its corresponding type to facilitate lookup by code.
         */
        private static Map<Integer, IndicatorType> codeToTypeMapping;

        private int key;

        private IndicatorType(int key) {
            this.key = key;
        }

        private static void initMapping() {
            codeToTypeMapping = new HashMap<Integer, IndicatorType>();
            for (IndicatorType s : values()) {
                codeToTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the alarm type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the alarm type.
         */
        public static IndicatorType getIndicatorType(int i) {
            if (codeToTypeMapping == null) {
                initMapping();
            }

            return codeToTypeMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }
    }

    @XStreamAlias("ZWaveIndicatorProperty")
    public enum IndicatorProperty {
        MULTILEVEL(0x01),
        BINARY(0x02),
        ON_OFF_PERIOD(0x03),
        ON_OFF_CYCLES(0x04),
        ON_TIME_WITH_OFF_PERIOD(0x05),
        INDICATOR6(0x06),
        INDICATOR7(0x07),
        LOW_POWER(0x10);

        /**
         * A mapping between the integer code and its corresponding type to facilitate lookup by code.
         */
        private static Map<Integer, IndicatorProperty> codeToTypeMapping;

        private int key;

        private IndicatorProperty(int key) {
            this.key = key;
        }

        private static void initMapping() {
            codeToTypeMapping = new HashMap<Integer, IndicatorProperty>();
            for (IndicatorProperty s : values()) {
                codeToTypeMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the alarm type code.
         * Returns null if the code does not exist.
         *
         * @param i the code to lookup
         * @return enumeration value of the alarm type.
         */
        public static IndicatorProperty getIndicatorProperty(int i) {
            if (codeToTypeMapping == null) {
                initMapping();
            }

            return codeToTypeMapping.get(i);
        }

        /**
         * @return the key
         */
        public int getKey() {
            return key;
        }
    }

    @XStreamAlias("ZWaveIndicator")
    public static class ZWaveIndicator {
        public IndicatorType type;
        public IndicatorProperty property;
        public Integer value;

        @Override
        public String toString() {
            return "ZWaveIndicator [type=" + type + ", property=" + property + ", value=" + value + "]";
        }

    }
}
