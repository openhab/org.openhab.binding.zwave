---
layout: documentation
title: Wave Pro Dimmer 1PM - ZWave
---

{% include base.html %}

# Wave Pro Dimmer 1PM The Device is a DIN-rail mountable, one-channel smart dimmer. It can work as a standalone or it can also be operated through Z-WaveÂ® home automation.
This describes the Z-Wave device *Wave Pro Dimmer 1PM*, manufactured by *Shelly* with the thing type UID of ```shelly_waveprodim1pm_00_000```.

The device is in the category of *Light Bulb*, defining Devices that illuminate something, such as bulbs, etc..

![Wave Pro Dimmer 1PM product image](https://opensmarthouse.org/zwavedatabase/1702/image/)


The Wave Pro Dimmer 1PM supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

The Device is a DIN-rail mountable, one-channel smart dimmer. It can work as a standalone or it can also be operated through Z-Wave® home automation. The Device can be accessed, controlled, and monitored remotely from any place where the User has internet connectivity, provided that the module is connected to a Z-Wave Gateway with internet access. It is compatible with switches and push-buttons (default).

The Device is a DIN-rail mountable, one-channel smart dimmer. It can work as a standalone or it can also be operated through Z-Wave® home automation. The Device can be accessed, controlled, and monitored remotely from any place where the User has internet connectivity, provided that the module is connected to a Z-Wave Gateway with internet access. It is compatible with switches and push-buttons (default).

The Device is a DIN rail mountable smart switch with power measurement. It controls the on/off function for one electrical device with a load up to 16 A. It is compatible with switches (default) and push-buttons.

Switch connected to input terminal SW (SW1)

If the SW (SW1) is configured as a switch (default), each toggle of the switch will change the output O (O1) state to the opposite state - on, off, on, etc.

Change switch position once: Change the state of the output O (O1) state to the opposite state and send the command to the associated devices in associated groups 2 and 3 (check chapter Z-Wave Association).

Change switch position twice: If the delay between the first in the second click is less then 500ms, this is interpreted as “Change the switch possition twice”. Send command to the associated devices (dimmers, shutters,….) in associated groups 2 and 3 (check chapter Z-Wave Association).

Switch-memory connected to input terminal SW (SW1)

If the SW (SW1) is configured as a switch-memory, than:

Switching to Close switch-memory contact: Change the state of the output state O (O1) to the On state and send command to the associated devices in associated groups 2 and 3 (check chapter Z-Wave Association)

Switching to Open switch-memory contact: Change the state of the output state O (O1) to the Off state and send command to the associated devices in associated groups 2 and 3 (check chapter Z-Wave Association)

Push-button connected to input terminal SW (SW1)

If the SW (SW1) is configured as a push-button in the Device settings, each press of the push-button changes the output state O (O1) to opposite - ON, OFF, ON, etc.

Short press: Change the state of the output state O (O1) to the opposite one and send command to the associated devices in associated groups 2 and 3 (check chapter Z-Wave Association)

Hold: Send command to the associated devices in associated group 3 (check chapter Z-Wave Association)

Release: Send command to the associated devices in associated group 3 (check chapter Z-Wave Association)

Switching On/Off load connected to O (O1)

Load connected to O (O1) is possible to switch On/Off by:

by Z-Wave command

Automatically switching can be enabled by proper Parameters No. 19 and 20 settings.

pressing the switch/push-button SW (SW1): Change the state of the connected load to the opposite one

### Inclusion Information

SmartStart adding (inclusion)

SmartStart enabled products can be added into a Z-Wave™ network by scanning the Z-Wave™ QR Code present on the Device with a gateway providing SmartStart inclusion. No further action is required, and the SmartStart device will be added automatically within 10 minutes of being switched on in the network vicinity.

With the gateway application scan the QR code on the Device label and add the Security 2 (S2) Device Specific Key (DSK) to the provisioning list in the gateway.

Connect the Device to a power supply.

Check if the blue LED is blinking slowly. If so, the Device is not added to a Z-Wave™ network.

Adding will be initiated automatically within a few seconds after connecting the Device to a power supply, and the Device will be added to a Z-Wave™ network automatically.

The blue LED will be blinking faster during the adding process.

The green LED will be blinking in slowly if the Device is successfully added to a Z-Wave™ network.

Adding (inclusion) with the S button

Connect the Device to a power supply.

Check if the blue LED is blinking slowly. If so, the Device is not added to a Z-Wave™ network.

Enable add/remove mode on the gateway.

To enter the Setting mode, quickly press and hold the S button on the Device until the LED turns solid blue.

Quickly release and then press and hold (> 2s) the S button on the Device until the blue LED starts blinking slowly. Releasing the S button will start the Learn mode.

The blue LED will be blinking faster during the adding process.

The green LED will be blinking slowly if the Device is successfully added to a Z-Wave™ network.

Note! In Setting mode, the Device has a timeout of 10s before entering again into Normal mode.

Adding (inclusion) with a switch/push-button

Connect the Device to a power supply.

Check if the blue LED is blinking slowly. If so, the Device is not added to a Z-Wave™ network.

Enable add/remove mode on the gateway.

Toggle the switch/push-button connected to any of the SW terminals (SW, SW1, SW2, etc.) 3 times within 3 seconds (this procedure puts the Device in Learn mode*). The Device must receive on/off signal 3 times, which means pressing the momentary switch 3 times, or toggling the switch on and off 3 times.

The blue LED will be blinking faster during the adding process.

The green LED will be blinking slowly if the Device is successfully added to a Z-Wave™ network.

*Learn mode - a state that allows the Device to receive network information from the gateway

SmartStart adding (inclusion)

SmartStart enabled products can be added into a Z-Wave™ network by scanning the Z-Wave™ QR Code present on the Device with a gateway providing SmartStart inclusion. No further action is required, and the SmartStart device will be added automatically within 10 minutes of being switched on in the network vicinity.

With the gateway application scan the QR code on the Device label and add the Security 2 (S2) Device Specific Key (DSK) to the provisioning list in the gateway.

Connect the Device to a power supply.

Check if the blue LED is blinking slowly. If so, the Device is not added to a Z-Wave™ network.

Adding will be initiated automatically within a few seconds after connecting the Device to a power supply, and the Device will be added to a Z-Wave™ network automatically.

The blue LED will be blinking faster during the adding process.

The green LED will be blinking in slowly if the Device is successfully added to a Z-Wave™ network.

Adding (inclusion) with the S button

Connect the Device to a power supply.

Check if the blue LED is blinking slowly. If so, the Device is not added to a Z-Wave™ network.

Enable add/remove mode on the gateway.

To enter the Setting mode, quickly press and hold the S button on the Device until the LED turns solid blue.

Quickly release and then press and hold (> 2s) the S button on the Device until the blue LED starts blinking slowly. Releasing the S button will start the Learn mode.

The blue LED will be blinking faster during the adding process.

The green LED will be blinking slowly if the Device is successfully added to a Z-Wave™ network.

Note! In Setting mode, the Device has a timeout of 10s before entering again into Normal mode.

Adding (inclusion) with a switch/push-button

Connect the Device to a power supply.

Check if the blue LED is blinking slowly. If so, the Device is not added to a Z-Wave™ network.

Enable add/remove mode on the gateway.

Toggle the switch/push-button connected to any of the SW terminals (SW, SW1, SW2, etc.) 3 times within 3 seconds (this procedure puts the Device in Learn mode*). The Device must receive on/off signal 3 times, which means pressing the momentary switch 3 times, or toggling the switch on and off 3 times.

The blue LED will be blinking faster during the adding process.

The green LED will be blinking slowly if the Device is successfully added to a Z-Wave™ network.

*Learn mode - a state that allows the Device to receive network information from the gateway

SmartStart adding (inclusion)

SmartStart enabled products can be added into a Z-Wave® network by scanning the Z-Wave® QR Code present on the Device with a gateway providing SmartStart inclusion. No further action is required, and the SmartStart device will be added automatically within 10 minutes of being switched on in the network vicinity.

With the gateway application scan the QR code on the Device label and add the Security 2 (S2) Device Specific Key (DSK) to the provisioning list in the gateway.

Connect the Device to a power supply.

Check if the blue LED is blinking in Mode 1. If so, the Device is not added to a Z-Wave® network.

Adding will be initiated automatically within a few seconds after connecting the Device to a power supply, and the Device will be added to a Z-Wave® network automatically.

The blue LED will be blinking in Mode 2 during the adding process.

The green LED will be blinking in Mode 1 if the Device is successfully added to a Z-Wave® network.

Adding (inclusion) with the S button

Connect the Device to a power supply.

Check if the blue LED is blinking in Mode 1. If so, the Device is not added to a Z-Wave® network.

Enable add/remove mode on the gateway.

To enter the Setting mode, quickly press and hold the S button on the Device until the LED turns solid blue.

Quickly release and then press and hold (> 2s) the S button on the Device until the blue LED starts blinking in Mode 3. Releasing the S button will start the Learn mode.

The blue LED will be blinking in Mode 2 during the adding process.

The green LED will be blinking in Mode 1 if the Device is successfully added to a Z-Wave® network.

Adding (inclusion) with a switch/push-button

Connect the Device to a power supply.

Check if the blue LED is blinking in Mode 1. If so, the Device is not added to a Z-Wave® network.

Enable add/remove mode on the gateway.

Toggle the switch/push-button connected to any of the SW terminals (SW, SW1, SW2, etc.) 3 times within 3 seconds (this procedure puts the Device in Learn mode*). The Device must receive on/off signal 3 times, which means pressing the momentary switch 3 times, or toggling the switch on and off 3 times.

The blue LED will be blinking in Mode 2 during the adding process.

The green LED will be blinking in Mode 1 if the Device is successfully added to a Z-Wave® network.

### Exclusion Information

Removing the Device from a Z-Wave™ network (exclusion)

Note! The Device will be removed from your Z-wave™ network, but any custom configuration parameters will not be erased.

Note! All Device outputs (O, O1, O2, etc. - depending on the Device type) will turn the load 1s on/1s off /1s on/1s off if the Device is successfully added to/removed from a Z-Wave™ network.

Removing (exclusion) with the S button

Connect the Device to a power supply.

Check if the green LED will be blinking slowly. If so, the Device is added to a Z-Wave™ network.

Enable add/remove mode on the gateway.

To enter the Setting mode, quickly press and hold the S button on the Device until the LED turns solid blue.

Quickly release and then press and hold (> 2s) the S button on the Device until the blue LED starts blinking slowly. Releasing the S button will start the LEARN MODE.

The blue LED will be blinking faster during the removing process.

The blue LED will be blinking slower if the Device is successfully removed from a Z-Wave™ network.

Note! In Setting mode, the Device has a timeout of 10s before entering again into Normal mode.

Removing (exclusion) with a switch/push-button

Connect the Device to a power supply.

Check if the green LED will be blinking slowly. If so, the Device is added to a Z-Wave™ network.

Enable add/remove mode on the gateway.

Toggle the switch/push-button connected to any of the SW terminals (SW, SW1, SW2,…) 3 times within 3 seconds (this procedure puts the Device in LEARN MODE). The Device must receive on/off signal 3 times, which means pressing the momentary switch 3 times, or toggling the switch on and off 3 times.

The blue LED will be blinking faster during the removing process.

The blue LED will be blinking slower if the Device is successfully removed from a Z-Wave™ network.

SmartStart adding (inclusion)

SmartStart enabled products can be added into a Z-Wave™ network by scanning the Z-Wave™ QR Code present on the Device with a gateway providing SmartStart inclusion. No further action is required, and the SmartStart device will be added automatically within 10 minutes of being switched on in the network vicinity.

With the gateway application scan the QR code on the Device label and add the Security 2 (S2) Device Specific Key (DSK) to the provisioning list in the gateway.

Connect the Device to a power supply.

Check if the blue LED is blinking slowly. If so, the Device is not added to a Z-Wave™ network.

Adding will be initiated automatically within a few seconds after connecting the Device to a power supply, and the Device will be added to a Z-Wave™ network automatically.

The blue LED will be blinking faster during the adding process.

The green LED will be blinking in slowly if the Device is successfully added to a Z-Wave™ network.

Adding (inclusion) with the S button

Connect the Device to a power supply.

Check if the blue LED is blinking slowly. If so, the Device is not added to a Z-Wave™ network.

Enable add/remove mode on the gateway.

To enter the Setting mode, quickly press and hold the S button on the Device until the LED turns solid blue.

Quickly release and then press and hold (> 2s) the S button on the Device until the blue LED starts blinking slowly. Releasing the S button will start the Learn mode.

The blue LED will be blinking faster during the adding process.

The green LED will be blinking slowly if the Device is successfully added to a Z-Wave™ network.

Note! In Setting mode, the Device has a timeout of 10s before entering again into Normal mode.

Adding (inclusion) with a switch/push-button

Connect the Device to a power supply.

Check if the blue LED is blinking slowly. If so, the Device is not added to a Z-Wave™ network.

Enable add/remove mode on the gateway.

Toggle the switch/push-button connected to any of the SW terminals (SW, SW1, SW2, etc.) 3 times within 3 seconds (this procedure puts the Device in Learn mode*). The Device must receive on/off signal 3 times, which means pressing the momentary switch 3 times, or toggling the switch on and off 3 times.

The blue LED will be blinking faster during the adding process.

The green LED will be blinking slowly if the Device is successfully added to a Z-Wave™ network.

*Learn mode - a state that allows the Device to receive network information from the gateway

SmartStart adding (inclusion)

SmartStart enabled products can be added into a Z-Wave® network by scanning the Z-Wave® QR Code present on the Device with a gateway providing SmartStart inclusion. No further action is required, and the SmartStart device will be added automatically within 10 minutes of being switched on in the network vicinity.

With the gateway application scan the QR code on the Device label and add the Security 2 (S2) Device Specific Key (DSK) to the provisioning list in the gateway.

Connect the Device to a power supply.

Check if the blue LED is blinking in Mode 1. If so, the Device is not added to a Z-Wave® network.

Adding will be initiated automatically within a few seconds after connecting the Device to a power supply, and the Device will be added to a Z-Wave® network automatically.

The blue LED will be blinking in Mode 2 during the adding process.

The green LED will be blinking in Mode 1 if the Device is successfully added to a Z-Wave® network.

Adding (inclusion) with the S button

Connect the Device to a power supply.

Check if the blue LED is blinking in Mode 1. If so, the Device is not added to a Z-Wave® network.

Enable add/remove mode on the gateway.

To enter the Setting mode, quickly press and hold the S button on the Device until the LED turns solid blue.

Quickly release and then press and hold (> 2s) the S button on the Device until the blue LED starts blinking in Mode 3. Releasing the S button will start the Learn mode.

The blue LED will be blinking in Mode 2 during the adding process.

The green LED will be blinking in Mode 1 if the Device is successfully added to a Z-Wave® network.

Adding (inclusion) with a switch/push-button

Connect the Device to a power supply.

Check if the blue LED is blinking in Mode 1. If so, the Device is not added to a Z-Wave® network.

Enable add/remove mode on the gateway.

Toggle the switch/push-button connected to any of the SW terminals (SW, SW1, SW2, etc.) 3 times within 3 seconds (this procedure puts the Device in Learn mode*). The Device must receive on/off signal 3 times, which means pressing the momentary switch 3 times, or toggling the switch on and off 3 times.

The blue LED will be blinking in Mode 2 during the adding process.

The green LED will be blinking in Mode 1 if the Device is successfully added to a Z-Wave® network.

### General Usage Information



## Channels

The following table summarises the channels available for the Wave Pro Dimmer 1PM -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Dimmer | switch_dimmer | switch_dimmer | DimmableLight | Dimmer | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Scene Number | scene_number | scene_number |  | Number | 
| Alarm (heat) | alarm_heat | alarm_heat | temperature_hot | Switch | 
| Alarm (power) | alarm_power | alarm_power | Energy | Switch | 
| Dimmer 1 | switch_dimmer1 | switch_dimmer | DimmableLight | Dimmer | 
| Electric meter (kWh) 1 | meter_kwh1 | meter_kwh | Energy | Number | 
| Electric meter (watts) 1 | meter_watts1 | meter_watts | Energy | Number | 
| Alarm (heat) 1 | alarm_heat1 | alarm_heat | temperature_hot | Switch | 
| Alarm (power) 1 | alarm_power1 | alarm_power | Energy | Switch | 
| Switch 2 | switch_binary2 | switch_binary | Switch | Switch | 
| Electric meter (kWh) 2 | meter_kwh2 | meter_kwh | Energy | Number | 
| Electric meter (watts) 2 | meter_watts2 | meter_watts | Energy | Number | 
| Switch 3 | switch_binary3 | switch_binary | Switch | Switch | 
| Electric meter (kWh) 3 | meter_kwh3 | meter_kwh | Energy | Number | 
| Electric meter (watts) 3 | meter_watts3 | meter_watts | Energy | Number | 

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

### Alarm (heat)
Indicates if a heat alarm is triggered.

The ```alarm_heat``` channel is of type ```alarm_heat``` and supports the ```Switch``` item and is in the ```temperature_hot``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Alarm (power)
Indicates if a power alarm is triggered.

The ```alarm_power``` channel is of type ```alarm_power``` and supports the ```Switch``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Dimmer 1
The brightness channel allows to control the brightness of a light.
            It is also possible to switch the light on and off.

The ```switch_dimmer1``` channel is of type ```switch_dimmer``` and supports the ```Dimmer``` item and is in the ```DimmableLight``` category.

### Electric meter (kWh) 1
Indicates the energy consumption (kWh).

The ```meter_kwh1``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts) 1
Indicates the instantaneous power consumption.

The ```meter_watts1``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Alarm (heat) 1
Indicates if a heat alarm is triggered.

The ```alarm_heat1``` channel is of type ```alarm_heat``` and supports the ```Switch``` item and is in the ```temperature_hot``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Alarm (power) 1
Indicates if a power alarm is triggered.

The ```alarm_power1``` channel is of type ```alarm_power``` and supports the ```Switch``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Switch 2
Switch the power on and off.

The ```switch_binary2``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Electric meter (kWh) 2
Indicates the energy consumption (kWh).

The ```meter_kwh2``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts) 2
Indicates the instantaneous power consumption.

