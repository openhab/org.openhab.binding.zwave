---
layout: documentation
title: ph-pse02 - ZWave
---

{% include base.html %}

# ph-pse02 Multisound indoor siren
This describes the Z-Wave device *ph-pse02*, manufactured by *Devolo* with the thing type UID of ```devolo_phpse02_00_000```.

The device is in the category of *Siren*, defining Siren used by Alarm systems.

![ph-pse02 product image](https://opensmarthouse.org/zwavedatabase/453/image/)


The ph-pse02 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gt<strong&gt### NOTE ###</strong&gt</p&gt <p&gtThe XML used for adding this device to the database comes from the Devolo Home Control Alarmsiren (http://www.devolo.de/article/devolo-home-control-alarmsirene/). But it seems to be made by Zipato (https://www.zipato.com/product/multisound-indoor-siren).<br /&gt<br /&gt<strong&gt### FEATURES ###</strong&gt<br /&gt<br /&gt- Indoor multichannel siren that works with a a variety of Z-Wave networks/controllers, regardless of the manufacturer<br /&gt- The new Z-Wave 500 series chip supports multichannel operationand higher data rates (9.6/40/100kbps)<br /&gt- 110db sound level<br /&gt- 6 sounds: Door Chime, Bi Bi Arm/Disarm, Intruder alarm, Ambulance, Police car<br /&gt- Higher output power enhances communication range(+6dBm output power as compared to -2.5dBm 300 series)<br /&gt- DC or Li-ion battery power (over 24 hours of battery life)<br /&gt- Battery overcharge protection<br /&gt- Very low power consumption<br /&gt- Over-the-air firmware update<br /&gt- Easy to install in EU/China/Taiwan wall sockets<br /&gt- Tamperproof protection<br /&gt<br /&gt<br /&gt<strong&gt### TECHNICAL SPECIFICATIONS ###</strong&gt<br /&gt<br /&gtPROTOCOL: Z-Wave Plus<br /&gt<br /&gtPOWER<br /&gt- DC 5V, Li-Ion battery (optional)<br /&gt- BATTERY CAPACITY 1150mAh<br /&gt<br /&gtOPERATING CONDITIONS:<br /&gt- OPERATING VOLTAGE 3.7V<br /&gt- OPERATING CURRENT 150 mA<br /&gt- OPERATION TEMPERATURE -10°C ~ 40°C<br /&gt<br /&gtRANGE<br /&gt- Minimum 30 meters indoor<br /&gt- 70 meters outdoor (meant for indoor use only)<br /&gt<br /&gtDIMENSIONS<br /&gt- DEVICE  DIMENSIONS 110 x 110 x 22 mm<br /&gt- DEVICE WEIGHT 113g<br /&gt- PACKAGE DIMENSIONS 117 x 50 x 117 mm<br /&gt- PACKAGE WEIGHT 217g<br /&gt<br /&gtFREQUENCY<br /&gt<br /&gt    ph-pse02.au 921.42 MHz (AU)<br /&gt    ph-pse02.eu 868.42 MHz (EU)<br /&gt    ph-pse02.in 865.20 MHz (IN)<br /&gt    ph-pse02.is 916.02 MHz (IS)<br /&gt    ph-pse02.ru 869.02 MHz (RU)<br /&gt    ph-pse02.us 908.42 MHz (US)<br /&gt<br /&gtSource: https://www.zipato.com/product/multisound-indoor-siren</p&gt

### Inclusion Information

<p&gtPress the tamper key three times within 1.5 seconds to enter the inclusion mode.</p&gt <p&gtAfter successful inclusion, the LED will light up for 1 second.</p&gt

### Exclusion Information

<p&gtPress the tamper key three times within 1.5 seconds to enter the exclusion mode.</p&gt

## Channels

The following table summarises the channels available for the ph-pse02 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 
| Binary Sensor | sensor_binary | sensor_binary |  | Switch | 
| Alarm (burglar) | alarm_burglar | alarm_burglar | Door | Switch | 
| Alarm (general) | alarm_general | alarm_general | Alarm | Switch | 
| Start Sound | notification_send | notification_send |  | Number | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Binary Sensor
Indicates if a sensor has triggered.

The ```sensor_binary``` channel is of type ```sensor_binary``` and supports the ```Switch``` item. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| ON | Triggered |
| OFF | Untriggered |

### Alarm (burglar)
Indicates if the burglar alarm is triggered.

The ```alarm_burglar``` channel is of type ```alarm_burglar``` and supports the ```Switch``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Alarm (general)
Indicates if an alarm is triggered.

The ```alarm_general``` channel is of type ```alarm_general``` and supports the ```Switch``` item and is in the ```Alarm``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Start Sound
<table&gt<tr&gt<td&gt </td&gt <td&gt <p&gtNotification </p&gt </td&gt <td&gt <p&gtEvent </p&gt </td&gt <td&gt <p&gtSound </p&gt </td&gt </tr&gt<tr&gt<td&gtevent1</td&gt <td&gt <p&gtSmokealarm (0x01) </p&gt </td&gt <td&gt <p&gt0x01 ~ 0xFF </p&gt </td&gt <td&gt <p&gtfire alert</p&gt </td&gt </tr&gt<tr&gt<td&gtevent2</td&gt <td&gt <p&gtAccesscontrol (0x06) </p&gt </td&gt <td&gt <p&gtWindow/Door open (0x16) </p&gt </td&gt <td&gt <p&gtdoor chime</p&gt </td&gt </tr&gt<tr&gt<td&gtevent7</td&gt <td&gt <p&gtAccesscontrol (0x06) </p&gt </td&gt <td&gt <p&gtActivate alarm system (0x03) </p&gt </td&gt <td&gt <p&gt2x beep </p&gt </td&gt </tr&gt<tr&gt<td&gtevent8</td&gt <td&gt <p&gtAccesscontrol (0x06) </p&gt </td&gt <td&gt <p&gtDeactivate alarm system (0x04) </p&gt </td&gt <td&gt <p&gt1x beep </p&gt </td&gt </tr&gt<tr&gt<td&gtevent3</td&gt <td&gt <p&gtHomesecurity (0x07) </p&gt </td&gt <td&gt <p&gt0x01 ~ 0xFF </p&gt </td&gt <td&gt <p&gtemergency</p&gt sound</td&gt </tr&gt<tr&gt<td&gtevent4</td&gt <td&gt <p&gtEmergency (0x0A) </p&gt </td&gt <td&gt <p&gtContact police (0x01) </p&gt </td&gt <td&gt <p&gtpolicecar</p&gt <p&gtsound</p&gt </td&gt </tr&gt<tr&gt<td&gtevent5</td&gt <td&gt <p&gtEmergency (0x0A) </p&gt </td&gt <td&gt <p&gtContact fire department (0x02) </p&gt </td&gt <td&gt <p&gtfire engine sound</p&gt </td&gt </tr&gt<tr&gt<td&gtevent6</td&gt <td&gt <p&gtEmergency (0x0A) </p&gt </td&gt <td&gt <p&gtContact ambulance (0x03) </p&gt </td&gt <td&gt <p&gtambulance</p&gt <p&gtsound </p&gt </td&gt </tr&gt<tr&gt<td&gtevent9</td&gt <td&gtEmergency (0x0A)</td&gt <td&gt <p&gtNotification (0xFE) </p&gt </td&gt <td&gt <p&gtsilent alarm (no sound,  flashing LED) </p&gt </td&gt </tr&gt</table&gt

Sends a notification.

The ```notification_send``` channel is of type ```notification_send``` and supports the ```Number``` item.



## Device Configuration

The following table provides a summary of the 3 configuration parameters available in the ph-pse02.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 7 | Costumer  Function | NotificationReport/BinaryReport |
| 29 | Disable Alarm | Disable the alarm function. |
| 31 | Alarm Duration | Play alarm sound duration. |

### Parameter 7: Costumer  Function

NotificationReport/BinaryReport
<p&gtNotification Type,</p&gt <p&gt0: Using Notification Report.</p&gt <p&gt1: Using Sensor Binary Report.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```4```.

This parameter has the configuration ID ```config_7_4``` and is of type ```INTEGER```.


### Parameter 29: Disable Alarm

Disable the alarm function.
<p&gtDisable the alarm function.</p&gt <p&gt1: Disable Alarm,</p&gt <p&gt0: Enable Alarm.</p&gt <p&gtCaution: After the power up, this configuration is always 0.</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_29_4``` and is of type ```INTEGER```.


### Parameter 31: Alarm Duration

Play alarm sound duration.
<p&gtPlay alarm sound duration, 1 tick is 30 seconds.</p&gt <p&gtDefault is 3 minutes, maximum is 63.5 minutes.</p&gt <p&gt0 means never auto stop.</p&gt
Values in the range 0 to 127 may be set.

The manufacturer defined default value is ```6```.

This parameter has the configuration ID ```config_31_4_0000000C``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The ph-pse02 supports 1 association group.

### Group 1: Report Message

Report Message, e.g. tamper alarm
<p&gtNotice: The device supports 1 group.</p&gt <p&gtThe group 1 is for receiving the report message, like tamper event. And the group 8 nodes maximum</p&gt

Association group 1 supports 8 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| Linked to BASIC|
| COMMAND_CLASS_SWITCH_MULTILEVEL_V1| |
| COMMAND_CLASS_SENSOR_BINARY_V2| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V4| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |

### Documentation Links

* [Quick Installation Guide V1.2](https://opensmarthouse.org/zwavedatabase/453/ph-pse02-Zipato-Siren-User-Manual-v1-2.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/453).
