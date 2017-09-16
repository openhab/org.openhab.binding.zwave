package org.openhab.binding.zwave.test.internal.protocol;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociation;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;

public class ZWaveAssociationGroupTest {
    @Test
    public void TestAssociationGroup() {
        ZWaveAssociationGroup group = new ZWaveAssociationGroup(1);

        group.addAssociation(1);
        assertEquals(1, group.getAssociationCnt());
        assertTrue(group.isAssociated(1));
        assertFalse(group.isAssociated(1, 0));

        group.addAssociation(1);
        assertEquals(1, group.getAssociationCnt());

        group.addAssociation(1, 0);
        assertEquals(2, group.getAssociationCnt());

        group.addAssociation(3, 2);
        assertEquals(3, group.getAssociationCnt());
    }

    @Test
    public void testIsAssociated() {
        ZWaveAssociationGroup group = new ZWaveAssociationGroup(1);
        group.addAssociation(1);
        assertTrue(group.isAssociated(1));
        assertFalse(group.isAssociated(2));
        assertFalse(group.isAssociated(1, 0));
        assertFalse(group.isAssociated(1, 2));

        ZWaveAssociation association = new ZWaveAssociation(1);
        assertTrue(group.isAssociated(association));

        association = new ZWaveAssociation(1, 1);
        assertFalse(group.isAssociated(association));

        group.addAssociation(1, 1);
        association = new ZWaveAssociation(1, 1);
        assertTrue(group.isAssociated(association));
    }

    @Test
    public void testIsProfileKnown() {
        ZWaveAssociationGroup group = new ZWaveAssociationGroup(1);
        assertFalse(group.isProfileKnown());

        group.setProfile1(0);
        group.setProfile2(0);
        assertTrue(group.isProfileKnown());
    }
}
