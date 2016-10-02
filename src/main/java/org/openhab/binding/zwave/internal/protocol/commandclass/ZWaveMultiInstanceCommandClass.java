/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSendDataMessageBuilder;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransactionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Multi Instance / Multi Channel command class. The Multi Instance command class is used to control
 * multiple instances of the same device class on the node. Multi Channel support (version 2) of the command class can
 * also handle multiple instances of different command classes. The instances are called endpoints in this version.
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

    private final Map<Integer, ZWaveEndpoint> endpoints = new HashMap<Integer, ZWaveEndpoint>();

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

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_MULTI_CHANNEL;
    }

    /**
     * Gets the endpoint object using it's endpoint ID as key.
     * Returns null if the endpoint is not found.
     *
     * @param endpointId the endpoint ID of the endpoint to get.
     * @return Endpoint object
     * @throws IllegalArgumentException thrown when the endpoint is not found.
     */
    public ZWaveEndpoint getEndpoint(int endpointId) {
        return endpoints.get(endpointId);
    }

    /**
     * Gets the collection of endpoints attached to this node.
     *
     * @return the collection of endpoints.
     */
    public Collection<ZWaveEndpoint> getEndpoints() {
        return endpoints.values();
    }

    /**
     * Handles Multi Instance Report message. Handles Report on
     * the number of instances for the command class.
     * This is for Version 1 of the command class.
     *
     * @param serialMessage the serial message to process.
     * @param offset the offset at which to start processing.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = MULTI_INSTANCE_REPORT, name = "MULTI_INSTANCE_REPORT")
    public void handleMultiInstanceReportResponse(ZWaveCommandClassPayload payload, int endpoint) {
        int commandClassCode = payload.getPayloadByte(1);
        int instances = payload.getPayloadByte(2);

        CommandClass commandClass = CommandClass.getCommandClass(commandClassCode);
        if (commandClass == null) {
            logger.error(String.format("NODE %d: Unsupported command class 0x%02x", getNode().getNodeId(),
                    commandClassCode));
            return;
        }

        logger.debug("NODE {}: Requested Command Class = {}", getNode().getNodeId(), commandClass);

        ZWaveCommandClass zwaveCommandClass = getNode().getCommandClass(commandClass);
        if (zwaveCommandClass == null) {
            logger.error(String.format("NODE %d: Unsupported command class %s (0x%02x)", getNode().getNodeId(),
                    commandClass, commandClassCode));
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
     * Handles Multi Instance Encapsulation message. Decapsulates an Application Command message and handles it using
     * the right instance.
     *
     * @param serialMessage the serial message to process.
     * @param offset the offset at which to start procesing.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = MULTI_INSTANCE_ENCAP, name = "MULTI_INSTANCE_ENCAP")
    public void handleMultiInstanceEncap(ZWaveCommandClassPayload payload, int endpoint)
            throws ZWaveSerialMessageException {
        int instance = payload.getPayloadByte(1);

        ZWaveCommandClassPayload encapPayload = new ZWaveCommandClassPayload(payload, 3);

        int commandClassCode = encapPayload.getPayloadByte(0);
        CommandClass commandClass = CommandClass.getCommandClass(commandClassCode);

        if (commandClass == null) {
            logger.error(String.format("NODE %d: Unsupported command class 0x%02x", getNode().getNodeId(),
                    commandClassCode));
            return;
        }

        logger.debug(String.format("NODE %d: Requested Command Class = %s (0x%02x)", getNode().getNodeId(),
                commandClass, commandClassCode));

        ZWaveCommandClass zwaveCommandClass = null;

        // first get command class from endpoint, if supported
        if (getVersion() >= 2) {
            ZWaveEndpoint nodeEndpoint = endpoints.get(instance);
            if (nodeEndpoint != null) {
                zwaveCommandClass = nodeEndpoint.getCommandClass(commandClass);
                if (zwaveCommandClass == null) {
                    logger.warn(String.format(
                            "NODE %d: CommandClass %s (0x%02x) not implemented by endpoint %d, fallback to main node.",
                            getNode().getNodeId(), commandClass, commandClassCode, instance));
                }
            }
        }

        if (zwaveCommandClass == null) {
            zwaveCommandClass = getNode().getCommandClass(commandClass);
        }

        if (zwaveCommandClass == null) {
            logger.error(String.format("NODE %d: Unsupported command class %s (0x%02x)", getNode().getNodeId(),
                    commandClass, commandClassCode));
            return;
        }

        logger.debug("NODE {}: Instance = {}, calling handleApplicationCommandRequest.", getNode().getNodeId(),
                instance);
        zwaveCommandClass.handleApplicationCommandRequest(encapPayload, instance);
    }

    /**
     * Handles Multi Channel Endpoint Report message. Handles Report on the number of endpoints and whether they are
     * dynamic and/or have the same command classes.
     * This is for Version 2 of the command class.
     *
     * @param serialMessage the serial message to process.
     * @param offset the offset at which to start processing.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = MULTI_CHANNEL_ENDPOINT_REPORT, name = "MULTI_CHANNEL_ENDPOINT_REPORT")
    public void handleMultiChannelEndpointReport(ZWaveCommandClassPayload payload, int endpoint) {
        boolean changingNumberOfEndpoints = (payload.getPayloadByte(1) & 0x80) != 0;
        endpointsAreTheSameDeviceClass = (payload.getPayloadByte(1) & 0x40) != 0;
        int endpointsSupported = payload.getPayloadByte(2) & 0x7F;

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
            ZWaveEndpoint nodeEndpoint = new ZWaveEndpoint(i);
            endpoints.put(i, nodeEndpoint);
        }
    }

    /**
     * Handles Multi Channel Capability Report message. Handles Report on an endpoint and adds command classes to the
     * endpoint.
     * This is for Version 2 of the command class.
     *
     * @param serialMessage the serial message to process.
     * @param offset the offset at which to start processing.
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
                endpointsAreTheSameDeviceClass ? "true" : false);

        // Loop over all endpoints, or just set command classes on one, depending on whether
        // all endpoints have the same device class.
        int startId = endpointsAreTheSameDeviceClass ? 1 : receivedEndpointId;
        int endId = endpointsAreTheSameDeviceClass ? endpoints.size() : receivedEndpointId;

        boolean supportsBasicCommandClass = getNode().supportsCommandClass(CommandClass.COMMAND_CLASS_BASIC);

        for (int endpointId = startId; endpointId <= endId; endpointId++) {
            // Create a new endpoint
            ZWaveEndpoint nodeEndpoint = endpoints.get(endpointId);
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

        if (!endpointsAreTheSameDeviceClass) {
            for (ZWaveEndpoint ep : endpoints.values()) {
                // only advance node stage when all endpoints are known.
                if (ep.getDeviceClass().getBasicDeviceClass() == Basic.NOT_KNOWN) {
                    return;
                }
            }
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
        if (generic == null) {
            logger.error(
                    String.format("NODE %d: Endpoint %d has invalid device class. generic = 0x%02x, specific = 0x%02x.",
                            getNode().getNodeId(), endpoint.getEndpointId(), genericDeviceClass, specificDeviceClass));
            return false;
        }
        Specific specific = Specific.getSpecific(generic, specificDeviceClass);
        if (specific == null) {
            logger.error(
                    String.format("NODE %d: Endpoint %d has invalid device class. generic = 0x%02x, specific = 0x%02x.",
                            getNode().getNodeId(), endpoint.getEndpointId(), genericDeviceClass, specificDeviceClass));
            return false;
        }

        logger.debug("NODE {}: Endpoint Id = {}", getNode().getNodeId(), endpoint.getEndpointId());
        logger.debug("NODE {}: Endpoints is dynamic = {}", getNode().getNodeId(), dynamic ? "true" : false);
        logger.debug(
                String.format("NODE %d: Basic = %s 0x%02x", getNode().getNodeId(), basic.getLabel(), basic.getKey()));
        logger.debug(String.format("NODE %d: Generic = %s 0x%02x", getNode().getNodeId(), generic.getLabel(),
                generic.getKey()));
        logger.debug(String.format("NODE %d: Specific = %s 0x%02x", getNode().getNodeId(), specific.getLabel(),
                specific.getKey()));

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
     * @param offset The offset in the message.
     * @param endpoint The endpoint
     * @throws ZWaveSerialMessageException
     */
    private void addSupportedCommandClasses(ZWaveCommandClassPayload payload, ZWaveEndpoint endpoint)
            throws ZWaveSerialMessageException {
        for (int i = 0; i < payload.getPayloadLength() - 4; i++) {
            // Get the command class ID
            int data = payload.getPayloadByte(4 + i);
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

    /**
     * Handles Multi Channel Encapsulation message. Decapsulates an Application Command message and handles it using the
     * right endpoint.
     *
     * @param serialMessage the serial message to process.
     * @param offset the offset at which to start processing.
     * @throws ZWaveSerialMessageException
     */
    @ZWaveResponseHandler(id = MULTI_CHANNEL_ENCAP, name = "MULTI_CHANNEL_ENCAP")
    public void handleMultiChannelEncap(ZWaveCommandClassPayload payload, int endpoint)
            throws ZWaveSerialMessageException {

        CommandClass commandClass;
        ZWaveCommandClass zwaveCommandClass;
        int originatingEndpointId = payload.getPayloadByte(2);
        int destinationEndpointId = payload.getPayloadByte(3);

        if (useDestEndpointAsSource) {
            // Not a full swap. Do not use destinationEndpointId after this line
            // and leave scope intact.
            originatingEndpointId = destinationEndpointId;
        }

        ZWaveCommandClassPayload encapPayload = new ZWaveCommandClassPayload(payload, 4);

        int commandClassCode = encapPayload.getPayloadByte(0);
        commandClass = CommandClass.getCommandClass(commandClassCode);

        if (commandClass == null) {
            logger.error(String.format("NODE %d: Unsupported command class 0x%02x", getNode().getNodeId(),
                    commandClassCode));
            return;
        }

        logger.debug(String.format("NODE %d: Requested Command Class = %s (0x%02x)", getNode().getNodeId(),
                commandClass, commandClassCode));
        ZWaveEndpoint nodeEndpoint = endpoints.get(originatingEndpointId);

        if (nodeEndpoint == null) {
            logger.error("NODE {}: Endpoint {} not found. Cannot set command classes.", getNode().getNodeId(),
                    originatingEndpointId);
            return;
        }

        zwaveCommandClass = nodeEndpoint.getCommandClass(commandClass);

        if (zwaveCommandClass == null) {
            logger.warn(String.format(
                    "NODE %d: CommandClass %s (0x%02x) not implemented by endpoint %d, fallback to main node.",
                    getNode().getNodeId(), commandClass, commandClassCode, originatingEndpointId));
            zwaveCommandClass = getNode().getCommandClass(commandClass);
        }

        if (zwaveCommandClass == null) {
            logger.error(String.format("NODE %d: CommandClass %s (0x%02x) not implemented.", getNode().getNodeId(),
                    commandClass, commandClassCode));
            return;
        }

        logger.debug("NODE {}: Endpoint = {}, calling handleApplicationCommandRequest.", getNode().getNodeId(),
                originatingEndpointId);
        zwaveCommandClass.handleApplicationCommandRequest(encapPayload, originatingEndpointId);
    }

    /**
     * Gets a SerialMessage with the MULTI_INSTANCE_GET command.
     * Returns the number of instances for this command class.
     *
     * @param the command class to return the number of instances for.
     * @return the serial message.
     */
    public ZWaveTransaction getMultiInstanceGetMessage(CommandClass commandClass) {
        logger.debug("NODE {}: Creating new message for command MULTI_INSTANCE_GET command class {}",
                getNode().getNodeId(), commandClass);

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), MULTI_INSTANCE_GET).withNodeId(getNode().getNodeId())
                .withPayload(commandClass.getKey()).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), MULTI_INSTANCE_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the MULTI_INSTANCE_ENCAP command.
     * Encapsulates a message for a specific instance.
     *
     * @param serialMessage the serial message to encapsulate
     * @param instance the number of the instance to encapsulate the message for.
     * @return the encapsulated serial message.
     */
    public SerialMessage getMultiInstanceEncapMessage(SerialMessage serialMessage, int instance) {
        logger.debug("NODE {}: Creating new message for command MULTI_INSTANCE_ENCAP instance {}",
                this.getNode().getNodeId(), instance);

        byte[] payload = serialMessage.getMessagePayload();
        byte[] newPayload = new byte[payload.length + 3];
        System.arraycopy(payload, 0, newPayload, 0, 2);
        System.arraycopy(payload, 0, newPayload, 3, payload.length);
        newPayload[1] += 3;
        newPayload[2] = (byte) getCommandClass().getKey();
        newPayload[3] = MULTI_INSTANCE_ENCAP;
        newPayload[4] = (byte) (instance);

        serialMessage.setMessagePayload(newPayload);
        return serialMessage;
    }

    /**
     * Gets a SerialMessage with the MULTI CHANNEL ENDPOINT GET command.
     * Returns the endpoints for this node.
     *
     * @return the serial message.
     */
    public ZWaveTransaction getMultiChannelEndpointGetMessage() {
        logger.debug("NODE {}: Creating new message for command MULTI_CHANNEL_ENDPOINT_GET",
                this.getNode().getNodeId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), MULTI_CHANNEL_ENDPOINT_GET).withNodeId(getNode().getNodeId())
                .build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), MULTI_CHANNEL_ENDPOINT_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the MULTI_CHANNEL_CAPABILITY_GET command.
     * Gets the capabilities for a specific endpoint.
     *
     * @param the number of the endpoint to get the
     * @return the serial message.
     */
    public ZWaveTransaction getMultiChannelCapabilityGetMessage(ZWaveEndpoint endpoint) {
        logger.debug("NODE {}: Creating new message for command MULTI_CHANNEL_CAPABILITY_GET endpoint {}",
                this.getNode().getNodeId(), endpoint.getEndpointId());

        SerialMessage serialMessage = new ZWaveSendDataMessageBuilder()
                .withCommandClass(getCommandClass(), MULTI_CHANNEL_CAPABILITY_GET).withNodeId(getNode().getNodeId())
                .withPayload(endpoint.getEndpointId()).build();

        return new ZWaveTransactionBuilder(serialMessage)
                .withExpectedResponseClass(SerialMessageClass.ApplicationCommandHandler)
                .withExpectedResponseCommandClass(getCommandClass(), MULTI_CHANNEL_CAPABILITY_REPORT)
                .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the MULTI_INSTANCE_ENCAP command.
     * Encapsulates a message for a specific instance.
     *
     * @param serialMessage the serial message to encapsulate
     * @param endpoint the endpoint to encapsulate the message for.
     * @return the encapsulated serial message.
     */
    public SerialMessage getMultiChannelEncapMessage(SerialMessage serialMessage, ZWaveEndpoint endpoint) {
        logger.debug("NODE {}: Creating new message for command MULTI_CHANNEL_ENCAP endpoint {}", getNode().getNodeId(),
                endpoint.getEndpointId());

        byte[] payload = serialMessage.getMessagePayload();
        byte[] newPayload = new byte[payload.length + 4];
        System.arraycopy(payload, 0, newPayload, 0, 2);
        System.arraycopy(payload, 0, newPayload, 4, payload.length);
        newPayload[1] += 4;
        newPayload[2] = (byte) getCommandClass().getKey();
        newPayload[3] = MULTI_CHANNEL_ENCAP;
        newPayload[4] = 0x01;
        newPayload[5] = (byte) endpoint.getEndpointId();

        serialMessage.setMessagePayload(newPayload);
        return serialMessage;
    }

    /**
     * Initializes the Multi instance / endpoint command class by setting the number of instances or getting the
     * endpoints.
     *
     * @return SerialMessage message to send
     */
    public ArrayList<ZWaveTransaction> initEndpoints(boolean refresh) {
        ArrayList<ZWaveTransaction> result = new ArrayList<ZWaveTransaction>();

        logger.debug("NODE {}: Initialising endpoints - version {}", getNode().getNodeId(), getVersion());
        switch (getVersion()) {
            case 1:
                // Get number of instances for all command classes on this node.
                for (ZWaveCommandClass commandClass : getNode().getCommandClasses()) {
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
                for (ZWaveCommandClass commandClass : getNode().getCommandClasses()) {
                    commandClass.setInstances(1);
                }

                // Request the number of endpoints
                if (refresh == true || endpoints.size() == 0) {
                    result.add(getMultiChannelEndpointGetMessage());
                } else {
                    for (Map.Entry<Integer, ZWaveEndpoint> entry : endpoints.entrySet()) {
                        if (refresh == true || entry.getValue().getCommandClasses().size() == 0) {
                            result.add(getMultiChannelCapabilityGetMessage(entry.getValue()));
                        }
                    }
                }
                break;
            default:
                logger.warn(String.format("NODE %d: Unknown version %d for command class %s (0x%02x)",
                        getNode().getNodeId(), getVersion(), getCommandClass().toString(), getCommandClass().getKey()));
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
