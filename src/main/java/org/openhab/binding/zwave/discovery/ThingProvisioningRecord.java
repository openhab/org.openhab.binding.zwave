package org.openhab.binding.zwave.discovery;

/**
 * Information required to provision a Thing prior to it being included into a system. This may be required if a device
 * requires a Device Specific Key that must be provisioned before the device may be detected.
 *
 * @author Chris Jackson
 *
 */
public class ThingProvisioningRecord {
    private String deviceAddress;

    private String deviceKey;

    /**
     * @return the deviceAddress
     */
    public String getDeviceAddress() {
        return deviceAddress;
    }

    /**
     * @param deviceAddress the deviceAddress to set
     */
    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    /**
     * @return the deviceKey
     */
    public String getDeviceKey() {
        return deviceKey;
    }

    /**
     * @param deviceKey the deviceKey to set
     */
    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }
}
