/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
