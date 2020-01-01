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
package org.openhab.binding.zwave.internal.protocol.serialmessage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveTransactionMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class processes a serial message from the zwave controller
 *
 * @author Chris Jackson
 */
public class SerialApiGetCapabilitiesMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(SerialApiGetCapabilitiesMessageClass.class);

    private String serialAPIVersion = "Unknown";
    private int manufactureId = 0;
    private int deviceType = 0;
    private int deviceId = 0;

    private Set<SerialMessage.SerialMessageClass> apiCapabilities = new HashSet<>();

    public ZWaveSerialPayload doRequest() {
        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.SerialApiGetCapabilities).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.trace("Handle Message Serial API Get Capabilities - Length {}",
                incomingMessage.getMessagePayload().length);

        serialAPIVersion = String.format("%d.%d", incomingMessage.getMessagePayloadByte(0),
                incomingMessage.getMessagePayloadByte(1));
        manufactureId = ((incomingMessage.getMessagePayloadByte(2)) << 8) | (incomingMessage.getMessagePayloadByte(3));
        deviceType = ((incomingMessage.getMessagePayloadByte(4)) << 8) | (incomingMessage.getMessagePayloadByte(5));
        deviceId = (((incomingMessage.getMessagePayloadByte(6)) << 8) | (incomingMessage.getMessagePayloadByte(7)));

        logger.debug("API Version    = {}", serialAPIVersion);
        logger.debug("Manufacture ID = 0x{}", Integer.toHexString(manufactureId));
        logger.debug("Device Type    = 0x{}", Integer.toHexString(deviceType));
        logger.debug("Device ID      = 0x{}", Integer.toHexString(deviceId));

        apiCapabilities = new HashSet<>();
        // Print the list of messages supported by this controller
        for (int by = 8; by < incomingMessage.getMessagePayload().length; by++) {
            for (int bi = 0; bi < 8; bi++) {
                if ((incomingMessage.getMessagePayloadByte(by) & (0x01 << bi)) != 0) {
                    SerialMessage.SerialMessageClass msgClass = SerialMessage.SerialMessageClass
                            .getMessageClass(((by - 8) << 3) + bi + 1);
                    if (msgClass == null) {
                        logger.trace("Supports: Unknown Class {}", Integer.toHexString(((by - 8) << 3) + bi + 1));
                    } else {
                        logger.trace("Supports: {}", msgClass);
                        apiCapabilities.add(msgClass);
                    }
                }
            }
        }

        transaction.setTransactionComplete();
        return true;
    }

    public String getSerialAPIVersion() {
        return serialAPIVersion;
    }

    public int getManufactureId() {
        return manufactureId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public Set<SerialMessageClass> getApiCapabilities() {
        return Collections.unmodifiableSet(apiCapabilities.stream().collect(Collectors.toSet()));
    }
}
