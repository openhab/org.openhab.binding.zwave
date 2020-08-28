---
layout: documentation
title: ZMNHCA - ZWave
---

{% include base.html %}

# ZMNHCA Flush Shutter
This describes the Z-Wave device *ZMNHCA*, manufactured by *[Goap](http://www.qubino.com/)* with the thing type UID of ```qubino_zmnhca_00_000```.

The device is in the category of *Blinds*, defining Roller shutters, window blinds, etc..

![ZMNHCA product image](https://opensmarthouse.org/zwavedatabase/635/image/)


The ZMNHCA supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtFlush Shutter module is used to control the motor of blinds, rollers, shades, garage doors, gates, venetian blinds (including slates tilting positioning), etc… The module can be controlled either through a Z-Wave network or through the wall switch.</p&gt

### Inclusion Information

<ul&gt<li&gtpress service button S for more than 2 second or</li&gt <li&gtpress push button I1 three times within 3s (3 times change switch state within 3 seconds)</li&gt </ul&gt

### Exclusion Information

<ul&gt<li&gtpress service button S for more than 6 second or</li&gt <li&gtpress push button I1 five times within 3s (5 times change switch state within 3 seconds) in the first 60 seconds after the module is connected to the power supply</li&gt </ul&gt

## Channels

The following table summarises the channels available for the ZMNHCA -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 
| Blinds Control | blinds_control | blinds_control | Blinds | Rollershutter | 
| Sensor (temperature) | sensor_temperature | sensor_temperature | Temperature | Number:Temperature | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| switch | switch_binary1 | switch_binary | Switch | Switch | 
| Blinds Control | blinds_control1 | blinds_control | Blinds | Rollershutter | 
| Temperatur Sensor | sensor_temperature1 | sensor_temperature | Temperature | Number:Temperature | 
| Electric meter (watts) | meter_watts1 | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh1 | meter_kwh | Energy | Number | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Blinds Control
Provides start / stop control of blinds.

The ```blinds_control``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.

### Sensor (temperature)
Indicates the current temperature.

The ```sensor_temperature``` channel is of type ```sensor_temperature``` and supports the ```Number:Temperature``` item and is in the ```Temperature``` category.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### switch
Switch the power on and off.

The ```switch_binary1``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Blinds Control
Provides start / stop control of blinds.

The ```blinds_control1``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.

### Temperatur Sensor
Indicates the current temperature.

The ```sensor_temperature1``` channel is of type ```sensor_temperature``` and supports the ```Number:Temperature``` item and is in the ```Temperature``` category.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts1``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh1``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.



## Device Configuration

The following table provides a summary of the 12 configuration parameters available in the ZMNHCA.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 10 | ALL ON / ALL OFF | Responds to commands ALL ON / ALL OFF from Main Controller |
| 40 | Reporting in Watts on power change for Q1 or Q2 | Power consumption change threshold for sending updates |
| 42 | Reporting in Watts by time interval for Q1 or Q2 | Power reporting in Watts by time interval for Q1 or Q2 |
| 71 | Operating modes | Operation Mode (Shutter or Venetian) |
| 72 | Slats tilting full turn time | Slat full turn time in tenths of a second. |
| 73 | Slats position | Slats position |
| 74 | Motor moving up/down time | Shutter motor moving time of complete opening or complete closing |
| 76 | Motor operation detection | Power threshold |
| 78 | Forced Shutter calibration | 0 to 1 a Shutter enters the calibration mode |
| 80 | Reporting to controller | reports regarding to main controller |
| 85 | Power consumption max delay time | max time before motor power consumption is read |
| 90 | Relay delay time | This parameter defines the time delay between output relay switching |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 10: ALL ON / ALL OFF

Responds to commands ALL ON / ALL OFF from Main Controller
<p&gtModule responds to commands ALL ON / ALL OFF that may be sent by the main controller or by other controller belonging to the system.</p&gt <p&gtAvailable config. parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gt default value 255</li&gt <li&gt 255 - ALL ON active, ALL OFF active.</li&gt <li&gt 0 - ALL ON is not active, ALL OFF is not active</li&gt <li&gt 1 - ALL ON is not active ALL OFF active</li&gt <li&gt 2 - ALL ON active ALL OFF is not active</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | ALL ON is not active ALL OFF is not active |
| 1 | ALL ON is not active ALL OFF active |
| 2 | ALL ON is inactive ALL OFF is inactive |
| 255 | ALL ON active, ALL OFF active |

The manufacturer defined default value is ```255``` (ALL ON active, ALL OFF active).

This parameter has the configuration ID ```config_10_2``` and is of type ```INTEGER```.


### Parameter 40: Reporting in Watts on power change for Q1 or Q2

Power consumption change threshold for sending updates
<p&gtPower report is send (push) only when actual power (in Watts) in real time changes for more than set percentage comparing to previous actual power in Watts, step is 1%.</p&gt <p&gtSet value means percentage, set value from 0 – 100 = 0% - 100%. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 1</li&gt <li&gt 0 - reporting disabled</li&gt <li&gt 1 - 100 = 1% - 100% Reporting enabled.</li&gt </ul&gt<p&gtNOTE: if power changed is less than 1W, the report is not send (pushed), independent of percentage set.</p&gt
Values in the range 1 to 100 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_40_1``` and is of type ```INTEGER```.


### Parameter 42: Reporting in Watts by time interval for Q1 or Q2

Power reporting in Watts by time interval for Q1 or Q2
<p&gtSet value means time interval (0 – 32767) in seconds, when power report is send. Available configuration parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gt default value 300 = 300s</li&gt <li&gt 0 - Reporting Disabled</li&gt <li&gt 1 - 32767 = 1 second - 32767 seconds. Reporting enabled, power report is send with time interval set by entered value.</li&gt </ul&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```300```.

This parameter has the configuration ID ```config_42_2``` and is of type ```INTEGER```.


### Parameter 71: Operating modes

Operation Mode (Shutter or Venetian)
<p&gtThis parameter defines selection between two available operating modes. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 0</li&gt <li&gt 0 - Shutter mode</li&gt <li&gt 1 - venetian mode (up/down and slate rotation) NOTE1: After parameter change, first exclude module (without setting parameters to default value) then wait at least 30s and then re include the module!</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Shutter mode |
| 1 | Venetian mode (up/down and slate rotation) |

The manufacturer defined default value is ```0``` (Shutter mode).

This parameter has the configuration ID ```config_71_1``` and is of type ```INTEGER```.


### Parameter 72: Slats tilting full turn time

Slat full turn time in tenths of a second.
<p&gtThis parameter defines the time necessary for slats to make full turn (180 degrees). Available configuration parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gtdefault value 150 = 1,5 seconds</li&gt <li&gt0 - tilting time disabled</li&gt <li&gt1 - 32767 = 0,01seconds - 327,67 seconds</li&gt </ul&gt<p&gtNOTE: If time set is too high, this will result that after full turn, Shutter will start move up/down, for time remaining.</p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```150```.

This parameter has the configuration ID ```config_72_2``` and is of type ```INTEGER```.


### Parameter 73: Slats position

Slats position
<p&gtThis parameter defines slats position after up/down movement through Z-wave or push-buttons. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 1</li&gt <li&gt 0 - Slats return to previously set position only in case of UI control.</li&gt <li&gt 1 - Slats return to previously set position in case of UI control, push button operation or when the limit switch is reached.</li&gt <li&gt2 - Slats return to previously set position in case of UI control, push button, when the limit switch is reached or after receiving a “STOP” control frame (Switch Multilevel Stop Level Change) from UI</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | previously set position only in case of UI control |
| 1 | position in case of UI control, push |
| 2 | position in case of UI control, push button |

The manufacturer defined default value is ```2``` (position in case of UI control, push button).

This parameter has the configuration ID ```config_73_1``` and is of type ```INTEGER```.


### Parameter 74: Motor moving up/down time

Shutter motor moving time of complete opening or complete closing
<p&gtThis parameter defines Shutter motor moving time of complete opening or complete closing. Available configuration parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gt default value 0</li&gt <li&gt 0 - moving time disabled (working with limit switches)</li&gt <li&gt 1 - 32767 = 0,1seconds - 3276,7seconds. After that time motor is stopped (relay goes to off state)</li&gt </ul&gt<p&gtNOTE: Important is that the reference position to manually set moving time is always Shutter lower position!</p&gt <p&gtSet parameter 74 to 0 and move the Shutter (using up/down push buttons or main controller UI) to the lowest desired position. On this Shutter position, set parameter 74 to time for complete opening or complete closing. At this point Shutter can be moved up (open) for set time, but can't be moved down because this position is already set as lower Shutter position.</p&gt <p&gtTo change Shutter lower position below already set (manual recalibration), parameter 74 must be set to 0 and repeat the procedure described above.</p&gt <p&gtIn case Shutter has limit switches, but anyhow you would like to limit opening/closing position by time, you can still do it. In case you put time that is longer that opening/closing real time limited by limit switches, Shutter will stop at limit switch, but the module relay will switch off after define time, not by Shutter limit switch. Take in consideration that in this condition, the positioning with slider through UI will not show correct Shutter position. NOTE that is not recommended using this for slates operation since its positioning can be compromised during time.</p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_74_2``` and is of type ```INTEGER```.


### Parameter 76: Motor operation detection

Power threshold
<p&gtPower threshold to be interpreted when motor reach the limit switch. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 10 = 10W</li&gt <li&gt 0 - 127 = 1-127 W. The value 0 means reaching a limit switch will not be detected</li&gt </ul&gt<p&gtNOTE: Motors with power consumption less than 0,5W could not be auto calibrated. In that case set time manually (par. 74).</p&gt
Values in the range 0 to 127 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_76_1``` and is of type ```INTEGER```.


### Parameter 78: Forced Shutter calibration

0 to 1 a Shutter enters the calibration mode
<p&gtBy modifying the parameters setting from 0 to 1 a Shutter DC module enters the calibration mode. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 0</li&gt <li&gt 1 - Start calibration process (when calibration process is finished, completing full cycle - up, down and up, set the parameter 78 (Forced Shutter calibration) value back to 0.</li&gt </ul&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_78_1``` and is of type ```INTEGER```.


### Parameter 80: Reporting to controller

reports regarding to main controller
<p&gtThis parameter defines if reporting regarding power level, multilevel, etc,…is reported to controller or not. Available configuration parameters: default value 1</p&gt <ul&gt<li&gt0 reporting to controller is disabled</li&gt <li&gt1 reporting to controller</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | reporting to controller is disabled |
| 1 | reporting to controller is enabled |

The manufacturer defined default value is ```1``` (reporting to controller is enabled).

This parameter has the configuration ID ```config_80_1``` and is of type ```INTEGER```.


### Parameter 85: Power consumption max delay time

max time before motor power consumption is read
<p&gtThis parameter defines the max time before motor power consumption is read after one of the relays is switched ON. If there is no power consumption during this max time (motor not connected, damaged or requires higher time to start, motor in end position,...) the relay will switch OFF. Time is defined by entering it manually. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 8 = 800ms</li&gt <li&gt 3 - 50 = 0,3seconds - 5seconds (100ms resolution)</li&gt <li&gt 0 = time is set automatically</li&gt </ul&gt
Values in the range 0 to 50 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_85_1``` and is of type ```INTEGER```.


### Parameter 90: Relay delay time

This parameter defines the time delay between output relay switching
<p&gtThis parameter defines the minimum time delay between next motor movement (minimum time between switching motor off and on again). Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 5 = 500ms</li&gt <li&gt 1 - 30 = 0,1seconds - 3seconds (100ms resolution)</li&gt </ul&gt
Values in the range 1 to 30 may be set.

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_90_1``` and is of type ```INTEGER```.

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

The ZMNHCA supports 5 association groups.

### Group 1: basic on/off

triggered at change of the input I1 state and reflecting its state

Association group 1 supports 16 nodes.

### Group 2: basic on/of

triggered at change of the input I2 state and reflecting its state

Association group 2 supports 16 nodes.

### Group 3: multileve

triggered at changes of value of the Flush shutter position

Association group 3 supports 16 nodes.

### Group 4: multilevel

triggered at changes of value of slats tilting position

Association group 4 supports 16 nodes.

### Group 5: default reporting group


Association group 5 supports 16 nodes.

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
| COMMAND_CLASS_SENSOR_MULTILEVEL_V3| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |
#### Endpoint 1

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| |
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_SENSOR_MULTILEVEL_V3| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |

### Documentation Links

* [Manual](https://opensmarthouse.org/zwavedatabase/635/flush-shutter-ZMNHCA2.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/635).
