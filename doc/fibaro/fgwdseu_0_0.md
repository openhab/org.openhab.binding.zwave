---
layout: documentation
title: FGWDSEU - ZWave
---

{% include base.html %}

# FGWDSEU WALLI SWITCH FIBARO DOUBLE SWITCH
This describes the Z-Wave device *FGWDSEU*, manufactured by *[Fibargroup](http://www.fibaro.com/)* with the thing type UID of ```fibaro_fgwdseu_00_000```.

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![FGWDSEU product image](https://opensmarthouse.org/zwavedatabase/1181/image/)


The FGWDSEU supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtFIBARO Walli Double Switch is a smart wall switch designed to control one or two light sources via Z-Wave network. It measures active power and energy consumed by the controlled load. You can install it with provided cover plate or other compatible. <br /&gtMain features of FIBARO Walli Double Switch: </p&gt <p&gtCan be used with:</p&gt <ul&gt<li&gtConventional incandescent and halogen light sources,</li&gt <li&gtLED lamps,</li&gt <li&gtFluorescent lamps,</li&gt <li&gtElectronic transformers (for ELV halogen lamps and LED bulbs),</li&gt <li&gtFerromagnetic transformers (for MLV halogen lamps).</li&gt <li&gtCan be used with provided cover plate or one of the following: <ul&gt<li&gtGIRA – System 55 (Standard 55, E2, Event, Event Clear), </li&gt <li&gtLegrand – Céliane, </li&gt <li&gtSchneider – Odace.</li&gt <li&gtActive power and energy consumption metering.</li&gt </ul&gt</li&gt <li&gtSupports Z-Wave network Security Modes: S0 with AES-128 encryption and S2 Authenticated with PRNG-based encryption. </li&gt <li&gtWorks as Z-Wave signal repeater (all non-battery operated devices within the network will act as repeaters to increase reliability of the network). </li&gt <li&gtMay be used with all devices certified with the Z-Wave Plus certificate and should be compatible with such devices produced by other manufacturers. </li&gt </ul&gt

### Inclusion Information

<p&gtAdding (Inclusion) – Z-Wave device learning mode, allowing to add the device to existing Z-Wave network. i If you want to change single/double switch operating mode (see “Single/Double Switch operating mode” on page 14), you must do it before adding the device to the Z-Wave network. 5.1: Adding manually To add the device to the Z-Wave network manually: 1. Power the device. 2. Set the main controller in (Security/non-Security Mode) add mode (see the controller’s manual). 3. Quickly, three times click one of the buttons. 4. If you are adding in Security S2 Authenticated, input the underlined part of the DSK (label on the bottom of the box). 5. LED will start blinking yellow, wait for the adding process to end. 6. Adding result will be confirmed by the Z-Wave controller’s message and the LED frame: • Green – successful (non-secure, S0, S2 non-authenticated), • Magenta – successful (Security S2 Authenticated), • Red – not successful.</p&gt

### Exclusion Information

<p&gtRemoving (Exclusion) – Z-Wave device learning mode, allowing to remove the device from existing Z-Wave network. Removing also results in resetting the device to factory defaults.</p&gt <p&gtTo remove the device from the Z-Wave network:</p&gt <ol&gt<li&gtPower the device.</li&gt <li&gtSet the main controller into remove mode (see the controller’s manual).</li&gt <li&gtQuickly, three times click one of the buttons.</li&gt <li&gtLED will start blinking yellow, wait for the removing process to end.</li&gt <li&gtSuccessful removing will be confirmed by the Z-Wave controller’s message and red LED colour.</li&gt </ol&gt

## Channels

The following table summarises the channels available for the FGWDSEU -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Scene Number | scene_number | scene_number |  | Number | 
| Alarm (power) | alarm_power | alarm_power | Energy | Switch | 
| Alarm (system) | alarm_system | alarm_system |  | Switch | 
| Switch Binary | switch_binary1 | switch_binary | Switch | Switch | 
| Electric meter (watts) | meter_watts1 | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh1 | meter_kwh | Energy | Number | 
| Switch Binary | switch_binary2 | switch_binary | Switch | Switch | 
| Electric meter (watts) | meter_watts2 | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh2 | meter_kwh | Energy | Number | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

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

### Switch Binary
Switch the power on and off.

The ```switch_binary1``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts1``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh1``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Switch Binary
Switch the power on and off.

The ```switch_binary2``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts2``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh2``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.



## Device Configuration

The following table provides a summary of the 39 configuration parameters available in the FGWDSEU.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Remember device state | Remember device state |
| 2 | First channel – overload safety switch | First channel – overload safety switch |
| 3 | Second channel – overload safety switch | Second channel – overload safety switch |
| 10 | LED Frame - power limit | LED frame – power limit |
| 11 | LED frame – colour when ON | LED frame – colour when ON |
| 12 | LED frame – colour when OFF | LED frame – colour when OFF |
| 13 | LED frame – brightness | LED frame – brightness |
| 20 | Buttons operation | Buttons operation |
| 24 | Buttons orientation | Buttons orientation |
| 25 | Outputs orientation | Outputs orientation (Only in Double Switch Mode) |
| 30 | Alarm configuration - 1st slot | Alarm configuration - 1st slot |
| 31 | Alarm configuration - 2nd slot | Alarm configuration - 2nd slot |
| 32 | Alarm configuration - 3rd slot | Alarm configuration - 3rd slot |
| 33 | Alarm configuration - 4th slot | Alarm configuration - 4th slot |
| 34 | Alarm configuration - 5th slot | Alarm configuration - 5th slot |
| 35 | Alarm configuration – duration | Alarm configuration – duration |
| 40 | First button – scenes sent | First button – scenes sent |
| 41 | Second button – scenes sent | Second button – scenes sent |
| 60 | Power reports – include self-consumption | Power reports – include self-consumption |
| 61 | Power reports for first channel – on change | Power reports for first channel – on change |
| 62 | Power reports for first channel – periodic | Power reports for first channel – periodic |
| 63 | Power reports for second channel – on change | Power reports for second channel – on change |
| 64 | Power reports for second channel – periodic | Power reports for second channel – periodic |
| 65 | Energy reports for first channel – on change | Energy reports for first channel – on change |
| 66 | Energy reports for first channel – periodic | Energy reports for first channel – periodic |
| 67 | Energy reports for second channel – on change | Energy reports for second channel – on change |
| 68 | Energy reports for second channel – periodic | Energy reports for second channel – periodic |
| 150 | First channel – operating mode | First channel – operating mode |
| 151 | Second channel – operating mode | Second channel – operating mode |
| 152 | 1st ch - reaction to switch | First channel - reaction to switch for delayed OFF / pulse modes |
| 153 | 2nd ch - reaction to switch | Second channel - reaction to switch for delayed OFF / pulse modes |
| 154 | 1st ch - time parameter for delayed | First channel - time parameter for delayed OFF / pulse modes |
| 155 | 2nd ch - time parameter for delayed | Second channel - time parameter for delayed OFF / pulse modes |
| 156 | 1st ch – ON value sent to 2nd/3rd | First channel – Switch ON value sent to 2nd and 3rd association groups |
| 157 | 1st ch – OFF value sent to 2nd/3rd | First channel – Switch OFF value sent to 2nd and 3rd association groups |
| 158 | 1st ch – Double Click value sent to 2nd/3rd | First channel – Double Click value sent to 2nd and 3rd association groups |
| 159 | 2nd ch – ON value sent to 4th/5th | Second channel – Switch ON value sent to 4th and 5th association groups |
| 160 | 2nd ch – OFF value sent to 4th/5th | Second channel – Switch OFF value sent to 4th and 5th association groups |
| 161 | 2nd ch – Double Click value sent to 4th/5th | Second channel – Double Click value sent to 4th and 5th association groups |

### Parameter 1: Remember device state

Remember device state

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | remains switched off after restoring power |
| 1 | restores remembered state after restoring power |

The manufacturer defined default value is ```1``` (restores remembered state after restoring power).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: First channel – overload safety switch

First channel – overload safety switch
<p&gtThis function allows to turn off the controlled device in case of exceeding the defined power. Controlled device can be turned back on via button or sending a control frame. </p&gt <p&gt<strong&gtSingle Switch Mode</strong&gt</p&gt <p&gt0 – function disabled <br /&gt10-45000 (1.0-4500.0W, step 0.1W) – power threshold</p&gt <p&gt<strong&gtDouble Switch Mode</strong&gt</p&gt <p&gt0 – function disabled <br /&gt10-36200 (1.0-3620.0W, step 0.1W) – power threshold</p&gt
Values in the range 0 to 45000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_2_4``` and is of type ```INTEGER```.


### Parameter 3: Second channel – overload safety switch

Second channel – overload safety switch
<p&gt<strong&gtOnly in Double Switch Mode</strong&gt <br /&gtThis function allows to turn off the controlled device in case of exceeding the defined power. Controlled device can be turned back on via button or sending a control frame.</p&gt <p&gtAvailable values 0 – function disabled <br /&gt10-36200 (1.0-3620.0W, step 0.1W) – power threshold</p&gt
Values in the range 0 to 36200 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_3_4``` and is of type ```INTEGER```.


### Parameter 10: LED Frame - power limit

LED frame – power limit
<p&gtThis parameter determines maximum active power. Exceeding it results in the LED frame flashing violet. Function is active only when parameter 11 is set to 8 or 9.</p&gt <p&gtSingle Switch Mode <br /&gtDefault value 36800 (3680W) <br /&gtAvailable values 500-36800 (50.0-3680.0W, step 0.1W) – power threshold</p&gt <p&gtDouble Switch Mode <br /&gtDefault value 30000 (3000W) <br /&gtAvailable values 500-30000 (50.0-3000.0W, step 0.1W) – power threshold</p&gt
Values in the range 500 to 30000 may be set.

The manufacturer defined default value is ```30000```.

This parameter has the configuration ID ```config_10_4``` and is of type ```INTEGER```.


### Parameter 11: LED frame – colour when ON

LED frame – colour when ON

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | LED disabled |
| 1 | White |
| 2 | Red |
| 3 | Green |
| 4 | Blue |
| 5 | Yellow |
| 6 | Cyan |
| 7 | Magenta |
| 8 | smoothly dep. on measured power |
| 9 | in steps dep on measured power |

The manufacturer defined default value is ```0``` (LED disabled).

This parameter has the configuration ID ```config_11_1``` and is of type ```INTEGER```.


### Parameter 12: LED frame – colour when OFF

LED frame – colour when OFF
<p&gtThis parameter defines the LED colour when the device is OFF.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | LED disabled |
| 1 | White |
| 2 | Red |
| 3 | Green |
| 4 | Blue |
| 5 | Yellow |
| 6 | Cyan |
| 7 | Magenta |

The manufacturer defined default value is ```0``` (LED disabled).

This parameter has the configuration ID ```config_12_1``` and is of type ```INTEGER```.


### Parameter 13: LED frame – brightness

LED frame – brightness
<p&gtThis parameter allows to adjust the LED frame brightness.</p&gt <p&gtAvailable values <br /&gt0 – LED disabled <br /&gt1-100 (1-100% brightness) <br /&gt101 – brightness directly proportional to measured power <br /&gt102 – brightness inversely proportional to measured power </p&gt
Values in the range 0 to 102 may be set.

The manufacturer defined default value is ```100```.

This parameter has the configuration ID ```config_13_1``` and is of type ```INTEGER```.


### Parameter 20: Buttons operation

Buttons operation
<p&gtThis parameter defines how device buttons should control the channels.</p&gt <p&gt<strong&gtSingle Switch Mode<br /&gt</strong&gtDefault value 1 <strong&gt<br /&gt</strong&gtAvailable values <br /&gt1 – 1st and 2nd button toggle the load <br /&gt2 – 1st button turns the load ON, 2nd button turns the load OFF<br /&gt3 – device works in 2-way/3-way switch configuration</p&gt <p&gt<strong&gtDouble Switch Mode<br /&gt</strong&gtDefault Value 3<br /&gtAvailable values <br /&gt1 – 1st and 2nd button toggle both channels <br /&gt2 – 1st button turns both channels ON, 2nd button turns both channels OFF <br /&gt3 – 1st button toggles 1st channel, 2nd button toggles 2nd channel <br /&gt4 – 1st button single click turns 1st channel ON, 1st button double click turns 2nd channel ON, 2nd button single click turns both channels OFF</p&gt
Values in the range 1 to 4 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_20_1``` and is of type ```INTEGER```.


### Parameter 24: Buttons orientation

Buttons orientation
<p&gtThis parameter allows reversing the operation of the buttons. <br /&gtAvailable values <br /&gt0 – default (1st button controls 1st channel, 2nd button controls 2nd channel) <br /&gt1 – reversed (1st button controls 2nd channel, 2nd button controls 1st channel)</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | default |
| 1 | reversed |

The manufacturer defined default value is ```0``` (default).

This parameter has the configuration ID ```config_24_1``` and is of type ```INTEGER```.


### Parameter 25: Outputs orientation

Outputs orientation (Only in Double Switch Mode)
<p&gtThis parameter allows reversing the operation of Q1 and Q2 without changing the wiring (e.g. in case of invalid connection). Changing orientation turns both outputs off. <br /&gtAvailable values <br /&gt0 – default (Q1 - 1st channel, Q2 - 2nd channel) <br /&gt1 – reversed (Q1 - 2nd channel, Q2 - 1st channel)</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | default |
| 1 | reversed |

The manufacturer defined default value is ```0``` (default).

This parameter has the configuration ID ```config_25_1``` and is of type ```INTEGER```.


### Parameter 30: Alarm configuration - 1st slot

Alarm configuration - 1st slot
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification. <br /&gtAvailable values <br /&gt1B [MSB] – Notification Type <br /&gt2B – Notification Status <br /&gt3B – Event/State Parameters <br /&gt4B [LSB] – action: <br /&gt 0x00 – no action<br /&gt 0xX1 – turn ON<br /&gt 0xX2 – turn OFF<br /&gt 0xX3 – turn ON/OFF continuously<br /&gt 0x0X – no action on LED frame<br /&gt 0x1X – LED frame blinks red<br /&gt 0x2X – LED frame blinks green<br /&gt 0x4X – LED frame blinks blue<br /&gt 0x8X – disable LED frame<br /&gt 0xFX – LED frame LAPD signal (red-white-blue)</p&gt
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_30_4``` and is of type ```INTEGER```.


### Parameter 31: Alarm configuration - 2nd slot

Alarm configuration - 2nd slot
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification. <br /&gtAvailable values <br /&gt1B [MSB] – Notification Type <br /&gt2B – Notification Status <br /&gt3B – Event/State Parameters <br /&gt4B [LSB] – action: <br /&gt 0x00 – no action<br /&gt 0xX1 – turn ON<br /&gt 0xX2 – turn OFF<br /&gt 0xX3 – turn ON/OFF continuously<br /&gt 0x0X – no action on LED frame<br /&gt 0x1X – LED frame blinks red<br /&gt 0x2X – LED frame blinks green<br /&gt 0x4X – LED frame blinks blue<br /&gt 0x8X – disable LED frame<br /&gt 0xFX – LED frame LAPD signal (red-white-blue)</p&gt
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_31_4``` and is of type ```INTEGER```.


### Parameter 32: Alarm configuration - 3rd slot

Alarm configuration - 3rd slot
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification.<br /&gtAvailable Values<br /&gt1B [MSB] – Notification Type <br /&gt2B – Notification Status <br /&gt3B – Event/State Parameters <br /&gt4B [LSB] – action: <br /&gt 0x00 – no action<br /&gt 0xX1 – turn ON<br /&gt 0xX2 – turn OFF<br /&gt 0xX3 – turn ON/OFF continuously<br /&gt 0x0X – no action on LED frame<br /&gt 0x1X – LED frame blinks red<br /&gt 0x2X – LED frame blinks green<br /&gt 0x4X – LED frame blinks blue<br /&gt 0x8X – disable LED frame<br /&gt 0xFX – LED frame LAPD signal (red-white-blue)</p&gt
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_32_4``` and is of type ```INTEGER```.


### Parameter 33: Alarm configuration - 4th slot

Alarm configuration - 4th slot
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification.<br /&gtAvailable values <br /&gt1B [MSB] – Notification Type <br /&gt2B – Notification Status <br /&gt3B – Event/State Parameters <br /&gt4B [LSB] – action: <br /&gt 0x00 – no action<br /&gt 0xX1 – turn ON<br /&gt 0xX2 – turn OFF<br /&gt 0xX3 – turn ON/OFF continuously<br /&gt 0x0X – no action on LED frame<br /&gt 0x1X – LED frame blinks red<br /&gt 0x2X – LED frame blinks green<br /&gt 0x4X – LED frame blinks blue<br /&gt 0x8X – disable LED frame<br /&gt 0xFX – LED frame LAPD signal (red-white-blue)</p&gt
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_33_4``` and is of type ```INTEGER```.


### Parameter 34: Alarm configuration - 5th slot

Alarm configuration - 5th slot
<p&gtThis parameter determines to which alarm frames and how the device should react. The parameters consist of 4 bytes, three most significant bytes are set according to the official Z-Wave protocol specification.<br /&gtAvailable values <br /&gt1B [MSB] – Notification Type <br /&gt2B – Notification Status <br /&gt3B – Event/State Parameters <br /&gt4B [LSB] – action: <br /&gt 0x00 – no action<br /&gt 0xX1 – turn ON<br /&gt 0xX2 – turn OFF<br /&gt 0xX3 – turn ON/OFF continuously<br /&gt 0x0X – no action on LED frame<br /&gt 0x1X – LED frame blinks red<br /&gt 0x2X – LED frame blinks green<br /&gt 0x4X – LED frame blinks blue<br /&gt 0x8X – disable LED frame<br /&gt 0xFX – LED frame LAPD signal (red-white-blue)</p&gt
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_34_4``` and is of type ```INTEGER```.


### Parameter 35: Alarm configuration – duration

Alarm configuration – duration
<p&gtThis parameter defines duration of alarm sequence. When time set in this parameter elapses, alarm is cancelled, LED frame and relay restore normal operation, but do not recover state from before the alarm.<br /&gtAvailable values <br /&gt0 – infinite <br /&gt1-32400 (1s-9h, 1s step) – duration</p&gt
Values in the range 0 to 32400 may be set.

The manufacturer defined default value is ```600```.

This parameter has the configuration ID ```config_35_2``` and is of type ```INTEGER```.


### Parameter 40: First button – scenes sent

First button – scenes sent
<p&gtThis parameter determines which actions result in sending scene IDs assigned to them. Values can be combined (e.g. 1+2=3 means that scenes for single and double click are sent). Enabling scenes for triple click disables entering the device in learn mode by triple clicking.<br /&gtAvailable values <br /&gt1 – Key pressed 1 time <br /&gt2 – Key pressed 2 times <br /&gt4 – Key pressed 3 times <br /&gt8 – Key hold down and key released</p&gt
The following option values may be configured, in addition to values in the range 0 to 15 -:

| Value  | Description |
|--------|-------------|
| 1 | Key pressed 1 time |
| 2 | Key pressed 2 times |
| 4 | Key pressed 3 times |
| 8 | Key hold down and key released |

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_40_1``` and is of type ```INTEGER```.


### Parameter 41: Second button – scenes sent

Second button – scenes sent
<p&gtThis parameter determines which actions result in sending scene IDs assigned to them. Values can be combined (e.g. 1+2=3 means that scenes for single and double click are sent). Enabling scenes for triple click disables entering the device in learn mode by triple clicking. <br /&gtAvailable values <br /&gt1 – Key pressed 1 time <br /&gt2 – Key pressed 2 times <br /&gt4 – Key pressed 3 times <br /&gt8 – Key hold down and key released</p&gt
The following option values may be configured, in addition to values in the range 0 to 15 -:

| Value  | Description |
|--------|-------------|
| 1 | Key pressed 1 time |
| 2 | Key pressed 2 times |
| 4 | Key pressed 3 times |
| 8 | Key hold down and key released |

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_41_1``` and is of type ```INTEGER```.


### Parameter 60: Power reports – include self-consumption

Power reports – include self-consumption
<p&gtThis parameter determines whether the power measurements for Endpoint 1 should include power consumed by the device itself.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Self-consumption not included |
| 1 | Self-consumption included |

The manufacturer defined default value is ```0``` (Self-consumption not included).

This parameter has the configuration ID ```config_60_1``` and is of type ```INTEGER```.


### Parameter 61: Power reports for first channel – on change

Power reports for first channel – on change
<p&gtThis parameter defines minimal change (from the last reported) in measured power that results in sending new report. For loads under 50W the parameter is irrelevant, report are sent every 5W change.<br /&gtAvailable values <br /&gt0 – reporting on change disabled <br /&gt1-500 (1-500%, 1% step) – minimal change</p&gt
Values in the range 0 to 500 may be set.

The manufacturer defined default value is ```15```.

This parameter has the configuration ID ```config_61_2``` and is of type ```INTEGER```.


### Parameter 62: Power reports for first channel – periodic

Power reports for first channel – periodic
<p&gtThis parameter defines reporting interval for measured power. Periodic reports are independent from changes in value (parameter 61).<br /&gtAvailable values <br /&gt0 – periodic reports disabled <br /&gt30-32400 (30s-9h, 1s step) – time interval</p&gt
Values in the range 0 to 32400 may be set.

The manufacturer defined default value is ```2600```.

This parameter has the configuration ID ```config_62_2``` and is of type ```INTEGER```.


### Parameter 63: Power reports for second channel – on change

Power reports for second channel – on change
<p&gt<strong&gtOnly in Double Switch Mode</strong&gt</p&gt <p&gtThis parameter defines minimal change (from the last reported) in measured power that results in sending new report. For loads under 50W the parameter is irrelevant, report are sent every 5W change.<br /&gtAvailable values <br /&gt0 – reporting on change disabled <br /&gt1-500 (1-500%, 1% step) – minimal change</p&gt
Values in the range 0 to 500 may be set.

The manufacturer defined default value is ```15```.

This parameter has the configuration ID ```config_63_2``` and is of type ```INTEGER```.


### Parameter 64: Power reports for second channel – periodic

Power reports for second channel – periodic
<p&gt<strong&gtOnly in Double Switch Mode</strong&gt</p&gt <p&gtThis parameter defines reporting interval for measured power. Periodic reports are independent from changes in value (parameter 63).<br /&gtAvailable values <br /&gt0 – periodic reports disabled <br /&gt30-32400 (30s-9h, 1s step) – time interval<br /&gt</p&gt
Values in the range 0 to 32400 may be set.

The manufacturer defined default value is ```3600```.

This parameter has the configuration ID ```config_64_2``` and is of type ```INTEGER```.


### Parameter 65: Energy reports for first channel – on change

Energy reports for first channel – on change
<p&gtThis parameter defines minimal change (from the last reported) in measured energy that results in sending new report.<br /&gtAvailable values <br /&gt0 – reporting on change disabled <br /&gt1-500 (0.01-5kWh, 0.01kWh step) – minimal change</p&gt
Values in the range 0 to 500 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_65_2``` and is of type ```INTEGER```.


### Parameter 66: Energy reports for first channel – periodic

Energy reports for first channel – periodic
<p&gtThis parameter defines reporting interval for measured energy. Periodic reports are independent from changes in value (parameter 65).<br /&gtAvailable values <br /&gt0 – periodic reports disabled <br /&gt30-32400 (30s-9h, 1s step) – time interval</p&gt
Values in the range 0 to 32400 may be set.

The manufacturer defined default value is ```3600```.

This parameter has the configuration ID ```config_66_2``` and is of type ```INTEGER```.


### Parameter 67: Energy reports for second channel – on change

Energy reports for second channel – on change
<p&gt<strong&gtOnly in Double Switch Mode </strong&gt</p&gt <p&gtDescription This parameter defines minimal change (from the last reported) in measured energy that results in sending new report.<br /&gtAvailable values <br /&gt0 – reporting on change disabled <br /&gt1-500 (0.01-5kWh, 0.01kWh step) – minimal change</p&gt
Values in the range 0 to 500 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_67_2``` and is of type ```INTEGER```.


### Parameter 68: Energy reports for second channel – periodic

Energy reports for second channel – periodic
<p&gt<strong&gtOnly in Double Switch Mode </strong&gt</p&gt <p&gtDescription This parameter defines reporting interval for measured energy. Periodic reports are independent from changes in value (parameter 67). <br /&gtAvailable values <br /&gt0 – periodic reports disabled <br /&gt30-32400 (30s-9h, 1s step) – time interval</p&gt
Values in the range 0 to 32400 may be set.

The manufacturer defined default value is ```3600```.

This parameter has the configuration ID ```config_68_2``` and is of type ```INTEGER```.


### Parameter 150: First channel – operating mode

First channel – operating mode
<p&gtThis parameter allows to choose operating for the 1st channel .</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | standard operation |
| 1 | delayed OFF |
| 2 | single pulse |

The manufacturer defined default value is ```0``` (standard operation).

This parameter has the configuration ID ```config_150_1``` and is of type ```INTEGER```.


### Parameter 151: Second channel – operating mode

Second channel – operating mode
<p&gt<strong&gtOnly in Double Switch Mode </strong&gt</p&gt <p&gtThis parameter allows to choose operating for the 2nd channel.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | standard operation |
| 1 | delayed OFF |
| 2 | single pulse |

The manufacturer defined default value is ```0``` (standard operation).

This parameter has the configuration ID ```config_151_1``` and is of type ```INTEGER```.


### Parameter 152: 1st ch - reaction to switch

First channel - reaction to switch for delayed OFF / pulse modes
<p&gtThis parameter determines how the device in timed mode reacts to pushing the button for 1st channel. The parameter is relevant only for button toggles modes (parameter 20 set to 1 or 3).</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | cancel mode and set default state |
| 1 | no reaction - mode runs until it ends |
| 2 | reset timer - start counting from the beginning |

The manufacturer defined default value is ```0``` (cancel mode and set default state).

This parameter has the configuration ID ```config_152_1``` and is of type ```INTEGER```.


### Parameter 153: 2nd ch - reaction to switch

Second channel - reaction to switch for delayed OFF / pulse modes
<p&gt<strong&gtOnly in Double Switch Mode </strong&gt</p&gt <p&gtThis parameter determines how the device in timed mode reacts to pushing the button for 2nd channel. The parameter is relevant only for button toggles modes (parameter 20 set to 1 or 3).</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | cancel mode and set default state |
| 1 | no reaction - mode runs until it ends |
| 2 | reset timer - start counting from the beginning |

The manufacturer defined default value is ```0``` (cancel mode and set default state).

This parameter has the configuration ID ```config_153_1``` and is of type ```INTEGER```.


### Parameter 154: 1st ch - time parameter for delayed

First channel - time parameter for delayed OFF / pulse modes
<p&gtThis parameter allows to set time parameter used in timed modes for 1st channel (parameter 150). Delay time for switching off or duration of the pulse. <br /&gtAvailable values <br /&gt0 – 0.1 second <br /&gt1-32000 (1-32000s, 1s step) – time parameter</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```50```.

This parameter has the configuration ID ```config_154_2``` and is of type ```INTEGER```.


### Parameter 155: 2nd ch - time parameter for delayed

Second channel - time parameter for delayed OFF / pulse modes
<p&gt<strong&gtOnly in Double Switch Mode</strong&gt</p&gt <p&gtThis parameter allows to set time parameter used in timed modes for 2nd channel (parameter 151). Delay time for switching off or duration of the pulse. <br /&gtAvailable values <br /&gt0 – 0.1 second <br /&gt1-32000 (1-32000s, 1s step) – time parameter</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```50```.

This parameter has the configuration ID ```config_155_2``` and is of type ```INTEGER```.


### Parameter 156: 1st ch – ON value sent to 2nd/3rd

First channel – Switch ON value sent to 2nd and 3rd association groups
<p&gtThis parameter defines value sent with Switch ON command to devices associated in 2nd and 3rd association group for manual changes of Endpoint 1 state. <br /&gtAvailable values<br /&gt0-99 or 255</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```255```.

This parameter has the configuration ID ```config_156_2``` and is of type ```INTEGER```.


### Parameter 157: 1st ch – OFF value sent to 2nd/3rd

First channel – Switch OFF value sent to 2nd and 3rd association groups
<p&gtThis parameter defines value sent with Switch OFF command to devices associated in 2nd and 3rd association group for manual changes of Endpoint 1 state. <br /&gtAvailable values <br /&gt0-99 or 255</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_157_2``` and is of type ```INTEGER```.


### Parameter 158: 1st ch – Double Click value sent to 2nd/3rd

First channel – Double Click value sent to 2nd and 3rd association groups
<p&gtThis parameter defines value sent with Double Click command to devices associated in 2nd and 3rd association group for manual changes of Endpoint 1 state. <br /&gtAvailable values <br /&gt0-99 or 255</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```99```.

This parameter has the configuration ID ```config_158_2``` and is of type ```INTEGER```.


### Parameter 159: 2nd ch – ON value sent to 4th/5th

Second channel – Switch ON value sent to 4th and 5th association groups
<p&gt<strong&gtOnly in Double Switch Mode </strong&gt</p&gt <p&gtThis parameter defines value sent with Switch ON command to devices associated in 4th and 5th association group for manual changes of Endpoint 2 state. <br /&gtAvailable values <br /&gt0-99 or 255 </p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```255```.

This parameter has the configuration ID ```config_159_2``` and is of type ```INTEGER```.


### Parameter 160: 2nd ch – OFF value sent to 4th/5th

Second channel – Switch OFF value sent to 4th and 5th association groups
<p&gt<strong&gtOnly in Double Switch Mode </strong&gt</p&gt <p&gtThis parameter defines value sent with Switch OFF command to devices associated in 4th and 5th association group for manual changes of Endpoint 2 state. <br /&gtAvailable values <br /&gt0-99 or 255</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_160_2``` and is of type ```INTEGER```.


### Parameter 161: 2nd ch – Double Click value sent to 4th/5th

Second channel – Double Click value sent to 4th and 5th association groups
<p&gt<strong&gtOnly in Double Switch Mode</strong&gt</p&gt <p&gtThis parameter defines value sent with Double Click command to devices associated in 4th and 5th association group for manual changes of Endpoint 2 state. <br /&gtAvailable values <br /&gt0-99 or 255</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```99```.

This parameter has the configuration ID ```config_161_2``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The FGWDSEU supports 5 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
<p&gt1st association group – “Lifeline” reports the device status and allows for assigning single device only (main controller by default).</p&gt

Association group 1 supports 1 node.

### Group 2: ON/OFF (1)

<p&gt2nd association group – “On/Off (1)” is used to turn the associated devices on/off reflecting button operation for 1st channel (uses Basic command class).</p&gt

Association group 2 supports 5 nodes.

### Group 3: Dimmer (1)

<p&gt3rd association group – “Dimmer (1)” is used to change level of associated devices reflecting button operation for 1st channel (uses Switch Multilevel command class).</p&gt

Association group 3 supports 5 nodes.

### Group 4: ON/OFF (2)

<p&gt4th association group* – “On/Off (2)” is used to turn the associated devices on/off reflecting button operation for 2nd channel (uses Basic command class).</p&gt

Association group 4 supports 5 nodes.

### Group 5: Dimmer (2)

<p&gt5th association group* – “Dimmer (2)” is used to change level of associated devices reflecting button operation for 2nd channel (uses Switch Multilevel command class).</p&gt

Association group 5 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_APPLICATION_STATUS_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
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
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_METER_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 2

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_APPLICATION_STATUS_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_METER_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Manual](https://opensmarthouse.org/zwavedatabase/1181/FGWDSEU-221-T-EN-0-16.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1181).
