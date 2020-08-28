---
layout: documentation
title: LZW30-SN - ZWave
---

{% include base.html %}

# LZW30-SN Red Series On/Off switch
This describes the Z-Wave device *LZW30-SN*, manufactured by *Inovelli* with the thing type UID of ```inovelli_lzw30sn_00_000```.

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![LZW30-SN product image](https://opensmarthouse.org/zwavedatabase/1113/image/)


The LZW30-SN supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtInovelli’s new on/off switch is packed with features and designed with your house in mind.</p&gt <p&gtFrom a hardware side we’ve upgraded our Z-Wave module for better distance and added in one of our favorite features: power monitoring. The switch can be used in any 3-Way or 4-Way setting and allows you to use either an auxiliary switch, or your existing dumb switch.</p&gt <p&gtFrom a firmware side, this switch has it all (literally). Scene control, notifications (flashes if your garage door is left open) and can be configured on any HUB.</p&gt

### Inclusion Information

<p&gtOnce the inclusion process has started press the config button 3X and the LED bar will flash blue.  If the switch was included successfully the bar will turn GREEN, however if the switch was not included successfully it will turn RED.</p&gt

### Exclusion Information

<p&gtPut your HUB in Exclusion mode and press the Config Button 3X until your HUB says the device is excluded.</p&gt

## Channels

The following table summarises the channels available for the LZW30-SN -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Reset Meter  [Deprecated]| meter_reset | meter_reset | Energy | Switch | 
| Scene Number | scene_number | scene_number |  | Number | 
| LED strip effect | config_decimal | config_decimal |  | Number | 
| LED Indicator Color | config_decimal | config_decimal |  | Number | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Reset Meter [Deprecated]
Reset the meter.

The ```meter_reset``` channel is of type ```meter_reset``` and supports the ```Switch``` item and is in the ```Energy``` category.

**Note:** This channel is marked as deprecated so should not be used.

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

### LED strip effect
Generic class for configuration parameter.

The ```config_decimal``` channel is of type ```config_decimal``` and supports the ```Number``` item.

### LED Indicator Color
Generic class for configuration parameter.

The ```config_decimal``` channel is of type ```config_decimal``` and supports the ```Number``` item.



## Device Configuration

The following table provides a summary of the 17 configuration parameters available in the LZW30-SN.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Power On State | Power On State |
| 2 | Invert Switch | Invert Switch |
| 3 | Auto Off Timer | Auto Off Timer |
| 4 | Association Behavior | Association Behavior |
| 5 | LED Indicator Color | LED Indicator Color |
| 6 | LED Indicator Intensity | LED Indicator Intensity |
| 7 | LED Indicator Intensity (When Off) | LED Indicator Intensity (When Off) |
| 8 | LED Strip Effect | LED Strip Effect |
| 8 | LED Strip Effect (Color) | LED Strip Effect (Color) |
| 8 | LED Strip Effect (Brightness) | LED Strip Effect (Brightness) |
| 8 | LED Strip Effect (Duration) | LED Strip Effect (Duration) |
| 8 | LED Strip Effect (Effect) | LED Strip Effect (Effect) |
| 9 | LED Strip Timeout | LED Strip Timeout |
| 10 | Active Power Reports | Active Power Reports |
| 11 | Periodic Power & Energy Reports | Periodic Power & Energy Reports |
| 12 | Energy Reports | Energy Reports |
| 51 | Enable Instant On | Enable Instant On |

### Parameter 1: Power On State

Power On State
<p&gtWhen power is restored, the switch reverts to either On, Off or last level</p&gt <p&gt0 = Returns to State before Power Outage</p&gt <p&gt1 = On</p&gt <p&gt2 = Off</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Prior State |
| 1 | On |
| 2 | Off |

The manufacturer defined default value is ```0``` (Prior State).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: Invert Switch

Invert Switch
<p&gtInverts the switch (Tap Down = On, Tap Up = Off)</p&gt <p&gt0 = Disabled</p&gt <p&gt1 = Enabled</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Disabled |
| 1 | Enabled |

The manufacturer defined default value is ```0``` (Disabled).

This parameter has the configuration ID ```config_2_1``` and is of type ```INTEGER```.


### Parameter 3: Auto Off Timer

Auto Off Timer
<p&gtAutomatically turns the switch off after x amount of seconds</p&gt <p&gt0 = Disabled</p&gt <p&gt1= 1 second</p&gt <p&gt32767 = 32767 seconds</p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_3_2``` and is of type ```INTEGER```.


### Parameter 4: Association Behavior

Association Behavior
<p&gtWhen should the switch send commands to the associated devices:</p&gt <p&gt01 = Local, 02 = 3-Way, 03 = 3-Way & Local, 04 = Z-Wave Hub, 05 = Z-Wave Hub & Local, 06 = Z-Wave Hub & 3-Way, 07 = Z-Wave Hub & Local & 3-Way, 08 = Timer, 09 = Timer & Local, 10 = Timer & 3-Way, 11 =Timer & 3-Way & Local, 12 = Timer & Z-Wave Hub, 13 = Timer & Z-Wave Hub & Local, 14 = Timer & Z-Wave Hub & 3-Way, 15 = All</p&gt
Values in the range 0 to 15 may be set.

The manufacturer defined default value is ```15```.

This parameter has the configuration ID ```config_4_1``` and is of type ```INTEGER```.


### Parameter 5: LED Indicator Color

LED Indicator Color
<p&gtThis will set the default color of the LED bar.  Calculated by using a hue color circle (Value / 255 * 360).  Se website for more info.</p&gt <p&gt0 = Red</p&gt <p&gt21 = Orange</p&gt <p&gt42 = Yellow</p&gt <p&gt85 = Green</p&gt <p&gt127 = Cyan</p&gt <p&gt170 = Blue</p&gt <p&gt212 = Violet</p&gt <p&gt234 = Pink</p&gt
The following option values may be configured, in addition to values in the range 0 to 255 -:

| Value  | Description |
|--------|-------------|
| 0 | Red |
| 21 | Orange |
| 42 | Yellow |
| 85 | Green |
| 127 | Cyan |
| 170 | Blue |
| 212 | Violet |
| 234 | Pink |
| 255 | White |

The manufacturer defined default value is ```170``` (Blue).

This parameter has the configuration ID ```config_5_2``` and is of type ```INTEGER```.


### Parameter 6: LED Indicator Intensity

LED Indicator Intensity
<p&gtThis will set the intensity of the LED bar (ie: how bright it is) in 10% increments</p&gt <p&gt0 = Off</p&gt <p&gt1 = Low</p&gt <p&gt5 = Medium</p&gt <p&gt10 = High</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Off |
| 1 | 10% |
| 2 | 20% |
| 3 | 30% |
| 4 | 40% |
| 5 | 50% |
| 6 | 60% |
| 7 | 70% |
| 8 | 80% |
| 9 | 90% |
| 10 | 100% |

The manufacturer defined default value is ```5``` (50%).

This parameter has the configuration ID ```config_6_1``` and is of type ```INTEGER```.


### Parameter 7: LED Indicator Intensity (When Off)

LED Indicator Intensity (When Off)
<p&gtThis is the intensity of the LED bar when the switch is off in 10% increments.</p&gt <p&gt0 = Off</p&gt <p&gt1 = Low</p&gt <p&gt5 = Medium</p&gt <p&gt10 = High</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Off |
| 1 | 10% |
| 2 | 20% |
| 3 | 30% |
| 4 | 40% |
| 5 | 50% |
| 6 | 60% |
| 7 | 70% |
| 8 | 80% |
| 9 | 90% |
| 10 | 100% |

The manufacturer defined default value is ```1``` (10%).

This parameter has the configuration ID ```config_7_1``` and is of type ```INTEGER```.


### Parameter 8: LED Strip Effect

LED Strip Effect
<p&gtThis will allow you to add some sweet effects to your LED bar (ie: pulse, chase, solid, etc)</p&gt <p&gtByte 1 = Color.  This will set the color of the LED bar.  Calculated by using a hue color circle (Value / 255 * 360).</p&gt <p&gtByte 2 = Brightness.  Sets the brightness in 10% increments.</p&gt <p&gtByte 3 = Duration. </p&gt <p&gtByte 4 = Effect. Sets the effect: 0 = Off, 1 = Solid, 2 = Fast Blink, 3 = Slow Blink, 4 = Pulse</p&gt <p&gtAddition:  To get the final code for the desired color/effect/duration, use this calculator:  https://nathanfiscus.github.io/inovelli-notification-calc/</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 83823359 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_8_4``` and is of type ```INTEGER```.


### Parameter 8: LED Strip Effect (Color)

LED Strip Effect (Color)
<p&gtThis will allow you to add some sweet effects to your LED bar (ie: pulse, chase, solid, etc)</p&gt <p&gtThis will set the color of the LED bar.  Calculated by using a hue color circle (Value / 255 * 360).</p&gt <p&gt0 = Red</p&gt <p&gt21 = Orange</p&gt <p&gt42 = Yellow</p&gt <p&gt85 = Green</p&gt <p&gt127 = Cyan</p&gt <p&gt170 = Blue</p&gt <p&gt212 = Violet</p&gt <p&gt234 = Pink</p&gt
The following option values may be configured, in addition to values in the range 0 to 255 -:

| Value  | Description |
|--------|-------------|
| 0 | Red |
| 21 | Orange |
| 42 | Yellow |
| 85 | Green |
| 127 | Cyan |
| 170 | Blue |
| 212 | Violet |
| 234 | Pink |
| 255 | White |

The manufacturer defined default value is ```0``` (Red).

This parameter has the configuration ID ```config_8_4_000000FF``` and is of type ```INTEGER```.


### Parameter 8: LED Strip Effect (Brightness)

LED Strip Effect (Brightness)
<p&gtThis will allow you to add some sweet effects to your LED bar (ie: pulse, chase, solid, etc)</p&gt <p&gtSets the brightness in 10% increments.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Off |
| 1 | 10% |
| 2 | 20% |
| 3 | 30% |
| 4 | 40% |
| 5 | 50% |
| 6 | 60% |
| 7 | 70% |
| 8 | 80% |
| 9 | 90% |
| 10 | 100% |

The manufacturer defined default value is ```0``` (Off).

This parameter has the configuration ID ```config_8_4_0000FF00``` and is of type ```INTEGER```.


### Parameter 8: LED Strip Effect (Duration)

LED Strip Effect (Duration)
<p&gtThis will allow you to add some sweet effects to your LED bar (ie: pulse, chase, solid, etc)</p&gt <p&gt1 = 1 Second</p&gt <p&gt2 = 2 Seconds</p&gt <p&gt3 = 3 Seconds</p&gt <p&gt4 = 4 Seconds</p&gt <p&gt5 = 5 Seconds</p&gt <p&gt6 = 6 Seconds</p&gt <p&gt7 = 7 Seconds</p&gt <p&gt8 = 8 Seconds</p&gt <p&gt9 = 9 Seconds</p&gt <p&gt10 = 10 Seconds</p&gt <p&gt20 = 20 Seconds</p&gt <p&gt30 = 30 Seconds</p&gt <p&gt40 = 40 Seconds</p&gt <p&gt50 = 50 Seconds</p&gt <p&gt60 = 60 Seconds</p&gt <p&gt62 = 2 Minutes</p&gt <p&gt63 = 3 Minutes</p&gt <p&gt64 = 4 Minutes</p&gt <p&gt65 = 5 Minutes</p&gt <p&gt255 = Indefinitely</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_8_4_00FF0000``` and is of type ```INTEGER```.


### Parameter 8: LED Strip Effect (Effect)

LED Strip Effect (Effect)
<p&gtThis will allow you to add some sweet effects to your LED bar (ie: pulse, chase, solid, etc)</p&gt <p&gt0 = Off</p&gt <p&gt1 = Solid</p&gt <p&gt2 = Fast Blink</p&gt <p&gt3 = Slow Blink</p&gt <p&gt4 = Pulse</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Off |
| 1 | Solid |
| 2 | Fast Blink |
| 3 | Slow Blink |
| 4 | Pulse |

The manufacturer defined default value is ```0``` (Off).

This parameter has the configuration ID ```config_8_4_7F000000``` and is of type ```INTEGER```.


### Parameter 9: LED Strip Timeout

LED Strip Timeout
<p&gtWhen the LED strip is disabled (LED Strip Intensity is set to 0), this setting allows the LED strip to turn on temporarily while being adjusted.  Time is in seconds.<br /&gt</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Stay Off |
| 1 | One Second |
| 2 | Two Seconds |
| 3 | Three Seconds |
| 4 | Four Seconds |
| 5 | Five Seconds |
| 6 | Six Seconds |
| 7 | Seven Seconds |
| 8 | Eight Seconds |
| 9 | Nine Seconds |
| 10 | Ten Seconds |

The manufacturer defined default value is ```0``` (Stay Off).

This parameter has the configuration ID ```config_9_1``` and is of type ```INTEGER```.


### Parameter 10: Active Power Reports

Active Power Reports
<p&gtThe power level change that will result in a new power report being sent (% of previous report)</p&gt <p&gt0 = Disabled</p&gt <p&gt10 = 10% of previous report</p&gt <p&gt100 = 100% of previous report</p&gt
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_10_1``` and is of type ```INTEGER```.


### Parameter 11: Periodic Power & Energy Reports

Periodic Power & Energy Reports
<p&gtTime period between consecutive power and energy reports being sent (in seconds)</p&gt <p&gt0 = 0 seconds</p&gt <p&gt1 = 1 second</p&gt <p&gt32767 = 32767 seconds</p&gt <p&gtTimer resets after every report is sent</p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```3600```.

This parameter has the configuration ID ```config_11_2``` and is of type ```INTEGER```.


### Parameter 12: Energy Reports

Energy Reports
<p&gtThe energy level change that will result in a new energy report being sent (% of previous report)</p&gt <p&gt0 = Disabled</p&gt <p&gt10 = 10% of previous report</p&gt <p&gt100 = 100% of previous report</p&gt
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_12_1``` and is of type ```INTEGER```.


### Parameter 51: Enable Instant On

Enable Instant On
<p&gtEnable instant on (ie: disable the 700ms delay) for physical switch. Note, if you disable the delay, it will also disable scene control except for Button 1 (ie: tap up 1x or tap down 1x) and button 7 (config button). All other buttons (2-6) will be disabled.</p&gt 0: No Delay 1: 700ms Delay (default)
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | No delay |
| 1 | 700ms delay |

The manufacturer defined default value is ```1``` (700ms delay).

This parameter has the configuration ID ```config_51_1``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The LZW30-SN supports 2 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
<p&gtMembers of this group will receive unsolicited messages related to the status of the switch.</p&gt

Association group 1 supports 5 nodes.

### Group 2: Basic Set

<p&gt1. Single press Up button sends BasicSet (0xFF)</p&gt <p&gtand</p&gt <p&gt2. Single press Down sends BasicSet (0x00)</p&gt

Association group 2 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_APPLICATION_STATUS_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| Linked to BASIC|
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_CENTRAL_SCENE_V3| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_PROTECTION_V2| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Product manual](https://opensmarthouse.org/zwavedatabase/1113/LZW30-SN-Manual.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1113).
