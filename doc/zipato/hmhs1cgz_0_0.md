---
layout: documentation
title: HM-HS1CG-Z - ZWave
---

{% include base.html %}

# HM-HS1CG-Z Combustible Gas Sensor
This describes the Z-Wave device *HM-HS1CG-Z*, manufactured by *[Zipato](http://www.zipato.com/)* with the thing type UID of ```zipato_hmhs1cgz_00_000```.

The device is in the category of *Sensor*, defining Device used to measure something.

![HM-HS1CG-Z product image](https://opensmarthouse.org/zwavedatabase/963/image/)


The HM-HS1CG-Z supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtThis Smart Combustible Gas Sensor adopts super low power consumption Z-Wave Wireless module. Used for detecting combusting gas leakage and preventing danger caused by gas leakage it also can be used as Z-Wave repealer to extend wireless transmission distance. It adopts high stability semi-conductor gas sensor. Sensor alarms if gas density in air exceeds set alarm level. It is applicable for kitchen or possible gas leakage in in-door area.</p&gt

### Inclusion Information

<ul&gt<li&gtClick [Add] icon in Z-Wave Controller.</li&gt <li&gtPress the Net_Button 3 times within 1.5s, Green LED is Blinking 3 times within 1 second.</li&gt <li&gtIf add Process is successful, Green LED will turn on</li&gt </ul&gt

### Exclusion Information

<ul&gt<li&gtClick [Remove] icon in Z-Wave Controller.</li&gt <li&gtPress the Net_Button 3 times within 1.5s</li&gt <li&gtIf remove Process is successful, Green LED is Blinking 6 times, then turn on</li&gt </ul&gt

## Channels

The following table summarises the channels available for the HM-HS1CG-Z -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Alarm | alarm_general | alarm_general | Alarm | Switch | 

### Alarm
Indicates if an alarm is triggered.

The ```alarm_general``` channel is of type ```alarm_general``` and supports the ```Switch``` item and is in the ```Alarm``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |



## Device Configuration

The device has no configuration parameters defined.

## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The HM-HS1CG-Z supports 2 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
<ul&gt<li&gtLifeline between controller and the device.</li&gt <li&gtSupported command classes: Notification report, and Device Reset Locally notification.</li&gt <li&gtAssociation Group description: Association group 1: Lifeline association group</li&gt <li&gtMaximum supported node is 1.</li&gt </ul&gt<p&gtInclude command classes: Notification Report and Device Reset Locally notification.</p&gt

Association group 1 supports 1 node.

### Group 2: Root Device group(Notification)

<p&gtMaximum supported nodes are 5.<br /&gt<br /&gt1-Notification reports status of gas detected or no gas <br /&gt<br /&gt    Association group 1: Lifeline association group<br /&gt<br /&gt2-When the sensor detects status change of gas detected and no gas detected, the device will be triggered.</p&gt

Association group 2 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ALARM_V7| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |

### Documentation Links

* [Manual](https://opensmarthouse.org/zwavedatabase/963/hm-hs1cg-z-USER-MANUAL.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/963).
