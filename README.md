---
layout: documentation
---

{% include base.html %}

# Z-Wave Binding

&#10;&#9;   &#10;&#9;   The ZWave binding supports an interface to a wireless Z-Wave home automation network. &#10;&lt;br&gt;&#10;ZWave is a wireless home automation protocol with reliable two way communications between nodes. It supports a mesh network where mains powered nodes can route messages between nodes that could otherwise not communicate with each other. The network supports hop distances of up to four hops.&#10;&lt;br&gt;&#10;A wide range of devices are supported from lights, switches and sensors to smoke alarms, window coverings and keyfobs. Z-Wave certification guarantees that certified devices will be compatible with each other and the network.&#10;&lt;br&gt;&#10;The binding uses a standard Z-Wave serial stick to communicate with the Z-Wave devices. There are many sticks available, and they all support the same interface so the binding does not distinguish between them.&#9;   &#10;&#9;    &#10;&#9;


## Supported Things

### ZWave Serial Adapter

Before the binding can be used, a serial adapter must be added. This needs to be done manually. Select `Serial ZStick`, and enter the serial port.


## Discovery

Once the binding is authorized, and an adapter is added, it automatically reads all devices that are included into the network. This is read directly from the Z-Wave controller and new things are added to the Inbox. When the discovery process is started, the binding will put the controller into inclusion mode for a defined period of time to allow new devices to be discovered and added to the network.


## Binding Configuration

There is no binding level configuration required for the Z-Wave binding. All configuration is performed on the devices, or the controller. This allows the system to support multiple controllers.


### Controller Configuration

The following section lists the controller configuration. If using manual configuration in text files, the parameter names are given in the square brackets.


#### Serial Port [port]

Sets the serial port name for the controller.


#### Controller Is Master [controller_master]

When *Controller Is Master* is true, the binding expects to be the main Z-Wave controller in the system. This is not related to the type of controller (*Primary* or *Secondary*), but is related to how devices will be configured.  This will instruct the binding to perform configuration of the device to send network related information such as device wakeup to the binding.

Many functions in Z-Wave only allow a single node to be set, and this is normally reserved for the main system controller. For example, battery device *Wakeup Node*, and *Lifeline* association groups usually only allow a single device to be set.

For most systems, this should be set to *true* - the only time when it should be *false* is if you normally control your Z-Wave network through a different system.


#### Controller Is SUC [controller_suc]

Sets the controller as a Static Update Controller within the network


#### Heal Time [heal_time]

Sets the nightly heal time (in hours).


#### Inclusion Mode [inclusion_mode]

The inclusion mode setting allows the user to set how the controller will initiate inclusion when discovery is initiated. There are three options available -:

* Low Power Inclusion: In this mode devices must be within 1 meter of the controller to be included.
* High Power Inclusion: In this mode devices must be able to communicate directly with the controller, so can be 10 to 15 meters from the controller under most conditions.
* Network Wide Inclusion: In this mode devices can be anywhere in the network. This mode 


#### Secure Inclusion Mode [security_inclusionmode]

The secure command classes allow you to secure communications between devices in your network. Secure communications is a good thing, and for devices such as locks that protect the security of your property, it's mandatory. However, most devices support the same communications and functions over the standard communication classes, without security. The secure classes come with some negative points - they communicate more slowly, and consume more power in battery devices. This is because there is roughly twice as much communication required for the security classes. You should therefore consider if you need all devices secured, or if it is acceptable for your system to only secure entry devices.

This option allows you to select which classes will be configured to use security - you can elect to use security on all devices that support it, or only on entry control devices.


#### Inclusion Mode Timeout [controller_inclusiontimeout]

This sets the maximum time that the controller will remain in inclusion or exclusion mode. Once inclusion is enabled, it will be disabled either when the first device is discovered, or when the timeout occurs. Generally this should be kept to a minimum as it increases the opportunity for unwanted nodes to be included into the network.

Note that updating this value will cause the controller to be reinitialised which will take all your Z-Wave devices offline for a short period.


#### Default Wakeup Period [controller_wakeupperiod]

This sets the system wide default wakeup period. If a battery device does not have the wakeup period set to a value, then the system will configure it to use this value during the device configuration. 

It is defined in seconds.


#### Network Security Key [security_networkkey]

This sets the network security key used in your network for securing communications using the secure command classes. It is a 16 byte value, specified in hexadecimal.


### Thing Configuration

There are a huge number of things supported by the Z-Wave binding, so configuration can not be covered here and you should refer to the device manual.

Things configured manually require the following minimum configuration to be set. -:

| Configuration    | Description                                                                                                   |
|------------------|---------------------------------------------------------------------------------------------------------------|
| zwave_nodeid     | Sets the node id of the node within the network.                                                              |
| zwave_deviceid   | Specifies the manufacturer device ID for this device (as decimal). This is used to get the thing type from the database.  |
| zwave_devicetype | Specifies the manufacturer device type for this device (as decimal). This is used to get the thing type from the database. |
| zwave_version    | Specifies the application version for this device. This is used to get the thing type from the database.      |


## Channels

### Controller Channels

The table below summarises the channels available in the controller. These provide health information about the communications between the binding and the controller.

| Channel    | Description                                            |
|------------|--------------------------------------------------------|
| serial_sof | Counts number of frames started                        |
| serial_ack | Counts number of frames acknowledged by the controller |
| serial_nak | Counts number of frame rejected by the controller      |
| serial_can | Counts number of frames cancelled by the controller    |
| serial_oof | Counts number of bytes received out of frame flow      |
| serial_cse | Counts number of frames received with checksum error   |

## Channel types
|Channel Type Id|Item Type|ReadOnly|Options|Description|
|---|---|---|---|---|
<a name="channel-id-alarm_access"></a>alarm_access | Switch |  Yes    |  Ok, Alarm  | Indicates if the access control alarm is triggered&#10;        
<a name="channel-id-alarm_burglar"></a>alarm_burglar | Switch |  Yes    |  Ok, Alarm  | Indicates if the burglar alarm is triggered&#10;        
<a name="channel-id-alarm_co"></a>alarm_co | Switch |  Yes    |  Ok, Alarm  | Indicates if the carbon monoxide alarm is triggered&#10;        
<a name="channel-id-alarm_co2"></a>alarm_co2 | Switch |  Yes    |  Ok, Alarm  | Indicates if the carbon dioxide alarm is triggered&#10;        
<a name="channel-id-alarm_entry"></a>alarm_entry | Switch |  Yes    |  Open, Closed  | Indicates if the entry alarm is triggered&#10;        
<a name="channel-id-alarm_flood"></a>alarm_flood | Switch |  Yes    |  Ok, Alarm  | Indicates if the flood alarm is triggered&#10;        
<a name="channel-id-alarm_general"></a>alarm_general | Switch |  Yes    |  Ok, Alarm  | Indicates if an alarm is triggered&#10;        
<a name="channel-id-alarm_heat"></a>alarm_heat | Switch |  Yes    |  Ok, Alarm  | Indicates if a heat alarm is triggered&#10;        
<a name="channel-id-alarm_motion"></a>alarm_motion | Switch |  Yes    |  Ok, Alarm  | Indicates if a motion alarm is triggered&#10;        
<a name="channel-id-alarm_power"></a>alarm_power | Switch |  Yes    |  Ok, Alarm  | Indicates if a power alarm is triggered&#10;        
<a name="channel-id-alarm_smoke"></a>alarm_smoke | Switch |  Yes    |  Ok, Alarm  | Indicates if a smoke is triggered&#10;        
<a name="channel-id-alarm_tamper"></a>alarm_tamper | Switch |  Yes    |  Ok, Alarm  | Indicates if the tamper alarm is triggered&#10;        
<a name="channel-id-barrier_position"></a>barrier_position | Number |   No   |    | Indicates the position of the barrier&#10;        
<a name="channel-id-barrier_state"></a>barrier_state | Number |  Yes    |  Closed, Closing, Stopped, Opening, Open  | Indicates the state of the barrier&#10;        
<a name="channel-id-blinds_control"></a>blinds_control | Rollershutter |   No   |    | Provides start / stop control of blinds&#10;        
<a name="channel-id-color_color"></a>color_color | Color |   No   |    | The color channel allows to control the color of a light.&#10;            It is also possible to dim values and switch the light on and off.&#10;        
<a name="channel-id-color_temperature"></a>color_temperature | Dimmer |   No   |    | The color temperature channel allows to set the color&#10;            temperature of a light from 0 (cold) to 100 (warm).
<a name="channel-id-lock_door"></a>lock_door | Switch |   No   |    | Lock and unlock the door
<a name="channel-id-meter_current"></a>meter_current | Number |  Yes    |    | Indicates the instantaneous current consumption
<a name="channel-id-meter_gas_cubic_meters"></a>meter_gas_cubic_meters | Number |  Yes    |    | Indicates the gas use in cubic meters
<a name="channel-id-meter_gas_cubic_feet"></a>meter_gas_cubic_feet | Number |  Yes    |    | Indicates the gas use in cubic feet
<a name="channel-id-meter_gas_pulses"></a>meter_gas_pulses | Number |  Yes    |    | Indicates the gas use in pulses
<a name="channel-id-meter_kvah"></a>meter_kvah | Number |  Yes    |    | Indicates the energy consumption (kVAh)
<a name="channel-id-meter_kwh"></a>meter_kwh | Number |  Yes    |    | Indicates the energy consumption (kWh)
<a name="channel-id-meter_powerfactor"></a>meter_powerfactor | Number |  Yes    |    | Indicates the instantaneous power factor
<a name="channel-id-meter_pulse"></a>meter_pulse | Number |  Yes    |    | Indicates the pulse count
<a name="channel-id-meter_reset"></a>meter_reset | Switch |   No   |    | Reset the meter
<a name="channel-id-meter_voltage"></a>meter_voltage | Number |  Yes    |    | Indicates the instantaneous voltage
<a name="channel-id-meter_water_cubic_meters"></a>meter_water_cubic_meters | Number |  Yes    |    | Indicates the instantaneous water consumption
<a name="channel-id-meter_water_cubic_feet"></a>meter_water_cubic_feet | Number |  Yes    |    | Indicates the instantaneous water consumption
<a name="channel-id-meter_water_gallons"></a>meter_water_gallons | Number |  Yes    |    | Indicates the instantaneous water consumption
<a name="channel-id-meter_water_pulse"></a>meter_water_pulse | Number |  Yes    |    | Indicates the instantaneous water consumption
<a name="channel-id-meter_watts"></a>meter_watts | Number |  Yes    |    | Indicates the instantaneous power consumption
<a name="channel-id-notification_smoke_alarm"></a>notification_smoke_alarm | Number |  Yes    |  Previous Events cleared, Smoke detected, Smoke detected, Unknown Location, Smoke Alarm Test, Replacement Required  | Smoke Alarm
<a name="channel-id-notification_co_alarm"></a>notification_co_alarm | Number |  Yes    |  Previous Events cleared, Carbon monoxide detected, Carbon monoxide detected, Unknown Location, Carbon monoxide Test, Replacement Required  | CO Alarm
<a name="channel-id-notification_co2_alarm"></a>notification_co2_alarm | Number |  Yes    |  Previous Events cleared, Carbon dioxide detected, Carbon dioxide detected, Unknown Location, Carbon dioxide Test, Replacement Required  | CO2 Alarm
<a name="channel-id-notification_heat_alarm"></a>notification_heat_alarm | Number |  Yes    |  Previous Events cleared, Overheat detected, Overheat detected, Unknown Location, Rapid Temperature Rise, Rapid Temperature Rise, Unknown Location, Under heat detected, Under heat detected, Unknown Location  | Heat Alarm
<a name="channel-id-notification_water_alarm"></a>notification_water_alarm | Number |  Yes    |  Previous Events cleared, Water Leak detected, Water Leak detected, Unknown Location, Water Level Dropped, Water Level Dropped, Unknown Location, Replace Water Filter  | Water Alarm
<a name="channel-id-notification_access_control"></a>notification_access_control | Number |  Yes    |  Previous Events cleared, Manual Lock Operation, Manual Unlock Operation, RF Lock Operation, RF Unlock Operation, Keypad Lock Operation, Keypad Unlock Operation, Manual Not Fully Locked Operation, RF Not Fully Locked Operation, Auto Lock Locked Operation, Auto Lock Not Fully Operation, LockJammed, All user codes deleted, Single user code deleted, New user code added, New user code not added due to duplicate code, Keypad temporary disabled, Keypad busy, New Program code Entered- Unique code for lock configuration, Manually Enter user Access code exceeds code limit, Unlock by RF with invalid user code, Locked by RF with invalid user code, Window/Door is open, Window/Door is closed, Barrier performing initialization process, Barrier operation (Open / Close) force has been exceeded, Barrier motor has exceeded manufacturer&#39;s operational time limit, Barrier operation has exceeded physical mechanical limits, Barrier unable to perform requested operation due to UL requirements, Barrier Unattended operation has been disabled per UL requirements, Barrier failed to perform Requested operation, device malfunction, Barrier Vacation Mode, Barrier Safety Beam Obstacle, Barrier Sensor Not Detected / Supervisory Error, Barrier Sensor Low Battery Warning, Barrier detected short in WallStation wires, Barrier associated with non-Z-wave remote control  | Access Control
<a name="channel-id-notification_home_security"></a>notification_home_security | Number |  Yes    |  Previous events cleared, Intrusion, Intrusion, Unknown Location, Tampering, Product cover removed, Tampering, Invalid Code, Glass Breakage, Glass Breakage, Unknown Location, Motion Detection, Motion Detection, Unknown Location  | Home Security
<a name="channel-id-notification_power_management"></a>notification_power_management | Number |  Yes    |  Previous events cleared, Power has been applied, AC mains disconnected, AC mains re-connected, Surge detected, Voltage Drop/Drift, Over-current detected, Over-voltage detected, Over-load detected, Load error, Replace battery soon, Replace battery now, Battery is charging, Battery is fully charged, Charge battery soon, Charge battery now!  | Power Management
<a name="channel-id-notification_system"></a>notification_system | Number |  Yes    |  Previous Events cleared, System hardware failure, System software failure, System hardware failure with manufacturer proprietary failure code, System software failure with manufacturer proprietary failure code  | System
<a name="channel-id-notification_emergency_alarm"></a>notification_emergency_alarm | Number |  Yes    |  Previous Events cleared, Contact Police, Contact Fire Service, Contact Medical Service  | Emergency Alarm
<a name="channel-id-notification_clock"></a>notification_clock | Number |  Yes    |  Previous Events cleared, Wake Up Alert, Timer Ended, Time remaining  | Clock
<a name="channel-id-notification_appliance"></a>notification_appliance | Number |  Yes    |  Previous Events cleared, Program started, Program in progress, Program completed, Replace main filter, Failure to set target temperature, Supplying water, Water supply failure, Boiling, Boiling failure, Washing, Washing failure, Rinsing, Rinsing failure, Draining, Draining failure, Spinning, Spinning failure, Drying, Drying failure, Fan failure, Compressor failure  | Appliance
<a name="channel-id-notification_home_health"></a>notification_home_health | Number |  Yes    |  Previous Events cleared, Leaving Bed, Sitting on bed, lying on bed, Posture changed, Sitting on edge of bed, Volatile Organic Compound level  | Home Health
<a name="channel-id-protection_local"></a>protection_local | Number |   No   |  Unprotected, Protection by sequence, No operation possible  | Sets the local protection mode&#10;        
<a name="channel-id-protection_rf"></a>protection_rf | Number |   No   |  Unprotected, No RF control, No RF response at all  | Sets the rf protection mode&#10;        
<a name="channel-id-scene_number"></a>scene_number | Number |   No   |    | Triggers when a scene button is pressed
<a name="channel-id-sensor_barpressure"></a>sensor_barpressure | Number |  Yes    |    | Indicates the barometric pressure
<a name="channel-id-sensor_binary"></a>sensor_binary | Switch |  Yes    |  Triggered, Untriggered  | Indicates if a sensor has triggered&#10;        
<a name="channel-id-sensor_dewpoint"></a>sensor_dewpoint | Number |  Yes    |    | Indicates the dewpoint
<a name="channel-id-sensor_direction"></a>sensor_direction | Number |  Yes    |    | Indicates the direction
<a name="channel-id-sensor_door"></a>sensor_door | Contact |  Yes    |  Open, Closed  | Indicates if the door/window is open or closed&#10;        
<a name="channel-id-sensor_flood"></a>sensor_flood | Switch |  Yes    |  Flood, Ok  | Indicates a flood sensor is activated
<a name="channel-id-sensor_general"></a>sensor_general | Number |  Yes    |    | 
<a name="channel-id-sensor_luminance"></a>sensor_luminance | Number |  Yes    |    | Indicates the current light reading
<a name="channel-id-sensor_power"></a>sensor_power | Number |  Yes    |    | Indicates the energy consumption (kWh)
<a name="channel-id-sensor_relhumidity"></a>sensor_relhumidity | Number |  Yes    |    | Indicates the current relative humidity
<a name="channel-id-sensor_rainrate"></a>sensor_rainrate | Number |  Yes    |    | Indicates the current relative humidity
<a name="channel-id-sensor_seismicintensity"></a>sensor_seismicintensity | Number |  Yes    |    | Indicates the current seismic intensity level
<a name="channel-id-sensor_temperature"></a>sensor_temperature | Number |  Yes    |    | Indicates the current temperature
<a name="channel-id-sensor_ultraviolet"></a>sensor_ultraviolet | Number |  Yes    |    | Indicates the current ultraviolet level
<a name="channel-id-sensor_velocity"></a>sensor_velocity | Number |  Yes    |    | Indicates the current velocity
<a name="channel-id-switch_binary"></a>switch_binary | Switch |   No   |    | Switch the power on and off
<a name="channel-id-switch_dimmer"></a>switch_dimmer | Dimmer |   No   |    | The brightness channel allows to control the brightness of a light.&#10;            It is also possible to switch the light on and off.&#10;        
<a name="channel-id-thermostat_setpoint"></a>thermostat_setpoint | Number |   No   |    | Sets the thermostate setpoint
<a name="channel-id-thermostat_fan_mode"></a>thermostat_fan_mode | Number |   No   |  Auto, On, Auto High, On High, Unknown(4), Unknown(5), Circulate  | Sets the fan mode
<a name="channel-id-thermostat_fan_state"></a>thermostat_fan_state | Number |   No   |  Idle, Running, Running High  | Sets the fan mode&#10;        
<a name="channel-id-thermostat_state"></a>thermostat_state | Number |   No   |  Idle, Heating, Cooling, Fan Only, Pending Heat, Pending Cool, Vent / Economiser  | Sets the thermostat operating state&#10;        
<a name="channel-id-thermostat_mode"></a>thermostat_mode | Number |   No   |  Off, Heat, Cool, Auto, Aux Heat, Resume, Fan Only, Furnace, Dry Air, Moist Air, Auto Changeover, Heat Economy, Cool Economy, Away  | Sets the thermostat&#10;        
<a name="channel-id-time_offset"></a>time_offset | Number |   No   |    | Provides the current time difference for the devices time&#10;        
<a name="channel-id-serial_sof"></a>serial_sof | Number |  Yes    |    | Counter tracking the number of SOF bytes received
<a name="channel-id-serial_ack"></a>serial_ack | Number |  Yes    |    | Counter tracking the number of frames acknowldeged by the controller
<a name="channel-id-serial_nak"></a>serial_nak | Number |  Yes    |    | Counter tracking the number of frames rejected by the controller
<a name="channel-id-serial_can"></a>serial_can | Number |  Yes    |    | Counter tracking the number of frames cancelled by the controller
<a name="channel-id-serial_oof"></a>serial_oof | Number |  Yes    |    | Counter tracking the number of out of flow bytes received
<a name="channel-id-eurotronic_cometz_00_000_thermostat_mode"></a>eurotronic_cometz_00_000_thermostat_mode | Number |   No   |  Off, Manual, Heat, Economy Heat  | Sets the thermostat mode
<a name="channel-id-eurotronic_stellaz_00_000_thermostat_mode"></a>eurotronic_stellaz_00_000_thermostat_mode | Number |   No   |  Economy Heat, Off, Heat  | Sets the thermostat mode
<a name="channel-id-honeywell_th8320zw_00_000_thermostat_mode"></a>honeywell_th8320zw_00_000_thermostat_mode | Number |   No   |  Auto, Off, Economy Heat, Cool, Economy Cool, Heat, Resume  | Sets the thermostat mode
<a name="channel-id-honeywell_th8320zw_00_000_thermostat_fanmode"></a>honeywell_th8320zw_00_000_thermostat_fanmode | Number |   No   |  Auto (Low), On (Low), Circulate  | Sets the thermostat fan mode
<a name="channel-id-honeywell_th8320zw_00_000_thermostat_fanstate"></a>honeywell_th8320zw_00_000_thermostat_fanstate | Number |   No   |  Idle  | Sets the thermostat fan state
<a name="channel-id-horstmann_asrzw_00_000_thermostat_mode"></a>horstmann_asrzw_00_000_thermostat_mode | Number |   No   |  Off, Heat  | Sets the thermostat mode
<a name="channel-id-horstmann_hrt4zw_00_000_thermostat_mode"></a>horstmann_hrt4zw_00_000_thermostat_mode | Number |   No   |  Heat, Off  | Sets the thermostat mode
<a name="channel-id-horstmann_ssr302_00_000_thermostat_mode"></a>horstmann_ssr302_00_000_thermostat_mode | Number |   No   |  Off, Heat  | Sets the thermostat mode
<a name="channel-id-horstmann_ssr302_00_000_thermostat_mode"></a>horstmann_ssr302_00_000_thermostat_mode | Number |   No   |  Off, Heat  | Sets the thermostat mode
<a name="channel-id-horstmann_ssr302_00_000_thermostat_mode"></a>horstmann_ssr302_00_000_thermostat_mode | Number |   No   |  Off, Heat  | Sets the thermostat mode
<a name="channel-id-intermatic_ca8900_00_000_thermostat_mode"></a>intermatic_ca8900_00_000_thermostat_mode | Number |   No   |  Economy Heat, Cool, Off, Heat, Economy Cool  | Sets the thermostat mode
<a name="channel-id-linear_tbz48_00_000_thermostat_mode"></a>linear_tbz48_00_000_thermostat_mode | Number |   No   |  Off, Aux Heat, Auto, Heat, Cool  | Sets the thermostat mode
<a name="channel-id-linear_tbz48_00_000_thermostat_fanmode"></a>linear_tbz48_00_000_thermostat_fanmode | Number |   No   |  On (Low), Auto (Low)  | Sets the thermostat fan mode
<a name="channel-id-linear_tbz48_00_000_thermostat_fanstate"></a>linear_tbz48_00_000_thermostat_fanstate | Number |   No   |  Running  | Sets the thermostat fan state
<a name="channel-id-qubino_zmnhid_00_000_thermostat_mode"></a>qubino_zmnhid_00_000_thermostat_mode | Number |   No   |  Off, Auto  | Sets the thermostat mode
<a name="channel-id-qubino_zmnhla_00_000_thermostat_mode"></a>qubino_zmnhla_00_000_thermostat_mode | Number |   No   |  Auto, Off  | Sets the thermostat mode
<a name="channel-id-rcs_tbz48_00_000_thermostat_mode"></a>rcs_tbz48_00_000_thermostat_mode | Number |   No   |  Heat, Auto, Aux Heat, Off, Cool  | Sets the thermostat mode
<a name="channel-id-rcs_tbz48_00_000_thermostat_fanmode"></a>rcs_tbz48_00_000_thermostat_fanmode | Number |   No   |  Auto (Low), On (Low)  | Sets the thermostat fan mode
<a name="channel-id-rcs_tbz48_00_000_thermostat_fanstate"></a>rcs_tbz48_00_000_thermostat_fanstate | Number |   No   |  Running  | Sets the thermostat fan state
<a name="channel-id-rcs_tz43_00_000_thermostat_mode"></a>rcs_tz43_00_000_thermostat_mode | Number |   No   |  Auto, Off, Heat, Cool, Aux Heat  | Sets the thermostat mode
<a name="channel-id-rcs_tz43_00_000_thermostat_fanmode"></a>rcs_tz43_00_000_thermostat_fanmode | Number |   No   |  On (Low), Auto (Low)  | Sets the thermostat fan mode
<a name="channel-id-rcs_tz43_00_000_thermostat_fanstate"></a>rcs_tz43_00_000_thermostat_fanstate | Number |   No   |  Running  | Sets the thermostat fan state
<a name="channel-id-remotec_zts110_00_000_thermostat_mode"></a>remotec_zts110_00_000_thermostat_mode | Number |   No   |  Cool, Auto, Off, Heat, Resume  | Sets the thermostat mode
<a name="channel-id-remotec_zts110_00_000_thermostat_fanmode"></a>remotec_zts110_00_000_thermostat_fanmode | Number |   No   |  Auto (Low), On (Low)  | Sets the thermostat fan mode
<a name="channel-id-remotec_zts110_00_000_thermostat_fanstate"></a>remotec_zts110_00_000_thermostat_fanstate | Number |   No   |  Idle  | Sets the thermostat fan state
<a name="channel-id-remotec_zxt120_00_000_thermostat_mode"></a>remotec_zxt120_00_000_thermostat_mode | Number |   No   |  Resume, Cool, Heat, Dry Air, Off, Auto  | Sets the thermostat mode
<a name="channel-id-remotec_zxt120_00_000_thermostat_fanmode"></a>remotec_zxt120_00_000_thermostat_fanmode | Number |   No   |  Auto (Low), On (Low), Auto (High), UNKNOWN_5, UNKNOWN_4, On (high)  | Sets the thermostat fan mode
<a name="channel-id-rtc_ct100_00_000_thermostat_mode"></a>rtc_ct100_00_000_thermostat_mode | Number |   No   |  Auto, Heat, Off, Cool  | Sets the thermostat mode
<a name="channel-id-rtc_ct100_00_000_thermostat_fanmode"></a>rtc_ct100_00_000_thermostat_fanmode | Number |   No   |  On (Low), Auto (Low)  | Sets the thermostat fan mode
<a name="channel-id-rtc_ct100_00_000_thermostat_fanstate"></a>rtc_ct100_00_000_thermostat_fanstate | Number |   No   |  Running  | Sets the thermostat fan state
<a name="channel-id-rtc_ct30_00_000_thermostat_mode"></a>rtc_ct30_00_000_thermostat_mode | Number |   No   |  Cool, Off, Heat  | Sets the thermostat mode
<a name="channel-id-rtc_ct30_00_000_thermostat_fanmode"></a>rtc_ct30_00_000_thermostat_fanmode | Number |   No   |  Auto (Low), On (Low)  | Sets the thermostat fan mode
<a name="channel-id-rtc_ct32_00_000_thermostat_mode"></a>rtc_ct32_00_000_thermostat_mode | Number |   No   |  Cool, Auto, Economy Cool, Heat, Economy Heat, Off  | Sets the thermostat mode
<a name="channel-id-rtc_ct32_00_000_thermostat_fanmode"></a>rtc_ct32_00_000_thermostat_fanmode | Number |   No   |  Auto (Low), On (Low)  | Sets the thermostat fan mode
<a name="channel-id-rtc_ct32_00_000_thermostat_fanstate"></a>rtc_ct32_00_000_thermostat_fanstate | Number |   No   |  Idle  | Sets the thermostat fan state
<a name="channel-id-rtc_ct32_00_000_thermostat_mode"></a>rtc_ct32_00_000_thermostat_mode | Number |   No   |  Cool, Auto, Economy Cool, Heat, Economy Heat, Off  | Sets the thermostat mode
<a name="channel-id-rtc_ct32_00_000_thermostat_fanmode"></a>rtc_ct32_00_000_thermostat_fanmode | Number |   No   |  Auto (Low), On (Low)  | Sets the thermostat fan mode
<a name="channel-id-rtc_ct32_00_000_thermostat_fanstate"></a>rtc_ct32_00_000_thermostat_fanstate | Number |   No   |  Idle  | Sets the thermostat fan state
<a name="channel-id-stelpro_stzw402_00_000_thermostat_mode"></a>stelpro_stzw402_00_000_thermostat_mode | Number |   No   |  Heat, Economy Heat  | Sets the thermostat mode
<a name="channel-id-thermofloor_tf016_00_000_thermostat_mode"></a>thermofloor_tf016_00_000_thermostat_mode | Number |   No   |  Economy Heat, Heat, Cool, Off  | Sets the thermostat mode
<a name="channel-id-thermofloor_tf016_01_008_thermostat_mode"></a>thermofloor_tf016_01_008_thermostat_mode | Number |   No   |  Cool, Economy Heat, Off, Heat  | Sets the thermostat mode
<a name="channel-id-trane_tzemt400bb32maa_00_000_thermostat_mode"></a>trane_tzemt400bb32maa_00_000_thermostat_mode | Number |   No   |  Auto, Aux Heat, Heat, Cool, Off  | Sets the thermostat mode
<a name="channel-id-trane_tzemt400bb32maa_00_000_thermostat_fanmode"></a>trane_tzemt400bb32maa_00_000_thermostat_fanmode | Number |   No   |  On (Low), Auto (Low)  | Sets the thermostat fan mode
<a name="channel-id-trane_tzemt400bb32maa_00_000_thermostat_fanstate"></a>trane_tzemt400bb32maa_00_000_thermostat_fanstate | Number |   No   |  Idle  | Sets the thermostat fan state
<a name="channel-id-trane_xl624_00_000_thermostat_mode"></a>trane_xl624_00_000_thermostat_mode | Number |   No   |  Cool, Aux Heat, Heat, Off, Auto  | Sets the thermostat mode
<a name="channel-id-trane_xl624_00_000_thermostat_fanmode"></a>trane_xl624_00_000_thermostat_fanmode | Number |   No   |  Circulate, Auto (Low), On (Low)  | Sets the thermostat fan mode
<a name="channel-id-trane_xl624_00_000_thermostat_fanstate"></a>trane_xl624_00_000_thermostat_fanstate | Number |   No   |  Idle, Running  | Sets the thermostat fan state
<a name="channel-id-trane_xr524_00_000_thermostat_mode"></a>trane_xr524_00_000_thermostat_mode | Number |   No   |  Aux Heat, Auto, Off, Heat, Cool  | Sets the thermostat mode
<a name="channel-id-trane_xr524_00_000_thermostat_fanmode"></a>trane_xr524_00_000_thermostat_fanmode | Number |   No   |  Auto (Low), Circulate, On (Low)  | Sets the thermostat fan mode
<a name="channel-id-trane_xr524_00_000_thermostat_fanstate"></a>trane_xr524_00_000_thermostat_fanstate | Number |   No   |  Idle  | Sets the thermostat fan state