The ```meter_watts2``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Switch 3
Switch the power on and off.

The ```switch_binary3``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Electric meter (kWh) 3
Indicates the energy consumption (kWh).

The ```meter_kwh3``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts) 3
Indicates the instantaneous power consumption.

The ```meter_watts3``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.



## Device Configuration

The following table provides a summary of the 20 configuration parameters available in the Wave Pro Dimmer 1PM.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | SW (SW1) Switch type | This parameter defines how the Device should treat the switch (which type) connected to the SW (SW1) terminal. |
| 2 | SW2 Switch type | This parameter defines how the Device should treat the switch (which type) connected to the SW2 terminal. |
| 7 | SW (SW1) detach mode | In this mode the input SW (SW1) is separated/not changing the state of the output. |
| 8 | SW2 detach mode | In this mode the input SW 2 is separated/not changing the state of the output. |
| 17 | Restore the O (O1) state after a power failure | This parameter determines if the on/off status is saved and restored for the load connected to O (O1) after a power failure. |
| 19 | O (O1) Auto OFF with timer | If the load O (O1) is ON, you can schedule it to turn OFF automatically after the period of time defined in this parameter. The timer resets to zero each time the Device receives an ON command, either remotely (from the gateway or associated device) or lo |
| 20 | O (O1) Auto ON with timer | If the load O (O1) is OFF, you can schedule it to turn ON automatically after the period of time defined in this parameter. The timer resets to zero each time the Device receives an OFF command, either remotely (from the gateway or associated device) or l |
| 25 | Set timer units to s or ms for O (O1) | Set Timer Units to Seconds or Milliseconds Choose if you want to set the timer in seconds or milliseconds in Parameters No. 19, 20. |
| 36 | O (O1) Power report on change - percentage | This parameter determines the minimum change in consumed power that will result in sending a new report to the gateway. |
| 39 | Minimal time between reports (O) O1 | This parameter determines the minimum time that must elapse before a new power report on O (O1) is sent to the gateway. |
| 78 | Forced Dimmer calibration O (O1) | By setting this parameter to value 1 the Device will start executing force calibration procedure for channel O (O1). The parameter also reports the calibration status by sending the get parameter value command |
| 117 | Remote Device reboot | This parameter enable restarting or rebooting the Device without physical intervention. Use this parameter only for troubleshooting scope. After device reboot the parameter value will be set to default. |
| 118 | Enable SW for inclusion | This parameter defines whether the Device should allow exclusion over inputs (SW) when the device is already included. |
| 120 | Factory Reset | Reset to factory default settings and removed from the z-wave network |
| 121 | MAX dimming value O (O1) | The value set in this parameter determines the maximum dimming value (the highest value which can be set on the device, when, for example, dimming lights with wall switch or slider in the GUI (Gateway - hub)). |
| 123 | Min. dimming value dim specific par. O (O1) | The value set in this parameter determines the minimum dimming value (the lowest value which can be set on the device, when, for example, dimming lights with wall switch or slider in the GUI (Gateway - hub)) |
| 125 | Dimming time (soft on/off) dim specific par. O (O1) | (Dimming time short click O (O1))Choose the time during which the device will move between the min. and max. dimming values by a short press of the momentary switch connected to input terminal. |
| 127 | Fade rate O (O1) | (Dimming time when key hold)Choose the time during which the Dimmer will move between the min. and max. dimming values during a continuous press of the momentary switch connected to terminal, by an associated device. |
| 129 | Minimum brightens on toggle O (O1) dim specific par. | Minimum brightness on toggle can be changed by the user to the desired level. It is applied on toggle on (Off → On state change) when the Light brightness < min\_brightness\_on_toggle. |
| 131 |  SW1 & SW2 Dual Button Mode | W1 & SW2 Dual button (only push button) |

