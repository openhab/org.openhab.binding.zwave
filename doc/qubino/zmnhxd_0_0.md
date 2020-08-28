---
layout: documentation
title: ZMNHXD - ZWave
---

{% include base.html %}

# ZMNHXD Qubino 3-Phase Smart Meter
This describes the Z-Wave device *ZMNHXD*, manufactured by *[Goap](http://www.qubino.com/)* with the thing type UID of ```qubino_zmnhxd_00_000```.

The device is in the category of *Sensor*, defining Device used to measure something.

![ZMNHXD product image](https://opensmarthouse.org/zwavedatabase/900/image/)


The ZMNHXD supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtQubino 3-Phase Smart Meter is used for energy measurements in three-phase electrical power network. It reduces energy consumption, lowers your utility</p&gt <p&gt<strong&gtFACTORY RESET</strong&gt</p&gt <ol&gt<li&gtConnect the device to the power supply</li&gt <li&gtPress and hold the S service button between 6 seconds and 20 seconds</li&gt <li&gtDevice will be removed from you network</li&gt </ol&gt<p&gt<strong&gtLED1 (Green)</strong&gt</p&gt <ul&gt<li&gtLED is ON = Power ON, module is included</li&gt <li&gtLED is 1s OFF, 1s ON = Power ON, module is excluded</li&gt </ul&gt<p&gt<strong&gtLED2 (Yellow)</strong&gt</p&gt <ol&gt<li&gtExternal IR relay enabled only <ul&gt<li&gtLED is ON = External IR relay is turned ON</li&gt <li&gtLED is OFF = External IR relay is turned OFF</li&gt <li&gtLED is 0.5s OFF, 0.5s ON = IR communication error</li&gt </ul&gt</li&gt <li&gtExternal TRIAC relay enabled only <ul&gt<li&gtLED is ON = External IR relay is turned ON</li&gt <li&gtLED is OFF = External IR relay is turned OFF</li&gt </ul&gt</li&gt <li&gtBoth TRIAC an IR enabled <ul&gt<li&gtLED is ON = External IR relay is turned ON</li&gt <li&gtLED is OFF = External IR relay is turned OFF</li&gt <li&gtLED is 0.5s OFF, 0.5s ON = IR communication error</li&gt </ul&gt</li&gt <li&gtExternal IR relay disabled <ul&gt<li&gtLED is ON = modbus packet is sent</li&gt <li&gtLED is OFF = modbus packet is received</li&gt </ul&gt</li&gt </ol&gt

### Inclusion Information

<p&gt<strong&gtAUTOMATICALLY ADDING THE DEVICE TO A Z-WAVE NETWORK (AUTO INCLUSION)</strong&gt</p&gt <ol&gt<li&gtEnable add/remove mode on your Z-Wave gateway (hub)</li&gt <li&gtAutomatic selection of secure/insecure inclusion</li&gt <li&gtThe device can be automatically added to a Z-Wave network during the first 2 minutes</li&gt <li&gtConnect the device to the power supply</li&gt <li&gtAuto-inclusion will be initiated within 5 seconds of connection to the power supply and the device will automatically enroll in your network</li&gt </ol&gt<p&gtNOTE: For S2 inclusion please check chapter – »16. Z-Wave Security«.</p&gt <p&gt<strong&gtMANUALLY ADDING THE DEVICE TO A Z-WAVE NETWORK (MANUAL INCLUSION)</strong&gt</p&gt <ol&gt<li&gtConnect the device to the power supply</li&gt <li&gtEnable add/remove mode on your Z-Wave gateway (hub)</li&gt <li&gtToggle the Service button S between 0.2 and 6 seconds</li&gt <li&gtA new multi-channel device will appear on your dashboard</li&gt </ol&gt

### Exclusion Information

<p&gt<strong&gtREMOVAL FROM A ZWAVE NETWORK (Z-WAVE EXCLUSION)</strong&gt</p&gt <ol&gt<li&gtConnect the device to the power supply</li&gt <li&gtMake sure the device is within direct range of your Z-Wave gateway (hub) or use a hand-held Z-Wave remote to perform exclusion</li&gt <li&gtEnable add/remove mode on your Z-Wave gateway (hub)</li&gt <li&gtPress and hold the S service button between 0.2 and 6 seconds</li&gt <li&gtThe device will be removed from your network but custom configuration parameters will not be erased</li&gt </ol&gt

### General Usage Information

<p&gt<strong&gtInstallation</strong&gt</p&gt <p&gtBefore installing the device, please read the following carefully and follow the instructions exactly:</p&gt <p&gt<strong&gtDanger of electrocution!</strong&gt</p&gt <p&gtInstallation of this device requires a great degree of skill and may be performed only by a licensed and qualified electrician. Please keep in mind that even when the device is turned off, voltage may still be present in the device’s terminals.</p&gt <p&gt<strong&gtNote!</strong&gt</p&gt <p&gtDo not connect the device to loads exceeding the recommended values. Connect the device exactly as shown in the provided diagrams. Improper wiring may be dangerous and result in equipment damage.</p&gt <p&gtElectrical installation must be protected by directly associated overcurrent protection fuse with rated current up to 65A, it must be used according to wiring diagram to achieve appropriate overload protection of the device.<br /&gt<br /&gtThe installation process, tested and approved by professional electricians, consists of the following simple steps:</p&gt <ul&gt<li&gtStep 1 – Turn OFF the fuse:<br /&gtTo prevent electrical shock and/or equipment damage, disconnect electrical power at the main fuse or circuit breaker before installation and maintenance.<br /&gtBe aware that even if the circuit breaker is off, some voltage may remain in the wires — before proceeding with the installation, be sure no voltage is present in the wiring.<br /&gtTake extra precautions to avoid accidentally turning the device on during installation.</li&gt <li&gtStep 2 – Installing the device:<br /&gtConnect the device exactly according to the diagrams shown below</li&gt </ul&gt

## Channels

The following table summarises the channels available for the ZMNHXD -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Electric meter (power factor) | meter_powerfactor | meter_powerfactor | Energy | Number | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Electric meter (kVAh) | meter_kvah | meter_kvah | Energy | Number | 
| Electric meter (power factor) 1 | meter_powerfactor1 | meter_powerfactor | Energy | Number | 
| Electric meter (watts) 1 | meter_watts1 | meter_watts | Energy | Number | 
| Electric meter (kWh) 1 | meter_kwh1 | meter_kwh | Energy | Number | 
| Electric meter (kVAh) 1 | meter_kvah1 | meter_kvah | Energy | Number | 
| Electric meter (power factor) 2 | meter_powerfactor2 | meter_powerfactor | Energy | Number | 
| Electric meter (amps) 2 | meter_current2 | meter_current | Energy | Number | 
| Electric meter (watts) 2 | meter_watts2 | meter_watts | Energy | Number | 
| Electric meter (volts) 2 | meter_voltage2 | meter_voltage | Energy | Number | 
| Electric meter (power factor) 3 | meter_powerfactor3 | meter_powerfactor | Energy | Number | 
| Electric meter (amps) 3 | meter_current3 | meter_current | Energy | Number | 
| Electric meter (watts) 3 | meter_watts3 | meter_watts | Energy | Number | 
| Electric meter (volts) 3 | meter_voltage3 | meter_voltage | Energy | Number | 
| Electric meter (power factor) 4 | meter_powerfactor4 | meter_powerfactor | Energy | Number | 
| Electric meter (amps) 4 | meter_current4 | meter_current | Energy | Number | 
| Electric meter (watts) 4 | meter_watts4 | meter_watts | Energy | Number | 
| Electric meter (volts) 4 | meter_voltage4 | meter_voltage | Energy | Number | 

### Electric meter (power factor)
Indicates the instantaneous power factor.

The ```meter_powerfactor``` channel is of type ```meter_powerfactor``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kVAh)
Indicates the energy consumption (kVAh).

The ```meter_kvah``` channel is of type ```meter_kvah``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (power factor) 1
Indicates the instantaneous power factor.

The ```meter_powerfactor1``` channel is of type ```meter_powerfactor``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts) 1
Indicates the instantaneous power consumption.

The ```meter_watts1``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh) 1
Indicates the energy consumption (kWh).

The ```meter_kwh1``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kVAh) 1
Indicates the energy consumption (kVAh).

The ```meter_kvah1``` channel is of type ```meter_kvah``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (power factor) 2
Indicates the instantaneous power factor.

The ```meter_powerfactor2``` channel is of type ```meter_powerfactor``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (amps) 2
Indicates the instantaneous current consumption.

The ```meter_current2``` channel is of type ```meter_current``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts) 2
Indicates the instantaneous power consumption.

