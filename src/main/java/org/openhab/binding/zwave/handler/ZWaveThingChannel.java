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
package org.openhab.binding.zwave.handler;

import java.util.Map;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.zwave.internal.converter.ZWaveCommandClassConverter;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveThingChannel {
    public enum DataType {
        DecimalType,
        HSBType,
        IncreaseDecreaseType,
        OnOffType,
        OpenClosedType,
        PercentType,
        StringType,
        DateTimeType,
        UpDownType,
        QuantityType,
        StopMoveType;
    }

    // int nodeId;
    private int endpoint;
    private ChannelUID uid;
    private ChannelTypeUID channelTypeUID;
    private String commandClass;
    private ZWaveCommandClassConverter converter;
    private DataType dataType;
    private Map<String, String> arguments;

    public ZWaveThingChannel(ZWaveControllerHandler controller, ChannelTypeUID channelTypeUID, ChannelUID uid,
            DataType dataType, String commandClassName, int endpoint, Map<String, String> arguments) {
        this.uid = uid;
        this.arguments = arguments;
        this.commandClass = commandClassName;
        this.endpoint = endpoint;
        this.dataType = dataType;
        this.channelTypeUID = channelTypeUID;

        // Get the converter
        CommandClass commandClass = ZWaveCommandClass.CommandClass.getCommandClass(commandClassName);
        if (commandClass == null) {
            // logger.debug("NODE {}: Error finding command class {} on channel {}", nodeId, uid, commandClassName);
        } else {
            this.converter = ZWaveCommandClassConverter.getConverter(controller, commandClass);
            if (this.converter == null) {
                // logger.debug("NODE {}: No converter found for {}, class {}", nodeId, uid, commandClassName);
            }
        }
    }

    public ChannelUID getUID() {
        return uid;
    }

    public ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }

    public String getCommandClass() {
        return commandClass;
    }

    public int getEndpoint() {
        return endpoint;
    }

    public DataType getDataType() {
        return dataType;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public ZWaveCommandClassConverter getConverter() {
        return converter;
    }
}
