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
package org.openhab.binding.zwave.internal;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterGroup;
import org.eclipse.smarthome.config.core.ConfigDescriptionProvider;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveUserCodeCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveUserCodeCommandClass.UserCode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveUserCodeCommandClass.UserIdStatusType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Chris Jackson
 *
 */
@Component(immediate = true, service = { ConfigDescriptionProvider.class, ConfigOptionProvider.class })
public class ZWaveConfigProvider implements ConfigDescriptionProvider, ConfigOptionProvider {
    private final static Logger logger = LoggerFactory.getLogger(ZWaveConfigProvider.class);

    private static ThingRegistry thingRegistry;
    private static ThingTypeRegistry thingTypeRegistry;
    private static ConfigDescriptionRegistry configDescriptionRegistry;

    private static Set<ThingTypeUID> zwaveThingTypeUIDList = new HashSet<ThingTypeUID>();
    private static List<ZWaveProduct> productIndex = new ArrayList<ZWaveProduct>();

    private static final Object productIndexLock = new Object();

    // The following is a list of classes that are controllable.
    // This is used to filter endpoints so that when we display a list of nodes/endpoints
    // for configuring associations, we only list endpoints that are useful
    private static final Set<ZWaveCommandClass.CommandClass> controllableClasses = Collections
            .unmodifiableSet(Stream.of(CommandClass.COMMAND_CLASS_BASIC, CommandClass.COMMAND_CLASS_SWITCH_BINARY,
                    CommandClass.COMMAND_CLASS_SWITCH_MULTILEVEL, CommandClass.COMMAND_CLASS_SWITCH_TOGGLE_BINARY,
                    CommandClass.COMMAND_CLASS_SWITCH_TOGGLE_MULTILEVEL, CommandClass.COMMAND_CLASS_CHIMNEY_FAN,
                    CommandClass.COMMAND_CLASS_THERMOSTAT_HEATING, CommandClass.COMMAND_CLASS_THERMOSTAT_MODE,
                    CommandClass.COMMAND_CLASS_THERMOSTAT_OPERATING_STATE,
                    CommandClass.COMMAND_CLASS_THERMOSTAT_SETPOINT, CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_MODE,
                    CommandClass.COMMAND_CLASS_THERMOSTAT_FAN_STATE).collect(Collectors.toSet()));

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        ZWaveConfigProvider.thingRegistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        ZWaveConfigProvider.thingRegistry = null;
    }

    @Reference
    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        ZWaveConfigProvider.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        ZWaveConfigProvider.thingTypeRegistry = null;
    }

    @Reference
    protected void setConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        ZWaveConfigProvider.configDescriptionRegistry = configDescriptionRegistry;
    }

    protected void unsetConfigDescriptionRegistry(ConfigDescriptionRegistry configDescriptionRegistry) {
        ZWaveConfigProvider.configDescriptionRegistry = null;
    }

    @Override
    public Collection<ConfigDescription> getConfigDescriptions(Locale locale) {
        logger.debug("getConfigDescriptions called");
        return Collections.emptySet();
    }

    @Override
    public ConfigDescription getConfigDescription(URI uri, Locale locale) {
        if (!"thing".equals(uri.getScheme()) && !"thing-type".equals(uri.getScheme())) {
            return null;
        }

        ThingTypeUID thingTypeUID = new ThingTypeUID(uri.getSchemeSpecificPart());

        // Is this a zwave thing?
        if (!thingTypeUID.getBindingId().equals(ZWaveBindingConstants.BINDING_ID)) {
            return null;
        }

        List<ConfigDescriptionParameter> parameters = new ArrayList<ConfigDescriptionParameter>();

        if ("thing-type".equals(uri.getScheme())) {
            if (uri.getSchemeSpecificPart().equals(ZWaveBindingConstants.CONTROLLER_SERIAL.toString())) {
                return null;
            }
            parameters.add(
                    ConfigDescriptionParameterBuilder.create(ZWaveBindingConstants.CONFIGURATION_NODEID, Type.INTEGER)
                            .withLabel("Node ID").withMinimum(new BigDecimal("1")).withMaximum(new BigDecimal("232"))
                            .withAdvanced(true).withReadOnly(true).withRequired(true)
                            .withDescription("Sets the node ID<BR/>"
                                    + "The node ID is assigned by the controller and can not be changed.")
                            .withGroupName("thingcfg").build());

            return ConfigDescriptionBuilder.create(uri).withParameters(parameters).build();
        }

        ThingUID thingUID = new ThingUID(uri.getSchemeSpecificPart());

        Thing thing = getThing(thingUID);
        if (thing == null) {
            logger.debug("No thing found in getConfigDescription {}", uri);
            return null;
        }
        ThingUID bridgeUID = thing.getBridgeUID();
        if (bridgeUID == null) {
            logger.debug("No bridgeUID found in getConfigDescription {}", uri);
            return null;
        }

        // Get the controller for this thing
        Thing bridge = getThing(bridgeUID);
        if (bridge == null) {
            logger.debug("No bridge found in getConfigDescription {}", uri);
            return null;
        }

        final BigDecimal cfgNodeId = (BigDecimal) thing.getConfiguration()
                .get(ZWaveBindingConstants.CONFIGURATION_NODEID);
        if (cfgNodeId == null) {
            logger.debug("No nodeId found in getConfigDescription {}", uri);
            return null;
        }
        int nodeId = cfgNodeId.intValue();

        // Get its handler and node
        ZWaveControllerHandler handler = (ZWaveControllerHandler) bridge.getHandler();
        if (handler == null) {
            logger.debug("NODE {}: No bridge handler found in getConfigDescription", nodeId);
            return null;
        }

        ZWaveNode node = handler.getNode(nodeId);
        if (node == null) {
            logger.debug("NODE {}: Node not found in getConfigDescription", nodeId);
            return null;
        }

        List<ConfigDescriptionParameterGroup> groups = new ArrayList<ConfigDescriptionParameterGroup>();
        groups.add(new ConfigDescriptionParameterGroup("actions", "", false, "Actions", "Actions"));
        groups.add(new ConfigDescriptionParameterGroup("thingcfg", "home", false, "Device Configuration",
                "Device Configuration"));

        List<ParameterOption> options = new ArrayList<ParameterOption>();
        options.add(new ParameterOption("600", "10 Minutes"));
        options.add(new ParameterOption("1800", "30 Minutes"));
        options.add(new ParameterOption("3600", "1 Hour"));
        options.add(new ParameterOption("7200", "2 Hours"));
        options.add(new ParameterOption("10800", "3 Hours"));
        options.add(new ParameterOption("21600", "6 Hours"));
        options.add(new ParameterOption("43200", "12 Hours"));
        options.add(new ParameterOption("86400", "1 Day"));
        options.add(new ParameterOption("172800", "2 Days"));
        options.add(new ParameterOption("864000", "10 Days"));

        parameters.add(
                ConfigDescriptionParameterBuilder.create(ZWaveBindingConstants.CONFIGURATION_POLLPERIOD, Type.INTEGER)
                        .withLabel(ZWaveBindingConstants.CONFIG_BINDING_POLLINGPERIOD_LABEL)
                        .withDescription(ZWaveBindingConstants.CONFIG_BINDING_POLLINGPERIOD_DESC).withDefault("86400")
                        .withMinimum(new BigDecimal(15)).withMaximum(new BigDecimal(864000)).withOptions(options)
                        .withLimitToOptions(false).withGroupName("thingcfg").build());

        options = new ArrayList<ParameterOption>();
        options.add(new ParameterOption("0", "Disabled"));
        parameters.add(ConfigDescriptionParameterBuilder
                .create(ZWaveBindingConstants.CONFIGURATION_CMDREPOLLPERIOD, Type.INTEGER)
                .withLabel(ZWaveBindingConstants.CONFIG_BINDING_CMDREPOLLPERIOD_LABEL)
                .withDescription(ZWaveBindingConstants.CONFIG_BINDING_CMDREPOLLPERIOD_DESC).withDefault("1500")
                .withMinimum(new BigDecimal(100)).withMaximum(new BigDecimal(15000)).withOptions(options)
                .withLimitToOptions(false).withGroupName("thingcfg").build());

        // If we support the wakeup class, then add the configuration
        ZWaveWakeUpCommandClass wakeupCmdClass = (ZWaveWakeUpCommandClass) node
                .getCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_WAKE_UP);
        if (wakeupCmdClass != null) {
            groups.add(new ConfigDescriptionParameterGroup("wakeup", "sleep", false, "Wakeup Configuration",
                    "Configuration for wakeup parameters on battery devices"));

            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL, Type.INTEGER)
                    .withLabel("Wakeup Interval").withMinimum(new BigDecimal(wakeupCmdClass.getMinInterval()))
                    .withMaximum(new BigDecimal(wakeupCmdClass.getMaxInterval()))
                    .withDescription("Sets the number of seconds that the device will wakeup<BR/>"
                            + "Setting a shorter time will allow openHAB to configure the device more regularly, but may use more battery power.<BR>"
                            + "<B>Note:</B> This setting does not impact device notifications such as alarms.")
                    .withGroupName("wakeup").build());

            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_WAKEUPNODE, Type.INTEGER).withLabel("Wakeup Node")
                    .withAdvanced(true).withMinimum(new BigDecimal(1)).withMaximum(new BigDecimal(232))
                    .withDescription("Sets the wakeup node to which the device will send notifications.<BR/>"
                            + "This should normally be set to the openHAB controller - "
                            + "if it isn't, openHAB will not receive notifications when the device wakes up, "
                            + "and will not be able to configure the device.")
                    .withGroupName("wakeup").build());
        }

        // If we support the node name class, then add the configuration
        if (node.getCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_NODE_NAMING) != null) {
            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_NODENAME, Type.TEXT).withLabel("Node Name")
                    .withDescription("Sets a string for the device name").withGroupName("thingcfg").build());
            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_NODELOCATION, Type.TEXT)
                    .withDescription("Sets a string for the device location").withLabel("Node Location")
                    .withGroupName("thingcfg").build());
        }

        // If we support the switch_all class, then add the configuration
        if (node.getCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_SWITCH_ALL) != null) {
            options = new ArrayList<ParameterOption>();
            options.add(new ParameterOption("0", "Exclude from All On and All Off groups"));
            options.add(new ParameterOption("1", "Include in All On group"));
            options.add(new ParameterOption("2", "Include in All Off group"));
            options.add(new ParameterOption("255", "Include in All On and All Off groups"));
            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_SWITCHALLMODE, Type.INTEGER)
                    .withLabel("Switch All Mode")
                    .withDescription("Set the mode for the switch when receiving SWITCH ALL commands.").withDefault("0")
                    .withGroupName("thingcfg").withOptions(options).withLimitToOptions(true).build());
        }

        // If we support DOOR_LOCK - add options
        if (node.getCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_DOOR_LOCK) != null) {
            parameters.add(ConfigDescriptionParameterBuilder
                    .create(ZWaveBindingConstants.CONFIGURATION_DOORLOCKTIMEOUT, Type.INTEGER).withLabel("Lock Timeout")
                    .withDescription("Set the timeout on the lock.").withDefault("30").withGroupName("thingcfg")
                    .build());
        }

        ZWaveUserCodeCommandClass userCodeClass = (ZWaveUserCodeCommandClass) node
                .getCommandClass(ZWaveCommandClass.CommandClass.COMMAND_CLASS_USER_CODE);
        if (userCodeClass != null && userCodeClass.getNumberOfSupportedCodes() > 0) {
            groups.add(new ConfigDescriptionParameterGroup("usercode", "lock", false, "User Code",
                    "Define the user codes for locks"));

            for (int code = 1; code <= userCodeClass.getNumberOfSupportedCodes(); code++) {
                UserCode userCode = userCodeClass.getCachedUserCode(code);
                parameters.add(ConfigDescriptionParameterBuilder
                        .create(ZWaveBindingConstants.CONFIGURATION_USERCODE_LABEL + code, Type.TEXT)
                        .withLabel("Code " + code + " Label").withDescription("Name for user code " + code)
                        .withGroupName("usercode").build());
                parameters.add(ConfigDescriptionParameterBuilder
                        .create(ZWaveBindingConstants.CONFIGURATION_USERCODE_CODE + code, Type.TEXT)
                        .withLabel("Code " + code).withDescription("Set the user code (4 to 10 numbers)")
                        .withReadOnly(userCode.getState() == UserIdStatusType.RESERVED_BY_ADMINISTRATOR)
                        .withGroupName("usercode").build());
            }
        }

        // If we're FAILED, allow removing from the controller
        // if (node.getNodeState() == ZWaveNodeState.FAILED) {
        parameters.add(ConfigDescriptionParameterBuilder.create("action_remove", Type.BOOLEAN)
                .withLabel("Remove device from controller").withAdvanced(true).withOptions(options).withDefault("false")
                .withGroupName("actions").build());
        // } else {
        // Otherwise, allow us to put this on the failed list
        parameters.add(ConfigDescriptionParameterBuilder.create("action_failed", Type.BOOLEAN)
                .withLabel("Set device as FAILed").withAdvanced(true).withOptions(options).withDefault("false")
                .withGroupName("actions").build());
        // }

        if (node.isInitializationComplete() == true) {
            parameters.add(ConfigDescriptionParameterBuilder.create("action_reinit", Type.BOOLEAN)
                    .withLabel("Reinitialise the device").withAdvanced(true).withOptions(options).withDefault("false")
                    .withGroupName("actions").build());
        }
        parameters
                .add(ConfigDescriptionParameterBuilder.create("action_heal", Type.BOOLEAN).withLabel("Heal the device")
                        .withAdvanced(true).withOptions(options).withDefault("false").withGroupName("actions").build());

        return ConfigDescriptionBuilder.create(uri).withParameters(parameters).withParameterGroups(groups).build();
    }

    private static void initialiseZWaveThings() {
        // Check that we know about the registry
        if (thingTypeRegistry == null) {
            return;
        }

        synchronized (productIndexLock) {
            zwaveThingTypeUIDList = new HashSet<ThingTypeUID>();
            productIndex = new ArrayList<ZWaveProduct>();

            // Get all the thing types
            Collection<ThingType> thingTypes = thingTypeRegistry.getThingTypes();
            for (ThingType thingType : thingTypes) {
                // Is this for our binding?
                if (ZWaveBindingConstants.BINDING_ID.equals(thingType.getBindingId()) == false) {
                    continue;
                }

                // Create a list of all things supported by this binding
                zwaveThingTypeUIDList.add(thingType.getUID());

                // Get the properties
                Map<String, String> thingProperties = thingType.getProperties();

                if (thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_REFERENCES) == null) {
                    logger.debug("ZWave product {} has no references!", thingType.getUID());
                    continue;
                }

                String[] references = thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_REFERENCES).split(",");
                for (String ref : references) {
                    String[] values = ref.split(":");
                    Integer type;
                    Integer id = null;
                    if (values.length != 2) {
                        logger.debug("ZWave product {} has invalid references! '{}'", thingType.getUID(),
                                thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_REFERENCES));
                        continue;
                    }

                    type = Integer.parseInt(values[0], 16);
                    if (!values[1].trim().equals("*")) {
                        id = Integer.parseInt(values[1], 16);
                    }
                    String versionMin = thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_VERSIONMIN);
                    String versionMax = thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_VERSIONMAX);
                    productIndex.add(new ZWaveProduct(thingType.getUID(),
                            Integer.parseInt(thingProperties.get(ZWaveBindingConstants.PROPERTY_XML_MANUFACTURER), 16),
                            type, id, versionMin, versionMax));
                }
            }
        }
    }

    public static synchronized List<ZWaveProduct> getProductIndex() {
        if (productIndex.size() == 0) {
            initialiseZWaveThings();
        }
        return productIndex;
    }

    public static Set<ThingTypeUID> getSupportedThingTypes() {
        if (zwaveThingTypeUIDList.size() == 0) {
            initialiseZWaveThings();
        }
        return zwaveThingTypeUIDList;
    }

    public static ThingType getThingType(ThingTypeUID thingTypeUID) {
        // Check that we know about the registry
        if (thingTypeRegistry == null) {
            return null;
        }

        return thingTypeRegistry.getThingType(thingTypeUID);
    }

    public static ThingType getThingType(ZWaveNode node) {
        // Check that we know about the registry
        if (thingTypeRegistry == null) {
            logger.debug("{}: Unable to get thing type as registry not set", node.getNodeId());
            return null;
        }

        for (ZWaveProduct product : ZWaveConfigProvider.getProductIndex()) {
            logger.trace("{}: Checking {}: {}", node.getNodeId(), product.getThingTypeUID(), product);
            if (product.match(node) == true) {
                logger.trace("{}: Matched {}: {}", node.getNodeId(), product.getThingTypeUID(), product);
                return thingTypeRegistry.getThingType(product.thingTypeUID);
            }
        }

        return null;
    }

    /**
     * Gets the configuration parameters for a ZWave Thing. If the thing contains no configuration, it will return null.
     *
     * @param type the {@link ThingType} required to retrieve the configuration
     * @return the {@link ConfigDescription}
     */
    public static ConfigDescription getThingTypeConfig(ThingType type) {
        // Check that we know about the registry
        if (configDescriptionRegistry == null) {
            return null;
        }

        URI configUri = type.getConfigDescriptionURI();
        if (configUri == null) {
            return null;
        }
        return configDescriptionRegistry.getConfigDescription(configUri);
    }

    public static Thing getThing(ThingUID thingUID) {
        // Check that we know about the registry
        if (thingRegistry == null) {
            return null;
        }

        return thingRegistry.get(thingUID);
    }

    /**
     * Check if this node supports a controllable command class
     *
     * @param node the {@link ZWaveNode)
     * @return true if a controllable class is supported
     */
    private boolean supportsControllableClass(ZWaveNode node) {
        for (CommandClass commandClass : controllableClasses) {
            if (node.supportsCommandClass(commandClass) == true) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if this node supports a controllable command class
     *
     * @param node the {@link ZWaveNode)
     * @return true if a controllable class is supported
     */
    private boolean supportsControllableClass(ZWaveEndpoint endpoint) {
        for (CommandClass commandClass : controllableClasses) {
            if (endpoint.supportsCommandClass(commandClass) == true) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        // We need to update the options of all requests for association groups...
        if (!"thing".equals(uri.getScheme())) {
            return null;
        }

        ThingUID thingUID = new ThingUID(uri.getSchemeSpecificPart());
        ThingType thingType = thingTypeRegistry.getThingType(thingUID.getThingTypeUID());
        if (thingType == null) {
            return null;
        }

        // Is this a zwave thing?
        if (!thingUID.getBindingId().equals(ZWaveBindingConstants.BINDING_ID)) {
            return null;
        }

        // And is it an association group?
        if (!param.startsWith("group_")) {
            return null;
        }

        // And make sure this is a node because we want to get the id off the end...
        if (!thingUID.getId().startsWith("node")) {
            return null;
        }
        int nodeId = Integer.parseInt(thingUID.getId().substring(4));

        Thing thing = getThing(thingUID);
        ThingUID bridgeUID = thing.getBridgeUID();

        // Get the controller for this thing
        Thing bridge = getThing(bridgeUID);
        if (bridge == null) {
            return null;
        }

        // Get its handler
        ZWaveControllerHandler handler = (ZWaveControllerHandler) bridge.getHandler();
        if (handler == null) {
            return null;
        }

        boolean supportsMultiInstanceAssociation = false;
        ZWaveNode myNode = handler.getNode(nodeId);
        if (myNode == null) {
            return null;
        }

        if (myNode.getCommandClass(CommandClass.COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION) != null) {
            supportsMultiInstanceAssociation = true;
        }

        List<ParameterOption> options = new ArrayList<ParameterOption>();

        // Add the controller (ie openHAB) to the top of the list
        options.add(new ParameterOption(ZWaveBindingConstants.GROUP_CONTROLLER, "Controller"));

        // And iterate over all its nodes
        Collection<ZWaveNode> nodes = handler.getNodes();
        for (ZWaveNode node : nodes) {
            // Don't add its own id or the controller
            if (node.getNodeId() == nodeId || node.getNodeId() == handler.getOwnNodeId()) {
                continue;
            }

            String nodeName = "Node " + node.getNodeId();

            // Get this nodes thing so we can find the name
            // TODO: Add this when thing names are supported!
            // Thing thingNode = getThing(thingUID);

            // If the device supports multi_instance_association class, then add all controllable endpoints as well...
            // If this node also supports multi_instance class
            if (supportsMultiInstanceAssociation == true && node.getEndpointCount() > 1
                    && node.getCommandClass(CommandClass.COMMAND_CLASS_MULTI_CHANNEL) != null) {
                // Loop through all the endpoints for this device and add any that are controllable

                for (int endpointId = 1; endpointId < node.getEndpointCount(); endpointId++) {
                    if (supportsControllableClass(node.getEndpoint(endpointId))) {
                        String endpointName = nodeName;
                        if (endpointId != 0) {
                            endpointName += " (Endpoint " + endpointId + ")";
                        }
                        options.add(new ParameterOption("node_" + node.getNodeId() + "_" + endpointId, endpointName));
                    }
                }
            } else if (supportsControllableClass(node)) {
                // Add the node for the standard association class if it supports a controllable class
                options.add(new ParameterOption("node_" + node.getNodeId(), nodeName));
            }
        }

        return Collections.unmodifiableList(options);
    }
}