### Device Channels


## Initialisation

To initialise the binding and get your Z-Wave network running, you need to follow the following steps -:

* Manually install the serial controller. It doesn't matter what type of Z-Wave dongle you have, there is only a single *thing type* since all sticks use the same communication protocol, and the binding can detect what functions the device supports by communicating with the stick.
* Set the serial port in the controller configuration.
* In the UI enable *discovery* mode - this will add all existing things into the *discovery inbox*. From the *inbox* you can add the device directly into the system.
* the binding should automatically detect the device type and should provide a list of *channels* to which you can attach *items*. Note that it may take some time to discover the device type - especially in battery devices.


## Z-Wave Network

This section provides information on the Z-Wave network, and how functions are implemented in the binding.


### Network Overview

The Z-Wave network includes devices known as *Controllers* and *Slaves*. As the name suggests, *Controllers* control how the network runs and provide network administration functions, while *Slaves* are users of the network.


#### Home ID
The network is identified with a *Home ID*. This is programmed into the controller, and can't be changed. It is used to identify the network in all frames that are transmitted over the air. When a device is included into a network, the controller sets the *Home ID* of the network in the slave so that the slave will only communicate over this network until it is removed from the network.


#### Node ID
Each node in the network is identified with a *Node ID*. The controller allocated the *Node ID* when the device is included into the network. A single Z-Wave network supports 232 devices (*Node ID* 1 to 232). The controller will allocate node IDs sequentially. Normally therefore the controller has Node ID 1 since it is normally the first device in the network. IDs will then be allocated sequentially up to number 232 after which the controller will allocate unused addresses.


#### Message Routing

A Z-Wave network is a *Mesh Network*. This allows each device in the network to route frames within the network so that devices don't need to communicate directly with the controller.  The Z-Wave network allows up to 4 hops.  For a network to work efficiently, each device must be able to communicate with a number of other devices - this creates what is known as a "Strong Mesh". It provides the controller with multiple options when selecting routes, and makes the network tolerant of device failures or radio interference issues.

If a route doesn't work, the controller will try a different route - up to three routes will be tried and if the message is not delivered the controller will report the failure.

Frames can also be transmitted as *Broadcast Frames* within the Z-Wave network, however to avoid overloading the network, *Broadcast Frames* are not routed so can only be used within direct communication of the sending device.


#### Command Classes

Z-Wave uses what are known as *Command Classes* to communicate. These *Command Classes* are a set of standardised commands to allow devices to perform specific functions. Each *Command Class* has a specific function, and each device will likely support multiple classes to allow the users to interact with the device.

Every device supports the *BASIC* command class. This is normally mapped to a specific *Command Class* - the *BASIC* class is used to provide a high degree of interoperability between devices, and is especially useful when devices are communicating peer-to-peer as slaves do not need to know the detail of many different classes.


### Controllers

There are different types of controllers in a Z-Wave network. This section provides an overview of the different types of controller.


#### Primary Controller
There is a single *Primary* controller in the network. This controller provides the network routing table


#### Static Update Controller (SUC)


#### Static ID Server (SIS)


### Slaves

Most devices in your network are *slaves* - they come in in two types, *routing* and *non-routing*.


## Z-Wave in practice

This section endeavors to provide some practical information about Z-Wave networks and how the system works that may be of use to users.


### Inclusion and Exclusion

Inclusion and exclusion are always started by the primary controller, unless an *SIS* is available in the network, in which case any controller can start these functions.  To include or exclude a device in the network, set the controller into include mode, and press the appropriate button on the device to place the device into include mode.  All Z-Wave devices will have such a button, and you should refer to the device manual.

Secure inclusion must be started from the binding. This is because once the device is included into the network, a key exchange takes place between the binding and the device. This key exchange must take place within a very short time of the inclusion, and if it doesn't succeed, the device must be excluded and included again.  Secure inclusion will generate a lot of activity on the network, so you should avoid other activities at the same time, and the device being included should be close to the controller to reduce any retries that could cause the security handshake to fail.


### Device Initialisation

As soon as the device is discovered (eg included) it is added to the inbox. At this point we still don't know the manufacturer etc.

During the initialisation of a device, the binding performs a discovery and configuration phase. It requests information from the device first to find out information like the manufacturer and what device classes are supported. Once it knows this, the device is shown in the inbox with a 'proper label' based on information from the database.

We then initialise some information in the device such as associations. Association configuration is read from the database. Configuration parameters are not configured automatically and must be manually configured through a user interface.

This discovery is only performed once, and the information is then persisted when the binding is restarted. On each restart the binding will perform an update of the information to read any dynamic data from the device.


### Thing States

Internally the binding holds a device state and these states are mapped to the system states ONLINE and OFFLINE. This section provides an explanation of the meaning of the states.

* ONLINE - A device is considered to be operating normally.
* DEAD - A device is considered DEAD if it does not respond to a message three times. This is a binding state only and while the binding will continue to attempt to contact a DEAD device retries to the node will be stopped until it responds. DEAD devices can slow down communications within the network so you are advised to remove DEAD devices from the network if possible. DEAD devices will be marked as OFFLINE within the system status.
* FAILED - A device is considered FAILED if the controller can not communicate with the device. The binding does not control this. FAILED devices are treated in a similar way to DEAD devices however the controller will reduce communications to the device and will timeout quicker. It should be noted that the controller will generally not consider battery devices as failed. FAILED devices will be marked as OFFLINE within the system status.


### Associations

Associations are used by Z-Wave devices to send commands from one device to another, independent of the controller. This could be used to turn on a light when a movement sensor is triggered without requiring the message from the movement sensor to be used to trigger a rule, and for the rule to send a message to turn on the light. Associations are also used to send state updates to the controller when the state of a device changes. For example, if you turn a light on, the device will let the controller know so that the state of the light is shown correctly within the user interface.

Often there is a *Lifeline* association group, and normally this is the only association that is required in order to notify the binding of changes to the device. If you set the controller node into other association groups, you will likely receive multiple notifications - while this shouldn't cause problems most of the time, it can reduce battery life in battery powered devices, and may cause issues with rules.

It should be noted that associations are only triggered from a local action within the device. Thus if a command is sent from one device to a second device, and the second device has an association to notify the controller when it changes state, this will not be sent to the controller in these circumstances.


### Battery Devices

Z-Wave battery devices require additional configuration in order for them to operate properly. In Z-Wave, most battery devices spend the majority of the time sleeping, and they only wake up very occasionally to allow commands to be sent to them. The binding therefore queues any messages to battery devices until they wake up - this can be every 10 minutes or so, or it could be once per day! All battery devices will have a a button, or some other way to wake the device up, and this can be useful while configuring the devices.

In order to configure the device properly following its initial inclusion in the network, the device must be woken up a number of times while close to the controller. During this time, the binding will read the device information, but will also configure some settings. The most important is to configure the wakeup period, and wakeup node - until this is done, the device will not wake up periodically, and if it is out of direct range of the controller, it will not be able to communicate with the controller.

A battery device will be considered *DEAD* if the controller does not receive a wakeup notification, or some other message, within approximately twice the wakeup period. In this event, the thing will be set offline and the device considered *DEAD*.


### Polling

The binding supports periodic polling. This has two purposes - firstly to ensure that a device is still responding, and secondly to update the bindings representation of the state of the device.  Where possible *associations* should be used to update the device state - this will keep network traffic to a minimum, which will improve the network latency, and it will be faster since *associations* send updates to the controller immediately where polling will always be noticeably slower.

If a device fails to respond to a poll, then it will be marked as DEAD and shown as offline. For battery devices, if they do no provide a wakeup within a period of twice the wakeup period, then they will also be considered dead and taken offline.

Keep the polling at a slow rate unless your device doesn't support *associations*. This will reduce network traffic, reduce the chance of timeouts and retries, and therefore improve the overall performance of the network.


### Binding Maintenance Functions

The binding provides a number of facilities for maintaining the network.


#### Mesh Heal

Sometimes the Z-Wave mesh can get messed up and nodes can become 'lost'. In theory, the Z-Wave controller should automatically resolve these issues, and any device that finds itself orphaned from the network should send a *Explorer Frames* to request a routing update.

In order to manually repair the mesh, the binding implements a *mesh heal* function. This will systematically work through the network nodes, starting with the controller and working outwards. For each node, the controller will request an update to the nodes neighbors - this can take up to a minute to complete foe each node, although it is normally much less. The neighbor update will only be performed on nodes that are *listening* - this means battery devices will not be updated through this process but they should be updated by the controller.

While the neighbor update is running, all nodes in the system will be taken offline to avoid network traffic that may adversely impact the update.

