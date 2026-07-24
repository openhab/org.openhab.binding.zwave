/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Supervision command class.
 *
 * @author Robert Eckhoff - Initial contribution
 */
@XStreamAlias("COMMAND_CLASS_SUPERVISION")
public class ZWaveSupervisionCommandClass extends ZWaveCommandClass {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveSupervisionCommandClass.class);

    public static final int SUPERVISION_GET = 0x01;
    public static final int SUPERVISION_REPORT = 0x02;
    public static final int SUPERVISION_STATUS_NO_SUPPORT = 0x00;
    public static final int SUPERVISION_STATUS_WORKING = 0x01;
    public static final int SUPERVISION_STATUS_FAIL = 0x02;
    public static final int SUPERVISION_STATUS_SUCCESS = 0xFF;

    private static final int REQUEST_UPDATES_BIT = 0x80;
    private static final int MORE_UPDATES_FOLLOW_BIT = 0x40;

    public ZWaveSupervisionCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
        this.versionMax = 2;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_SUPERVISION;
    }

    public ZWaveCommandClassTransactionPayload getSupervisionGetEncapMessage(
            ZWaveCommandClassTransactionPayload transactionPayload, int sessionId, boolean requestStatusUpdates) {
        ByteArrayOutputStream newPayload = new ByteArrayOutputStream();
        newPayload.write(getCommandClass().getKey());
        newPayload.write(SUPERVISION_GET);
        newPayload.write((requestStatusUpdates ? REQUEST_UPDATES_BIT : 0) | (sessionId & 0x3F));
        newPayload.write(transactionPayload.getPayloadLength());

        try {
            newPayload.write(transactionPayload.getPayloadBuffer());
            ZWaveCommandClassTransactionPayload supervisionTransaction = new ZWaveCommandClassTransactionPayload(
                    transactionPayload.getNodeId(), newPayload.toByteArray(), transactionPayload.getPriority(),
                    CommandClass.COMMAND_CLASS_SUPERVISION, SUPERVISION_REPORT);

            supervisionTransaction.setRequiresResponse(transactionPayload.getRequiresResponse());
            supervisionTransaction.setMaxAttempts(transactionPayload.getMaxAttempts());
            if (transactionPayload.getRequiresSecurity()) {
                supervisionTransaction.setRequiresSecurity();
            }

            return supervisionTransaction;
        } catch (IOException e) {
            logger.debug("NODE {}: Error encapsulating supervision message", getNode().getNodeId(), e);
        }

        return null;
    }

    @ZWaveResponseHandler(id = SUPERVISION_REPORT, name = "SUPERVISION_REPORT")
    public void handleSupervisionReport(ZWaveCommandClassPayload payload, int endpoint)
            throws ZWaveSerialMessageException {
        if (payload.getPayloadLength() < 4) {
            logger.debug("NODE {}: Supervision report payload too short", getNode().getNodeId());
            return;
        }

        int properties = payload.getPayloadByte(2);
        boolean moreUpdatesFollow = (properties & MORE_UPDATES_FOLLOW_BIT) != 0;
        int sessionId = properties & 0x3F;
        int status = payload.getPayloadByte(3);
        int duration = payload.getPayloadLength() > 4 ? payload.getPayloadByte(4) : 0;

        logger.debug(
                "NODE {}: Supervision report endpoint={}, session={}, status=0x{}, moreUpdatesFollow={}, duration={}",
                getNode().getNodeId(), endpoint, sessionId, Integer.toHexString(status), moreUpdatesFollow, duration);

        getNode().handleSupervisionReport(sessionId, status, moreUpdatesFollow, endpoint);
    }
}
