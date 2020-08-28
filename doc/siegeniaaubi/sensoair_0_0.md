---
layout: documentation
title: Sensoair - ZWave
---

{% include base.html %}

# Sensoair Air quality sensor for indoor use
This describes the Z-Wave device *Sensoair*, manufactured by *[SIEGENIA-AUBI KG](https://www.siegenia.com)* with the thing type UID of ```siegeniaaubi_sensoair_00_000```.

The device is in the category of *Sensor*, defining Device used to measure something.

![Sensoair product image](https://opensmarthouse.org/zwavedatabase/451/image/)


The Sensoair supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtThis device measures levels og carbon dioxide & VOCs.</p&gt

### Inclusion Information

<p&gtPress the push-button on the bottom of the unit once.</p&gt

### Exclusion Information

<p&gtPress the push-button on the bottom of the unit once.</p&gt

## Channels

The following table summarises the channels available for the Sensoair -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Sensor (CO2) | sensor_co2 | sensor_co2 | CarbonDioxide | Number | 

### Sensor (CO2)
Indicates the CO2 level.

The ```sensor_co2``` channel is of type ```sensor_co2``` and supports the ```Number``` item and is in the ```CarbonDioxide``` category. This is a read only channel so will only be updated following state changes from the device.



## Device Configuration

The following table provides a summary of the 2 configuration parameters available in the Sensoair.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Device Configuration Value 1 | Configuration of the device operating mode |
| 2 | Interval for unsolicited Sensor Report Mode B | defines how often a Sensor report is sent |

### Parameter 1: Device Configuration Value 1

Configuration of the device operating mode
<ul&gt<li&gtbit 0    Unsolicited Multilevel Report Mode A    0 = disabled, 1 = <strong&gtenabled</strong&gt</li&gt <li&gtbit 1    Unsolicited Multilevel Report Mode B    0 = <strong&gtdisabled</strong&gt, 1 = enabled</li&gt <li&gtbit 2    Basic Set                                         0 = disabled, 1 = <strong&gtenabled</strong&gt</li&gt <li&gtbit 3    Broadcast Multilevel Report                 0 = disabled, 1 = <strong&gtenabled</strong&gt</li&gt <li&gtbit 7    SENSOAIR LEDs                               0 = disabled, 1 = <strong&gtenabled</strong&gt</li&gt </ul&gt<p&gt<strong&gtUnsolicited Multilevel Report Mode A</strong&gt</p&gt <p&gtSENSOAIR sends an usolicited multilevel report when the CO<sub&gt2</sub&gt value<br /&gtexceeds one of the following threshold values:<br /&gt600 ppm, 800 ppm, 1000 ppm, 1500 ppm, 2000 ppm, 2500 ppm</p&gt <p&gt<strong&gtUnsolicited Multilevel Report Mode B<br /&gt</strong&gt</p&gt <p&gtSENSOAIR sends the current CO<sub&gt2</sub&gt value (without being requested) in an interval of<br /&gt5 - 65000 seconds. The interval can be configured with parameter 2.</p&gt
Values in the range 0 to 143 may be set.

The manufacturer defined default value is ```141```.

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: Interval for unsolicited Sensor Report Mode B

defines how often a Sensor report is sent
<p&gtWhen enabled by parameter 1, SENSOAIR sends the current CO<sub&gt2</sub&gt value (without being requested) in an interval of 5 - 65000 seconds. The interval (default setting = 30s) is configured as follows:</p&gt <p&gtDevice Configuration Value 1 = MSB (default 0x00)</p&gt <p&gtDevice Configuration Value 2 = LSB (default 0x1e)</p&gt
Values in the range 5 to 65000 may be set.

The manufacturer defined default value is ```30```.

This parameter has the configuration ID ```config_2_2``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The Sensoair supports 1 association group.

### Group 1: Group 1


Association group 1 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SENSOR_MULTILEVEL_V3| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |
| COMMAND_CLASS_SENSOR_CONFIGURATION_V1| |

### Documentation Links

* [User Manual](https://opensmarthouse.org/zwavedatabase/451/co2-eng.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/451).
