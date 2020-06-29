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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Multi Instance / Multi Channel command class. The Multi Instance command class is used to control
 * multiple instances of the same device class on the node.
 * <p>
 * Multi Channel support (version 2) of the command class can also handle multiple instances of different command
 * classes. The instances are called endpoints in this version.
 *
 * Useful references -:
 * https://groups.google.com/d/msg/openzwave/FeFNBI8GAKk/dyXAO54BiqgJ
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 * @author Michiel Leegwater
 */
@XStreamAlias("COMMAND_CLASS_MULTI_CHANNEL")
public class ZWaveMultiInstanceCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiInstanceCommandClass.class);

    private static final int MAX_SUPPORTED_VERSION = 2;

    // Version 1
    private static final int MULTI_INSTANCE_GET = 4;
    private static final int MULTI_INSTANCE_REPORT = 5;
    private static final int MULTI_INSTANCE_ENCAP = 6;

    // Version 2
    private static final int MULTI_CHANNEL_ENDPOINT_GET = 7;
    private static final int MULTI_CHANNEL_ENDPOINT_REPORT = 8;
    private static final int MULTI_CHANNEL_CAPABILITY_GET = 9;
    private static final int MULTI_CHANNEL_CAPABILITY_REPORT = 10;
    private static final int MULTI_CHANNEL_ENDPOINT_FIND = 11;
    private static final int MULTI_CHANNEL_ENDPOINT_FIND_REPORT = 12;
    private static final int MULTI_CHANNEL_ENCAP = 13;

    // private final Map<Integer, ZWaveEndpoint> endpoints = new HashMap<Integer, ZWaveEndpoint>();

    private boolean useDestEndpointAsSource = false;
    private boolean endpointsAreTheSameDeviceClass;

    // List of classes that DO NOT support multiple instances.
    // This is used to reduce the number of requests during initialisation.
    // Only add a class to this list if you are sure it doesn't support multiple instances!
    @XStreamOmitField
    private static final List<CommandClass> singleInstanceClasses = Arrays.asList(
            CommandClass.COMMAND_CLASS_NO_OPERATION, CommandClass.COMMAND_CLASS_CONFIGURATION,
            CommandClass.COMMAND_CLASS_TIME, CommandClass.COMMAND_CLASS_TIME_PARAMETERS,
            CommandClass.COMMAND_CLASS_CLOCK, CommandClass.COMMAND_CLASS_WAKE_UP, CommandClass.COMMAND_CLASS_BATTERY);

    /**
     * Creates a new instance of the ZWaveMultiInstanceCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveMultiInstanceCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        versionMax = MAX_SUPPORTED_VERSION;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_MULTI_CHANNEL;
    }

    /**
     * Handles Multi Instance Report message. Handles Report on
     * the number of instances for the command class.
     * This is for Version 1 of the command class.
     *
     * @param payload
     * @param endpoint
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = MULTI_INSTANCE_REPORT, name = "MULTI_INSTANCE_REPORT")
    public void handleMultiInstanceReportResponse(ZWaveCommandClassPayload payload, int endpoint) {
        int commandClassCode = payload.getPayloadByte(2);
        int instances = payload.getPayloadByte(3);

        CommandClass commandClass = CommandClass.getCommandClass(commandClassCode);
        if (commandClass == null) {
            logger.error("NODE {}: Unsupported command class 0x{}", getNode().getNodeId(),
                    Integer.toHexString(commandClassCode));
            return;
        }

        logger.debug("NODE {}: Requested Command Class = {}", getNode().getNodeId(), commandClass);

        ZWaveCommandClass zwaveCommandClass = getNode().getCommandClass(commandClass);
        if (zwaveCommandClass == null) {
            logger.error("NODE {}: Unsupported command class {} (0x{})", getNode().getNodeId(), commandClass,
                    Integer.toHexString(commandClassCode));
            return;
        }

        if (instances == 0) {
            logger.debug("NODE {}: Instances = 0. Setting to 1.", getNode().getNodeId());
            instances = 1;
        }

        zwaveCommandClass.setInstances(instances);
        logger.debug("NODE {}: Command class {}, has {} instance(s).", getNode().getNodeId(), commandClass, instances);
    }

    /**
     * Handles Multi Channel Endpoint Report message. Handles Report on the number of endpoints and whether they are
     * dynamic and/or have the same command classes.
     * This is for Version 2 of the command class.
     *
     * @param payload
     * @param endpoint
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = MULTI_CHANNEL_ENDPOINT_REPORT, name = "MULTI_CHANNEL_ENDPOINT_REPORT")
    public void handleMultiChannelEndpointReport(ZWaveCommandClassPayload payload, int endpoint) {
        boolean changingNumberOfEndpoints = (payload.getPayloadByte(2) & 0x80) != 0;
        endpointsAreTheSameDeviceClass = (payload.getPayloadByte(2) & 0x40) != 0;
        int endpointsSupported = payload.getPayloadByte(3) & 0x7F;

        logger.debug("NODE {}: Changing number of endpoints = {}", getNode().getNodeId(),
                changingNumberOfEndpoints ? "true" : false);
        logger.debug("NODE {}: Endpoints are the same device class = {}", getNode().getNodeId(),
                endpointsAreTheSameDeviceClass ? "true" : false);
        logger.debug("NODE {}: Number of endpoints = {}", getNode().getNodeId(), endpointsSupported);

        // TODO: handle dynamically added endpoints. Have never seen such a device.
        if (changingNumberOfEndpoints) {
            logger.warn(
                    "NODE {}: Changing number of endpoints, expect some weird behavior during multi channel handling.",
                    getNode().getNodeId());
        }

        // Add all the endpoints
        for (int i = 1; i <= endpointsSupported; i++) {
            getNode().addEndpoint(i);
        }
    }

    /**
     * Handles Multi Channel Capability Report message. Handles Report on an endpoint and adds command classes to the
     * endpoint.
     * This is for Version 2 of the command class.
     *
     * @param payload
     * @param endpoint
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = MULTI_CHANNEL_CAPABILITY_REPORT, name = "MULTI_CHANNEL_CAPABILITY_REPORT")
    public void handleMultiChannelCapabilityReport(ZWaveCommandClassPayload payload, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Process Multi-channel capability Report", getNode().getNodeId());

        int receivedEndpointId = payload.getPayloadByte(2) & 0x7F;
        boolean dynamic = ((payload.getPayloadByte(2) & 0x80) != 0);
        int genericDeviceClass = payload.getPayloadByte(3);
        int specificDeviceClass = payload.getPayloadByte(4);

        logger.debug("NODE {}: Endpoints are the same device class = {}", getNode().getNodeId(),
                endpointsAreTheSameDeviceClass);

        // Loop over all endpoints, or just set command classes on one, depending on whether
        // all endpoints have the same device class.
        int startId = endpointsAreTheSameDeviceClass ? 1 : receivedEndpointId;
        int endId = endpointsAreTheSameDeviceClass ? (getNode().getEndpointCount() - 1) : receivedEndpointId;

        boolean supportsBasicCommandClass = getNode().supportsCommandClass(CommandClass.COMMAND_CLASS_BASIC);

        for (int endpointId = startId; endpointId <= endId; endpointId++) {
            // Create a new endpoint
            ZWaveEndpoint nodeEndpoint = getNode().getEndpoint(endpointId);
            if (nodeEndpoint == null) {
                logger.error("NODE {}: Endpoint {} not found. Cannot set command classes.", getNode().getNodeId(),
                        endpointId);
                continue;
            }

            // Add the device classes
            if (!updateDeviceClass(nodeEndpoint, genericDeviceClass, specificDeviceClass, dynamic)) {
                // Updating device class failed, already logged, continue with next endpoint
                continue;
            }

            // Add basic command class, if it's also supported by the parent node.
            if (supportsBasicCommandClass) {
                ZWaveCommandClass commandClass = new ZWaveBasicCommandClass(getNode(), getController(), nodeEndpoint);
                nodeEndpoint.addCommandClass(commandClass);
            }

            // Add all the command classes supported by this endpoint
            addSupportedCommandClasses(payload, nodeEndpoint);
        }
    }

    /**
     * Determines the device class properties of the endpoint.
     *
     * @param endpoint The endpoint to update.
     * @param genericDeviceClass The generic device class of the parent device of the endpoint
     * @param specificDeviceClass The specific device class of the parent device of the endpoint
     * @param dynamic True when the endpoint is dynamic.
     * @return True when successful, false otherwise
     */
    private boolean updateDeviceClass(ZWaveEndpoint endpoint, int genericDeviceClass, int specificDeviceClass,
            boolean dynamic) {
        Basic basic = getNode().getDeviceClass().getBasicDeviceClass();
        Generic generic = Generic.getGeneric(genericDeviceClass);

        logger.debug("NODE {}: Endpoint Id = {}", getNode().getNodeId(), endpoint.getEndpointId());
        logger.debug("NODE {}: Endpoints is dynamic = {}", getNode().getNodeId(), dynamic ? "true" : false);
        logger.debug("NODE {}: Basic = {} 0x{}", getNode().getNodeId(), basic.toString(),
                Integer.toHexString(basic.getKey()));

        if (generic == null) {
            logger.error("NODE {}: Endpoint {} has invalid device class. generic = 0x{}, specific = 0x{}.",
                    getNode().getNodeId(), endpoint.getEndpointId(), Integer.toHexString(genericDeviceClass),
                    Integer.toHexString(specificDeviceClass));
            return false;
        }
        Specific specific = Specific.getSpecific(generic, specificDeviceClass);
        if (specific == null) {
            logger.error("NODE {}: Endpoint {} has invalid device class. generic = 0x{}, specific = 0x{}.",
                    getNode().getNodeId(), endpoint.getEndpointId(), Integer.toHexString(genericDeviceClass),
                    Integer.toHexString(specificDeviceClass));
            return false;
        }

        logger.debug("NODE {}: Generic = {} 0x{}", getNode().getNodeId(), generic.toString(),
                Integer.toHexString(generic.getKey()));
        logger.debug("NODE {}: Specific = {} 0x{}", getNode().getNodeId(), specific.toString(),
                Integer.toHexString(specific.getKey()));

        ZWaveDeviceClass deviceClass = endpoint.getDeviceClass();
        deviceClass.setBasicDeviceClass(basic);
        deviceClass.setGenericDeviceClass(generic);
        deviceClass.setSpecificDeviceClass(specific);

        return true;
    }

    /**
     * Adds command classes to the endpoint based on the message from the device.
     *
     * @param payload The message to get command classes from.
     * @param endpoint The endpoint
     * @throws ZWaveSerialMessageException
     */
    private void addSupportedCommandClasses(ZWaveCommandClassPayload payload, ZWaveEndpoint endpoint)
            throws ZWaveSerialMessageException {
        for (int ccPnt = 5; ccPnt < payload.getPayloadLength(); ccPnt++) {
            // Get the command class ID
            int data = payload.getPayloadByte(ccPnt);
            if (data == 0xef) {
                // TODO: Implement control command classes
                break;
            }

            // Create the command class
            ZWaveCommandClass commandClass = ZWaveCommandClass.getInstance(data, getNode(), getController(), endpoint);
            if (commandClass == null) {
                continue;
            }

            logger.debug("NODE {}: Endpoint {}: Adding command class {}.", getNode().getNodeId(),
                    endpoint.getEndpointId(), commandClass.getCommandClass());
            endpoint.addCommandClass(commandClass);

            ZWaveCommandClass parentClass = getNode().getCommandClass(commandClass.getCommandClass());

            // Copy version info to endpoint classes.
            if (parentClass != null) {
                commandClass.setVersion(parentClass.getVersion());
            }

            // With V2, we only have a single instance
            commandClass.setInstances(1);
        }
    }

    public int getSourceEndpoint(ZWaveCommandClassPayload payload) {
        int originatingEndpointId = payload.getPayloadByte(2);
        int destinationEndpointId = payload.getPayloadByte(3);

        if (useDestEndpointAsSource) {
            if (destinationEndpointId > 1) {
                // swap specified in node options and condition satisfied:
                if (originatingEndpointId <= 1) {
                    // swap originating and destination.
                    int temp = originatingEndpointId;
                    originatingEndpointId = destinationEndpointId;
                    destinationEndpointId = temp;
                } else {
                    // received an encapsulation for an endpoint on the controller.
                    logger.info("NODE {}: Received a multi instance encapsulation with a destination endpoint = {}. ",
                            getNode().getNodeId(), destinationEndpointId);
                    if (originatingEndpointId <= 1) {
                        // and it originates from either a root command class or a command class on the first endpoint.
                        // high probability of a firmware bug.
                        logger.warn("NODE {}: The originating endpoint is {}. Please notify author.",
                                getNode().getNodeId(), originatingEndpointId);
                    }
                }
            }
        }

        return originatingEndpointId;
    }

    /**
     * Gets a SerialMessage with the MULTI_INSTANCE_GET command.
     * Returns the number of instances for this command class.
     *
     * @param the command class to return the number of instances for.
     * @return the serial message.
     */
    public ZWaveCommandClassTransactionPayload getMultiInstanceGetMessage(CommandClass commandClass) {
        logger.debug("NODE {}: Creating new message for command MULTI_INSTANCE_GET command class {}",
                getNode().getNodeId(), commandClass);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MULTI_INSTANCE_GET).withPayload(commandClass.getKey()).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(MULTI_INSTANCE_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the MULTI_INSTANCE_ENCAP command.
     * Encapsulates a message for a specific instance.
     *
     * @param serialMessage the serial message to encapsulate
     * @param instance the number of the instance to encapsulate the message for.
     * @return the encapsulated serial message.
     */
    public ZWaveCommandClassTransactionPayload getMultiInstanceEncapMessage(
            ZWaveCommandClassTransactionPayload transactionPayload, int instance) {
        logger.debug("NODE {}: Creating new message for command MULTI_INSTANCE_ENCAP instance {}",
                getNode().getNodeId(), instance);

        ByteArrayOutputStream newPayload = new ByteArrayOutputStream();
        newPayload.write(getCommandClass().getKey());
        newPayload.write(MULTI_INSTANCE_ENCAP);
        newPayload.write(instance);
        try {
            newPayload.write(transactionPayload.getPayloadBuffer());

            return new ZWaveCommandClassTransactionPayload(transactionPayload.getNodeId(), newPayload.toByteArray(),
                    transactionPayload.getPriority(), transactionPayload.getExpectedResponseCommandClass(),
                    transactionPayload.getExpectedResponseCommandClassCommand());
        } catch (IOException e) {
            logger.debug("NODE {}: Error encapsulating multi instance", getNode().getNodeId(), e);
        }

        return null;
    }

    /**
     * Gets a SerialMessage with the MULTI_CHANNEL_ENDPOINT_GET command.
     * Returns the endpoints for this node.
     *
     * @return the serial message.
     */
    public ZWaveCommandClassTransactionPayload getMultiChannelEndpointGetMessage() {
        logger.debug("NODE {}: Creating new message for command MULTI_CHANNEL_ENDPOINT_GET", getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MULTI_CHANNEL_ENDPOINT_GET).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(MULTI_CHANNEL_ENDPOINT_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the MULTI_CHANNEL_CAPABILITY_GET command.
     * Gets the capabilities for a specific endpoint.
     *
     * @param the number of the endpoint to get the
     * @return the serial message.
     */
    public ZWaveCommandClassTransactionPayload getMultiChannelCapabilityGetMessage(int endpoint) {
        logger.debug("NODE {}: Creating new message for command MULTI_CHANNEL_CAPABILITY_GET endpoint {}",
                getNode().getNodeId(), endpoint);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                MULTI_CHANNEL_CAPABILITY_GET).withPayload(endpoint).withPriority(TransactionPriority.Config)
                        .withExpectedResponseCommand(MULTI_CHANNEL_CAPABILITY_REPORT).build();
    }

    /**
     * Gets a SerialMessage with the MULTI_INSTANCE_ENCAP command.
     * Encapsulates a message for a specific instance.
     *
     * @param serialMessage the serial message to encapsulate
     * @param endpoint the endpoint to encapsulate the message for.
     * @return the encapsulated serial message.
     */
    public ZWaveCommandClassTransactionPayload getMultiChannelEncapMessage(
            ZWaveCommandClassTransactionPayload transactionPayload, int endpoint) {
        logger.debug("NODE {}: Creating new message for command MULTI_CHANNEL_ENCAP endpoint {}", getNode().getNodeId(),
                endpoint);

        ByteArrayOutputStream newPayload = new ByteArrayOutputStream();
        newPayload.write(getCommandClass().getKey());
        newPayload.write(MULTI_CHANNEL_ENCAP);
        newPayload.write(1); // TODO: Make this a parameter to allow multiple source endpoints
        newPayload.write(endpoint);
        try {
            newPayload.write(transactionPayload.getPayloadBuffer());
            return new ZWaveCommandClassTransactionPayload(transactionPayload.getNodeId(), newPayload.toByteArray(),
                    transactionPayload.getPriority(), transactionPayload.getExpectedResponseCommandClass(),
                    transactionPayload.getExpectedResponseCommandClassCommand());
        } catch (IOException e) {
            logger.debug("NODE {}: Error encapsulating multi instance", getNode().getNodeId(), e);
        }

        return null;
    }

    /**
     * Initializes the Multi instance / endpoint command class by setting the number of instances
     * or getting the endpoints.
     *
     * @return SerialMessage message to send
     */
    public ArrayList<ZWaveCommandClassTransactionPayload> initEndpoints(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        logger.debug("NODE {}: Initialising endpoints - version {}", getNode().getNodeId(), getVersion());
        switch (getVersion()) {
            case 1:
                // Get number of instances for all command classes on this node.
                for (ZWaveCommandClass commandClass : getNode().getCommandClasses(0)) {
                    logger.debug("NODE {}: ENDPOINTS - checking {}, Instances {}", getNode().getNodeId(),
                            commandClass.getCommandClass().toString(), commandClass.getInstances());

                    // Skip classes known NOT to support multiple instances.
                    // This allows us to reduce the number of frames we send during initialisation
                    // where we already know it doesn't support multi-instance.
                    if (singleInstanceClasses.contains(commandClass.getCommandClass())) {
                        commandClass.setInstances(1);
                        logger.debug("NODE {}: ENDPOINTS - skipping {}", getNode().getNodeId(),
                                commandClass.getCommandClass().toString());
                        continue;
                    }
                    // Instances is set to 1 after it's initialised
                    if (commandClass.getInstances() == 0) {
                        logger.debug("NODE {}: ENDPOINTS - found    {}", getNode().getNodeId(),
                                commandClass.getCommandClass().toString());
                        result.add(getMultiInstanceGetMessage(commandClass.getCommandClass()));
                    }
                }
                break;
            case 2:
                // Set all classes to a single instance
                for (ZWaveCommandClass commandClass : getNode().getCommandClasses(0)) {
                    commandClass.setInstances(1);
                }

                // Request the number of endpoints
                if (refresh == true || getNode().getEndpointCount() == 1) {
                    result.add(getMultiChannelEndpointGetMessage());
                } else {
                    // We know the number of endpoints, so request the capabilites of each
                    for (int endpoint = 1; endpoint < getNode().getEndpointCount(); endpoint++) {
                        if (refresh == true || getNode().getCommandClasses(endpoint).size() == 0) {
                            result.add(getMultiChannelCapabilityGetMessage(endpoint));
                        }
                    }
                }
                break;
            default:
                logger.warn("NODE {}: Unknown version {} for command class {} (0x{})", getNode().getNodeId(),
                        getVersion(), getCommandClass().toString(), Integer.toHexString(getCommandClass().getKey()));
                break;
        }

        return result;
    };

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("true".equals(options.get("useDestination"))) {
            useDestEndpointAsSource = true;
        }

        return true;
    }
}