Once the neighbor update is complete, the system will perform a routing update on all nodes. Z-Wave is a "source routed mesh network" which means that the controller needs to tell the end nodes information about its routes. Specifically, the controller will provide each node a list of routes required to talk to the controller, the SUC (if it exists in the network), and other nodes to which the controller needs to talk to (eg for associated devices). The binding simply instructs the stick to configure a route between two nodes - the route itself if derived by the stick and the binding has no visibility of the actual routes being used.


#### Joining as a secondary controller

The binding can be added to the network as a secondary controller. This can be useful if you already have another home automation system and you want to use the binding as a secondary controller.


#### Network updates

A secondary controller can receive an updated list of network nodes from the primary controller...


### Z-Wave Device Database

A database of device information is required for Z-Wave since there is no way to know the devices configuration directly from the device. Some Z-Wave command classes have preset configuration, and these we can implicitly configure, however the configuration command class has no device specific declarations. This data is normally provided by the device manufacturer in the manual, or on their website.

The binding makes use of the database to derive device names, and provide a list of channels that are available on the device. If there is no database entry, then (currently) the device will show up as *Unknown* in the things list.

Devices are identified in the database by 4 pieces of information that are provided by the device during the initial discovery process. These are -:

* Manufacturer ID
* Device Type
* Device ID
* Firmware version

The primary identification is performed using the Manufacturer ID, Device Type and Device ID. Many devices use multiple deviceType and deviceId sets to identify different regions, or other minor differences, and some manufacturers will produce multiple firmware versions for the same device, so this information is also used in some instances.


#### Unknown Devices 

If the device is listed as *Unknown*, then the device has not been fully discovered by the binding and will not work correctly. There are a few possible reasons for this -:

* **The device is not in the database.** If the device attributes show that this device has a valid manufacturer ID, device ID and type, then this is likely the case (eg. you see a label like "*Z-Wave node 1 (0082:6015:020D::2.0)*"). Even if the device appears to be in the database, some manufacturers use multiple sets of references for different regions or versions, and your device references may not be in the database. In either case, the database must be updated and you should raise an issue to get this addressed.
* **The device initialisation is not complete.** Once the device is included into the network, the binding must interrogate it to find out what type of device it is. One part of this process is to get the manufacturer information required to identify the device, and until this is done, the device will remain unknown. For mains powered devices, this will occur quickly, however for battery devices the device must be woken up a number of  times to allow the discovery phase to complete. This must be performed with the device close to the controller.  


## Supported Things