### Parameter 1: SW (SW1) Switch type

This parameter defines how the Device should treat the switch (which type) connected to the SW (SW1) terminal.
0 - momentary switch,

1 - toggle switch (contact closed - ON / contact opened - OFF),

2 - toggle switch (device changes status when switch changes status)
The following option values may be configured, in addition to values in the range 0 to 2 -:

| Value  | Description |
|--------|-------------|
| 0 | momentary switch |
| 1 | toggle switch (contact closed - ON / contact opened - OFF) |
| 2 | toggle switch (device changes status when switch changes status) |

The manufacturer defined default value is ```2``` (toggle switch (device changes status when switch changes status)).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: SW2 Switch type

This parameter defines how the Device should treat the switch (which type) connected to the SW2 terminal.

The following option values may be configured, in addition to values in the range 0 to 2 -:

| Value  | Description |
|--------|-------------|
| 0 | momentary switch (push button) |
| 1 | toggle switch (contact closed - ON / contact opened - OFF) |
| 2 | toggle switch (device changes status when switch changes status) |

The manufacturer defined default value is ```0``` (momentary switch (push button)).

This parameter has the configuration ID ```config_2_2``` and is of type ```INTEGER```.


### Parameter 7: SW (SW1) detach mode

In this mode the input SW (SW1) is separated/not changing the state of the output.
0 - normal mode

