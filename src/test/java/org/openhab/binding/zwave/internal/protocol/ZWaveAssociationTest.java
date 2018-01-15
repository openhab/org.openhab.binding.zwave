/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

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
