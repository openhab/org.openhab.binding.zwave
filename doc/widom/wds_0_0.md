---
layout: documentation
title: WDS - ZWave
---

{% include base.html %}

# WDS Universal Double Switch
This describes the Z-Wave device *WDS*, manufactured by *wiDom* with the thing type UID of ```widom_wds_00_000```.

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![WDS product image](https://opensmarthouse.org/zwavedatabase/432/image/)


The WDS supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtUniversal Double Switch is an ON/OFF control device designed to independently control two separate loads, suited for use as both a local and remote switch. Similarly to the other WiDom “in wall” devices, it can be fully integrated into pre-existing systems and configured to associate configurable behaviours to a specific number of clicks, in full integration with the Z-Wave home automation ecosystem.</p&gt <p&gtEach of its two channels features an integrated consumption measurement device. The Universal Double Switch also boasts the lowest energy consumption on the market.</p&gt <p&gtAt the same time, it is completely configurable so that it can adapt to the most varied needs while also being ready to be used without needing additional configurations in order to operate.</p&gt <p&gtThanks to the framework developed by WiDom, the various types of “events” on the “External Switch” can be recognised and associated to actions to be performed on the device, on any associated devices, on all devices on the network. The events that the system recognises are the number of “clicks” or hold on the External Switch.</p&gt <p&gt<br /&gtExternal switch: External button or switch connected to the input I1 or I2</p&gt <p&gtEvents: The actions performed on the external Switch: Clicks or Hold.</p&gt <p&gtClick: If the external switch is a button (when pressed it autonomously returns to the initial position), a click means pressing and then releasing it. If the external switch is bistable (when pressed it does not return to the initial position), a single click means one single flip of the switch.</p&gt <p&gtTechnical Specifications</p&gt <p&gtPower Supply: 230 VAC±10% 50/60 Hz<br /&gtMaximum Load on Relay Resistive Loads: 8 A for each channel; 10A for both channels<br /&gtLimit Temperature: 105 °C<br /&gtWork Temperature: -10 – 40 °C<br /&gtRadio Protocol Z-Wave: 868,4 MHz<br /&gtRadio Range: Up to 100 m outdoor, Up to 40 m indoor<br /&gtDimensions (WxDxH): 37x37x17 mm<br /&gtConsumption: < 260 mW in standby; < 480 mW with one working load; < 700 mW with both working loads<br /&gtElectrical IP Rating: IP 20<br /&gtActuator element: Relay<br /&gtConformity: CE, RoHS</p&gt

### Inclusion Information

<p&gtThe device supports both the Network Wide Inclusion (which offers the opportunity of inclusion into a network even if the device is not directly connected to the controller) and the Normal Inclusion mechanisms.</p&gt <p&gtIf the device is not included into a Z-Wave network, a single click on the (B) button or on one of the external switches will launch the process of traditional inclusion. If the device inclusion procedure does not start within 2 seconds, the Network Wide Inclusion network will be launched lasting a variable amount of time between 15-30 seconds.</p&gt <p&gtINFO: Through the inclusion procedure, activated with a single click on one of the external switches, the system determines the type of external switch (see parameter No. 62).</p&gt

### Exclusion Information

<p&gtOnly a controller can remove a device from the network. WiDom Universal Double Switch is compatible with all Z-Wave certified controllers. After the exclusion procedure has been activated by the controller, the device can be removed, putting it in Exclusion Mode by three consecutive clicks on the (B) button or on the external switch, when available.</p&gt

## Channels

The following table summarises the channels available for the WDS -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Switch 1 | switch_binary1 | switch_binary | Switch | Switch | 
| Electric meter (watts) 1 | meter_watts1 | meter_watts | Energy | Number | 
| Switch 2 | switch_binary2 | switch_binary | Switch | Switch | 
| Electric meter (watts) 2 | meter_watts2 | meter_watts | Energy | Number | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Switch 1
Switch the power on and off.

The ```switch_binary1``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Electric meter (watts) 1
Indicates the instantaneous power consumption.

The ```meter_watts1``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Switch 2
Switch the power on and off.

The ```switch_binary2``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.

### Electric meter (watts) 2
Indicates the instantaneous power consumption.

The ```meter_watts2``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.



## Device Configuration

The following table provides a summary of the 15 configuration parameters available in the WDS.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Status upon receipt of 1 click  | Defines the status of the output O1/O2 after 1 Click |
| 2 | Status upon receipt of 2 clicks | Defines the status of the output O1/O2 after 2 Clicks |
| 3 | Type of outputs | Defines how the outputs are controlled |
| 4 | Number of clicks that control the device | Defines the Clicks on the Switches that control associated devices |
| 5 | Level used to control association group 2 and 3 | Defines how to control the devices associated to group 2 and 3. |
| 6 | Level used to control association group 4 and 5. | Defines how to control the devices associated to group 4 and 5. |
| 10 | Timer to switch OFF the Channel 1 | Defines the time after which the Channel 1 is switched OFF |
| 11 | Timer to switch OFF the Channel 2 | Defines the time after which the Channel 2 is switched OFF |
| 12 | Timer to switch ON the Channel 1 | Defines the time after which the Channel 1 is switched ON |
| 13 | Timer to switch ON the Channel 2 | Defines the time after which the Channel 2 is switched ON |
| 20 | Status after Multi-Channel Basic Set command | Defines how Basic Set commands controlling outputs |
| 21 | Outputs status upon receipt of a Basic Set command | Define which channels are controlled by Basic Set command |
| 60 | Start-up status | Defines the status of the device following a restart. |
| 61 | Configuration reset | Defines which parameters should be reset to default values |
| 62 | Type of external switch | Defines the type of external switch connected to the device |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 1: Status upon receipt of 1 click 

Defines the status of the output O1/O2 after 1 Click
<p&gtDefines the status of the output O1/O2 when the switch connected to I1/I2 receives 1 Click</p&gt <p&gtINFO:</p&gt <p&gt0 – IN_OUT configuration is available on WiDom Universal Double Switch starting from the<br /&gtfirmware version 1.40.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | IN_OUT |
| 1 | TOGGLE |
| 2 | On |
| 3 | Off |
| 4 | IGNORE |

The manufacturer defined default value is ```1``` (TOGGLE).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: Status upon receipt of 2 clicks

Defines the status of the output O1/O2 after 2 Clicks
<p&gtDefines the status of the output O1/O2 when the switch connected to I1/I2 receives 2 Clicks</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 1 | TOGGLE |
| 2 | On |
| 3 | Off |
| 4 | IGNORE |

The manufacturer defined default value is ```1``` (TOGGLE).

This parameter has the configuration ID ```config_2_1``` and is of type ```INTEGER```.


### Parameter 3: Type of outputs

Defines how the outputs are controlled
<p&gtDefines if the outputs are controlled individually, as a traditional device with two channels, or if its behaviour simulates a single pole double throw relay</p&gt <p&gt<strong&gtParameter value and</strong&gt <strong&gtdevice reaction</strong&gt</p&gt <p&gt0 – SINGLE CHANNELS (Default Value)</p&gt <p&gtChannel 1 and Channel 2 are controlled individually</p&gt <p&gtFrom 1 to 9 – SINGLE POLE DOUBLE THROW RELAY</p&gt <p&gtThe two channels are in opposite status. If the Channel 1 is close the Channel 2 is open, if the Channel 1 is open the Channel 2 is close. The value of the parameter defines the closing delay of the relay in tenth of seconds</p&gt <p&gtFrom 10 to 19 – SEQUENCING RELAY</p&gt <p&gtChannels are toggled in this way: both channels ON, only channel 1 ON, only channel 2 ON, both channels OFF. The value of the parameter, deducted of 9, defines the closing delay of the relay in tenth of seconds.</p&gt <p&gtFrom 20 to 39 – SELECTOR</p&gt <p&gtBoth channels can be OFF but they can never be ON simultaneously. It can be used for fan coil control, roller shutters, and in general for two-speed systems. The value of the parameter, deducted of 19, defines the closing delay of the relay in tenth of seconds.</p&gt <p&gt<strong&gtINFO:</strong&gt 0 – configuration is available on WiDom Universal Double Switch starting from the firmware version 1.40.</p&gt
Values in the range 0 to 39 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_3_1``` and is of type ```INTEGER```.


### Parameter 4: Number of clicks that control the device

Defines the Clicks on the Switches that control associated devices
<p&gtDefines the number of clicks on the Switch 1 or Switch 2 that enable the control of the correspondent associated devices</p&gt <p&gt<strong&gtTIP</strong&gt:</p&gt <p&gtThrough this parameter, together the setting of the parameters 1 and 2, it is possible to control simultaneously the local load and the associated devices (e.g. 1 click control both local load and associated devices) or in totally independent way (e.g. 1 click control only the local load, 2 clicks control only the associated devices).</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 1 | Controlled by 1 Click |
| 2 | Controlled by 2 Clicks |

The manufacturer defined default value is ```2``` (Controlled by 2 Clicks).

This parameter has the configuration ID ```config_4_1``` and is of type ```INTEGER```.


### Parameter 5: Level used to control association group 2 and 3

Defines how to control the devices associated to group 2 and 3.
<h4&gtConfiguration value and Action performed on the associated device</h4&gt <h5&gt<br /&gt0 – SWITCH\_OFF</h5&gt <p&gtThe associated devices are switched OFF</p&gt <h5&gt<br /&gt-1 – SWITCH\_ON</h5&gt <p&gtThe associated devices are switched ON</p&gt <h5&gtFrom 1 to 99</h5&gt <p&gtThe associated devices (dimmer, roller shutters) are set to the indicated level (only for devices associated to group 3)</p&gt <h5&gt<br /&gt100 – RELAY\_STATUS (Default Value)</h5&gt <p&gtIf the Relay 1 is ON/OFF, the associated devices are ON/OFF</p&gt <h5&gt<br /&gt101 – RELAY\_STATUS\_BUT\_IGNORE\_IF\_OFF</h5&gt <p&gtIf the Relay 1 is ON the associated devices are ON; if it is OFF no action is taken on the associated devices</p&gt <h5&gt<br /&gt102 – RELAY\_STATUS\_BUT\_IGNORE\_IF\_ON</h5&gt <p&gtIf the Relay 1 is OFF the associated devices are OFF; if it is ON no action is taken on the associated devices</p&gt <h5&gt<br /&gt103 – RELAY\_OPPOSITE\_BUT\_IGNORE\_IF\_OFF</h5&gt <p&gtIf the Relay 1 is ON the associated devices are OFF; if it is OFF no action is taken on the associated devices</p&gt <h5&gt<br /&gt104 – RELAY\_OPPOSITE\_BUT\_IGNORE\_IF\_ON</h5&gt <p&gtIf the Relay 1 is OFF the associated devices are ON; if it is ON no action is taken on the associated devices</p&gt <h5&gt<br /&gt105 – RELAY\_OPPOSITE</h5&gt <p&gtIf the Relay 1 is ON/OFF, the associated devices are OFF/ON</p&gt <h5&gt106 – IGNORE</h5&gt <p&gtNo action is taken on the associated devices</p&gt
Values in the range -1 to 106 may be set.

The manufacturer defined default value is ```100```.

This parameter has the configuration ID ```config_5_1``` and is of type ```INTEGER```.


### Parameter 6: Level used to control association group 4 and 5.

Defines how to control the devices associated to group 4 and 5.
<h4&gtConfiguration value and Action performed on the associated device</h4&gt <h5&gt<br /&gt0 – SWITCH\_OFF</h5&gt <p&gtThe associated devices are switched OFF</p&gt <h5&gt<br /&gt-1 – SWITCH\_ON</h5&gt <p&gtThe associated devices are switched ON</p&gt <h5&gtFrom 1 to 99</h5&gt <p&gtThe associated devices (dimmer, roller shutters) are set to the indicated level (only for devices associated to group 3)</p&gt <h5&gt<br /&gt100 – RELAY\_STATUS (Default Value)</h5&gt <p&gtIf the Relay 1 is ON/OFF, the associated devices are ON/OFF</p&gt <h5&gt<br /&gt101 – RELAY\_STATUS\_BUT\_IGNORE\_IF\_OFF</h5&gt <p&gtIf the Relay 1 is ON the associated devices are ON; if it is OFF no action is taken on the associated devices</p&gt <h5&gt<br /&gt102 – RELAY\_STATUS\_BUT\_IGNORE\_IF\_ON</h5&gt <p&gtIf the Relay 1 is OFF the associated devices are OFF; if it is ON no action is taken on the associated devices</p&gt <h5&gt<br /&gt103 – RELAY\_OPPOSITE\_BUT\_IGNORE\_IF\_OFF</h5&gt <p&gtIf the Relay 1 is ON the associated devices are OFF; if it is OFF no action is taken on the associated devices</p&gt <h5&gt<br /&gt104 – RELAY\_OPPOSITE\_BUT\_IGNORE\_IF\_ON</h5&gt <p&gtIf the Relay 1 is OFF the associated devices are ON; if it is ON no action is taken on the associated devices</p&gt <h5&gt<br /&gt105 – RELAY\_OPPOSITE</h5&gt <p&gtIf the Relay 1 is ON/OFF, the associated devices are OFF/ON</p&gt <h5&gt106 – IGNORE</h5&gt <p&gtNo action is taken on the associated devices</p&gt
Values in the range -1 to 106 may be set.

The manufacturer defined default value is ```100```.

This parameter has the configuration ID ```config_6_1``` and is of type ```INTEGER```.


### Parameter 10: Timer to switch OFF the Channel 1

Defines the time after which the Channel 1 is switched OFF
<p&gt0 (Default Value): Timer disabled</p&gt <p&gt<br /&gtFrom 1 to 32000 (seconds): After this time the relay of the Channel 1 is OFF</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_10_2``` and is of type ```INTEGER```.


### Parameter 11: Timer to switch OFF the Channel 2

Defines the time after which the Channel 2 is switched OFF
<p&gt0 (Default Value): Timer disabled</p&gt <p&gt<br /&gtFrom 1 to 32000 (seconds): After this time the relay of the Channel 2 is OFF</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_11_2``` and is of type ```INTEGER```.


### Parameter 12: Timer to switch ON the Channel 1

Defines the time after which the Channel 1 is switched ON
<p&gt0 (Default Value): Timer disabled</p&gt <p&gtFrom 1 to 32000 (seconds): After this time the relay of the Channel 1 is ON</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_12_2``` and is of type ```INTEGER```.


### Parameter 13: Timer to switch ON the Channel 2

Defines the time after which the Channel 2 is switched ON
<p&gt0 (Default Value): Timer disabled</p&gt <p&gtFrom 1 to 32000 (seconds): After this time the relay of the Channel 2 is ON</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_13_2``` and is of type ```INTEGER```.
This is a read only parameter.


### Parameter 20: Status after Multi-Channel Basic Set command

Defines how Basic Set commands controlling outputs
<p&gtThe two channels can be controlled individually by Z-Wave network. The status of the channels upon receipt of a Multi-Channel Basic Set command is defined by the value set on the parameter.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 1 | As Received |
| 2 | Ignore if ON |
| 3 | Ignore if OFF |
| 4 | IGNORE |

The manufacturer defined default value is ```1``` (As Received).

This parameter has the configuration ID ```config_20_1``` and is of type ```INTEGER```.


### Parameter 21: Outputs status upon receipt of a Basic Set command

Define which channels are controlled by Basic Set command
<p&gtThe channels of the device can be controlled individually from all other Z-Wave devices that support the multichannel feature. In order to support also the integration with no-multichannel systems, this parameter allows to define if the receipt of a no-multichannel systems, this parameter allows to define if the receipt of a no-multichannel command controls only the Channel 1 or both.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 1 | CHANNEL 1 |
| 3 | BOTH CHANNELS |

The manufacturer defined default value is ```3``` (BOTH CHANNELS).

This parameter has the configuration ID ```config_21_2``` and is of type ```INTEGER```.


### Parameter 60: Start-up status

Defines the status of the device following a restart.

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | OFF_OFF |
| 1 | ON_OFF |
| 2 | OFF_ON |
| 3 | ON_ON |
| 4 | PREVIOUS STATUS |

The manufacturer defined default value is ```4``` (PREVIOUS STATUS).

This parameter has the configuration ID ```config_60_1``` and is of type ```INTEGER```.


### Parameter 61: Configuration reset

Defines which parameters should be reset to default values

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | FACTORY RESET |
| 1 | ASSOCIATIONS RESET |
| 2 | CONFIGURATIONS RESET |
| 3 | RESTART DEVICE |
| 4 | IGNORE |

The manufacturer defined default value is ```4``` (IGNORE).

This parameter has the configuration ID ```config_61_1``` and is of type ```INTEGER```.


### Parameter 62: Type of external switch

Defines the type of external switch connected to the device

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | IGNORE |
| 1 | BUTTON |
| 2 | SWITCH |

The manufacturer defined default value is ```1``` (BUTTON).

This parameter has the configuration ID ```config_62_1``` and is of type ```INTEGER```.

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

The WDS supports 5 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
<p&gtDevices to receive notifications on: status changes; instantaneous power level; device local reset </p&gt

Association group 1 supports 8 nodes.

### Group 2: On/Off control (Switch 1)

<p&gtDevices controlled by switch 1</p&gt

Association group 2 supports 8 nodes.

### Group 3: Dimming control (Switch 1)

<p&gtDevices controlled by switch 1</p&gt

Association group 3 supports 8 nodes.

### Group 4: On/Off control (Switch 2)

<p&gtDevices controlled by switch 2</p&gt

Association group 4 supports 8 nodes.

### Group 5: Dimming control (Switch 2)

<p&gtDevices controlled by switch 2</p&gt

Association group 5 supports 8 nodes.

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
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |
#### Endpoint 1

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| Linked to BASIC|
| COMMAND_CLASS_METER_V3| |
#### Endpoint 2

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| Linked to BASIC|
| COMMAND_CLASS_METER_V3| |

### Documentation Links

* [User Manual (IT)](https://opensmarthouse.org/zwavedatabase/432/Widom-Double-Relay-IT-0.pdf)
* [User Manual (EN)](https://opensmarthouse.org/zwavedatabase/432/Widom-Double-Relay-EN.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/432).
