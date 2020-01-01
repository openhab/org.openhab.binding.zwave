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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction;

public class ApplicationUpdateMessageClassTest {
    @Test
    public void NodeInfoRequestFailed() {
        SerialMessage incomingMessage = new SerialMessage(
                new byte[] { 0x01, 0x06, 0x00, 0x49, (byte) 0x81, 0x00, 0x00, 0x31 });

        ZWaveSerialPayload payload = new RequestNodeInfoMessageClass().doRequest(10);
        ZWaveTransaction transaction = new ZWaveTransaction(payload);
        ApplicationUpdateMessageClass appUpdate = new ApplicationUpdateMessageClass();
        try {
            appUpdate.handleRequest(null, transaction, incomingMessage);
        } catch (ZWaveSerialMessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(false);
        }

        assertTrue(appUpdate.correlateTransactionResponse(transaction, incomingMessage));
    }

}
