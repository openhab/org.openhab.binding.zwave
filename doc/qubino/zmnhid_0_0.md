---
layout: documentation
title: ZMNHID - ZWave
---

{% include base.html %}

# ZMNHID Flush on/off thermostat
This describes the Z-Wave device *ZMNHID*, manufactured by *[Goap](http://www.qubino.com/)* with the thing type UID of ```qubino_zmnhid_00_000```.

The device is in the category of *HVAC*, defining Air condition devices, Fans.

![ZMNHID product image](https://opensmarthouse.org/zwavedatabase/358/image/)


The ZMNHID supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtThis Z-Wave module is used to regulate temperature. The module can be controlled either  through Z-wave network  or  through  the wall switch.<br /&gtThe module is designed to be mounted inside a “flush  mounting box” and  is  hidden  behind a traditional wall switch.<br /&gtModule measures power consumption of connected heating device</p&gt

### Inclusion Information

<p&gt<strong&gtModule Inclusion (Adding to Z-Wave network)</strong&gt</p&gt <ul&gt<li&gtConnect module to power supply</li&gt <li&gtEnable add/remove mode on main controller</li&gt <li&gtAuto inclusion   (works   for   about   5   seconds   after connected to power supply) <em&gt<strong&gtor</strong&gt</em&gt</li&gt <li&gtPress service button S for more than 2 second <em&gt<strong&gtor</strong&gt</em&gt</li&gt <li&gtPress  push  button  I1  three  times  within  3s  (3  times change switch state within 3 seconds).</li&gt </ul&gt<p&gt<strong&gtNOTE1</strong&gt</p&gt <p&gtFor   auto inclusion   procedure,   first   set   main controller into inclusion mode and then connect module to power supply.</p&gt <p&gt<strong&gtNOTE2</strong&gt</p&gt <p&gtWhen  connecting  temperature  sensor  to  module that  has  already  been  included,  you  have  to  exclude module  first.<br /&gtSwitch  off  power supply,connect  the  sensor and re include the module.</p&gt

### Exclusion Information

<p&gt<strong&gtModule    Exclusion/Reset    (Removing from Z-Wave network)</strong&gt</p&gt <ul&gt<li&gtConnect module to power supply</li&gt <li&gtBring module within maximum 1 meter (3 feet) of  the main controller,</li&gt <li&gtEnable add/remove mode on main controller press push button I1 five times within 3s (5 times change switch state within 3 seconds) in the first 60 seconds after the module is connected to the power supply or</li&gt <li&gtPress  service  button S (only  applicable  for  24V SELV supply voltage) for more than 6 second.</li&gt </ul&gt<p&gt<br /&gt<strong&gtBy this function all parameters of the module are set to default values and own ID is deleted.</strong&gt<br /&gt<strong&gtIf push button I1 is pressed three times within 3s (or service button S is  pressed more than 2 and less than 6 seconds) module is excluded, but configuration parameters are not set to default values. </strong&gt<br /&gt<br /&gt<strong&gtNOTE</strong&gt</p&gt <p&gtIf the module is included with parameters 100,101 or  102  with  values <br /&gtdifferent to default and module reset is done, wait at least 30s before next inclusion.</p&gt

## Channels

The following table summarises the channels available for the ZMNHID -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Sensor (temperature) | sensor_temperature | sensor_temperature | Temperature | Number:Temperature | 
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number | 
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number | 
| Thermostat mode | thermostat_mode | thermostat_mode | Temperature | Number | 
| Setpoint (heating) | thermostat_setpoint | thermostat_setpoint | Heating | Number:Temperature | 

### Sensor (temperature)
Indicates the current temperature.

The ```sensor_temperature``` channel is of type ```sensor_temperature``` and supports the ```Number:Temperature``` item and is in the ```Temperature``` category.

### Electric meter (watts)
Indicates the instantaneous power consumption.

The ```meter_watts``` channel is of type ```meter_watts``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Electric meter (kWh)
Indicates the energy consumption (kWh).

The ```meter_kwh``` channel is of type ```meter_kwh``` and supports the ```Number``` item and is in the ```Energy``` category. This is a read only channel so will only be updated following state changes from the device.

### Thermostat mode
Sets the thermostat.

The ```thermostat_mode``` channel is of type ```thermostat_mode``` and supports the ```Number``` item and is in the ```Temperature``` category.
The following state translation is provided for this channel to the ```Number``` item type -:

| Value | Label     |
|-------|-----------|
| 0 | Off |
| 1 | Heat |
| 2 | Cool |
| 3 | Auto |
| 4 | Aux Heat |
| 5 | Resume |
| 6 | Fan Only |
| 7 | Furnace |
| 8 | Dry Air |
| 9 | Moist Air |
| 10 | Auto Changeover |
| 11 | Heat Economy |
| 12 | Cool Economy |
| 13 | Away |

### Setpoint (heating)
Sets the thermostat setpoint.

The ```thermostat_setpoint``` channel is of type ```thermostat_setpoint``` and supports the ```Number:Temperature``` item and is in the ```Heating``` category.



## Device Configuration

The following table provides a summary of the 32 configuration parameters available in the ZMNHID.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Input 1 switch type | Input 1 switch type |
| 2 | Input 2 switch type | Input 2 switch type |
| 3 | Input 3 switch type | Input 3 switch type |
| 4 | Input 1 contact type |  |
| 5 | Input 2 contact type |  |
| 6 | Parameter   no.   6 | Input 3 contact type |
| 10 | Parameter   no.   10 | Activate / deactivate functions ALL ON/ALL OFF |
| 11 | Parameter no. 11 | I1 Functionality selection |
| 12 | Parameter   no.   12 | I2 Functionality selection |
| 13 | Parameter   no.   13 | I3 Functionality selection |
| 40 | Parameter   no.   40 | Power reporting in Watts on Power change |
| 42 | Parameter   no.   42 | Power reporting in Watts by time interval for Q1 |
| 43 | Parameter   no.   43 | Hysteresis On |
| 44 | Parameter   no.   44 | Hysteresis Off |
| 45 | Parameter   no.   45 | Antifreeze |
| 59 | Parameter   no.   59 | Thermostat Mode |
| 60 | Parameter   no.   60 | Too low temperature limit |
| 61 | Parameter   no.   61 | Too high temperature limit |
| 63 | Parameter   no.   63 | Output Switch selection |
| 70 | Input 1 status on delay | Switching reaction time on delay for I1 |
| 71 | Input 1 status off delay | Switching reaction time off delay for I1 |
| 72 | Input 2 status on delay | Switching reaction time on delay for I2 |
| 73 | Input 2 status off delay | Switching reaction time off delay for I2 |
| 74 | Input 3 status on delay | Switching reaction time on delay for I3 |
| 75 | Input 3 status off delay | Switching reaction time off delay for I3 |
| 100 | Parameter  no.  100  | Enable / Disable Endpoint I1 or select Notification Type and Event |
| 101 | Parameter  no.  101  | Enable / Disable Endpoint I2 or select Notification Type and Event |
| 102 | Parameter  no.  102 | Enable / Disable Endpoint I3 or select Notification Type and Event |
| 110 | Parameter no. 110 | Temperature sensor offset settings |
| 120 | Parameter   no.   120  | Digital temperature sensor reporting |
| 121 | Parameter   no.   121  | Digital temperature sensor / setpoint selector |
| 122 | Parameter   no.   122 | Node ID of external battery powered room sensor |
|  | Switch All Mode | Set the mode for the switch when receiving SWITCH ALL commands |

### Parameter 1: Input 1 switch type

Input 1 switch type

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | mono-stable switch type (push button) |
| 1 | bi-stable switch type |

The manufacturer defined default value is ```1``` (bi-stable switch type).

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: Input 2 switch type

Input 2 switch type

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | mono-stable switch type (push button) |
| 1 | bi-stable switch type |

The manufacturer defined default value is ```1``` (bi-stable switch type).

This parameter has the configuration ID ```config_2_1``` and is of type ```INTEGER```.


### Parameter 3: Input 3 switch type

Input 3 switch type

The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | mono-stable switch type (push button) |
| 1 | bi-stable switch type |

The manufacturer defined default value is ```1``` (bi-stable switch type).

This parameter has the configuration ID ```config_3_1``` and is of type ```INTEGER```.


### Parameter 4: Input 1 contact type



The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | NO (normally open) input type |
| 1 | NC (normally close) input type |

The manufacturer defined default value is ```0``` (NO (normally open) input type).

This parameter has the configuration ID ```config_4_1``` and is of type ```INTEGER```.


### Parameter 5: Input 2 contact type



The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | NO (normally open) input type |
| 1 | NC (normally close) input type |

The manufacturer defined default value is ```0``` (NO (normally open) input type).

This parameter has the configuration ID ```config_5_1``` and is of type ```INTEGER```.


### Parameter 6: Parameter   no.   6

Input 3 contact type
<p&gtSee parameter 4 (valid for I3 instead of I1)</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | NO (normally open) input type |
| 1 | NC (normally close) input type |

The manufacturer defined default value is ```0``` (NO (normally open) input type).

This parameter has the configuration ID ```config_6_1``` and is of type ```INTEGER```.


### Parameter 10: Parameter   no.   10

Activate / deactivate functions ALL ON/ALL OFF
<p&gtModule responds to commands ALL ON / ALL OFF                 that may be sent by the main controller or by other controller belonging to the system.</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | ALL ON is not active ALL OFF is not active |
| 1 | ALL ON is not active ALL OFF active |
| 2 | ALL ON active ALL OFF is not active |
| 255 | ALL ON active, ALL OFF active |

The manufacturer defined default value is ```255``` (ALL ON active, ALL OFF active).

This parameter has the configuration ID ```config_10_2``` and is of type ```INTEGER```.


### Parameter 11: Parameter no. 11

I1 Functionality selection
<p&gtAvailable config. parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gtDefault value 1</li&gt <li&gt32767 – input  I1 doesn’t influence  on  the Heat/Cool process</li&gt <li&gt1 - input I1 changes the mode of the thermostat between Off  and Heat/Cool.In  this  case  function  on window sensor is disabled</li&gt <li&gt2 -input   I1   influences   on   heating/cooling valves according  to  status of window sensor. In  this  case function of Off and Heat/Cool selection by I1 is disabled.</li&gt </ul&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_11_2``` and is of type ```INTEGER```.


### Parameter 12: Parameter   no.   12

I2 Functionality selection
<p&gtAvailable config. parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gtDefault value 32767</li&gt <li&gt32767 - input  I2 does  not  influence  on  the Heat/Cool process</li&gt <li&gtFrom  0  to  990 - Temperature  set  point  from  0.0°C  to 99.0°C. When   I2   is   pressed,   it   automatically   set temperature setpoint according to value defined here. In this case function of condense sensor is disabled</li&gt <li&gtFrom 1001 to 1150 - Temperature set point from -0.1°C to -15.0°C.<br /&gtWhen  I2  is  pressed,  it  automatically  set temperature setpoint according to value defined here. In this case function of condense sensor is disabled</li&gt <li&gt2000 - Input  I2  influences  on  the heating/cooling valve according  to status  of  condense  sensor,  In  this  case function of setpoint selection with I2 is disabled.</li&gt </ul&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_12_2``` and is of type ```INTEGER```.


### Parameter 13: Parameter   no.   13

I3 Functionality selection
<p&gtAvailable config. parameters (data type is 2 Byte DEC):</p&gt <ul&gt<li&gtDefault value 32767</li&gt <li&gt32767 - input  I3 does not influence on the Heat/Cool process</li&gt <li&gt1 - input I3 changes the mode of the thermostat between Heat and Cool and override  parameter  59.  In this case function on flood sensor is disabled</li&gt <li&gt2 - input  I3  influences  on  cooling and heating  valves according to status of flood sensor. In this case function of Heat and Cool selection by I3 is disabled.</li&gt </ul&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```32767```.

This parameter has the configuration ID ```config_13_2``` and is of type ```INTEGER```.


### Parameter 40: Parameter   no.   40

Power reporting in Watts on Power change
<p&gt0 = Reporting Disabled</p&gt <p&gt1–100 = 1% - 100% Reporting Enabled</p&gt <p&gtPower report is sent (pushed) only when actual power in Watts in real time changed for more than set percentage comparing to previous actual power in Watts, step is 1%.</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_40_1``` and is of type ```INTEGER```.


### Parameter 42: Parameter   no.   42

Power reporting in Watts by time interval for Q1
<p&gtSet value means time interval (0 - 32767) in seconds when power is sent.</p&gt <p&gtAvailable config. parameters:</p&gt <p&gtdefault value is 0 (power report is disabled).</p&gt <p&gt0 = Reporting Disabled</p&gt <p&gt1–32767 = 1 second – 32767 seconds. Reporting enabled.</p&gt <p&gtPower report is sent with time interval set by entered value.</p&gt
Values in the range 0 to 32767 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_42_2``` and is of type ```INTEGER```.


### Parameter 43: Parameter   no.   43

Hysteresis On
<p&gtThis parameter defines temperature min difference between real measured temperature and set-point temperature to turn device on.</p&gt <p&gtAvailable config. parameters (data type is 2 Byte DEC): <br /&gtdefault value 1005 (-0.5 °C)<br /&gt0 -127  =  0.0°C ~ 12.7 °C  <br /&gt1001 -1127  =  -0.1°C ~ -12.7 °C</p&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```1005```.

This parameter has the configuration ID ```config_43_2``` and is of type ```INTEGER```.


### Parameter 44: Parameter   no.   44

Hysteresis Off
<p&gtThis parameter defines temperature min difference between real measured temperature and set-point temperature to turn device off.</p&gt <p&gtAvailable config. parameters (data type is 2 Byte DEC): <br /&gtdefault value 5 (0.5 °C)<br /&gt0 -127  =  0.0°C ~ 12.7 °C  <br /&gt1001 -1127  =  -0.1°C ~ -12.7 °C</p&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_44_2``` and is of type ```INTEGER```.


### Parameter 45: Parameter   no.   45

Antifreeze
<p&gtSet value means at which temperature the device will be turned on even if the thermostat was manually set to off.</p&gt <p&gtAvailable config. parameters (data type is 2 Byte DEC): </p&gt <ul&gt<li&gtdefault value 50(5.0 °C)</li&gt <li&gt0 -127  =  0.0°C ... 12.7 °C </li&gt <li&gt1001 -1127  =  -0.1°C ~ -12.7 °C</li&gt <li&gt255 - Antifreeze functionality disabled</li&gt </ul&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```50```.

This parameter has the configuration ID ```config_45_2``` and is of type ```INTEGER```.


### Parameter 59: Parameter   no.   59

Thermostat Mode
<p&gtAvailable config. parameters (data type is 1 Byte DEC):<br /&gtdefault value 0<br /&gt0  - Heat mode<br /&gt1 - Cool mode<br /&gt<br /&gtNOTE: <br /&gtAfter parameter change, first exclude module (without  setting  parameters to default value) and then re include the module!</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Heat Mode |
| 1 | Cool Mode |

The manufacturer defined default value is ```0``` (Heat Mode).

This parameter has the configuration ID ```config_59_1``` and is of type ```INTEGER```.


### Parameter 60: Parameter   no.   60

Too low temperature limit
<p&gtAvailable config. parameters (data type is 2 Byte DEC): <br /&gtdefault value 50 (Too low temperature limit is 5.0 °C)<br /&gt1 -1000  =  0.1°C ~ 100.0 °C step is  0.1°C. <br /&gt1001 -1150  =  -0.1°C ~ -15.0 °C</p&gt <p&gt<strong&gtNOTE</strong&gt</p&gt <p&gtToo low temperature limit is set by entered value. In case is set value out of this range, module is changing set value automatically to default value.</p&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```50```.

This parameter has the configuration ID ```config_60_2``` and is of type ```INTEGER```.


### Parameter 61: Parameter   no.   61

Too high temperature limit
<p&gtAvailable config. parameters (data type is 2 Byte DEC): <br /&gtdefault value 700 (Too high temperature limit is 70.0 °C)<br /&gt1 -1000  =  0.1°C ~ 100.0 °C step is  0.1°C. </p&gt <p&gt<strong&gtNOTE</strong&gt</p&gt <p&gtToo high temperature limit is set by entered value. In case is set value out of this range, module is changing set value automatically to default value.</p&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```700```.

This parameter has the configuration ID ```config_61_2``` and is of type ```INTEGER```.


### Parameter 63: Parameter   no.   63

Output Switch selection
<p&gtSet value means the type of the device that is connected to the on/off output. The  device  type  can  be  normally  open (NO) or normally close (NC). </p&gt <p&gtAvailable config. parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gtdefault value 0</li&gt <li&gt0 - When system is turned off the output is 0 V.</li&gt <li&gt1 - When system is turned off the output is 230 V.</li&gt </ul&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | When system is turned off the output is 0 V. |
| 1 | When system is turned off the output is 230 V. |

The manufacturer defined default value is ```0``` (When system is turned off the output is 0 V.).

This parameter has the configuration ID ```config_63_1``` and is of type ```INTEGER```.


### Parameter 70: Input 1 status on delay

Switching reaction time on delay for I1
<p&gtIf  the  value  of  parameter  is  different  to  0,  means  that the <br /&gtInfluence  of  this input  to  heating  or  cooling will  react  after inserted  time. This  parameter  has influence only when  the window sensor functionality is selected by the parameter no 11.</p&gt <p&gt<strong&gtNOTE</strong&gt: Device status on UI change immediately</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_70_2``` and is of type ```INTEGER```.


### Parameter 71: Input 1 status off delay

Switching reaction time off delay for I1
<p&gtIf the value of parameter is different to 0, means that the Influence of this input to heating or cooling will react after inserted time. This parameter has influence only when the window sensor functionality is selected by the parameter no 11.</p&gt <p&gt<strong&gtNOTE</strong&gt</p&gt <p&gtDevice status on UI change immediately</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_71_2``` and is of type ```INTEGER```.


### Parameter 72: Input 2 status on delay

Switching reaction time on delay for I2
<p&gt<strong&gtSee parameter 70 (valid for I2 instead of I1)</strong&gt<br /&gtThis parameter has influence  only  when  the  condense sensor functionality is selected by the parameter no12.</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_72_2``` and is of type ```INTEGER```.


### Parameter 73: Input 2 status off delay

Switching reaction time off delay for I2
<p&gt<strong&gtSee parameter 71 (valid for I2 instead of I1)</strong&gt<br /&gtThis  parameter  has  influence  only  when  the  condense sensor functionality is selected by the parameter no12.</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_73_2``` and is of type ```INTEGER```.


### Parameter 74: Input 3 status on delay

Switching reaction time on delay for I3
<p&gt<strong&gtSee parameter 70 (valid for I2 instead of I1)</strong&gt<br /&gtThis parameter has influence  only  when  the  condense sensor functionality is selected by the parameter no13.</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_74_2``` and is of type ```INTEGER```.


### Parameter 75: Input 3 status off delay

Switching reaction time off delay for I3
<p&gt<strong&gtSee parameter 71 (valid for I2 instead of I1)</strong&gt<br /&gtThis  parameter  has  influence  only  when  the  condense sensor functionality is selected by the parameter no13.</p&gt
Values in the range 0 to 32000 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_75_2``` and is of type ```INTEGER```.


### Parameter 100: Parameter  no.  100 

Enable / Disable Endpoint I1 or select Notification Type and Event
<p&gtEnabling I1 means that Endpoint (I1) will be present on UI. <br /&gtDisabling it will result in hiding the endpoint according to the parameter set value. Additionally, a Notification Type and Event can be selected for the endpoint. Available configuration parameters (data type is 1 Byte DEC):<br /&gtEndpoint device type selection:<br /&gtnotification sensor (1 -6): <br /&gtGENERIC\_TYPE\_SENSOR\_NOTIFICATION, <br /&gtSPECIFIC\_TYPE\_NOTIFICATION\_SENSOR<br /&gtdefault value 0<br /&gt1 - Home Security; Motion Detection,unknown location.<br /&gt2 - CO; Carbon Monoxide detected, unknown location.<br /&gt3 - CO2; Carbon Dioxide detected, unknown location.<br /&gt4 - W ater Alarm; Water Leak detected, unknown location.<br /&gt5 - Heat Alarm; Overheat detected, unknown location.<br /&gt6 - Smoke Alarm; Smoke detected, unknown location.<br /&gt0 - Endpoint, I1 disabled<br /&gt- sensor binary (9): <br /&gtGENERIC\_TYPE\_SENSOR\_BINARY, <br /&gtSPECIFIC\_TYPE\_NOT\_USED <br /&gt9 - Sensor binary <br /&gtNOTE1: <br /&gtAfter   parameter  change,   first   exclude  module (without  setting parameters  to default  value)  and  then  re include the module!<br /&gtNOTE2:   <br /&gtWhen the parameter is set to value 9 the notifications are send for Home Security.</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_100_1``` and is of type ```INTEGER```.


### Parameter 101: Parameter  no.  101 

Enable / Disable Endpoint I2 or select Notification Type and Event
<p&gtSee parameter 100 (valid for I2 instead of I1)</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_101_1``` and is of type ```INTEGER```.


### Parameter 102: Parameter  no.  102

Enable / Disable Endpoint I3 or select Notification Type and Event
<p&gtSee parameter 100 (valid for I3 instead of I1)</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_102_1``` and is of type ```INTEGER```.


### Parameter 110: Parameter no. 110

Temperature sensor offset settings
<p&gtSet value result in adding or subtracting that value to actual measured value by sensor.</p&gt <p&gtAvailable config. parameters (data type is 2 Byte DEC):<br /&gt<br /&gt</p&gt <ul&gt<li&gtdefault value 32536</li&gt <li&gt32536 – offset is 0.0°C</li&gt <li&gtFrom 1 to 100 – value from 0.1 °C to 10.0 °C is added to actual measured temperature.</li&gt <li&gtFrom  1001  to  1100 –value  from -0.1  °C  to -10.0  °C  is subtracted to actual measured temperature.</li&gt </ul&gt
Values in the range 0 to 65535 may be set.

The manufacturer defined default value is ```32536```.

This parameter has the configuration ID ```config_110_2``` and is of type ```INTEGER```.


### Parameter 120: Parameter   no.   120 

Digital temperature sensor reporting
<p&gtIf digital temperature sensor is connected, module reports measured  temperature on temperature change defined by this parameter.</p&gt <p&gtAvailable config. parameters (data type is 1 Byte DEC):</p&gt <ul&gt<li&gtdefault value 5</li&gt <li&gt0 –Reporting disabled</li&gt <li&gt1 -127 = 0.1°C – 12.7°C, step is 0.1°C</li&gt </ul&gt
Values in the range 0 to 127 may be set.

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_120_1``` and is of type ```INTEGER```.


### Parameter 121: Parameter   no.   121 

Digital temperature sensor / setpoint selector
<p&gtIf  digital  temperature  sensor is not connected, module can grab measured   temperature from   external   secondary module.<br /&gt<br /&gtAvailable config. parameters (data type is 1 Byte DEC):<br /&gtDefault value 0<br /&gt<br /&gt0 - Internal   digital   temperature   sensor   is   mounted, setpoint is set by controller<br /&gt<br /&gt1 - (bit  0) temperature  is  grabbed  from  external  always on sensor with sensor\_multilevel\_get sent by association 3<br /&gt<br /&gt2 - (bit  1) temperature  is  grabbed  from  external  battery powered room sensor declared in parameter 122<br /&gt<br /&gt4 - (bit  2) setpoint  is  gragged  from  external  always  on module with thermostat\_setpoint\_get sent by association 5  <br /&gt<br /&gt8 - (bit  3) setpoint  is  grabbed  from  external  battery powered room sensor declared in parameter 122. <br /&gt<br /&gt10 – (bit  1  and  bit  3) temperature  AND  setpoint  are grabbed  from external  battery  powered  room  sensor declared in parameter 122</p&gt
Values in the range 0 to 255 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_121_1``` and is of type ```INTEGER```.


### Parameter 122: Parameter   no.   122

Node ID of external battery powered room sensor
<p&gtIf  digital  temperature  sensor is not connected, module can grab measured temperature from external battery powered room sensor defined by this parameter.<br /&gt<br /&gtAvailable config. parameters (data type is 1 Byte DEC):<br /&gt<br /&gtDefault value 0<br /&gt<br /&gt0 – external battery powered room sensor not in function<br /&gt<br /&gt1 -254 = Node  ID  of  external  battery  powered  room sensor<br /&gt<br /&gt<strong&gtNOTE</strong&gt:  </p&gt <p&gtGet sensor Node ID from controller and set parameter 122 immediately  after  sensor  weak  up  (after button press on it etc.)</p&gt
Values in the range 0 to 254 may be set.

The manufacturer defined default value is ```0```.

This parameter has the configuration ID ```config_122_1``` and is of type ```INTEGER```.

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

The ZMNHID supports 6 association groups.

### Group 1: Basic on/off Q1


Association group 1 supports 16 nodes.

### Group 2: Basic on/off I2


Association group 2 supports 16 nodes.

### Group 3: Basic on/off I3


Association group 3 supports 16 nodes.

### Group 4: High Temperature


Association group 4 supports 16 nodes.

### Group 5: Low Temperature


Association group 5 supports 16 nodes.

### Group 6: Default Reporting Group


Association group 6 supports 1 node.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SWITCH_ALL_V1| |
| COMMAND_CLASS_SENSOR_MULTILEVEL_V5| |
| COMMAND_CLASS_METER_V3| |
| COMMAND_CLASS_THERMOSTAT_MODE_V2| |
| COMMAND_CLASS_THERMOSTAT_SETPOINT_V2| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_ASSOCIATION_V1| |
| COMMAND_CLASS_VERSION_V1| |

### Documentation Links

* [User Manual](https://opensmarthouse.org/zwavedatabase/358/Qubino-Flush-On-Off-Thermostat-PLUS-user-manual-V1-2.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/358).
