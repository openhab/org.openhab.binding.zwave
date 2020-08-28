---
layout: documentation
title: WiDom Universal Relay Switch - ZWave
---

{% include base.html %}

# WiDom Universal Relay Switch WiDom Universal Relay Switch
This describes the Z-Wave device *WiDom Universal Relay Switch*, manufactured by *wiDom* with the thing type UID of ```widom_ubs_01_000```.
This version of the device is limited to firmware version 1.0

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![WiDom Universal Relay Switch product image](https://opensmarthouse.org/zwavedatabase/298/image/)


The WiDom Universal Relay Switch supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtWiDOM Universal Relay Switch is an ON\\OFF device based on latching relay and can be used as both a local and remote switch.</p&gt

### Inclusion Information

<p&gtBy default, if the device is not included in any network, as soon as it is powered the Network Wide Inclusion procedure starts and lasts for about 1 minute. The procedure can be reactivated at the next device restart or by <strong&gtpressing the (R) button</strong&gt.</p&gt <p&gtThe device may be included in the network through Normal Inclusion by <strong&gtpressing the (B) button once or three times or alternatively by pressing the external switch once. </strong&gt</p&gt <p&gtINFO:</p&gt <p&gtDuring the inclusion procedure activated by a single click on the external switch, the system also automatically determines the switch type (See parameter No. 62)</p&gt

### Exclusion Information

<p&gtAfter the exclusion function has been activated from the controller, the device can be removed, putting it in <em&gtLearning Mode</em&gt by</p&gt <p&gt<strong&gtpressing the (B) button once or three times</strong&gt</p&gt <p&gtor more conveniently by</p&gt <p&gt<strong&gtusing the external switch</strong&gt after having enabled the learning mode activation from external switch (see parameter N°65).</p&gt

### General Usage Information

<p&gtWith WiDom devices, the normal switches/buttons found in a traditional electrical system can become intelligent control systems. WiDom Framework recognises the number of clicks or hold event on the external switch and can be configured to perform different actions based on the identified event. </p&gt

## Channels

The following table summarises the channels available for the WiDom Universal Relay Switch -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch | 

### Switch
Switch the power on and off.

The ```switch_binary``` channel is of type ```switch_binary``` and supports the ```Switch``` item and is in the ```Switch``` category.



## Device Configuration

The following table provides a summary of the 2 configuration parameters available in the WiDom Universal Relay Switch.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Device status 1 click | Device status when the external switch receives 1 click |
| 2 | Device status 2 click | Device status when the external switch receives 2 clicks |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 1: Device status 1 click

Device status when the external switch receives 1 click

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 1 | TOGGLE |
| 2 | On |
| 3 | Off |
| 4 | IGNORE |

The manufacturer defined default value is ```1``` (TOGGLE).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: Device status 2 click

Device status when the external switch receives 2 clicks
<p&gtDevice status when the external switch receives 2 clicks</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 1 | TOGGLE |
| 2 | On |
| 3 | Off |
| 4 | IGNORE |

The manufacturer defined default value is ```1``` (TOGGLE).

This parameter has the configuration ID ```config_2_1``` and is of type ```INTEGER```.

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

The WiDom Universal Relay Switch supports 5 association groups.

### Group 1: Group 1

<p&gtDevices that will be notified of changes in its status</p&gt

Association group 1 supports 16 nodes.

### Group 2: Group 2

<p&gtDevices that will be controlled by a single click on the external switch</p&gt

Association group 2 supports 16 nodes.

### Group 3: Group 3

<p&gtDevices that will be controlled by a double click on the external switch</p&gt

Association group 3 supports 16 nodes.

### Group 4: Group 4

<p&gtDevices that will be controlled by a triple click on the external switch</p&gt

Association group 4 supports 16 nodes.

### Group 5: Group 5

<p&gtDevices that will be controlled by hold on the external switch</p&gt

Association group 5 supports 16 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_BINARY_V1| Linked to BASIC|
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_NODE_NAMING_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |

### Documentation Links

* [User Manual](https://opensmarthouse.org/zwavedatabase/298/Widom-UBS--Operating-Manual-multiple-pages-en.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/298).
