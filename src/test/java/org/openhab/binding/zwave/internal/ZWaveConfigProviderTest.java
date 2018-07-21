package org.openhab.binding.zwave.internal;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.junit.Test;

/**
 *
 * @author Chris Jackson
 *
 */
public class ZWaveConfigProviderTest {

    @Test
    public void getConfigDescription() throws URISyntaxException {
        ZWaveConfigProvider provider = new ZWaveConfigProvider();

        // Serial controller needs to return null for the thing-type
        URI uri = new URI("thing-type:zwave:serial_zstick");
        assertNull(provider.getConfigDescription(uri, null));

        // Other devices return the node_id
        uri = new URI("thing-type:zwave:device");
        ConfigDescription config = provider.getConfigDescription(uri, null);
        assertEquals(uri, config.getUID());
        ConfigDescriptionParameter parameter = config.getParameters().get(0);
        assertEquals("node_id", parameter.getName());

        // No registry set so null response
        uri = new URI("thing:zwave:device:controller:node1");
        assertNull(provider.getConfigDescription(uri, null));

        // Configuration configuration = Mockito.mock(Configuration.class);
        // Mockito.when(configuration.get(Matchers.any())).thenReturn(new BigDecimal("1"));

        // ZWaveControllerHandler controllerHandler = Mockito.mock(ZWaveControllerHandler.class);
        // Thing bridge = Mockito.mock(Thing.class);
        // Mockito.when(bridge.getHandler()).thenReturn(controllerHandler);

        // ThingUID bridgeUid = new ThingUID("zwave:serial_zstick:xxx");
        // ThingUID thingUid = new ThingUID("thing:zwave:device:controller:node1");

        // Thing thing = Mockito.mock(Thing.class);
        // Mockito.when(thing.getBridgeUID()).thenReturn(bridgeUid);
        // Mockito.when(thing.getConfiguration()).thenReturn(configuration);

        // ThingRegistry registry = Mockito.mock(ThingRegistry.class);
        // Mockito.when(registry.get(bridgeUid)).thenReturn(bridge);
        // Mockito.when(registry.get(thingUid)).thenReturn(thing);

        // provider.setThingRegistry(registry);
        // uri = new URI(thingUid.getAsString());
        // config = provider.getConfigDescription(uri, null);
        // parameter = config.getParameters().get(0);
        // assertEquals("node_id", parameter.getName());

    }
}
