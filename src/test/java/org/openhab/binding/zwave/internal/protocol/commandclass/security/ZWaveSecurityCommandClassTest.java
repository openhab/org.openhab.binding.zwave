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
package org.openhab.binding.zwave.internal.protocol.commandclass.security;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveSecurityCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.impl.CommandClassSecurityV1;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

public class ZWaveSecurityCommandClassTest {
    private static String TEST_KEY = "01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10";

    // @Test
    public void generateMAC() throws Exception {
        byte[] data = { (byte) 0x5d, 0x5c, (byte) 0xa9 };
        byte[] iv = { (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte) 0xaa,
                (byte) 0xaa, (byte) 0xf3, 0x68, 0x41, (byte) 0xd7, 0x37, (byte) 0xf6, (byte) 0xa5, (byte) 0x94 };
        byte[] expectedBytes = { (byte) 0xeb, 0x3d, (byte) 0xd5, (byte) 0x8c, 0x1e, 0x4c, (byte) 0xde, 0x1e };

        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);
        ZWaveSecurityCommandClass security = new ZWaveSecurityCommandClass(node, controller, endpoint);
        security.setNetworkKey(TEST_KEY);
        // byte[] actualBytes = security.generateMAC((byte) CommandClassSecurityV1.SECURITY_MESSAGE_ENCAPSULATION, data,
        // (byte) 1, (byte) 2, iv);
        // assertTrue(Arrays.equals(expectedBytes, actualBytes));
    }

    @Ignore
    @Test
    public void testEncapsulatePayload() throws Exception {
        byte nodeId = 0x01;
        byte[] payload = { nodeId, 2, (byte) CommandClass.COMMAND_CLASS_SECURITY.getKey(),
                CommandClassSecurityV1.SECURITY_COMMANDS_SUPPORTED_GET, };

        // Create the security class
        ZWaveNode nodeTx = Mockito.mock(ZWaveNode.class);
        Mockito.when(nodeTx.getNodeId()).thenReturn(0x02);
        ZWaveController controllerTx = Mockito.mock(ZWaveController.class);
        Mockito.when(controllerTx.getOwnNodeId()).thenReturn(0x01);

        ZWaveNode nodeRx = Mockito.mock(ZWaveNode.class);
        Mockito.when(nodeRx.getNodeId()).thenReturn(0x01);
        ZWaveController controllerRx = Mockito.mock(ZWaveController.class);
        Mockito.when(controllerRx.getOwnNodeId()).thenReturn(0x02);
        ArgumentCaptor<ZWaveCommandClassTransactionPayload> argumentRx = ArgumentCaptor
                .forClass(ZWaveCommandClassTransactionPayload.class);
        Mockito.doNothing().when(controllerRx).enqueue(argumentRx.capture());

        ZWaveEndpoint endpoint = Mockito.mock(ZWaveEndpoint.class);

        // Create the receive node
        ZWaveSecurityCommandClass securityRx = new ZWaveSecurityCommandClass(nodeRx, controllerRx, endpoint);
        securityRx.setNetworkKey(TEST_KEY);

        // Create the transmit node
        ZWaveSecurityCommandClass securityTx = new ZWaveSecurityCommandClass(nodeTx, controllerTx, endpoint);
        securityTx.setNetworkKey(TEST_KEY);

        // Create the nonce request in the transmit node, and send it to the receive node
        ZWaveCommandClassTransactionPayload nonceGet = securityTx.getSecurityNonceGet();
        securityRx.handleSecurityNonceGet(nonceGet, 0);

        // We should have captured the nonce report
        assertNotNull(argumentRx.getValue());

        // Get a nonce from the receiver and pass it to the transmitter
        securityTx.handleSecurityNonceReport(argumentRx.getValue(), 0);
        assertTrue(securityTx.isNonceAvailable());

        // Now encapsulate our message
        byte[] request = securityTx.getSecurityMessageEncapsulation(payload);

        byte[] response = securityRx.getSecurityMessageDecapsulation(request);

        assertTrue(Arrays.equals(payload, response));
    }
}