## Things
<table>
<thead>
<th>Thing Type Id</th>
<th>Channels</th>
<th>Channel Groups</th>
<th>Config</th>
<th>Description</th>
</thead>
<tbody>
<tr>
<td><a name="thing-id-device"></a>device</td>
<td></td>
<td></td>
<td></td>
<td>&#10;&#9;&#9;  This device has not been fully discovered by the binding. There are a few possible reasons for this -:&#10;&#9;&#9;  &lt;ul&gt;&#10;&#9;&#9;  &lt;li&gt;&lt;b&gt;The device is not in the database.&lt;/b&gt; If the device attributes show that this device has a valid&#10;&#9;&#9;  manufacturer ID, device ID and type, then this is likely the case (eg. you see a label like &quot;&lt;i&gt;Z-Wave node 1&#10;&#9;&#9;  (0082:6015:020D::2.0)&lt;/i&gt;&quot;). Even if the device appears to be in the database, some manufacturers use multiple&#10;&#9;&#9;  sets of references for different regions or versions, and your device references may not be in the database.&#10;&#9;&#9;  In either case, the database must be updated and you should raise an issue to get this addressed.&#10;&#9;&#9;  &lt;li&gt;&lt;b&gt;The device initialisation is not complete.&lt;/b&gt; Once the device is included into the network, the binding&#10;&#9;&#9;  must interrogate it to find out what type of device it is. One part of this process is to get the manufacturer&#10;&#9;&#9;  information required to identify the device, and until this is done, the device will remain unknown. For mains&#10;&#9;&#9;  powered devices, this will occur quickly, however for battery devices the device must be woken up a number of&#10;&#9;&#9;  times to allow the discovery phase to complete. This must be performed with the device close to the controller.  &#10;&#9;&#9;  &lt;/ul&gt; &#10;&#9;&#9;</td>
</tr>
<tr>
<td><a name="thing-id-act_45602_00_000"></a>act_45602_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Lamp Module with Dimmer Control</td>
</tr>
<tr>
<td><a name="thing-id-act_zdp200_00_000"></a>act_zdp200_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>&#10;HomePro ZDP200 Wall Dimmer&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Use the button on the device.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Use Habmin or another zwave tool to exclude the device from the zwave mesh.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-act_zrp200_00_000"></a>act_zrp200_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;HomePro Applicance Module ZRP200&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;This is a very old appliance module, which doesn&#39;t report the manufacturer information. You&#39;ll have to edit the node.xml file(s) yourself. Change the manufacturer from 0x7fffffff to 0x1, to set it to manufacturer ACT. Also, change the deviceType to 7fff and the deviceid to 7fff.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Use the button on the device.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Use Habmin or another zwave tool to exclude the device from the zwave mesh.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_aeonzw120_00_000"></a>aeon_aeonzw120_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Door/Window sensor Gen5</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsa03202_00_000"></a>aeon_dsa03202_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Minimote 4 button remote control</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsb05_00_000"></a>aeon_dsb05_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>4 in One MultiSensor</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsb09_00_000"></a>aeon_dsb09_00_000</td>
<td>  <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-sensor_power1">sensor_power1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-sensor_power2">sensor_power2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a>,    <a href="#channel-id-sensor_power3">sensor_power3</a>,    <a href="#channel-id-meter_kwh3">meter_kwh3</a>,    <a href="#channel-id-meter_watts3">meter_watts3</a> </td>
<td></td>
<td></td>
<td>Home Energy Meter</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsb28_00_000"></a>aeon_dsb28_00_000</td>
<td>  <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_reset">meter_reset</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-meter_current1">meter_current1</a>,    <a href="#channel-id-meter_voltage1">meter_voltage1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a>,    <a href="#channel-id-meter_current2">meter_current2</a>,    <a href="#channel-id-meter_voltage2">meter_voltage2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-meter_watts3">meter_watts3</a>,    <a href="#channel-id-meter_voltage3">meter_voltage3</a>,    <a href="#channel-id-meter_current3">meter_current3</a>,    <a href="#channel-id-meter_kwh3">meter_kwh3</a> </td>
<td></td>
<td></td>
<td>Home Energy Meter G2</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsb29_00_000"></a>aeon_dsb29_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Door/Window sensor Gen2</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsb45_00_000"></a>aeon_dsb45_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Water Sensor&lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;Press WakeUp button or hold z-wave button&lt;/p&gt; &lt;p&gt;The Water Sensor will keep waking up for 8 seconds after sending the wake up notification command.&lt;br /&gt;The Water Sensor will keep waking up for 8 seconds to waiting for the next command after receiving a command.&lt;br /&gt;The Water Sensor will be woken up for 10 minutes when power is on (configurable).&lt;br /&gt;There are 3 ways to exit the Wake up 10 minutes state:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Triple click the tamper switch, and the Water Sensor will sleep immediately&lt;/li&gt; &lt;li&gt;Receive the “Wake up no more information CC” command , sleep right now;&lt;/li&gt; &lt;li&gt;Receive the other command except “Wake up no more information CC” , the Water Sensor will wake up for 8 seconds and then go to sleep.&lt;/li&gt; &lt;/ol&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsb54_00_000"></a>aeon_dsb54_00_000</td>
<td>  <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Recessed Door/Window Sensor</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc06_00_000"></a>aeon_dsc06_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>Smart Energy Switch</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc08_00_000"></a>aeon_dsc08_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>Smart Energy Illuminator</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc10_00_000"></a>aeon_dsc10_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Heavy Duty Smart Switch</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc11_00_000"></a>aeon_dsc11_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a>,    <a href="#channel-id-switch_binary3">switch_binary3</a>,    <a href="#channel-id-meter_kwh3">meter_kwh3</a>,    <a href="#channel-id-meter_watts3">meter_watts3</a>,    <a href="#channel-id-switch_binary4">switch_binary4</a>,    <a href="#channel-id-meter_kwh4">meter_kwh4</a>,    <a href="#channel-id-meter_watts4">meter_watts4</a>,    <a href="#channel-id-switch_binary5">switch_binary5</a>,    <a href="#channel-id-meter_kwh5">meter_kwh5</a>,    <a href="#channel-id-meter_watts5">meter_watts5</a>,    <a href="#channel-id-switch_binary6">switch_binary6</a>,    <a href="#channel-id-meter_kwh6">meter_kwh6</a>,    <a href="#channel-id-meter_watts6">meter_watts6</a> </td>
<td></td>
<td></td>
<td>&#10;Smart Strip&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Update command classes -:&lt;br /&gt;METER:5 :: ADD&lt;br /&gt;METER:6 :: ADD&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc12_00_000"></a>aeon_dsc12_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>Micro Smart Energy Switch</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc14_00_000"></a>aeon_dsc14_00_000</td>
<td></td>
<td></td>
<td></td>
<td>Micro Motor Controller</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc17_00_000"></a>aeon_dsc17_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>Micro Double Smart Switch</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc18_00_000"></a>aeon_dsc18_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_reset">meter_reset</a> </td>
<td></td>
<td></td>
<td>Micro Smart Energy Switch G2</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc19_00_000"></a>aeon_dsc19_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_current">meter_current</a> </td>
<td></td>
<td></td>
<td>Micro Smart Energy Illuminator G2</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc24_00_000"></a>aeon_dsc24_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a> </td>
<td></td>
<td></td>
<td>Smart Energy Switch G2</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc25_00_000"></a>aeon_dsc25_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Smart Energy Illuminator G2</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc26_00_000"></a>aeon_dsc26_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Micro Switch G2</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsc27_00_000"></a>aeon_dsc27_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Micro Illuminator G2</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsd31_00_000"></a>aeon_dsd31_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Outlet Plugable Siren</td>
</tr>
<tr>
<td><a name="thing-id-aeon_dsd37_00_000"></a>aeon_dsd37_00_000</td>
<td></td>
<td></td>
<td></td>
<td>Range Extender</td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw056_00_000"></a>aeon_zw056_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;Doorbell&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Aeon Labs Doorbell is a switch binary device based on Z-wave enhanced slave library V6.51.06.&lt;/p&gt; &lt;p&gt;The Doorbell supports playing MP3 music files with a press of this doorbell. It has a 16MB flash memory that can store up to 100 ringtones. The volume can be adjusted manually via short press on the Volume Button, also you can switch the doorbell sound to the next via long press on the Volume Button. You may change/update your doorbell ringtone at any point in time you want by connecting your Doorbell to your PC to update the sound track on your Doorbell’s flash memory. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;1. Install Doorbell, and plug it into the socket of AC Power.&lt;br /&gt;2. Let the primary controller into inclusion mode (If you don’t know how to do this, please refer to its manual).&lt;br /&gt;3. Press the Action Button.&lt;br /&gt;4. If the inclusion is failed, please repeat the process from step 2. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;1. Install Doorbell, and plug it into the socket of AC Power.&lt;br /&gt;2. Let the primary controller into exclusion mode (If you don’t know how to do this, refer to its manual).&lt;br /&gt;3. Press the Action Button.&lt;br /&gt;4. If the remove is failed, please repeat the process from step 2.&lt;/p&gt; &lt;p&gt;Note: If Doorbell is removed from Z-wave network, it will be reset to factory default. &lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw062_00_000"></a>aeon_zw062_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-barrier_state">barrier_state</a> </td>
<td></td>
<td></td>
<td>&#10;Aeon Labs Garage Door Controller Gen5 &lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Aeon Labs Garage Door Controller is a smart and wireless Garage Door Control system, you can control the garage door to open, close, or stop moving via wireless signal on your gateway client or phone application.&lt;/p&gt; &lt;p&gt;The Garage Door Controller allows you to configure different alarm sounds to indicate the door ’s action.&lt;/p&gt; &lt;p&gt;Each action alarm sound can be customized. To change or update new alarm sounds for the Garage Door Controller, connect the Garage Door Controller to your PC host with a USB cable and download your sound files to the flash memory (128 MB) of the Garage Door Controller.&lt;/p&gt; &lt;p&gt;It can be included and operated in any Z-wave network with other Z-wave certified devices from other manufacturers and/or other applications. All non-battery operated nodes within the network will act as repeaters regardless of vendor to increase reliability of the network.&lt;/p&gt; &lt;p&gt;It is also a security Z-wave device and supports the Over The Air (OTA) feature for the product’s firmware upgrade.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Add Garage Door Controller into Z-Wave Network:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Install Garage Door Controller, and connect it to the 5V DC Adapter.&lt;/li&gt; &lt;li&gt;Let the primary controller into inclusion mode (If you don’t know how to do this, please refer to its manual).&lt;/li&gt; &lt;li&gt;Press the Z-Wave Button.&lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Remove Garage Door Controller from Z-Wave Network:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Install Garage Door Controller, and connect it to the 5V DC Adapter.&lt;/li&gt; &lt;li&gt;Let the primary controller into exclusion mode (If you don’t know how to do this, refer to its manual).&lt;/li&gt; &lt;li&gt;Press the Z-Wave Button.&lt;/li&gt; &lt;li&gt;If the remove is failed, please repeat the process from step 2.&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;Note: If Garage Door Controller is removed from Z-wave network, it will be reset to factory default.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw074_00_000"></a>aeon_zw074_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>4 in One MultiSensor (G5)</td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw075_00_000"></a>aeon_zw075_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Smart Energy Switch 3rd Edition</td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw078_00_000"></a>aeon_zw078_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>Heavy Duty Switch</td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw080_00_000"></a>aeon_zw080_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;Siren Gen5&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Update command classes -:&lt;br /&gt;METER:5 :: ADD&lt;br /&gt;METER:6 :: ADD&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;1. Install Siren, and plug it into the socket of AC Power.&lt;br /&gt;2. Let the primary controller into inclusion mode (If you don’t know how to do this, please refer to its manual).&lt;br /&gt;3. Press the Action Button.&lt;br /&gt;4. If the Learning is failed, please repeat the process from step 2.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;1. Install Siren, and plug it into the socket of AC Power.&lt;br /&gt;2. Let the primary controller into exclusion mode (If you don’t know how to do this, refer to its manual). 3. Press the Action Button.&lt;br /&gt;4. If the remove is failed, please repeat the process from step 2.&lt;/p&gt; &lt;p&gt;Note: If Siren is removed from Z‐wave network, it will be reset to factory default.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw088_01_000"></a>aeon_zw088_01_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;4 button keyfob - Gen 5&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Key Fob Gen5 is a fully functional Z-wave remote controller, which can include, exclude and control the other Z-Wave certified devices. It also can be acted a secondary controller in a network.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Short press the “Include” button on the Key Fob, it will go into inclusion state and its blue LED will blink slowly to wait including other slave Z-Wave devices.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw089_00_000"></a>aeon_zw089_00_000</td>
<td>  <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Recessed Door Sensor Gen5&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Aeon Labs Recessed Door Sensor Gen5 is a door detector that can detect the state of the door&#39;s open/close.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;ol&gt;&lt;li&gt;Power on the Recessed Door Sensor Gen5.&lt;/li&gt; &lt;li&gt;Let the primary controller into inclusion mode (If you don’t know how to do this, refer to its manual).&lt;/li&gt; &lt;li&gt;Press the Z‐Wave button.&lt;/li&gt; &lt;li&gt;If the inclusion is success, Recessed Door Sensor Gen5’s LED will be kept turning on for 10 minutes. If the LED still blinks slowly, in which you need to repeat the process from step 2.  &lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;ol&gt;&lt;li&gt;Power on the Recessed Door Sensor Gen5.&lt;/li&gt; &lt;li&gt;Let the primary controller into exclusion mode (If you don’t know how to do this, refer to its manual).&lt;/li&gt; &lt;li&gt;Press the Z‐Wave button.&lt;/li&gt; &lt;li&gt;If the exclusion is success, Recessed Door Sensor Gen5’s LED will blink slowly. If LED still be solid status for 3 seconds after you short press the Z‐Wave button, in which you need to repeat the process from step 2&lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;Press and hold the Z‐wave Button for 6 seconds. It will sleep after you released the z‐wave button for 10 seconds, or sleep right away when received the Wake Up No More Information and then the led will turn off.   &lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw095_00_000"></a>aeon_zw095_00_000</td>
<td>  <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_current1">meter_current1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_voltage1">meter_voltage1</a>,    <a href="#channel-id-meter_current2">meter_current2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-meter_voltage2">meter_voltage2</a> </td>
<td></td>
<td></td>
<td>Home Energy Meter - Gen 5</td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw096_00_000"></a>aeon_zw096_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_reset">meter_reset</a> </td>
<td></td>
<td></td>
<td>&#10;Smart Switch 6&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Aeon Labs Smart Switch is a Z-Wave power binary switch device based on Z-Wave enhanced 232 slave library V6.51.06.&lt;/p&gt; &lt;p&gt;Its surface has the Smart RGB LEDs on, which can be used for indicating the output load status, the strength of wireless signal. You can also configure its indication colour according to your favour.&lt;/p&gt; &lt;p&gt;It can be included and operated in any Z-wave network with other Z-wave certified devices from other manufacturers and/or other applications. All non-battery operated nodes within the network will act as repeaters regardless of vendor to increase reliability of the network.&lt;/p&gt; &lt;p&gt;It is also a security Z-wave device and supports the Over The Air (OTA) feature for the product’s firmware upgrade.&lt;br /&gt;As soon as Smart Switch is removed from a z-wave network it will be restored into default factory setting.&lt;/p&gt; &lt;p&gt;Reset Smart Switch to factory Default:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Make sure the Smart Switch has been connected to the power supply.&lt;/li&gt; &lt;li&gt;Press and hold the Z-wave button for 20 seconds.&lt;/li&gt; &lt;li&gt;If holding time more than one second, the LED will blink faster and faster. If holding time more than 20seconds, the green LED will be on for 2 seconds and then remain colorful gradient status, it indicates reset success, otherwise please repeat step 2.&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;Note:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;This procedure should only be used when the primary controller is inoperable.&lt;/li&gt; &lt;li&gt;Reset Smart Switch to factory default settings will: sets the Smart Switch to not in Z-Wave network state; delete the Association setting, power measure value, Scene Configuration Settings and restore the Configuration setting to the default&lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Add Smart Switch into a z-wave network:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Insert the Smart Switch to power socket, The purple LED will be colorful gradient status.&lt;/li&gt; &lt;li&gt;Let the primary controller into inclusion mode (If you don’t know how to do this, refer to its manual).&lt;/li&gt; &lt;li&gt;Press the Action button.&lt;/li&gt; &lt;li&gt;If the inclusion success, Smart Switch LED will be solid. Otherwise, the LED will remain colorful gradient status, in which you need to repeat the process from step 2.&lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Remove Smart Switch from a z-wave network:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Insert the Smart Switch to power socket, The Smart Switch LED will be solid.&lt;/li&gt; &lt;li&gt;Let the primary controller into remove mode (If you don’t know how to do this, refer to its manual).&lt;/li&gt; &lt;li&gt;Press the Action button.&lt;/li&gt; &lt;li&gt;If the remove is successful, Smart Switch LED will blink slowly. If Smart Switch LED still be solid, please repeat the process from step 2.&lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;mains device&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw097_00_000"></a>aeon_zw097_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Dry Contact Sensor</td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw098_00_000"></a>aeon_zw098_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-color_color">color_color</a>,    <a href="#channel-id-color_temperature">color_temperature</a> </td>
<td></td>
<td></td>
<td>&#10;LED Bulb&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Non-secure inclusion:&lt;br /&gt;1. Power on your LED Bulb.&lt;br /&gt;2. Set the primary controller into inclusion mode (If you don’t know how to do this, please refer to its manual).&lt;br /&gt;3. Turn off the LED Bulb and then turn on.&lt;br /&gt;4. If the inclusion is failed, please repeat the process from step 2. &lt;/p&gt; &lt;p&gt;Secure inclusion:&lt;br /&gt;1. Power on your LED Bulb.&lt;br /&gt;2. Set the primary controller into inclusion mode (If you don’t know how to do this, please refer to its manual).&lt;br /&gt;3. Turn off the LED Bulb and then turn on it on 3 times within 2 seconds.&lt;br /&gt;4. If the inclusion fails, please repeat the process from step 2.&lt;/p&gt; &lt;p&gt;Note: If LED Bulb has been successfully included into your Z-Wave network, its warm white LED will be solid. If the linking was unsuccessful and the LED Bulb continues to be active with a colourful gradient. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;1. Power on your LED Bulb.&lt;br /&gt;2. Set the primary controller into exclusion mode (If you don’t know how to do this, please refer to its manual).&lt;br /&gt;3. Turn off the LED Bulb and then turn on 3 times within 2 seconds.&lt;br /&gt;4. If the exclusion fails, please repeat the process from step 2.&lt;/p&gt; &lt;p&gt;Note: If LED Bulb has been successfully excluded from your Z-Wave network, its warm white LED will be active with a colourful gradient. If the exclusion was unsuccessful and the LED Bulb continues to be solid. &lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw099_00_000"></a>aeon_zw099_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>&#10;Smart Dimmer 6&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Turn the primary controller of Z-Wave network into inclusion mode, short press the product’s Action button that you can find on the product&#39;s housing.&lt;/p&gt; &lt;p&gt;Endpoint 1 is used to Set/Get the state of output load.&lt;/p&gt; &lt;p&gt;Endpoint 2 is used to Set/Get the brightness level of RGB LED when it is in Night light mode. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;1. Insert the Smart Dimmer to power socket, The RGB LED will be colorful gradient status.&lt;br /&gt;2. Let the primary controller into inclusion mode (If you don’t know how to do this, refer to its manual).&lt;br /&gt;3. Press the Action button.&lt;br /&gt;4. If the inclusion success, Smart Dimmer LED will be solid. Otherwise, the LED will remain colorful gradient status, in which you need to repeat the process from step 2. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;1. Insert the Smart Dimmer to power socket, The Smart Dimmer LED will be solid.&lt;br /&gt;2. Let the primary controller into remove mode (If you don’t know how to do this, refer to its manual).&lt;br /&gt;3. Press the Action button.&lt;br /&gt;4. If the remove is successful, Smart Dimmer LED will be colorful gradient status. If Smart Dimmer LED still be solid, please repeat the process from step 2.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw100_00_000"></a>aeon_zw100_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-sensor_ultraviolet">sensor_ultraviolet</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>MultiSensor 6</td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw100_01_007"></a>aeon_zw100_01_007</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-sensor_ultraviolet">sensor_ultraviolet</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;MultiSensor 6&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Technical Specifications: Operating distance:  Up to 500 feet/150 meters outdoors   Operating temperature: 0°C to 40°C   Relative humidity: 8% RH to 80% RH       Function of Z_Wave Button: Click one time:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Send non-security Node Info Frame&lt;/li&gt; &lt;li&gt;Add MultiSensor into Z-wave network (non security inclusion)&lt;/li&gt; &lt;li&gt;Remove MultiSensor from Z-wave network&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;  Short press 2 times within 1 second:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Send Security Node Info Frame&lt;/li&gt; &lt;li&gt;Add Multisensor into Z-Wave network (security inclusion)&lt;/li&gt; &lt;li&gt;Remove MultiSensor from network&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;  Press and hold for 3 seconds:&lt;/p&gt; &lt;p&gt;Enable/Disable wake up for 10 minutes.&lt;/p&gt; &lt;p&gt;(when it is enabled, the orange Led will fast blink)&lt;/p&gt; &lt;p&gt;  Press and hold for 20 seconds:&lt;/p&gt; &lt;p&gt;Reset MultiSensor to factory default (see documentation)&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;see &quot;Device Overview&quot;&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;see &quot;Device Overview&quot;&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw100_01_008"></a>aeon_zw100_01_008</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_ultraviolet">sensor_ultraviolet</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>MultiSensor 6</td>
</tr>
<tr>
<td><a name="thing-id-aeon_zw112_00_000"></a>aeon_zw112_00_000</td>
<td>  <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Door/Window Sensor 6&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Aeon Labs Door/ Window Sensor 6 is a smart Z-Wave sensor that can detect the status of door/window&#39;s open/close in real time. &lt;br /&gt;It’s a security Z-Wave device that supports security encryption. Also it supports the “Over the Air Firmware Updating” that allows you wirelessly update its firmware if needs. &lt;br /&gt;It can be included and operated in any Z-Wave network with other Z-Wave certified devices from manufacturers and/or other applications.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Turn the primary controller into inclusion mode, short press the product’s Action Button that you can find in the back of the product. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Turn the primary controller into exclusion mode, short press the product’s Action Button that you can find in back of the product. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;Press the Action Button once, which will trigger sending out the Wake up notification command, press and hold the Action Button for 3 seconds, which will toggle on/off the Sensor be waked up for 10 minutes. &lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-benext_molite_00_000"></a>benext_molite_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Movement sensor with temperature and light sensor</td>
</tr>
<tr>
<td><a name="thing-id-benext_p1dongle_00_000"></a>benext_p1dongle_00_000</td>
<td>  <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a>,    <a href="#channel-id-meter_gas_cubic_meters3">meter_gas_cubic_meters3</a> </td>
<td></td>
<td></td>
<td>&#10;P1-dongle&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The P1-dongle sends smart meter data wirelessly to your controller, helping you get insight of your consumption and things like the overall yield of your solar panels. The P1-dongle can connect to smart meters that support DSMR (Dutch Smart Meter Reader Protocol) eg. all smart meters with a P1-connector.&lt;/p&gt; &lt;p&gt;LED Status Information&lt;/p&gt; &lt;ul&gt;&lt;li&gt;Ready to install: LED pulse once per second&lt;/li&gt; &lt;li&gt;Including: LED pulse twice per second&lt;/li&gt; &lt;li&gt;Excluding: LED pulse three times per 1.5 second&lt;/li&gt; &lt;li&gt;Include Successful: LED on for one second&lt;/li&gt; &lt;li&gt;Ready and operating in a Z-Wave network: LED continuously on&lt;/li&gt; &lt;li&gt;No ready and not included in a Z-Wave network: LED pulse once per second&lt;/li&gt; &lt;/ul&gt;&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;ul&gt;&lt;li&gt;Put you controller in inclusion mode&lt;/li&gt; &lt;li&gt;Connect the P1 Dongle to your Smart Meter (use a power source when needed)&lt;/li&gt; &lt;li&gt;The P1 Dongle will switch to Network Wide Inclusion automatically&lt;/li&gt; &lt;/ul&gt;&lt;p&gt;Use the button to switch the P1 Dongle to Inclusion (LED pulse twice per second) or Exclusion / Reset (LED pulse three times per 1.5 second) mode&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;ul&gt;&lt;li&gt;Put your controller and the P1 Dongle in exclusion mode (LED pulse three times per 1.5 second)&lt;/li&gt; &lt;li&gt;The dongle will exclude from it&#39;s former network and reset to node ID zero &lt;/li&gt; &lt;li&gt;If the P1 Dongle wasn&#39;t included in a network before, this procedure will act as a node reset&lt;/li&gt; &lt;/ul&gt;&lt;p&gt;Use the button to switch the P1 Dongle to Inclusion (LED pulse twice per second) or Exclusion / Reset (LED pulse three times per 1.5 second) mode&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-benext_plugindimmer_00_000"></a>benext_plugindimmer_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>BeNext Plug-in Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-brk_zcombo_00_000"></a>brk_zcombo_00_000</td>
<td>  <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Smoke and Carbon Monoxide Alarm</td>
</tr>
<tr>
<td><a name="thing-id-brk_zsmoke_00_000"></a>brk_zsmoke_00_000</td>
<td>  <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Smoke Alarm</td>
</tr>
<tr>
<td><a name="thing-id-chromagic_hsm02_00_000"></a>chromagic_hsm02_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Door Window Sensor</td>
</tr>
<tr>
<td><a name="thing-id-cooper_rf9500_00_000"></a>cooper_rf9500_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Battery Switch</td>
</tr>
<tr>
<td><a name="thing-id-cooper_rf9501_00_000"></a>cooper_rf9501_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>15A Light Switch</td>
</tr>
<tr>
<td><a name="thing-id-cooper_rf9517_00_000"></a>cooper_rf9517_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>&#10;Accessory Switch&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Aspire RF Accessory Switch Compatible with Aspire RF switch (RF9501, RF9518) for wireless 3-way control eliminating the need for traditional 3-way wiring (up to 5 locations)&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;switch will have blinking blue LED when first powered on initiate inclusion mode on controller device press the RF9517 button once RF9517 blue LED should turn solid indicating inclusion.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;initiate exclusion mode on controller device press the RF9517 button once RF9517 blue LED should begin blinking indicating inclusion.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-cooper_rf9540n_00_000"></a>cooper_rf9540n_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>All Load Dimmer Light Switch</td>
</tr>
<tr>
<td><a name="thing-id-cooper_rf9542_00_000"></a>cooper_rf9542_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Dimmer Accessory Switch</td>
</tr>
<tr>
<td><a name="thing-id-cooper_rftr9505_00_000"></a>cooper_rftr9505_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Duplex receptical</td>
</tr>
<tr>
<td><a name="thing-id-cooper_rfwc5_00_000"></a>cooper_rfwc5_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>5-Scene Keypad</td>
</tr>
<tr>
<td><a name="thing-id-danfoss_014g0160_00_000"></a>danfoss_014g0160_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Z-Wave room sensor&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Push one time LED Button&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Push one time LED Button&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;Any button (LED, Temp+ or Temp-)&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-danfoss_lc13_00_000"></a>danfoss_lc13_00_000</td>
<td>  <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-time_offset">time_offset</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>&#10;Living Connect Z Thermostat&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Radiator Thermostat&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Short push middle button&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;Short push middle button&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-danfoss_lcz251_00_000"></a>danfoss_lcz251_00_000</td>
<td>  <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Living Connect Z Thermostat 2.51</td>
</tr>
<tr>
<td><a name="thing-id-danfoss_mt02650_00_000"></a>danfoss_mt02650_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Devolo Thermostat (09356)&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Radiator Thermostat&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Short push middle button&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;Short push middle button&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-devolo_mt02646_00_000"></a>devolo_mt02646_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_powerfactor">meter_powerfactor</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_reset">meter_reset</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Home Control Metering Plug</td>
</tr>
<tr>
<td><a name="thing-id-devolo_mt02647_00_000"></a>devolo_mt02647_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-lock_door">lock_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Devolo Motion Sensor</td>
</tr>
<tr>
<td><a name="thing-id-devolo_mt02648_00_000"></a>devolo_mt02648_00_000</td>
<td>  <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Door/Window Contact</td>
</tr>
<tr>
<td><a name="thing-id-devolo_mt2653_00_000"></a>devolo_mt2653_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Keyfob</td>
</tr>
<tr>
<td><a name="thing-id-dlink_dchz110_00_000"></a>dlink_dchz110_00_000</td>
<td>  <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Door &amp; Window Sensor</td>
</tr>
<tr>
<td><a name="thing-id-dlink_dchz120_00_000"></a>dlink_dchz120_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Battery Motion Sensor</td>
</tr>
<tr>
<td><a name="thing-id-dlink_dchz510_00_000"></a>dlink_dchz510_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;Siren&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;ol&gt;&lt;li&gt;Have Z-Wave Controller enter inclusion mode.&lt;/li&gt; &lt;li&gt;Pressing tamper key three times within 1.5 seconds to enter the inclusion mode.&lt;/li&gt; &lt;li&gt;After add successful, the LED will light ON 1 second &lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;ol&gt;&lt;li&gt;Have Z-Wave Controller enter exclusion mode.&lt;/li&gt; &lt;li&gt;Pressing tamper key three times within 1.5 seconds to enter the exclusion mode.&lt;/li&gt; &lt;li&gt;Node ID has been excluded. &lt;/li&gt; &lt;/ol&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-domitech_zb22_00_000"></a>domitech_zb22_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Smart LED Light Bulb</td>
</tr>
<tr>
<td><a name="thing-id-domitech_ze27_00_000"></a>domitech_ze27_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>&#10;Smart LED Light&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Domitech Smart LED Retrofit Kit ZE27EU is a member of the Z-Wave® family and communicate with other Z-Wave® certified devices in a control network. The Smart Bulb can be controlled by the Z-Wave remote controller or apps. Each Z-Wave device serves as a node to repeat the signal in the network, thus, extending the overall Z-Wave mesh wireless network range. Different types and brands of Z-Wave devices can be associated with domitech Smart Bulb in your system and they will work together to optimize and expand the coverage of your Z-Wave network. Once setup is completed, you can enjoy the convenience and leisure which Smart Bulb offers. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Step 1: Place the controller in inclusion mode.&lt;/p&gt; &lt;p&gt;Step 2: Turn on power to the socket after promoted by your network controller inclusion mode. The Smart Bulb will be included into your network within 30 seconds. The smart bulb will flash 2 times after Inclusion is successful. If the controller/gateway shows it failed, repeat the procedure.&lt;/p&gt; &lt;p&gt;Note: If Inclusion still fails after the 2nd attempt, you need to first RESET the Smart Bulb before repeating the above steps.&lt;/p&gt; &lt;p&gt;Before repeating the above steps, try moving the Smart Bulb to the bulb socket in the same room as your home gateway in case the preferred outlet is out of range initially. Repeat step 1-2 until the Smart Bulb is added to your network. Once the light bulb has been successfully added to your home network, you can move it to the preferred location in the home.&lt;/p&gt; &lt;p&gt;Manually Reset – Flick the wall switch “OFF-ON” cycle 4 times within 4 seconds (Turn OFF and turn ON will be counted as 1 “OFF-ON” cycle). The Smart Bulb will flash twice after Reset is successful. Use this procedure only in the event that the network primary controller is lost or otherwise inoperable.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;&lt;strong&gt;By controller&lt;/strong&gt; - The Smart Bulb can be excluded from your network by your controller/Gateway. Similar to the Inclusion process, turn off power to your light bulb and place your network controller into exclusion mode by following the controller manufacturer&#39;s instructions. Once prompted by your network controller, turn ON the light switch. The Smart Bulb will flash twice to confirm that it has successfully been excluded from your network. Please refer to your controller/gateway instructions manual for details.&lt;/p&gt; &lt;p&gt;&lt;strong&gt;Manually&lt;/strong&gt; – The Smart Bulb can be excluded manually by flicking the wall switch ”OFF-ON” cycle 4 times within 4 seconds (Turn OFF and turn ON will be counted as 1 “OFF-ON” cycle). The bulb will flash twice after Reset is successful. Use this procedure only in the event that the network primary controller is lost or otherwise inoperable.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-dragontech_wd100_00_000"></a>dragontech_wd100_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Wall Dimmer Switch</td>
</tr>
<tr>
<td><a name="thing-id-dragontech_ws100_00_000"></a>dragontech_ws100_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Wall On/Off Switch</td>
</tr>
<tr>
<td><a name="thing-id-eco_dwzwave2_00_000"></a>eco_dwzwave2_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Z-Wave Door/Window Sensor</td>
</tr>
<tr>
<td><a name="thing-id-eco_pir1_00_000"></a>eco_pir1_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Z-Wave PIR Motion Sensor&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The sensor has two Association groups available for up to five Z-Wave Node IDs each.&lt;br /&gt;Association group one is intended for but not limited to controllers. All nodes whose Node ID&lt;br /&gt;that have been set in Association group one will receive all unsolicited Alarm Report frames,&lt;br /&gt;and Basic Report frames of 0x00 and 0xFF. Association group two is intended for any device&lt;/p&gt; &lt;p&gt;that is controllable with a Basic Set of 0xFF such as lights, sirens, or chimes.&lt;br /&gt;When a sensor such as a door or window sensor is opened/faulted, the sensor will send a Basic&lt;br /&gt;Report to nodes in Association group one and a Basic Set of 0xFF to Association group two.&lt;br /&gt;When the door or window is closed the sensor will send a Basic Report of 0x00 to only group&lt;br /&gt;one. The Configuration command class can configure the sensor to send Basic Sets of 0x00 to&lt;br /&gt;nodes in group two (turning devices off). See Configuration Command Class.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;By default, a sensor is configured to send Wake Up Notification frames every three hours. A&lt;br /&gt;controller may change the duration between Wake Up Notification frames to be between one&lt;br /&gt;hour and one week in increments of two hundred seconds with the Wake Up Interval Set&lt;br /&gt;command. The sensor will send Wake Up Notification frames to the Node ID specified in the&lt;br /&gt;Wake Up Set Interval command or 255 if no valid nodes have been set.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-eco_tiltzwave2_00_000"></a>eco_tiltzwave2_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Z-Wave Garage Door Sensor</td>
</tr>
<tr>
<td><a name="thing-id-econet_ebv105_00_000"></a>econet_ebv105_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;Wireless Water Shutoff Valve&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The EBV105 water shutoff controller is easy to install and configure. No special tools or plumbing required. The included clamp supports installation &lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-enerwave_zw20r_00_000"></a>enerwave_zw20r_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;20A Tamper Resistant Duplex Receptacle&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;When the controller is in inclusion mode and the blue LED blinks on the&lt;br /&gt;ZW20R, press the program button of ZW20R, and then the controller will&lt;br /&gt;verify the inclusion&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;When the controller is in exclusion mode, press the&lt;br /&gt;program button of ZW20R, and then the controller will remove it from the&lt;br /&gt;current Z-Wave network, and the LED will blink on the receptacle.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;mains device&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-enerwave_zw20rm_00_000"></a>enerwave_zw20rm_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>In-wall Smart Meter Duplex Receptacle</td>
</tr>
<tr>
<td><a name="thing-id-enerwave_zw500d_00_000"></a>enerwave_zw500d_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-enerwave_zw500dm_00_000"></a>enerwave_zw500dm_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>In-wall Smart Meter Dimmer Switch</td>
</tr>
<tr>
<td><a name="thing-id-enerwave_zwn333_00_000"></a>enerwave_zwn333_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Plug-in Appliance Module</td>
</tr>
<tr>
<td><a name="thing-id-enerwave_zwnbpc_00_000"></a>enerwave_zwnbpc_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>PIR Sensor</td>
</tr>
<tr>
<td><a name="thing-id-eurotronic_cometz_00_000"></a>eurotronic_cometz_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_setpoint_furnace">thermostat_setpoint_furnace</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Thermostatic Valve</td>
</tr>
<tr>
<td><a name="thing-id-eurotronic_stellaz_00_000"></a>eurotronic_stellaz_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_setpoint_furnace">thermostat_setpoint_furnace</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Thermostatic Valve</td>
</tr>
<tr>
<td><a name="thing-id-everspring135_st812_00_000"></a>everspring135_st812_00_000</td>
<td>  <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_flood">alarm_flood</a> </td>
<td></td>
<td></td>
<td>Flood sensor</td>
</tr>
<tr>
<td><a name="thing-id-everspring_ad131_00_000"></a>everspring_ad131_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Dimmer Plugin</td>
</tr>
<tr>
<td><a name="thing-id-everspring_ad147_00_000"></a>everspring_ad147_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Z-Wave Dimmer Plug</td>
</tr>
<tr>
<td><a name="thing-id-everspring_an157_00_000"></a>everspring_an157_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Switch Plugin</td>
</tr>
<tr>
<td><a name="thing-id-everspring_an158_00_000"></a>everspring_an158_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Switch Meter Plugin</td>
</tr>
<tr>
<td><a name="thing-id-everspring_an181_00_000"></a>everspring_an181_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_powerfactor">meter_powerfactor</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>&#10;Mini Plug Switch with Metering (Gen5)&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The Metering Plug is designed to control the on/off status appliances load in your house. For metering the unit can detect up to 10485.75kW*h and can support wattage, voltage, ampere, and PF detection. The unit can also detect overload upon which the unit will switch off relay and keep LED flashing until power is off and re-applied. At 220-240V voltage, this Plug can support up to 2500W resistive. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Press the link key three times within 1.5 seconds to put the unit into inclusion mode.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Press the link key three times within 1.5 seconds to put the unit into exclusion mode.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-everspring_hac01_00_000"></a>everspring_hac01_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>In-Wall Remote Insert</td>
</tr>
<tr>
<td><a name="thing-id-everspring_hsm02_00_000"></a>everspring_hsm02_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Door/Window Contact</td>
</tr>
<tr>
<td><a name="thing-id-everspring_se812_00_000"></a>everspring_se812_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Siren</td>
</tr>
<tr>
<td><a name="thing-id-everspring_sf812_00_000"></a>everspring_sf812_00_000</td>
<td>  <a href="#channel-id-alarm_smoke">alarm_smoke</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_smoke">alarm_smoke</a> </td>
<td></td>
<td></td>
<td>Smoke Sensor</td>
</tr>
<tr>
<td><a name="thing-id-everspring_sm103_00_000"></a>everspring_sm103_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Door/Window Contact</td>
</tr>
<tr>
<td><a name="thing-id-everspring_sp103_00_000"></a>everspring_sp103_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Motion Detector</td>
</tr>
<tr>
<td><a name="thing-id-everspring_sp814_00_000"></a>everspring_sp814_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Motion Detector</td>
</tr>
<tr>
<td><a name="thing-id-everspring_st812_00_000"></a>everspring_st812_00_000</td>
<td>  <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_flood">alarm_flood</a> </td>
<td></td>
<td></td>
<td>Flood Sensor</td>
</tr>
<tr>
<td><a name="thing-id-everspring_st814_00_000"></a>everspring_st814_00_000</td>
<td>  <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_co2">alarm_co2</a>,    <a href="#channel-id-alarm_smoke">alarm_smoke</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-sensor_temperature1">sensor_temperature1</a>,    <a href="#channel-id-sensor_relhumidity2">sensor_relhumidity2</a> </td>
<td></td>
<td></td>
<td>Temperature and Humidity Sensor</td>
</tr>
<tr>
<td><a name="thing-id-everspring_st815_00_000"></a>everspring_st815_00_000</td>
<td></td>
<td></td>
<td></td>
<td>Illumination Sensor</td>
</tr>
<tr>
<td><a name="thing-id-evolve_lfm20_00_000"></a>evolve_lfm20_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td></td>
</tr>
<tr>
<td><a name="thing-id-evolve_lrmas_00_000"></a>evolve_lrmas_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Wall Mounted Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-evolve_lsm15_00_000"></a>evolve_lsm15_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Wall Mounted Switch</td>
</tr>
<tr>
<td><a name="thing-id-fakro_zws12_00_000"></a>fakro_zws12_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-blinds_control">blinds_control</a> </td>
<td></td>
<td></td>
<td>Chain Actuator</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgbs001_00_000"></a>fibaro_fgbs001_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_binary1">sensor_binary1</a>,    <a href="#channel-id-alarm_general1">alarm_general1</a>,    <a href="#channel-id-sensor_binary2">sensor_binary2</a>,    <a href="#channel-id-alarm_general2">alarm_general2</a>,    <a href="#channel-id-sensor_temperature3">sensor_temperature3</a>,    <a href="#channel-id-sensor_temperature4">sensor_temperature4</a>,    <a href="#channel-id-sensor_temperature5">sensor_temperature5</a>,    <a href="#channel-id-sensor_temperature6">sensor_temperature6</a> </td>
<td></td>
<td></td>
<td>Universal Binary Sensor</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgcc001_00_000"></a>fibaro_fgcc001_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Fibaro Swipe Scene Controller</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgd211_00_000"></a>fibaro_fgd211_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Universal Dimmer 500W</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgd211_01_009"></a>fibaro_fgd211_01_009</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Universal Dimmer 500W</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgd211_02_001"></a>fibaro_fgd211_02_001</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Universal Dimmer 500W</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgd212_00_000"></a>fibaro_fgd212_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-switch_dimmer1">switch_dimmer1</a>,    <a href="#channel-id-sensor_power1">sensor_power1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-alarm_general1">alarm_general1</a>,    <a href="#channel-id-switch_dimmer2">switch_dimmer2</a> </td>
<td></td>
<td></td>
<td>Dimmer 2</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgfs101_00_000"></a>fibaro_fgfs101_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-sensor_flood">sensor_flood</a>,    <a href="#channel-id-sensor_binary1">sensor_binary1</a>,    <a href="#channel-id-sensor_temperature2">sensor_temperature2</a> </td>
<td></td>
<td></td>
<td>Flood Sensor</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgfs101_25_025"></a>fibaro_fgfs101_25_025</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-alarm_flood">alarm_flood</a>,    <a href="#channel-id-sensor_binary1">sensor_binary1</a>,    <a href="#channel-id-sensor_temperature2">sensor_temperature2</a> </td>
<td></td>
<td></td>
<td>Flood Sensor</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgfs101_03_002"></a>fibaro_fgfs101_03_002</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-alarm_flood">alarm_flood</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_flood">alarm_flood</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Flood Sensor</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgk101_00_000"></a>fibaro_fgk101_00_000</td>
<td>  <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_flood">alarm_flood</a>,    <a href="#channel-id-alarm_co">alarm_co</a>,    <a href="#channel-id-alarm_smoke">alarm_smoke</a>,    <a href="#channel-id-alarm_co2">alarm_co2</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-alarm_heat">alarm_heat</a>,    <a href="#channel-id-sensor_temperature2">sensor_temperature2</a> </td>
<td></td>
<td></td>
<td>&#10;Door Opening Sensor&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;&lt;strong&gt;Prepare the sensor:&lt;/strong&gt;&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Open the sensor with a small screwdriver (be careful)&lt;/li&gt; &lt;li&gt;Remove the paper between the battery an the contact to activate the sensor.&lt;/li&gt; &lt;li&gt;Close the sensor (be sure that it is closed correctly. The sensor must not act like a button if you push the casing. This sometimes happens. It is important to close the device correctly.)&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;&lt;strong&gt;Include the sensor in the network:&lt;/strong&gt;&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Start HABmin.&lt;/li&gt; &lt;li&gt;Navigate to Configuration &amp;gt; Bindings &amp;gt; select the ZWAVE Binding &amp;gt; select the devices tab on the right&lt;/li&gt; &lt;li&gt;You should see at least your ZWAVE-Controller in the list of devices.&lt;/li&gt; &lt;li&gt;Hit &quot;include&quot; to start the 30 sec include mode&lt;/li&gt; &lt;li&gt;Hit the button on the back of the sensor 3 times in a short row.&lt;/li&gt; &lt;li&gt;The device starts to blink in blue. If it stops repeat step 5 until the 30 sec inclusion is over. Keep the sensor not to close and not too far from the controller (between 30 cm and 100 cm)&lt;/li&gt; &lt;li&gt;Hit &quot;reload properties&quot; in HABmin.&lt;/li&gt; &lt;li&gt;The new device should appear in the list (grey with no name)&lt;/li&gt; &lt;li&gt;If not, hit reload in your browser and navigate back to the device list.&lt;/li&gt; &lt;li&gt;The new device should appear in the list (grey with no name)&lt;/li&gt; &lt;li&gt;If not, restart your openHAB server, hit reload in your browser and navigate back to the device list.&lt;/li&gt; &lt;li&gt;The new device should appear in the list (grey with no name)&lt;/li&gt; &lt;li&gt;If not, repeat from step 4 until the device appears (This should happen rarely. I once needed two tries. Iadded 10 devices all together.)&lt;/li&gt; &lt;li&gt;Hit the button on the back of the sensor 3 times in a short row.&lt;/li&gt; &lt;li&gt;The device starts to blink in blue. Keep the sensor not to close and not too far from the controller (between 30 cm and 100 cm)&lt;/li&gt; &lt;li&gt;Hit &quot;reload properties&quot; in HABmin.&lt;/li&gt; &lt;li&gt;Repeat steps 14 - 16 several times. The device in the list will first show up a name, then turn to yellow and then to green.&lt;/li&gt; &lt;li&gt;Congratulations, you fully added the sensor and it should work correctly now.&lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;The default wake-up interval will drain the battery in a few weeks to months. It&#39;s advised to set a minimum wake-up interval of 3 hours to 24 hours after the device is configured correctly and a node.xml file has been created for the device.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgk101_03_002"></a>fibaro_fgk101_03_002</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Door Opening Sensor</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgms001_00_000"></a>fibaro_fgms001_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Motion Sensor</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgms001_03_002"></a>fibaro_fgms001_03_002</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_seismicintensity">sensor_seismicintensity</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Motion Sensor</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgr221_00_000"></a>fibaro_fgr221_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-blinds_control">blinds_control</a> </td>
<td></td>
<td></td>
<td>Roller Shutter Controller</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgrgbw_00_000"></a>fibaro_fgrgbw_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-switch_dimmer1">switch_dimmer1</a>,    <a href="#channel-id-switch_dimmer2">switch_dimmer2</a>,    <a href="#channel-id-switch_dimmer3">switch_dimmer3</a>,    <a href="#channel-id-switch_dimmer4">switch_dimmer4</a>,    <a href="#channel-id-switch_dimmer5">switch_dimmer5</a> </td>
<td></td>
<td></td>
<td>RGBW Controller</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgrm222_00_000"></a>fibaro_fgrm222_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-blinds_control">blinds_control</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a> </td>
<td></td>
<td></td>
<td>Roller Shutter</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgrm222_25_025"></a>fibaro_fgrm222_25_025</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a> </td>
<td></td>
<td></td>
<td>Roller Shutter</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgs211_00_000"></a>fibaro_fgs211_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>Relay Switch 1x3kW</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgs212_00_000"></a>fibaro_fgs212_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>Relay Switch 1x2.5kW</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgs221_01_004"></a>fibaro_fgs221_01_004</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>Double Relay Switch 2x1.5kW</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgs221_01_009"></a>fibaro_fgs221_01_009</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>Double Relay Switch 2x1.5kW</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgs221_02_001"></a>fibaro_fgs221_02_001</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>Double Relay Switch 2x1.5kW</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgs222_00_000"></a>fibaro_fgs222_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>Double Relay Switch 2x1.5kW</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgs223_00_000"></a>fibaro_fgs223_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a> </td>
<td></td>
<td></td>
<td>&#10;Double Switch 2 &lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;FIBARO Switch 2 is designed to be installed in standard wall switch boxes or anywhere else where it is necessary to control electric devices. FIBARO Switch 2 allows to control connected devices either via the Z-Wave+ network or via a switch connected directly to it and is equipped with active power and energy consumption metering functionality.&lt;/p&gt; &lt;p&gt;Main features of FIBARO Switch 2:&lt;/p&gt; &lt;ul&gt;&lt;li&gt;Compatible with any Z-Wave or Z-Wave+ Controller,&lt;/li&gt; &lt;li&gt;Supports protected mode (Z-Wave network security mode) with&lt;/li&gt; &lt;li&gt;AES-128 encryption,&lt;/li&gt; &lt;li&gt;Advanced microprocessor control,&lt;/li&gt; &lt;li&gt;Active power and energy metering functionality,&lt;/li&gt; &lt;li&gt;Works with various types of switches – momentary, toggle, three-way, etc,&lt;/li&gt; &lt;li&gt;To be installed in wall switch boxes of dimensions allowing for installation, conforming to provisions of applicable regulations,&lt;/li&gt; &lt;li&gt;FIBARO Switch 2 is an extension unit.&lt;/li&gt; &lt;/ul&gt;&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Put controller into inclusion mode and triple click S1 switch&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Put controller into exclusion mode and triple click S1 switch&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgsd002_00_000"></a>fibaro_fgsd002_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-time_offset">time_offset</a>,    <a href="#channel-id-alarm_smoke">alarm_smoke</a>,    <a href="#channel-id-alarm_heat">alarm_heat</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Smoke Detector</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgss001_00_000"></a>fibaro_fgss001_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-time_offset">time_offset</a>,    <a href="#channel-id-alarm_smoke">alarm_smoke</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-alarm_heat">alarm_heat</a> </td>
<td></td>
<td></td>
<td>Smoke Sensor</td>
</tr>
<tr>
<td><a name="thing-id-fibaro_fgwp101_00_000"></a>fibaro_fgwp101_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_power">sensor_power</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_reset">meter_reset</a> </td>
<td></td>
<td></td>
<td>Metered Wall Plug Switch</td>
</tr>
<tr>
<td><a name="thing-id-fortrezz_mimolite_00_000"></a>fortrezz_mimolite_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_general">sensor_general</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>&#10;Digital or Analog Voltage input and/or Dry Contact Relay &lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The MIMOlite module provides one analog or digital input and one relay output (isolated dry contacts, NO-COM-NC) and can be controlled by ZWaveTM.  The system includes a program switch for Z-WaveTM inclusion/exclusion and a status light (LED) for various indications. &lt;/p&gt; &lt;p&gt;Input SIG1 is an analog input, internally pulled-up to the MIMOlite supply voltage. The system allows trigger conditions to be set based on the input voltage being inside or outside a user-defined range (configured via Z-Wave).  This provides great flexibility for capturing events in a wide variety of applications. The trigger status of the input can be read via Z-WaveTM and/or can be automatically sent to a configured node, typically the Controller.  In addition, a count of the trigger events that have occurred for the input channel is internally recorded (and stored in the ‘pulse count’) and is available to be requested or automatically sent via Z-Wave.  The current triggered/un-triggered status can also be read via ZWave.  The SIG1 input can be associated with up to two other Z-WaveTM devices, such that an associated device will automatically turn on (or off) based on the occurrence of a trigger event.  Finally, the analog input channel can be configured so that the analog input level (not just binary trigger status) is periodically sent to up to two other associated nodes. &lt;/p&gt; &lt;p&gt;The output relay is typically commanded via Z-WaveTM commands. In addition, the user can configure the input SIG1 trigger condition to be mapped to the output relay.  For example, Relay 1 can be automatically turned on based on Input SIG1 being triggered.  The relay activation can be set via a jumper or via Z-WaveTM for either momentary or latched operation.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;ol&gt;&lt;li&gt;Set up the inclusion mode at the controller (for detailed directions, please refer to your controller user manual)&lt;/li&gt; &lt;li&gt;If the LED has a periodic single blink, the unit will be automatically included.  Otherwise, the button has been previously pressed and automatic inclusion mode is no longer active.  In this case, briefly press the button once and the controller will indicate that the unit has been included in the network.  Also, the Status LED will blink when the inclusion completes. Inclusion and exclusion are always done at normal transmit power mode.  &lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;ol&gt;&lt;li&gt;Set up the exclusion mode at the controller (for detailed directions, please refer to your controller user manual)&lt;/li&gt; &lt;li&gt;Press the MIMOlite button and the controller will indicate the unit has been removed from the network. The Status LED will blink when the exclusion completes.  &lt;/li&gt; &lt;/ol&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-fortrezz_ssa1_00_000"></a>fortrezz_ssa1_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a> </td>
<td></td>
<td></td>
<td>&#10;Siren and Strobe Alarm&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;h1&gt;Description&lt;/h1&gt; &lt;p&gt;The SSA1/SSA2 is a Z-Wave&lt;sup&gt;TM&lt;/sup&gt; enabled device and will sound a loud siren and flash a strobe light when an alarm message or alert is received on any Z-Wave&lt;sup&gt;TM&lt;/sup&gt; enabled network.&lt;/p&gt; &lt;ul&gt;&lt;li&gt;SSA1: Clear lense&lt;/li&gt; &lt;li&gt;SSA2: Red lense&lt;/li&gt; &lt;/ul&gt;&lt;h1&gt;Testing the SSA1&lt;/h1&gt; &lt;p&gt;After connecting power to the SSA1, you can test the alarm mode after manually exiting the Network Wide Inclusion (NWI) mode by pressing the button once or after the unit is included into any Z-Wave&lt;sup&gt;TM&lt;/sup&gt; enabled network. To test the SSA1, press and hold the button. The SSA1 will turn on and remain on until the button is released. While the siren is on, it will continually cycle for 1 second off / 4 seconds on for up to 5 minutes or until it is turned off. While the strobe is on, the unit will flash at a rate of once per second.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;ol&gt;&lt;li&gt;Set up the inclusion mode at the controller (If the controller supports NWI, the SSA1 will automatically be included at power on);&lt;/li&gt; &lt;li&gt;If the controller does not support NWI, press the SSA1 button once to exit NWI mode.&lt;/li&gt; &lt;li&gt;Press the button again to include the unit in the network. The controller will indicate that the unit has been included in the network. Also, the Status LED will flash when the inclusion completes. Inclusion and exclusion are always done at normal transmit power mode.&lt;/li&gt; &lt;/ol&gt;&lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;ol&gt;&lt;li&gt;Set up the exclusion mode at the controller&lt;/li&gt; &lt;li&gt;Press the SSA1 button once. The SSA1 will be removed from the network and the Status LED will flash. &lt;/li&gt; &lt;/ol&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-fortrezz_wv01_00_000"></a>fortrezz_wv01_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Wireless Z-Wave Water Valve</td>
</tr>
<tr>
<td><a name="thing-id-fortrezz_wwa02_00_000"></a>fortrezz_wwa02_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Wireless Water and Temperature Alarm</td>
</tr>
<tr>
<td><a name="thing-id-ge_12718_00_000"></a>ge_12718_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Smart Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-ge_12727_00_000"></a>ge_12727_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;GE 12727 Z-Wave Wireless Lighting Control Smart Toggle Switch&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;1. Follow the instructions for your Z-wave certified controller to include a device to the Z-wave network.&lt;/p&gt; &lt;p&gt;2. Once the controller is ready to include your device, press up and release the toggle switch to include it in the network.&lt;/p&gt; &lt;p&gt;Note: Your controller may need to be within 10 feet of the device to be included.&lt;/p&gt; &lt;p&gt;3. Once your controller has confirmed that the device has been included, refresh the Z-wave network to optimize performance.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-ge_12729_00_000"></a>ge_12729_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>&#10;In-Wall Smart Dimmer&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;1. Follow the instructions for your Z-wave certified controller to include a device to the Z-wave network.&lt;/p&gt; &lt;p&gt;2. Once the controller is ready to include your device, press up and release the toggle switch to include it in the network.&lt;/p&gt; &lt;p&gt;Note: Your controller may need to be within 10 feet of the device to be included.&lt;/p&gt; &lt;p&gt;3. Once your controller has confirmed that the device has been included, refresh the Z-wave network to optimize performance.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-ge_45603_00_000"></a>ge_45603_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Fluorescent Light &amp; Appliance Module</td>
</tr>
<tr>
<td><a name="thing-id-ge_45604_00_000"></a>ge_45604_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Outdoor Lighting Control Module</td>
</tr>
<tr>
<td><a name="thing-id-ge_45605_00_000"></a>ge_45605_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Duplex receptacle</td>
</tr>
<tr>
<td><a name="thing-id-ge_45606_00_000"></a>ge_45606_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>2-Way Dimmer Switch</td>
</tr>
<tr>
<td><a name="thing-id-ge_45607_00_000"></a>ge_45607_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>2-Way Dimmer Switch</td>
</tr>
<tr>
<td><a name="thing-id-ge_45609_00_000"></a>ge_45609_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>On/Off Relay Switch</td>
</tr>
<tr>
<td><a name="thing-id-ge_zw1001_00_000"></a>ge_zw1001_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Smart Outlet</td>
</tr>
<tr>
<td><a name="thing-id-ge_zw3003_00_000"></a>ge_zw3003_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>&#10;In-Wall Dimmer&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Also goes by GE / Jasco catalog number 12724.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-ge_zw3101_00_000"></a>ge_zw3101_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Lamp Module</td>
</tr>
<tr>
<td><a name="thing-id-ge_zw4002_00_000"></a>ge_zw4002_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>&#10;In-Wall Smart Fan Control&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;&lt;strong&gt;Basic Operation&lt;/strong&gt;&lt;/p&gt; &lt;p&gt;Note: Before starting, fan must be set to ‘HIGH’ position using pull chain. The connected fan can then be turned ON/OFF and adjust speed levels in two ways:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Manually from the front panel rocker of the In-wall Fan Control&lt;/li&gt; &lt;li&gt;Remotely with a Z-Wave controller&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;&lt;strong&gt;Manual Control&lt;/strong&gt;&lt;/p&gt; &lt;p&gt;To turn the connected fan ON: Press and release the top of the rocker. Note: Fan will return to last speed setting of Fan Control. Default setting – HIGH.&lt;/p&gt; &lt;p&gt;To turn the fan OFF: Press and release the bottom of the rocker.&lt;/p&gt; &lt;p&gt;&lt;strong&gt;Adjust fan speed&lt;/strong&gt;&lt;/p&gt; &lt;p&gt;To decrease fan speed: Press and hold lower rocker.&lt;/p&gt; &lt;p&gt;To increase fan speed: Press and hold upper rocker.&lt;/p&gt; &lt;p&gt;The LED indicator confirms fan speed settings by flashing patterns&lt;/p&gt; &lt;ul&gt;&lt;li&gt;LOW – LED will blink every 2 seconds for 10 seconds&lt;/li&gt; &lt;li&gt;MEDIUM – LED will blink every second for 10 seconds&lt;/li&gt; &lt;li&gt;HIGH – LED will blink every half second for 10 seconds&lt;/li&gt; &lt;/ul&gt;&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Once the controller is ready to include your device, press and release the top or bottom of the smart fan control switch (rocker) to include it in the network.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-ge_zw4005_00_000"></a>ge_zw4005_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;On/Off Relay Switch&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Also marketed as GE / Jasco 12722.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-greenwave_gwpn1_03_000"></a>greenwave_gwpn1_03_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_reset">meter_reset</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>&#10;Single-socket PowerNode&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;This Configuration is for the 1 plug version of the PowerNode.&lt;/p&gt; &lt;p&gt;It should work for both NS210 and NS310 devices.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;On the PowerNode, press and hold the &lt;strong&gt;Sync&lt;/strong&gt; button for approximately one second until the activity indicator displays a clockwise rotating pattern. This indicates the PowerNode is attempting inclusion. During this process, verify that the Gateway activity indicator still displays a clockwise rotating pattern.&lt;/p&gt; &lt;p&gt;After a few seconds, the rotating pattern on both the PowerNode and the Gateway stops. All bars turn green forming a circle for several seconds. This indicates a successful inclusion.&lt;/p&gt; &lt;p&gt;If all bars on the activity indicator start flashing instead of forming a solid circle, then the PowerNode inclusion process has failed, and you must start the sync process again. If syncing continually fails even though the PowerNode is close to the Gateway, then it may be an indication of a hardware fault, and the PowerNode might need replacing.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;On the PowerNode, press and hold the &lt;strong&gt;Sync&lt;/strong&gt; button for approximately one second until the PowerNode activity indicator begins to display a counter-clockwise rotating pattern. The PowerNode is attempting exclusion.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-greenwave_gwpn5_00_000"></a>greenwave_gwpn5_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-switch_binary3">switch_binary3</a>,    <a href="#channel-id-meter_watts3">meter_watts3</a>,    <a href="#channel-id-meter_kwh3">meter_kwh3</a>,    <a href="#channel-id-switch_binary4">switch_binary4</a>,    <a href="#channel-id-meter_watts4">meter_watts4</a>,    <a href="#channel-id-meter_kwh4">meter_kwh4</a>,    <a href="#channel-id-switch_binary5">switch_binary5</a>,    <a href="#channel-id-meter_watts5">meter_watts5</a>,    <a href="#channel-id-meter_kwh5">meter_kwh5</a> </td>
<td></td>
<td></td>
<td>&#10;Multi-socket PowerNode (5-plug)&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;This Configuration is for the 6 plug version of the PowerNode.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;On the PowerNode, press and hold the &lt;strong&gt;sync&lt;/strong&gt; button for approximately one second until the activity indicator displays a clockwise rotating pattern. This indicates the PowerNode is attempting inclusion. During this process, verify that the Gateway activity indicator still displays a clockwise rotating pattern.&lt;/p&gt; &lt;p&gt;After a few seconds, the rotating pattern on both the PowerNode and the Gateway stops. All bars turn green forming a circle for several seconds. This indicates a successful inclusion.&lt;/p&gt; &lt;p&gt;If all bars on the activity indicator start flashing instead of forming a solid circle, then the PowerNode inclusion process has failed, and you must start the sync process again. If syncing continually fails even though the PowerNode is close to the Gateway, then it may be an indication of a hardware fault, and the PowerNode might need replacing.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;On the PowerNode, press and hold the &lt;strong&gt;Sync&lt;/strong&gt; button for approximately one second until the PowerNode activity indicator begins to display a counter-clockwise rotating pattern. The PowerNode is attempting exclusion.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-greenwave_gwpn6_00_000"></a>greenwave_gwpn6_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-switch_binary3">switch_binary3</a>,    <a href="#channel-id-meter_watts3">meter_watts3</a>,    <a href="#channel-id-meter_kwh3">meter_kwh3</a>,    <a href="#channel-id-switch_binary4">switch_binary4</a>,    <a href="#channel-id-meter_watts4">meter_watts4</a>,    <a href="#channel-id-meter_kwh4">meter_kwh4</a>,    <a href="#channel-id-switch_binary5">switch_binary5</a>,    <a href="#channel-id-meter_watts5">meter_watts5</a>,    <a href="#channel-id-meter_kwh5">meter_kwh5</a>,    <a href="#channel-id-switch_binary6">switch_binary6</a>,    <a href="#channel-id-meter_watts6">meter_watts6</a>,    <a href="#channel-id-meter_kwh6">meter_kwh6</a> </td>
<td></td>
<td></td>
<td>&#10;Multi-socket PowerNode (6-plug)&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;This Configuration is for the 6 plug version of the PowerNode.&lt;/p&gt; &lt;p&gt;It should work for both NP210 and NP310 devices (except &lt;em&gt;NP210&lt;/em&gt;-G-EN which is 5-port UK version).&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;On the PowerNode, press and hold the &lt;strong&gt;Sync&lt;/strong&gt; button for approximately one second until the activity indicator displays a clockwise rotating pattern. This indicates the PowerNode is attempting inclusion. During this process, verify that the Gateway activity indicator still displays a clockwise rotating pattern.&lt;/p&gt; &lt;p&gt;After a few seconds, the rotating pattern on both the PowerNode and the Gateway stops. All bars turn green forming a circle for several seconds. This indicates a successful inclusion.&lt;/p&gt; &lt;p&gt;If all bars on the activity indicator start flashing instead of forming a solid circle, then the PowerNode inclusion process has failed, and you must start the sync process again. If syncing continually fails even though the PowerNode is close to the Gateway, then it may be an indication of a hardware fault, and the PowerNode might need replacing.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;On the PowerNode, press and hold the &lt;strong&gt;Sync&lt;/strong&gt; button for approximately one second until the PowerNode activity indicator begins to display a counter-clockwise rotating pattern. The PowerNode is attempting exclusion.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-homeseer_ezmotionexpress_00_000"></a>homeseer_ezmotionexpress_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Wireless 3-in-1 Sensor</td>
</tr>
<tr>
<td><a name="thing-id-homeseer_ezmultipli_00_000"></a>homeseer_ezmultipli_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-color_color">color_color</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Multi Sensor</td>
</tr>
<tr>
<td><a name="thing-id-homeseer_hswd100_00_000"></a>homeseer_hswd100_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Scene Capable Wall Dimmer Switch</td>
</tr>
<tr>
<td><a name="thing-id-homeseer_hsws100_00_000"></a>homeseer_hsws100_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Scene Capable Wall Switch</td>
</tr>
<tr>
<td><a name="thing-id-honeywell_th8320zw_00_000"></a>honeywell_th8320zw_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a> </td>
<td></td>
<td></td>
<td>VisionPRO Z-Wave Touchscreen Programmable Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-horstmann_asrzw_00_000"></a>horstmann_asrzw_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a> </td>
<td></td>
<td></td>
<td>Thermostat Receiver</td>
</tr>
<tr>
<td><a name="thing-id-horstmann_hrt4zw_00_000"></a>horstmann_hrt4zw_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Battery Powered Wall Thermostat&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;&lt;strong&gt;Note&lt;/strong&gt;: This device is also sold as Secure SRT321&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-horstmann_scsc17_00_000"></a>horstmann_scsc17_00_000</td>
<td></td>
<td></td>
<td></td>
<td>7 Day Room Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-horstmann_sir321_00_000"></a>horstmann_sir321_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;RF Countdown Timer&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Wirelessly controllable 3 KW wall switch timer function&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-horstmann_ssr302_00_000"></a>horstmann_ssr302_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-thermostat_mode1">thermostat_mode1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-thermostat_mode2">thermostat_mode2</a> </td>
<td></td>
<td></td>
<td>Two Channel Boiler Actuator</td>
</tr>
<tr>
<td><a name="thing-id-intermatic_ca3500_00_000"></a>intermatic_ca3500_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>15 Amp Split-Duplex Receptacle</td>
</tr>
<tr>
<td><a name="thing-id-intermatic_ca8900_00_000"></a>intermatic_ca8900_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a> </td>
<td></td>
<td></td>
<td>Z-Wave Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-intermatic_ha01c_00_000"></a>intermatic_ha01c_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;In-Wall Dual Receptacle&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The Intermatic HA01C Wall Receptacle is an in­-wall Z-­Wave switch that can be controlled to automatically and wirelessly to replace an existing receptacle to control lamps, or small appliances. It also works as a range extender for your other Z-­Wave devices. This In-Wall receptacle features two plug receptacles, both of which provide standard power operation, only the bottom plug in the outlet is controllable by Z-Wave. Ratings: 15 A, 125 VAC, 50/60 Hz Operating Temperature Range: 32°F to 104°F / 0°C to 40°C Radio Operating Frequency: 908.42 MHz RF (wireless) Range: 50-100 ft. under normal operating conditions&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;After hard-wiring the HA01 into place, the device is ready to join your Z-Wave™ network.&lt;/p&gt; &lt;p&gt;NOTE: Before you begin, devices (such as lamps) should be plugged in and turned ON before including the HA01 in your network. Put the controller into INCLUSION mode. Press the ON/OFF/PROGRAM button on the HA01 In-Wall Receptacle.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;If necessary, you can delete the HA01 In-Wall Receptacle from your Z-Wave™ network. Put the controller into EXCLUSION mode. Press the ON/OFF/PROGRAM button on the HA01 In-Wall Receptacle.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-intermatic_ha04c_00_000"></a>intermatic_ha04c_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Outdoor Module</td>
</tr>
<tr>
<td><a name="thing-id-kichler_15dc200_00_000"></a>kichler_15dc200_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-time_offset">time_offset</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-switch_binary3">switch_binary3</a>,    <a href="#channel-id-switch_binary4">switch_binary4</a>,    <a href="#channel-id-switch_binary5">switch_binary5</a> </td>
<td></td>
<td></td>
<td>200W Design Pro LED Controller</td>
</tr>
<tr>
<td><a name="thing-id-kwikset_914trl_00_000"></a>kwikset_914trl_00_000</td>
<td>  <a href="#channel-id-lock_door">lock_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Touchpad Electronic Deadbolt&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt; Press button “A” on the lock one time to include it in your system.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt; Press button “A” on the lock one time to exclude it in your system.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-leviton_dzmx11lz_00_000"></a>leviton_dzmx11lz_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Scene Capable Push On/Off</td>
</tr>
<tr>
<td><a name="thing-id-leviton_dzpa1_00_000"></a>leviton_dzpa1_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Plug-in Appliance Module</td>
</tr>
<tr>
<td><a name="thing-id-leviton_dzpd3_00_000"></a>leviton_dzpd3_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Lamp Module</td>
</tr>
<tr>
<td><a name="thing-id-leviton_dzr15_00_000"></a>leviton_dzr15_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Scene Capable Receptacle</td>
</tr>
<tr>
<td><a name="thing-id-leviton_dzs15_00_000"></a>leviton_dzs15_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Scene Capable Push On/Off</td>
</tr>
<tr>
<td><a name="thing-id-leviton_rzp03_00_000"></a>leviton_rzp03_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Scene Capable Plug-In Lamp Dimming Module</td>
</tr>
<tr>
<td><a name="thing-id-leviton_vrcs2mrx_00_000"></a>leviton_vrcs2mrx_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Vizia RF + 2-Button Scene Controller with Switches</td>
</tr>
<tr>
<td><a name="thing-id-leviton_vri06_00_000"></a>leviton_vri06_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Incandescent Scene Capable Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-leviton_vrmx11lz_00_000"></a>leviton_vrmx11lz_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a> </td>
<td></td>
<td></td>
<td>Scene Capable Push On/Off Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-leviton_vrp03_00_000"></a>leviton_vrp03_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>300W Scene Capable Plug-In Lamp Dimming Module</td>
</tr>
<tr>
<td><a name="thing-id-leviton_vrpa1_00_000"></a>leviton_vrpa1_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>&#10;Vizia RF + Scene Capable Plug-in Module&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The Vizia RF + Scene Capable Plug-in Appliance Module (VRPA1-1LW) is ideal for any residential setting where remote ON/OFF switching of appliances, motor loads up to 1/2 HP, or freestanding lights, including Incandescent, Magnetic Low Voltage, Fluorescent and Compact Fluorescent loads is needed. Typical applications include switching of fluorescent lamps, portable fans, kitchen appliances, indoor fountains and more. The Vizia RF + Scene Capable Plug-in Appliance Module also allows users to incorporate small appliances into scene and zone home control applications. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;ol&gt;&lt;li&gt;Plug in the device&lt;/li&gt; &lt;li&gt;Press and hold the front button until the light changes to a flashing amber&lt;/li&gt; &lt;/ol&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-leviton_vrpd3_00_000"></a>leviton_vrpd3_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Scene Capable Plug-in Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-leviton_vrs05_00_000"></a>leviton_vrs05_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Scene Capable Switch</td>
</tr>
<tr>
<td><a name="thing-id-leviton_vrs15_00_000"></a>leviton_vrs15_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Scene Capable Push On/Off</td>
</tr>
<tr>
<td><a name="thing-id-linear_fs20z_00_000"></a>linear_fs20z_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Isolated Contact Fixture Module</td>
</tr>
<tr>
<td><a name="thing-id-linear_lb60z1_00_000"></a>linear_lb60z1_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Dimmable LED Light Bulb</td>
</tr>
<tr>
<td><a name="thing-id-linear_ngd00z4_00_000"></a>linear_ngd00z4_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a> </td>
<td></td>
<td></td>
<td>Garage Door Controller</td>
</tr>
<tr>
<td><a name="thing-id-linear_pd300z2_00_000"></a>linear_pd300z2_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Plug-in Wall Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-linear_ps15z_00_000"></a>linear_ps15z_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Plug-In Appliance Module</td>
</tr>
<tr>
<td><a name="thing-id-linear_tbz48_00_000"></a>linear_tbz48_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-time_offset">time_offset</a> </td>
<td></td>
<td></td>
<td>Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-linear_wa105dbz1_00_000"></a>linear_wa105dbz1_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Siren &amp; Strobe</td>
</tr>
<tr>
<td><a name="thing-id-linear_wa105dbz_00_000"></a>linear_wa105dbz_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Siren</td>
</tr>
<tr>
<td><a name="thing-id-linear_wadwaz1_00_000"></a>linear_wadwaz1_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_entry">alarm_entry</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Door/Windows Sensor&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Linear Z-Wave products are easy to install, and allow dealers to create an integrated wireless network with nearly limitless expansion and interoperability with security and health monitoring systems, energy management, home entertainment, appliances, and more.&lt;/p&gt; &lt;p&gt;The WADWAZ-1 sensor monitors a door or window and sends Z-Wave signals when the door is opened or closed.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Refer to your Controller operating instructions to add this module under the command of the Wireless Controller.&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Unscrew the screw fastening the rear cover and slide the rear cover down.&lt;/li&gt; &lt;li&gt;Insert a CR123A battery into the battery compartment and LED will start to fl ash slowly, which means the sensor has not yet performed “inclusion” with the Controller.&lt;/li&gt; &lt;li&gt;Prepare the Controller to include a unit to the network by adding it to a group (method of adding a node to the network). Refer to the Controller instructions.&lt;/li&gt; &lt;li&gt;If your Controller supports Network Wide Inclusion (NWI) locate the WADWAZ-1 near the proposed installation location. If not skip to Step 6.&lt;/li&gt; &lt;li&gt;With your Controller in Inclusion mode, you should see an indication on your Controller that the “device was included” in the network. It should display binary switch / Linear. The LED will stop blinking. Skip to Step 9. If the LED does not stop blinking, relocate the WADWAZ-1 to within 100 feet (line of sight) of a Z-Wave device or your hub and repeat Step 5. If the LED continues to blink, your Controller does not support NWI and continue with Step 6.&lt;/li&gt; &lt;li&gt;Place the WADWAZ-1 within 3 feet of the Controller.&lt;/li&gt; &lt;li&gt;With your Controller in Inclusion mode, depress the Program switch for 1 second then release.&lt;/li&gt; &lt;li&gt;You should see an indication on your Controller that the “device was included” in the network. It should display binary switch / Linear. The LED will stop blinking.&lt;/li&gt; &lt;li&gt;The device will appear in the list of Switches&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;&lt;strong&gt;NOTE&lt;/strong&gt;: If you have trouble adding the WADWAZ-1 to a group it may be that the Home ID and Node ID were not cleared from it after testing. You must first “RESET UNIT” with your Controller to remove it from the network. Although adding it to a group includes it in the network, removing it from a group does not remove it from the network. If removed from a group, it functions as a repeater (only). “RESET UNIT” removes it completely from the network&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;For “Exclusion” from (removing from) a network:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Set up the Z-WaveTM Interface Controller into “exclusion” mode, and following its instruction to delete the WADWAZ-1 from the Controller.&lt;/li&gt; &lt;li&gt;Press the Program switch of WADWAZ-1 for 1 second and release to be excluded. The LED light will fl ash continuously when the sensor is in the Exclusion condition.&lt;/li&gt; &lt;/ol&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-linear_wapirz_00_000"></a>linear_wapirz_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>PIR Motion Detector</td>
</tr>
<tr>
<td><a name="thing-id-linear_wd500z_00_000"></a>linear_wd500z_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Wall Dimmer Switch</td>
</tr>
<tr>
<td><a name="thing-id-linear_wo15z_00_000"></a>linear_wo15z_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Single Wall Outlet</td>
</tr>
<tr>
<td><a name="thing-id-linear_ws15z_00_000"></a>linear_ws15z_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Wall Mounted Switch</td>
</tr>
<tr>
<td><a name="thing-id-linear_wt00z1_00_000"></a>linear_wt00z1_00_000</td>
<td></td>
<td></td>
<td></td>
<td>3-Way Wall Accessory Switch</td>
</tr>
<tr>
<td><a name="thing-id-logic_zhc5010_00_000"></a>logic_zhc5010_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_dimmer1">switch_dimmer1</a>,    <a href="#channel-id-scene_number1">scene_number1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-switch_dimmer2">switch_dimmer2</a>,    <a href="#channel-id-scene_number2">scene_number2</a>,    <a href="#channel-id-switch_binary3">switch_binary3</a>,    <a href="#channel-id-switch_dimmer3">switch_dimmer3</a>,    <a href="#channel-id-scene_number3">scene_number3</a>,    <a href="#channel-id-switch_binary4">switch_binary4</a>,    <a href="#channel-id-switch_dimmer4">switch_dimmer4</a>,    <a href="#channel-id-scene_number4">scene_number4</a> </td>
<td></td>
<td></td>
<td>&#10;FUGA Wall 4-way Switch + Relay&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;ZHC5010 is a wall switch module with Z-Wave communication. The module contains four buttons, four LEDs, a built-in relay switch and is designed to fit into a standard LK FUGA® wall box (one-module format). The ZHC5010 Wall Switch allows you to control the local load as well as Z-Wave connected devices in up to four additional Z-Wave groups.&lt;/p&gt; &lt;p&gt;Please note that for simplicity reasons, this configuration set does not include;&lt;/p&gt; &lt;ul&gt;&lt;li&gt;Configuration of LEDs&lt;/li&gt; &lt;li&gt;Multi Level support&lt;/li&gt; &lt;li&gt;House Clean Mode support&lt;/li&gt; &lt;li&gt;Control of association groups&lt;/li&gt; &lt;/ul&gt;&lt;p&gt;Support for this could be added to the profile later as per description in the manual.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Place your primary controller in Adding Mode by following the manufacturer&#39;s instructions, then activate the add mode on the device by triple-clicking the upper left button on the module, or by triple-clicking the little button in the middle of the module (hidden behind the small plastic cover). Adding Mode is indicated by blinking of upper left LED until the timeout occurs after 10 seconds or the module has been added in the network. &lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Follow the inclusion procedures; the device is removed in the same manner, when the controller is in Removing Mode.&lt;/p&gt; &lt;p&gt;Factory Reset ZHC5010 can be factory reset by pressing the small button in the middle of the module (normally covered by small plastic cover) for at least 10 seconds. Remove the middle plastic cover by means of a small screwdriver, and press the small button for at least 10 seconds, until all the LEDs lights up. Please use this procedure only when the network primary controller is missing or otherwise inoperable.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-mcohome__00_000"></a>mcohome__00_000</td>
<td></td>
<td></td>
<td></td>
<td>Programmable Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-mcohome_mhs411_00_000"></a>mcohome_mhs411_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Touch Panel Switch (Single)</td>
</tr>
<tr>
<td><a name="thing-id-mcohome_mhs412_00_000"></a>mcohome_mhs412_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>Touch Panel Switch (Dual)</td>
</tr>
<tr>
<td><a name="thing-id-merten_507601_00_000"></a>merten_507601_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Dual Pole Wall Switch</td>
</tr>
<tr>
<td><a name="thing-id-merten_5082xx_00_000"></a>merten_5082xx_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Battery Powered Wall Controller MOVE</td>
</tr>
<tr>
<td><a name="thing-id-nexia_db100z_00_000"></a>nexia_db100z_00_000</td>
<td>  <a href="#channel-id-notification_power_management">notification_power_management</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Doorbell</td>
</tr>
<tr>
<td><a name="thing-id-nodon_crc3100_00_000"></a>nodon_crc3100_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Octon Remote Control</td>
</tr>
<tr>
<td><a name="thing-id-nodon_cws3101_00_000"></a>nodon_cws3101_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Wall Switch</td>
</tr>
<tr>
<td><a name="thing-id-nodon_softremote_00_000"></a>nodon_softremote_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Remote Control</td>
</tr>
<tr>
<td><a name="thing-id-northq_nq9021_00_000"></a>northq_nq9021_00_000</td>
<td>  <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-time_offset">time_offset</a> </td>
<td></td>
<td></td>
<td>NorthQ Electrical Meter</td>
</tr>
<tr>
<td><a name="thing-id-philio_pan04_00_000"></a>philio_pan04_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_powerfactor">meter_powerfactor</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-meter_powerfactor1">meter_powerfactor1</a>,    <a href="#channel-id-meter_current1">meter_current1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_voltage1">meter_voltage1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-meter_powerfactor2">meter_powerfactor2</a>,    <a href="#channel-id-meter_current2">meter_current2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-meter_voltage2">meter_voltage2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a>,    <a href="#channel-id-switch_binary3">switch_binary3</a>,    <a href="#channel-id-meter_powerfactor3">meter_powerfactor3</a>,    <a href="#channel-id-meter_current3">meter_current3</a>,    <a href="#channel-id-meter_kwh3">meter_kwh3</a>,    <a href="#channel-id-meter_voltage3">meter_voltage3</a>,    <a href="#channel-id-meter_watts3">meter_watts3</a> </td>
<td></td>
<td></td>
<td>In Wall Dual Relay(1 Way) Switch Module 2x 1.5kW with power meter</td>
</tr>
<tr>
<td><a name="thing-id-philio_pan06_00_000"></a>philio_pan06_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-switch_binary3">switch_binary3</a> </td>
<td></td>
<td></td>
<td>In Wall Dual Relay(1 Way) Switch Module 2x 1.5kW</td>
</tr>
<tr>
<td><a name="thing-id-philio_pan11_00_000"></a>philio_pan11_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_powerfactor">meter_powerfactor</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_reset">meter_reset</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Smart Energy Plug In Switch</td>
</tr>
<tr>
<td><a name="thing-id-philio_pat02c_01_011"></a>philio_pat02c_01_011</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_flood">alarm_flood</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Flood Sensor</td>
</tr>
<tr>
<td><a name="thing-id-philio_pse02_00_000"></a>philio_pse02_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Siren</td>
</tr>
<tr>
<td><a name="thing-id-philio_psm02_00_000"></a>philio_psm02_00_000</td>
<td>  <a href="#channel-id-alarm_motion">alarm_motion</a>,    <a href="#channel-id-alarm_tamper">alarm_tamper</a>,    <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Slim Multi-Sensor</td>
</tr>
<tr>
<td><a name="thing-id-philio_psr04_00_000"></a>philio_psr04_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Smart Color Button&lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;By default, this device only wakes up every 86400 seconds (24 hours). To manually wake this device up:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Hold the device vertically, or against a wall (this will not work if the device is horizontal).&lt;/li&gt; &lt;li&gt;Rotate the wheel so that the pointer is at the top of the device.&lt;/li&gt; &lt;li&gt;Wait at least three seconds.&lt;/li&gt; &lt;li&gt;Rotate the wheel clockwise through 180 degrees to the bottom of the device.&lt;/li&gt; &lt;li&gt;Immediately press the centre button once.&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;This activates the wakeup for 10 seconds, during which time you will be able to identify the device correctly.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-philio_pst02a_00_000"></a>philio_pst02a_00_000</td>
<td>  <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-alarm_motion">alarm_motion</a>,    <a href="#channel-id-alarm_tamper">alarm_tamper</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_access">alarm_access</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Slim Multi-Sensor (PIR/Door/Temperature/Illumination)</td>
</tr>
<tr>
<td><a name="thing-id-philio_pst02b_00_000"></a>philio_pst02b_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Slim Multi-Sensor (PIR/Temperature/Illumination)</td>
</tr>
<tr>
<td><a name="thing-id-philio_pst02c_00_000"></a>philio_pst02c_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Slim Multi-Sensor (Door/Temperature/Illumination)</td>
</tr>
<tr>
<td><a name="thing-id-popp_004001_00_000"></a>popp_004001_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Smoke Detector and Siren</td>
</tr>
<tr>
<td><a name="thing-id-popp_005107_00_000"></a>popp_005107_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Solar Powered Outdoor Siren</td>
</tr>
<tr>
<td><a name="thing-id-popp_009204_00_000"></a>popp_009204_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>KFOB-C Remote-Control</td>
</tr>
<tr>
<td><a name="thing-id-popp_009303_00_000"></a>popp_009303_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Wall Controller</td>
</tr>
<tr>
<td><a name="thing-id-popp_05438_00_000"></a>popp_05438_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>Indoor/Outdoor Wall Plug Switch</td>
</tr>
<tr>
<td><a name="thing-id-popp_123580_00_000"></a>popp_123580_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Wall Plug Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-popp_123610_00_000"></a>popp_123610_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Wall Plug Switch</td>
</tr>
<tr>
<td><a name="thing-id-popp_123665_00_000"></a>popp_123665_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_powerfactor">meter_powerfactor</a>,    <a href="#channel-id-meter_current">meter_current</a> </td>
<td></td>
<td></td>
<td>Wall Plug Meter Switch</td>
</tr>
<tr>
<td><a name="thing-id-popp_pope005206_00_000"></a>popp_pope005206_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_dewpoint">sensor_dewpoint</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-sensor_velocity">sensor_velocity</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_barpressure">sensor_barpressure</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_pulse">meter_pulse</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Z Weather</td>
</tr>
<tr>
<td><a name="thing-id-prodrive_ed20_00_000"></a>prodrive_ed20_00_000</td>
<td>  <a href="#channel-id-time_offset">time_offset</a>,    <a href="#channel-id-meter_gas_cubic_meters1">meter_gas_cubic_meters1</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-meter_kwh3">meter_kwh3</a>,    <a href="#channel-id-meter_kwh4">meter_kwh4</a>,    <a href="#channel-id-meter_kwh5">meter_kwh5</a>,    <a href="#channel-id-meter_kwh6">meter_kwh6</a> </td>
<td></td>
<td></td>
<td>&#10;Eneco Meter Adapter&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The meter adapter measures gas and electricity usage of analog and digital meters. Analog meters are read by means of optical sensors that can be placed on the meter to monitor the rotating disc or the blinking LED. In case of a digital meter, the meter adapter can be connected to the digital meter by means of a P1 digital communication interface. The meter adapter measures the cumulative and instantaneous gas and electricity consumption and sends this information to a Z-Wave enabled display (ED2.0 Display).&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Press Button&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhaa_00_000"></a>qubino_zmnhaa_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-sensor_binary1">sensor_binary1</a>,    <a href="#channel-id-sensor_binary2">sensor_binary2</a> </td>
<td></td>
<td></td>
<td>Flush 1 relay</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhad_00_000"></a>qubino_zmnhad_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-alarm_power">alarm_power</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-sensor_binary2">sensor_binary2</a>,    <a href="#channel-id-alarm_power2">alarm_power2</a>,    <a href="#channel-id-sensor_binary3">sensor_binary3</a>,    <a href="#channel-id-alarm_power3">alarm_power3</a> </td>
<td></td>
<td></td>
<td>Flush 1 relay</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhba_00_000"></a>qubino_zmnhba_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a> </td>
<td></td>
<td></td>
<td>Flush 2 relays</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhbd_00_000"></a>qubino_zmnhbd_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a>,    <a href="#channel-id-meter_watts2">meter_watts2</a>,    <a href="#channel-id-meter_kwh2">meter_kwh2</a> </td>
<td></td>
<td></td>
<td>Flush 2 relays</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhcd_00_000"></a>qubino_zmnhcd_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-blinds_control">blinds_control</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>Flush Shutter</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhda_00_000"></a>qubino_zmnhda_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-sensor_binary1">sensor_binary1</a>,    <a href="#channel-id-sensor_binary2">sensor_binary2</a> </td>
<td></td>
<td></td>
<td>Flush dimmer</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhdd_00_000"></a>qubino_zmnhdd_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_dimmer1">switch_dimmer1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-sensor_binary2">sensor_binary2</a>,    <a href="#channel-id-alarm_general2">alarm_general2</a>,    <a href="#channel-id-sensor_binary3">sensor_binary3</a>,    <a href="#channel-id-alarm_general3">alarm_general3</a> </td>
<td></td>
<td></td>
<td>Flush Dimmer Plus</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhid_00_000"></a>qubino_zmnhid_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a> </td>
<td></td>
<td></td>
<td>Flush on/off thermostat</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhja_00_000"></a>qubino_zmnhja_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_binary1">sensor_binary1</a>,    <a href="#channel-id-sensor_binary2">sensor_binary2</a>,    <a href="#channel-id-sensor_binary3">sensor_binary3</a> </td>
<td></td>
<td></td>
<td>Flush Pilot</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhjd_00_000"></a>qubino_zmnhjd_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_binary1">sensor_binary1</a>,    <a href="#channel-id-sensor_binary2">sensor_binary2</a>,    <a href="#channel-id-sensor_binary3">sensor_binary3</a> </td>
<td></td>
<td></td>
<td>Flush Pilot</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhla_00_000"></a>qubino_zmnhla_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a> </td>
<td></td>
<td></td>
<td>Flush PWM thermostat</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhnd_00_000"></a>qubino_zmnhnd_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Flush 1D relay</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhod_00_000"></a>qubino_zmnhod_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-blinds_control">blinds_control</a>,    <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-blinds_control1">blinds_control1</a>,    <a href="#channel-id-meter_watts1">meter_watts1</a>,    <a href="#channel-id-meter_kwh1">meter_kwh1</a>,    <a href="#channel-id-sensor_temperature2">sensor_temperature2</a> </td>
<td></td>
<td></td>
<td>Flush Shutter</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhsd_00_000"></a>qubino_zmnhsd_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>DIN Rail Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-qubino_zmnhvd_00_000"></a>qubino_zmnhvd_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Flush Dimmer 0-10V</td>
</tr>
<tr>
<td><a name="thing-id-rcs_tbz48_00_000"></a>rcs_tbz48_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-rcs_tz43_00_000"></a>rcs_tz43_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a>,    <a href="#channel-id-time_offset">time_offset</a> </td>
<td></td>
<td></td>
<td>Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-reitz_05431_00_000"></a>reitz_05431_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Duewi ZW BJ ES 1000 / Reitz 05431 / ZWave.me 05457 Single button wall switch</td>
</tr>
<tr>
<td><a name="thing-id-reitz_05433_00_000"></a>reitz_05433_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>One Paddle Wall Dimmer Insert</td>
</tr>
<tr>
<td><a name="thing-id-reitz_054375_00_000"></a>reitz_054375_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Schuko Plug Switch</td>
</tr>
<tr>
<td><a name="thing-id-reitz_06436_00_000"></a>reitz_06436_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Insert blind control</td>
</tr>
<tr>
<td><a name="thing-id-reitz_064394_00_000"></a>reitz_064394_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Schuko Plug Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-remotec_zrc100_00_000"></a>remotec_zrc100_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Remote Control</td>
</tr>
<tr>
<td><a name="thing-id-remotec_zrc90_00_000"></a>remotec_zrc90_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Scene master 8 button remote</td>
</tr>
<tr>
<td><a name="thing-id-remotec_zts110_00_000"></a>remotec_zts110_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Remotec ZTS-110 Z Wave Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-remotec_zxt-120au_00_000"></a>remotec_zxt-120au_00_000</td>
<td></td>
<td></td>
<td></td>
<td>Remotec ZXT-120 AC IR Remote</td>
</tr>
<tr>
<td><a name="thing-id-remotec_zxt120_00_000"></a>remotec_zxt120_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>AC IR Remote</td>
</tr>
<tr>
<td><a name="thing-id-rtc_ct100_00_000"></a>rtc_ct100_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-time_offset">time_offset</a>,    <a href="#channel-id-sensor_temperature1">sensor_temperature1</a>,    <a href="#channel-id-sensor_relhumidity2">sensor_relhumidity2</a> </td>
<td></td>
<td></td>
<td>Z-Wave Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-rtc_ct30_00_000"></a>rtc_ct30_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-time_offset">time_offset</a> </td>
<td></td>
<td></td>
<td>Z-Wave Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-rtc_ct32_00_000"></a>rtc_ct32_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_furnace">thermostat_setpoint_furnace</a>,    <a href="#channel-id-thermostat_setpoint_dry_air">thermostat_setpoint_dry_air</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-time_offset">time_offset</a>,    <a href="#channel-id-sensor_temperature1">sensor_temperature1</a>,    <a href="#channel-id-sensor_relhumidity1">sensor_relhumidity1</a>,    <a href="#channel-id-thermostat_mode1">thermostat_mode1</a>,    <a href="#channel-id-thermostat_state1">thermostat_state1</a>,    <a href="#channel-id-thermostat_setpoint_furnace1">thermostat_setpoint_furnace1</a>,    <a href="#channel-id-thermostat_setpoint_dry_air1">thermostat_setpoint_dry_air1</a>,    <a href="#channel-id-thermostat_setpoint_heating1">thermostat_setpoint_heating1</a>,    <a href="#channel-id-thermostat_setpoint_cooling1">thermostat_setpoint_cooling1</a>,    <a href="#channel-id-thermostat_fanmode1">thermostat_fanmode1</a>,    <a href="#channel-id-thermostat_fanstate1">thermostat_fanstate1</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-time_offset1">time_offset1</a>,    <a href="#channel-id-sensor_temperature2">sensor_temperature2</a>,    <a href="#channel-id-sensor_relhumidity2">sensor_relhumidity2</a> </td>
<td></td>
<td></td>
<td>Z-Wave Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-schlage_be469_00_000"></a>schlage_be469_00_000</td>
<td>  <a href="#channel-id-lock_door">lock_door</a> </td>
<td></td>
<td></td>
<td>Touchscreen Deadbolt</td>
</tr>
<tr>
<td><a name="thing-id-schlage_fe599nx_00_000"></a>schlage_fe599nx_00_000</td>
<td>  <a href="#channel-id-lock_door">lock_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Connected Keypad with Lever</td>
</tr>
<tr>
<td><a name="thing-id-sensative_1101011_00_000"></a>sensative_1101011_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Strips-MaZw</td>
</tr>
<tr>
<td><a name="thing-id-shenzhen_doorwindowsensor_00_000"></a>shenzhen_doorwindowsensor_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_access">alarm_access</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Door/Window Sensor&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;1) Disassemble the door sensor main body and insert the battery. Make sure the&lt;/p&gt; &lt;p&gt;device is located within the direct range of the controller.&lt;/p&gt; &lt;p&gt;2) Set the controller into the learning mode (see mail controller’s operating manual).&lt;/p&gt; &lt;p&gt;3) Quickly, triple click the code button, LED light will flash for 5 times.&lt;/p&gt; &lt;p&gt;4) Door sensor will be detected and included in the Z-Wave network.&lt;/p&gt; &lt;p&gt;5) Wait for the main controller to configure the sensor.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;1) Make sure the sensor is connected to power source.&lt;/p&gt; &lt;p&gt;2) Set the main controller into the learning mode (see main controller’s operating&lt;/p&gt; &lt;p&gt;manual).&lt;/p&gt; &lt;p&gt;3) Quickly, triple click the code button, LED light will flash for 5 times.&lt;/p&gt; &lt;p&gt;4) Wait for the main controller to delete the sensor.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;The minimum wakeup interval is 300s (5 minutes)&lt;/p&gt; &lt;p&gt;The maximum wakeup interval is 16,777,200s (about 194 days)&lt;/p&gt; &lt;p&gt;Allowable min step among each wakeup interval is 60 seconds, such as 360s, 420s, 480s…&lt;/p&gt; &lt;p&gt;Note: The default value is 12 hours. This value is longer, the battery life is greater.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-shenzhen_motionsensor_00_000"></a>shenzhen_motionsensor_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;PIR Motion Sensor&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;1) Disassemble the PIR main body and insert the battery into the contact sensor. Make&lt;/p&gt; &lt;p&gt;sure the device is located within the direct range of the controller.&lt;/p&gt; &lt;p&gt;2) Set the controller into the learning mode (see mail controller’s operating manual).&lt;/p&gt; &lt;p&gt;3) Quickly, triple click the code button, LED light will flash red for 5 times.&lt;/p&gt; &lt;p&gt;4) PIT will be detected and included in the Z-Wave network.&lt;/p&gt; &lt;p&gt;5) Wait for the main controller to configure the PIR.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;1) Make sure the sensor is connected to power source.&lt;/p&gt; &lt;p&gt;2) Set the main controller into the learning mode (see main controller’s operating&lt;/p&gt; &lt;p&gt;manual).&lt;/p&gt; &lt;p&gt;3) Quickly, triple click the code button, LED light will flash red for 5 times.&lt;/p&gt; &lt;p&gt;4) Wait for the main controller to delete the sensor.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Wakeup Information&lt;/h2&gt;&lt;p&gt;The minimum wakeup interval is 300s&lt;/p&gt; &lt;p&gt;The maximum wakeup interval is 16,777,200s (about 194 days)&lt;/p&gt; &lt;p&gt;Allowable interval among each wakeup interval is 60 second, such as 360, 420, 480…&lt;/p&gt; &lt;p&gt;Note: The default value is 12 hours. This value is longer, the battery life is greater.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-shenzhen_powerplug_00_000"></a>shenzhen_powerplug_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-alarm_power">alarm_power</a> </td>
<td></td>
<td></td>
<td>&#10;Metered Wall Plug Switch&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;This Plug provides line voltage, current load, power consumption and energy&lt;/p&gt; &lt;p&gt;consumption measuring. &lt;/p&gt; &lt;p&gt;&lt;strong&gt;Voltage&lt;/strong&gt; – The Supply Power Voltage For Plug.&lt;/p&gt; &lt;p&gt;&lt;strong&gt;Current&lt;/strong&gt; – The Current for the Electric Device Connect to Plug Consumption.&lt;/p&gt; &lt;p&gt;&lt;strong&gt;Power&lt;/strong&gt; – Power Consumed by an Electric Device in an instant, unit: Watt (W).&lt;/p&gt; &lt;p&gt;&lt;strong&gt;Energy&lt;/strong&gt; – Energy Consumed by an Electric Device through a Time Period. Most&lt;/p&gt; &lt;p&gt;commonly measured in Kilowatt-hours (kWh). One kilowatt-hour is Equal to One&lt;/p&gt; &lt;p&gt;Kilowatt of Power Consumed over a Period of One Hour, 1kWh = 1000Wh.0Wh.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;1) Plug the power plug in the socket. Make sure the device is located within the direct&lt;/p&gt; &lt;p&gt;range of the main controller.&lt;/p&gt; &lt;p&gt;2) Set the main controller to the learning mode&lt;/p&gt; &lt;p&gt;3) Quickly, triple click the code button, the device will enter inclusion mode, and the&lt;/p&gt; &lt;p&gt;LED light will flash red and yellow five times .&lt;/p&gt; &lt;p&gt;4) Power plug will be detected and added to the Z-Wave network.&lt;/p&gt; &lt;p&gt;5) Wait for the main controller to configure the sensor.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;1) Make sure the sensor is connected to power source.&lt;/p&gt; &lt;p&gt;2) Set the main controller to the learning mode&lt;/p&gt; &lt;p&gt;3) Quickly, triple click the code button, LED light will flash red and yellow five&lt;/p&gt; &lt;p&gt;times.&lt;/p&gt; &lt;p&gt;4) Wait for the main controller to delete the sensor&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-somfy_zrtsi_00_000"></a>somfy_zrtsi_00_000</td>
<td></td>
<td></td>
<td></td>
<td>Z-Wave to RTS Interface Controller</td>
</tr>
<tr>
<td><a name="thing-id-somfy_zrtsivnode_00_000"></a>somfy_zrtsivnode_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-blinds_control">blinds_control</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Z-Wave to RTS Interface Virtual Node</td>
</tr>
<tr>
<td><a name="thing-id-stelpro_stzw402_00_000"></a>stelpro_stzw402_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a> </td>
<td></td>
<td></td>
<td>4000W Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-swiid_swzcs1_00_000"></a>swiid_swzcs1_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Cord Switch</td>
</tr>
<tr>
<td><a name="thing-id-telldus_tzdw100_00_000"></a>telldus_tzdw100_00_000</td>
<td>  <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Door/window sensor</td>
</tr>
<tr>
<td><a name="thing-id-telldus_tzwp100_00_000"></a>telldus_tzwp100_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Wall Plug Switch</td>
</tr>
<tr>
<td><a name="thing-id-thermofloor_tf016_00_000"></a>thermofloor_tf016_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_setpoint_furnace">thermostat_setpoint_furnace</a>,    <a href="#channel-id-thermostat_setpoint_heating_econ">thermostat_setpoint_heating_econ</a> </td>
<td></td>
<td></td>
<td>ZWave Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-thermofloor_tf016_01_008"></a>thermofloor_tf016_01_008</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_setpoint_heating_econ">thermostat_setpoint_heating_econ</a>,    <a href="#channel-id-thermostat_setpoint_furnace">thermostat_setpoint_furnace</a> </td>
<td></td>
<td></td>
<td>ZWave Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-tkb_tsm02_00_000"></a>tkb_tsm02_00_000</td>
<td>  <a href="#channel-id-alarm_motion">alarm_motion</a>,    <a href="#channel-id-alarm_tamper">alarm_tamper</a>,    <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Slim Multi-Sensor</td>
</tr>
<tr>
<td><a name="thing-id-tkb_tz08_00_000"></a>tkb_tz08_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a>,    <a href="#channel-id-meter_powerfactor">meter_powerfactor</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-alarm_general">alarm_general</a> </td>
<td></td>
<td></td>
<td>Rollershutter Controller</td>
</tr>
<tr>
<td><a name="thing-id-tkb_tz65d_00_000"></a>tkb_tz65d_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Dual Paddle Wall Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-tkb_tz66d_00_000"></a>tkb_tz66d_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Dual Paddle Wall Switch</td>
</tr>
<tr>
<td><a name="thing-id-tkb_tz67_00_000"></a>tkb_tz67_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Wall Plug Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-tkb_tz68_00_000"></a>tkb_tz68_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>&#10;Wall Plug&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The TKB TZ68 is a switch plug that can be placed between a wall outlet and electric devices, plugged in by cord. It can switch all loads up to 3500 W. The device is IP 20 rated and can therefore only be used in dry environments.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;To confirm Inclusion hit the button on the device.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;To confirm Exclusion hit the button on the device.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-tkb_tz88_00_000"></a>tkb_tz88_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-meter_powerfactor">meter_powerfactor</a>,    <a href="#channel-id-meter_current">meter_current</a>,    <a href="#channel-id-meter_watts">meter_watts</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_voltage">meter_voltage</a> </td>
<td></td>
<td></td>
<td>Energy Monitoring Wall Plug</td>
</tr>
<tr>
<td><a name="thing-id-trane_tzemt400bb32maa_00_000"></a>trane_tzemt400bb32maa_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a>,    <a href="#channel-id-time_offset">time_offset</a> </td>
<td></td>
<td></td>
<td>Trane Z-Wave Programmable Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-trane_xl624_00_000"></a>trane_xl624_00_000</td>
<td>  <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_general">sensor_general</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a> </td>
<td></td>
<td></td>
<td>Touchscreen Comfort Control Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-trane_xr524_00_000"></a>trane_xr524_00_000</td>
<td>  <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-thermostat_mode">thermostat_mode</a>,    <a href="#channel-id-thermostat_state">thermostat_state</a>,    <a href="#channel-id-thermostat_setpoint_heating">thermostat_setpoint_heating</a>,    <a href="#channel-id-thermostat_setpoint_cooling">thermostat_setpoint_cooling</a>,    <a href="#channel-id-thermostat_fanmode">thermostat_fanmode</a>,    <a href="#channel-id-thermostat_fanstate">thermostat_fanstate</a> </td>
<td></td>
<td></td>
<td>Touchscreen Comfort Control Thermostat</td>
</tr>
<tr>
<td><a name="thing-id-vision_zd2102_00_000"></a>vision_zd2102_00_000</td>
<td>  <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-sensor_door">sensor_door</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Door Window Sensor&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;This sensor monitors door/ window and send Z-Wave signal when door or window is opened and closed.&lt;/p&gt; &lt;p&gt;When the device is secure included into Z-Wave network, above communication will be encrypted.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-vision_zg8101_00_000"></a>vision_zg8101_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Garage Door Tilt Sensor</td>
</tr>
<tr>
<td><a name="thing-id-vision_zl7101_00_000"></a>vision_zl7101_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>Lamp Dimmer Module</td>
</tr>
<tr>
<td><a name="thing-id-vision_zl7431_00_000"></a>vision_zl7431_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>In Wall Relay Switch</td>
</tr>
<tr>
<td><a name="thing-id-vision_zl7432_00_000"></a>vision_zl7432_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>In Wall Dual Relay Switch</td>
</tr>
<tr>
<td><a name="thing-id-vision_zm1601_00_000"></a>vision_zm1601_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Battery Siren</td>
</tr>
<tr>
<td><a name="thing-id-vision_zm1602_00_000"></a>vision_zm1602_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>AC/DC Siren</td>
</tr>
<tr>
<td><a name="thing-id-vision_zm1702_00_000"></a>vision_zm1702_00_000</td>
<td>  <a href="#channel-id-lock_door">lock_door</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-alarm_entry">alarm_entry</a> </td>
<td></td>
<td></td>
<td>&#10;Door Lock with Handle&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;The ZM1702 is a Z-Wave controllable, single dead bolt lock. The mechanics can be adopted to right or left opening doors. The door lock can be applied for doors from a thickness of 38 mm and up. The door can be locked and unlocked using the inner side turn piece and/or the key pad. The wireless control allows to lock/unlock the lock, set/unset up to 15 different key codes (4 - 8 key long) and to limit the validity of certain key code.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;Press Keypads &#39;C&#39;, &#39;8&#39;, &#39;8&#39;, &#39;8&#39; and manually turn the door lock from inside to be included.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;Press Keypads &#39;C&#39;, &#39;8&#39;, &#39;8&#39;, &#39;8&#39; and manually turn the door lock from inside to be excluded.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-vision_zp3102_00_000"></a>vision_zp3102_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Motion Sensor</td>
</tr>
<tr>
<td><a name="thing-id-vision_zp3103_00_000"></a>vision_zp3103_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Shock Sensor</td>
</tr>
<tr>
<td><a name="thing-id-vision_zse40_00_000"></a>vision_zse40_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Zooz 4-in-one motion/temperature/humidity/luminance sensor</td>
</tr>
<tr>
<td><a name="thing-id-vision_zw4101_00_000"></a>vision_zw4101_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-switch_dimmer">switch_dimmer</a> </td>
<td></td>
<td></td>
<td>&#10;Drapery controller (up/stop/down)&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Drapery controller, supports up/stop/down commands.&lt;/p&gt; &lt;p&gt;Devices is OEMed as Monoprice 11993.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-widom_ubs_01_000"></a>widom_ubs_01_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>WiDom Universal Relay Switch</td>
</tr>
<tr>
<td><a name="thing-id-wintop_1122r_01_004"></a>wintop_1122r_01_004</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-switch_binary1">switch_binary1</a>,    <a href="#channel-id-switch_binary2">switch_binary2</a> </td>
<td></td>
<td></td>
<td>Dual In-wall Switch</td>
</tr>
<tr>
<td><a name="thing-id-wintop_dhszwdmiw01_00_000"></a>wintop_dhszwdmiw01_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>DHS Z-Wave Micro Dimmer</td>
</tr>
<tr>
<td><a name="thing-id-wintop_easyplug_00_000"></a>wintop_easyplug_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-meter_watts">meter_watts</a> </td>
<td></td>
<td></td>
<td>Wall Plug</td>
</tr>
<tr>
<td><a name="thing-id-wintop_ishutter_00_000"></a>wintop_ishutter_00_000</td>
<td></td>
<td></td>
<td></td>
<td>Wintop iShutter</td>
</tr>
<tr>
<td><a name="thing-id-wintop_le120_00_000"></a>wintop_le120_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Appliance Module</td>
</tr>
<tr>
<td><a name="thing-id-wintop_multisensor_00_000"></a>wintop_multisensor_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a>,    <a href="#channel-id-sensor_temperature1">sensor_temperature1</a>,    <a href="#channel-id-sensor_luminance2">sensor_luminance2</a> </td>
<td></td>
<td></td>
<td>Wintop Multi-sensor</td>
</tr>
<tr>
<td><a name="thing-id-wintop_wtrfid_00_000"></a>wintop_wtrfid_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-alarm_burglar">alarm_burglar</a>,    <a href="#channel-id-alarm_access">alarm_access</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Mini Keypad RFID/Z-Wave</td>
</tr>
<tr>
<td><a name="thing-id-yale_ykfcon_00_000"></a>yale_ykfcon_00_000</td>
<td>  <a href="#channel-id-lock_door">lock_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Smart Living Keyfree Smart Lock</td>
</tr>
<tr>
<td><a name="thing-id-yale_yrd120_00_000"></a>yale_yrd120_00_000</td>
<td>  <a href="#channel-id-lock_door">lock_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Touchscreen deadbolt without key&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;The lock can be included in a Z-Wave Network from the touchscreen as follows:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Touch the lock screen to activate&lt;/li&gt; &lt;li&gt;Enter the master pin and press #&lt;/li&gt; &lt;li&gt;Press 7, then #&lt;/li&gt; &lt;li&gt;Press 1, then #&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;The lock will now be in inclusion mode.  Alternatively, the lock can be included by pressing and holding the radio button until the unit beeps 2 times. Release the button and the unit will enter inclusion mode.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;The lock can be excluded from a Z-Wave Network from the touchscreen as follows:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Touch the lock screen to activate&lt;/li&gt; &lt;li&gt;Enter the master pin and press #&lt;/li&gt; &lt;li&gt;Press 7, then #&lt;/li&gt; &lt;li&gt;Press 3, then #&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;The lock will now be in exclusion mode.  Alternatively, the lock can be excluded by pressing and holding the radio button until the unit beeps 5 times. Release the button and the unit will enter exclusion mode.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-yale_yrd210_00_000"></a>yale_yrd210_00_000</td>
<td>  <a href="#channel-id-lock_door">lock_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Push Button Deadbolt</td>
</tr>
<tr>
<td><a name="thing-id-yale_yrd220_00_000"></a>yale_yrd220_00_000</td>
<td>  <a href="#channel-id-lock_door">lock_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Yale Real Living Touchscreen Deadbolt&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;To include the lock in a Z-Wave Network (taken from the installation manual:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Touch the lock screen to activate&lt;/li&gt; &lt;li&gt;Enter the master pin and press #&lt;/li&gt; &lt;li&gt;Press 7, then #&lt;/li&gt; &lt;li&gt;Press 1, then #&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;The lock will now be in inclusion mode.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;To exclude the lock from a Z-Wave Network (taken from the installation manual:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Touch the lock screen to activate&lt;/li&gt; &lt;li&gt;Enter the master pin and press #&lt;/li&gt; &lt;li&gt;Press 7, then #&lt;/li&gt; &lt;li&gt;Press 3, then #&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;The lock will now be in exclusion mode.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-yale_yrl220_00_000"></a>yale_yrl220_00_000</td>
<td>  <a href="#channel-id-lock_door">lock_door</a>,    <a href="#channel-id-alarm_general">alarm_general</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>&#10;Yale Real Living Touchscreen Lever Lock&lt;br /&gt;&lt;h2&gt;Inclusion Information&lt;/h2&gt;&lt;p&gt;To include the lock in a Z-Wave Network (taken from the installation manual:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Touch the lock screen to activate&lt;/li&gt; &lt;li&gt;Enter the master pin and press #&lt;/li&gt; &lt;li&gt;Press 7, then #&lt;/li&gt; &lt;li&gt;Press 1, then #&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;The lock will now be in inclusion mode.&lt;/p&gt; &lt;br /&gt;&lt;h2&gt;Exclusion Information&lt;/h2&gt;&lt;p&gt;To exclude the lock from a Z-Wave Network (taken from the installation manual:&lt;/p&gt; &lt;ol&gt;&lt;li&gt;Touch the lock screen to activate&lt;/li&gt; &lt;li&gt;Enter the master pin and press #&lt;/li&gt; &lt;li&gt;Press 7, then #&lt;/li&gt; &lt;li&gt;Press 3, then #&lt;/li&gt; &lt;/ol&gt;&lt;p&gt;The lock will now be in exclusion mode.&lt;/p&gt;&#10;    </td>
</tr>
<tr>
<td><a name="thing-id-zipato_rgbwe27zw_00_000"></a>zipato_rgbwe27zw_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-color_color">color_color</a>,    <a href="#channel-id-color_temperature">color_temperature</a> </td>
<td></td>
<td></td>
<td>RGBW bulb</td>
</tr>
<tr>
<td><a name="thing-id-zwaveme_kfob_00_000"></a>zwaveme_kfob_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>4 button keyfob</td>
</tr>
<tr>
<td><a name="thing-id-zwaveme_wallcs_00_000"></a>zwaveme_wallcs_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Wall Controller</td>
</tr>
<tr>
<td><a name="thing-id-zwaveme_zme05431_00_000"></a>zwaveme_zme05431_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a> </td>
<td></td>
<td></td>
<td>Flush mountable switch</td>
</tr>
<tr>
<td><a name="thing-id-zwaveme_zme05433_00_000"></a>zwaveme_zme05433_00_000</td>
<td>  <a href="#channel-id-switch_dimmer">switch_dimmer</a>,    <a href="#channel-id-scene_number">scene_number</a> </td>
<td></td>
<td></td>
<td>Zwave.Me Dimmer Set Everlux</td>
</tr>
<tr>
<td><a name="thing-id-zwaveme_zme05459_00_000"></a>zwaveme_zme05459_00_000</td>
<td>  <a href="#channel-id-switch_binary">switch_binary</a>,    <a href="#channel-id-blinds_control">blinds_control</a> </td>
<td></td>
<td></td>
<td>Blinds controller</td>
</tr>
<tr>
<td><a name="thing-id-zwaveme_zme06443_00_000"></a>zwaveme_zme06443_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Single Paddle Wall Controller</td>
</tr>
<tr>
<td><a name="thing-id-zwaveme_zmerc2_00_000"></a>zwaveme_zmerc2_00_000</td>
<td>  <a href="#channel-id-scene_number">scene_number</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Z-Wave Remote Control+</td>
</tr>
<tr>
<td><a name="thing-id-zwaveme_zweather_00_000"></a>zwaveme_zweather_00_000</td>
<td>  <a href="#channel-id-sensor_binary">sensor_binary</a>,    <a href="#channel-id-sensor_luminance">sensor_luminance</a>,    <a href="#channel-id-sensor_velocity">sensor_velocity</a>,    <a href="#channel-id-sensor_barpressure">sensor_barpressure</a>,    <a href="#channel-id-sensor_temperature">sensor_temperature</a>,    <a href="#channel-id-sensor_relhumidity">sensor_relhumidity</a>,    <a href="#channel-id-sensor_dewpoint">sensor_dewpoint</a>,    <a href="#channel-id-meter_pulse">meter_pulse</a>,    <a href="#channel-id-meter_kwh">meter_kwh</a>,    <a href="#channel-id-battery-level">battery-level</a> </td>
<td></td>
<td></td>
<td>Z-Wave weather interface</td>
</tr>
</tbody>
</table>