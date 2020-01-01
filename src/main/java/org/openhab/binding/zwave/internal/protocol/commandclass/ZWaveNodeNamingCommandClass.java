/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Node Naming command class.
 * The Node Naming command class is used to assign names and locations to devices.
 * Setting name and location of devices is mainly an installer task, so this command class
 * is only used to configure the Z-Wave network and help troubleshoot it later by giving the installer
 * the means to specify where in the home a device is located and what friendly name it has instead
 * of working with just node numbers.
 *
 * An openHab administration tool like HABmin can set and read the nodes' names and locations.
 *
 * @author Chris Jackson
 * @author Pedro Paixao
 */

@XStreamAlias("COMMAND_CLASS_NODE_NAMING")
public class ZWaveNodeNamingCommandClass extends ZWaveCommandClass implements ZWaveCommandClassDynamicState {

    public enum Type {
        NODENAME_NAME,
        NODENAME_LOCATION
    }

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveNodeNamingCommandClass.class);

    private static final int NAME_SET = 0x01;
    private static final int NAME_GET = 0x02;
    private static final int NAME_REPORT = 0x03;

    private static final int LOCATION_SET = 0x04;
    private static final int LOCATION_GET = 0x05;
    private static final int LOCATION_REPORT = 0x06;

    private static final int ENCODING_ASCII = 0x00;
    private static final int ENCODING_EXTENDED_ASCII = 0x01;
    private static final int ENCODING_UTF16 = 0x02;

    private static final int MAX_STRING_LENGTH = 16;

    @XStreamOmitField
    private boolean initialiseName = false;
    @XStreamOmitField
    private boolean initialiseLocation = false;

    String name = new String();
    String location = new String();

    /**
     * Creates a new instance of the ZWaveNameLocationCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveNodeNamingCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    public String getName() {
        if (initialiseName) {
            return name;
        }
        return null;
    }

    public String getLocation() {
        if (initialiseLocation) {
            return location;
        }
        return null;
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_NODE_NAMING;
    }

    /**
     * Get a string from the serial message
     *
     * @param serialMessage
     * @param offset
     * @return String
     * @throws ZWaveSerialMessageException
     */
    protected String getString(ZWaveCommandClassPayload payload) {
        if (payload.getPayloadLength() <= 2) {
            return "";
        }
        int charPresentation = payload.getPayloadByte(2);

        // First 5 bits are reserved so 0 them
        charPresentation = 0x07 & charPresentation;

        switch (charPresentation) {
            case ENCODING_ASCII:
                logger.debug("NODE {} : Node Name is encoded with standard ASCII codes", getNode().getNodeId());
                break;
            case ENCODING_EXTENDED_ASCII:
                logger.debug("NODE {} : Node Name is encoded with standard and OEM Extended ASCII codes",
                        getNode().getNodeId());
                break;
            case ENCODING_UTF16:
                logger.debug("NODE {} : Node Name is encoded with Unicode UTF-16", getNode().getNodeId());
                break;
            default:
                logger.debug("NODE {} : Node Name encoding is unsupported. Encoding code {}", getNode().getNodeId(),
                        charPresentation);
                return null;
        }

        int numBytes = payload.getPayloadLength() - 3;

        if (numBytes < 0) {
            logger.error("NODE {} : Node Name report error in message length ({})", getNode().getNodeId(),
                    payload.getPayloadLength());
            return null;
        }

        if (numBytes == 0) {
            return new String();
        }

        // Maximum length is 16 bytes
        if (numBytes > MAX_STRING_LENGTH) {
            logger.warn("NODE {} : Node Name is too big; maximum is {} characters {}", getNode().getNodeId(),
                    MAX_STRING_LENGTH, numBytes);
            numBytes = MAX_STRING_LENGTH;
        }

        if (charPresentation != ENCODING_ASCII) {
            logger.debug("NODE {}: Switching to using ASCII encoding", getNode().getNodeId());
            charPresentation = ENCODING_ASCII;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Check for null terminations - ignore anything after the first null
        for (int c = 0; c < numBytes; c++) {
            if (payload.getPayloadByte(c + 3) > 32 && payload.getPayloadByte(c + 3) < 127) {
                baos.write((byte) (payload.getPayloadByte(c + 3)));
            }
        }
        try {
            return new String(baos.toByteArray(), "ASCII");
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        /*
         * byte[] strBuffer = Arrays.copyOfRange(serialMessage.getMessagePayload(), offset + 2, offset + 2 + numBytes);
         *
         * String response = null;
         * try {
         * switch (charPresentation) {
         * case ENCODING_ASCII:
         * // Using standard ASCII codes. (values 128-255 are ignored)
         * break;
         * case ENCODING_EXTENDED_ASCII:
         * // Using standard and OEM Extended ASCII
         * response = new String(strBuffer, "ASCII");
         * break;
         *
         * case ENCODING_UTF16:
         * // Unicode UTF-16
         * String sTemp = new String(strBuffer, "UTF-16");
         * response = new String(sTemp.getBytes("UTF-8"), "UTF-8");
         * break;
         * }
         * } catch (UnsupportedEncodingException uee) {
         * System.out.println("Exception: " + uee);
         * }
         * if (response == null) {
         * return null;
         * }
         *
         * return response.replaceAll("\\p{C}", "?");
         */
    }

    @ZWaveResponseHandler(id = NAME_REPORT, name = "NAME_REPORT")
    public void handleNameReport(ZWaveCommandClassPayload payload, int endpoint) {
        String name = getString(payload);
        if (name == null) {
            return;
        }

        this.name = name;
        logger.debug("NODE {}: Node name: {}", getNode().getNodeId(), name);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), name, Type.NODENAME_NAME);
        getController().notifyEventListeners(zEvent);
    }

    @ZWaveResponseHandler(id = LOCATION_REPORT, name = "LOCATION_REPORT")
    public void handleLocationReport(ZWaveCommandClassPayload payload, int endpoint) {
        String location = getString(payload);
        if (name == null) {
            return;
        }

        this.location = location;
        logger.debug("NODE {}: Node location: {}", getNode().getNodeId(), location);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                getCommandClass(), location, Type.NODENAME_LOCATION);
        getController().notifyEventListeners(zEvent);
    }

    /**
     * Gets a SerialMessage with the NAME GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getNameMessage() {
        logger.debug("NODE {}: Creating new message for application command NAME_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), NAME_GET)
                .withExpectedResponseCommand(NAME_REPORT).withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the NAME GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getLocationMessage() {
        logger.debug("NODE {}: Creating new message for application command LOCATION_GET", this.getNode().getNodeId());

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), LOCATION_GET)
                .withExpectedResponseCommand(LOCATION_REPORT).withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the Name or Location SET command
     *
     * @param the level to set.
     * @return the serial message
     */
    private ZWaveCommandClassTransactionPayload setValueMessage(String str, int command) {
        logger.debug("NODE {}: Creating new message for application command NAME_SET to {}", this.getNode().getNodeId(),
                str);

        byte[] nameBuffer = null;
        byte encoding = ENCODING_ASCII;

        // First we want to see if this can be encoded in ASCII
        CharsetEncoder asciiEncoder = StandardCharsets.US_ASCII.newEncoder();
        if (asciiEncoder.canEncode(str) == true) {
            nameBuffer = str.getBytes(StandardCharsets.US_ASCII);
        } else {
            nameBuffer = str.getBytes(StandardCharsets.UTF_16);
            encoding = ENCODING_UTF16;
        }

        int len = nameBuffer.length;
        if (len > 16) {
            len = 16;
        }

        byte[] payload = new byte[len + 1];
        payload[0] = encoding;
        System.arraycopy(nameBuffer, 0, payload, 1, len);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(), command)
                .withPayload(payload).withPriority(TransactionPriority.Config).build();
    }

    public ZWaveCommandClassTransactionPayload setNameMessage(String name) {
        return setValueMessage(name, NAME_SET);
    }

    public ZWaveCommandClassTransactionPayload setLocationMessage(String location) {
        return setValueMessage(location, LOCATION_SET);
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> getDynamicValues(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        if (refresh == true) {
            initialiseName = false;
            initialiseLocation = false;
        }

        if (initialiseName == false) {
            result.add(getNameMessage());
        }
        if (initialiseLocation == false) {
            result.add(getLocationMessage());
        }

        return result;
    }
}
