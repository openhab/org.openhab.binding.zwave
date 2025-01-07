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
package org.openhab.binding.zwave.internal.protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

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
