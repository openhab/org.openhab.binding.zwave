---
layout: documentation
title: MotionSwitch LED - ZWave
---

{% include base.html %}

# MotionSwitch LED Infrared motion detector with orientation light
This describes the Z-Wave device *MotionSwitch LED*, manufactured by *Steinel* with the thing type UID of ```steinel_motionswitchled_00_000```.

The device is in the category of *Light Bulb*, defining Devices that illuminate something, such as bulbs, etc..

![MotionSwitch LED product image](https://opensmarthouse.org/zwavedatabase/1060/image/)


The MotionSwitch LED supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtInfrared motion detector with orientation light and Z-Wave interface for indoor power sockets.</p&gt <p&gtThe ininfrared sensor can be used for switching light ON and OFF automatically. The unit is not suitable for burglar alarm systems as it is not tamperproof in the manner prescribed for such systems. The orientation light can be switched ON and OFF via the sensor or the button on the MotionSwitch LED. The relevant setting can be programmed.</p&gt

### Inclusion Information

<p&gtTo start the motion detector's inclusion mode, press the button three times in rapid succession. During inclusion mode and following configuration process device is blinking.</p&gt

### Exclusion Information

<p&gtTo start the motion detector's exclusion mode, press the button three times in rapid succession. During exclusion mode and following configuration process device is blinking.</p&gt <p&gtFollowing exclusion, all configuration parameters (time, sensitivity etc.) remain intact until the next inclusion.</p&gt

## Channels

The following table summarises the channels available for the MotionSwitch LED -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Dimmer | switch_dimmer | switch_dimmer | DimmableLight | Dimmer | 
| Sensor (luminance) | sensor_luminance | sensor_luminance |  | Number | 
| Scene Number | scene_number | scene_number |  | Number | 
| Duration of light after motion detection [s] | config_decimal | config_decimal |  | Number | 
| Light threshold [lx] | config_decimal | config_decimal |  | Number | 
| Alarm (burglar) | alarm_burglar | alarm_burglar | Door | Switch | 
| Alarm (burglar) 1 | alarm_burglar1 | alarm_burglar | Door | Switch | 
| Sensor (luminance) 2 | sensor_luminance2 | sensor_luminance |  | Number | 
| Switch 3 | switch_binary3 | switch_binary | Switch | Switch | 
| Scene Number 4 | scene_number4 | scene_number |  | Number | 

### Dimmer
The brightness channel allows to control the brightness of a light.
            It is also possible to switch the light on and off.

The ```switch_dimmer``` channel is of type ```switch_dimmer``` and supports the ```Dimmer``` item and is in the ```DimmableLight``` category.

### Sensor (luminance)
Indicates the current light reading.

The ```sensor_luminance``` channel is of type ```sensor_luminance``` and supports the ```Number``` item. This is a read only channel so will only be updated following state changes from the device.

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

### Duration of light after motion detection [s]
Generic class for configuration parameter.

The ```config_decimal``` channel is of type ```config_decimal``` and supports the ```Number``` item.

### Light threshold [lx]
Generic class for configuration parameter.

The ```config_decimal``` channel is of type ```config_decimal``` and supports the ```Number``` item.

### Alarm (burglar)
Indicates if the burglar alarm is triggered.

The ```alarm_burglar``` channel is of type ```alarm_burglar``` and supports the ```Switch``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Alarm (burglar) 1
Indicates if the burglar alarm is triggered.

The ```alarm_burglar1``` channel is of type ```alarm_burglar``` and supports the ```Switch``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Sensor (luminance) 2
Indicates the current light reading.

The ```sensor_luminance2``` channel is of type ```sensor_luminance``` and supports the ```Number``` item. This is a read only channel so will only be updated following state changes from the device.

### Switch 3
Switch the power on and off.

The ```switch_binary3``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Scene Number 4
Triggers when a scene button is pressed.

The ```scene_number4``` channel is of type ```scene_number``` and supports the ```Number``` item.
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



## Device Configuration

The following table provides a summary of the 16 configuration parameters available in the MotionSwitch LED.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | TIME | Duration of light after motion detection [s] |
| 2 | LIGHT | Light threshold [lx] |
| 5 | SENSITIVITY | Motion Radar Sensitivity [%] |
| 6 | Brightness measuring interval  | Brightness measuring interval [min] |
| 8 | GLOBAL_LIGHT | Use external Ambient Light Value |
| 9 | SLAVE_MODE | Disable local control |
| 10 | OFF_BEHAVIOUR | Off behaviour ( timeout ) |
| 11 | ON_BEHAVIOUR | On behaviour ( timeout ) |
| 12 | ON_TIME_OVER | On behaviour time over ( timeout ) |
| 13 | ON_OFF_BEHAVIOUR | Sequence On-Off behaviour ( timeout ) |
| 14 | OFF_ON_BEHAVIOUR | Sequence Off-On behaviour ( timeout ) |
| 15 | SEQUENCE_TIME | Sequence Off-On behaviour ( timeout ) |
| 16 | MOTION_DISABLE | Motion Off behaviour ( timeout ) |
| 17 | BUTTON_BEHAVIOUR | Toggle button behaviour |
| 18 | BUTTON_SCENES | Toggle button Scene 1-4 |
| 19 | LEGACY_INACTIVITY_TIME ) | Toggle button inactivity time in "legacy" mode ( timeout ) |

### Parameter 1: TIME

Duration of light after motion detection [s]

The following option values may be configured, in addition to values in the range 5 to 900 -:

| Value  | Description |
|--------|-------------|
| 0 | Duration of light after motion detection [s] |

The manufacturer defined default value is ```180```.

This parameter has the configuration ID ```config_1_2``` and is of type ```INTEGER```.


### Parameter 2: LIGHT

Light threshold [lx]
<p&gtLight threshold: 2 - 2000, 0 lx</p&gt <p&gt0 - run Learn ambient light sequence. 2000 - is used as daylight ( always night mode ).</p&gt
The following option values may be configured, in addition to values in the range 0 to 2000 -:

| Value  | Description |
|--------|-------------|
| 0 | Light threshold [lx] |

The manufacturer defined default value is ```2000```.

This parameter has the configuration ID ```config_2_2``` and is of type ```INTEGER```.


### Parameter 5: SENSITIVITY

Motion Radar Sensitivity [%]

Values in the range 2 to 100 may be set.

The manufacturer defined default value is ```100```.

This parameter has the configuration ID ```config_5_1``` and is of type ```INTEGER```.


### Parameter 6: Brightness measuring interval 

Brightness measuring interval [min]
<p&gtInterval for measuring ambient light when lamp is on ( lamp switches off briefly and measures ): 5 - 120 min. 0 = function is off.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 120 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_6_1``` and is of type ```INTEGER```.


### Parameter 8: GLOBAL_LIGHT

Use external Ambient Light Value
<p&gtWhen GLOBAL_LIGHT mode is ON - device overrides its own light sensor values and uses Light report values from any Z-Wave light sensor instead - this has to be configured appropriately to send light automatically. If the last remote light level value is older than 30 minutes, the internal light value is used again until the next external value is received.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_8_1``` and is of type ```INTEGER```.


### Parameter 9: SLAVE_MODE

Disable local control
<p&gtBit Field:</p&gt <table&gt<tr&gt<td&gtBit</td&gt <td&gt7</td&gt <td&gt6</td&gt <td&gt5</td&gt <td&gt4</td&gt <td&gt3</td&gt <td&gt2</td&gt <td&gt1</td&gt <td&gt0</td&gt </tr&gt<tr&gt<td&gtFunction</td&gt <td&gt-</td&gt <td&gt-</td&gt <td&gt-</td&gt <td&gt-</td&gt <td&gt-</td&gt <td&gt"Stupid" mode</td&gt <td&gtCentral unit<br /&gtchecking in<br /&gtslave mode</td&gt <td&gtSlave mode</td&gt </tr&gt</table&gt<p&gt<br /&gt<strong&gt"Stupid" mode ( bit 2 = 1 ):</strong&gt<br /&gt- has higher priority then slave mode.<br /&gt- LED is permanently on ( for simple power wall switch controlling ).</p&gt <p&gt<br /&gt<strong&gtSlave mode ( bit 0 = 1 ):</strong&gt<br /&gt- only if included in Z-Wave network<br /&gt- useful for controlling via third-party sensor<br /&gt- LED is directly controlled via Z-Wave, internal sensors are not used for controlling it</p&gt <p&gt<br /&gt<strong&gtCentral unit checking ( bit 1 = 1 ):</strong&gt ( useful especially for controlling via gateway )<br /&gtWhen slave bit is 0:<br /&gt- device signalises fail of lifeline connection ( if this bit is zero, fail of lifeline connection<br /&gtis not signalised )<br /&gtWhen slave bit is 1:<br /&gt- device checks presence of Z-Wave device in lifeline group ( gateway ). If it is not<br /&gtpresent for 2 minutes ( testing repeatedly every 30 seconds ) device switches<br /&gtto normal mode in the same way as after the end of local disabled mode<br /&gt( ON_BEHAVIOUR )<br /&gt- the device checks every 1 minute for recovery of Lifeline connection.<br /&gt- if no lifeline specified - it works in normal mode</p&gt <p&gt<br /&gt<strong&gtBe careful with this option</strong&gt, device stops using its own motion sensor in Slave<br /&gtand Stupid mode.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 4 may be set.

The manufacturer defined default value is ```2```.

This parameter has the configuration ID ```config_9_1``` and is of type ```INTEGER```.


### Parameter 10: OFF_BEHAVIOUR

Off behaviour ( timeout )
<p&gtBehaviour after BASIC OFF ( and similar commands ).<br /&gtIf a transition ( even with zero change ) with a non-default duration is to be processed,<br /&gtthe transition cannot be interrupted by any motion event in any case.</p&gt <p&gt0 LED is switched off and remains so until any new motion event ( local or remote ) is received.<br /&gt1 - 209 LED is switched off and remains so until after a specified timeout once a new motion event ( local or remote ) is received.<br /&gtTimeout:<br /&gt1..100 - 1 second ( 1 ) to 100 seconds ( 100 ) in 1-second resolution<br /&gt101..200 - 1 minute ( 101 ) to 100 minutes ( 200 ) 1-minute resolution<br /&gt201..209 - 1 hour ( 201 ) to 9 hours ( 209 ) in 1-hour resolution<br /&gt210 - 254 Reserved<br /&gt255 LED is switched off for TIME ( cfg 1 ). It does not wait for a motion event and works normally via current motion evaluation.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 209 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_10_2``` and is of type ```INTEGER```.


### Parameter 11: ON_BEHAVIOUR

On behaviour ( timeout )
<p&gtBehaviour after BASIC ON ( and similar commands ).<br /&gtIf a transition ( even with zero change ) with a non-default duration is to be processed, the transition cannot be interrupted by any motion event in any case.</p&gt <p&gt0 LED is switched on and remains so until any new motion event ( local or remote ) is received. It then works normally via current motion evaluation.<br /&gtNotice - during the day, this mode cannot be ended remotely due to motion events not being transmitted - only via local motion sensor if enabled.<br /&gt1 - 209 LED is switched off and remains so until after a specified timeout<br /&gtonce a new motion event ( local or remote ) is received.<br /&gtTimeout:<br /&gt1..100 - 1 second ( 1 ) to 100 seconds ( 100 ) in 1-second resolution<br /&gt101..200 - 1 minute ( 101 ) to 100 minutes ( 200 ) 1-minute resolution<br /&gt201..209 - 1 hour ( 201 ) to 9 hours ( 209 ) in 1-hour resolution<br /&gtNotice - during the day, this mode cannot be ended remotely due to motion<br /&gtevents not being transmitted - only via local motion sensor if enabled.<br /&gt210 - 254 Reserved<br /&gt255 LED is switched off for TIME ( cfg 1 ). It does not wait for a motion<br /&gtevent and works normally via current motion evaluation.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```255```.

This parameter has the configuration ID ```config_11_2``` and is of type ```INTEGER```.


### Parameter 12: ON_TIME_OVER

On behaviour time over ( timeout )
<p&gtTime limit to stop waiting for motion after timeout of ON\_BEHAVIOUR or OFF\_ON_BEHAVIOUR ( 0-209 ) to prevent staying ON forever when there is no motion.</p&gt <p&gt0 No additional waiting for motion.<br /&gt1..100 - 1 second ( 1 ) to 100 seconds ( 100 ) in 1-second resolution<br /&gt101..200 - 1 minute ( 101 ) to 100 minutes ( 200 ) in 1-minute resolution<br /&gt201..209 - 1 hour ( 201 ) to 9 hours ( 209 ) in 1-hour resolution<br /&gt210 - 254 Reserved<br /&gt255 Never stop waiting for motion.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 209 may be set.

The manufacturer defined default value is ```204```.

This parameter has the configuration ID ```config_12_2``` and is of type ```INTEGER```.


### Parameter 13: ON_OFF_BEHAVIOUR

Sequence On-Off behaviour ( timeout )
<p&gtBehaviour after a rapid sequence of BASIC ON and BASIC OFF commands.<br /&gtThe intention is to use a much longer timeout value than the time after a single ON command which should then be followed by a short timeout value.<br /&gtThe behaviour is the same as for parameter 10 ( OFF\_LOCAL\_DISABLE ) except: 255 - device ignores ON - OFF sequence and uses OFF behaviour</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 209 may be set.

The manufacturer defined default value is ```204```.

This parameter has the configuration ID ```config_13_2``` and is of type ```INTEGER```.


### Parameter 14: OFF_ON_BEHAVIOUR

Sequence Off-On behaviour ( timeout )
<p&gtBehaviour after a rapid sequence of BASIC OFF and BASIC ON commands.<br /&gtThe intention is to use a much longer timeout value than the time after a single OFF command which should then be followed by a short timeout value.<br /&gtThe behaviour is the same as for parameter 11 ( ON\_LOCAL\_DISABLE ) except: 255 - device ignores OFF - ON sequence and uses ON behaviour.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 209 may be set.

The manufacturer defined default value is ```204```.

This parameter has the configuration ID ```config_14_2``` and is of type ```INTEGER```.


### Parameter 15: SEQUENCE_TIME

Sequence Off-On behaviour ( timeout )
<p&gtTime in [100 milliseconds] of maximum delay between BASIC ON and BASIC OFF ( and vice versa ) to consider this as a sequence. It is typically 1 second, but can be exceptionally longer due to retransmissions and overload - in this case, a longer interval can be allowed ( up to 5 seconds ).</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 10 to 50 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_15_1``` and is of type ```INTEGER```.


### Parameter 16: MOTION_DISABLE

Motion Off behaviour ( timeout )
<p&gtMotion disable timeout after BASIC SET to motion endpoint when the internal motion sensor is not used for evaluating the behaviour of the LED and groups 2 and 3. Events are, however, still transmitted to the Lifeline, and the device can be controlled via remote motion sensors.</p&gt <p&gt<br /&gt0 BASIC SET to Motion sensor endpoint ignored, BASIC to root is mapped to LED endpoint, motion sensor still enabled<br /&gt1 - 209 Internal motion sensor is disabled for specified timeout after BASIC SET 0x00 to Motion sensor endpoint. BASIC to root is mapped to Motion sensor endpoint. ( SPIR )<br /&gtTimeout:<br /&gt1..100 - 1 second ( 1 ) to 100 seconds ( 100 ) in 1-second resolution<br /&gt101..200 - 1 minute ( 101 ) to 100 minutes ( 200 ) in 1-minute resolution<br /&gt201..209 - 1 hour ( 201 ) to 9 hours ( 209 ) in 1-hour resolution<br /&gt210 - 254 Reserved<br /&gt255 BASIC SET to Motion sensor endpoint ignored, BASIC to root is mapped to LED endpoint, motion sensor still disabled</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 209 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_16_2``` and is of type ```INTEGER```.


### Parameter 17: BUTTON_BEHAVIOUR

Toggle button behaviour
<p&gtBit Field:</p&gt <table&gt<tr&gt<td&gtBit</td&gt <td&gt7</td&gt <td&gt6</td&gt <td&gt5</td&gt <td&gt4</td&gt <td&gt3</td&gt <td&gt2</td&gt <td&gt1</td&gt <td&gt0</td&gt </tr&gt<tr&gt<td&gtFunction</td&gt <td&gt-</td&gt <td&gt-</td&gt <td&gt-</td&gt <td&gtDim<br /&gtdisable</td&gt <td&gtNetwork<br /&gtbehaviour</td&gt <td&gtStandalone<br /&gtbehaviour</td&gt <td&gtScene<br /&gtswitch</td&gt <td&gtDim<br /&gtincrease</td&gt </tr&gt</table&gt<p&gt<br /&gt<strong&gtDim increase ( bit 0 ):</strong&gt<br /&gt- long press of button does dim decreasing ( 0 = default )<br /&gt- long press of button does dim increasing ( 1 )</p&gt <p&gt<strong&gtScene switch ( bit 1 ):</strong&gt<br /&gt- short press of button does toggle - on/off ( 0 = default )<br /&gt- short press of button does scene switch ( 1 )</p&gt <p&gt<strong&gtStandalone behaviour ( bit 2 ):</strong&gt<br /&gt- using "legacy" sensor deactivity after button press ( 0 = default )<br /&gt- using "cfg 10-14" sensor deactivity after button press ( 1 )</p&gt <p&gt<strong&gtNetwork behaviour ( bit 3 ):</strong&gt<br /&gt- using "legacy" sensor deactivity after button press ( 0 )<br /&gt- using "cfg 10-14" sensor deactivity after button press ( 1 = default )</p&gt <p&gt<strong&gtDim disable ( bit 4 ):</strong&gt<br /&gt- dim events ( long press ) are used ( 0 = default )<br /&gt- dim events ( long press ) are ignored ( 1 )</p&gt <p&gt"legacy" sensor deactivity behaviour does this:<br /&gt- Pressing push button when LED is OFF: LED will switch on as long as there is<br /&gtmovement and the delay time runs of<br /&gt- Pressing push button when LED is ON: LED will switch off as long as there is<br /&gtmovement ans the delay time runs of ( invers logic ).</p&gt <p&gt"cfg 10-14" sensor deactivity behaviour uses behaviour specified in parameters<br /&gt10-14 ( the same behaviour as pressing of external switch using BASIC ON/OFF<br /&gtfunctions )</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 31 may be set.

The manufacturer defined default value is ```8```.

This parameter has the configuration ID ```config_17_1``` and is of type ```INTEGER```.


### Parameter 18: BUTTON_SCENES

Toggle button Scene 1-4
<p&gtParameters 18 specifies scene numbers ( from 1 to number ) to be switched by<br /&gttoggle button. 0 menas - do not useany.</p&gt
Values in the range 0 to 127 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_18_1``` and is of type ```INTEGER```.


### Parameter 19: LEGACY_INACTIVITY_TIME )

Toggle button inactivity time in "legacy" mode ( timeout )
<p&gtIn "legacy" button mode function this timeout is used to cancel motion sensor inactivity mode after motion end ( both for on and off state of LED ).</p&gt <p&gtTimeout:<br /&gt1..100 - 1 second ( 1 ) to 100 seconds ( 100 ) in 1-second resolution<br /&gt101..200 - 1 minute ( 101 ) to 100 minutes ( 200 ) in 1-minute resolution<br /&gt201..209 - 1 hour ( 201 ) to 9 hours ( 209 ) in 1-hour resolution</p&gt
Values in the range 1 to 209 may be set.

The manufacturer defined default value is ```103```.

This parameter has the configuration ID ```config_19_2``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The MotionSwitch LED supports 8 association groups.

### Group 0: ROOT DEVICE

Root

Association group 0 supports 1 node.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
Lifeline
<p&gtLifeline messages<br /&gt- Device Reset Locally<br /&gt- Notifications:<br /&gt0x07 ( Home security ) - Motion Begin event ( 0x08 )<br /&gt0x07 ( Home security ) - Motion End event ( 0x00, 0x08 )<br /&gt- Binary Switch Report - LED state<br /&gt- Multilevel Sensor Report - value of internal ambient light sensor<br /&gt- Central scene notification - button events</p&gt <p&gtMotion Begin and Motion End events are sent along with frames to group 3. If multichannel<br /&gtassociation is created the events are sent from motion sensor endpoint.<br /&gtSwitch Report is sent immediately upon a change of status along with frames<br /&gtto group 2. If multichannel association is created the events are sent from LED<br /&gtendpoint.<br /&gtAll notifications to lifeline are sent as sensor states regardless of sensor settings<br /&gtand states as SLAVE\_MODE, LOCAL\_DISABLED and MOTION_ENABLE.<br /&gtMultilevel Sensor Report is sent a maximum of once per 1 minute ( if the value has<br /&gtchanged by at least 3% ) and a minimum of once per 15 minutes ( if the value has<br /&gtnot changed ). If the ambient light value is old ( cannot be measured because of<br /&gtpermanent light ), the value is not transmitted via lifeline. Multilevel Sensor Report<br /&gtcan also be added to some other events to send in bulk. If multichannel association<br /&gtis created the events are sent from light sensor endpoint.<br /&gtCentral scene notification is send as reaction to user interaction. If multichannel<br /&gtassociation is created the events are send from toogle button endpoint.</p&gt

Association group 1 supports 1 node.

### Group 2: On/Off control

On/Off control
<p&gtGroup 2 is used for directly controlling Z-Wave devices via BASIC SET commands<br /&gtthrough the evaluation of movement and light, as with internal use ( so that all of<br /&gtthese devices work together ). This is intended for use especially with third-party<br /&gtdevices that do not implement reactions for motion events. BASIC\_SET and<br /&gtsimilar Z-Wave commands are not retransmitted intentionally to slaves and must<br /&gtbe sent to slave devices via the controlling device simultaneously. Only for use in<br /&gtmaster-slave system, multi-device control is not possible.<br /&gtGroup 2 is evaluated and frames are transmitted there also in SLAVE\_MODE, regardless<br /&gtof LOCAL\_DISABLED state and when MOTION\_ENABLE is off ( not using<br /&gtinternal motion sensor, just reacting to remote motion events in this case ).<br /&gtIf multichannel association is created the events are send from motion sensor<br /&gtendpoint.</p&gt

Association group 2 supports 16 nodes.

### Group 3: Notification Report

Notification Report
<p&gtGroup 3 sends MOTION\_BEGIN and MOTION\_END frames.<br /&gtMOTION\_BEGIN frame = Notification 0x07 ( Home security ) - Motion detection without location ( 0x08 )<br /&gtMOTION\_END frame = Notification 0x07 ( Home security ) - Event inactive ( 0x00, parameter 0x08 )<br /&gtAfter the first motion detection, MOTION\_BEGIN is sent. If continual movement is detected, MOTION\_BEGIN is sent every 1 minute repeatedly. When motion ends, MOTION\_END is sent 5 seconds after the last motion detection.<br /&gtNotification to group 3 is sent only when NIGHT\_MODE = ON and MOTION\_ENABLE = ON, regardless of LOCAL\_DISABLE state.<br /&gtAll devices in a group should have the same TIME settings in order that they switch<br /&gtoff at the same time.<br /&gtIf multichannel association is created the events are send from motion sensor<br /&gtendpoint.</p&gt

Association group 3 supports 16 nodes.

### Group 4: Ambient light

Ambient light
<p&gtAmbient Light via Group 4 is intended to substitute locally measured LUX values in target devices - so that the network can have one source of ambient light value.<br /&gtFrames are sent a maximum of once per 2.5 minutes and a minimum of once per 15 minutes.<br /&gtWhen device already uses remote Ambient light value, then this value is also retransmitted to group 4.<br /&gtAll devices in such a group should have the same LIGHT ( threshold ) settings in order that night mode is detected at the same time.<br /&gtIf multichannel association is created the events are send from light sensor endpoint.</p&gt

Association group 4 supports 6 nodes.

### Group 5: Button on/off

Button on/off
<p&gtGroup 5 is used for directly controlling Z-Wave devices via BASIC SET commands by button. This function is allowed by default in cfg 17 ( BUTTON_BEHAVIOUR ).<br /&gtIf multichannel association is created the events are sent from toggle button endpoint.</p&gt

Association group 5 supports 16 nodes.

### Group 6: Button scene

Button scene
<p&gtGroup 6 is used for scene activation - switches from scene 1 to scene specified in cfg 18 ( BUTTON\_SCENES ) - increased by 1 - this function must be allowed in cfg 17 ( BUTTON\_BEHAVIOUR ). If multichannel association is created the events are sent from toggle button endpoint.</p&gt

Association group 6 supports 16 nodes.

### Group 7: Button dim

Button dim
<p&gtGroup 7 is used for dimming ( use just for multilevel lamps ). This function is allowed by default in cfg 17 ( BUTTON_BEHAVIOUR ).<br /&gtIf multichannel association is created the events are sent from toggle button endpoint.</p&gt

Association group 7 supports 16 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_APPLICATION_STATUS_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V1| Linked to BASIC|
| COMMAND_CLASS_SCENE_ACTIVATION_V1| |
| COMMAND_CLASS_SENSOR_MULTILEVEL_V4| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_CENTRAL_SCENE_V3| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V4| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 1

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ALARM_V4| Linked to BASIC|
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 2

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SENSOR_MULTILEVEL_V4| Linked to BASIC|
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 3

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_APPLICATION_STATUS_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| Linked to BASIC|
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 4

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_CENTRAL_SCENE_V3| Linked to BASIC|
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Motion_Switch_LED-180718.pdf](https://opensmarthouse.org/zwavedatabase/1060/Motion-Switch-LED-180718.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1060).