1 - detached mode
The following option values may be configured, in addition to values in the range 0 to 1 -:

| Value  | Description |
|--------|-------------|
| 0 | Normal mode |
| 1 | Detached mode |

The manufacturer defined default value is ```0``` (Normal mode).

This parameter has the configuration ID ```config_7_1``` and is of type ```INTEGER```.


### Parameter 8: SW2 detach mode

In this mode the input SW 2 is separated/not changing the state of the output.
0 - normal mode

1 - detached mode

If SW2 is set to “normal mode - not detached”, Par. n. 131 must be set to dual mode “1 - Active”)
The following option values may be configured, in addition to values in the range 0 to 1 -:

| Value  | Description |
|--------|-------------|
| 0 | Normal mode |
| 1 | Detached mode |

The manufacturer defined default value is ```0``` ( Normal mode).

This parameter has the configuration ID ```config_8_1``` and is of type ```INTEGER```.


### Parameter 17: Restore the O (O1) state after a power failure

This parameter determines if the on/off status is saved and restored for the load connected to O (O1) after a power failure.
0 - Device saves last on/off status and restores it after a power failure

1 - Device does not save on/off status and does not restore it after a power failure, it remains off

NOTE: This functionality does not apply when Parameter 1 is configured with the value "1 - toggle switch (contact closed - ON / contact opened - OFF)"
The following option values may be configured, in addition to values in the range 0 to 1 -:

