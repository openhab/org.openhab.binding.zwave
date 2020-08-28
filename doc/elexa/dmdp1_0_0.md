---
layout: documentation
title: DMDP1 - ZWave
---

{% include base.html %}

# DMDP1 Dome Window and Door Sensor Pro
This describes the Z-Wave device *DMDP1*, manufactured by *Elexa Consumer Products Inc.* with the thing type UID of ```elexa_dmdp1_00_000```.

The device is in the category of *Sensor*, defining Device used to measure something.

![DMDP1 product image](https://opensmarthouse.org/zwavedatabase/884/image/)


The DMDP1 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is unable to participate in the routing of data from other devices.

The DMDP1 does not permanently listen for messages sent from the controller - it will periodically wake up automatically to check if the controller has messages to send, but will sleep most of the time to conserve battery life. Refer to the *Wakeup Information* section below for further information.

## Overview

<p&gtFrom Dome Home Automation, monitor every door in your house with the window / door sensor PRO, including your mailbox door! The Door/Window PRO magnet is 1/4” wide, and the magnet can be placed up to one inch away from the sensor which means you can place it nearly anywhere to keep track of openings in places you may have never thought possible. Use the Dome Home Automation Z-Wave Door/Window Sensor PRO to secure cabinets, windows, doors and other fixtures you want to keep closed or monitor activity around. Did we mention the PRO’s battery life is 10 years and it has a range of 260 feet? And just when you thought it couldn't get any better, the new Door/Window Sensor Pro also monitors the ambient temperature! Use the temperature information to monitor for extreme temperature fluctuation and keep your house safe from frozen pipes or damaged flooring.</p&gt

### Inclusion Information

<p&gt<strong&gtInclusion - New Installation</strong&gt</p&gt <ol&gt<li&gtFor proper inclusion, bring the Door/Window Sensor to the final location where it will be used\*.</li&gt <li&gtFollow the instructions for your Z-Wave controller to enter inclusion mode.</li&gt <li&gtRemove the Battery Tab protruding from the Battery Tab Slit.</li&gt <li&gtThe device will automatically enter inclusion mode for two minutes and the LED Indicator will alternate between green and red.</li&gt </ol&gt<p&gtUpon successful inclusion the LED Indicator will flash green three times then stop blinking.</p&gt <p&gt<strong&gtInclusion - Re-Installation</strong&gt</p&gt <ol&gt<li&gtFor proper inclusion, bring the Door/Window Sensorto the final location where it will be used\*.</li&gt <li&gtFollow the instructions for your Z-Wave controller to enter inclusion mode.</li&gt <li&gtRemove the Sensor Cover and the LED Indicator will start blinking yellow.</li&gt <li&gtPress the Button quickly 3 times in a row.</li&gt <li&gtThe device will enter inclusion mode for two minutes and the LED Indicator will alternate between green and red.</li&gt </ol&gt<p&gtAfter successful inclusion, the LED Indicator will flash green three times then blink red until the cover is replaced.</p&gt

### Exclusion Information

<p&gt<strong&gtExclusion</strong&gt</p&gt <p&gtFollow the instructions for your Z-Wave Certified Controller to enter exclusion mode. When prompted by the controller:</p&gt <ol&gt<li&gtRemove the SENSOR COVER and the LED Indicator will start blinking red.</li&gt <li&gtPress the Button quickly 3 times in a row.</li&gt </ol&gt<p&gtThe LED Indicator will flash green three times indicating exclusion/disconnection and will then continue flashing red (indicating tamper) until the cover is replaced.</p&gt

### Wakeup Information

The DMDP1 does not permanently listen for messages sent from the controller - it will periodically wake up automatically to check if the controller has messages to send, but will sleep most of the time to conserve battery life. The wakeup period can be configured in the user interface - it is advisable not to make this too short as it will impact battery life - a reasonable compromise is 1 hour.

The wakeup period does not impact the devices ability to report events or sensor data. The device can be manually woken with a button press on the device as described below - note that triggering a device to send an event is not the same as a wakeup notification, and this will not allow the controller to communicate with the device.


<p&gt<strong&gtWaking Up the Door/Window Sensor Pro</strong&gt</p&gt <p&gtBecause the Mouser is a battery powered device, it wakes up on regular intervals to give battery and other status updates to the controller, as well as to accept configuration settings from the controller. This helps to extend the battery life. The device can be forced to wake up to submit these reports or accept new settings immediately by simply pressing and holding the BUTTON for half a second. The LED INDICATOR will flash once indicating successful wake up.</p&gt

### General Usage Information

<h3&gtDescription & Features</h3&gt <p&gtThe Dome Door/Window Sensor is a battery powered security-enabled\* Z-Wave Plus magnetic reed switch that can monitor the status of doors, windows, and anything else that opens and closes. It can also report ambient temperature levels. The Door/Window Sensor consists of two parts - the “sensor,” and the “magnet.” The sensor has a “reed switch” inside, which is sensitive to the magnet’s presence when aligned properly and within the defined distance. When the sensor and magnet are brought together or pulled apart, the sensor will report its open/close status to its Z-Wave controller, and the encrypted wireless communication\* ensures that user data remains secure. Because many manufacturers use Z-Wave to communicate, the Door/Window Sensor can interact with different products of different categories.</p&gt <p&gt<strong&gtKey Features</strong&gt</p&gt <ul&gt<li&gtTemperature Sensor</li&gt <li&gtUp to 220’ range</li&gt <li&gtTen-Year Battery Life</li&gt <li&gtZ-Wave Signal Strength Indication</li&gt <li&gt1” Max distance between sensor & magnet</li&gt <li&gtUltra-Narrow (1/4”) Magnet (for Placement Between Door and Casing)</li&gt <li&gtZ-Wave Plus Certified</li&gt <li&gtS0 Security-Enabled\*</li&gt <li&gtMonitor doors, windows, medicine cabinets, drawers, garage doors, and many other openings</li&gt </ul&gt<p&gt\*A security-enabled Z-Wave Plus controller is required to use all features of the Door/Window Sensor. </p&gt <h3&gtSpecifications</h3&gt <p&gt<strong&gtTechnical Specifications</strong&gt</p&gt <table&gt<tr&gt<td&gtRadio protocol</td&gt <td&gtZ-Wave(500 series)</td&gt </tr&gt<tr&gt<td&gtPower supply</td&gt <td&gtSingle CR14505 3.6V battery</td&gt </tr&gt<tr&gt<td&gtWorking current</td&gt <td&gt35mA</td&gt </tr&gt<tr&gt<td&gtStandby current</td&gt <td&gt7uA</td&gt </tr&gt<tr&gt<td&gtRadio frequency</td&gt <td&gt908.4 MHz US</td&gt </tr&gt<tr&gt<td&gtRange</td&gt <td&gtUp to 150’ depending on environment</td&gt </tr&gt<tr&gt<td&gtDimensions (L x W x H)</td&gt <td&gtSensor: 2.9” x 0.85” x 0.87” (75 x 21 x 22 mm) <br /&gt Magnet: 1.6” x 0.25” x 0.5” (40 x 6.35 x 12 mm)</td&gt </tr&gt<tr&gt<td&gtPackage Contents</td&gt <td&gtUser Manual, Sensor, Magnet, Battery, Double-Stick Tape, 2x Screws, 2x Wall Anchors</td&gt </tr&gt</table&gt<h3&gtFactory Reset & Misc. Functions</h3&gt <p&gt<strong&gtResetting the Door/Window Sensor Pro</strong&gt</p&gt <p&gtIf needed, the Door/Window Sensor Pro can be reset locally by following these steps.</p&gt Only do this when your Z-Wave controller is disconnected or otherwise unreachable. Beware that resetting your device will disconnect it from the system<ol&gt<li&gtRemove the Sensor Cover and confirm that your Door/Window Sensor is powered up.</li&gt <li&gtWait for 5 seconds.</li&gt <li&gtMove the Magnet and Sensor Base in and out of the closed position 6 times.</li&gt <li&gtThe LED Indicator will light up red for two seconds when reset successfully.</li&gt </ol&gt<p&gtThe Door/Window Sensor’s memory will be erased to factory settings.</p&gt <h3&gtPhysical Installation</h3&gt <p&gtThe Door/Window Sensor can be install with double stick tape or the provided screws. The device should already be included in your Z-Wave system before continuing further.</p&gt <p&gt<strong&gtPre-Installation Checklist</strong&gt</p&gt <ul&gt<li&gtThe Magnet Assembly and Sensor Assembly should be less than 1-1/4” apart when closed (Figure 3.)</li&gt <li&gtHold the Magnet Assembly and Sensor Assembly in place by hand where you wish to install them, move them in and out of the closed position, and make sure the LED Indicator blinks in response. This will confirm that the door and frame are spaced correctly to accommodate the sensor.</li&gt <li&gtWhen moving the Magnet and Sensor in and out of the closed position, a Green followed by another Green or Red LED Indicator corresponds to Excellent or Poor Z-Wave Signal strength respectively.</li&gt <li&gtMake sure the Sensor Cover Release Button will be accessible in the final position.</li&gt <li&gtThe Magnet is small enough (only 1/4” wide) to fit in the groove between the door and the casing.</li&gt <li&gtFinally, confirm that you are still within range of your Z-Wave controller.</li&gt </ul&gt<p&gt Figure 3 - Placing the Door/Window Sensor on the Door</p&gt <p&gt<strong&gtInstallation Using Double-Stick Tape</strong&gt</p&gt <ol&gt<li&gtWipe the door and door-frame clean of dust and anything else that will interfere with the tape’s stickiness.</li&gt <li&gtPeel the double-stick tape and adhere the Sensor Assembly to the door surface.</li&gt <li&gtRepeat the process for the Magnet Assembly, making sure the Magnet Assembly, and Sensor Assembly are no more than 1-1/4” apart when closed. The lines on the sides of the Magnet Assembly, and Sensor Assembly should be in line.</li&gt <li&gtOpen and close the door to make sure the sensor works as expected (the LED Indicator blinks) and that the signal reaches your Z-Wave controller.</li&gt </ol&gt<p&gt<strong&gtInstallation Using Screws</strong&gt</p&gt <p&gt Figure 4 - Installing the Door/Window Sensor With Screws</p&gt <ol&gt<li&gtRemove the Sensor Cover and Battery from the Sensor Base and the Magnet Cover from the Magnet Base.</li&gt <li&gtHold the Sensor Base in place and drive the included screws through the screw holes into the door.</li&gt <li&gtRepeat the process for the Magnet Assembly, making sure the Magnet Assembly, and Sensor Assembly are no more than 1-1/4” apart when the door is closed. The lines on the sides of the MAGNET and SENSOR should be in line.</li&gt <li&gtReplace the Battery, Sensor Cover, and Magnet Cover.</li&gt <li&gtOpen and close the door to make sure the sensor works as expected (the LED Indicator blinks) and that the signal reaches your Z-Wave controller.</li&gt </ol&gt  <h3&gtLED Behavior</h3&gt <table&gt<tr&gt<td&gtGreen</td&gt <td&gtBlinks Twice Within 5 Seconds</td&gt <td&gt…the sensor Detects the Magnet (door) moving, and the Z-Wave signal reached the controller.</td&gt </tr&gt<tr&gt<td&gtBlinks 3 times quickly</td&gt <td&gt…the device is successfully included into the system.</td&gt </tr&gt<tr&gt<td&gtRed</td&gt <td&gtStays on for 2 seconds</td&gt <td&gt…the device is reset to factory settings.</td&gt </tr&gt<tr&gt<td&gtBlinks 3 Times quickly</td&gt <td&gt… the Button is pressed 3 times quickly and the device is excluded from the network.</td&gt </tr&gt<tr&gt<td&gtBlinks indefinitely</td&gt <td&gt…the Sensor Cover is removed and the device is already included in a system.</td&gt </tr&gt<tr&gt<td&gtYellow (Green + Red)</td&gt <td&gtBlinks Indefinitely</td&gt <td&gt…the Sensor Cover is removed and the device is not yet included in a system.</td&gt </tr&gt<tr&gt<td&gtRed & Green Alternating</td&gt <td&gtTwo Flashes (Green-Red) Within 5 Seconds</td&gt <td&gt…the sensor Detects the Magnet moving away or getting close (as the Door is opened or closed) and the Z-Wave signal was not received.</td&gt </tr&gt<tr&gt<td&gtFlashing in alternating colors</td&gt <td&gt…the Door/Window Sensor is in inclusion mode. It will continue blinking and remain in inclusion mode for up to two minutes, until it is included in a system.</td&gt </tr&gt</table&gt<p&gt Table 2 - LED Behavior</p&gt   <h3&gtButton Behavior</h3&gt <table&gt<tr&gt<td&gtOpen the Cover</td&gt <td&gtDoor/Window Sensor Not Yet Included in System</td&gt <td&gtLED Indicator blinks yellow (red + green) until the Sensor Cover is replaced</td&gt </tr&gt<tr&gt<td&gtDoor/Window Sensor Already Included in System</td&gt <td&gtDevice sends a tamper notification to its controller, and the LED Indicator blinks red until the Sensor Cover is replaced</td&gt </tr&gt<tr&gt<td&gtPush CONNECT BUTTON 3 Times</td&gt <td&gtDoor/Window Sensor Already Included in System</td&gt <td&gtDevice sends node info to Group 1</td&gt </tr&gt<tr&gt<td&gtDoor/Window Sensor Already Included, and Controller is in Exclusion Mode</td&gt <td&gtDevice is excluded from the system and removes the Home ID from its memory</td&gt </tr&gt<tr&gt<td&gtDoor/Window Sensor Not Yet Included in System, and Controller is in Inclusion Mode</td&gt <td&gtDevice enters inclusion mode and includes into whichever network is also in inclusion mode</td&gt </tr&gt<tr&gt<td&gtMove the Magnet and Sensor In and Out of the Closed Position 6 Times with Cover Removed</td&gt <td&gtDoor/Window Sensor Already Included in System</td&gt <td&gtDevice will be reset to factory settings, and a DEVICE\_RESET\_LOCALLY command will be sent to Group 1</td&gt </tr&gt<tr&gt<td&gtAny condition (as long as the device has power)</td&gt <td&gtThe device’s memory will erase to factory default settings and any associations, configuration parameters, and other locally saved data will be lost</td&gt </tr&gt<tr&gt<td&gtPress the Button Once</td&gt <td&gtDoor/Window Sensor Is Already Included in System</td&gt <td&gtDevice sends a wake up notification to Node 1.</td&gt </tr&gt</table&gt<p&gt Table 3 - Button Behavior</p&gt

## Channels

The following table summarises the channels available for the DMDP1 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Binary Sensor | sensor_binary | sensor_binary |  | Switch | 
| Alarm (access) | alarm_access | alarm_access | Door | Switch | 
| Alarm (burglar) | alarm_burglar | alarm_burglar | Door | Switch | 
| Battery Level | battery-level | system.battery_level | Battery | Number |

### Binary Sensor
Indicates if a sensor has triggered.

The ```sensor_binary``` channel is of type ```sensor_binary``` and supports the ```Switch``` item. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| ON | Triggered |
| OFF | Untriggered |

### Alarm (access)
Indicates if the access control alarm is triggered.

The ```alarm_access``` channel is of type ```alarm_access``` and supports the ```Switch``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Alarm (burglar)
Indicates if the burglar alarm is triggered.

The ```alarm_burglar``` channel is of type ```alarm_burglar``` and supports the ```Switch``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

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

The following table provides a summary of the 9 configuration parameters available in the DMDP1.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Enable/Disable LED Indicator | Enable/Disable LED Indicator |
| 2 | Configure Open/Close Reports Sent | Configure Open/Close Reports Sent |
| 3 | Switch Between Notification and Binary Sensor | Switch Between Notification and Binary Sensor Command Classes |
| 4 | Enable/Disable Temperature Sensor | Enable/Disable Temperature Sensor |
| 5 | Temperature offset | Temperature offset |
| 6 | Temperature Units | Temperature Units |
| 7 | Basic Set Value (Group 2) | Basic Set Value (Group 2) |
| 8 | Basic Set Value (Group 3) | Basic Set Value (Group 3) |
| 9 | Basic Set Value (Group 4) | Basic Set Value (Group 4) |
|  | Wakeup Interval | Sets the interval at which the device will accept commands from the controller |
|  | Wakeup Node | Sets the node ID of the device to receive the wakeup notifications |

### Parameter 1: Enable/Disable LED Indicator

Enable/Disable LED Indicator
<p&gtThis parameter enables or disables the LED Indicator flashing to indicate opening and closing events. The Door/Window Sensor will always flash the LED Indicator for inclusion, exclusion, tamper, and other system events.</p&gt <table&gt<tr&gt<td&gtSize</td&gt <td&gtName</td&gt <td&gtAvailable Values</td&gt <td&gtDefault Value</td&gt </tr&gt<tr&gt<td&gt01</td&gt <td&gtEnable/Disable LED Indicator</td&gt <td&gt00 (Open/Close LED Indicator Disabled)<br /&gt01 (Open/Close LED Indicator Enabled)</td&gt <td&gt01 <br /&gt (Open/Close LED Indicator Enabled)</td&gt </tr&gt</table&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Open/Close LED Indicator Disabled |
| 1 | Open/Close LED Indicator Enabled |

The manufacturer defined default value is ```1``` (Open/Close LED Indicator Enabled).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: Configure Open/Close Reports Sent

Configure Open/Close Reports Sent
<p&gtThis parameter switches the Notification or Binary Sensor report sent when the door opens or closes. If this parameter is set to 01, the Door/Window Sensor will report “closed” instead of “open”when the Magnet and Sensor are pulled apart.</p&gt <table&gt<tr&gt<td&gtSize</td&gt <td&gtName</td&gt <td&gtAvailable Values</td&gt <td&gtDefault Value</td&gt </tr&gt<tr&gt<td&gt01</td&gt <td&gtConfigure Open/Close Reports Sent</td&gt <td&gt00 (Reports Closed when Magnet is close)<br /&gt01 (Reports Open when Magnet is close)</td&gt <td&gt00 <br /&gt (Reports Closed when Magnet is close)</td&gt </tr&gt</table&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Reports Closed when Magnet is close |
| 1 | Reports Open when Magnet is close |

The manufacturer defined default value is ```0``` (Reports Closed when Magnet is close).

This parameter has the configuration ID ```config_2_1``` and is of type ```INTEGER```.


### Parameter 3: Switch Between Notification and Binary Sensor

Switch Between Notification and Binary Sensor Command Classes
<p&gtBy default, the Door/Window Sensor uses the Notification command class to communicate open/close and tamper events. If this Parameter is set to 1, the device will instead use the Binary Sensor command class.</p&gt <table&gt<tr&gt<td&gtSize</td&gt <td&gtName</td&gt <td&gtAvailable Values</td&gt <td&gtDefault Value</td&gt </tr&gt<tr&gt<td&gt01</td&gt <td&gtSwitch Between Notification and Binary Sensor Command Classes</td&gt <td&gt00 (Notification Command Class is Used)<br /&gt01 (Binary Sensor Command Class is Used)</td&gt <td&gt00 <br /&gt (Notification Command Class is Used)</td&gt </tr&gt</table&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Notification Command Class is Used |
| 1 | Binary Sensor Command Class is Used |

The manufacturer defined default value is ```0``` (Notification Command Class is Used).

This parameter has the configuration ID ```config_3_1``` and is of type ```INTEGER```.


### Parameter 4: Enable/Disable Temperature Sensor

Enable/Disable Temperature Sensor
<p&gtThe Door/Window Sensor can also monitor and report ambient temperature conditions. However, temperature sensing functionality is disabled by default to save battery life, and this configuration parameter is used to enable or disable this feature.</p&gt <table&gt<tr&gt<td&gtSize</td&gt <td&gtName</td&gt <td&gtAvailable Values</td&gt <td&gtDefault Value</td&gt </tr&gt<tr&gt<td&gt01</td&gt <td&gtEnable/Disable Temperature Sensor</td&gt <td&gt00 (Temperature Sensor Disabled)<br /&gt01 (Temperature Sensor Enabled)</td&gt <td&gt00 <br /&gt (Temperature Sensor Disabled) </td&gt </tr&gt</table&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Temperature Sensor Disabled |
| 1 | Temperature Sensor Enabled |

The manufacturer defined default value is ```0``` (Temperature Sensor Disabled).

This parameter has the configuration ID ```config_4_1``` and is of type ```INTEGER```.


### Parameter 5: Temperature offset

Temperature offset
<p&gtThis Configuration Parameter can offset the temperature value reported by ± 125 degrees to compensate for temperature variances in a room - for example, the temperature near the ceiling in a room is significantly higher than the average temperature of the same room. This Parameter accepts a signed 8-bit value with an absolute value of up to 125, and its units are Dependant on Configuration Parameter 07</p&gt <table&gt<tr&gt<td&gtSize</td&gt <td&gtName</td&gt <td&gtAvailable Values</td&gt <td&gtDefault Value</td&gt </tr&gt<tr&gt<td&gt01</td&gt <td&gtTemperature offset</td&gt <td&gt00 ~ 7D (0°~125°)<br /&gt83 ~ FF (-125° ~ -1°)</td&gt <td&gt00 <br /&gt (0° Offset) </td&gt </tr&gt</table&gt
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_5_1``` and is of type ```INTEGER```.


### Parameter 6: Temperature Units

Temperature Units
<p&gtThe Door/Window Sensor can report temperature values in either Celsius or Fahrenheit, and this parameter allows the user to decide which unit is used.</p&gt <table&gt<tr&gt<td&gtSize</td&gt <td&gtName</td&gt <td&gtAvailable Values</td&gt <td&gtDefault Value</td&gt </tr&gt<tr&gt<td&gt01</td&gt <td&gtTemperature Units</td&gt <td&gt00 (Temperature Reported in Fahrenheit)<br /&gt01 (Temperature Reported in Celsius)</td&gt <td&gt00 <br /&gt (Temperature Reported in Fahrenheit) </td&gt </tr&gt</table&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Temperature Reported in Fahrenheit |
| 1 | Temperature Reported in Celsius |

The manufacturer defined default value is ```0``` (Temperature Reported in Fahrenheit).

This parameter has the configuration ID ```config_6_1``` and is of type ```INTEGER```.


### Parameter 7: Basic Set Value (Group 2)

Basic Set Value (Group 2)
<p&gtThis parameter sets the value sent by the Basic Set command to Association Group 2 (for more information, see “Association Group Info”.)</p&gt <table&gt<tr&gt<td&gtSize</td&gt <td&gtName</td&gt <td&gtAvailable Values</td&gt <td&gtDefault Value</td&gt </tr&gt<tr&gt<td&gt01</td&gt <td&gtBasic Set Value (Group 2)</td&gt <td&gt00 (0/Turn Off Device)<br /&gt01 ~ 63 (0-99)<br /&gtFF (255/Turn On Device)</td&gt <td&gtFF <br /&gt (255/Turn On Device) </td&gt </tr&gt</table&gt
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_7_1``` and is of type ```INTEGER```.


### Parameter 8: Basic Set Value (Group 3)

Basic Set Value (Group 3)
<p&gtThis parameter sets the value sent by the Basic Set command to Association Group 3. The Door/Window Sensor sends a Basic Set command to Association Group 4 when the door or window opens (for more information, see “Association Group Info”.)</p&gt <table&gt<tr&gt<td&gtSize</td&gt <td&gtName</td&gt <td&gtAvailable Values</td&gt <td&gtDefault Value</td&gt </tr&gt<tr&gt<td&gt01</td&gt <td&gtBasic Set Value (Group 2)</td&gt <td&gt00 (0/Turn Off Device)<br /&gt01 ~ 63 (0-99)<br /&gtFF (255/Turn On Device)</td&gt <td&gtFF <br /&gt (255/Turn On Device)</td&gt </tr&gt</table&gt
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_8_1``` and is of type ```INTEGER```.


### Parameter 9: Basic Set Value (Group 4)

Basic Set Value (Group 4)
<p&gtThis parameter sets the value sent by the Basic Set command to Association Group 4.The Door/Window Sensor sends a Basic Set command to Association Group 4 when the door or window opens (for more information, see “Association Group Info”.)</p&gt <table&gt<tr&gt<td&gtSize</td&gt <td&gtName</td&gt <td&gtAvailable Values</td&gt <td&gtDefault Values</td&gt </tr&gt<tr&gt<td&gt01</td&gt <td&gtBasic Set Value (Group 4)</td&gt <td&gt00 (0/Turn Off Device)<br /&gt01 ~ 63 (0-99)<br /&gtFF (-1/Turn On Device)</td&gt <td&gtFF <br /&gt (-1/Turn On Device) </td&gt </tr&gt</table&gt
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_9_1``` and is of type ```INTEGER```.

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

The DMDP1 supports 5 association groups.

### Group 1: Group 1

Group 1 (Lifeline Group)
<p&gtGroup 1 is the “Lifeline” group, which can hold five members, typically including the main Z-Wave controller. The Door/Window Sensor sends this group a Notification Report or a Binary Sensor Report when it is opened or closed (see Configuration Parameter 3.) It also sends this group a multilevel sensor report to report the temperature and a Battery Report in response to Battery Get commands.</p&gt

Association group 1 supports 5 nodes.

### Group 2: Group 2

Group 2
<p&gtGroup 2 supports up to 5 members and the Door/Window Sensor sends a Basic Set command to this group (or the Control Group) to directly trigger devices (like a light, chime, etc.) when the tamper switch either opens or closes. The value of the Basic Set command (e.g. brightness of the lamp) is configured using configuration parameter 07.</p&gt

Association group 2 supports 5 nodes.

### Group 3: Group 3

Group 3
<p&gtGroup 3 supports up to 5 members and the Door/Window Sensor sends it a NOTIFICATION\_REPORT or SENSOR\_BINARY_REPORT when the tamper switch either opens or closes.</p&gt

Association group 3 supports 5 nodes.

### Group 4: Group 4

Group 4
<p&gtGroup 4 supports up to 5 members and the Door/Window Sensor sends a Basic Set command to this group to directly trigger devices (like a light, chime, etc.) when the sensor detects the door/window opening. The value of the Basic Set command (e.g. brightness of the lamp) is configured using configuration parameter 08 and 09.</p&gt

Association group 4 supports 5 nodes.

### Group 5: Group 5

Group 5
<p&gtGroup 5 supports up to 5 members and the Door/Window Sensor sends it a NOTIFICATION\_REPORT or SENSOR\_BINARY_REPORT when the sensor detects the door/window opening.</p&gt

Association group 5 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_SENSOR_BINARY_V2| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V4| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_BATTERY_V1| |
| COMMAND_CLASS_WAKE_UP_V2| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Dome Door Sensor Manual](https://opensmarthouse.org/zwavedatabase/884/Dome-Door-Sensor-Manual.pdf)
* [Dome API Documentation](https://opensmarthouse.org/zwavedatabase/884/Dome-API-Documentation.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/884).
