---
layout: documentation
title: ZV-9101 - ZWave
---

{% include base.html %}

# ZV-9101 In-Wall Dimmer Module
This describes the Z-Wave device *ZV-9101*, manufactured by *[Sunricher](www.sunricher.com)* with the thing type UID of ```sunricher_zv9101pushdim_00_000```.

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![ZV-9101 product image](https://opensmarthouse.org/zwavedatabase/1058/image/)


The ZV-9101 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtZ-Wave Built-in receiver with dimmer function. Control input, AC 230V / DC 12V, for Pushdim. Max 400 W resistive load, 200 W LED. Front edge / Rear dimmer, adjustable lowest level. Compatible with Nexa Bridge and other Z-Wave controllers.</p&gt

### Inclusion Information

<ul&gt<li&gtPress the "Reset" button 3 times within 3 seconds or</li&gt <li&gtpress 3 times on connected switch within 3 seconds</li&gt </ul&gt

### Exclusion Information

<ul&gt<li&gtSet the Z-Wave controller in the exclusion mode (see controller's manual)</li&gt <li&gtPress the "Reset" button 3 times within 3 seconds</li&gt </ul&gt

### General Usage Information

<p&gtControl with retractive switch: Press once to turn on. Press again to turn off. To dim, keep the button pressed in when the light is on. The light comes on with the luminance it had when it was turned off.</p&gt

## Channels

The following table summarises the channels available for the ZV-9101 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Dimmer | switch_dimmer | switch_dimmer | DimmableLight | Dimmer | 
| Scene Number | scene_number | scene_number |  | Number | 
| Alarm (system) | alarm_system | alarm_system |  | Switch | 

### Dimmer
The brightness channel allows to control the brightness of a light.
            It is also possible to switch the light on and off.

The ```switch_dimmer``` channel is of type ```switch_dimmer``` and supports the ```Dimmer``` item and is in the ```DimmableLight``` category.

### Scene Number
Triggers when a scene button is pressed.

The ```scene_number``` channel is of type ```scene_number``` and supports the ```Number``` item.

### Alarm (system)
Indicates if a system alarm is triggered.

The ```alarm_system``` channel is of type ```alarm_system``` and supports the ```Switch``` item. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |



## Device Configuration

The following table provides a summary of the 6 configuration parameters available in the ZV-9101.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 2 | Saving load state | Saving load state before power failure |
| 3 | Enable/disable to send report  | basic report to the Lifeline when the load state changed |
| 4 | Default fade time | Default fade time (unit is second) |
| 5 | brightness value | Setting minimum brightness value |
| 6 | MOSFET | Choose MOSFET |
| 7 | removal from network | Enable/disable external switch to be added to and removed from a network |

### Parameter 2: Saving load state

Saving load state before power failure
<p&gtInfo: Saving load state before power failure 0 – shutoff load 1 – turn on load 2 – save load state before power failure</p&gt
Values in the range 0 to 2 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_2_1``` and is of type ```INTEGER```.


### Parameter 3: Enable/disable to send report 

basic report to the Lifeline when the load state changed
<p&gtEnable/disable to send the basic report to the Lifeline when the load state changed (When value set as 1, re-power on the dimmer, it will send Basic report automatically)</p&gt <p&gt0 – Disable to send Basic report</p&gt <p&gt1 – Enable to send Basic report</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_3_1``` and is of type ```INTEGER```.


### Parameter 4: Default fade time

Default fade time (unit is second)
<p&gtDefault fade time (unit is second, this value has the same function as Duration of Multilevel) Valid value: 0x00 - 0xFF</p&gt <table&gt<tr&gt<td&gtValue</td&gt <td&gtDescription</td&gt </tr&gt<tr&gt<td&gt0-127</td&gt <td&gt1 second to 127 seconds in 1 second resolution</td&gt </tr&gt<tr&gt<td&gt128-254</td&gt <td&gt1 minute to 127 minutes in 1 minute resolution</td&gt </tr&gt<tr&gt<td&gt255</td&gt <td&gtFactory default duration. (1 sec.)</td&gt </tr&gt</table&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_4_1``` and is of type ```INTEGER```.


### Parameter 5: brightness value

Setting minimum brightness value
<p&gtSetting minimum brightness value Valid value: 0 - 50, the bigger the value is, the higher the load’s minimum brightness is Note: when configure this value, the value of Multilevel will be configured as 1 automatically so that users can preview the corre-sponding effect of minimum brightness value</p&gt
Values in the range 0 to 50 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_5_1``` and is of type ```INTEGER```.


### Parameter 6: MOSFET

Choose MOSFET
<p&gtChoose MOSFET driving type 0 – trailing edge 1 – leading edge</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_6_1``` and is of type ```INTEGER```.


### Parameter 7: removal from network

Enable/disable external switch to be added to and removed from a network
<p&gtEnable/disable external switch to be added to and removed from a network (when enables this function, triple press the external switch within 1.5 seconds to be added to or removed from a net-work) 0 – disable 1 - enable</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_7_1``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The ZV-9101 supports 1 association group.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
Emergency Shutoff
<p&gtThe dimmer has accounted for Emergency Shutoff, when the temperature rises above 90°C, Emergency Shutoff is sent to Lifeline.</p&gt <p&gtSystem (0x09) Emergency Shutoff status</p&gt

Association group 1 supports 1 node.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| Linked to BASIC|
| COMMAND_CLASS_SCENE_ACTIVATION_V1| |
| COMMAND_CLASS_SCENE_ACTUATOR_CONF_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V8| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [zv-9101 swe/eng manual](https://opensmarthouse.org/zwavedatabase/1058/ZV-9101-manual.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1058).
