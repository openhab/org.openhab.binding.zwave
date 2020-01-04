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

import org.junit.Test;

public class ZWaveAssociationGroupTest {
    @Test
    public void TestAssociationGroup() {
        ZWaveAssociationGroup group = new ZWaveAssociationGroup(1);

        group.addAssociation(new ZWaveAssociation(1));
        assertEquals(1, group.getAssociationCnt());
        assertTrue(group.isAssociated(new ZWaveAssociation(1)));
        assertFalse(group.isAssociated(new ZWaveAssociation(1, 0)));

        group.addAssociation(new ZWaveAssociation(1));
        assertEquals(1, group.getAssociationCnt());

        group.removeAssociation(new ZWaveAssociation(1));
        assertEquals(0, group.getAssociationCnt());
        group.addAssociation(new ZWaveAssociation(1));

        group.addAssociation(new ZWaveAssociation(1, 0));
        assertEquals(2, group.getAssociationCnt());

        group.addAssociation(new ZWaveAssociation(3, 2));
        assertEquals(3, group.getAssociationCnt());

        group.removeAssociation(new ZWaveAssociation(3, 2));
        assertEquals(2, group.getAssociationCnt());
    }

    @Test
    public void testIsAssociated() {
        ZWaveAssociationGroup group = new ZWaveAssociationGroup(1);
        group.addAssociation(new ZWaveAssociation(1));
        assertTrue(group.isAssociated(new ZWaveAssociation(1)));
        assertFalse(group.isAssociated(new ZWaveAssociation(2)));
        assertFalse(group.isAssociated(new ZWaveAssociation(1, 0)));
        assertFalse(group.isAssociated(new ZWaveAssociation(1, 2)));

        ZWaveAssociation association = new ZWaveAssociation(1);
        assertTrue(group.isAssociated(association));

        association = new ZWaveAssociation(1, 1);
        assertFalse(group.isAssociated(association));

        group.addAssociation(new ZWaveAssociation(1, 1));
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
