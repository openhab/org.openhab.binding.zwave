/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.handler;

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
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingHandler;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;

/**
 * Test of the ZWaveThingHandler
 *
 * @author Chris Jackson - Initial contribution
 *
 */
public class ZWaveThingHandlerTest {

    private List<SerialMessage> doConfigurationUpdate(String param, Object value) {
        ThingType thingType = new ThingType(new ThingTypeUID("bindingId", "thingTypeId"), null, "label", null, null,
                null, null, null);
        Thing thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"),
                new Configuration());

        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveController controller = Mockito.mock(ZWaveController.class);

        ThingHandlerCallback thingCallback = Mockito.mock(ThingHandlerCallback.class);
        ZWaveThingHandler thingHandler = new ZWaveThingHandler(thing);
        thingHandler.setCallback(thingCallback);
        ArgumentCaptor<SerialMessage> argument;
        argument = ArgumentCaptor.forClass(SerialMessage.class);
        Field fieldControllerHandler;
        try {
            ZWaveWakeUpCommandClass wakeupClass = new ZWaveWakeUpCommandClass(node, controller, null);

            ZWaveAssociationCommandClass associationClass = new ZWaveAssociationCommandClass(node, controller, null);

            ZWaveControllerHandler controllerHandler = Mockito.mock(ZWaveControllerHandler.class);
            Mockito.doNothing().when(controllerHandler).sendData(argument.capture());
            Mockito.doNothing().when(thingCallback).thingUpdated(Matchers.any(Thing.class));

            fieldControllerHandler = thingHandler.getClass().getDeclaredField("controllerHandler");
            fieldControllerHandler.setAccessible(true);
            fieldControllerHandler.set(thingHandler, controllerHandler);

            Mockito.when(controller.getOwnNodeId()).thenReturn(1);
            Mockito.when(controllerHandler.getNode(Matchers.anyInt())).thenReturn(node);
            Mockito.when(node.getNodeId()).thenReturn(1);
            Mockito.when(node.getAssociationGroup(Matchers.anyInt())).thenReturn(new ZWaveAssociationGroup(1));
            Mockito.when(node.getCommandClass(Matchers.eq(CommandClass.WAKE_UP))).thenReturn(wakeupClass);
            Mockito.when(node.getCommandClass(Matchers.eq(CommandClass.ASSOCIATION))).thenReturn(associationClass);
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

        return argument.getAllValues();
    }

    @Test
    public void TestConfigurationWakeup() {
        List<SerialMessage> response = doConfigurationUpdate(ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL,
                new BigDecimal(600));

        assertEquals(2, response.size());
        assertTrue(Arrays.equals(response.get(0).getMessageBuffer(),
                new byte[] { 1, 13, 0, 19, 1, 6, -124, 4, 0, 2, 88, 1, 0, 0, 61 }));
        assertTrue(Arrays.equals(response.get(1).getMessageBuffer(),
                new byte[] { 1, 9, 0, 19, 1, 2, -124, 5, 0, 0, 103 }));
    }

    @Test
    public void TestConfigurationAssociation() {
        List<String> nodeList = new ArrayList<String>();
        nodeList.add("node_1_0");
        List<SerialMessage> response = doConfigurationUpdate("group_1", nodeList);

        // Check that there are only 2 requests - the SET and GET
        // Note that these are currently null due to mocking
        assertEquals(2, response.size());
    }
}
