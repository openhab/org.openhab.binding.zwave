---
layout: documentation
title: Heatit Z-Push Button 2 - ZWave
---

{% include base.html %}

# Heatit Z-Push Button 2 Wall Mounted Switch
This describes the Z-Wave device *Heatit Z-Push Button 2*, manufactured by *ThermoFloor* with the thing type UID of ```thermofloor_pushbutton2_00_000```.

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![Heatit Z-Push Button 2 product image](https://opensmarthouse.org/zwavedatabase/1424/image/)


The Heatit Z-Push Button 2 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is unable to participate in the routing of data from other devices.

The Heatit Z-Push Button 2 does not permanently listen for messages sent from the controller - it will periodically wake up automatically to check if the controller has messages to send, but will sleep most of the time to conserve battery life. Refer to the *Wakeup Information* section below for further information.

## Overview

Heatit Z-Push Button 2 is a battery operated wall switch with 2 push buttons. The switch enables you to control the Z-Wave- products in your smart home system. Managing your Z-Wave products becomes easy and practical. The switch can be set up to control scenarios or work in associations with other Z-Wave products.

Functions:

Battery operated wall switch

Fits Gira System 55, Elko RS-16 and Schneider Exxact

LED-diode

Easy to install with screws or double-sided tape

Supports encryption modes S0, S2 Authenticated Class, S2 Unauthenticated Class

### Inclusion Information

Step 1. Make sure the remote control does not belong to any Z-Wave network, short press on any button, if LED indicator does not turn on, the remote control does not belong to any network, then continue to step 2, if LED indicator turns on, it means the remote control has already been added to a network, please first set the remote control to removing mode (see exclusion information), then continue to step 2.

Step 2. Set primary controller/gateway into adding mode.

Step 3. Press and hold down both buttons I and O for 3 seconds, LED indicator, the remote control will be set to adding mode, and waiting to be added, after 10s LED indicator blinks 6 times quickly to indicate successful adding.

### Exclusion Information

There are two removing methods.

Method 1: Removing from the primary controller/gateway as follows:

Set the primary controller/gateway into removing mode.

Press and hold down both buttons I and O for 3 seconds, LED indicator turns on, the remote control will be set to removing mode, and waiting to be removed, after 7s LED indicator blinks 4 times quickly to indicate successful removing.

Method 2: Factory reset the remote control will force the remote control to be removed from a network.

A factory reset is done by press and holding down buttons I and O together for over 10 seconds, LED indicator turns on and then blinks 4 times quickly to indicate successful factory reset.

Note: Factory reset is not recommended for removing, please use this procedure only if the primary controller/gateway is missing or otherwise inoperable.

### Wakeup Information

The Heatit Z-Push Button 2 does not permanently listen for messages sent from the controller - it will periodically wake up automatically to check if the controller has messages to send, but will sleep most of the time to conserve battery life. The wakeup period can be configured in the user interface - it is advisable not to make this too short as it will impact battery life - a reasonable compromise is 1 hour.

The wakeup period does not impact the devices ability to report events or sensor data. The device can be manually woken with a button press on the device as described below - note that triggering a device to send an event is not the same as a wakeup notification, and this will not allow the controller to communicate with the device.




### General Usage Information



## Channels

The following table summarises the channels available for the Heatit Z-Push Button 2 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Scene Number | scene_number | scene_number |  | Number | 
| Battery Level | battery-level | system.battery_level | Battery | Number |

### Scene Number
Triggers when a scene button is pressed.

The ```scene_number``` channel is of type ```scene_number``` and supports the ```Number``` item.
This channel provides the scene, and the event as a decimal value in the form ```<scene>.<event>```. The scene number is set by the device, and the event is as follows -:

| Event ID | Event Description  |
|----------|--------------------|
| 0        | Single key press   |
| 1        | Key released       |
| 2        | Key held down      |
| 3        | Double keypress    |
| 4        | Tripple keypress   |
| 5        | 4 x keypress       |
| 6        | 5 x keypress       |

### Battery Level
Represents the battery level as a percentage (0-100%). Bindings for things supporting battery level in a different format (e.g. 4 levels) should convert to a percentage to provide a consistent battery level reading.

The ```system.battery-level``` channel is of type ```system.battery-level``` and supports the ```Number``` item and is in the ```Battery``` category.
This channel provides the battery level as a percentage and also reflects the low battery warning state. If the battery state is in low battery warning state, this will read 0%.


## Device Configuration

The device has no configuration parameters defined.

## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The Heatit Z-Push Button 2 supports 2 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
Send Command Class "Device Reset Locally Notification V1" to associated devices of the group to report factory reset information when factory reset the remote control.

When remote control battery power value changes, send Command Class "Battery Report V1" to associated devices of this group to report power value information.

Short press or press and hold any button to send scene activation command to the associated devices of this group using Command Class "Central Scene Notification V3".

Association group 1 supports 5 nodes.

### Group 2: Launch 1

Short press I /O button of Group 2 to

send ON/OFF command to associated

devices of this group using Command

Class ”Basic Set V2”.

Press and hold down I / O button of

Group 2 to send light intensity increase/decrease command to associated

devices of this group using Command

Class ”Multilevel V4”.

Association group 2 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_TRANSPORT_SERVICE_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_CENTRAL_SCENE_V3| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_SUPERVISION_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_BATTERY_V1| |
| COMMAND_CLASS_WAKE_UP_V2| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
| COMMAND_CLASS_SECURITY_2_V1| |

### Documentation Links

* [Manual](https://opensmarthouse.org/zwavedatabase/1424/reference/manual_heatit_z-push_button_2_fw_1.26_ver2019-a_eng.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1424).
