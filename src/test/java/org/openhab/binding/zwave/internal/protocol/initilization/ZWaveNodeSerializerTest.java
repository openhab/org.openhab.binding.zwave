package org.openhab.binding.zwave.internal.protocol.initilization;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmSensorCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveClimateControlScheduleCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;
import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeSerializer;
import org.openhab.core.OpenHAB;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Tests for ZWaveNode serialization into XML
 *
 * @author Sami Salonen - Initial contribution
 */
public class ZWaveNodeSerializerTest {

    private @TempDir Path tempDir;
    private ZWaveNodeSerializer serializer;
    private String previousUserData;

    @BeforeEach
    public void startup() throws IOException {
        previousUserData = System.setProperty(OpenHAB.USERDATA_DIR_PROG_ARGUMENT, tempDir.toString());
        serializer = new ZWaveNodeSerializer();
    }

    @AfterEach
    public void cleanup() {
        if (previousUserData == null) {
            System.clearProperty(OpenHAB.USERDATA_DIR_PROG_ARGUMENT);
        } else {
            System.setProperty(OpenHAB.USERDATA_DIR_PROG_ARGUMENT, previousUserData);
        }
    }

    private Document domFromXML(File file) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(file);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new IllegalStateException("Failed to create Document from XML in: " + file, e);
        }
    }

    private static void assertEndpointSomewhatEqual(ZWaveEndpoint e1, ZWaveEndpoint e2) {
        assertEquals(e1.getEndpointId(), e2.getEndpointId());
        assertEquals(e1.getDeviceClass(), e2.getDeviceClass());
        // compare command classes
        assertArrayEquals(
                e1.getCommandClasses().stream().map(c -> c.getCommandClass()).collect(Collectors.toList()).toArray(),
                e2.getCommandClasses().stream().map(c -> c.getCommandClass()).collect(Collectors.toList()).toArray());
        assertArrayEquals(
                e1.getCommandClasses().stream().map(c -> c.getInstances()).collect(Collectors.toList()).toArray(),
                e2.getCommandClasses().stream().map(c -> c.getInstances()).collect(Collectors.toList()).toArray());
        assertArrayEquals(
                e1.getCommandClasses().stream().map(c -> c.getVersion()).collect(Collectors.toList()).toArray(),
                e2.getCommandClasses().stream().map(c -> c.getVersion()).collect(Collectors.toList()).toArray());
        // compare secure command classes
        assertArrayEquals(e1.getSecureCommandClasses().toArray(), e2.getSecureCommandClasses().toArray());
    }

    private String[] getText(Document doc, String expression) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        final NodeList nodes;
        try {
            nodes = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException("Failed to compile XPath expression: " + expression, e);
        }
        String[] result = new String[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            Node item = nodes.item(i);
            result[i] = item.getTextContent();
        }
        return result;
    }

    private ZWaveNode createInitializedNode(int homeId, int nodeId) {
        ZWaveNode node = new ZWaveNode(homeId, nodeId, null);
        node.setNodeStage(ZWaveNodeInitStage.DONE);
        return node;
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T[] parseEnums(Class<T> enumClass, String[] enumNames) {
        return Stream.of(enumNames).map(e -> Enum.valueOf(enumClass, e))
                .toArray(cnt -> (T[]) Array.newInstance(enumClass, cnt));
    }

    @Test
    public void serializeAndDeserialize() {
        assertNotNull(tempDir);
        File resultingXmlFile = new File(tempDir.toString(), "zwave/network_00000000__node_0.xml");
        ZWaveNode node = createInitializedNode(0, 0);
        ZWaveEndpoint endpoint = node.addEndpoint(0);
        endpoint.addCommandClass(new ZWaveAlarmCommandClass(node, null, null));
        endpoint.addCommandClass(new ZWaveAlarmSensorCommandClass(node, null, null));
        endpoint.addCommandClass(new ZWaveClimateControlScheduleCommandClass(node, null, null));
        assertFalse(resultingXmlFile.exists());
        serializer.serializeNode(node);
        assertTrue(resultingXmlFile.exists());

        ZWaveNode deserializedNode = serializer.deserializeNode(0, 0);

        // Non-exhaustive comparison of deserialized node and original node
        assertEquals(0, deserializedNode.getHomeId());
        assertEquals(0, deserializedNode.getNodeId());
        assertEquals(node.getEndpointCount(), deserializedNode.getEndpointCount());
        for (int i = 0; i < node.getEndpointCount(); i++) {
            assertEndpointSomewhatEqual(node.getEndpoint(i), deserializedNode.getEndpoint(i));
        }
    }

    @Test
    public void testOrderingOfCommands() {
        File resultingXmlFile = new File(tempDir.toString(), "zwave/network_00000000__node_0.xml");
        ZWaveNode node = createInitializedNode(0, 0);
        int[] endpointIds = new int[] { 3, 10, 1, 0 };
        for (int endpointId : endpointIds) {
            ZWaveEndpoint endpoint = node.addEndpoint(endpointId);
            endpoint.addCommandClass(new ZWaveAlarmCommandClass(node, null, null));
            endpoint.addCommandClass(new ZWaveAlarmSensorCommandClass(node, null, null));
            endpoint.addCommandClass(new ZWaveClimateControlScheduleCommandClass(node, null, null));

            endpoint.addSecureCommandClass(CommandClass.COMMAND_CLASS_ASSOCIATION);
            endpoint.addSecureCommandClass(CommandClass.COMMAND_CLASS_ASSOCIATION_GRP_INFO);
            endpoint.addSecureCommandClass(CommandClass.COMMAND_CLASS_METER_PULSE);
        }

        serializer.serializeNode(node);

        Document doc = domFromXML(resultingXmlFile);

        assertEndpointsOrdering(doc);
        for (int endpointId : endpointIds) {
            // Non-exhaustive checks of orderings
            assertSupportedCommandClassesOrdering(node, doc, endpointId);
            assertSecureCommandClassesOrdering(node, doc, endpointId);
        }
    }

    private void assertEndpointsOrdering(Document doc) {
        String[] endpointIdsStrings = getText(doc, "/node/endpoints/entry/endPoint/endpointId");
        int[] endpointIds = Stream.of(endpointIdsStrings).mapToInt(Integer::parseInt).toArray();
        int[] sortedIds = Arrays.copyOf(endpointIds, endpointIds.length);
        Arrays.sort(sortedIds);
        assertArrayEquals(endpointIds, sortedIds);
    }

    private void assertSupportedCommandClassesOrdering(ZWaveNode node, Document doc, int endpointId) {
        CommandClass[] vals = parseEnums(ZWaveCommandClass.CommandClass.class,
                getText(doc, "/node/endpoints/entry/endPoint[endpointId = '" + endpointId
                        + "']/supportedCommandClasses/entry/commandClass"));
        CommandClass[] expected = node.getEndpoint(endpointId).getCommandClasses().stream()
                .map(ZWaveCommandClass::getCommandClass).sorted().toArray(ZWaveCommandClass.CommandClass[]::new);
        assertArrayEquals(expected, vals);
    }

    private void assertSecureCommandClassesOrdering(ZWaveNode node, Document doc, int endpointId) {
        CommandClass[] vals = parseEnums(ZWaveCommandClass.CommandClass.class, getText(doc,
                "/node/endpoints/entry/endPoint[endpointId = '" + endpointId + "']/secureCommandClasses/commandClass"));
        CommandClass[] expected = node.getEndpoint(endpointId).getSecureCommandClasses().stream().sorted()
                .toArray(ZWaveCommandClass.CommandClass[]::new);
        assertArrayEquals(expected, vals);
    }

}