| Value  | Description |
|--------|-------------|
| 0 | Device saves last on/off status and restores it after a power failure |
| 1 | NC |

The manufacturer defined default value is ```0``` (Device saves last on/off status and restores it after a power failure).

This parameter has the configuration ID ```config_17_1``` and is of type ```INTEGER```.


### Parameter 19: O (O1) Auto OFF with timer

If the load O (O1) is ON, you can schedule it to turn OFF automatically after the period of time defined in this parameter. The timer resets to zero each time the Device receives an ON command, either remotely (from the gateway or associated device) or lo
0 - Auto OFF Disabled

1 - 32535 = 1 - 32535 seconds or milliseconds – see Parameter no. 25. Set timer units to s or ms for O (O1) resolution 100ms
Values in the range 0 to 32535 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_19_2``` and is of type ```INTEGER```.


### Parameter 20: O (O1) Auto ON with timer

If the load O (O1) is OFF, you can schedule it to turn ON automatically after the period of time defined in this parameter. The timer resets to zero each time the Device receives an OFF command, either remotely (from the gateway or associated device) or l
0 = Auto ON Disabled

1 - 32535 seconds (or milliseconds – see Parameter no. 25. Auto ON tim
Values in the range 0 to 32535 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_20_2``` and is of type ```INTEGER```.


