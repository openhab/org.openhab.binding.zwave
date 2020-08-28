---
layout: documentation
title: ZMNHCD - ZWave
---

{% include base.html %}

# ZMNHCD Flush Shutter
This describes the Z-Wave device *ZMNHCD*, manufactured by *[Goap](http://www.qubino.com/)* with the thing type UID of ```qubino_zmnhcd_04_001```.
This version of the device is limited to firmware versions above 4.1

The device is in the category of *Blinds*, defining Roller shutters, window blinds, etc..

![ZMNHCD product image](https://opensmarthouse.org/zwavedatabase/213/image/)


The ZMNHCD supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtThe Qubino Flush Shutter DC device is ideal for remotely controlling 12–24V DC motors of blinds, rollers, shades, venetian blinds and similar sunscreens.</p&gt

### Inclusion Information

<p&gt<strong&gtAUTOMATICALLY ADDING THE DEVICE TO A Z-WAVE NETWORK (AUTO INCLUSION)</strong&gt</p&gt <ol&gt<li&gtEnable add/remove mode on your Z-Wave gateway (hub)</li&gt <li&gtConnect the device to the power supply (with the temperature sensor already connected – sold separately\*)</li&gt <li&gtAuto-inclusion will be initiated within 5 seconds of connection to the power supply and the device will automatically enrol in your network</li&gt </ol&gt<p&gtMANUALLY<strong&gt ADDING THE DEVICE TO A Z-WAVE NETWORK (MANUAL INCLUSION)</strong&gt</p&gt <ol&gt<li&gtEnable add/remove mode on your Z-Wave gateway (hub)</li&gt <li&gtConnect the device to the power supply (with the temperature sensor already connected\*)</li&gt <li&gtToggle the switch connected to the I1 terminal 3 times within 3 seconds <strong&gtOR</strong&gt If the device is powered by 24 V SELV supply, press and hold the S(Service) button between 2 and 6 seconds4.</li&gt <li&gtA new multi-channel device will appear on your dashboard</li&gt </ol&gt<p&gt*If connecting the temperature sensor, switch off the power supply and make sure the device is excluded from your network BEFORE connecting the sensor.</p&gt <p&gtⓘ Make sure the device is excluded from your network before connecting the temperature sensor. Switch off the power supply, connect the temperature sensor, and re-include the device to your network.</p&gt

### Exclusion Information

<p&gtREMOVAL FROM A ZWAVE NETWORK (Z-WAVE EXCLUSION)</p&gt <ol&gt<li&gtConnect the device to the power supply</li&gt <li&gtMake sure the device is within direct range of your Z-Wave gateway (hub) or use a hand-held Z-Wave remote to perform exclusion</li&gt <li&gtEnable add/remove mode on your Z-Wave gateway (hub)</li&gt <li&gtToggle the switch connected to the I1 terminal 3 times within 3 seconds OR If the device is powered by 24 V SELV supply, press and hold the S (Service) button between 2 and 6 seconds</li&gt <li&gtThe device will be removed from your network but any custom configuration parameters will not be erased</li&gt </ol&gt<p&gtFACTORY RESET</p&gt <ol&gt<li&gtConnect the device to the power supply</li&gt <li&gtWithin the first minute (60 seconds) the device is connected to the power supply, toggle the switch connected to the I1 terminal 5 times within 5 seconds (5 times change switch state) OR If the device is powered by 24 V SELV supply, press and hold the S (Service) button for at least 6 seconds</li&gt </ol&gt<p&gt<br /&gtⓘ By resetting the device, all custom parameters previously set on the device will return to their default values, and the owner ID will be deleted. Use this reset procedure only when the main gateway (hub) is missing or otherwise inoperable.</p&gt

### General Usage Information

<p&gtTo enable endpoint 2 (lamella tilt for venetian blinds), follow the procedure:</p&gt <ol&gt<li&gtset Param 71 to 0 -> save ( make sure it has been set really )</li&gt <li&gtset Param 71 to 1 -> save ( make sure it has been set really )</li&gt <li&gtexclude device from network and delete Node xml, stop openHAB</li&gt <li&gtswitch off the power supply of the device ( according to the support team this is an alternative to wait 30s )</li&gt <li&gtswitch on the power supply again and start openHAB</li&gt <li&gtinclude device to the network</li&gt <li&gtadd the Thing to openhab via HABmin</li&gt <li&gtBe patient until the binding until processed all your nodes again</li&gt </ol&gt

## Channels

The following table summarises the channels available for the ZMNHCD -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 
| Blinds Control | blinds_control | blinds_control | Blinds | Rollershutter | 
| Sensor (temperature) | sensor_temperature | sensor_temperature | Temperature | Number:Temperature | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Switch 1 | switch_binary1 | switch_binary | Switch | Switch | 
| Blinds control 1 | blinds_control1 | blinds_control | Blinds | Rollershutter | 
| Electric meter (kWh) 1 | meter_kwh1 | meter_kwh | Energy | Number | 
| Electric meter (watts) 1 | meter_watts1 | meter_watts | Energy | Number | 
| Switch 2 | switch_binary2 | switch_binary | Switch | Switch | 
| Blinds control 2 | blinds_control2 | blinds_control | Blinds | Rollershutter | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Blinds Control
Provides start / stop control of blinds.

The ```blinds_control``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.

### Sensor (temperature)
Indicates the current temperature.

The ```sensor_temperature``` channel is of type ```sensor_temperature``` and supports the ```Number:Temperature``` item and is in the ```Temperature``` category.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Switch 1
Switch the power on and off.

The ```switch_binary1``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Blinds control 1
Provides start / stop control of blinds.

The ```blinds_control1``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.

### Electric meter (kWh) 1
Indicates the energy consumption (kWh).

The ```meter_kwh1``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts) 1
Indicates the instantaneous power consumption.

The ```meter_watts1``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Switch 2
Switch the power on and off.

The ```switch_binary2``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Blinds control 2
Provides start / stop control of blinds.

The ```blinds_control2``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.



## Device Configuration

The following table provides a summary of the 15 configuration parameters available in the ZMNHCD.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 10 | ALL ON/ALL OFF | Responds to commands ALL ON / ALL OFF from Main Controller |
| 40 | Power reporting in watts on power change | Power consumption change threshold for sending updates |
| 42 | Power reporting in Watts by time interval | Power reporting in Watts by time interval for Q1 or Q2 |
| 71 | Operating modes | Operation Mode (Shutter or Venetian) |
| 72 | Slats tilting full turn time | Slat full turn time in tenths of a second. |
| 73 | Slats position | Slats position after up/down movement. |
| 74 | Motor moving up/down time | Shutter motor moving time of complete opening or complete closing |
| 76 | Motor operation detection | Power threshold to be interpreted when motor reach the limit switch |
| 78 | Forced Shutter calibration | Enters calibration mode if set to 1 |
| 80 | Reporting to Controller | Defines if reporting regarding power level, etc is reported to controller. |
| 85 | Power consumption max delay time | Time delay for detecting motor errors |
| 86 | Power consumption at limit switch delay time | Sets the time delay for detecting limit switches |
| 90 | Relay delay time | Defines the minimum time delay between next motor movement |
| 110 | Temperature sensor offset settings | Adds or removes an offset from the measured temperature. |
| 120 | Digital temperature sensor reporting | Threshold for sending temperature change reports |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 10: ALL ON/ALL OFF

Responds to commands ALL ON / ALL OFF from Main Controller
<p&gtModule responds to commands ALL ON / ALL OFF that may be sent by the main controller or by other controller belonging to the system.</p&gt <p&gtAvailable config. parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gt default value 255</li&gt <li&gt 255 - ALL ON active, ALL OFF active.</li&gt <li&gt 0 - ALL ON is not active, ALL OFF is not active</li&gt <li&gt 1 - ALL ON is not active ALL OFF active</li&gt <li&gt 2 - ALL ON active ALL OFF is not active</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | ALL ON is not active ALL OFF is not active |
| 1 | ALL ON is not active ALL OFF active |
| 2 | ALL ON is not active ALL OFF is not active |
| 255 | ALL ON active, ALL OFF active |

The manufacturer defined default value is ```255``` (ALL ON active, ALL OFF active).

This parameter has the configuration ID ```config_10_2``` and is of type ```INTEGER```.


### Parameter 40: Power reporting in watts on power change

Power consumption change threshold for sending updates
<p&gtPower report is send (push) only when actual power (in Watts) in real time changes for more than set percentage comparing to previous actual power in Watts, step is 1%.</p&gt <p&gtSet value means percentage, set value from 0 – 100 = 0% - 100%. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 1</li&gt <li&gt 0 - reporting disabled</li&gt <li&gt 1 - 100 = 1% - 100% Reporting enabled.</li&gt </ul&gt<p&gtNOTE: if power changed is less than 1W, the report is not send (pushed), independent of percentage set.</p&gt
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_40_1``` and is of type ```INTEGER```.


### Parameter 42: Power reporting in Watts by time interval

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
<p&gtThis parameter defines the time necessary for slats to make full turn (180 degrees). Available configuration parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gt default value 150 = 1,5 seconds</li&gt <li&gt 0 - tilting time disabled</li&gt <li&gt 1 - 32767 = 0,01seconds - 327,67 seconds</li&gt </ul&gt<p&gtNOTE: If time set is too high, this will result that after full turn, Shutter will start move up/down, for time remaining.</p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```150```.

This parameter has the configuration ID ```config_72_2``` and is of type ```INTEGER```.


### Parameter 73: Slats position

Slats position after up/down movement.
<p&gtThis parameter defines slats position after up/down movement through Z-wave or push-buttons. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 1</li&gt <li&gt 0 - Slats return to previously set position only in case of Z-wave control (not valid for limit switch positions).</li&gt <li&gt 1 - Slats return to previously set position in case of Z-wave control, push-button operation or when the lower limit switch is reached.</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Previous position for Z-wave control only |
| 1 | Return to previous position in all cases |

The manufacturer defined default value is ```1``` (Return to previous position in all cases).

This parameter has the configuration ID ```config_73_1``` and is of type ```INTEGER```.


### Parameter 74: Motor moving up/down time

Shutter motor moving time of complete opening or complete closing
<p&gtThis parameter defines Shutter motor moving time of complete opening or complete closing. Available configuration parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gt default value 0</li&gt <li&gt 0 - moving time disabled (working with limit switches)</li&gt <li&gt 1 - 32767 = 0,1seconds - 3276,7seconds. After that time motor is stopped (relay goes to off state)</li&gt </ul&gt<p&gtNOTE: Important is that the reference position to manually set moving time is always Shutter lower position!</p&gt <p&gtSet parameter 74 to 0 and move the Shutter (using up/down push buttons or main controller UI) to the lowest desired position. On this Shutter position, set parameter 74 to time for complete opening or complete closing. At this point Shutter can be moved up (open) for set time, but can't be moved down because this position is already set as lower Shutter position.</p&gt <p&gtTo change Shutter lower position below already set (manual recalibration), parameter 74 must be set to 0 and repeat the procedure described above.</p&gt <p&gtIn case Shutter has limit switches, but anyhow you would like to limit opening/closing position by time, you can still do it. In case you put time that is longer that opening/closing real time limited by limit switches, Shutter will stop at limit switch, but the module relay will switch off after define time, not by Shutter limit switch. Take in consideration that in this condition, the positioning with slider through UI will not show correct Shutter position. NOTE that is not recommended using this for slates operation since its positioning can be compromised during time.</p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_74_2``` and is of type ```INTEGER```.


### Parameter 76: Motor operation detection

Power threshold to be interpreted when motor reach the limit switch
<p&gtPower threshold to be interpreted when motor reach the limit switch. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 6 = 0,6W</li&gt <li&gt 5 - 100 (0,5W - 10W), step is 0,1W.</li&gt </ul&gt<p&gtNOTE: Motors with power consumption less than 0,5W could not be auto calibrated. In that case set time manually (par. 74).</p&gt
Values in the range 0 to 127 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_76_1``` and is of type ```INTEGER```.


### Parameter 78: Forced Shutter calibration

Enters calibration mode if set to 1
<p&gtBy modifying the parameters setting from 0 to 1 a Shutter DC module enters the calibration mode. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 0</li&gt <li&gt 1 - Start calibration process (when calibration process is finished, completing full cycle - up, down and up, set the parameter 78 (Forced Shutter calibration) value back to 0.</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Default |
| 1 | Start Calibration Process |

The manufacturer defined default value is ```0``` (Default).

This parameter has the configuration ID ```config_78_1``` and is of type ```INTEGER```.


### Parameter 80: Reporting to Controller

Defines if reporting regarding power level, etc is reported to controller.
<p&gtThis parameter defines if reporting regarding power level, multilevel, etc,…is reported to controller or not. Available configuration parameters: default value 1</p&gt <ul&gt<li&gt0 reporting to controller is disabled</li&gt <li&gt1 reporting to controller</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Reporting to Controller Disabled |
| 1 | Reporting to Controller Enabled |

The manufacturer defined default value is ```1``` (Reporting to Controller Enabled).

This parameter has the configuration ID ```config_80_1``` and is of type ```INTEGER```.


### Parameter 85: Power consumption max delay time

Time delay for detecting motor errors
<p&gtThis parameter defines the max time before motor power consumption is read after one of the relays is switched ON. If there is no power consumption during this max time (motor not connected, damaged or requires higher time to start, motor in end position,...) the relay will switch OFF. Time is defined by entering it manually. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 8 = 800ms</li&gt <li&gt 3 - 50 = 0,3seconds - 5seconds (100ms resolution)</li&gt </ul&gt
Values in the range 0 to 50 may be set.

The manufacturer defined default value is ```8```.

This parameter has the configuration ID ```config_85_1``` and is of type ```INTEGER```.


### Parameter 86: Power consumption at limit switch delay time

Sets the time delay for detecting limit switches
<p&gtThis parameter defines the max time at limit switch, when power consumption is below power threshold. If the power consumption during this time is below power threshold (par. 76), the active output will switch off, means that limit switch is reached. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 8 = 800ms</li&gt <li&gt 3 - 50 = 0,3seconds - 5seconds (100ms resolution)</li&gt </ul&gt
Values in the range 3 to 50 may be set.

The manufacturer defined default value is ```8```.

This parameter has the configuration ID ```config_86_1``` and is of type ```INTEGER```.


### Parameter 90: Relay delay time

Defines the minimum time delay between next motor movement
<p&gtThis parameter defines the minimum time delay between next motor movement (minimum time between switching motor off and on again). Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt default value 5 = 500ms</li&gt <li&gt 1 - 30 = 0,1seconds - 3seconds (100ms resolution)</li&gt </ul&gt
Values in the range 1 to 30 may be set.

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_90_1``` and is of type ```INTEGER```.


### Parameter 110: Temperature sensor offset settings

Adds or removes an offset from the measured temperature.
<p&gtSet value is added or subtracted to actual measured value by sensor. Available configuration parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gt default value 32536</li&gt <li&gt 32536 - offset is 0.0C</li&gt <li&gt From 1 to 100 - value from 0.1 °C to 10.0 °C is added to actual measured temperature.</li&gt <li&gt From 1001 to 1100 - value from -0.1 °C to -10.0 °C is subtracted to actual measured temperature.</li&gt </ul&gt
Values in the range 1 to 32536 may be set.

The manufacturer defined default value is ```32536```.

This parameter has the configuration ID ```config_110_2``` and is of type ```INTEGER```.


### Parameter 120: Digital temperature sensor reporting

Threshold for sending temperature change reports
<p&gtIf digital temperature sensor is connected, module reports measured temperature on temperature change defined by this parameter. Available configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gt Default value 5 = 0,5°C</li&gt <li&gt 0 - reporting disabled</li&gt <li&gt 1-127 = 0,1°C - 12,7°C, step is 0,1°C</li&gt </ul&gt
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

The ZMNHCD supports 5 association groups.

### Group 1: Controller Updates


Association group 1 supports 1 node.

### Group 2: On/Off Triggered by I2


Association group 2 supports 16 nodes.

### Group 3: On/Off Triggered by I3


Association group 3 supports 16 nodes.

### Group 4: Multi-level Triggerd by Shutter Position


Association group 4 supports 16 nodes.

### Group 5: Multilevel Trigger by Slat Position


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
| COMMAND_CLASS_SENSOR_MULTILEVEL_V1| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V1| |
#### Endpoint 1

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| Linked to BASIC|
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V1| |
#### Endpoint 2

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| Linked to BASIC|
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V1| |

### Documentation Links

* [User Manual](https://opensmarthouse.org/zwavedatabase/213/Qubino-Flush-Shutter-PLUS-user-manual-V1-4.pdf)
* [Qubino-Flush-Shutter-PLUS-user-manual-V1.0](https://opensmarthouse.org/zwavedatabase/213/ZMNHCD-S1.pdf)
* [Qubino Flush Shutter extended manual eng 2.2](https://opensmarthouse.org/zwavedatabase/213/Qubino-Flush-Shutter-PLUS-extended-manual-eng-2-2-1.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/213).