The ```meter_watts2``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (volts) 2
Indicates the instantaneous voltage.

The ```meter_voltage2``` channel is of type ```meter_voltage``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (power factor) 3
Indicates the instantaneous power factor.

The ```meter_powerfactor3``` channel is of type ```meter_powerfactor``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (amps) 3
Indicates the instantaneous current consumption.

The ```meter_current3``` channel is of type ```meter_current``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts) 3
Indicates the instantaneous power consumption.

The ```meter_watts3``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (volts) 3
Indicates the instantaneous voltage.

The ```meter_voltage3``` channel is of type ```meter_voltage``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (power factor) 4
Indicates the instantaneous power factor.

The ```meter_powerfactor4``` channel is of type ```meter_powerfactor``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (amps) 4
Indicates the instantaneous current consumption.

The ```meter_current4``` channel is of type ```meter_current``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (watts) 4
Indicates the instantaneous power consumption.

The ```meter_watts4``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (volts) 4
Indicates the instantaneous voltage.

The ```meter_voltage4``` channel is of type ```meter_voltage``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.



## Device Configuration

The following table provides a summary of the 8 configuration parameters available in the ZMNHXD.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 7 | Input switch (i1) function selection | Available configuration parameters for input switch I1 |
| 40 | Reporting on power change | This parameter is valid for Active Powers Total, Phase1, Phase2 and Phase3. |
| 42 | Reporting on time interval | Energy report time intervals |
| 43 | Other Values - Reporting on time interval | Energy reports for Voltage, Current, Total Power Factor, Total Reactive Power |
| 100 | Enable / Disable External IR relay (BICOM) | Enable / Disable External IR relay (BICOM) |
| 101 | Enable / Disable External relay (IKA) | Enable / Disable External relay (IKA) |
| 106 | maximum power threshold of all phases together | maximum power threshold of all phases together |
| 112 | Power threshold – Delay before power on | Power threshold – Delay before power on |

