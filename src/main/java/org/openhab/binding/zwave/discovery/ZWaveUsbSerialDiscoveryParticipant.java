/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.discovery;

import static java.util.Arrays.asList;
import static org.openhab.binding.zwave.ZWaveBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDeviceInformation;
import org.eclipse.smarthome.config.discovery.usbserial.UsbSerialDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * Discovery for ZWave USB dongles, integrated in Eclipse SmartHome's USB-serial discovery by implementing
 * a component of type {@link UsbSerialDiscoveryParticipant}.
 * <p/>
 * Currently, this {@link UsbSerialDiscoveryParticipant} supports two generic type dongles:
 * ZWave and ZWave Plus USB dongles.
 *
 * @author Aitor Iturrioz - initial contribution
 */
@Component(service = UsbSerialDiscoveryParticipant.class)
public class ZWaveUsbSerialDiscoveryParticipant implements UsbSerialDiscoveryParticipant {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(asList(CONTROLLER_SERIAL));

    public static final int ZWAVE_USB_DONGLE_VENDOR_ID = 0x10c4;
    public static final int ZWAVE_USB_DONGLE_PRODUCT_ID = 0xea60;
    public static final String ZWAVE_USB_DONGLE_DEFAULT_LABEL = "ZWave USB Dongle";

    public static final int ZWAVE_PLUS_USB_DONGLE_VENDOR_ID = 0x0658;
    public static final int ZWAVE_PLUS_USB_DONGLE_PRODUCT_ID = 0x0200;
    public static final String ZWAVE_PLUS_USB_DONGLE_DEFAULT_LABEL = "ZWave Plus USB Dongle";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public @Nullable DiscoveryResult createResult(UsbSerialDeviceInformation deviceInformation) {
        if (isZWaveUSBDongle(deviceInformation)) {
            return DiscoveryResultBuilder.create(createSerialControllerThingType(deviceInformation))
                    .withLabel(createZWaveUSBDongleLabel(deviceInformation))
                    .withRepresentationProperty(CONFIGURATION_PORT)
                    .withProperty(CONFIGURATION_PORT, deviceInformation.getSerialPort()).build();
        } else if (isZWavePlusUSBDongle(deviceInformation)) {
            return DiscoveryResultBuilder.create(createSerialControllerThingType(deviceInformation))
                    .withLabel(createZWavePlusUSBDongleLabel(deviceInformation))
                    .withRepresentationProperty(CONFIGURATION_PORT)
                    .withProperty(CONFIGURATION_PORT, deviceInformation.getSerialPort()).build();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(UsbSerialDeviceInformation deviceInformation) {
        if (isZWaveUSBDongle(deviceInformation) || isZWavePlusUSBDongle(deviceInformation)) {
            return createSerialControllerThingType(deviceInformation);
        } else {
            return null;
        }
    }

    private ThingUID createSerialControllerThingType(UsbSerialDeviceInformation deviceInformation) {
        if (deviceInformation.getSerialNumber() != null) {
            return new ThingUID(CONTROLLER_SERIAL, deviceInformation.getSerialNumber());
        } else {
            return new ThingUID(CONTROLLER_SERIAL, String.valueOf(deviceInformation.getProductId()));
        }
    }

    private boolean isZWaveUSBDongle(UsbSerialDeviceInformation deviceInformation) {
        return deviceInformation.getVendorId() == ZWAVE_USB_DONGLE_VENDOR_ID
                && deviceInformation.getProductId() == ZWAVE_USB_DONGLE_PRODUCT_ID;
    }

    private @Nullable String createZWaveUSBDongleLabel(UsbSerialDeviceInformation deviceInformation) {
        if (deviceInformation.getProduct() != null && !deviceInformation.getProduct().isEmpty()) {
            return String.format("%s (%s)", ZWAVE_USB_DONGLE_DEFAULT_LABEL, deviceInformation.getProduct());
        } else {
            return ZWAVE_USB_DONGLE_DEFAULT_LABEL;
        }
    }

    private boolean isZWavePlusUSBDongle(UsbSerialDeviceInformation deviceInformation) {
        return deviceInformation.getVendorId() == ZWAVE_PLUS_USB_DONGLE_VENDOR_ID
                && deviceInformation.getProductId() == ZWAVE_PLUS_USB_DONGLE_PRODUCT_ID;
    }

    private @Nullable String createZWavePlusUSBDongleLabel(UsbSerialDeviceInformation deviceInformation) {
        if (deviceInformation.getProduct() != null && !deviceInformation.getProduct().isEmpty()) {
            return String.format("%s (%s)", ZWAVE_PLUS_USB_DONGLE_DEFAULT_LABEL, deviceInformation.getProduct());
        } else {
            return ZWAVE_PLUS_USB_DONGLE_DEFAULT_LABEL;
        }
    }
}