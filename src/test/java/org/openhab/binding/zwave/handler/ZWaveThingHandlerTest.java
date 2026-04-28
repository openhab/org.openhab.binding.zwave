/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zwave.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openhab.binding.zwave.ZWaveBindingConstants;
import org.openhab.binding.zwave.firmwareupdate.ZWaveFirmwareUpdateSession;
import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Basic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Generic;
import org.openhab.binding.zwave.internal.protocol.ZWaveDeviceClass.Specific;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveNodeState;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAssociationCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveNodeNamingCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveWakeUpCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNetworkEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveNodeStatusEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.binding.firmware.ProgressCallback;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.openhab.core.types.Command;

/**
 * Test of the ZWaveThingHandler
 *
 * @author Chris Jackson - Initial contribution
 * @author Robert Eckhoff - Firmware update tests
 *
 */
public class ZWaveThingHandlerTest {
    private ArgumentCaptor<ZWaveCommandClassTransactionPayload> payloadCaptor;
    Configuration configResult;

    class ZWaveThingHandlerForTest extends ZWaveThingHandler {

        public ZWaveThingHandlerForTest(Thing zwaveDevice) {
            super(zwaveDevice);
        }

        @Override
        protected void validateConfigurationParameters(Map<String, Object> configurationParameters) {
        }
    }

    class ZWaveThingHandlerStatusCaptureTest extends ZWaveThingHandler {
        private ThingStatusInfo statusInfo = new ThingStatusInfo(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE,
                null);

        public ZWaveThingHandlerStatusCaptureTest(Thing zwaveDevice) {
            super(zwaveDevice);
        }

        @Override
        protected void validateConfigurationParameters(Map<String, Object> configurationParameters) {
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
            statusInfo = new ThingStatusInfo(status, statusDetail, description);
        }

        public ThingStatusInfo getCapturedStatusInfo() {
            return statusInfo;
        }
    }

    class ZWaveThingHandlerPropertiesCaptureTest extends ZWaveThingHandler {
        private Map<String, String> properties = Map.of();

        public ZWaveThingHandlerPropertiesCaptureTest(Thing zwaveDevice) {
            super(zwaveDevice);
        }

        @Override
        protected void validateConfigurationParameters(Map<String, Object> configurationParameters) {
        }

        @Override
        protected void updateProperties(Map<String, String> properties) {
            this.properties = new HashMap<>(properties);
        }

        public Map<String, String> getCapturedProperties() {
            return properties;
        }
    }

