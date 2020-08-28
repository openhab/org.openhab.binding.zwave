---
layout: documentation
title: DZ6HD - ZWave
---

{% include base.html %}

# DZ6HD 600W Dimmer
This describes the Z-Wave device *DZ6HD*, manufactured by *Leviton* with the thing type UID of ```leviton_dz6hd_00_000```.

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![DZ6HD product image](https://opensmarthouse.org/zwavedatabase/556/image/)


The DZ6HD supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

Universal incandescent, LED, wall-mount CFL dimmer

### Inclusion Information

Network Wide Inclusion:

Network Wide Inclusion allows your device to be included into the network using devices already in the network to assist with communication. Work your way from the closest devices to the controller outward.

  * Enter Programming Mode by holding the top of the paddle for 7 seconds, the Locator LED will blink amber.
  * Tap the top of the paddle one time. The Locator LED will quickly ash green.  
    The Decora SmartTM Z-Wave® device is ready to learn into the Z-Wave® network.
  * Follow directions in the Z-Wave controller to complete the adding process.
  * Upon successful addition to network the LED will turn off and then blink green 3 times.
  * If the adding process is not successful the LED will ash red 3 times.

Traditional Inclusion:

For older controllers Traditional Inclusion is supported. Depending on the age of the controller the controller will need to be 3 to 35 feet from the device when including.

  * Enter Programming Mode by holding the top of the paddle for 7 seconds, the Locator LED will blink amber.
  * The Decora SmartTM Z-Wave® device is ready to add to the Z-Wave® network.
  * Follow directions in the Z-Wave® controller to enter learn mode.
  * Tap the top of the paddle one time. The Locator LED will quickly ash green. The Z-Wave® controller will begin to pair with the Decora SmartTM device.
  * Upon successful addition to the network the LED will turn off and then blink green 3 times.
  * If the adding process is not successful the LED will ash red 3 times.

### Exclusion Information

When removing a device from a Z-Wave® network best practice is to use the exclusion command found in the Z-Wave® controller.

  * Enter Programming Mode by holding the top of the paddle for 7 seconds, the Locator LED will blink amber.
  * Follow directions in the Z-Wave® controller to enter exclusion mode.
  * Tap the top of the paddle one time. The Locator LED will quickly flash green.  The Z-Wave® controller will exclude the Decora SmartTM device.
  * The Z-Wave® controller will confirm successful exclusion from the network.

## Channels

The following table summarises the channels available for the DZ6HD -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Dimmer | switch_dimmer | switch_dimmer | DimmableLight | Dimmer | 
| Scene Number | scene_number | scene_number |  | Number | 

### Dimmer
The brightness channel allows to control the brightness of a light.
            It is also possible to switch the light on and off.

The ```switch_dimmer``` channel is of type ```switch_dimmer``` and supports the ```Dimmer``` item and is in the ```DimmableLight``` category.

### Scene Number
Triggers when a scene button is pressed.

The ```scene_number``` channel is of type ```scene_number``` and supports the ```Number``` item.



## Device Configuration

The following table provides a summary of the 8 configuration parameters available in the DZ6HD.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Fade On Time | Fade on time |
| 2 | Fade Off Time | Fade off time |
| 3 | Minimum Light Level | Minimum light level |
| 4 | Maximum Light Level | Maximum light level |
| 5 | Preset Light Level | Preset light level |
| 6 | LED Dim Level Indicator Timeout | LED dim level indicator timeout |
| 7 | Locator LED Status | Locator LED status |
| 8 | Load Type | Load type |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 1: Fade On Time

Fade on time
  * 0: Instant on
  * 1 to 127: 1-127 seconds
  * 128 to 253: 1-126 minutes
Values in the range 0 to 253 may be set.

The manufacturer defined default value is ```2```.

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: Fade Off Time

Fade off time
  * 0: Instant off
  * 1 to 127: 1-127 seconds
  * 128 to 253: 1-126 minutes
Values in the range 0 to 253 may be set.

The manufacturer defined default value is ```2```.

This parameter has the configuration ID ```config_2_1``` and is of type ```INTEGER```.


### Parameter 3: Minimum Light Level

Minimum light level

Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_3_1``` and is of type ```INTEGER```.


### Parameter 4: Maximum Light Level

Maximum light level

Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```100```.

This parameter has the configuration ID ```config_4_1``` and is of type ```INTEGER```.


### Parameter 5: Preset Light Level

Preset light level
  * 0: Last dim level
  * 1 to 100: Level
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_5_1``` and is of type ```INTEGER```.


### Parameter 6: LED Dim Level Indicator Timeout

LED dim level indicator timeout
  * 0: LED indicators off
  * 1 to 254: Timeout in seconds
  * 255: Always on
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```3```.

This parameter has the configuration ID ```config_6_1``` and is of type ```INTEGER```.


### Parameter 7: Locator LED Status

Locator LED status
  * 0: LED off
  * 254: Status mode
  * 255: Locator mode
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```255```.

This parameter has the configuration ID ```config_7_1``` and is of type ```INTEGER```.


### Parameter 8: Load Type

Load type
  * 0: Incandescent
  * 1: LED
  * 2: CFL
Values in the range 0 to 2 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_8_1``` and is of type ```INTEGER```.

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

The DZ6HD supports 1 association group.

### Group 1: Group 1

Z-Wave Plus Lifeline
A NOTIFICATION frame is sent to the nodes in this association group when a Lifeline event occurs.

Association group 1 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V1| Linked to BASIC|
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_SCENE_ACTIVATION_V1| |
| COMMAND_CLASS_SCENE_ACTUATOR_CONF_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_HAIL_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |

### Documentation Links

* [User Manual](https://opensmarthouse.org/zwavedatabase/556/DI-000-DZ6HD-02A-X4--1-.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/556).