### Parameter 25: Set timer units to s or ms for O (O1)

Set Timer Units to Seconds or Milliseconds Choose if you want to set the timer in seconds or milliseconds in Parameters No. 19, 20.

The following option values may be configured, in addition to values in the range 0 to 1 -:

| Value  | Description |
|--------|-------------|
| 0 | timer set in seconds |
| 1 | timer set in milliseconds |

The manufacturer defined default value is ```0``` (timer set in seconds).

This parameter has the configuration ID ```config_25_1``` and is of type ```INTEGER```.


### Parameter 36: O (O1) Power report on change - percentage

This parameter determines the minimum change in consumed power that will result in sending a new report to the gateway.
:0 - reports are disabled

1-100 (1-100%) - change in power
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```10```.

This parameter has the configuration ID ```config_36_1``` and is of type ```INTEGER```.


### Parameter 39: Minimal time between reports (O) O1

This parameter determines the minimum time that must elapse before a new power report on O (O1) is sent to the gateway.

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | reports are disabled |

The manufacturer defined default value is ```30```.

This parameter has the configuration ID ```config_39_1``` and is of type ```INTEGER```.


### Parameter 78: Forced Dimmer calibration O (O1)

By setting this parameter to value 1 the Device will start executing force calibration procedure for channel O (O1). The parameter also reports the calibration status by sending the get parameter value command
1 - start calibration