### Parameter 7: Input switch (i1) function selection

Available configuration parameters for input switch I1
<ul&gt<li&gt0 – disabled</li&gt <li&gt2 – IR external relay control – mono stable push button</li&gt <li&gt3 – IR external relay control – bi-stable switch</li&gt <li&gt4 – External relay control – mono stable push button</li&gt <li&gt5 – External relay control – bi-stable switch</li&gt </ul&gt<p&gt<strong&gtNote!</strong&gt</p&gt <ul&gt<li&gtBy setting the parameter 7 to value 4 or 5 the external Relay (IKA) is working with input switch without enabling parameter no. 101</li&gt <li&gtTo make the IR Relay (BICOM) responsive to the digital input, in addition to the setting of the configuration parameter 7, parameter 100 must also be set to value 1 or 2.</li&gt </ul&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured, in addition to values in the range 0 to 5 -:

| Value  | Description |
|--------|-------------|
| 0 | Disabled |
| 2 | IR ext relay ctrl – mono stable push btn |
| 3 | IR external relay control – bi-stable switch |
| 4 | External relay control – mono stable push button |
| 5 | External relay control – bi-stable switch |

The manufacturer defined default value is ```0``` (disabled).

This parameter has the configuration ID ```config_7_1``` and is of type ```INTEGER```.


