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

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceType;
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
public class GetControllerCapabilitiesMessageClass extends ZWaveCommandProcessor {
    private final Logger logger = LoggerFactory.getLogger(GetControllerCapabilitiesMessageClass.class);

    private final byte CONTROLLER_IS_SECONDARY = 0x01;
    private final byte CONTROLLER_ON_OTHER_NETWORK = 0x02;
    private final byte CONTROLLER_NODEID_SERVER_PRESENT = 0x04;
    private final byte CONTROLLER_IS_REAL_PRIMARY = 0x08;
    private final byte CONTROLLER_IS_SUC = 0x10;

    private boolean isSecondary = false;
    private boolean isOnOtherNetwork = false;
    private boolean isServerPresent = false;
    private boolean isRealPrimary = false;
    private boolean isSUC = false;

    public ZWaveSerialPayload doRequest() {
        logger.debug("Creating GET_CONTROLLER_CAPABILITIES message");

        // Create the request
        return new ZWaveTransactionMessageBuilder(SerialMessageClass.GetControllerCapabilities).build();
    }

    @Override
    public boolean handleResponse(ZWaveController zController, ZWaveTransaction transaction,
            SerialMessage incomingMessage) throws ZWaveSerialMessageException {
        logger.trace("Handle Message Get Controller Capabilities - Length {}",
                incomingMessage.getMessagePayload().length);

        isSecondary = ((incomingMessage.getMessagePayloadByte(0) & CONTROLLER_IS_SECONDARY) != 0) ? true : false;
        isOnOtherNetwork = ((incomingMessage.getMessagePayloadByte(0) & CONTROLLER_ON_OTHER_NETWORK) != 0) ? true
                : false;
        isServerPresent = ((incomingMessage.getMessagePayloadByte(0) & CONTROLLER_NODEID_SERVER_PRESENT) == 1) ? true
                : false;
        isRealPrimary = ((incomingMessage.getMessagePayloadByte(0) & CONTROLLER_IS_REAL_PRIMARY) != 0) ? true : false;
        isSUC = ((incomingMessage.getMessagePayloadByte(0) & CONTROLLER_IS_SUC) != 0) ? true : false;

        logger.debug("Controller is secondary = {}", isSecondary);
        logger.debug("Controller is on other network = {}", isOnOtherNetwork);
        logger.debug("Node ID Server is present = {}", isServerPresent);
        logger.debug("Controller is real primary = {}", isRealPrimary);
        logger.debug("Controller is SUC = {}", isSUC);

        transaction.setTransactionComplete();
        return true;
    }

    public boolean getIsSecondary() {
        return isSecondary;
    }

    public boolean getIsOnOtherNetwork() {
        return isOnOtherNetwork;
    }

    public boolean getIsServerPresent() {
        return isServerPresent;
    }

    public boolean getIsRealPrimary() {
        return isRealPrimary;
    }

    public boolean getIsSUC() {
        return isSUC;
    }

    public ZWaveDeviceType getDeviceType() {
        if (isSecondary) {
            return ZWaveDeviceType.SECONDARY;
        } else {
            if (isSUC) {
                return ZWaveDeviceType.SUC;
            } else {
                return ZWaveDeviceType.PRIMARY;
            }
        }
    }
}
