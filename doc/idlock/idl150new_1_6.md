---
layout: documentation
title: ID-150 - ZWave
---

{% include base.html %}

# ID-150 Z wave module for ID Lock 150 and 101
This describes the Z-Wave device *ID-150*, manufactured by *ID Lock AS* with the thing type UID of ```idlock_idl150new_01_006```.
This version of the device is limited to firmware versions above 1.6

The device is in the category of *Lock*, defining Devices whose primary pupose is locking something.

![ID-150 product image](https://opensmarthouse.org/zwavedatabase/1106/image/)


The ID-150 supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is unable to participate in the routing of data from other devices.

## Overview

The ID Lock Z-wave module is a security enabled Z-wave Plus product that is able to use encrypted Z-wave Plus messages in order to communicate to other Z-wave Plus products enabled security.

The module is proprietary for the ID Lock 150 and also backwards compatible with the ID Lock 101. The ID Lock Z-wave module must be used in conjunction with a Security Enabled Z-wave Controller in order to fully utilize their full capability.

The ID Lock Z-wave module can be included and operated in any Z-wave network containing certified other Z-wave products regardless of manufacturer. The ID Lock Z-wave module does not support the Basic Set Command Class.

### Inclusion Information

  1. Bring the controller as close as possible to the lock unit
  2. Set the controller to inclusion mode
  3. Push and hold KEY button on the inside panel on the lock until all keys on the outside light up and a audible signal is given.
  4. Release the KEY button on the inside panel
  5. Enter your Master PIN and press * 
  6. Click 2 and then *
  7. Click 5, don't press *
  8. The LED flashes blue when inclusion is in progress
  9. The inclusion may take some time as security is required

### Exclusion Information

  1. Set the controller to Exclusion mode
  2. Push and hold KEY button on the inside panel on the lock until all keys on the outside light up and a audible signal is given.
  3. Release the KEY button on the inside panel
  4. Enter your Master PIN and press * 
  5. Click 2 and then *
  6. Click 5, don't press *
  7. The LED flashes blue when exclusion is in progress

### General Usage Information

Configuration parameters 2 RFID Registration Configuration and 10 Retrieve RFID Information have not been added since they are only valid for the 101 model. 

## Channels

The following table summarises the channels available for the ID-150 -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Door Lock | lock_door | lock_door | Door | Switch | 
| Door State | sensor_door | sensor_door | Door | Contact | 
| Alarm (access) | alarm_access | alarm_access | Door | Switch | 
| Alarm (emergency)  [Deprecated]| alarm_emergency | alarm_emergency | Alarm | Switch | 
| Alarm (burglar) | alarm_burglar | alarm_burglar | Door | Switch | 
| Alarm (raw)  [Deprecated]| alarm_raw | alarm_raw |  | String | 
| Battery Level | battery-level | system.battery_level | Battery | Number |

### Door Lock
Lock and unlock the door.

The ```lock_door``` channel is of type ```lock_door``` and supports the ```Switch``` item and is in the ```Door``` category.
The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| ON | Locked |
| OFF | Unlocked |

### Door State
Indicates if the door/window is open or closed.

The ```sensor_door``` channel is of type ```sensor_door``` and supports the ```Contact``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Contact``` item type -:

| Value | Label     |
|-------|-----------|
| OPEN | Open |
| CLOSED | Closed |

### Alarm (access)
Indicates if the access control alarm is triggered.

The ```alarm_access``` channel is of type ```alarm_access``` and supports the ```Switch``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Alarm (emergency) [Deprecated]
Indicates Police, Fire, or Medical services should be contacted.

The ```alarm_emergency``` channel is of type ```alarm_emergency``` and supports the ```Switch``` item and is in the ```Alarm``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| ON | EMERGENCY |
| OFF | OK |

**Note:** This channel is marked as deprecated so should not be used.

### Alarm (burglar)
Indicates if the burglar alarm is triggered.

The ```alarm_burglar``` channel is of type ```alarm_burglar``` and supports the ```Switch``` item and is in the ```Door``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |

### Alarm (raw) [Deprecated]
Provides alarm parameters as json string.

The ```alarm_raw``` channel is of type ```alarm_raw``` and supports the ```String``` item. This is a read only channel so will only be updated following state changes from the device.
This channel sets, and provides the alarm state as a JSON string. It is designed for use in rules.
**Note:** This channel is marked as deprecated so should not be used.

### Battery Level
Represents the battery level as a percentage (0-100%). Bindings for things supporting battery level in a different format (e.g. 4 levels) should convert to a percentage to provide a consistent battery level reading.

The ```system.battery-level``` channel is of type ```system.battery-level``` and supports the ```Number``` item and is in the ```Battery``` category.
This channel provides the battery level as a percentage and also reflects the low battery warning state. If the battery state is in low battery warning state, this will read 0%.


## Device Configuration

The following table provides a summary of the 7 configuration parameters available in the ID-150.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Door lock mode | Set if the lock is in away mode and if automatic locking should be enabled |
| 2 | RFID Mode | RFID Mode |
| 3 | Door Hinge Position Mode | Tell the lock which side your hinges are on seen from the outside |
| 4 | Door Audio Volume Level | Set the Audio Volume Level of the Lock |
| 5 | Door ReLock Mode | Sets if the door should relock or not |
| 6 | Service PIN Mode | Sets the validity of the service PIN |
| 7 | Door Lock Model Type | Sends information if the model of the lock is 101 or 150 |
|  | Lock Timeout | Sets the time after which the door will auto lock |

### Parameter 1: Door lock mode

Set if the lock is in away mode and if automatic locking should be enabled
Auto lock Mode, Manual lock mode, Activate Away Mode, Deactivate Away Mode.

If value is 0x02 (Enable Away, Manual lock) and the door is unlocked value will be set to 0x00.

If value is 0x03 (Enable Away, Auto lock) and the door is unlocked value will be set to 0x01.

 Default Value: 1 (Disable Away/Auto Lock Mode)
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Disable Away Manual Lock |
| 1 | Disable Away Auto Lock |
| 2 | Enable Away Manual Lock |
| 3 | Enable Away Auto Lock |

The manufacturer defined default value is ```1``` (Disable Away Auto Lock).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: RFID Mode

RFID Mode

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 5 | RFID activated |
| 9 | RFID deactivated |

The manufacturer defined default value is ```5``` (RFID activated).

This parameter has the configuration ID ```config_2_1``` and is of type ```INTEGER```.


### Parameter 3: Door Hinge Position Mode

Tell the lock which side your hinges are on seen from the outside

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Right hinged operation |
| 1 | Left hinged operation |

The manufacturer defined default value is ```0``` (Right hinged operation).

This parameter has the configuration ID ```config_3_1``` and is of type ```INTEGER```.


### Parameter 4: Door Audio Volume Level

Set the Audio Volume Level of the Lock

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | No sound |
| 1 | Level 1 |
| 2 | Level 2 |
| 3 | Level 3 |
| 4 | Level 4 |
| 5 | Level 5 (Default) |
| 6 | Max. sound level |

The manufacturer defined default value is ```5``` (Level 5 (Default)).

This parameter has the configuration ID ```config_4_1``` and is of type ```INTEGER```.


### Parameter 5: Door ReLock Mode

Sets if the door should relock or not
With this configuration ID Lock can automatically relock the door if an already locked door gets unlocked, but remains unopened. This is avoided by deactivating relocking.
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Disabled |
| 1 | Enabled |

The manufacturer defined default value is ```1``` (Enabled).

This parameter has the configuration ID ```config_5_1``` and is of type ```INTEGER```.


### Parameter 6: Service PIN Mode

Sets the validity of the service PIN
A configuration get command on this parameter returns the latest set parameter value (set by Z-wave).

This is a set only value, if changed locally on keypad these values are not changed on Z-wave module. Value 5 and 6 are for future use on door lock.
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Deactivated |
| 1 | Valid 1 time |
| 2 | Valid 2 times |
| 3 | Valid 5 times |
| 4 | Valid 10 times |
| 7 | Always Valid |
| 8 | Valid for 12h |
| 9 | Valid for 24h |
| 254 | Disabled |

The manufacturer defined default value is ```0``` (Deactivated).

This parameter has the configuration ID ```config_6_1``` and is of type ```INTEGER```.


### Parameter 7: Door Lock Model Type

Sends information if the model of the lock is 101 or 150
This configuration is only accepted by configuration get command

It is a read only parameter. Default value depends on the door lock model type.

101 = ID Lock 101

150 = ID Lock 150
Values in the range 0 to 0 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_7_1``` and is of type ```INTEGER```.
This is a read only parameter.

### Lock Timeout

Sets the time after which the door will auto lock.

This parameter has the configuration ID ```doorlock_timeout``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The ID-150 supports 1 association group.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
Notification Reports are sent out unsolicated to device included in the association group.

Association group 1 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_DOOR_LOCK_V1| |
| COMMAND_CLASS_USER_CODE_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V4| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_BATTERY_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Firmware Release Notes](https://opensmarthouse.org/zwavedatabase/1106/Versjonshistorikk-----ID-Lock.pdf)
* [ID Lock 150 installation and user manual](https://opensmarthouse.org/zwavedatabase/1106/ID-Lock-150-installation-and-user-manual.pdf)
* [ID Lock 150 Z wave manual](https://opensmarthouse.org/zwavedatabase/1106/IDLock150-ZWave-UserManual-v2-1.pdf)
* [ZWave user manual v3.02 (FW 1.6)](https://opensmarthouse.org/zwavedatabase/1106/IDLock150-ZWave-UserManual-v3-02.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1106).
