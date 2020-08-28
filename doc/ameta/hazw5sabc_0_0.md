---
layout: documentation
title: HA-ZW-5SABC - ZWave
---

{% include base.html %}

# HA-ZW-5SABC 4 In 1 Motion Sensor
This describes the Z-Wave device *HA-ZW-5SABC*, manufactured by *Ameta International* with the thing type UID of ```ameta_hazw5sabc_00_000```.

The device is in the category of *Motion Detector*, defining Motion sensors/detectors.

![HA-ZW-5SABC product image](https://opensmarthouse.org/zwavedatabase/1211/image/)


The HA-ZW-5SABC supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is unable to participate in the routing of data from other devices.

The HA-ZW-5SABC does not permanently listen for messages sent from the controller - it will periodically wake up automatically to check if the controller has messages to send, but will sleep most of the time to conserve battery life. Refer to the *Wakeup Information* section below for further information.

## Overview

<p&gtThe 4-in-1 motion sensor is designed for using with scenes in home automation systems, integrate motion, light, temperature and humidity sensors, powered by CR123A battery or MicroUSB cable. The Motion Sensor lets you know when movement is detected in a certain area and can trigger different actions in response to that movement (or lack of movement). It also lets you know the ambient temperature and humidity to trigger different actions to make you more comfort. </p&gt

### Inclusion Information

<ul&gt<li&gtPut your primary controller in inclusion mode</li&gt <li&gtShort press the button at the back of the sensor once.</li&gt <li&gtThe 4-in-1 Motion Sensor LED will blink, If the inclusion is successful, then LED will stay on for 2 seconds. Otherwise, the LED will blink until timeout, in which case you need to repeat the process from step b.</li&gt </ul&gt

### Exclusion Information

<ul&gt<li&gtPut your primary controller in exclusion mode</li&gt <li&gtShort press the button at the back of the sensor once.</li&gt <li&gtThe 4-in-1 Motion Sensor LED will blink, If the exclusion is successful, then LED will stay on for 2 seconds. Otherwise, the LED will blink until timeout, in which case you need to repeat the process from step b.</li&gt </ul&gt

### Wakeup Information

The HA-ZW-5SABC does not permanently listen for messages sent from the controller - it will periodically wake up automatically to check if the controller has messages to send, but will sleep most of the time to conserve battery life. The wakeup period can be configured in the user interface - it is advisable not to make this too short as it will impact battery life - a reasonable compromise is 1 hour.

The wakeup period does not impact the devices ability to report events or sensor data. The device can be manually woken with a button press on the device as described below - note that triggering a device to send an event is not the same as a wakeup notification, and this will not allow the controller to communicate with the device.


<p&gtTo wake up the sensor so that your hub can send it configuration parameters quickly press on the button on the back 3 times.</p&gt

## Channels

The following table summarises the channels available for the HA-ZW-5SABC -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Sensor (relative humidity) | sensor_relhumidity | sensor_relhumidity | Humidity | Number | 
| Sensor (luminance) | sensor_luminance | sensor_luminance |  | Number | 
| Sensor (temperature) | sensor_temperature | sensor_temperature | Temperature | Number:Temperature | 
| Alarm (burglar) | alarm_burglar | alarm_burglar | Door | Switch | 
| Motion Alarm | alarm_motion | alarm_motion | Motion | Switch | 
| Battery Level | battery-level | system.battery_level | Battery | Number |

### Sensor (relative humidity)
Indicates the current relative humidity.

The ```sensor_relhumidity``` channel is of type ```sensor_relhumidity``` and supports the ```Number``` item and is in the ```Humidity``` category. This is a read only channel so will only be updated following state changes from the device.

### Sensor (luminance)
Indicates the current light reading.

The ```sensor_luminance``` channel is of type ```sensor_luminance``` and supports the ```Number``` item. This is a read only channel so will only be updated following state changes from the device.

### Sensor (temperature)
Indicates the current temperature.

The ```sensor_temperature``` channel is of type ```sensor_temperature``` and supports the ```Number:Temperature``` item and is in the ```Temperature``` category.

### Alarm (burglar)
Indicates if the burglar alarm is triggered.

The ```alarm_burglar``` channel is of type ```alarm_burglar``` and supports the ```Switch``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Motion Alarm
Indicates if a motion alarm is triggered.

The ```alarm_motion``` channel is of type ```alarm_motion``` and supports the ```Switch``` item and is in the ```Motion``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Battery Level
Represents the battery level as a percentage (0-100%). Bindings for things supporting battery level in a different format (e.g. 4 levels) should convert to a percentage to provide a consistent battery level reading.

The ```system.battery-level``` channel is of type ```system.battery-level``` and supports the ```Number``` item and is in the ```Battery``` category.
This channel provides the battery level as a percentage and also reflects the low battery warning state. If the battery state is in low battery warning state, this will read 0%.


## Device Configuration

The following table provides a summary of the 15 configuration parameters available in the HA-ZW-5SABC.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 10 | Low Battery Power Level Alarm | Value at which sensor reports low battery to the gateway |
| 12 | PIR Sensitivity |  |
| 13 | PIR Trigger Time | PIR Trigger Time (Time Between PIR Readings) |
| 14 | Basic Set Command Send after PIR Trigger | Basic Set Command Send after PIR Trigger |
| 15 | PIR Trigger Correspondence Action | PIR Trigger Correspondence Action |
| 100 | Change Parameters 101-104 Back to Default Settings | Change Parameters 101-104 Back to Default Settings |
| 101 | Temperature Reporting Interval | Temperature Reporting Interval |
| 102 | Humidity Reporting Interval | Humidity Reporting Interval |
| 103 | Luminance Reporting Interval | Luminance Reporting Interval |
| 104 | Battery Reporting Interval | Battery Reporting Interval |
| 110 | Change Parameters 111-114 Back to Default Settings | Change Parameters 111-114 Back to Default Settings |
| 111 | Temperature Threshold | Temperature Threshold |
| 112 | Humidity Threshold | Humidity Threshold |
| 113 | Luminance Threshold | Luminance Threshold |
| 114 | Battery Threshold | Battery Threshold |
|  | Wakeup Interval | Sets the interval at which the device will accept commands from the controller |
|  | Wakeup Node | Sets the node ID of the device to receive the wakeup notifications |

### Parameter 10: Low Battery Power Level Alarm

Value at which sensor reports low battery to the gateway

Values in the range 10 to 50 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_10_1``` and is of type ```INTEGER```.


### Parameter 12: PIR Sensitivity



Values in the range 0 to 10 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_12_1``` and is of type ```INTEGER```.


### Parameter 13: PIR Trigger Time

PIR Trigger Time (Time Between PIR Readings)
<p&gtThe amount of seconds between motion detection (ie. interval)</p&gt <p&gt5 = 5 seconds</p&gt
Values in the range 5 to 15300 may be set.

The manufacturer defined default value is ```30```.

This parameter has the configuration ID ```config_13_2``` and is of type ```INTEGER```.


### Parameter 14: Basic Set Command Send after PIR Trigger

Basic Set Command Send after PIR Trigger
<p&gtShould Basic Set Command be sent after PIR is triggered:</p&gt <p&gt0=No, 1 = Yes</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_14_1``` and is of type ```INTEGER```.


### Parameter 15: PIR Trigger Correspondence Action

PIR Trigger Correspondence Action
<p&gtAbility to reverse the Basic Set behavior for devices associated in group 2.</p&gt <p&gt0 = Turn the associated device ON when motion is tripped, and OFF when motion stops.</p&gt <p&gt1 = Turn the associated device OFF when motion is tripped, and ON when motion stops.</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_15_1``` and is of type ```INTEGER```.


### Parameter 100: Change Parameters 101-104 Back to Default Settings

Change Parameters 101-104 Back to Default Settings
<p&gtIf changes are made to parameters 101-104, you can set parameter 100 to 1 to reset 101-104 back to default.</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_100_1``` and is of type ```INTEGER```.


### Parameter 101: Temperature Reporting Interval

Temperature Reporting Interval
<p&gtThe interval between when temperature is reported to the gateway</p&gt <p&gt0=Off, 1 = 1 second</p&gt <p&gt(Note: the sensor reporting time will round to the nearest minute)</p&gt
Values in the range 0 to 2678400 may be set.

The manufacturer defined default value is ```7200```.

This parameter has the configuration ID ```config_101_4``` and is of type ```INTEGER```.


### Parameter 102: Humidity Reporting Interval

Humidity Reporting Interval
<p&gtThe interval between when humidity is reported to the gateway</p&gt <p&gt0 = Off, 1 = 1 second</p&gt <p&gt(Note: the sensor reporting time will round to the nearest minute)</p&gt
Values in the range 0 to 2678400 may be set.

The manufacturer defined default value is ```7200```.

This parameter has the configuration ID ```config_102_4``` and is of type ```INTEGER```.


### Parameter 103: Luminance Reporting Interval

Luminance Reporting Interval
<p&gtThe interval between when luminance is reported to the gateway</p&gt <p&gt0 = Off, 1 = 1 second</p&gt <p&gt(Note: the sensor reporting time will round to the nearest minute)</p&gt
Values in the range 0 to 2678400 may be set.

The manufacturer defined default value is ```7200```.

This parameter has the configuration ID ```config_103_4``` and is of type ```INTEGER```.


### Parameter 104: Battery Reporting Interval

Battery Reporting Interval
<p&gtThe interval between when battery is reported to the gateway</p&gt <p&gt0 = Off, 1 = 1 second</p&gt <p&gt(Note: the sensor reporting time will round to the nearest minute)</p&gt
Values in the range 0 to 2678400 may be set.

The manufacturer defined default value is ```86400```.

This parameter has the configuration ID ```config_104_4``` and is of type ```INTEGER```.


### Parameter 110: Change Parameters 111-114 Back to Default Settings

Change Parameters 111-114 Back to Default Settings
<p&gtIf changes are made to parameters 111-114, you can set parameter 110 to 1 to reset 111-114 back to default.</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_110_1``` and is of type ```INTEGER```.


### Parameter 111: Temperature Threshold

Temperature Threshold
<p&gtSet the threshold of the temperature for your sensor</p&gt <p&gt1 = 0.1 degree Celsius, 500 = 50 degrees Celsius</p&gt
Values in the range 1 to 500 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_111_2``` and is of type ```INTEGER```.


### Parameter 112: Humidity Threshold

Humidity Threshold
<p&gtSet the threshold of the humidity for your sensor</p&gt <p&gt1 = 1%, 2 = 2%</p&gt
Values in the range 1 to 32 may be set.

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_112_1``` and is of type ```INTEGER```.


### Parameter 113: Luminance Threshold

Luminance Threshold
<p&gtSet the threshold of the luminance for your sensor</p&gt <p&gt1 = 1 unit lux, 2 = 2 unit lux</p&gt
Values in the range 1 to 65528 may be set.

The manufacturer defined default value is ```150```.

This parameter has the configuration ID ```config_113_2``` and is of type ```INTEGER```.


### Parameter 114: Battery Threshold

Battery Threshold
<p&gtSet the threshold of the battery for your sensor</p&gt <p&gt1 = 1%, 2 = 2%</p&gt
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_114_1``` and is of type ```INTEGER```.

### Wakeup Interval

The wakeup interval sets the period at which the device will listen for messages from the controller. This is required for battery devices that sleep most of the time in order to conserve battery life. The device will wake up at this interval and send a message to the controller to tell it that it can accept messages - after a few seconds, it will go back to sleep if there is no further communications. 

This setting is defined in *seconds*. It is advisable not to set this interval too short or it could impact battery life. A period of 1 hour (3600 seconds) is suitable in most instances.

Note that this setting does not affect the devices ability to send sensor data, or notification events.

This parameter has the configuration ID ```wakeup_interval``` and is of type ```INTEGER```.

### Wakeup Node

When sleeping devices wake up, they send a notification to a listening device. Normally, this device is the network controller, and normally the controller will set this automatically to its own address.
In the event that the network contains multiple controllers, it may be necessary to configure this to a node that is not the main controller. This is an advanced setting and should not be changed without a full understanding of the impact.

This parameter has the configuration ID ```wakeup_node``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The HA-ZW-5SABC supports 2 association groups.

### Group 1: Group 1

Sensor will send updates
<ol&gt<li&gtNotification Report<br /&gtSensor will send notification report when motion detection unknown location and (event inactive)</li&gt <li&gtMultilevel Sensor Report<br /&gtSensor will send multilevel sensor report (temperature, humidity, luminance) interval of 2 hours</li&gt <li&gtBattery Report<br /&gtSensor will send battery report when the battery level is low and the battery report's value is 0xFF</li&gt <li&gtDevice Reset Locally</li&gt </ol&gt

Association group 1 supports 5 nodes.

### Group 2: Group 2

Send Basic Set when PIR is triggered

Association group 2 supports 5 nodes.

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
| COMMAND_CLASS_ALARM_V8| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_BATTERY_V1| |
| COMMAND_CLASS_WAKE_UP_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Device Manual](https://opensmarthouse.org/zwavedatabase/1211/AIBASE-Z-Wave-Multi-Sensor-UserGuide-20180606.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1211).
