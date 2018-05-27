---
layout: documentation
title: K8 - ZWave
---

{% include base.html %}

# K8 Z-Wave Battery Wall Controller
This describes the Z-Wave device *K8*, manufactured by *Sigma Designs (Former Zensys)* with the thing type UID of ```sigma_zwck8_00_000```.

The device is in the category of *Wall Switch*, defining Any device attached to the wall that controls a binary status of something, for ex. a light switch.

![K8 product image](https://www.cd-jackson.com/zwave_device_uploads/431/431_default.jpg)


## Overview

Note that this device incorrectly reports the manufacturer ID. This may be due to the device being uncertified and therefore this device may be removed from the database at some stage.

### Inclusion Information

1. Set the Master (Primary) controller into network Inclusion Mode.
2. Press and hold ca. 2 sec. both I and O keys of Group 2 (the top row of keys) on the Wall Controller unit at the same time. The LED light will stay solid. Release the keys.

### Exclusion Information

1. Set the Master (Primary) controller into network Exclusion Mode.
2. Press and hold ca. 2 sec. both I and O keys of Group 2 (the top row of keys) on the Wall Controller unit at the same time. The LED light will stay solid. Release the keys.

### Wakeup Information

Press and hold ca. 2 sec. both I and O keys on the top row (Group 2 keys). The LED indicator should turn ON if the battery has enough power and the Wall Controller works well.

## Channels

The following table summarises the channels available for the K8

| Channel | Channel Id | Category | Item Type |
|---------|------------|----------|-----------|
| Binary Sensor | sensor_binary | Door | Switch | 
| battery-level | system.battery-level | Battery | Number |

### Binary Sensor

Indicates if a sensor has triggered
        

The ```sensor_binary``` channel supports the ```Switch``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| ON | Triggered |
| OFF | Untriggered |

### Battery Level

Represents the battery level as a percentage (0-100%). Bindings for things supporting battery level in a different format (e.g. 4 levels) should convert to a percentage to provide a consistent battery level reading.

The ```system.battery-level``` channel supports the ```Number``` item and is in the ```Battery``` category.



## Device Configuration

The device has no configuration parameters configured.

## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The K8 supports 5 association groups.

### Group 1: Lifeline


This group supports 1 nodes.

### Group 2: Button pair 1


This group supports 20 nodes.

### Group 3: Button pair 2


This group supports 20 nodes.

### Group 4: Button pair 3


This group supports 20 nodes.

### Group 5: Button pair 4


This group supports 20 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_MULTILEVEL_V1| Linked to BASIC|
| COMMAND_CLASS_SENSOR_BINARY_V0| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_BATTERY_V1| |
| COMMAND_CLASS_WAKE_UP_V2| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V2| |

### Documentation Links

* [User manual](https://www.cd-jackson.com/zwave_device_uploads/431/ZWC-K8.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](http://www.cd-jackson.com/index.php/zwave/zwave-device-database/zwave-device-list/devicesummary/431).
