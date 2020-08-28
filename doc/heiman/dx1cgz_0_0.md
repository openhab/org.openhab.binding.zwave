---
layout: documentation
title: DX1CG-Z - ZWave
---

{% include base.html %}

# DX1CG-Z CH4 Gas Sensor
This describes the Z-Wave device *DX1CG-Z*, manufactured by *[Heiman Technology Co. Ltd](http://www.heimantech.com/)* with the thing type UID of ```heiman_dx1cgz_00_000```.

The device is in the category of *Sensor*, defining Device used to measure something.

![DX1CG-Z product image](https://opensmarthouse.org/zwavedatabase/682/image/)


The DX1CG-Z supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

This Smart Combustible Gas Sensor adopts super low power consumption Z-Wave Wireless module. Used for detecting combusting gas leakage and preventing danger caused by gas leakage it also can be used as Z-Wave repeater to extend wireless transmission distance. It adopts high stability semiconductor gas sensor.

Sensor alarms if gas density in air exceeds set alarm level. It is applicable for kitchen or possible gas leakage in in-door area.

### Inclusion Information

Press the Net_Button 3 times within 1.5s, Green LED is Blinking 3 times within 1 second. If add Process is successful, Green LED will turn on.

### Exclusion Information

Press the Net_Button 3 times within 1.5s. If remove Process is successful, Green LED is Blinking 6 times, then turn on.

## Channels

The following table summarises the channels available for the DX1CG-Z -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Combustible Gas Alarm | alarm_combustiblegas | alarm_combustiblegas |  | Switch | 

### Combustible Gas Alarm
Indicates if the combustible gas alarm is triggered.

The ```alarm_combustiblegas``` channel is of type ```alarm_combustiblegas``` and supports the ```Switch``` item. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |



## Device Configuration

The device has no configuration parameters defined.

## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The DX1CG-Z supports 2 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.

Association group 1 supports 1 node.

### Group 2: Root Device group (Notifcation)


Association group 2 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_ALARM_V1| Linked to BASIC|
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |

### Documentation Links

* [Quickstart](https://opensmarthouse.org/zwavedatabase/682/Heiman-Quick-Start.pdf)
* [Manual](https://opensmarthouse.org/zwavedatabase/682/DX1CG-Z-USER-MANUAL.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/682).
