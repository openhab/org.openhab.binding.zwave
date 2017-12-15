
# WA105DBZ Siren

This describes the Z-Wave device **WA105DBZ**, manufactured by **Linear Corp** with the thing type UID of ```linear_wa105dbz_00_000```. 

Siren

## Channels
The following table summarises the channels available for the WA105DBZ Siren.

| Channel | Channel Id | Channel Type UID | Category | Item Type |
|---------|------------|------------------|----------|-----------|
| Switch | switch_binary | switch_binary | Switch | Switch |
|  | battery-level | system.battery-level |  |  |




### Device Configuration
The following table provides a summary of the configuration parameters available in the WA105DBZ Siren.
Detailed information on each parameter can be found below.

| Parameter   | Description |
|-------------|-------------|
| 0: Siren Strobe Mode | Defines the reaction of the siren |
| 1: Alarm auto stop | Defines the auto time out of the alarm indication |
| 1: Update Controller |  |




#### 0: Siren Strobe Mode

Defines the reaction of the siren


| Property         | Value    |
|------------------|----------|
| Configuration ID | config_0_1 |
| Data Type        | INTEGER || Default Value | 0 |
| Options | All Enabled (0) |
|  | Siren Only (1) |
|  | Strobe Only (2) |






#### 1: Alarm auto stop

Defines the auto time out of the alarm indication


| Property         | Value    |
|------------------|----------|
| Configuration ID | config_1_1 |
| Data Type        | INTEGER || Default Value | 0 |
| Options | 30 seconds (0) |
|  | 60 seconds (1) |
|  | 120 seconds (2) |
|  | Continuous (3) |






#### 1: Update Controller




| Property         | Value    |
|------------------|----------|
| Configuration ID | group_1 |
| Data Type        | TEXT |
| Range |  to  |






---

Did you spot an error in the above definition or want to improve the content?
You can edit the database [here](http://www.cd-jackson.com/index.php/zwave/zwave-device-database/zwave-device-list/devicesummary/365).

