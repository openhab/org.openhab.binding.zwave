---
layout: documentation
title: SW-ZAP-1 - ZWave
---

{% include base.html %}

# SW-ZAP-1 Swiid Plug
This describes the Z-Wave device *SW-ZAP-1*, manufactured by *CBCC Domotique SAS* with the thing type UID of ```cbcc_swzap1_00_000```.

The device is in the category of *Power Outlet*, defining Small devices to be plugged into a power socket in a wall which stick there.

![SW-ZAP-1 product image](https://opensmarthouse.org/zwavedatabase/1003/image/)


The SW-ZAP-1 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

The SwiidPlug™ is designed to switch on and off remotely appliances (mainly lights) which are plugged into its female socket, The SwiidPlug™ is suitable for the wireless switching of incandescent light bulbs as well as halogen and LED lights (all transformer types). The SwiidPlug™ can be operated manually by pressing on its FUNCTION button which then acts as an on/off toggle switch and can be operated remotely and wirelessly by Z -Wave®. The SwiidPlug™ can also handle associations with other devices, both ways to and from it. Thus, your SwiidPlug™ can respond directly to a signal from another Z-Wave® device in your network, such as the triggering of a presence detector. As soon as it has been plugged into a mains socket, the SwiidPlug™ can be included into a Z-Wave® network so as to interoperate with controllers and other Z-Wave® devices. 

http://www.swiid.com/medias/documents/ZAP-User-s-manual-vBAT-30.11.2014.pdf

http://www.swiid.com/medias/documents/advanced-swiidplug.pdf

### Inclusion Information

Put your Controller into inclusion mode, with the LED flashing alternatively blue and yellow, press the FUNCTION button of your SwiidPlug 3 times (3x) within 2 seconds.

### Exclusion Information

Put your Controller into exclusion mode, press the FUNCTION button of your SwiidPlug 3 times (3x) within 2 seconds.

## Channels

The following table summarises the channels available for the SW-ZAP-1 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.



## Device Configuration

The following table provides a summary of the 1 configuration parameters available in the SW-ZAP-1.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Switch All | Switch All |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 1: Switch All

Switch All

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Disabled (Default) |
| 255 | Enabled |

The manufacturer defined default value is ```255``` (Enabled).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.

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

The SW-ZAP-1 supports 2 association groups.

### Group 1: Group 1


Association group 1 supports 5 nodes.

### Group 2: Group 2


Association group 2 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| Linked to BASIC|
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_PROTECTION_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |

### Documentation Links

* [ZAP User Manual](https://opensmarthouse.org/zwavedatabase/1003/ZAP-User-s-manual-vBAT-30-11-2014.pdf)
* [Advanced swiidplug](https://opensmarthouse.org/zwavedatabase/1003/Advanced-swiidplug.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1003).