2 - device is calibrated (read only)

3 - device is not calibrated (read only)

4 - calibration error (read only)
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 1 | start calibration |
| 2 | device is calibrated (read only) |
| 3 | device is not calibrated (read only) |
| 4 | calibration error (read only) |

The manufacturer defined default value is ```3``` (device is not calibrated (read only)).

This parameter has the configuration ID ```config_78_1``` and is of type ```INTEGER```.


### Parameter 117: Remote Device reboot

This parameter enable restarting or rebooting the Device without physical intervention. Use this parameter only for troubleshooting scope. After device reboot the parameter value will be set to default.
0 - function inactive

1 - Remote device reboot
The following option values may be configured, in addition to values in the range 0 to 1 -:

| Value  | Description |
|--------|-------------|
| 0 | function inactive |
| 1 | Remote device reboot |

The manufacturer defined default value is ```0``` (function inactive).

This parameter has the configuration ID ```config_117_1``` and is of type ```INTEGER```.


### Parameter 118: Enable SW for inclusion

This parameter defines whether the Device should allow exclusion over inputs (SW) when the device is already included.
0 - Don't allow exclusion

1 - Allow exclusion
The following option values may be configured, in addition to values in the range 0 to 1 -:

| Value  | Description |
|--------|-------------|
| 0 | Don't allow exclusion |
| 1 | Allow exclusion |