    private ZWaveThingHandler doConfigurationUpdate(String param, Object value) {
        ThingType thingType = ThingTypeBuilder.instance("bindingId", "thingTypeId", "label").build();

        Thing thing = ThingBuilder.create(thingType.getUID(), new ThingUID(thingType.getUID(), "thingId"))
                .withConfiguration(new Configuration()).build();

        ZWaveNode node = Mockito.mock(ZWaveNode.class);
        ZWaveController controller = Mockito.mock(ZWaveController.class);
        ZWaveControllerHandler controllerHandler = Mockito.mock(ZWaveControllerHandler.class);

        Mockito.when(controllerHandler.isControllerMaster()).thenReturn(false);

        ThingHandlerCallback thingCallback = Mockito.mock(ThingHandlerCallback.class);
        ZWaveThingHandler thingHandler = new ZWaveThingHandlerForTest(thing);
        thingHandler.setCallback(thingCallback);
        payloadCaptor = ArgumentCaptor.forClass(ZWaveCommandClassTransactionPayload.class);
        Field fieldControllerHandler;
        try {
            ZWaveWakeUpCommandClass wakeupClass = new ZWaveWakeUpCommandClass(node, controller, null);
            ZWaveAssociationCommandClass associationClass = new ZWaveAssociationCommandClass(node, controller, null);
            ZWaveNodeNamingCommandClass namingClass = new ZWaveNodeNamingCommandClass(node, controller, null);

            Mockito.doNothing().when(node).sendMessage(payloadCaptor.capture());

            fieldControllerHandler = ZWaveThingHandler.class.getDeclaredField("controllerHandler");
            fieldControllerHandler.setAccessible(true);
            fieldControllerHandler.set(thingHandler, controllerHandler);

            Mockito.when(controller.getOwnNodeId()).thenReturn(1);
            Mockito.when(controllerHandler.getOwnNodeId()).thenReturn(1);
            Mockito.when(controllerHandler.getNode(ArgumentMatchers.anyInt())).thenReturn(node);
            Mockito.when(node.getNodeId()).thenReturn(1);
            Mockito.when(node.getAssociationGroup(ArgumentMatchers.anyInt())).thenReturn(new ZWaveAssociationGroup(1));
            Mockito.when(node.getCommandClass(ArgumentMatchers.eq(CommandClass.COMMAND_CLASS_WAKE_UP)))
                    .thenReturn(wakeupClass);
            Mockito.when(node.getCommandClass(ArgumentMatchers.eq(CommandClass.COMMAND_CLASS_ASSOCIATION)))
                    .thenReturn(associationClass);
            Mockito.when(node.getCommandClass(ArgumentMatchers.eq(CommandClass.COMMAND_CLASS_NODE_NAMING)))
                    .thenReturn(namingClass);
        } catch (NoSuchFieldException | SecurityException e) {
            fail(e);
        } catch (IllegalArgumentException e) {
            fail(e);
        } catch (IllegalAccessException e) {
            fail(e);
        }

        Map<String, Object> config = new HashMap<String, Object>();
        config.put(param, value);
        thingHandler.handleConfigurationUpdate(config);

        configResult = thingHandler.getThing().getConfiguration();

        return thingHandler;
    }

    private List<ZWaveCommandClassTransactionPayload> doConfigurationUpdateCommands(String param, Object value) {
        ZWaveThingHandler thingHandler = doConfigurationUpdate(param, value);

        // Check that the pending status has been updated
        Collection<ConfigStatusMessage> status = thingHandler.getConfigStatus();
        assertEquals(1, status.size());
        assertEquals(status.iterator().next().parameterName, param);

        return payloadCaptor.getAllValues();
    }

    private void setNodeId(ZWaveThingHandler thingHandler, int nodeId) {
        try {
            Field field = ZWaveThingHandler.class.getDeclaredField("nodeId");
            field.setAccessible(true);
            field.setInt(thingHandler, nodeId);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }
    }

    private void setFirmwareProgressCallback(ZWaveThingHandler thingHandler, ProgressCallback progressCallback) {
        try {
            Field field = ZWaveThingHandler.class.getDeclaredField("firmwareProgressCallback");
            field.setAccessible(true);
            field.set(thingHandler, progressCallback);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }
    }

    private void setFirmwareSession(ZWaveThingHandler thingHandler, ZWaveFirmwareUpdateSession firmwareSession) {
        try {
            Field field = ZWaveThingHandler.class.getDeclaredField("firmwareSession");
            field.setAccessible(true);
            field.set(thingHandler, firmwareSession);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }
    }

