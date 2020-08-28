---
layout: documentation
title: Z-BRDG-433 - ZWave
---

{% include base.html %}

# Z-BRDG-433 Plug in Z-Wave to 433Mhz bridge plus lamp dimmer
This describes the Z-Wave device *Z-BRDG-433*, manufactured by *[Vision Security](http://www.visionsecurity.com.tw/)* with the thing type UID of ```vision_2gigzbrdg433_00_000```.

The device is in the category of *Power Outlet*, defining Small devices to be plugged into a power socket in a wall which stick there.

![Z-BRDG-433 product image](https://opensmarthouse.org/zwavedatabase/978/image/)


The Z-BRDG-433 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtThis GoControl (formerly 2GIG) Plug-in Lamp Module is integrates with other Z-Wave products. It can also act as a wireless repeater to ensure that commands intended for another device in the network are received (useful when a device would otherwise be out of radio range).</p&gt <ul&gt<li&gtOne-controlled outlet for control and dimming of lamps (Z-Wave hub required)</li&gt <li&gtWorks with many Z-Wave controllers, including SmartThings, WINK, FIBARO Home Center, Vera, and many more!</li&gt <li&gtFor indoor use only</li&gt <li&gtMay be packaged referencing 2GIG Energy Bridge; device sold as a Z-Wave lamp dimmer only</li&gt </ul&gt<p&gt<strong&gtSPECIFICATIONS</strong&gt</p&gt <ul&gt<li&gtSignal (Frequency): 908.42 MHz</li&gt <li&gtMaximum Load (Incandescent): 300W maximum, 120 VAC</li&gt <li&gtRange: Up to 100 feet between the wireless controller and/or the closest Z-Wave device</li&gt <li&gt120 VAC, 60 H</li&gt </ul&gt

### Inclusion Information

<p&gtWhen the device is unpaired, press the button on the front till the relay inside clicks to put it into Zwave pairing mode.</p&gt

### Exclusion Information

<p&gtAt this time I have no idea how to exclude this device, If I figure it out I will update this.</p&gt

### General Usage Information

<p&gtWorks as a plug in lamp dimmer for a 2 wire lamp.</p&gt <p&gtRequires to plugged into a grounded outlet.</p&gt <p&gtAlso acts as a Zwave to 433Mhz bridge for connection to various energy monitoring devices</p&gt <p&gtSold as 2GIG, Vision Security, Norteck Security, and Go Control as far as I can see.</p&gt <p&gtSeems to be intended for installers as finding information on it's use with Zwave is quite difficult.</p&gt

## Channels

The following table summarises the channels available for the Z-BRDG-433 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Dimmer | switch_dimmer | switch_dimmer | DimmableLight | Dimmer | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 

### Dimmer
The brightness channel allows to control the brightness of a light.
            It is also possible to switch the light on and off.

The ```switch_dimmer``` channel is of type ```switch_dimmer``` and supports the ```Dimmer``` item and is in the ```DimmableLight``` category.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.



## Device Configuration

The device has no configuration parameters defined.

## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The device does not support associations.
## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V1| |
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_METER_V2| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_PROTECTION_V1| |
| COMMAND_CLASS_VERSION_V1| |

### Documentation Links

* [Manual for 2GIG version of this](https://opensmarthouse.org/zwavedatabase/978/2GIG-Z-BRDG-433-Manual.pdf)
* [Sell sheet for 2GIG version](https://opensmarthouse.org/zwavedatabase/978/NPA-Energy-Bridge-1.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/978).
