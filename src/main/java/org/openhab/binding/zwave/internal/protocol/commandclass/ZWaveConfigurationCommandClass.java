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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveConfigurationParameter;
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
 * Handles the Configuration command class. This allows reading and writing of node configuration parameters
 *
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_CONFIGURATION")
public class ZWaveConfigurationCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveConfigurationCommandClass.class);

    private static final int CONFIGURATION_SET = 0x04;
    private static final int CONFIGURATION_GET = 0x05;
    private static final int CONFIGURATION_REPORT = 0x06;

    // Stores the list of configuration parameters. These are used for persistence of values and restore.
    private Map<Integer, ZWaveConfigurationParameter> configParameters = new HashMap<Integer, ZWaveConfigurationParameter>();

    /**
     * Creates a new instance of the ZWaveConfigurationCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveConfigurationCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_CONFIGURATION;
    }

    /**
     * Processes a CONFIGURATIONCMD_REPORT message.
     *
     * @param serialMessage
     *            the incoming message to process.
     * @param offset
     *            the offset position from which to start message processing.
     * @param endpoint
     *            the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = CONFIGURATION_REPORT, name = "CONFIGURATIONCMD_REPORT")
    public void handleConfigurationReport(ZWaveCommandClassPayload payload, int endpoint) {
        // Extract the parameter index and value
        int parameter = payload.getPayloadByte(2);
        int size = payload.getPayloadByte(3);

        // ZWave plus devices seem to return 0 if we request a parameter that doesn't exist
        if (size == 0) {
            logger.debug("NODE {}: Parameter {} response has 0 length", getNode().getNodeId(), parameter);
            return;
        }

        // Recover the data
        try {
            int value = extractValue(payload.getPayloadBuffer(), 4, size);

            logger.debug("NODE {}: Node configuration report, parameter = {}, value = {}, size = {}",
                    getNode().getNodeId(), parameter, value, size);

            ZWaveConfigurationParameter configurationParameter;

            // Check if the parameter exists in our list
            configurationParameter = configParameters.get(parameter);
            if (configurationParameter == null) {
                configurationParameter = new ZWaveConfigurationParameter(parameter, value, size);
            } else {
                configurationParameter.setValue(value);
            }

            configParameters.put(parameter, configurationParameter);

            ZWaveConfigurationParameterEvent zEvent = new ZWaveConfigurationParameterEvent(getNode().getNodeId(),
                    configurationParameter);
            getController().notifyEventListeners(zEvent);
        } catch (NumberFormatException e) {
            return;
        }
    }

    /**
     * Gets a SerialMessage with the CONFIGURATIONCMD_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getConfigMessage(int parameter) {
        // Check if the parameter exists in our list
        ZWaveConfigurationParameter configurationParameter = configParameters.get(parameter);
        if (configurationParameter != null && configurationParameter.getWriteOnly() == true) {
            logger.debug("NODE {}: CONFIGURATION_GET ignored for parameter {} - parameter is write only",
                    getNode().getNodeId(), parameter);
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command CONFIGURATIONCMD_GET",
                getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                CONFIGURATION_GET).withPayload(parameter).withExpectedResponseCommand(CONFIGURATION_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the CONFIGURATIONCMD_SET command
     *
     * @param parameter the parameter to set.
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload setConfigMessage(ZWaveConfigurationParameter parameter) {
        if (parameter != null && parameter.getReadOnly() == true) {
            logger.debug("NODE {}: CONFIGURATION_SET ignored for parameter {} - parameter is read only",
                    getNode().getNodeId(), parameter);
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command CONFIGURATION_SET",
                this.getNode().getNodeId());

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write(parameter.getIndex());
        outputData.write(parameter.getSize());

        for (int i = 0; i < parameter.getSize(); i++) {
            outputData.write(parameter.getValue() >> ((parameter.getSize() - i - 1) * 8));
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                CONFIGURATION_SET).withPayload(outputData.toByteArray()).withPriority(TransactionPriority.Config)
                        .build();
    }

    /**
     * Gets the stored parameter.
     *
     * @param index the parameter to get.
     * @return the stored parameter value;
     */
    public ZWaveConfigurationParameter getParameter(Integer index) {
        return configParameters.get(index);
    }

    /**
     * Gets all parameters
     *
     * @return
     */
    public Map<Integer, ZWaveConfigurationParameter> getParameters() {
        return Collections.unmodifiableMap(configParameters);
    }

    /**
     * Sets a parameter as Read Only.
     * Some parameters in some devices can not be written to. Trying to write them results in a timeout and this should
     * be avoided.
     *
     * @param index the parameter index
     * @param readOnly true if the parameter can not be read
     */
    public void setParameterReadOnly(Integer index, boolean readOnly) {
        ZWaveConfigurationParameter configurationParameter;

        // Check if the parameter exists in our list
        configurationParameter = configParameters.get(index);
        if (configurationParameter == null) {
            configurationParameter = new ZWaveConfigurationParameter(index, 0, 1);
            configParameters.put(index, configurationParameter);
        }

        configurationParameter.setReadOnly(readOnly);
    }

    /**
     * Sets a parameter as Write Only.
     * Some parameters in some devices can not be read. Trying to read them results in a timeout and this should be
     * avoided.
     *
     * @param index the parameter index
     * @param size the parameter size
     * @param writeOnly true if the parameter can not be read
     */
    public void setParameterWriteOnly(Integer index, Integer size, boolean writeOnly) {
        ZWaveConfigurationParameter configurationParameter;

        // Check if the parameter exists in our list
        configurationParameter = configParameters.get(index);
        if (configurationParameter == null) {
            configurationParameter = new ZWaveConfigurationParameter(index, 0, 1);
            configParameters.put(index, configurationParameter);
        }

        configurationParameter.setWriteOnly(writeOnly);
    }

    /**
     * ZWave configuration parameter received event.
     * Sent from the Configuration Command Class to the binding when a configuration value is received.
     *
     * @author Chris Jackson
     */
    public class ZWaveConfigurationParameterEvent extends ZWaveCommandClassValueEvent {

        /**
         * Constructor. Creates a new instance of the ZWaveConfigurationParameterEvent class.
         *
         * @param nodeId the nodeId of the event. Must be set to the controller node.
         */
        public ZWaveConfigurationParameterEvent(int nodeId, ZWaveConfigurationParameter parameter) {
            super(nodeId, 0, CommandClass.COMMAND_CLASS_CONFIGURATION, parameter);
        }

        /**
         * Returns the {@link ZWaveConfigurationParameter} that was received as event.
         *
         * @return the configuration parameter.
         */
        public ZWaveConfigurationParameter getParameter() {
            return (ZWaveConfigurationParameter) getValue();
        }
    }
}
