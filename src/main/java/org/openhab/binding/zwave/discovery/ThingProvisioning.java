package org.openhab.binding.zwave.discovery;

import java.util.List;

/**
 * This interface allows a UI to read and write provisioning information to a binding.
 * <p>
 * Provisioning information is required to be passed to the binding prior to discovery. This interface allows a UI, or
 * other service to pass provisioning information to the binding, and to read the state of the provisioning list.
 *
 * @author Chris Jackson
 *
 */
public interface ThingProvisioning {

    /**
     * Gets the thing provisioning list for display in the UI. The provisioning list is generally stored in the hardware
     * (eg dongle) - this method should retrieve the provisioning list from the dongle and present it to the UI
     *
     * @return List of {@link ThingProvisioningRecord}s
     */
    List<ThingProvisioningRecord> getProvisioningList();

    /**
     * Clears the provisioning list
     * 
     * @return true on success
     */
    boolean clearProvisioningList();

    /**
     * Adds a {@link ThingProvisioningRecord} to the bindings provisioning list
     *
     * @param record the {@link ThingProvisioningRecord} to add
     * @return true on success
     */
    boolean addProvisioningRecord(ThingProvisioningRecord record);
}
