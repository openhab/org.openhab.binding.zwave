---
layout: documentation
title: HKZW_SO03 - ZWave
---

{% include base.html %}

# HKZW_SO03 Smart Plug
This describes the Z-Wave device *HKZW_SO03*, manufactured by *[Hank](http://www.hank-tech.com/)* with the thing type UID of ```hank_hkzwso03_00_000```.

The device is in the category of *Power Outlet*, defining Small devices to be plugged into a power socket in a wall which stick there.

![HKZW_SO03 product image](https://opensmarthouse.org/zwavedatabase/677/image/)


The HKZW_SO03 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtWELL DONE!<br /&gt You have picked the finest solution for your smart home, congratulations!<br /&gt Now it's time to enjoy these great features of your new Smart Plug:</p&gt <ul&gt<li&gtWireless control of floor lamps and small appliances</li&gt <li&gtEnergy monitoring in live mode or over time (your Z-Wave gateway needs to support this feature)</li&gt <li&gtScene inclusion for custom automation scenarios when included to a Z-Wave gateway controller</li&gt <li&gtLED indicator displaying Z-Wave signal strength and power usage</li&gt <li&gtSecurity enabled Z-Wave device with AES signal encryption (requires a security enabled gateway controller for full functionality)</li&gt <li&gtZ-Wave Plus with improved 500 chip for faster and safer wireless communication</li&gt <li&gtBuilt-in range extender for a stronger, more reliable network</li&gt <li&gt2 USB charging ports for your smart phone or tablet (no Z-Wave functionality)</li&gt <li&gtBuilt-in overload protection in Z-Wave outlet and USB ports</li&gt </ul&gt

### Inclusion Information

<p&gtAuto Z-Wave Inclusion</p&gt <ol&gt<li&gtPut your Z-Wave controller in inclusion mode</li&gt <li&gtPlug the smart plug into a grounded receptacle located within 3 feet from your Z-Wave gateway controller</li&gt <li&gtLED indicator will flash blue</li&gt <li&gtA new on / off device should be recognized by your Z-Wave controller</li&gt </ol&gt<p&gtManual Z-Wave Inclusion</p&gt <ol&gt<li&gt Plug the smart plug into a grounded receptacle located within 3 feet from your Z-Wave gateway controller</li&gt <li&gtPut your Z-Wave controller in inclusion mode</li&gt <li&gtPress and release the Z-Wave button 3 TIMES QUICKLY</li&gt <li&gtLED indicator will flash blue</li&gt <li&gtA new on / off device should be recognized by your Z-Wave controller</li&gt </ol&gt<p&gtSecure Z-Wave Inclusion</p&gt <ol&gt<li&gt Plug the smart plug into a grounded receptacle located within 3 feet from your Z-Wave gateway controller</li&gt <li&gtPut your Z-Wave controller in secure inclusion mode</li&gt <li&gtPress and HOLD the Z-Wave button for at least 3 seconds</li&gt <li&gtLED indicator will flash green</li&gt <li&gtLED indicator will turn solid green once the inclusion is completed. Please allow 5 minutes for configuration. DO NOT press any buttons or move the device during that time</li&gt <li&gtA new secure on / off device should be recognized by your Z-Wave controller</li&gt </ol&gt

### Exclusion Information

<ol&gt<li&gtPlug the smart plug into a grounded receptacle located within 3 feet from your Z-Wave gateway controller</li&gt <li&gtPut your Z-Wave controller in exclusion mode</li&gt <li&gtPress and release the Z-Wave button 3 TIMES QUICKLY</li&gt <li&gtLED indicator will flash orange</li&gt <li&gtThe smart plug should disappear from your controller's device list and LED indicator will remain solid orange for a few seconds to indicate exclusion</li&gt </ol&gt<p&gtPlease repeat the process following all steps carefully if the first attempt is unsuccessful</p&gt

## Channels

The following table summarises the channels available for the HKZW_SO03 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 
| Scene Number | scene_number | scene_number |  | Number | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Electric meter (volts) | meter_voltage | meter_voltage | Energy | Number | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Electric meter (amps) | meter_current | meter_current | Energy | Number | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Scene Number
Triggers when a scene button is pressed.

The ```scene_number``` channel is of type ```scene_number``` and supports the ```Number``` item.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (volts)
Indicates the instantaneous voltage.

The ```meter_voltage``` channel is of type ```meter_voltage``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (amps)
Indicates the instantaneous current consumption.

The ```meter_current``` channel is of type ```meter_current``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.



## Device Configuration

The following table provides a summary of the 4 configuration parameters available in the HKZW_SO03.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 20 | Overload Protection | Overload Protection |
| 21 | On/Off Status Recovery After Power Failure | On/Off Status Recovery After Power Failure |
| 24 | On/Off Status Change Notifications | On/Off Status Change Notifications |
| 27 | Led Indicator Control | Led Indicator Control |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 20: Overload Protection

Overload Protection
<p&gt16.5A for over 5 seconds when this setting is enabled.</p&gt <p&gtWe <strong&gtDO NOT</strong&gt recommend changing this parameter’s value as it may result in device damage and malfunction.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Disabled |
| 1 | Enabled (default) |

The manufacturer defined default value is ```1``` (Enabled (default)).

This parameter has the configuration ID ```config_20_1``` and is of type ```INTEGER```.


### Parameter 21: On/Off Status Recovery After Power Failure

On/Off Status Recovery After Power Failure
<p&gtChoose the recovery state for your Smart Plug if power outage occurs.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Remember Status |
| 1 | Turn ON (default) |
| 2 | Turn OFF |

The manufacturer defined default value is ```0``` (Remember Status).

This parameter has the configuration ID ```config_21_1``` and is of type ```INTEGER```.


### Parameter 24: On/Off Status Change Notifications

On/Off Status Change Notifications
<p&gtYour Smart Plug will automatically send a notification to the controller and other associated devices if its status changes from on to off or the other way round.</p&gt <p&gtChoose when you want it to send the report.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Disabled |
| 1 | Always Send (default) |
| 2 | Only Manual Changes |

The manufacturer defined default value is ```1``` (Always Send (default)).

This parameter has the configuration ID ```config_24_1``` and is of type ```INTEGER```.


### Parameter 27: Led Indicator Control

Led Indicator Control
<p&gtYou can choose how the LED indicator displays power consumption on the smart plug</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Always On (default) |
| 1 | 5 Seconds |

The manufacturer defined default value is ```0``` (Always On (default)).

This parameter has the configuration ID ```config_27_1``` and is of type ```INTEGER```.

### Switch All Mode

Set the mode for the switch when receiving SWITCH ALL commands.

The following option values may be configured -:
| Value  | Description |
|--------|-------------|
| 0 | Exclude from All On and All Off groups |
| 1 | Include in All On group |
| 2 | Include in All Off group |
| 255 | Include in All On and All Off groups |

This parameter has the configuration ID ```switchall_mode``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The HKZW_SO03 supports 1 association group.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.

Association group 1 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| Linked to BASIC|
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_SCENE_ACTIVATION_V1| |
| COMMAND_CLASS_SCENE_ACTUATOR_CONF_V1| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |

### Documentation Links

* [Zooz ZEN06 Manual](https://opensmarthouse.org/zwavedatabase/677/Zooz-Z-Wave-Smart-Plug-ZEN06-Manual.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/677).
