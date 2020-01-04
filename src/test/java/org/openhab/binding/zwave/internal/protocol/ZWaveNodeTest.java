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
package org.openhab.binding.zwave.internal.protocol;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiInstanceCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test for {@link ZWaveNode}
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ZWaveNodeTest {
    @Test
    public void setAssociation() {
        ZWaveController controller = null;
        ZWaveEndpoint endpoint = null;
        ZWaveNode node = new ZWaveNode(1, 2, controller);
        node.addCommandClass(new ZWaveAssociationCommandClass(node, controller, endpoint));
        node.addCommandClass(new ZWaveMultiAssociationCommandClass(node, controller, endpoint));

        ZWaveCommandClassTransactionPayload msg;
        byte[] expectedResponse;

        // Setting device endpoint null and receive endpoint 0 should use single instance when only 1 endpoint in the
        // node
        expectedResponse = new byte[] { -123, 1, 0, 5 };
        msg = node.setAssociation(0, new ZWaveAssociation(5, 0));
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 0 should use single instance when only 1 endpoint in the
        // node
        expectedResponse = new byte[] { -123, 1, 0, 5 };
        msg = node.setAssociation(0, new ZWaveAssociation(5, 1));
        byte[] a = msg.getPayloadBuffer();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 0 should use multi instance when more than 1 endpoint
        expectedResponse = new byte[] { -114, 1, 0, 5 };
        node.addEndpoint(1);
        msg = node.setAssociation(0, new ZWaveAssociation(5, 0));
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 1 should use multi instance
        expectedResponse = new byte[] { -114, 1, 0, 0, 5, 1 };
        msg = node.setAssociation(0, new ZWaveAssociation(5, 1));
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 0 should use single instance
        expectedResponse = new byte[] { -114, 1, 0, 5 };
        msg = node.setAssociation(0, new ZWaveAssociation(5));
        byte[] x = msg.getPayloadBuffer();
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }

    private List<ZWaveCommandClassPayload> processCommand(byte[] data) {
        ZWaveCommandClassPayload payload = new ZWaveCommandClassPayload(data);

        ZWaveController controller = null;
        ZWaveEndpoint endpoint = null;
        ZWaveNode node = new ZWaveNode(1, 2, controller);
        node.addCommandClass(new ZWaveMultiInstanceCommandClass(node, controller, endpoint));

        return node.processCommand(payload);
    }

    @Test
    public void testMultiChannelShortFrame() {
        List<ZWaveCommandClassPayload> response = processCommand(new byte[] { 0x60, 0x0D, 0x01 });

        assertNull(response);
    }
}
