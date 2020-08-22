package org.openhab.binding.zwave.discovery;

import static java.util.Arrays.asList;
import static org.openhab.binding.zwave.ZWaveBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.config.discovery.mdns.internal.MDNSDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link ZWaveRFC2217DiscoveryParticipant} is responsible for discovering remote ZWave controllers that are
 * available through RFC2217. It uses {@link MDNSDiscoveryService}.
 *
 * @author Aitor Iturrioz - Initial contribution
 *
 */
@Component(service = MDNSDiscoveryParticipant.class, immediate = true)
@NonNullByDefault
public class ZWaveRFC2217DiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(asList(CONTROLLER_SERIAL));

    public static final String ZWAVE_CONTROLLER_VENDOR_ID = "10c4";
    public static final String ZWAVE_CONTROLLER_MODEL_ID = "ea60";
    public static final String ZWAVE_CONTROLLER_DEFAULT_LABEL = "ZWave RFC2217 Controler";

    public static final String ZWAVE_PLUS_CONTROLLER_VENDOR_ID = "0658";
    public static final String ZWAVE_PLUS_CONTROLLER_MODEL_ID = "0200";
    public static final String ZWAVE_PLUS_CONTROLLER_DEFAULT_LABEL = "ZWave Plus RFC2217 Controller";

    private static final String SERVICE_TYPE = "_rfc2217._tcp.local.";

    @Override
    public Set<@NonNull ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID thingUID = getThingUID(service);
        if (thingUID != null) {
            String ipAddress = service.getHostAddresses()[0];
            int port = service.getPort();
            String port_id = String.format("rfc2217://%s:%d", ipAddress, port);

            String label = String.format("%s (%s)",
                    isZWaveController(service) ? ZWAVE_CONTROLLER_DEFAULT_LABEL : ZWAVE_PLUS_CONTROLLER_DEFAULT_LABEL,
                    ipAddress);

            return DiscoveryResultBuilder.create(thingUID).withProperty(CONFIGURATION_PORT, port_id)
                    .withRepresentationProperty(CONFIGURATION_PORT).withLabel(label).build();
        }
        return null;

    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        if (service.getType() != null && service.getType().equals(getServiceType())) {
            if (isZWaveController(service) || isZWavePlusController(service)) {
                String ipAddress = getIPAddress(service);
                if (ipAddress != null) {
                    String ipAddressLastOctet = getIPAddressLastOctet(ipAddress);
                    String id = String.format("%s%s%s", service.getPropertyString("VENDOR_ID"),
                            service.getPropertyString("MODEL_ID"), ipAddressLastOctet);

                    return new ThingUID(CONTROLLER_SERIAL, id);
                }
            }
        }
        return null;
    }

    private boolean isZWaveController(ServiceInfo service) {
        return ZWAVE_CONTROLLER_VENDOR_ID.equals(service.getPropertyString("VENDOR_ID"))
                && ZWAVE_CONTROLLER_MODEL_ID.equals(service.getPropertyString("MODEL_ID"));
    }

    private boolean isZWavePlusController(ServiceInfo service) {
        return ZWAVE_PLUS_CONTROLLER_VENDOR_ID.equals(service.getPropertyString("VENDOR_ID"))
                && ZWAVE_PLUS_CONTROLLER_MODEL_ID.equals(service.getPropertyString("MODEL_ID"));
    }

    private @Nullable String getIPAddress(ServiceInfo service) {
        if (service.getHostAddresses() != null && service.getHostAddresses().length > 0
                && !service.getHostAddresses()[0].isEmpty()) {
            return service.getHostAddresses()[0];
        }
        return null;
    }

    /**
     * Get the last octet from an IP address.
     * For example, for "192.168.1.15", "15" will be returned.
     */
    private String getIPAddressLastOctet(String ipAddress) {
        String[] ipAddressChunk = ipAddress.split("\\.");
        return ipAddressChunk[ipAddressChunk.length - 1];

    }
}