The manufacturer defined default value is ```0``` (Don't allow exclusion).

This parameter has the configuration ID ```config_118_1``` and is of type ```INTEGER```.


### Parameter 120: Factory Reset

Reset to factory default settings and removed from the z-wave network

The following option values may be configured, in addition to values in the range 0 to 0 -:

| Value  | Description |
|--------|-------------|
| 0 | Don’t do Factory reset |
| 1431655765 | Do the Factory reset (hex 0x55555555) |

The manufacturer defined default value is ```0``` (Don’t do Factory reset).

This parameter has the configuration ID ```config_120_4``` and is of type ```INTEGER```.


### Parameter 121: MAX dimming value O (O1)

The value set in this parameter determines the maximum dimming value (the highest value which can be set on the device, when, for example, dimming lights with wall switch or slider in the GUI (Gateway - hub)).
1 - 99% = 2% - 99%, step is 1%

NOTE: The maximum level may not be lower than the minimum level!

NOTE: The output level is automatically adjusted according to changed value.This is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 1 to 99 may be set.

The manufacturer defined default value is ```99```.

This parameter has the configuration ID ```config_121_1``` and is of type ```INTEGER```.


### Parameter 123: Min. dimming value dim specific par. O (O1)

The value set in this parameter determines the minimum dimming value (the lowest value which can be set on the device, when, for example, dimming lights with wall switch or slider in the GUI (Gateway - hub))
0 - 98 = 1 % - 98 %, step is 1 %. 

Minimum dimming value is set by entering a value.

NOTE: The maximum level may not be higher than the minimum level!

NOTE: The output level is automatically adjusted according to changed value.
Values in the range 0 to 98 may be set.

The manufacturer defined default value is ```15```.

This parameter has the configuration ID ```config_123_1``` and is of type ```INTEGER```.


### Parameter 125: Dimming time (soft on/off) dim specific par. O (O1)

(Dimming time short click O (O1))Choose the time during which the device will move between the min. and max. dimming values by a short press of the momentary switch connected to input terminal.
1 - 10 = 1 seconds - 10 seconds, step is 1 second
Values in the range 0 to 10 may be set.

The manufacturer defined default value is ```3```.

This parameter has the configuration ID ```config_125_1``` and is of type ```INTEGER```.


### Parameter 127: Fade rate O (O1)

(Dimming time when key hold)Choose the time during which the Dimmer will move between the min. and max. dimming values during a continuous press of the momentary switch connected to terminal, by an associated device.
0 = non-dimmable load

1 = 5% / sec

2 = 7% / sec

3 = 10% /sec (default)

4 = 15% / sec

5 = 20% / sec
Values in the range 0 to 20 may be set.

The manufacturer defined default value is ```3```.

This parameter has the configuration ID ```config_127_1``` and is of type ```INTEGER```.


### Parameter 129: Minimum brightens on toggle O (O1) dim specific par.

Minimum brightness on toggle can be changed by the user to the desired level. It is applied on toggle on (Off → On state change) when the Light brightness < min\_brightness\_on_toggle.
1 - 99 = 2 % - 99 %, 

step is 1 %. 
Values in the range 1 to 99 may be set.

The manufacturer defined default value is ```15```.

This parameter has the configuration ID ```config_129_1``` and is of type ```INTEGER```.


### Parameter 131:  SW1 & SW2 Dual Button Mode

W1 & SW2 Dual button (only push button)
0 - Inactive

1 - Active
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_131_1``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The device does not support associations.
## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V3| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_TRANSPORT_SERVICE_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_CENTRAL_SCENE_V3| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_SUPERVISION_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V8| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_INDICATOR_V3| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
| COMMAND_CLASS_SECURITY_2_V1| |
#### Endpoint 1

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
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
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 3

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Manual](https://opensmarthouse.org/zwavedatabase/1702/reference/Shelly_Wave_Pro_Dimmer_1PM_____.pdf.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1702).
