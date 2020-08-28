---
layout: documentation
title: LDM32 - ZWave
---

{% include base.html %}

# LDM32 MK Honeywell Astral 2 Load Wall Dimmer - LDM32UC
This describes the Z-Wave device *LDM32*, manufactured by *Novar Electrical Devices and Systems (EDS)* with the thing type UID of ```novar_ldm32uc_00_000```.

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![LDM32 product image](https://opensmarthouse.org/zwavedatabase/998/image/)


The LDM32 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<h1&gtLDM32UC</h1&gt <p&gt<strong&gt2 GANG 300W DIMMER MODULE</strong&gt</p&gt <p&gtRF Range up to 75m (open field)<br /&gtCan control up to 5 devices<br /&gtCan control either a group or a scene<br /&gtPre programmed with HOLIDAY MODE function<br /&gtMains powered<br /&gt<br /&gtSTANDARD CARTON QUANTITY: 1</p&gt <p&gtSuitable for use with the following load types and maximum load ratings only.</p&gt <table&gt<tr&gt<td&gtLoad Type     </td&gt <td&gt2G 300W Dimmer</td&gt </tr&gt<tr&gt<td&gtGLS       </td&gt <td&gt25-300W per channel</td&gt </tr&gt<tr&gt<td&gtTungsten filament    </td&gt <td&gt25-300W per channel</td&gt </tr&gt<tr&gt<td&gtTungsten halogen     </td&gt <td&gt25-300W per channel</td&gt </tr&gt<tr&gt<td&gt12V ELV Tungsten halogen with wirewound or electronic transformers</td&gt <td&gt35-300W per channel</td&gt </tr&gt</table&gt<p&gt <br /&gtNot suitable with any other load type.  Do not use different types of lamps on the same load current.<br /&gt<br /&gtBS EN 60669-2-1<br /&gtBS EN 61000-6-1 / 3<br /&gtETSI EN 301489-1 / 2<br /&gtETSI EN 300220-1/ 2</p&gt <p&gtMounting Boxes:  Flush 35mm<br /&gt<br /&gtOperating Frequency  868.4 MHz<br /&gt<br /&gtSelect the appropriate 2 Gang  Dimmer Fascia for your Dimmer Module</p&gt <p&gtFull Data Sheet is available from here: View Full Data Sheet</p&gt

### Inclusion Information

<p&gtTo include device into another system press any operation button </p&gt

### Exclusion Information

<p&gtTo exclude device from a system quickly press Dis/Exc button twice in 1s </p&gt

## Channels

The following table summarises the channels available for the LDM32 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Dimmer | switch_dimmer | switch_dimmer | DimmableLight | Dimmer | 
| Dimmer 1 | switch_dimmer1 | switch_dimmer | DimmableLight | Dimmer | 
| Dimmer 2 | switch_dimmer2 | switch_dimmer | DimmableLight | Dimmer | 

### Dimmer
The brightness channel allows to control the brightness of a light.
            It is also possible to switch the light on and off.

The ```switch_dimmer``` channel is of type ```switch_dimmer``` and supports the ```Dimmer``` item and is in the ```DimmableLight``` category.

### Dimmer 1
The brightness channel allows to control the brightness of a light.
            It is also possible to switch the light on and off.

The ```switch_dimmer1``` channel is of type ```switch_dimmer``` and supports the ```Dimmer``` item and is in the ```DimmableLight``` category.

### Dimmer 2
The brightness channel allows to control the brightness of a light.
            It is also possible to switch the light on and off.

The ```switch_dimmer2``` channel is of type ```switch_dimmer``` and supports the ```Dimmer``` item and is in the ```DimmableLight``` category.



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
| COMMAND_CLASS_NO_OPERATION_V1| Linked to BASIC|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_APPLICATION_STATUS_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V2| |
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_NODE_NAMING_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V1| |
| COMMAND_CLASS_INDICATOR_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CMD_V1| |
#### Endpoint 1

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V2| Linked to BASIC|
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_NODE_NAMING_V1| |
#### Endpoint 2

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V2| Linked to BASIC|
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_NODE_NAMING_V1| |

### Documentation Links

* [Full Data Sheet](https://opensmarthouse.org/zwavedatabase/998/Data-Sheet---Dimmer--50042518-D-.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/998).
