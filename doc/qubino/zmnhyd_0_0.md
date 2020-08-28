---
layout: documentation
title: ZMNHYD - ZWave
---

{% include base.html %}

# ZMNHYD Smart Plug
This describes the Z-Wave device *ZMNHYD*, manufactured by *[Goap](http://www.qubino.com/)* with the thing type UID of ```qubino_zmnhyd_00_000```.

The device is in the category of *Power Outlet*, defining Small devices to be plugged into a power socket in a wall which stick there.

![ZMNHYD product image](https://opensmarthouse.org/zwavedatabase/822/image/)


The ZMNHYD supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtThis Z-Wave module is used for switching and energy measurements in single-phase electrical power networks and can be used in residential, industrial and utility applications.</p&gt <ul&gt<li&gtPower supply: 230V +/- 10%, 50 Hz</li&gt <li&gtPower load: 15A resistive max.</li&gt <li&gtOverload protection >16 A</li&gt <li&gtPower consumption <1W</li&gt </ul&gt

### Inclusion Information

<p&gtAuto inclusion (first time usage)</p&gt <ul&gt<li&gtstart inclusion mode of z-wave controller</li&gt <li&gtplug device into power outlet (5 seconds auto inclusion)</li&gt </ul&gt<p&gtManual inclusion</p&gt <ul&gt<li&gtplug device into power outlet</li&gt <li&gtstart inclusion mode of z-wave controller</li&gt <li&gtpress the service button (S) 3 times within 3 seconds</li&gt </ul&gt

### Exclusion Information

<ul&gt<li&gtplug device into power outlet</li&gt <li&gtensure device is within maximum 1 meter (3 feet) of the main controller</li&gt <li&gtenable exclusion mode on main controller</li&gt <li&gtpress service button (S) on module 3 times within 3 seconds (please note: configuration parameters will not be reset!)</li&gt </ul&gt

## Channels

The following table summarises the channels available for the ZMNHYD -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 
| Electric meter (volts) | meter_voltage | meter_voltage | Energy | Number | 
| Electric meter (amps) | meter_current | meter_current | Energy | Number | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Alarm (power) | alarm_power | alarm_power | Energy | Switch | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Electric meter (volts)
Indicates the instantaneous voltage.

The ```meter_voltage``` channel is of type ```meter_voltage``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (amps)
Indicates the instantaneous current consumption.

The ```meter_current``` channel is of type ```meter_current``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

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

The following table provides a summary of the 11 configuration parameters available in the ZMNHYD.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 10 | Activate / deactivate functions ALL ON / ALL OFF |  |
| 11 | Automatic turning OFF relay after set time | When the relay is turned ON, it automatically turns OFF after the defined time |
| 12 | On automatically with timer | Turn Smart plug On Automatically with Timer |
| 15 | Timer Settings Unit | Set Timer Units to Seconds or Milliseconds |
| 30 | Restore state ofter power failure | Restore on/off status for Smart plug 16A after power failure |
| 40 | Treshold Change in Power  for reporting | Change of power consumption [Watt] reporting threshold |
| 41 | Threshold time for power reporting | Threshold time for power reporting [Seconds] |
| 42 | Power Consumption Reporting Time Threshold | Power Consumption Reporting Time Threshold [Seconds] |
| 50 | Down value | Down value [watt] |
| 51 | Up value | Upper power threshold used in parameter no. 52 |
| 52 | Action in case of exceeding defined power values | Action in case of exceeding defined power values (parameters 50 and 51) |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 10: Activate / deactivate functions ALL ON / ALL OFF



The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | ALL ON disabled, ALL of disabled |
| 1 | ALL ON disabled, AL OFF active |
| 2 | ALL ON active, ALL OFF disabled |
| 255 | ALL ON active, ALL OFF active |

The manufacturer defined default value is ```255``` (ALL ON active, ALL OFF active).

This parameter has the configuration ID ```config_10_2``` and is of type ```INTEGER```.


### Parameter 11: Automatic turning OFF relay after set time

When the relay is turned ON, it automatically turns OFF after the defined time
<p&gtThe timer is reset each time, the module receives an ON command (from push button/main controller/association).</p&gt <ul&gt<li&gt0 = Auto OFF disabled</li&gt <li&gt1 - 32535 = 1 second to 32535 seconds delay. Auto OFF is enabled with defined time. Step is 1s.</li&gt </ul&gt
The following option values may be configured, in addition to values in the range 0 to 32535 -:

| Value  | Description |
|--------|-------------|
| 0 | Auto OFF disabled |

The manufacturer defined default value is ```0``` (Auto OFF disabled).

This parameter has the configuration ID ```config_11_2``` and is of type ```INTEGER```.


### Parameter 12: On automatically with timer

Turn Smart plug On Automatically with Timer
<p&gtIf Smart plug 16A is OFF, you can schedule it to turn ON automatically after a period of time defined in this parameter. The timer is reset to zero each time the device receives an OFF command, either remotely (from the gateway (hub) or associated device) or locally from the switch.</p&gt <ul&gt<li&gt0 - Auto ON Disabled</li&gt <li&gt1 - 32535 = 1 - 32535 seconds (or milliseconds – see Parameter no. 15) Auto ON timer enabled- for a given amount of seconds (or milliseconds).</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Auto ON Disabled |
| 0 | please delete this option |

The manufacturer defined default value is ```0``` (please delete this option).

This parameter has the configuration ID ```config_12_2``` and is of type ```INTEGER```.


### Parameter 15: Timer Settings Unit

Set Timer Units to Seconds or Milliseconds
<p&gtChoose if you want to set the timer in seconds or milliseconds in parameters 11 and 12. Please note that the value for this parameter applies to settings for Smart plug 16A in all of the above parameters (timer on / timer off).</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | timer set in seconds |
| 1 | timer set in milliseconds |

The manufacturer defined default value is ```0``` (timer set in seconds).

This parameter has the configuration ID ```config_15_1``` and is of type ```INTEGER```.


### Parameter 30: Restore state ofter power failure

Restore on/off status for Smart plug 16A after power failure

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Restore state after power failure |
| 1 | Do not restore state after power failure |

The manufacturer defined default value is ```0``` (Restore state after power failure).

This parameter has the configuration ID ```config_30_1``` and is of type ```INTEGER```.


### Parameter 40: Treshold Change in Power  for reporting

Change of power consumption [Watt] reporting threshold
<p&gtChoose by how much power consumption needs to increase or decrease to be reported. Values correspond to percentages, so if 20 is set (by default), the device will report any power consumption changes of 20% or more compared to the last reading.</p&gt <ul&gt<li&gt0 - Power consumption reporting disabled</li&gt <li&gt1 - 100 = 1% - 100% Power consumption reporting enabled. New value is reported only when Wattage in real time changes by more than the percentage value set in this parameter compared to the previous Wattage reading, starting at 1% (the lowest value possible).</li&gt </ul&gt<p&gtNOTE: Power consumption needs to increase or decrease by at least 1 Watt to be reported, REGARDLESS of percentage set in this parameter.</p&gt <p&gtNOTE: When reporting Watts, module will automatically report also \[V\] (Voltage) and \[A\] (Amperes)</p&gt
Values in the range 0 to 100 may be set.

The manufacturer defined default value is ```20```.

This parameter has the configuration ID ```config_40_1``` and is of type ```INTEGER```.


### Parameter 41: Threshold time for power reporting

Threshold time for power reporting [Seconds]
<p&gtSet value refers to the time interval with which power consumption in Watts is reported (0 – 32535 seconds). If 300 is entered (by default), energy consumption reports will be sent to the gateway (hub) every 300 seconds (or 5 minutes).</p&gt <ul&gt<li&gt0 - Power consumption reporting disabled</li&gt <li&gt30 - 32535 = 30 - 32535 seconds. Power consumption reporting enabled. Report is sent according to time interval (value) set here.</li&gt </ul&gt<p&gtThe device is reporting the following values (if there was a change): Power [W], Voltage [V] and Current [A]</p&gt
Values in the range 0 to 32535 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_41_2``` and is of type ```INTEGER```.


### Parameter 42: Power Consumption Reporting Time Threshold

Power Consumption Reporting Time Threshold [Seconds]
<p&gtSet value refers to the time interval with which power consumption in Watts is reported (0 – 32535 seconds).</p&gt <p&gtIf 300 is entered (by default), energy consumption reports will be sent to the gateway (hub) every 300 seconds (or 5 minutes).</p&gt <p&gtValues :</p&gt <ul&gt<li&gt0 : Power consumption reporting disabled</li&gt <li&gt30 - 32535 : 30 - 32535 seconds. Power consumption reporting enabled. Report is sent according to time interval (value) set here.</li&gt </ul&gt
Values in the range 0 to 32535 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_42_2``` and is of type ```INTEGER```.


### Parameter 50: Down value

Down value [watt]
<p&gtLower power threshold used in parameter no. 52. </p&gt <p&gtValues:</p&gt <ul&gt<li&gtdefault value 30 : 30 W <br /&gt</li&gt <li&gt0 – 4000 : 0W – 4000 W <br /&gt</li&gt </ul&gt<p&gtDown value cannot be higher than a value specified in parameter no. 51 <br /&gt</p&gt <p&gtNOTE:</p&gt <ul&gt<li&gtif parameter no. 50 value is 100W and if measured power is lower than 100W, the association is send association is send again if measured power will rise above 105W</li&gt <li&gtPower threshold step is 1W <br /&gt</li&gt </ul&gt
Values in the range 0 to 4000 may be set.

The manufacturer defined default value is ```30```.

This parameter has the configuration ID ```config_50_2``` and is of type ```INTEGER```.


### Parameter 51: Up value

Upper power threshold used in parameter no. 52
<p&gtValues</p&gt <ul&gt<li&gtdefault value 50 : 50 W</li&gt <li&gt0 – 4000 : 0 W – 400 W </li&gt </ul&gt<p&gtUp value cannot be lower than a value specified in the parameter no. 50.</p&gt <p&gtNOTE:</p&gt <ul&gt<li&gtIf parameter no. 51 value is 200W and if measured power is higher than 200W the association is send</li&gt <li&gtAssociation is send again if measured power will fall below 190W</li&gt <li&gtPower threshold step is 1W </li&gt </ul&gt
Values in the range 0 to 4000 may be set.

The manufacturer defined default value is ```50```.

This parameter has the configuration ID ```config_51_2``` and is of type ```INTEGER```.


### Parameter 52: Action in case of exceeding defined power values

Action in case of exceeding defined power values (parameters 50 and 51)

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Inactive |
| 1 | Turn the associated devices on if underpowered |
| 2 | Turn the associated devices off if underpowered |
| 3 | Turn the associated devices on if overpowered |
| 4 | Turn the associated devices off if overpowered |
| 5 | Turn the associated devices on/off |
| 6 | Turn the associated devices off/on |

The manufacturer defined default value is ```6``` (Turn the associated devices off/on).

This parameter has the configuration ID ```config_52_1``` and is of type ```INTEGER```.

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

The ZMNHYD supports 5 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
Reserved for communication with main controller

Association group 1 supports 1 node.

### Group 2: Status on/off

Send control command BASIC_SET 0x00/0xFF
<p&gtThis group is assigned to the Smart Plug status On/Off. It allows sending the control command BASIC_SET 0x00/0xFF to associated devices whenevert the Smart Plug is turned On or Off.</p&gt

Association group 2 supports 5 nodes.

### Group 3: Load dependent 

sending control commands BASIC_SET 0x00/0xFF depending on current load
<p&gtThis groups allows to send control commands BASIC_SET 0x00/0xFF to associated devices depending on the current load. This association group is configured through the parameters no. 50, 51 and 52.</p&gt

Association group 3 supports 5 nodes.

### Group 4: Secure Status On/Off

Send control command BASIC_SET 0x00/0xFF
<p&gtThis group is equivalent to association group 2, except commands are sent securily encapsulated.</p&gt

Association group 4 supports 5 nodes.

### Group 5: Secure Load dependent 

sending control commands BASIC_SET 0x00/0xFF depending on current load
<p&gtThis group is equivalent to association group 3, except commands are sent securily encapsulated.</p&gt

Association group 5 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| Linked to BASIC|
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V5| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Qubino Smart Plug 16A extended manual](https://opensmarthouse.org/zwavedatabase/822/Qubino-Smart-Plug-16A-PLUS-extended-manual-eng-2-2-2.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/822).
