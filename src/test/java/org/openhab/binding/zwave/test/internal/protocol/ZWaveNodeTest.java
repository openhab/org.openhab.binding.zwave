package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiAssociationCommandClass;

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

        SerialMessage msg;
        byte[] expectedResponse;

        // Setting device endpoint null and receive endpoint 0 should use single instance
        expectedResponse = new byte[] { 1, 11, 0, 19, 2, 4, -123, 1, 0, 5, 0, 0, 96 };
        msg = node.setAssociation(null, 0, 5, 0).getSerialMessage();
        msg.setCallbackId(0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));

        // Setting device endpoint null and receive endpoint 1 should use multi instance
        expectedResponse = new byte[] { 1, 13, 0, 19, 2, 6, -114, 1, 0, 0, 5, 1, 0, 0, 110 };
        msg = node.setAssociation(null, 0, 5, 1).getSerialMessage();
        msg.setCallbackId(0);
        assertTrue(Arrays.equals(msg.getMessageBuffer(), expectedResponse));
    }
}
