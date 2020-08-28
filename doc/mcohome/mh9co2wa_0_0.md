---
layout: documentation
title: MH9-CO2-WA - ZWave
---

{% include base.html %}

# MH9-CO2-WA CO2 Monitor Air quality detector
This describes the Z-Wave device *MH9-CO2-WA*, manufactured by *[McoHome Technology Co., Ltd](http://www.mcohome.com/)* with the thing type UID of ```mcohome_mh9co2wa_00_000```.

The device is in the category of *Sensor*, defining Device used to measure something.

![MH9-CO2-WA product image](https://opensmarthouse.org/zwavedatabase/1078/image/)


The MH9-CO2-WA supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtMCOHome CO2 Monitor is an air quality detector which compatible with Z-Wave technology.</p&gt <p&gtIt is mainly used to monitor CO2 concentration in industrial, agricultural, and residence environment, while monitoring the indoor temperature, humidity and VOC (optional) air quality.</p&gt <p&gtDevice can be included into any Z-Wave network, and is compatible with any other Z-Wave certified devices.</p&gt <p&gt<strong&gtSpecification</strong&gt</p&gt <ul&gt<li&gtPower Supply：85~260VAC</li&gt <li&gtCO2 display range: 0-2000ppm</li&gt <li&gtDefault threshold:1000ppm (adjustable)</li&gt <li&gtTemperature range：-9.0～50 ℃</li&gt <li&gtHumidity range: 0%～99%RH</li&gt <li&gtInstallation: Wall-mounted (Vertical)</li&gt <li&gtWork environment:-10~+8℃ 0-90%RH (Non-condensation)</li&gt <li&gtDimension：90\* 130\*28mm</li&gt <li&gtHole Pitch：60mm or 82m</li&gt <li&gtHousing: Tempered glass+ PC Alloy</li&gt </ul&gt<p&gt<strong&gtCommand Class supported by the device</strong&gt</p&gt <ul&gt<li&gtCOMMAND\_CLASS\_BASIC</li&gt <li&gtCOMMAND\_CLASS\_SENSOR\_MULTILEVEL\_V5</li&gt <li&gtCOMMAND\_CLASS\_CONFIGURATION</li&gt <li&gtCOMMAND\_CLASS\_NOTIFICATION</li&gt <li&gtCOMMAND\_CLASS\_ASSOCIATION</li&gt <li&gtCOMMAND\_CLASS\_MANUFACTURER\_SPECIFIC</li&gt <li&gtCOMMAND\_CLASS_VERSION</li&gt </ul&gt

### Inclusion Information

<p&gtActivate Inclusion/Exclusion mode in the gateway.</p&gt <p&gtWhen device is powered on, long press K2 can enter interface for inclusion or exclusion of Z-Wave network.</p&gt <p&gtIf device has not been included into any Z-Wave network before, “\- - -”will display on the screen.</p&gt <p&gtThen press K2 once, “\- - -” flashing and device enters into learning mode to get a node ID. If inclusion is success, a node ID will display on the screen. If not, “\- - -” will stop flashing in 20 sec.</p&gt

### Exclusion Information

<p&gtActivate Inclusion/Exclusion mode in the gateway.</p&gt <p&gtWhen device is powered on, long press K2 can enter interface for inclusion or exclusion of Z-Wave network.</p&gt <p&gtIf a node ID displays, it means the device is already in a Z-Wave network.</p&gt <p&gtTo press K2 once can remove it from the network. “\- - -” displays and press K1 once can return to normal work.</p&gt

## Channels

The following table summarises the channels available for the MH9-CO2-WA -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Sensor (CO2) | sensor_co2 | sensor_co2 | CarbonDioxide | Number | 
| Sensor (relative humidity) | sensor_relhumidity | sensor_relhumidity | Humidity | Number | 
| Sensor (temperature) | sensor_temperature | sensor_temperature | Temperature | Number:Temperature | 
| Alarm (CO2) | alarm_co2 | alarm_co2 | CarbonDioxide | Switch | 

### Sensor (CO2)
Indicates the CO2 level.

The ```sensor_co2``` channel is of type ```sensor_co2``` and supports the ```Number``` item and is in the ```CarbonDioxide``` category. This is a read only channel so will only be updated following state changes from the device.

### Sensor (relative humidity)
Indicates the current relative humidity.

The ```sensor_relhumidity``` channel is of type ```sensor_relhumidity``` and supports the ```Number``` item and is in the ```Humidity``` category. This is a read only channel so will only be updated following state changes from the device.

### Sensor (temperature)
Indicates the current temperature.

The ```sensor_temperature``` channel is of type ```sensor_temperature``` and supports the ```Number:Temperature``` item and is in the ```Temperature``` category.

### Alarm (CO2)
Indicates if the carbon dioxide alarm is triggered.

The ```alarm_co2``` channel is of type ```alarm_co2``` and supports the ```Switch``` item and is in the ```CarbonDioxide``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |



## Device Configuration

The following table provides a summary of the 1 configuration parameters available in the MH9-CO2-WA.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Notification Threshold | CO2 Notification Threshold |

### Parameter 1: Notification Threshold

CO2 Notification Threshold
<p&gtSets the CO2 notification threshold for association group 1.</p&gt <p&gtWhen the detected CO2 value is higher than the setting value, the device will send a (CO2 Detected Event) report to the Group 1. And this report will keep sending every 30 sec until the detected value is lower than the setting value.</p&gt
Values in the range 1 to 2000 may be set.

The manufacturer defined default value is ```1000```.

This parameter has the configuration ID ```config_1_2``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The MH9-CO2-WA supports 2 association groups.

### Group 1: Notification

sends report ever 30s when CO2 value above threshold
<p&gtGroup 1 is for “Notification” purpose, which can add up to 5 Node ID. Device works in “Push” mode in “Notification Command Class”.</p&gt <p&gtWhen the detected CO2 value is higher than the setting value, the device will send a (CO2 Detected Event) report to the Group 1. And this report will keep sending every 30 sec until the detected value is lower than the setting value.</p&gt <p&gtUse “Notification Set” can set this unsolicited report; this function is default as “OFF”.</p&gt

Association group 1 supports 5 nodes.

### Group 2: Gateway report

CO2, Temperature and Humidity is reported when changed
<p&gtA gateway is suggested to associate with Group 2, which can support only one Node ID. The<br /&gtgateway can ask for detected data any time. And the device will report to this associated device<br /&gt(gateway) when detected data changes:<br /&gtCO2: report when any change ≥50ppm“Multilevel sensor Report (CO2=0x11)”<br /&gtTemperature: report when any change≥ 0.5 “Multilevel ℃ sensor Report (Temp=0x01)”<br /&gtHumidity: report when any change≥ 2%“Multilevel sensor Report (Humidity=0x05)’</p&gt

Association group 2 supports 1 node.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SENSOR_MULTILEVEL_V5| |
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
| COMMAND_CLASS_SENSOR_CONFIGURATION_V1| |

### Documentation Links

* [Manual](https://opensmarthouse.org/zwavedatabase/1078/20180531151046.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1078).
