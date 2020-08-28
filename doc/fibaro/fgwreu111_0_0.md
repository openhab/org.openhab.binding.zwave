---
layout: documentation
title: FGWREU-111 - ZWave
---

{% include base.html %}

# FGWREU-111 Fibaro Walli Roller Shutter
This describes the Z-Wave device *FGWREU-111*, manufactured by *[Fibargroup](http://www.fibaro.com/)* with the thing type UID of ```fibaro_fgwreu111_00_000```.

The device is in the category of *Blinds*, defining Roller shutters, window blinds, etc..

![FGWREU-111 product image](https://opensmarthouse.org/zwavedatabase/1083/image/)


The FGWREU-111 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtSmart wall switch designed to control motors of roller blinds, awnings, venetian blinds and other single phase, AC powered devices via Z-Wave network.</p&gt <p&gtIt measures active power and energy consumed by the controlled load.</p&gt <p&gtYou can install it with provided cover plate and switch button or other compatible.</p&gt

### Inclusion Information

<p&gtZ-Wave device learning mode, allowing to add the device to existing Z-Wave network:</p&gt <ol&gt<li&gtPower the device.</li&gt <li&gtSet the main controller into the adding mode (OH2: search for new things, select Z-Wave binding).</li&gt <li&gtQuickly, three times click one of the buttons.</li&gt <li&gtIf you are adding in Security S2 Authenticated, input the underlined part of the DSK (label on the bottom of the box).</li&gt <li&gtLED will start blinking yellow, wait for the adding process to end.</li&gt <li&gtAdding result will be confirmed by the Z-Wave controller’s message and the LED frame: <ul&gt<li&gt<strong&gtGreen</strong&gt – successful (non-secure, S0, S2 non-authenticated),</li&gt <li&gt<strong&gtMagenta</strong&gt – successful (Security S2 Authenticated),</li&gt <li&gt<strong&gtRed</strong&gt – not successful.</li&gt </ul&gt</li&gt </ol&gt

### Exclusion Information

<p&gtZ-Wave device learning mode, allowing to remove the device from existing Z-Wave network. Removing also results in resetting the device to factory defaults:</p&gt <ol&gt<li&gtPower the device.</li&gt <li&gtSet the main controller in remove mode (see the controller's manual).</li&gt <li&gtQuickly, three times click one of the buttons.</li&gt <li&gtLED will start blinking yellow, wait for the removing process to end.</li&gt <li&gtSuccessful removing will be confirmed by the Z-Wave controller’s message and red LED colour.</li&gt </ol&gt

## Channels

The following table summarises the channels available for the FGWREU-111 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Blinds Control  [Deprecated]| blinds_control | blinds_control | Blinds | Rollershutter | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Scene Number | scene_number | scene_number |  | Number | 
| Alarm (power) | alarm_power | alarm_power | Energy | Switch | 
| Alarm (system) | alarm_system | alarm_system |  | Switch | 
| Blinds Control | blinds_control1 | blinds_control | Blinds | Rollershutter | 
| Electric meter (watts) 1 | meter_watts1 | meter_watts | Energy | Number | 
| Electric meter (kWh) 1 | meter_kwh1 | meter_kwh | Energy | Number | 
| Alarm (power) 1 | alarm_power1 | alarm_power | Energy | Switch | 
| Alarm (system) 1 | alarm_system1 | alarm_system |  | Switch | 
| Slats Control | blinds_control2 | blinds_control | Blinds | Rollershutter | 

### Blinds Control [Deprecated]
Provides start / stop control of blinds.

The ```blinds_control``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.

**Note:** This channel is marked as deprecated so should not be used.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Scene Number
Triggers when a scene button is pressed.

The ```scene_number``` channel is of type ```scene_number``` and supports the ```Number``` item.
This channel provides the scene, and the event as a decimal value in the form ```<scene>.<event>```. The scene number is set by the device, and the event is as follows -:

| Event ID | Event Description  |
|----------|--------------------|
| 0        | Single key press   |
| 1        | Key released       |
| 2        | Key held down      |
| 3        | Double keypress    |
| 4        | Tripple keypress   |
| 5        | 4 x keypress       |
| 6        | 5 x keypress       |

### Alarm (power)
Indicates if a power alarm is triggered.

The ```alarm_power``` channel is of type ```alarm_power``` and supports the ```Switch``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Alarm (system)
Indicates if a system alarm is triggered.

The ```alarm_system``` channel is of type ```alarm_system``` and supports the ```Switch``` item. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Blinds Control
Provides start / stop control of blinds.

The ```blinds_control1``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.

### Electric meter (watts) 1
Indicates the instantaneous power consumption.

The ```meter_watts1``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh) 1
Indicates the energy consumption (kWh).

The ```meter_kwh1``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Alarm (power) 1
Indicates if a power alarm is triggered.

The ```alarm_power1``` channel is of type ```alarm_power``` and supports the ```Switch``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Alarm (system) 1
Indicates if a system alarm is triggered.

The ```alarm_system1``` channel is of type ```alarm_system``` and supports the ```Switch``` item. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Slats Control
Provides start / stop control of blinds.

The ```blinds_control2``` channel is of type ```blinds_control``` and supports the ```Rollershutter``` item and is in the ```Blinds``` category.



## Device Configuration

The following table provides a summary of the 26 configuration parameters available in the FGWREU-111.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 11 | LED frame colour when moving | This parameter defines the LED colour when the motor is running. |
| 12 | LED frame – colour when not moving | This parameter defines the LED colour when the device motor is not running. |
| 13 | LED frame – brightness | This parameter allows to adjust the LED frame brightness. |
| 24 | Buttons orientation | This parameter allows reversing the operation of the buttons. |
| 25 | Outputs orientation | reversing the operation of Q1 and Q2 |
| 30 | Alarm configuration - 1st slot | Which alarm frames and how the device should react |
| 31 | Alarm configuration - 2nd slot | which alarm frames and how the device should react |
| 32 | Alarm configuration - 3rd slot | which alarm frames and how the device should react |
| 33 | Alarm configuration - 4th slot | which alarm frames and how the device should react |
| 34 | Alarm configuration - 5th slot | which alarm frames and how the device should react |
| 35 | Alarm configuration – duration | duration of alarm sequence |
| 40 | First button – scenes sent | which actions result in sending scene IDs |
| 41 | Second button – scenes sent | which actions result in sending scene IDs |
| 60 | Power reports – include self-consumption | power measurements include device itself |
| 61 | Power reports – on change | minimal change in measured power to report |
| 62 | Power reports – periodic | reporting interval for measured power |
| 65 | Energy reports – on change | minimal change in measured energy to report |
| 66 | Energy reports – periodic | This reporting interval for measured energy |
| 150 | Force calibration | Set 2 to force device into calibration mode |
| 151 | Operating mode | This parameter allows adjusting operation ac-cording to the connected device. |
| 152 | Venetian blind – time of full turn of the slats | time of full turn cycle of the slats |
| 153 | Set slats back to previous position | slats positioning in various situations |
| 154 | Delay motor stop after reaching end switch | time after which the motor will be stopped after contacts closed |
| 155 | Motor operation detection | Power threshold interpreted as reaching a limit switch. |
| 156 | Time of up movement | time needed for roller blinds to reach the top |
| 157 | Time of down movement | time needed for roller blinds to reach the bottom |

### Parameter 11: LED frame colour when moving

This parameter defines the LED colour when the motor is running.
<p&gt0 – LED disabled</p&gt <p&gt1 – White</p&gt <p&gt2 – Red</p&gt <p&gt3 – Green</p&gt <p&gt4 – Blue</p&gt <p&gt5 – Yellow</p&gt <p&gt6 – Cyan</p&gt <p&gt7 – Magenta</p&gt
Values in the range 0 to 7 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_11_1``` and is of type ```INTEGER```.


### Parameter 12: LED frame – colour when not moving

This parameter defines the LED colour when the device motor is not running.
<p&gt0 – LED disabled</p&gt <p&gt1 – White</p&gt <p&gt2 – Red</p&gt <p&gt3 – Green</p&gt <p&gt4 – Blue</p&gt <p&gt5 – Yellow</p&gt <p&gt6 – Cyan</p&gt <p&gt7 – Magenta</p&gt
Values in the range 0 to 7 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_12_1``` and is of type ```INTEGER```.


### Parameter 13: LED frame – brightness

This parameter allows to adjust the LED frame brightness.
<p&gt0 – LED disabled</p&gt <p&gt1-100 (1-100% brightness)</p&gt
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```100```.

This parameter has the configuration ID ```config_13_1``` and is of type ```INTEGER```.


### Parameter 24: Buttons orientation

This parameter allows reversing the operation of the buttons.
<p&gt0 – default (1st button UP, 2nd button DOWN)</p&gt <p&gt1 – reversed (1st button DOWN, 2nd button UP)</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_24_1``` and is of type ```INTEGER```.


### Parameter 25: Outputs orientation

reversing the operation of Q1 and Q2
<p&gtThis parameter allows reversing the operation of Q1 and Q2 without changing the wiring (e.g. in case of invalid motor connection).</p&gt <p&gt0 - default (Q1 – UP, Q2 – DOWN)</p&gt <p&gt1 - reversed (Q1 – DOWN, Q2 – UP)</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_25_1``` and is of type ```INTEGER```.


### Parameter 30: Alarm configuration - 1st slot

Which alarm frames and how the device should react
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most sig-nificant bytes are set according to the official Z-Wave protocol specification.</p&gt <p&gt1B [MSB] – Notification Type</p&gt <p&gt2B – Notification Status</p&gt <p&gt3B – Event/State Parameters</p&gt <p&gt4B [LSB] – action:</p&gt <ul&gt<li&gt0x00 – no action,</li&gt <li&gt0xX1 – open,</li&gt <li&gt0xX2 – close,</li&gt <li&gt0x0X – no action on LED frame,</li&gt <li&gt0x1X – LED frame blinks red,</li&gt <li&gt0x2X – LED frame blinks green,</li&gt <li&gt0x4X – LED frame blinks blue,</li&gt <li&gt0x8X – dis-able LED frame,</li&gt <li&gt0xFX – LED frame LAPD signal (red-white-blue)</li&gt </ul&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_30_4_0000000F``` and is of type ```INTEGER```.


### Parameter 31: Alarm configuration - 2nd slot

which alarm frames and how the device should react
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most sig-nificant bytes are set according to the official Z-Wave protocol specification.</p&gt <p&gtdefault: \[0x05, 0xFF, 0x00, 0x00\](Water Alarm, any notification, no action)</p&gt <p&gt1B [MSB] – Notification Type</p&gt <p&gt2B – Notification Status</p&gt <p&gt3B – Event/State Parameters</p&gt <p&gt4B [LSB] – action:</p&gt <ul&gt<li&gt0x00 – no action,</li&gt <li&gt0xX1 – open,</li&gt <li&gt0xX2 – close,</li&gt <li&gt0x0X – no action on LED frame,</li&gt <li&gt0x1X – LED frame blinks red,</li&gt <li&gt0x2X – LED frame blinks green,</li&gt <li&gt0x4X – LED frame blinks blue,</li&gt <li&gt0x8X – dis-able LED frame,</li&gt <li&gt0xFX – LED frame LAPD signal (red-white-blue)</li&gt </ul&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_31_4_0000000F``` and is of type ```INTEGER```.


### Parameter 32: Alarm configuration - 3rd slot

which alarm frames and how the device should react
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most sig-nificant bytes are set according to the official Z-Wave protocol specification.</p&gt <p&gtDefault: \[0x01, 0xFF, 0x00, 0x00\](Smoke Alarm, any notification, no action)</p&gt <p&gt1B [MSB] – Notification Type</p&gt <p&gt2B – Notification Status</p&gt <p&gt3B – Event/State Parameters</p&gt <p&gt4B [LSB] – action:</p&gt <ul&gt<li&gt0x00 – no action,</li&gt <li&gt0xX1 – open,</li&gt <li&gt0xX2 – close,</li&gt <li&gt0x0X – no action on LED frame,</li&gt <li&gt0x1X – LED frame blinks red,</li&gt <li&gt0x2X – LED frame blinks green,</li&gt <li&gt0x4X – LED frame blinks blue,</li&gt <li&gt0x8X – dis-able LED frame,</li&gt <li&gt0xFX – LED frame LAPD signal (red-white-blue)</li&gt </ul&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_32_4_0000000F``` and is of type ```INTEGER```.
This is a read only parameter.


### Parameter 33: Alarm configuration - 4th slot

which alarm frames and how the device should react
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most sig-nificant bytes are set according to the official Z-Wave protocol specification.</p&gt <p&gtdefault: \[0x02, 0xFF, 0x00, 0x00\](CO Alarm, any notification, no action)</p&gt <p&gt1B [MSB] – Notification Type</p&gt <p&gt2B – Notification Status</p&gt <p&gt3B – Event/State Parameters</p&gt <p&gt4B [LSB] – action:</p&gt <ul&gt<li&gt0x00 – no action,</li&gt <li&gt0xX1 – open,</li&gt <li&gt0xX2 – close,</li&gt <li&gt0x0X – no action on LED frame,</li&gt <li&gt0x1X – LED frame blinks red,</li&gt <li&gt0x2X – LED frame blinks green,</li&gt <li&gt0x4X – LED frame blinks blue,</li&gt <li&gt0x8X – dis-able LED frame,</li&gt <li&gt0xFX – LED frame LAPD signal (red-white-blue)</li&gt </ul&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_33_4_0000000F``` and is of type ```INTEGER```.


### Parameter 34: Alarm configuration - 5th slot

which alarm frames and how the device should react
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most sig-nificant bytes are set according to the official Z-Wave protocol specification.</p&gt <p&gtdefault: 0x04, 0xFF, 0x00, 0x00](Heat Alarm, any notification, no action)</p&gt <p&gt1B [MSB] – Notification Type</p&gt <p&gt2B – Notification Status</p&gt <p&gt3B – Event/State Parameters</p&gt <p&gt4B [LSB] – action:</p&gt <ul&gt<li&gt0x00 – no action,</li&gt <li&gt0xX1 – open,</li&gt <li&gt0xX2 – close,</li&gt <li&gt0x0X – no action on LED frame,</li&gt <li&gt0x1X – LED frame blinks red,</li&gt <li&gt0x2X – LED frame blinks green,</li&gt <li&gt0x4X – LED frame blinks blue,</li&gt <li&gt0x8X – dis-able LED frame,</li&gt <li&gt0xFX – LED frame LAPD signal (red-white-blue)</li&gt </ul&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_34_4_0000000F``` and is of type ```INTEGER```.


### Parameter 35: Alarm configuration – duration

duration of alarm sequence
<p&gtThis parameter defines duration of alarm se-quence. When time set in this parameter elaps-es, alarm is cancelled, LED frame and relay re-store normal operation, but do not recover state from before the alarm.</p&gt <p&gt0 – infinite</p&gt <p&gt1-32400 (1s-9h, 1s step) – duration</p&gt
Values in the range 0 to 32400 may be set.

The manufacturer defined default value is ```600```.

This parameter has the configuration ID ```config_35_2``` and is of type ```INTEGER```.


### Parameter 40: First button – scenes sent

which actions result in sending scene IDs
<p&gtThis parameter determines which actions result in sending scene IDs assigned to them. Values can be combined (e.g. 1+2=3 means that scenes for single and double click are sent).Enabling scenes for triple click disables entering the device in learn mode by triple clicking</p&gt <p&gt0 (no scenes)</p&gt <p&gt1 – Key pressed 1 time</p&gt <p&gt2 – Key pressed 2 times</p&gt <p&gt4 – Key pressed 3 times</p&gt <p&gt8 – Key hold down and key released</p&gt
Values in the range 0 to 15 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_40_1``` and is of type ```INTEGER```.


### Parameter 41: Second button – scenes sent

which actions result in sending scene IDs
<p&gtThis parameter determines which actions result in sending scene IDs assigned to them. Values can be combined (e.g. 1+2=3 means that scenes for single and double click are sent).Enabling scenes for triple click disables entering the device in learn mode by triple clicking.</p&gt <p&gt0 (no scenes)</p&gt <p&gt1 – Key pressed 1 time</p&gt <p&gt2 – Key pressed 2 times</p&gt <p&gt4 – Key pressed 3 times</p&gt <p&gt8 – Key hold down and key released</p&gt
Values in the range 0 to 15 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_41_1``` and is of type ```INTEGER```.


### Parameter 60: Power reports – include self-consumption

power measurements include device itself
<p&gtwhether the power measurements should include power consumed by the device itself</p&gt <p&gt0 – self-consumption not included</p&gt <p&gt1 – self-consumption included</p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_60_1``` and is of type ```INTEGER```.


### Parameter 61: Power reports – on change

minimal change in measured power to report
<p&gtThis parameter defines minimal change (from the last reported) in measured power that re-sults in sending new report. For loads under 50W the parameter is irrelevant, report are sent every 5W change.</p&gt <p&gt0 - reporting on change disabled</p&gt <p&gt1-500 (1-500%, 1% step) – minimal change</p&gt
Values in the range 0 to 500 may be set.

The manufacturer defined default value is ```15```.

This parameter has the configuration ID ```config_61_2``` and is of type ```INTEGER```.


### Parameter 62: Power reports – periodic

reporting interval for measured power
<p&gtThis parameter defines reporting interval for measured power. Periodic reports are indepen-dent from changes in value (parameter 61).</p&gt <p&gt0 – periodic reports disabled</p&gt <p&gt30-32400 (30s-9h, 1s step) – time interval</p&gt
Values in the range 0 to 32400 may be set.

The manufacturer defined default value is ```3600```.

This parameter has the configuration ID ```config_62_2``` and is of type ```INTEGER```.


### Parameter 65: Energy reports – on change

minimal change in measured energy to report
<p&gtThis parameter defines minimal change (from the last reported) in measured energy that re-sults in sending new report.</p&gt <p&gt0 - reporting on change disabled</p&gt <p&gt1-500 (0.01-5kWh, 0.01kWh step) – minimal change</p&gt
Values in the range 0 to 500 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_65_2``` and is of type ```INTEGER```.


### Parameter 66: Energy reports – periodic

This reporting interval for measured energy
<p&gtThis parameter defines reporting interval for measured energy. Periodic reports are indepen-dent from changes in value (parameter 65).</p&gt <p&gt0 – periodic reports disabled</p&gt <p&gt30-32400 (30s-9h, 1s step) – time interval</p&gt
Values in the range 0 to 32400 may be set.

The manufacturer defined default value is ```3600```.

This parameter has the configuration ID ```config_66_2``` and is of type ```INTEGER```.


### Parameter 150: Force calibration

Set 2 to force device into calibration mode
<p&gtBy setting this parameter to 2 the device enters the calibration mode. The parameter relevant only if the device is set to work in positioning mode (parameter 151 set to 1 or 2).</p&gt <p&gt0 - device is not calibrated</p&gt <p&gt1 - device is calibrated</p&gt <p&gt2 - force device calibration</p&gt
Values in the range 0 to 2 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_150_1``` and is of type ```INTEGER```.


### Parameter 151: Operating mode

This parameter allows adjusting operation ac-cording to the connected device.
<p&gt1 – roller blind (with positioning)</p&gt <p&gt2 – Venetian blind (with positioning)</p&gt <p&gt5 – roller blind with built-in driver</p&gt <p&gt6 – roller blind with built-in driver (impulse)</p&gt
Values in the range 1 to 6 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_151_1``` and is of type ```INTEGER```.


### Parameter 152: Venetian blind – time of full turn of the slats

time of full turn cycle of the slats
<p&gtFor Venetian blinds (parameter 151 set to 2) the parameter determines time of full turn cycle of the slats. The parameter is irrelevant for other modes.</p&gt <p&gt0-65535 (0 - 655.35s, every 0.01s) - time of turn</p&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```150```.

This parameter has the configuration ID ```config_152_4``` and is of type ```INTEGER```.


### Parameter 153: Set slats back to previous position

slats positioning in various situations
<p&gtFor Venetian blinds (parameter 151 set to 2) the parameter determines slats positioning in various situations. The parameter is irrelevant for other modes.</p&gt <p&gt0 – slats return to previously set position only in case of the main controller operation</p&gt <p&gt1 – slats return to previously set position in case of the main controller operation, momentary switch operation, or when the limit switch is reached</p&gt <p&gt2 – slats return to previously set position in case of the main controller operation, momen-tary switch operation, when the limit switch is reached or after receiving the Switch Multilevel Stop control frame</p&gt
Values in the range 0 to 2 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_153_1``` and is of type ```INTEGER```.


### Parameter 154: Delay motor stop after reaching end switch

time after which the motor will be stopped after contacts closed
<p&gtThe parameter determines the time after which the motor will be stopped after end switch contacts are closed.</p&gt <p&gt1-255 (0.1s - 25.5 seconds)</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_154_2``` and is of type ```INTEGER```.


### Parameter 155: Motor operation detection

Power threshold interpreted as reaching a limit switch.
<p&gt1-255 (1-255W) - report interval</p&gt <p&gt0 - no detection</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_155_2``` and is of type ```INTEGER```.


### Parameter 156: Time of up movement

time needed for roller blinds to reach the top
<p&gtThis parameter determines the time needed for roller blinds to reach the top. For modes with positioning value is set automatically during calibration, otherwise, it must be set manually.</p&gt <p&gt1-65535 (0.01 - 655.35 seconds)</p&gt
Values in the range 1 to 65535 may be set.

The manufacturer defined default value is ```6000```.

This parameter has the configuration ID ```config_156_4``` and is of type ```INTEGER```.


### Parameter 157: Time of down movement

time needed for roller blinds to reach the bottom
<p&gtThis parameter determines the time needed for roller blinds to reach the bottom.For modes with positioning value is set automatically during calibration, otherwise, it must be set manually.</p&gt <p&gt1-65535 (0.01 - 655.35 seconds)</p&gt
Values in the range 1 to 65535 may be set.

The manufacturer defined default value is ```6000```.

This parameter has the configuration ID ```config_157_4``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The FGWREU-111 supports 3 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.

Association group 1 supports 1 node.

### Group 2: Roller Shutter


Association group 2 supports 5 nodes.

### Group 3: Slats


Association group 3 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_APPLICATION_STATUS_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_CRC_16_ENCAP_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_CENTRAL_SCENE_V3| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V8| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_PROTECTION_V2| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 1

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_APPLICATION_STATUS_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ALARM_V8| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 2

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_APPLICATION_STATUS_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Operating Manual](https://opensmarthouse.org/zwavedatabase/1083/FGWREU-111-T-EN-v1-0.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1083).
