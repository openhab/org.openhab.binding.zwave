---
layout: documentation
title: ZMNHHD - ZWave
---

{% include base.html %}

# ZMNHHD Mini Dimmer
This describes the Z-Wave device *ZMNHHD*, manufactured by *[Goap](http://www.qubino.com/)* with the thing type UID of ```qubino_zmnhhd_00_000```.

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![ZMNHHD product image](https://opensmarthouse.org/zwavedatabase/1100/image/)


The ZMNHHD supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtMini Dimmer is a MOSFET-switching light device that also supports control of low-voltage halogen lamps with electronic transformers, dimmable compact fluorescent lights, and dimmable LED bulbs. It measures power consumption of the connected device. It supports push-button/momentary switches (default) and toggle switches. It can work with or without the neutral line. Qubino Mini Dimmer allows the easiest and quickest installation. It is designed to be mounted inside a “flush mounting box”, hidden behind a traditional wall switch. It acts as repeater in order to improve range and stability of Z-Wave network.</p&gt

### Inclusion Information

<p&gtInclusion</p&gt <p&gtAUTOMATICALLY ADDING THE DEVICE TO A Z-WAVE NETWORK (AUTO INCLUSION)</p&gt <ol&gt<li&gtEnable add/remove mode on your Z-Wave gateway (hub)</li&gt <li&gtAutomatic selection of secure/unsecure inclusion.</li&gt <li&gtThe device can be automatically added to a Z-Wave network during the first 2 minutes</li&gt <li&gtConnect the device to the power supply</li&gt <li&gtAuto-inclusion will be initiated within 5 seconds of connection to the power supply and the device will automatically enrol in your network. </li&gt </ol&gt<p&gt(when the device is excluded and connected to the power supply it automatically eneters the LEARN MODE state.)</p&gt <p&gt<strong&gtNOTE: LEARN MODE state allows the device to receive network infromation from the controller</strong&gt</p&gt <p&gt<strong&gtNOTE: Please wait at least 30 seconds between each inclusion and exclusion.</strong&gt</p&gt <p&gtMANUALLY ADDING THE DEVICE TO A Z-WAVE NETWORK (MANUAL INCLUSION)</p&gt <ol&gt<li&gtConnect the device to the power supply</li&gt <li&gtEnable add/remove mode on your Z-Wave gateway (hub)</li&gt <li&gtToggle the switch connected to the I1 terminal 3 times within 3 seconds (this procedure put the device in LEARN MODE)</li&gt </ol&gt<p&gtOR</p&gt <p&gtPress and hold the S (Service) button between 2 and 6 seconds if connected to 24-30VDC (this procedure put the device in LEARN MODE)</p&gt <p&gt4. A new device will appear on your dashboard</p&gt <p&gt<strong&gtNOTE: LEARN MODE state allows the device to receive network infromation from the conttoller </strong&gt</p&gt <p&gt<strong&gtNOTE: Please wait at least 30 seconds between each inclusion and exclusion.</strong&gt</p&gt <p&gtMini Dimmer supports the latest Security 2 feature. When adding the Mini Dimmer to a Z-Wave network with a controller supporting Security 2 (S2), the PIN code of the Z-Wave Device Specific Key (DSK) is required. The unique DSK code is printed on the product label and a copy is inserted in the packaging, which must not be lost. Do not remove the DSK from the product. As a backup measure, use the label in the packaging. The first five digits of the key are highlighted or underlined to help the user identify the PIN code portion of the DSK text.</p&gt

### Exclusion Information

<p&gtExclusion</p&gt <p&gtREMOVAL FROM A Z-WAVE NETWORK (Z-WAVE EXCLUSION)</p&gt <ol&gt<li&gtConnect the device to the power supply</li&gt <li&gtMake sure the device is within direct range of your Z-Wave gateway (hub) or use a hand-held Z-Wave remote to perform exclusion</li&gt <li&gtEnable add/remove mode on your Z-Wave gateway (hub)</li&gt <li&gtToggle the switch connected to the I1 terminal 3 times within 3 seconds (this procedure put the device in LEARN MODE)</li&gt </ol&gt<p&gtOR</p&gt <p&gtPress and hold the S (Service) button between 2 and 6 seconds if connected to 24-30VDC (this procedure put the device in LEARN MODE)</p&gt <ol&gt<li&gtThe device will be removed from your network, but any custom configuration parameters will not be erased</li&gt </ol&gt<p&gt<strong&gtNOTE: LEARN MODE state allows the device to receive network infromation from the conttoller </strong&gt</p&gt <p&gt<strong&gtNOTE: Please wait at least 30 seconds between each inclusion and exclusion.</strong&gt</p&gt

### General Usage Information

<p&gt<strong&gtFACTORY RESET</strong&gt</p&gt <ol&gt<li&gtConnect the device to the power supply.</li&gt <li&gtWithin the first minute (60seconds) the device is connected to the power supply, toggle the switch connected to the I1 terminal 5 times within 3 seconds.</li&gt </ol&gt<p&gtOR</p&gt <p&gtPress and hold the S (Service) button for at least 6 seconds if connected to 24-30VDC.</p&gt <p&gt<strong&gtNOTE:</strong&gt By resetting the device, all custom parameters previously set on the device will return to their default values, and the node ID will be deleted. Use this reset procedure only when the main gateway (hub) is missing or otherwise inoperable.</p&gt <p&gt<strong&gtNOTE:</strong&gt the reset with switch connected to I1 is possible only in the first minute after the device is connected to the power.</p&gt <p&gt<strong&gtNOTE:</strong&gt after the reset is successfully done the autocalibration will trigger and the green LED will start blinking.</p&gt <p&gt<strong&gtLED SIGNALIZATION FOR INCLUSION/EXCLUSION</strong&gt</p&gt <p&gtLED (green)</p&gt <ul&gt<li&gtLED is blinking (1 sec ON, 1 sec OFF) = module is excluded</li&gt <li&gtLED is ON = module is included</li&gt </ul&gt<p&gtLED (red)</p&gt <ul&gt<li&gtLED is OFF = normal operation</li&gt <li&gtLED is ON = overload</li&gt <li&gtLED is blinking (1 sec ON, 1 sec OFF) = over temperature</li&gt </ul&gt<p&gtLED (blue)</p&gt <ul&gt<li&gtLED is OFF = normal operation</li&gt <li&gtLED is blinking (1 sec ON, 1 sec OFF) = calibration in progress</li&gt <li&gtLED is ON = calibration failed</li&gt </ul&gt<p&gt<strong&gtNOTE:</strong&gt After each power cycle all 3 LEDs will blink once before resuming normal operation.</p&gt

## Channels

The following table summarises the channels available for the ZMNHHD -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Dimmer | switch_dimmer | switch_dimmer | DimmableLight | Dimmer | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Alarm (power) | alarm_power | alarm_power | Energy | Switch | 

### Dimmer
The brightness channel allows to control the brightness of a light.
            It is also possible to switch the light on and off.

The ```switch_dimmer``` channel is of type ```switch_dimmer``` and supports the ```Dimmer``` item and is in the ```DimmableLight``` category.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Alarm (power)
Indicates if a power alarm is triggered.

The ```alarm_power``` channel is of type ```alarm_power``` and supports the ```Switch``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |



## Device Configuration

The following table provides a summary of the 19 configuration parameters available in the ZMNHHD.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | In-wall Switch Type for Load to control I1 | In-wall Switch Type for Load to control I1 |
| 5 | Working mode | Working mode |
| 11 | Turn Load 1 Off Automatically with Timer | Turn Load 1 Off Automatically with Timer |
| 12 | Turn Load 1 On Automatically with Timer | Turn Load 1 On Automatically with Timer |
| 21 | Enable/Disable the Double click function | Enable/Disable the Double click function |
| 30 | Restore on/off status for load after power failure | Restore on/off status for load after power failure |
| 40 | Power Consumption Threshold | Power Consumption Threshold |
| 42 | Power Consumption Threshold (time) | Power Consumption Threshold (time) |
| 60 | Minimum dimming value | Minimum dimming value |
| 61 | Maximum dimming value | Maximum dimming value |
| 65 | Dimming time when key pressed | Dimming time when key pressed |
| 66 | Dimming time when key hold | Dimming time when key hold |
| 67 | Ignore start level | Ignore start level |
| 68 | Dimming duration | Dimming duration |
| 70 | Overload safety switch | Overload safety switch |
| 71 |  Calibration trigger | Calibration trigger |
| 72 | Calibration status (read only) | Calibration status (read only) |
| 73 | Alarm/Notification events | Alarm/Notification events |
| 74 | Alarm/Notification time interval | Alarm/Notification time interval |

### Parameter 1: In-wall Switch Type for Load to control I1

In-wall Switch Type for Load to control I1
<p&gtWith this parameter, you can select between push-button (momentary) and on/off toggle switch types. </p&gt <p&gtValues: </p&gt <p&gt• default value 0<br /&gt• 0 - push-button (momentary)<br /&gt• 1 - on/off toggle switch</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | push-button (momentary) |
| 1 | on/off toggle switch |

The manufacturer defined default value is ```0``` (push-button (momentary)).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 5: Working mode

Working mode
<p&gtWith this parameter, you can change the device presentation on the user interface.</p&gt <p&gtValues:<br /&gt• default value 0<br /&gt• 0 - Dimmer mode<br /&gt• 1 - Switch mode (works only in 3 way wiring-connection with neutral line)</p&gt <p&gt<strong&gtNOTE: After parameter change, first exclude the device (without setting parameters to default value) then wait at least 30s before reinclusion.</strong&gt</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Dimmer mode |
| 1 | Switch mode |

The manufacturer defined default value is ```0``` (Dimmer mode).

This parameter has the configuration ID ```config_5_1``` and is of type ```INTEGER```.


### Parameter 11: Turn Load 1 Off Automatically with Timer

Turn Load 1 Off Automatically with Timer
<p&gtIf Load is ON, you can schedule it to turn OFF automatically after a period of time defined in this parameter. The timer is reset to zero each time the device receives an ON or OFF command, either remotely (from the gateway (hub) or associated device) or locally from the switch.<br /&gt</p&gt <p&gtValues:<br /&gt• default value 0<br /&gt• 0 - Auto OFF Disabled<br /&gt• 1 - 32536 = 1 - 32536 seconds - Auto OFF timer enabled for a given amount of seconds </p&gt
Values in the range 0 to 32536 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_11_2``` and is of type ```INTEGER```.


### Parameter 12: Turn Load 1 On Automatically with Timer

Turn Load 1 On Automatically with Timer
<p&gtIf Load is OFF, you can schedule it to turn ON automatically after a period of time defined in this parameter. The timer is reset to zero each time the device receives an OFF or ON command, either remotely (from the gateway (hub) or associated device) or locally from the switch.</p&gt <p&gt<br /&gtValues:<br /&gt• default value 0<br /&gt• 0 - Auto ON Disabled<br /&gt• 1 - 32536 = 1 - 32536 seconds - Auto ON timer enabled for a given amount of seconds</p&gt
Values in the range 0 to 32536 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_12_2``` and is of type ```INTEGER```.


### Parameter 21: Enable/Disable the Double click function

Enable/Disable the Double click function
<p&gtIf the Double click function is enabled, a fast double click on the push-button will set the dimming level to the maximum dimming value.</p&gt <p&gt<br /&gtValues:<br /&gt• default value 0<br /&gt• 0 - double click disabled<br /&gt• 1 - double click enabled </p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Double click disabled |
| 1 | Double click enabled |

The manufacturer defined default value is ```0``` (Double click disabled).

This parameter has the configuration ID ```config_21_1``` and is of type ```INTEGER```.


### Parameter 30: Restore on/off status for load after power failure

Restore on/off status for load after power failure
<p&gtThis parameter determines if on/off status is saved and restored for the load after power failure.</p&gt <p&gt<br /&gtValues:<br /&gt• default value 0<br /&gt• 0 - Device saves last on/off status and restores it after a power failure.<br /&gt• 1 - Device does not save on/off status and does not restore it after a power failure, it remains off. </p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Save last on/off status |
| 1 | Don't save last on/off status |

The manufacturer defined default value is ```0``` (Save last on/off status).

This parameter has the configuration ID ```config_30_1``` and is of type ```INTEGER```.


### Parameter 40: Power Consumption Threshold

Power Consumption Threshold
<p&gtWatt Power Consumption Reporting Threshold for Load  </p&gt <p&gtChoose by how much the power consumption needs to increase or decrease to be reported. Values correspond to percentages so if 10 is set (by default), the device will report any power consumption changes of 10% or more compared to the last reading.</p&gt <p&gt<br /&gtValues (size is 1 byte dec):<br /&gt• default value 10<br /&gt• 0 - Power consumption reporting disabled<br /&gt• 1 - 100 = 1% - 100%</p&gt <p&gtPower consumption reporting enabled. New value is reported only when Wattage in real time changes by more than the percentage value set in this parameter compared to the previous Wattage reading, starting at 1% (the lowest value possible).</p&gt <p&gt<strong&gtNOTE: The power consumption needs to increase or decrease by at least 2 Watts to be reported, regardless of percentage set in this parameter.</strong&gt</p&gt
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_40_1``` and is of type ```INTEGER```.


### Parameter 42: Power Consumption Threshold (time)

Power Consumption Threshold (time)
<p&gtWatt Power Consumption Reporting Time Threshold for Load  </p&gt <p&gtSet value refers to the time interval with which power consumption in Watts is reported (0 – 32767 seconds). If 300 is entered, energy consumption reports will be sent to the gateway (hub) every 300 seconds (or 5 minutes) if there was a change compared from the last report.</p&gt <p&gt<br /&gtValues:<br /&gt• default value 0<br /&gt• 0 - Power consumption reporting on time interval disabled<br /&gt• 30 - 32767= 30 - 32767seconds. Power consumption reporting enabled. Report is sent according to time interval (value) set here.</p&gt <p&gt<br /&gt<strong&gtNOTE: Values from 1 to 29 are ignored by device due to standard recommendation.</strong&gt<br /&gt<strong&gtNOTE: The report will be send only if there was a change compared to the last report</strong&gt</p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_42_2``` and is of type ```INTEGER```.


### Parameter 60: Minimum dimming value

Minimum dimming value
<p&gtThe value set in this parameter determines the minimum dimming value (the lowest value which can be set on the device, when, for example, dimming lights with wall switch or slider in the GUI (Gateway - hub)).</p&gt <p&gtValues:<br /&gt• default value 0 = 0% (minimum dimming value)<br /&gt• 0- 98 = 0% - 98%, step is 1%. Minimum dimming value is set by entering a value.</p&gt <p&gt<strong&gtNOTE: The minimum level may not be higher than the maximum level!</strong&gt</p&gt
Values in the range 0 to 98 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_60_1``` and is of type ```INTEGER```.


### Parameter 61: Maximum dimming value

Maximum dimming value
<p&gtThe value set in this parameter determines the maximum dimming value (the highest value which can be set on the device, when, for example, dimming lights with wall switch or slider in the GUI (Gateway - hub))</p&gt <p&gtValues:<br /&gt• default value 99 = 99% (Maximum dimming value)<br /&gt• 1- 99 = 1% - 99%, step is 1%. Maximum dimming value is set by entering a value.</p&gt <p&gt<strong&gtNOTE: The maximum level may not be lower than the minimum level!</strong&gt</p&gt
Values in the range 1 to 99 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_61_1``` and is of type ```INTEGER```.


### Parameter 65: Dimming time when key pressed

Dimming time when key pressed
<p&gtChoose the time during which the device will move between the min. and max. dimming values by a short press of the push-button I1.<br /&gt</p&gt <p&gtValues:<br /&gt• default value 1 = 1s<br /&gt• 1 - 127 = 1 seconds- 127 seconds, step is 1 second </p&gt
Values in the range 1 to 127 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_65_1``` and is of type ```INTEGER```.


### Parameter 66: Dimming time when key hold

Dimming time when key hold
<p&gtChoose the time during which the Dimmer will move between the min. and max. dimming values during a continuous press of the push-button I1, by an associated device or through the UI controls (BasicSet, SwitchMultilevelSet).<br /&gt</p&gt <p&gtValues:<br /&gt• default value 3 = 3s<br /&gt• 1-127 = 1 second – 127 seconds<br /&gt• 128 – 253 = 1 minute – 126 minutes </p&gt
Values in the range 1 to 127 may be set.

The manufacturer defined default value is ```3```.

This parameter has the configuration ID ```config_66_2``` and is of type ```INTEGER```.


### Parameter 67: Ignore start level

Ignore start level
<p&gtChoose whether the device should use (or disregard) the start dimming level value. If the device is configured to use the start level, it should start the dimming process from the currently set dimming level. This parameter is used with association group 3.</p&gt <p&gtValues:<br /&gt• default value 0<br /&gt• 0 – use the start level value<br /&gt• 1 - ignore the start level value</p&gt <p&gt<strong&gtNOTE: Parameter is valid only in Dimmer mode. In Switch mode the parameter has no effect.</strong&gt</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | use the start level value |
| 1 | ignore the start level value |

The manufacturer defined default value is ```0``` (use the start level value).

This parameter has the configuration ID ```config_67_1``` and is of type ```INTEGER```.


### Parameter 68: Dimming duration

Dimming duration
<p&gtChoose the time during which the device will transition from the current value to the new target value. This parameter applies to the association group 3.</p&gt <p&gt<br /&gtValues:<br /&gt• default value 0 (dimming duration according to parameter 66)<br /&gt• 1 - 127 (from 1 to 127 seconds)</p&gt <p&gt<strong&gtNOTE: Parameter is valid only in Dimmer mode. In Switch mode the parameter has no effect.</strong&gt</p&gt
Values in the range 0 to 127 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_68_1``` and is of type ```INTEGER```.


### Parameter 70: Overload safety switch

Overload safety switch
<p&gtThe function allows for turning off the controlled device in case of exceeding the defined power for more than 5s. Controlled device can be turned back on by input I1 or sending a control frame.</p&gt <p&gt<br /&gtValues:<br /&gt• default value 200<br /&gt• 1 – 200 = 1 W – 200W<br /&gt• 0 = function not active</p&gt <p&gt<strong&gtNOTE: This functionality is not an overload safety protection, please check the technical specifications chapter for more details.</strong&gt</p&gt <p&gtIn case of overload the following message will be send towards the controller:<br /&gt• COMMAND\_CLASS\_NOTIFICATION_V5<br /&gt• The Alarm V1 type field set to 0x00<br /&gt• Notification Type 0x08 and 0x08 (Overload detected)</p&gt
Values in the range 1 to 200 may be set.

The manufacturer defined default value is ```200```.

This parameter has the configuration ID ```config_70_2``` and is of type ```INTEGER```.


### Parameter 71:  Calibration trigger

Calibration trigger
<p&gtChoose when will be the calibration procedure triggered.</p&gt <p&gt<br /&gtValues:<br /&gt• default value 0 - calibration done after power cycle if module is excluded<br /&gt• 1 – calibration done after power cycle regardless of inclusion status<br /&gt• 2 – force calibration. Calibration will start immediately </p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | calibration after exclusion |
| 1 | calibration after power cycle |
| 2 | force calibration |

The manufacturer defined default value is ```1``` (calibration after power cycle).

This parameter has the configuration ID ```config_71_1``` and is of type ```INTEGER```.


### Parameter 72: Calibration status (read only)

Calibration status (read only)
<p&gtWhit this parameter you can check the calibration status.</p&gt <p&gt<br /&gtValues:<br /&gt• default value 2 – calibration failed<br /&gt• 1 – calibration was successful<br /&gt• 2 – calibration failed </p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 1 | calibration was successful |
| 2 | calibration failed |

The manufacturer defined default value is ```2``` (calibration failed).

This parameter has the configuration ID ```config_72_1``` and is of type ```INTEGER```.
This is a read only parameter.


### Parameter 73: Alarm/Notification events

Alarm/Notification events
<p&gtThis parameter defines the module behaviour in case it receives any Alarm/Notification events.</p&gt <p&gt<br /&gtValues:<br /&gt• default value 0 – function not active<br /&gt• 1 – turn OFF<br /&gt• 2 – turn ON<br /&gt• 3 – start blinking (output turns 1s ON, and 1s OFF)</p&gt <p&gt<br /&gt<strong&gtNOTE: When value 3 is selected the default time interval of the blinking is 10 minutes. It can be stopped with a button press or sending a control frame. To adjust the time interval please refer to parameter 74 – Alarm/Notification time interval.</strong&gt</p&gt
Values in the range 0 to 3 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_73_1``` and is of type ```INTEGER```.


### Parameter 74: Alarm/Notification time interval

Alarm/Notification time interval
<p&gtAlarm/Notification time interval (dependant on parameter 73)</p&gt <p&gt<br /&gtThis parameter defines the time interval of the blinking state, once the module receives an alarm/notification event. Minimum step increase is 1 minute.</p&gt <p&gt<br /&gtValues (size is 1 byte dec):<br /&gt• default value 10 = 10 minutes<br /&gt• 1 – 125 = 1 -125 minutes</p&gt <p&gt<br /&gt<strong&gtNOTE: This parameter does not have any effect if parameter 73 is not set to value 3 </strong&gt</p&gt
Values in the range 1 to 125 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_74_1``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The ZMNHHD supports 4 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
Lifeline


Association group 1 supports 1 node.

### Group 2: Basic OnOff

Basic OnOff
<p&gtSupports the following command classes:</p&gt <p&gt• Basic set: triggered at change of output and reflecting its state</p&gt

Association group 2 supports 16 nodes.

### Group 3: StartStop level change

StartStop level change
<p&gtSupports the following command classes:</p&gt <p&gt• Start/Stop Level Change: triggered upon holding and releasing the switch connected to I1</p&gt <p&gt<strong&gtNOTE: Association 3 StartStop level change is switch type dependant. The switch must be a bi-stable switch type otherwise the association can not be trigered. To know how to change the switch type connected, please refer to chapter "Configuration parameters", in the official manual, for more information.</strong&gt</p&gt <p&gt<strong&gtNOTE: When the device is in switch mode (parameter 5 set to 1), this association group is</strong&gt<strong&gt not available. </strong&gt<br /&gt</p&gt

Association group 3 supports 16 nodes.

### Group 4: Multilevel set

Multilevel set
<p&gtSupports the following command classes:</p&gt <p&gt• Switch Multilevel Set: triggered at change of output and reflecting its state</p&gt <p&gt<strong&gtNOTE: When the device is in switch mode (parameter 5 set to 1), this association group is not available. </strong&gt</p&gt

Association group 4 supports 16 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| Linked to BASIC|
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V5| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [MiniDimmer_product_manual](https://opensmarthouse.org/zwavedatabase/1100/Qubino-Mini-Dimmer-extended-manual-eng-1-8.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1100).
