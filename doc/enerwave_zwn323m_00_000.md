# Plug-In Smart Meter Dimmer Switch ZWN323M

| Thing Type UID | enerwave_zwn323m_00_000               |
| Manufacturer   | Wenzhou MTLC Electric Appliances Co.,Ltd.  |
| Model          | Plug-In Smart Meter Dimmer Switch |

ZWN323M

## Channels
The following table summarises the channels available for the Plug-In Smart Meter Dimmer Switch ZWN323M.

| Channel | Channel Id | Channel Type UID | Category | Item Type |
|---------|------------|------------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch |
| Dimmer | switch_dimmer | switch_dimmer | DimmableLight | Dimmer |
| Sensor (power) | sensor_power | sensor_power | Energy | Number |
| Electric meter (watts) | meter_watts | meter_watts | Energy | Number |
| Electric meter (kWh) | meter_kwh | meter_kwh | Energy | Number |



### Dimmer

Restore Last Dimming level on ON.

| Configuration ID | config_restoreLastValue           |
| Data Type        | BOOLEAN           |
| Range            |  to  |
| Default Value    |         |
| Options          | |


### Device Configuration
The following table provides a summary of the configuration parameters available in the Plug-In Smart Meter Dimmer Switch ZWN323M. Detailed parameter information can be found below.

| Parameter   | Description |
|-------------|-------------|
| 1:  | Synchronization of load power and LED indicator |
| 8:  | Instant Energy Autosend Interval (send METER_REPORT) |
| 9:  | Instant Energy Autosend interval (send SENSOR_MULTILEVEL_REPORT) |
| 10:  | Accumulated Energy Autosend Interval report (send METER_REPORT) |
| 11:  | 
Enable automatic notifications to associated device&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Device will send a notification whenever the is a wattage change&lt;/p&gt;
         |
| 12:  | 
Minimum change in wattage report&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;0-255: 0.0-25.5W&lt;/p&gt;
         |
| 1: Notifications | Lifeline |
| 2: StatusReport | Send basic report |
| 3: PowerReport | PowerReport: Send meter power report |





#### 1: 

Synchronization of load power and LED indicator

| Configuration ID | config_1_1           |
| Data Type        | INTEGER           |
| Range            |  to  |
| Default Value    |         |
| Options          | |




#### 8: 

Instant Energy Autosend Interval (send METER_REPORT)

| Configuration ID | config_8_1           |
| Data Type        | INTEGER           |
| Range            | 0 to 255 |
| Default Value    |         |




#### 9: 

Instant Energy Autosend interval (send SENSOR_MULTILEVEL_REPORT)

| Configuration ID | config_9_1           |
| Data Type        | INTEGER           |
| Range            | 0 to 255 |
| Default Value    |         |




#### 10: 

Accumulated Energy Autosend Interval report (send METER_REPORT)

| Configuration ID | config_10_1           |
| Data Type        | INTEGER           |
| Range            | 0 to 255 |
| Default Value    |         |




#### 11: 


Enable automatic notifications to associated device&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;Device will send a notification whenever the is a wattage change&lt;/p&gt;
        

| Configuration ID | config_11_1           |
| Data Type        | INTEGER           |
| Range            |  to  |
| Default Value    |         |
| Options          | |




#### 12: 


Minimum change in wattage report&lt;br /&gt;&lt;h1&gt;Overview&lt;/h1&gt;&lt;p&gt;0-255: 0.0-25.5W&lt;/p&gt;
        

| Configuration ID | config_12_1           |
| Data Type        | INTEGER           |
| Range            | 0 to 255 |
| Default Value    |         |




#### 1: Notifications

Lifeline

| Configuration ID | group_1           |
| Data Type        | TEXT           |
| Range            |  to  |
| Default Value    |         |




#### 2: StatusReport

Send basic report

| Configuration ID | group_2           |
| Data Type        | TEXT           |
| Range            |  to  |
| Default Value    |         |




#### 3: PowerReport

PowerReport: Send meter power report

| Configuration ID | group_3           |
| Data Type        | TEXT           |
| Range            |  to  |
| Default Value    |         |



Did you spot an error in the above definition or want to improve the content? You can edit the database [here](http://www.cd-jackson.com/index.html/756).

