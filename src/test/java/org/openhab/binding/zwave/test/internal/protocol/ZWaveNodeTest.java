package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiAssociationCommandClass;
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

        // Setting device endpoint null and receive endpoint 0 should use multi instance
        expectedResponse = new byte[] { -114, 1, 0, 5 };
        msg = node.setAssociation(null, 0, 5, 0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 1 should use multi instance
        expectedResponse = new byte[] { -114, 1, 0, 0, 5, 1 };
        msg = node.setAssociation(null, 0, 5, 1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }
}
