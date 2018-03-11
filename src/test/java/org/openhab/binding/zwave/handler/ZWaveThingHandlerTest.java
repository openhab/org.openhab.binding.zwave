/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.handler;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeBuilder;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;

/**
 * Test of the ZWaveThingHandler
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ZWaveThingHandlerTest {

    class ZWaveThingHandlerForTest extends ZWaveThingHandler {

        public ZWaveThingHandlerForTest(Thing zwaveDevice) {
            super(zwaveDevice);
        }

        @Override
        protected void validateConfigurationParameters(Map<String, Object> configurationParameters) {
        }
    }

    private List<ZWaveCommandClassTransactionPayload> doConfigurationUpdate(String param, Object value) {
        ThingType thingType = ThingTypeBuilder.instance("bindingId", "thingTypeId", "label").build();

        Thing thing = ThingBuilder.create(thingType.getUID(), new ThingUID(thingType.getUID(), "thingId"))
                .withConfiguration(new Configuration()).build();

        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveController controller = Mockito.mock(ZWaveController.class);

        ThingHandlerCallback thingCallback = Mockito.mock(ThingHandlerCallback.class);
        ZWaveThingHandler thingHandler = new ZWaveThingHandlerForTest(thing);
        thingHandler.setCallback(thingCallback);
        ArgumentCaptor<ZWaveCommandClassTransactionPayload> payloadCaptor;
        payloadCaptor = ArgumentCaptor.forClass(ZWaveCommandClassTransactionPayload.class);
        Field fieldControllerHandler;
        try {
            ZWaveWakeUpCommandClass wakeupClass = new ZWaveWakeUpCommandClass(node, controller, null);

            ZWaveAssociationCommandClass associationClass = new ZWaveAssociationCommandClass(node, controller, null);

            ZWaveControllerHandler controllerHandler = Mockito.mock(ZWaveControllerHandler.class);
            Mockito.doNothing().when(node).sendMessage(payloadCaptor.capture());
            Mockito.doNothing().when(thingCallback).thingUpdated(Matchers.any(Thing.class));

            fieldControllerHandler = ZWaveThingHandler.class.getDeclaredField("controllerHandler");
            fieldControllerHandler.setAccessible(true);
            fieldControllerHandler.set(thingHandler, controllerHandler);

            Mockito.when(controller.getOwnNodeId()).thenReturn(1);
            Mockito.when(controllerHandler.getNode(Matchers.anyInt())).thenReturn(node);
            Mockito.when(node.getNodeId()).thenReturn(1);
            Mockito.when(node.getAssociationGroup(Matchers.anyInt())).thenReturn(new ZWaveAssociationGroup(1));
            Mockito.when(node.getCommandClass(Matchers.eq(CommandClass.COMMAND_CLASS_WAKE_UP))).thenReturn(wakeupClass);
            Mockito.when(node.getCommandClass(Matchers.eq(CommandClass.COMMAND_CLASS_ASSOCIATION)))
                    .thenReturn(associationClass);
        } catch (NoSuchFieldException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<String, Object> config = new HashMap<String, Object>();
        config.put(param, value);
        thingHandler.handleConfigurationUpdate(config);

        // Check that the pending status has been updated
        Collection<ConfigStatusMessage> status = thingHandler.getConfigStatus();
        assertEquals(1, status.size());
        assertEquals(status.iterator().next().parameterName, param);

        return payloadCaptor.getAllValues();
    }

    @Test
    public void TestConfigurationWakeup() {
        ZWaveCommandClassTransactionPayload msg;
        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdate(
                ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL, new BigDecimal(600));

        assertEquals(2, response.size());
        msg = response.get(0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), new byte[] { -124, 4, 0, 2, 88, 0 }));
        msg = response.get(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), new byte[] { -124, 5 }));
    }

    @Test
    public void TestConfigurationAssociationList() {
        List<String> nodeList = new ArrayList<String>();
        nodeList.add("node_1");
        nodeList.add("node_2_1");
        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdate("group_1", nodeList);

        // Check that there are only 3 requests - the SET and GET
        // Note that these are currently null due to mocking
        assertEquals(3, response.size());
    }

    @Test
    public void TestConfigurationAssociationRoot() {
        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdate("group_1", "node_1");

        // Check that there are only 2 requests - the SET and GET
        // Note that these are currently null due to mocking
        assertEquals(2, response.size());
    }

    @Test
    public void TestConfigurationAssociationEndpoint() {
        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdate("group_1", "node_1_0");

        // Check that there are only 2 requests - the SET and GET
        // Note that these are currently null due to mocking
        assertEquals(2, response.size());
    }
}