### Parameter 40: Reporting on power change

This parameter is valid for Active Powers Total, Phase1, Phase2 and Phase3.
<p&gtAvailable configuration parameters (data type is 1 Byte DEC)</p&gt <ul&gt<li&gtDefault value 50</li&gt <li&gt0 – reporting disabled</li&gt <li&gt1-100 = 1% - 100% reporting enabled. Power report is send only when actual power in Watts (in real time changes for more than set percentage comparing to previous Active Power, step is 1%.</li&gt </ul&gt<p&gt<strong&gtNOTE:</strong&gt if power change is less than 5 W, the report is not send (pushed).<br /&gt<strong&gtNOTE:</strong&gt Device is measuring also some disturbances even if on the output is no load. To avoid disturbances:</p&gt <ul&gt<li&gtIf measured Active Power (W) is below e.g. 5W-> the reported value in this case is 0W</li&gt </ul&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured, in addition to values in the range 0 to 100 -:

| Value  | Description |
|--------|-------------|
| 0 | reporting disabled |

The manufacturer defined default value is ```50```.

This parameter has the configuration ID ```config_40_1``` and is of type ```INTEGER```.


### Parameter 42: Reporting on time interval

Energy report time intervals
<p&gtThis parameter is currently valid only for Active Energy Total Import/Export (kWh), Reactive<br /&gtEnergy Total (kvarh), Total Energy (kVAh)</p&gt <p&gt<strong&gtAvailable configuration parameters (data type is 2 Byte DEC)</strong&gt</p&gt <ul&gt<li&gtDefault value 600 (600 seconds - 10 minutes)</li&gt <li&gt0 – reporting disabled</li&gt <li&gt600-65536 = 600 (600 seconds – 65536 seconds). Reporting enabled. Report is send with the time interval set by entered value.</li&gt </ul&gt<p&gt<strong&gtNote:</strong&gt Device is reporting only if there was a change of 0.1 in Energy</p&gt <p&gt<strong&gtNote:</strong&gt In the future will be possible to measure and report also Active Energy on PH1, PH2 and PH3.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured, in addition to values in the range 600 to 65535 -:

| Value  | Description |
|--------|-------------|
| 0 | reporting disabled |

The manufacturer defined default value is ```600```.

This parameter has the configuration ID ```config_42_2``` and is of type ```INTEGER```.


### Parameter 43: Other Values - Reporting on time interval

Energy reports for Voltage, Current, Total Power Factor, Total Reactive Power
<p&gt<strong&gtAvailable configuration parameters (data type is 2 Byte DEC)</strong&gt</p&gt <ul&gt<li&gtDefault value 600 (600 seconds - 10 minutes)</li&gt <li&gt0 – reporting disabled</li&gt <li&gt600-65536 = 600 (600 seconds – 65536 seconds). Reporting enabled. Report is sent with the time interval set by entered value.</li&gt <li&gtNote: Device is reporting only if there was a change</li&gt </ul&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured, in addition to values in the range 600 to 65535 -:

| Value  | Description |
|--------|-------------|
| 0 | reporting disabled |

The manufacturer defined default value is ```600```.

This parameter has the configuration ID ```config_43_2``` and is of type ```INTEGER```.


### Parameter 100: Enable / Disable External IR relay (BICOM)

Enable / Disable External IR relay (BICOM)
<p&gtAvailable configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gtdefault value 0</li&gt <li&gt0 – External IR relay disabled</li&gt <li&gt1 – External IR relay enabled and connected to all 3 Phases</li&gt <li&gt2 – External IR relay enabled and connected to a Phase 1</li&gt </ul&gt<p&gt<strong&gtNOTE1:</strong&gt After parameter change, first exclude module (without setting parameters to default value) and then re include the module.</p&gt <p&gt<strong&gtNOTE 2:</strong&gt If you don't have IR BICOM relay module mounted and you enable IR communication (parameter 100 is 1 or 2) there will be no valid IR relay state reported. It will be reported IR COMMUNICATION ERROR and LED2 will BLINK.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | External IR relay disabled |
| 1 | IR relay enabled for all 3 phases |
| 2 | IR relay enabled for Phase 1 |

The manufacturer defined default value is ```0``` (External IR relay disabled).

This parameter has the configuration ID ```config_100_1``` and is of type ```INTEGER```.


### Parameter 101: Enable / Disable External relay (IKA)

Enable / Disable External relay (IKA)
<p&gtAvailable configuration parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gtdefault value 0</li&gt <li&gt0 – External relay disabled</li&gt <li&gt1 – External relay enabled and connected to Phase 2</li&gt </ul&gt<p&gt<strong&gtNOTE:</strong&gt After parameter change, first exclude module (without setting parameters to default value) and then re include the module.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | External relay disabled |
| 1 | External relay enabled |

The manufacturer defined default value is ```0``` (External relay disabled).

This parameter has the configuration ID ```config_101_1``` and is of type ```INTEGER```.


### Parameter 106: maximum power threshold of all phases together

maximum power threshold of all phases together
<p&gtExternal IR relay (BICOM) power threshold settings – maximum power of all phases together</p&gt <p&gtThis parameter defines a threshold when External IR relay is being turned off. (If Parameter no. 100 is set to the value 1 or 2)</p&gt <p&gtAvailable configuration parameters (data type is 2 Byte<br /&gtDEC)</p&gt <ul&gt<li&gtDefault value 0</li&gt <li&gt0 – no function</li&gt <li&gt10-60000 – 10W-60000W</li&gt </ul&gt<p&gt<strong&gtNOTE:</strong&gt Meter is capable of measuring max 3x65A!</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
Values in the range 0 to 60000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_106_2``` and is of type ```INTEGER```.


### Parameter 112: Power threshold – Delay before power on

Power threshold – Delay before power on
<p&gtExternal IR relay/ External relay is turned off due to detected overload (as set by parameter 106&107) and remains off for a time, defined in this parameter. After that time, the output turns on to check, if the overload is still present.</p&gt <p&gtAvailable configuration parameters (data type is 2 Byte DEC)</p&gt <ul&gt<li&gtDefault value 0 (disabled)</li&gt <li&gt0 – External IR relay/ External relay will not turn back on</li&gt <li&gt30 – 32535 = 30 s – 32535 s</li&gt </ul&gt<p&gt<strong&gtNOTE:</strong&gt the delay time may be prolonged for more then 10s of the time set by the parameter.</p&gtThis is an advanced parameter and will therefore not show in the user interface without entering advanced mode.
The following option values may be configured, in addition to values in the range 0 to 32535 -:

| Value  | Description |
|--------|-------------|
| 0 | External relay will not turn back on |

The manufacturer defined default value is ```0``` (External relay will not turn back on).

This parameter has the configuration ID ```config_112_2``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The ZMNHXD supports 1 association group.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
Lifeline group (reserved for communication with the gateway (hub))

Association group 1 supports 1 node.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_METER_V3| Linked to BASIC|
| COMMAND_CLASS_TRANSPORT_SERVICE_V1| |
| COMMAND_CLASS_CRC_16_ENCAP_V1| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_MULTI_CHANNEL_V2| |
| COMMAND_CLASS_SUPERVISION_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
| COMMAND_CLASS_SECURITY_2_V1| |
#### Endpoint 1

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_METER_V3| Linked to BASIC|
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 2

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_METER_V3| Linked to BASIC|
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 3

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_METER_V3| Linked to BASIC|
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |
#### Endpoint 4

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_METER_V3| Linked to BASIC|
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_MULTI_CHANNEL_ASSOCIATION_V3| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Extended Manual](https://opensmarthouse.org/zwavedatabase/900/Qubino-3-Phase-Smart-Meter-PLUS-extended-manual-eng-3-3-2.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/900).
