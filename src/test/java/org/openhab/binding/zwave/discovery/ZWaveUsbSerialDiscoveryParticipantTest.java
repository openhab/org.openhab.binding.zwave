/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.discovery;

import static org.junit.Assert.*;
import static org.openhab.binding.zwave.ZWaveBindingConstants.*;
import static org.openhab.binding.zwave.discovery.ZWaveUsbSerialDiscoveryParticipant.*;

import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ZWaveUsbSerialDiscoveryParticipant}.
 */
public class ZWaveUsbSerialDiscoveryParticipantTest {

    private ZWaveUsbSerialDiscoveryParticipant discoveryParticipant;

    @Before
    public void setup() {
        discoveryParticipant = new ZWaveUsbSerialDiscoveryParticipant();
    }

    /**
     * If only USB vendor ID or only USB product ID or none of them matches, then no device is discovered.
     */
    @Test
    public void testAeotecZStickGen5DongleNotDiscovered() {
        assertNull(discoveryParticipant.getThingUID(forUsbDongle(ZWAVE_PLUS_USB_DONGLE_VENDOR_ID, 0x1234)));
        assertNull(discoveryParticipant.getThingUID(forUsbDongle(0xabcd, ZWAVE_PLUS_USB_DONGLE_PRODUCT_ID)));
        assertNull(discoveryParticipant.getThingUID(forUsbDongle(0xabcd, 0x1234)));

        assertNull(discoveryParticipant.createResult(forUsbDongle(ZWAVE_PLUS_USB_DONGLE_VENDOR_ID, 0x1234)));
        assertNull(discoveryParticipant.createResult(forUsbDongle(0xabcd, ZWAVE_PLUS_USB_DONGLE_PRODUCT_ID)));
        assertNull(discoveryParticipant.createResult(forUsbDongle(0xabcd, 0x1234)));
    }

    /**
     * For matching USB vendor and product ID, a suitable thingUID is returned.
     */
    @Test
    public void testAeotecZStickGen5DongleDiscoveredThingUID() {
        ThingUID thingUID = discoveryParticipant.getThingUID(forUsbDongle(ZWAVE_PLUS_USB_DONGLE_VENDOR_ID,
                ZWAVE_PLUS_USB_DONGLE_PRODUCT_ID, "serial", "/dev/ttyUSB0"));

        assertNotNull(thingUID);
        assertEquals(thingUID, new ThingUID(CONTROLLER_SERIAL, "serial"));
    }

    /**
     * For matching USB vendor and product ID, a suitable discovery result is returned.
     */
    @Test
    public void testAeotecZStickGen5DongleDiscoveredDiscoveryResult() {
        DiscoveryResult discoveryResult = discoveryParticipant.createResult(
                forUsbDongle(ZWAVE_PLUS_USB_DONGLE_VENDOR_ID, ZWAVE_PLUS_USB_DONGLE_PRODUCT_ID, null, "/dev/ttyUSB0"));

        assertNotNull(discoveryResult);
        assertEquals(discoveryResult.getThingUID(),
                new ThingUID(CONTROLLER_SERIAL, String.valueOf(ZWAVE_PLUS_USB_DONGLE_PRODUCT_ID)));
        assertNotNull(discoveryResult.getLabel());
        assertEquals(discoveryResult.getRepresentationProperty(), CONFIGURATION_PORT);
        assertEquals(discoveryResult.getProperties().get(CONFIGURATION_PORT), "/dev/ttyUSB0");
    }

    private UsbSerialDeviceInformation forUsbDongle(int vendorId, int productId, String serial, String device) {
        return new UsbSerialDeviceInformation(vendorId, productId, serial, null, null, 0, null, device);
    }

    private UsbSerialDeviceInformation forUsbDongle(int vendorId, int productId) {
        return new UsbSerialDeviceInformation(vendorId, productId, null, null, null, 0, null, "/dev/ttyUSB0");
    }

}
