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
package org.openhab.binding.zwave.internal;

import static org.junit.Assert.*;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Test;
import org.openhab.binding.zwave.internal.ZWaveProduct;

/**
 * Test product matches
 *
 * @author Chris Jackson
 *
 */
public class ZWaveProductTest {

    @Test
    public void testVersion() {
        ZWaveProduct product = new ZWaveProduct(new ThingTypeUID("xx:yy"), 1, 2, 3, "1.2", "1.7");

        assertTrue(product.match(1, 2, 3, "1.2"));
        assertTrue(product.match(1, 2, 3, "1.5"));
        assertTrue(product.match(1, 2, 3, "1.7"));

        assertFalse(product.match(2, 2, 3, "1.5"));
        assertFalse(product.match(1, 3, 3, "1.5"));
        assertFalse(product.match(1, 2, 2, "1.5"));
        assertFalse(product.match(2, 2, 3, "1.1"));
        assertFalse(product.match(2, 2, 3, "1.11"));
    }

    @Test
    public void testNoVersion() {
        ZWaveProduct product = new ZWaveProduct(new ThingTypeUID("xx:yy"), 1, 2, 3, null, null);

        assertTrue(product.match(1, 2, 3, "0.0"));
        assertTrue(product.match(1, 2, 3, "99.5"));
        assertTrue(product.match(1, 2, 3, "255.255"));
    }
}
