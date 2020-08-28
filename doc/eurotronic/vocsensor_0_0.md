---
layout: documentation
title: VOC-SENSOR - ZWave
---

{% include base.html %}

# VOC-SENSOR Air Quality Sensor
This describes the Z-Wave device *VOC-SENSOR*, manufactured by *Eurotronics* with the thing type UID of ```eurotronic_vocsensor_00_000```.

The device is in the category of *Sensor*, defining Device used to measure something.

![VOC-SENSOR product image](https://opensmarthouse.org/zwavedatabase/1240/image/)


The VOC-SENSOR supports routing. This allows the device to communicate using other routing enabled devices as intermediate routers.  This device is also able to participate in the routing of data between other devices in the mesh network.

## Overview

<p&gtZ-Wave sensor in an ultra slim design housing provides comprehensive information on indoor air quality.</p&gt <p&gtBased on these information it is possible to always ensure a healthy living climate.</p&gt <p&gt• Ultra-flat design housing</p&gt <p&gt• Use of high-precision, Swiss sensor technology</p&gt <p&gt• Multicolor LED for signalling ventilation recommendations</p&gt <p&gt• Returns the following values:</p&gt <p&gt- VOC value (volatile organic compounds) in ppm</p&gt <p&gt- CO2 value (as CO2 equivalent) in ppm</p&gt <p&gt- Temperature (°C) and humidity (%)</p&gt <p&gt- Dew point (°C)</p&gt <p&gt• Supports Z-Wave Plus S2 security (encryption)</p&gt <p&gt• Z-Wave repeater function</p&gt <p&gt• Operation with plug-in power supply unit</p&gt

### Inclusion Information

<p&gtPress the back button three times within 1 second.</p&gt <p&gtWhile the inclusion is active, the LED is blinking green. If the inclusion was successful the green LED will light up for 5 seconds. Otherwise the red led will light up for 5 seconds to indicate failure.</p&gt

### Exclusion Information

<p&gtPress the back button three times within 1 second.</p&gt <p&gtWhile the exclusion is active, the LED is blinking green. If the exclusion was successful the green LED will light up for 5 seconds. Otherwise the red led will light up for 5 seconds to indicate failure.</p&gt

### General Usage Information

<p&gtOn factory default the device does not belong to any Z-Wave network Air Quality Sensor Z-Wave Plus needs to be added to an existing wireless network to communicate with the devices of this network.</p&gt <p&gtThis process is called Inclusion.</p&gt <p&gtAir Quality Sensor Z-Wave Plus can also be removed from a network.</p&gt <p&gtThis process is called Exclusion.</p&gt <p&gtBoth processes are initiated by the primary controller of the Z-Wave network.</p&gt <p&gtThis controller is turned into exclusion respective inclusion mode.</p&gt <p&gtPlease consult the manual of your Z-Wave Controller how to activate Inclusion or Exclusion mode.</p&gt <p&gtIf Air Quality Sensor Z-Wave Plus has been added to a network, it has to be removed prior to be added to another wireless network.</p&gt

## Channels

The following table summarises the channels available for the VOC-SENSOR -:

| Channel Name | Channel ID | Channel Type | Category | Item Type |
|--------------|------------|--------------|----------|-----------|
| Sensor (relative humidity) | sensor_relhumidity | sensor_relhumidity | Humidity | Number | 
| Sensor (VOLATILE_ORGANIC_COMPOUND) | sensor_voc | sensor_voc |  |  | 
| Sensor (CO2) | sensor_co2 | sensor_co2 | CarbonDioxide | Number | 
| Sensor (temperature) | sensor_temperature | sensor_temperature | Temperature | Number:Temperature | 
| Sensor (dew point) | sensor_dewpoint | sensor_dewpoint | Temperature | Number | 
| Alarm (HOME_HEALTH) | alarm_general | alarm_general | Alarm | Switch | 

### Sensor (relative humidity)
Indicates the current relative humidity.

The ```sensor_relhumidity``` channel is of type ```sensor_relhumidity``` and supports the ```Number``` item and is in the ```Humidity``` category. This is a read only channel so will only be updated following state changes from the device.

### Sensor (VOLATILE_ORGANIC_COMPOUND)
<p&gtAir Quality Sensor Z-Wave Plus measures the VOC concentration and automatically reports sensor readings to associated devices.</p&gt <p&gtPer default the reporting threshold is ±500ppb.</p&gt <p&gtThis parameter can be altered via configuration command class.<br /&gtSensor type: „Volatile Organic Compound level“<br /&gtScale: Parts/million (ppm)<br /&gtPrecision: 3</p&gt

Channel type information on this channel is not found.

### Sensor (CO2)
Indicates the CO2 level.

The ```sensor_co2``` channel is of type ```sensor_co2``` and supports the ```Number``` item and is in the ```CarbonDioxide``` category. This is a read only channel so will only be updated following state changes from the device.

### Sensor (temperature)
Indicates the current temperature.

The ```sensor_temperature``` channel is of type ```sensor_temperature``` and supports the ```Number:Temperature``` item and is in the ```Temperature``` category.

### Sensor (dew point)
Indicates the dewpoint.

The ```sensor_dewpoint``` channel is of type ```sensor_dewpoint``` and supports the ```Number``` item and is in the ```Temperature``` category. This is a read only channel so will only be updated following state changes from the device.

### Alarm (HOME_HEALTH)
<p&gtHome Health<br /&gtAir pollution level has changed<br /&gtPollution level<br /&gt0x01: Clean<br /&gt0x02: Slightly polluted<br /&gt0x03: Moderately polluted<br /&gt0x04: Highly polluted</p&gt

Indicates if an alarm is triggered.

The ```alarm_general``` channel is of type ```alarm_general``` and supports the ```Switch``` item and is in the ```Alarm``` category. This is a read only channel so will only be updated following state changes from the device.

The following state translation is provided for this channel to the ```Switch``` item type -:

| Value | Label     |
|-------|-----------|
| OFF | OK |
| ON | Alarm |



## Device Configuration

The following table provides a summary of the 8 configuration parameters available in the VOC-SENSOR.
Detailed information on each parameter can be found in the sections below.

| Param | Name  | Description |
|-------|-------|-------------|
| 1 | Temperature on-change reporting | Temperature on-change reporting |
| 2 | Humidity on-change reporting | Humidity on-change reporting |
| 3 | Unit Temperature | Unit for Temperature (Celsius or Fahrenheit) |
| 4 | Resolution Temperature | Resolution for Temperature |
| 5 | Resolution Humidity | Resolution Humidity |
| 6 | VOC on-change reporting | VOC on-change reporting |
| 7 | CO2eq on-change reporting | CO2eq on-change reporting |
| 8 | Air quality indication via LED | Air quality indication via LED |

### Parameter 1: Temperature on-change reporting

Temperature on-change reporting
<p&gt0x00 No on-change reporting (only time-based reports).</p&gt <p&gt0x01 - 0x32 report if temperature changed by delta = 0,1°C - 5,0°C</p&gt
The following option values may be configured, in addition to values in the range 0 to 50 -:

| Value  | Description |
|--------|-------------|
| 0 | No on-change reporting |

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_1_1``` and is of type ```INTEGER```.


### Parameter 2: Humidity on-change reporting

Humidity on-change reporting
<p&gt0x00 No on-change reporting (only time-based reports)</p&gt <p&gt0x01 - 0x0A report if humidity changed by delta = 1% ...10%</p&gt
The following option values may be configured, in addition to values in the range 0 to 10 -:

| Value  | Description |
|--------|-------------|
| 0 | No on change reporting |

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_2_1``` and is of type ```INTEGER```.


### Parameter 3: Unit Temperature

Unit for Temperature (Celsius or Fahrenheit)
<p&gt0x00 Temperature reports in Celsius </p&gt <p&gt0x01 Temperature reports in Fahrenheit</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | Celsius |
| 1 | Fahrenheit |

The manufacturer defined default value is ```0``` (Celsius).

This parameter has the configuration ID ```config_3_1``` and is of type ```INTEGER```.


### Parameter 4: Resolution Temperature

Resolution for Temperature
<p&gt0x00 No resolution (example 22°C)</p&gt <p&gt0x01 1/10 resolution (example 22.3°C)</p&gt <p&gt0x02 1/100 resolution (example 22.35°C)</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | No resolution |
| 1 | 1/10 resolution |
| 2 | 1/100 resolution |

The manufacturer defined default value is ```1``` (1/10 resolution).

This parameter has the configuration ID ```config_4_1``` and is of type ```INTEGER```.


### Parameter 5: Resolution Humidity

Resolution Humidity
<p&gt0x00 No resolution (example 33%)</p&gt <p&gt0x01 1/10 resolution (example 33.4%)</p&gt <p&gt0x02 1/100 resolution (example 33.45%)</p&gt
The following option values may be configured -:

| Value  | Description |
|--------|-------------|
| 0 | No resolution |
| 1 | 1/10 resolution |
| 2 | 1/100 resolution |

The manufacturer defined default value is ```0``` (No resolution).

This parameter has the configuration ID ```config_5_1``` and is of type ```INTEGER```.


### Parameter 6: VOC on-change reporting

VOC on-change reporting
<p&gt0x00 No on-change reporting (only time-based reports)</p&gt <p&gt0x01 - 0x0A report if VOC reading changed by 100ppb - 1000ppb</p&gt
Values in the range 0 to 10 may be set.

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_6_1``` and is of type ```INTEGER```.


### Parameter 7: CO2eq on-change reporting

CO2eq on-change reporting
<p&gt0x00 No on-change reporting (only time-based reports)</p&gt <p&gt0x01 - 0x0A report if CO2eq reading changed by 100ppm - 1000ppm</p&gt
Values in the range 0 to 10 may be set.

The manufacturer defined default value is ```5```.

This parameter has the configuration ID ```config_7_1``` and is of type ```INTEGER```.


### Parameter 8: Air quality indication via LED

Air quality indication via LED
<p&gt0x00 No air quality indication via LEDs</p&gt <p&gt0x01 Indicate measured air quality via LEDs </p&gt
Values in the range 0 to 1 may be set.

The manufacturer defined default value is ```1```.

This parameter has the configuration ID ```config_8_1``` and is of type ```INTEGER```.


## Association Groups

Association groups allow the device to send unsolicited reports to the controller, or other devices in the network. Using association groups can allow you to eliminate polling, providing instant feedback of a device state change without unnecessary network traffic.

The VOC-SENSOR supports 2 association groups.

### Group 1: Lifeline

The Lifeline association group reports device status to a hub and is not designed to control other devices directly. When using the Lineline group with a hub, in most cases, only the lifeline group will need to be configured and normally the hub will perform this automatically during the device initialisation.
Air Quality Sensor Z-Wave Plus can be associated with other devices.
<p&gtCommands:</p&gt <p&gtDEVICE\_RESET\_LOCALLY\_NOTIFICATION,NOTIFICATION\_REPORT,SENSOR\_MULTILEVEL\_REPORT</p&gt

Association group 1 supports 1 node.

### Group 2: Temperature

Air Quality Sensor Z-Wave Plus can be associated with other devices.
<p&gtCommand: SENSOR\_MULTILEVEL\_REPORT</p&gt

Association group 2 supports 5 nodes.

## Technical Information

### Endpoints

#### Endpoint 0

| Command Class | Comment |
|---------------|---------|
| COMMAND_CLASS_NO_OPERATION_V1| |
| COMMAND_CLASS_BASIC_V1| |
| COMMAND_CLASS_SENSOR_MULTILEVEL_V10| |
| COMMAND_CLASS_ASSOCIATION_GRP_INFO_V1| |
| COMMAND_CLASS_DEVICE_RESET_LOCALLY_V1| |
| COMMAND_CLASS_ZWAVEPLUS_INFO_V1| |
| COMMAND_CLASS_CONFIGURATION_V1| |
| COMMAND_CLASS_ALARM_V8| |
| COMMAND_CLASS_MANUFACTURER_SPECIFIC_V1| |
| COMMAND_CLASS_POWERLEVEL_V1| |
| COMMAND_CLASS_FIRMWARE_UPDATE_MD_V1| |
| COMMAND_CLASS_ASSOCIATION_V2| |
| COMMAND_CLASS_VERSION_V2| |
| COMMAND_CLASS_SECURITY_V1| |

### Documentation Links

* [Installation and Operation Guide](https://opensmarthouse.org/zwavedatabase/1240/Eurotronic-LGS-Z-Wave-Plus-BDA-web-EN-1.pdf)

---

Did you spot an error in the above definition or want to improve the content?
You can [contribute to the database here](https://opensmarthouse.org/zwavedatabase/1240).
