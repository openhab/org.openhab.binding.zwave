package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociation;

/**
 * Tests for the Association group
 *
 * @author Chris Jackson
 *
 */
public class ZWaveAssociationTest {
    @Test
    public void testAssociationEquals() {
        ZWaveAssociation association1 = new ZWaveAssociation(1);
        ZWaveAssociation association2 = new ZWaveAssociation(1);
        assertTrue(association1.equals(association2));

        association1 = new ZWaveAssociation(1);
        association2 = new ZWaveAssociation(2);
        assertFalse(association1.equals(association2));

        association1 = new ZWaveAssociation(1);
        association2 = new ZWaveAssociation(1, 0);
        assertFalse(association1.equals(association2));

        association1 = new ZWaveAssociation(1, 0);
        association2 = new ZWaveAssociation(1, 1);
        assertFalse(association1.equals(association2));
    }
}
