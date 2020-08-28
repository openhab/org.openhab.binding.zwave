---
layout: documentation
title: ZMNHOD - ZWave
---

{% include base.html %}

# ZMNHOD Flush Shutter DC
This describes the Z-Wave device *ZMNHOD*, manufactured by *[Goap](http://www.qubino.com/)* with the thing type UID of ```qubino_zmnhod_00_000```.

The device is in the category of *Blinds*, defining Roller shutters, window blinds, etc..

![ZMNHOD product image](https://opensmarthouse.org/zwavedatabase/214/image/)


The ZMNHOD supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtThis Z-Wave module is used to control the motor of blinds, rollers, shades, venetian blinds, etc.. The module can be controlled either through a Z-Wave network or through the wall switch.</p&gt

### Inclusion Information

<ul&gt<li&gtpress service button S for more than 2 second or</li&gt <li&gtpress push button I1 three times within 3s (3 times change switch state within 3 seconds)</li&gt </ul&gt

### Exclusion Information

<ul&gt<li&gtpress service button S for more than 6 second or</li&gt <li&gtpress push button I1 five times within 3s (5 times change switch state within 3 seconds) in the first 60 seconds after the module is connected to the power supply.</li&gt </ul&gt

## Channels

The following table summarises the channels available for the ZMNHOD -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 
| Blinds control | blinds_control | blinds_control | Blinds | Rollershutter | 
| Binary Sensor | sensor_binary | sensor_binary |  | Switch | 
| Sensor (temperature) | sensor_temperature | sensor_temperature | Temperature | Number:Temperature | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Switch 1 | switch_binary1 | switch_binary | Switch | Switch | 
| Blinds control 1 | blinds_control1 | blinds_control | Blinds | Rollershutter | 
| Electric meter (watts) 1 | meter_watts1 | meter_watts | Energy | Number | 
| Electric meter (kWh) 1 | meter_kwh1 | meter_kwh | Energy | Number | 
| Sensor (temperature) 2 | sensor_temperature2 | sensor_temperature | Temperature | Number:Temperature | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Blinds control
Provides start / stop control of blinds.

The ```blinds_control``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.

### Binary Sensor
Indicates if a sensor has triggered.

The ```sensor_binary``` channel is of type ```sensor_binary``` and supports the ```Switch``` item. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| ON | Triggered |
| OFF | Untriggered |

### Sensor (temperature)
Indicates the current temperature.

The ```sensor_temperature``` channel is of type ```sensor_temperature``` and supports the ```Number:Temperature``` item and is in the ```Temperature``` category.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Switch 1
Switch the power on and off.

The ```switch_binary1``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Blinds control 1
Provides start / stop control of blinds.

The ```blinds_control1``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.

### Electric meter (watts) 1
Indicates the instantaneous power consumption.

The ```meter_watts1``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh) 1
Indicates the energy consumption (kWh).

The ```meter_kwh1``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Sensor (temperature) 2
Indicates the current temperature.

The ```sensor_temperature2``` channel is of type ```sensor_temperature``` and supports the ```Number:Temperature``` item and is in the ```Temperature``` category.



## Device Configuration

The following table provides a summary of the 14 configuration parameters available in the ZMNHOD.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 10 | Activate/deactivate functions ALL ON / ALL OFF  | Activate/deactivate functions ALL ON / ALL OFF |
| 40 | Power report (Watts) on power change for Q1 or Q2 | Power report (Watts) on power change for Q1 or Q2 |
| 42 | Power report (Watts) by time interval for Q1 or Q2 | Power report (Watts) by time interval for Q1 or Q2 |
| 71 | Operating modes | Operating modes |
| 72 | Slats tilting full turn time | Slats tilting full turn time |
| 73 | Slats position | Slats position |
| 74 | Motor moving up/down time | Motor moving up/down time |
| 76 | Motor operation detection | Motor operation detection |
| 78 | Forced Shutter DC calibration | Forced Shutter DC calibration |
| 85 | Power consumption max delay time | Power consumption max delay time |
| 86 | Power consumption at limit switch delay time | Power consumption at limit switch delay time |
| 90 | Time delay for next motor movement | Time delay for next motor movement |
| 110 | Temperature sensor offset settings | Temperature sensor offset settings |
| 120 | Digital temperature sensor reporting | Digital temperature sensor reporting |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 10: Activate/deactivate functions ALL ON / ALL OFF 

Activate/deactivate functions ALL ON / ALL OFF
<p&gtModule responds to commands ALL ON / ALL OFF that may be sent by the main controller or by other controllerbelonging to the system.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | ALL ON is not active, ALL OFF is not active |
| 1 | ALL ON is not active ALL OFF active |
| 2 | ALL ON is not active ALL OFF is not active |
| 255 | ALL ON active, ALL OFF active |

The manufacturer defined default value is ```255``` (ALL ON active, ALL OFF active).

This parameter has the configuration ID ```config_10_2``` and is of type ```INTEGER```.


### Parameter 40: Power report (Watts) on power change for Q1 or Q2

Power report (Watts) on power change for Q1 or Q2
<p&gtSet value means percentage, set value from 0 – 100 = 0% - 100%.</p&gt <p&gtAvailable configuration parameters </p&gt <ul&gt<li&gtdefault value 1</li&gt <li&gt0 - reporting disabled</li&gt <li&gt1 - 100 = 1% - 100% Reporting enabled. Power report is send (push) only when actual power (in Watts) in real time changes for more than set percentage comparing to previous actual power in Watts, step is 1%.</li&gt </ul&gt<p&gt<strong&gtNOTE</strong&gt: if power changed is less than 1W, the report is not send (pushed), independent of percentage set. </p&gt
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_40_1``` and is of type ```INTEGER```.


### Parameter 42: Power report (Watts) by time interval for Q1 or Q2

Power report (Watts) by time interval for Q1 or Q2
<p&gtSet value means time interval (0 – 32767) in seconds, when power report is send.</p&gt <p&gtAvailable configuration parameters</p&gt <ul&gt<li&gtdefault value 300 = 300s</li&gt <li&gt0 - Reporting Disabled</li&gt <li&gt1 - 32767 = 1 second - 32767 seconds. Reporting enabled, power report is send with time interval set by entered value. </li&gt </ul&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```300```.

This parameter has the configuration ID ```config_42_2``` and is of type ```INTEGER```.


### Parameter 71: Operating modes

Operating modes
<p&gtThis parameter defines selection between two available operating modes.</p&gt <p&gtAvailable configuration parameters </p&gt <ul&gt<li&gtdefault value 0</li&gt <li&gt0 - Shutter mode</li&gt <li&gt1 - venetian mode (up/down and slate rotation)</li&gt </ul&gt<p&gt<strong&gtNOTE1</strong&gt: After parameter change, first exclude module (without setting parameters to default value) then wait at least 30s and then re include the module!</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Shutter mode. |
| 1 | Venetian mode (up/down and slate rotation) |

The manufacturer defined default value is ```0``` (Shutter mode.).

This parameter has the configuration ID ```config_71_1``` and is of type ```INTEGER```.


### Parameter 72: Slats tilting full turn time

Slats tilting full turn time
<p&gtThis parameter defines the time necessary for slats to make full turn (180 degrees).</p&gt <p&gtAvailable configuration parameters </p&gt <ul&gt<li&gtdefault value 150 = 1,5 seconds</li&gt <li&gt0 - tilting time disabled</li&gt <li&gt1 - 32767 = 0,01seconds - 327,67 seconds</li&gt </ul&gt<p&gt<strong&gtNOTE</strong&gt: If time set is too high, this will result that after full turn, Shutter will start move up/down, for time remaining. </p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```150```.

This parameter has the configuration ID ```config_72_2``` and is of type ```INTEGER```.


### Parameter 73: Slats position

Slats position
<p&gtThis parameter defines slats position after up/down movement through Z-wave or push-buttons.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Return to previous position only with Z-wave |
| 1 | Return to previous position with Z-wave or button |

The manufacturer defined default value is ```1``` (Return to previous position with Z-wave or button).

This parameter has the configuration ID ```config_73_1``` and is of type ```INTEGER```.


### Parameter 74: Motor moving up/down time

Motor moving up/down time
<p&gtThis parameter defines Shutter motor moving time of complete opening or complete closing.</p&gt <p&gtAvailable configuration parameters</p&gt <ul&gt<li&gtdefault value 0</li&gt <li&gt0 - moving time disabled (working with limit switches)</li&gt <li&gt1 - 32767 = 0,1seconds - 3276,7seconds. After that time motor is stopped (relay goes to off state)</li&gt </ul&gt<p&gt<strong&gtNOTE</strong&gt: Important is that the reference position to manually set moving time is always Shutter lower position!</p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_74_2``` and is of type ```INTEGER```.


### Parameter 76: Motor operation detection

Motor operation detection
<p&gtPower threshold to be interpreted when motor reach the limit switch.</p&gt <p&gtAvailable configuration parameters</p&gt <ul&gt<li&gt default value 6 = 0,6W</li&gt <li&gt5 - 100 (0,5W - 10W), step is 0,1W.</li&gt </ul&gt<p&gt<strong&gtNOTE</strong&gt: Motors with power consumption less than 0,5W could not be auto calibrated. In that case set time manually (par. 74).</p&gt
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```6```.

This parameter has the configuration ID ```config_76_1``` and is of type ```INTEGER```.


### Parameter 78: Forced Shutter DC calibration

Forced Shutter DC calibration
<p&gtBy modifying the parameters setting from 0 to 1 a Shutter DC module enters the calibration mode.</p&gt <p&gtAvailable configuration parameters </p&gt <ul&gt<li&gtdefault value 0</li&gt <li&gt1 - Start calibration process (when calibration process is finished, completing full cycle - up, down and up, set the parameter 78 (Forced Shutter calibration) value back to 0</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Default |
| 1 | Start calibration process. |

The manufacturer defined default value is ```0``` (Default).

This parameter has the configuration ID ```config_78_1``` and is of type ```INTEGER```.


### Parameter 85: Power consumption max delay time

Power consumption max delay time
<p&gtThis parameter defines the max time before motor power consumption is read after one of the relays is switched ON. If there is no power consumption during this max time (motor not connected, damaged or requires higher time to start, motor in end position,...) the relay will switch OFF. Time is defined by entering it manually.</p&gt <p&gtAvailable configuration parameters</p&gt <ul&gt<li&gtdefault value 8 = 800ms</li&gt <li&gt3 - 50 = 0,3seconds - 5seconds (100ms resolution)</li&gt </ul&gt
Values in the range 3 to 50 may be set.

The manufacturer defined default value is ```8```.

This parameter has the configuration ID ```config_85_1``` and is of type ```INTEGER```.


### Parameter 86: Power consumption at limit switch delay time

Power consumption at limit switch delay time
<p&gtThis parameter defines the max time at limit switch, when power consumption is below power threshold. If the power consumption during this time is below power threshold (par. 76), the active output will switch off, means that limit switch is reached.</p&gt <p&gtAvailable configuration parameters </p&gt <ul&gt<li&gtdefault value 8 = 800ms</li&gt <li&gt3 - 50 = 0,3seconds - 5seconds (100ms resolution) </li&gt </ul&gt
Values in the range 3 to 50 may be set.

The manufacturer defined default value is ```8```.

This parameter has the configuration ID ```config_86_1``` and is of type ```INTEGER```.


### Parameter 90: Time delay for next motor movement

Time delay for next motor movement
<p&gtThis parameter defines the minimum time delay between next motor movement (minimum time between switching motor off and on again).</p&gt <p&gtAvailable configuration parameters </p&gt <ul&gt<li&gtdefault value 5 = 500ms</li&gt <li&gt1 - 30 = 0,1seconds - 3seconds (100ms resolution)</li&gt </ul&gt
Values in the range 1 to 30 may be set.

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_90_1``` and is of type ```INTEGER```.


### Parameter 110: Temperature sensor offset settings

Temperature sensor offset settings
<p&gtSet value is added or subtracted to actual measured value by sensor.</p&gt <p&gtAvailable configuration parameters</p&gt <ul&gt<li&gtdefault value 32536  32536 - offset is 0.0C</li&gt <li&gtFrom 1 to 100 - value from 0.1 °C to 10.0 °C is added to actual measured temperature.</li&gt <li&gtFrom 1001 to 1100 - value from -0.1 °C to -10.0 °C is subtracted to actual measured temperature.</li&gt </ul&gt
Values in the range 1 to 32536 may be set.

The manufacturer defined default value is ```32536```.

This parameter has the configuration ID ```config_110_2``` and is of type ```INTEGER```.


### Parameter 120: Digital temperature sensor reporting

Digital temperature sensor reporting
<p&gtIf digital temperature sensor is connected, module reports measured temperature on temperature change defined by this parameter.</p&gt <p&gtAvailable configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gtDefault value 5 = 0,5°C</li&gt <li&gt0 - reporting disabled</li&gt <li&gt1-127 = 0,1°C - 12,7°C, step is 0,1°C</li&gt </ul&gt
Values in the range 0 to 127 may be set.

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_120_1``` and is of type ```INTEGER```.

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

The ZMNHOD supports 9 association groups.

### Group 1: Default reporting group


Association group 1 supports 1 node.

### Group 2: Basic on/off -  input I1

Triggered at change of the input I1 state and reflecting its state

Association group 2 supports 16 nodes.

### Group 3: Basic on/off - input I2

Triggered at change of the input I2 state and reflecting its state

Association group 3 supports 16 nodes.

### Group 4: Basic on/off - direction of roller

Triggered at sensing moving direction of roller
<p&gtup=FF, down=0</p&gt

Association group 4 supports 16 nodes.

### Group 5: Basic on/off - roller position

Triggered at reaching roller position
<p&gtbottom=FF, top=0</p&gt

Association group 5 supports 16 nodes.

### Group 6: Basic on/off

Triggered at reaching roller position
<p&gtbottom=FF, not bottom=0</p&gt

Association group 6 supports 16 nodes.

### Group 7:  Multilevel set

triggered at changes of value of the Flush Shutter DC position

Association group 7 supports 16 nodes.

### Group 8: Multilevel set

Triggered at changes of value of slats tilting position

Association group 8 supports 16 nodes.

### Group 9: Multilevel sensor report

Triggered at change of temperature sensor

Association group 9 supports 16 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| |
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_SENSOR_BINARY_V1| |
| COMMAND_CLASS_SENSOR_MULTILEVEL_V7| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V1| |
#### Endpoint 1

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| |
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |
#### Endpoint 2

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SENSOR_MULTILEVEL_V7| Linked to BASIC|
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |

### Documentation Links

* [User Manual](https://opensmarthouse.org/zwavedatabase/214/Qubino-Flush-Shutter-DC-PLUS-user-manual-V1-4-eng.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/214).