    @Test
    public void testConfigurationLocation() {
        ZWaveCommandClassTransactionPayload msg;
        doConfigurationUpdate(ZWaveBindingConstants.CONFIGURATION_NODELOCATION, null);
        doConfigurationUpdate(ZWaveBindingConstants.CONFIGURATION_NODELOCATION, Integer.valueOf(1));

        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdateCommands(
                ZWaveBindingConstants.CONFIGURATION_NODELOCATION, "Testing");
        assertEquals(1, response.size());
        msg = response.get(0);
        byte[] expectedResponse = { 0x77, 0x04, 0x00, 0x54, 0x65, 0x73, 0x74, 0x69, 0x6E, 0x67 };
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), expectedResponse));
    }

    @Test
    public void testConfigurationWakeup() {
        ZWaveCommandClassTransactionPayload msg;
        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdateCommands(
                ZWaveBindingConstants.CONFIGURATION_WAKEUPINTERVAL, new BigDecimal(600));

        assertEquals(2, response.size());
        msg = response.get(0);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), new byte[] { -124, 4, 0, 2, 88, 1 }));
        msg = response.get(1);
        assertTrue(Arrays.equals(msg.getPayloadBuffer(), new byte[] { -124, 5 }));
    }

    @Test
    public void testConfigurationRepollPeriod() {
        ZWaveThingHandler handler = doConfigurationUpdate(ZWaveBindingConstants.CONFIGURATION_CMDREPOLLPERIOD,
                new BigDecimal(600));
        assertEquals(1, handler.getThing().getConfiguration().getProperties().size());
        Map<String, Object> config = handler.getThing().getConfiguration().getProperties();
        assertEquals(new BigDecimal(600), config.get(ZWaveBindingConstants.CONFIGURATION_CMDREPOLLPERIOD));

        handler = doConfigurationUpdate(ZWaveBindingConstants.CONFIGURATION_CMDREPOLLPERIOD, new BigDecimal(10));
        assertEquals(1, handler.getThing().getConfiguration().getProperties().size());
        config = handler.getThing().getConfiguration().getProperties();
        assertEquals(new BigDecimal(100), config.get(ZWaveBindingConstants.CONFIGURATION_CMDREPOLLPERIOD));

        handler = doConfigurationUpdate(ZWaveBindingConstants.CONFIGURATION_CMDREPOLLPERIOD, new BigDecimal(1000000));
        assertEquals(1, handler.getThing().getConfiguration().getProperties().size());
        config = handler.getThing().getConfiguration().getProperties();
        assertEquals(new BigDecimal(15000), config.get(ZWaveBindingConstants.CONFIGURATION_CMDREPOLLPERIOD));
    }

    @Test
    public void testConfigurationAssociationList() {
        List<String> nodeList = new ArrayList<String>();
        nodeList.add("node_1");
        nodeList.add("node_2_1");
        nodeList.add("node_2");
        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdateCommands("group_1", nodeList);

        List<String> associations = (List<String>) configResult.get("group_1");

        assertTrue(associations.contains("controller"));
        assertTrue(associations.contains("node_2"));
        assertTrue(associations.contains("node_2_1"));

        // Check that there are only 3 requests - the SET and GET
        // Note that these are currently null due to mocking
        assertEquals(4, response.size());
    }

    @Test
    public void testConfigurationAssociationListController() {
        List<String> nodeList = new ArrayList<String>();
        nodeList.add("controller");
        nodeList.add("node_2_1");
        doConfigurationUpdate("group_1", nodeList);

        List<String> associations = (List<String>) configResult.get("group_1");

        assertTrue(associations.contains("controller"));
        assertTrue(associations.contains("node_2_1"));

        // Check that there are only 3 requests - the SET and GET
        // Note that these are currently null due to mocking
        assertEquals(3, payloadCaptor.getAllValues().size());
    }

    @Test
    public void testConfigurationAssociationRoot() {
        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdateCommands("group_1", "node_1");

        // Check that there are only 2 requests - the SET and GET
        // Note that these are currently null due to mocking
        assertEquals(2, response.size());
    }

    @Test
    public void testConfigurationAssociationEndpoint() {
        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdateCommands("group_1", "node_1_0");

        // Check that there are only 2 requests - the SET and GET
        // Note that these are currently null due to mocking
        assertEquals(2, response.size());
    }

    @Test
    public void testConfigurationAssociationStringArray() {
        List<ZWaveCommandClassTransactionPayload> response = doConfigurationUpdateCommands("group_1", "[node_1]");

        // Check that there are only 2 requests - the SET and GET
        // Note that these are currently null due to mocking
        assertEquals(2, response.size());

        response = doConfigurationUpdateCommands("group_1", "[node_1, node_2_1, node_2]");
    }

    @Test
    public void testConvertToDataType() {
        ZWaveThingHandler sut = new ZWaveThingHandlerForTest(null);

        ChannelUID channelUID = new ChannelUID("channel:for:a:test");
        Command command = new QuantityType<>("22 °C");

        Command result = sut.convertCommandToDataType(channelUID, ZWaveThingChannel.DataType.DecimalType, command,
                ZWaveThingChannel.DataType.QuantityType);

        assertTrue(result instanceof DecimalType);
    }

    @Test
    public void testConvertToDataTypeFails() {
        ZWaveThingHandler sut = new ZWaveThingHandlerForTest(null);

        ChannelUID channelUID = new ChannelUID("channel:for:a:test");

        // couldnt convert to channel data-type because channel data-type is not an instance of State
        Command result = sut.convertCommandToDataType(channelUID, ZWaveThingChannel.DataType.StopMoveType,
                StopMoveType.STOP, ZWaveThingChannel.DataType.DecimalType);

        assertNull(result);

        // command is not an instance of State and couldnt be converted to something
        result = sut.convertCommandToDataType(channelUID, ZWaveThingChannel.DataType.DecimalType, StopMoveType.STOP,
                ZWaveThingChannel.DataType.StopMoveType);

        assertNull(result);
    }

    @Test
    public void getZWaveProperties() {
        ZWaveThingHandler thingHandler = new ZWaveThingHandlerForTest(Mockito.mock(Thing.class));

        Map<String, String> properties;

        properties = thingHandler.getZWaveProperties("");
        assertEquals(0, properties.size());

        properties = thingHandler.getZWaveProperties("arg");
        assertEquals(1, properties.size());
        assertTrue(properties.containsKey("arg"));
        assertNull(properties.get("arg"));

        properties = thingHandler.getZWaveProperties("arg=");
        assertEquals(1, properties.size());
        assertTrue(properties.containsKey("arg"));
        assertNull(properties.get("arg"));

        properties = thingHandler.getZWaveProperties("arg=val");
        assertEquals(1, properties.size());
        assertTrue(properties.containsKey("arg"));
        assertEquals("val", properties.get("arg"));

        properties = thingHandler.getZWaveProperties("arg1=val1,arg2=val2 , arg3 = val3 , arg4");
        assertEquals(4, properties.size());
        assertTrue(properties.containsKey("arg1"));
        assertEquals("val1", properties.get("arg1"));
        assertTrue(properties.containsKey("arg2"));
        assertEquals("val2", properties.get("arg2"));
        assertTrue(properties.containsKey("arg3"));
        assertEquals("val3", properties.get("arg3"));
        assertTrue(properties.containsKey("arg4"));
        assertNull(properties.get("arg4"));
    }

    static Stream<Arguments> firmwareFailureCases() {
        return Stream.of(Arguments.of(Integer.valueOf(1), "Firmware update failed (status 1)", "status 1"),
                Arguments.of("ERROR_TRANSMISSION_FAILED", "Firmware update failed: ERROR_TRANSMISSION_FAILED",
                        "ERROR_TRANSMISSION_FAILED"));
    }

    @ParameterizedTest
    @MethodSource("firmwareFailureCases")
    public void testFirmwareUpdateFailureSetsConfigurationErrorStatusAndReportsCallback(Object failureValue,
            String expectedDescription, String expectedCallbackDetail) {
        ThingType thingType = ThingTypeBuilder.instance("bindingId", "thingTypeId", "label").build();
        Thing thing = ThingBuilder.create(thingType.getUID(), new ThingUID(thingType.getUID(), "thingId"))
                .withConfiguration(new Configuration()).build();

        ZWaveThingHandlerStatusCaptureTest handler = new ZWaveThingHandlerStatusCaptureTest(thing);
        ProgressCallback progressCallback = Mockito.mock(ProgressCallback.class);
        setNodeId(handler, 12);
        setFirmwareProgressCallback(handler, progressCallback);

        handler.ZWaveIncomingEvent(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.FirmwareUpdate, 12,
                ZWaveNetworkEvent.State.Failure, failureValue));

        ThingStatusInfo statusInfo = handler.getCapturedStatusInfo();
        assertEquals(ThingStatus.ONLINE, statusInfo.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, statusInfo.getStatusDetail());
        assertEquals(expectedDescription, statusInfo.getDescription());
        Mockito.verify(progressCallback).failed("actions.firmware-update.error", expectedCallbackDetail);
    }

    @Test
    public void testFirmwareUpdateFailureIgnoresCallbackRuntimeException() {
        ThingType thingType = ThingTypeBuilder.instance("bindingId", "thingTypeId", "label").build();
        Thing thing = ThingBuilder.create(thingType.getUID(), new ThingUID(thingType.getUID(), "thingId"))
                .withConfiguration(new Configuration()).build();

        ZWaveThingHandlerStatusCaptureTest handler = new ZWaveThingHandlerStatusCaptureTest(thing);
        ProgressCallback progressCallback = Mockito.mock(ProgressCallback.class);
        Mockito.doThrow(new IllegalStateException("Timer already cancelled.")).when(progressCallback)
                .failed(Mockito.anyString(), Mockito.anyString());

        setNodeId(handler, 12);
        setFirmwareProgressCallback(handler, progressCallback);

        assertDoesNotThrow(() -> handler.ZWaveIncomingEvent(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.FirmwareUpdate,
                12, ZWaveNetworkEvent.State.Failure, "ERROR_TRANSMISSION_FAILED")));

        ThingStatusInfo statusInfo = handler.getCapturedStatusInfo();
        assertEquals(ThingStatus.ONLINE, statusInfo.getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, statusInfo.getStatusDetail());
        assertEquals("Firmware update failed: ERROR_TRANSMISSION_FAILED", statusInfo.getDescription());
    }

    @Test
    public void testFirmwareUpdateProgressRestoredAfterCommunicationDrop() {
        ThingType thingType = ThingTypeBuilder.instance("bindingId", "thingTypeId", "label").build();
        Thing thing = ThingBuilder.create(thingType.getUID(), new ThingUID(thingType.getUID(), "thingId"))
                .withConfiguration(new Configuration()).build();

        ZWaveThingHandlerStatusCaptureTest handler = new ZWaveThingHandlerStatusCaptureTest(thing);
        ZWaveFirmwareUpdateSession firmwareSession = Mockito.mock(ZWaveFirmwareUpdateSession.class);
        Mockito.when(firmwareSession.isActive()).thenReturn(true);
        Mockito.when(firmwareSession.getCurrentTransferProgressPercent()).thenReturn(79);

        setNodeId(handler, 12);
        setFirmwareSession(handler, firmwareSession);

        handler.ZWaveIncomingEvent(
                new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.FirmwareUpdate, 12, ZWaveNetworkEvent.State.Progress, 75));
        assertEquals("Firmware update in progress (75%)", handler.getCapturedStatusInfo().getDescription());

        handler.ZWaveIncomingEvent(new ZWaveNodeStatusEvent(12, ZWaveNodeState.DEAD));
        assertEquals(ThingStatus.OFFLINE, handler.getCapturedStatusInfo().getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.getCapturedStatusInfo().getStatusDetail());

        handler.ZWaveIncomingEvent(new ZWaveNodeStatusEvent(12, ZWaveNodeState.ALIVE));
        assertEquals(ThingStatus.ONLINE, handler.getCapturedStatusInfo().getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_PENDING, handler.getCapturedStatusInfo().getStatusDetail());
        assertEquals("Firmware update in progress (79%)", handler.getCapturedStatusInfo().getDescription());
    }

    @Test
    public void testFirmwareUpdateProgressIgnoredWhenSessionInactive() {
        ThingType thingType = ThingTypeBuilder.instance("bindingId", "thingTypeId", "label").build();
        Thing thing = ThingBuilder.create(thingType.getUID(), new ThingUID(thingType.getUID(), "thingId"))
                .withConfiguration(new Configuration()).build();

        ZWaveThingHandlerStatusCaptureTest handler = new ZWaveThingHandlerStatusCaptureTest(thing);
        ZWaveFirmwareUpdateSession firmwareSession = Mockito.mock(ZWaveFirmwareUpdateSession.class);
        Mockito.when(firmwareSession.isActive()).thenReturn(false);

        setNodeId(handler, 12);
        setFirmwareSession(handler, firmwareSession);

        handler.ZWaveIncomingEvent(new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.FirmwareUpdate, 12,
                ZWaveNetworkEvent.State.Failure, "ERROR_TRANSMISSION_FAILED"));

        ThingStatusInfo failedStatus = handler.getCapturedStatusInfo();
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, failedStatus.getStatusDetail());
        assertEquals("Firmware update failed: ERROR_TRANSMISSION_FAILED", failedStatus.getDescription());

        handler.ZWaveIncomingEvent(
                new ZWaveNetworkEvent(ZWaveNetworkEvent.Type.FirmwareUpdate, 12, ZWaveNetworkEvent.State.Progress, 50));

        ThingStatusInfo statusAfterProgress = handler.getCapturedStatusInfo();
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, statusAfterProgress.getStatusDetail());
        assertEquals("Firmware update failed: ERROR_TRANSMISSION_FAILED", statusAfterProgress.getDescription());
    }

    @Test
    public void testVersionValueEventRefreshesFirmwareProperties() {
        ThingType thingType = ThingTypeBuilder.instance("bindingId", "thingTypeId", "label").build();
        Thing thing = ThingBuilder.create(thingType.getUID(), new ThingUID(thingType.getUID(), "thingId"))
                .withConfiguration(new Configuration()).build();

        ZWaveThingHandlerPropertiesCaptureTest handler = new ZWaveThingHandlerPropertiesCaptureTest(thing);
        ZWaveControllerHandler controllerHandler = Mockito.mock(ZWaveControllerHandler.class);
        ZWaveNode node = Mockito.mock(ZWaveNode.class);

        setNodeId(handler, 12);

        try {
            Field fieldControllerHandler = ZWaveThingHandler.class.getDeclaredField("controllerHandler");
            fieldControllerHandler.setAccessible(true);
            fieldControllerHandler.set(handler, controllerHandler);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail(e);
        }

        Mockito.when(controllerHandler.getNode(12)).thenReturn(node);
        Mockito.when(node.getManufacturer()).thenReturn(Integer.MAX_VALUE);
        Mockito.when(node.getDeviceType()).thenReturn(Integer.MAX_VALUE);
        Mockito.when(node.getDeviceId()).thenReturn(Integer.MAX_VALUE);
        Mockito.when(node.getApplicationVersion()).thenReturn("9.8");
        Mockito.when(node.getDeviceClass()).thenReturn(new ZWaveDeviceClass(Basic.BASIC_TYPE_UNKNOWN,
                Generic.GENERIC_TYPE_NOT_USED, Specific.SPECIFIC_TYPE_NOT_USED));
        Mockito.when(node.isListening()).thenReturn(false);
        Mockito.when(node.isFrequentlyListening()).thenReturn(false);
        Mockito.when(node.isBeaming()).thenReturn(false);
        Mockito.when(node.isRouting()).thenReturn(false);
        Mockito.when(node.isSecure()).thenReturn(false);
        Mockito.when(node.getAssociationGroups()).thenReturn(Collections.emptyMap());
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_ZWAVEPLUS_INFO)).thenReturn(null);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_CONFIGURATION)).thenReturn(null);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_WAKE_UP)).thenReturn(null);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_SWITCH_ALL)).thenReturn(null);
        Mockito.when(node.getCommandClass(CommandClass.COMMAND_CLASS_NODE_NAMING)).thenReturn(null);

        handler.ZWaveIncomingEvent(
                new ZWaveCommandClassValueEvent(12, 0, CommandClass.COMMAND_CLASS_VERSION, "9.8"));

        Map<String, String> properties = handler.getCapturedProperties();
        assertEquals("9.8", properties.get(ZWaveBindingConstants.PROPERTY_VERSION));
        assertEquals("9.8", properties.get(Thing.PROPERTY_FIRMWARE_VERSION));
    }
}
