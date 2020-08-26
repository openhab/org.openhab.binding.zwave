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
package org.openhab.binding.zwave.internal.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a message which is used in serial API interface to communicate with USB Z-Wave stick
 *
 * A ZWave serial message frame is made up as follows
 * Byte 0 : SOF (Start of Frame) 0x01
 * Byte 1 : Length of frame - number of bytes to follow
 * Byte 2 : Request (0x00) or Response (0x01)
 * Byte 3 : Message Class (see SerialMessageClass)
 * Byte 4+: Message Class data >> Message Payload
 * Byte x : Last byte is checksum
 *
 * @author Chris Jackson
 * @author Victor Belov
 * @author Brian Crosby
 */
public class SerialMessage {

    private static final Logger logger = LoggerFactory.getLogger(SerialMessage.class);
    private final static AtomicLong callbackSequence = new AtomicLong(1);

    private byte[] messagePayload;
    private int messageLength = 0;
    private SerialMessageType messageType;
    private int messageClassKey;

    protected int messageNode = 255;

    public static final int TRANSMIT_OPTIONS_NOT_SET = 0;
    private int transmitOptions = TRANSMIT_OPTIONS_NOT_SET;
    private int callbackId = 0;

    /**
     * Indicates whether the serial message is valid.
     */
    public boolean isValid = false;

    /**
     * Constructor. Creates a new instance of the SerialMessage class.
     */
    public SerialMessage() {
        logger.trace("Creating empty message");
        messagePayload = new byte[] {};
    }

    /**
     * Constructor. Creates a new instance of the SerialMessage class using the
     * specified message class and message type. An expected reply can be given
     * to indicate that a transaction is complete. The priority indicates the
     * priority to send the message with. Higher priority messages are taken from
     * the send queue earlier than lower priority messages.
     *
     * @param messageClass the message class to use
     * @param messageType the message type to use
     * @param expectedReply the expected Reply for this messaage
     * @param priority the message priority
     */
    public SerialMessage(SerialMessageClass messageClass, SerialMessageType messageType) {
        this(255, messageClass, messageType);
    }

    /**
     * Constructor. Creates a new instance of the SerialMessage class using the
     * specified message class and message type. An expected reply can be given
     * to indicate that a transaction is complete. The priority indicates the
     * priority to send the message with. Higher priority messages are taken from
     * the send queue earlier than lower priority messages.
     *
     * @param nodeId the node the message is destined for
     * @param messageClass the message class to use
     * @param messageType the message type to use
     * @param expectedReply the expected Reply for this messaage
     * @param priority the message priority
     */
    public SerialMessage(int nodeId, SerialMessageClass messageClass, SerialMessageType messageType) {
        logger.trace("NODE {}: Creating empty message of class = {} (0x{}), type = {}", nodeId, messageClass,
                Integer.toHexString(messageClass.key), messageType);
        this.messageClassKey = messageClass.getKey();
        this.messageType = messageType;
        this.messagePayload = new byte[] {};
        this.messageNode = nodeId;

        if (messageClass.requiresRequest() && callbackId == 0) {
            callbackId = (int) callbackSequence.getAndIncrement();
            if (callbackId == 254) {
                callbackSequence.set(1);
            }
        }
    }

    /**
     * Constructor. Creates a new instance of the SerialMessage class from a
     * specified buffer.
     *
     * @param buffer the buffer to create the SerialMessage from.
     */
    public SerialMessage(byte[] buffer) {
        this(255, buffer);
    }

    /**
     * Constructor. Creates a new instance of the SerialMessage class from a
     * specified buffer, and subsequently sets the node ID.
     *
     * @param nodeId the node the message is destined for
     * @param buffer the buffer to create the SerialMessage from.
     */
    public SerialMessage(int nodeId, byte[] buffer) {
        logger.trace("NODE {}: Creating new SerialMessage from buffer = {}", nodeId, SerialMessage.bb2hex(buffer));
        // Handle signalling frames (ie ACK, CAN, NAK)
        if (buffer.length == 1) {
            if (buffer[0] == 0x06) {
                messageType = SerialMessageType.ACK;
            }
            if (buffer[0] == 0x15) {
                messageType = SerialMessageType.NAK;
            }
            if (buffer[0] == 0x18) {
                messageType = SerialMessageType.CAN;
            }
            return;
        }
        messageLength = buffer.length - 2; // buffer[1]; TODO: Why is this commented out?!?
        byte messageCheckSumm = calculateChecksum(buffer);
        byte messageCheckSummReceived = buffer[messageLength + 1];
        if (messageCheckSumm == messageCheckSummReceived) {
            logger.trace("NODE {}: Checksum matched", nodeId);
            isValid = true;
        } else {
            logger.trace("NODE {}: Checksum error. Calculated = 0x%02X, Received = 0x%02X", nodeId, messageCheckSumm,
                    messageCheckSummReceived);
            isValid = false;
            return;
        }
        messageType = buffer[2] == 0x00 ? SerialMessageType.Request : SerialMessageType.Response;
        messageClassKey = buffer[3] & 0xFF;
        messagePayload = Arrays.copyOfRange(buffer, 4, messageLength + 1);
        messageNode = nodeId;

        // TODO: This check isn't 100% correct - at least not for ApplicationUpdate as it has no callback id
        // TODO: Check if messageClass expects a response?
        if (messageType == SerialMessageType.Request) {
            callbackId = buffer[4] & 0xFF;
            messageNode = buffer[5] & 0xFF;
        }
        logger.trace("NODE {}: Message payload = {}", getMessageNode(), SerialMessage.bb2hex(messagePayload));
    }

    /**
     * Converts a byte array to a hexadecimal string representation
     *
     * @param bb the byte array to convert
     * @return string the string representation
     */
    static public String bb2hex(byte[] bb) {
        if (bb == null) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < bb.length; i++) {
            result.append(String.format("%02X ", bb[i]));
        }
        return result.toString();
    }

    /**
     * Converts a byte to a hexadecimal string representation
     *
     * @param bb the byte to convert
     * @return string the string representation
     */
    public static String b2hex(byte b) {
        return String.format("%02X ", b);
    }

    /**
     * Calculates a checksum for the specified buffer.
     *
     * @param buffer the buffer to calculate.
     * @return the checksum value.
     */
    private static byte calculateChecksum(byte[] buffer) {
        byte checkSum = (byte) 0xFF;
        for (int i = 1; i < buffer.length - 1; i++) {
            checkSum = (byte) (checkSum ^ buffer[i]);
        }
        logger.trace("Calculated checksum = {}", checkSum);
        return checkSum;
    }

    /**
     * Returns a string representation of this SerialMessage object.
     * The string contains message class, message type and buffer contents.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(120);
        builder.append("Message: class=");
        builder.append(SerialMessageClass.getMessageClass(messageClassKey));
        builder.append('[');
        builder.append(messageClassKey);
        builder.append(']');
        builder.append(", type=");
        builder.append(messageType);
        builder.append('[');
        builder.append(messageType.ordinal());
        builder.append(']');
        builder.append(", dest=");
        builder.append(messageNode);
        builder.append(", callback=");
        builder.append(callbackId);
        builder.append(", payload=");
        builder.append(SerialMessage.bb2hex(messagePayload));
        return builder.toString();
    };

    /**
     * Gets the SerialMessage as a byte array.
     *
     * @return the message
     */
    public byte[] getMessageBuffer() {
        ByteArrayOutputStream resultByteBuffer = new ByteArrayOutputStream();
        resultByteBuffer.write((byte) 0x01);
        final SerialMessageClass messageClass = SerialMessageClass.getMessageClass(messageClassKey);

        // Calculate and set length
        int messageLength = 3;
        if (messagePayload != null) {
            messageLength += messagePayload.length;
        }
        messageLength += (messageClass.requiresRequest() == true && messageType == SerialMessageType.Request ? 1 : 0)
                + (messageClass == SerialMessageClass.SendData && messageType == SerialMessageType.Request ? 1 : 0);

        resultByteBuffer.write((byte) messageLength);
        resultByteBuffer.write((byte) messageType.ordinal());
        resultByteBuffer.write((byte) messageClassKey);

        try {
            if (messagePayload != null) {
                resultByteBuffer.write(messagePayload);
            }
        } catch (IOException e) {
            logger.error("Error getting message payload buffer: ", e);
        }

        // Callback ID and transmit options for a Send Data message.
        if (messageClass == SerialMessageClass.SendData && messageType == SerialMessageType.Request) {
            resultByteBuffer.write(transmitOptions);
        }

        if (messageType == SerialMessageType.Request && messageClass.requiresRequest()) {
            resultByteBuffer.write(callbackId);
        }

        // Make space in the array for the checksum
        resultByteBuffer.write((byte) 0x00);

        // Convert to a byte array
        byte[] result = resultByteBuffer.toByteArray();

        // Calculate the checksum
        result[result.length - 1] = 0x01;
        result[result.length - 1] = calculateChecksum(result);

        logger.debug("Assembled message buffer = {}", SerialMessage.bb2hex(result));
        return result;
    }

    /**
     * Check whether an object is equal to this serial message.
     * A serial message is considered equal when:
     * - the object passed in is a serial message.
     * - the message class is equal
     * - the message type is equal
     * - the expected reply is equal
     * - the payload is equal
     *
     * @param obj the object to compare this message with.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!obj.getClass().equals(getClass())) {
            return false;
        }

        SerialMessage other = (SerialMessage) obj;

        if (other.messageClassKey != messageClassKey) {
            return false;
        }

        if (other.messageType != messageType) {
            return false;
        }

        return Arrays.equals(other.messagePayload, this.messagePayload);
    }

    /**
     * Gets the message type (Request / Response).
     *
     * @return the message type
     */
    public SerialMessageType getMessageType() {
        return messageType;
    }

    /**
     * Gets the message class. This is the function it represents.
     *
     * @return
     */
    public SerialMessageClass getMessageClass() {
        return SerialMessageClass.getMessageClass(messageClassKey);
    }

    /**
     * Gets the message class key.
     *
     * This function could be used to get the message class key if the message has been created by an unknown key.
     */
    public int getMessageClassKey() {
        return messageClassKey;
    }

    /**
     * Returns the Node Id for / from this message.
     *
     * @return the messageNode
     */
    public int getMessageNode() {
        return messageNode;
    }

    /**
     * Gets the message payload.
     *
     * @return the message payload
     */
    public byte[] getMessagePayload() {
        return messagePayload;
    }

    /**
     * Gets a byte of the message payload at the specified index.
     * The byte is returned as an integer between 0x00 (0) and 0xFF (255).
     *
     * @param index the index of the byte to return.
     * @return an integer between 0x00 (0) and 0xFF (255).
     * @throws ZWaveSerialMessageException
     */
    public int getMessagePayloadByte(int index) throws ZWaveSerialMessageException {
        if (index >= messagePayload.length) {
            throw new ZWaveSerialMessageException(
                    "Attempt to read message payload out of bounds: " + this.toString() + " (" + index + ")");
        }
        return messagePayload[index] & 0xFF;
    }

    /**
     * Sets the message payload.
     *
     * @param messagePayload
     */
    public void setMessagePayload(byte[] messagePayload) {
        this.messagePayload = messagePayload;
    }

    /**
     * Gets the transmit options for this SendData Request.
     *
     * @return the transmitOptions
     */
    public int getTransmitOptions() {
        return transmitOptions;
    }

    /**
     * Sets the transmit options for this SendData Request.
     *
     * @param transmitOptions the transmitOptions to set
     */
    public void setTransmitOptions(int transmitOptions) {
        this.transmitOptions = transmitOptions;
    }

    /**
     * Identifies if transmit options have been set yet for this SendData Req
     *
     * @return true if they were set
     */
    public boolean areTransmitOptionsSet() {
        return transmitOptions != TRANSMIT_OPTIONS_NOT_SET;
    }

    /**
     * Gets the callback ID for this SendData Request.
     *
     * @return the callbackId
     */
    public int getCallbackId() {
        return callbackId;
    }

    /**
     * Sets the callback ID for this SendData Request
     *
     * @param callbackId the callbackId to set
     */
    public void setCallbackId(int callbackId) {
        this.callbackId = callbackId;
        // callbackSequence.set(callbackId);
    }

    /**
     * Serial message type enumeration. Indicates whether the message is a request or a response.
     *
     */
    public enum SerialMessageType {
        Request, // 0x00
        Response, // 0x01
        ACK,
        NAK,
        CAN
    }

    /**
     * Serial message priority enumeration. Indicates the message priority.
     * Queue priority concept -:
     * <ul>
     * <li>RealTime: Messages that must be sent at highest priority. Generally this is reserved for battery devices so
     * we send messages while they are awake. The high priority allows their messages to jump the queue.
     * <i>RealTime</i> messages will not be queued in the wakeup queue. This is meant to be used for time critical
     * events that have no meaning if they are delayed.
     * <li>Immediate: Messages that must be sent at highest priority. Generally this is reserved for battery devices so
     * we send messages while they are awake. The high priority allows their messages to jump the queue.
     * <li>High: Other high priority messages
     * <li>Set: Messages relating to SET commands. This should only be used for SET type commands that need to be
     * responsive - eg light switches, or things that are expected to occur quickly.
     * <li>Get: Messages relating to GET commands. Most messages relating to reading data use this priority.
     * <li>Config: Messages relating to system configuration. This can be GET or SET type commands, but these are things
     * that don't need to be responsive.
     * <li>Poll: Messages relating to polling. Normally these are GET commands, but the system overrides the priority to
     * the lowest so they don't cause any impact on the system.
     * </ul>
     *
     */
    public enum SerialMessagePriority {
        RealTime,
        Immediate,
        High,
        Set,
        Get,
        Config,
        Poll
    }

    /**
     * Serial message class enumeration. Enumerates the different messages that can be exchanged with the controller.
     *
     */
    public enum SerialMessageClass {
        SerialApiGetInitData(0x02, true, false, false), // Request initial information about
                                                        // devices in network
        SerialApiApplicationNodeInfo(0x03), // Set controller node information (ie NIF)
        ApplicationCommandHandler(0x04), // Handle application command
        GetControllerCapabilities(0x05, true, false, false), // Request controller capabilities
        // (primary role,
        // SUC/SIS availability)
        SerialApiSetTimeouts(0x06, true, false, false), // Set Serial API timeouts
        SerialApiGetCapabilities(0x07, true, false, false), // Request Serial API capabilities
        SerialApiSoftReset(0x08, false, false, false), // Soft reset. Restarts Z-Wave chip
        RfReceiveMode(0x10), // Power down the RF section of the stick
        SetSleepMode(0x11), // Set the CPU into sleep mode
        SendNodeInfo(0x12), // Send Node Information Frame of the stick
        SendData(0x13, true, true, true), // Send data.
        SendDataMulti(0x14, true, true, true),
        GetVersion(0x15, true, false, false), // Request controller hardware version
        SendDataAbort(0x16, false, false, false), // Abort Send data.
        RfPowerLevelSet(0x17), // Set RF Power level
        SendDataMeta(0x18),
        GetRandom(0x1C), // Returns a random number
        MemoryGetId(0x20, true, false, false), // ???
        MemoryGetByte(0x21), // Get a byte of memory.
        MemoryPutByte(0x22),
        ReadMemory(0x23), // Read memory.
        WriteMemory(0x24),
        GetNetworkStats(0x3A, true, false, false),
        GetBackgroundRssi(0x3B, true, false, false),
        SetLearnNodeState(0x40), // ???
        IdentifyNode(0x41, true, false, false), // Get protocol info (baud rate, listening, etc.) for a given node
        SetDefault(0x42, false, true, false), // Reset controller and node info to default (original) values
        NewController(0x43), // ???
        ReplicationCommandComplete(0x44), // Replication send data complete
        ReplicationSendData(0x45), // Replication send data
        AssignReturnRoute(0x46, true, true, true), // Assign a return route from the specified node to the controller
        DeleteReturnRoute(0x47, true, true, true), // Delete all return routes from the specified node
        RequestNodeNeighborUpdate(0x48, false, true, true), // Ask the specified node to
                                                            // update its neighbors (then
                                                            // read them from the
                                                            // controller)
        ApplicationUpdate(0x49), // Get a list of supported (and controller) command classes
        AddNodeToNetwork(0x4a, false, true, false), // Control the addnode (or add controller)
                                                    // process...start, stop, etc.
        RemoveNodeFromNetwork(0x4b, false, true, false), // Control the remove node (or
                                                         // remove controller) process...start,
                                                         // stop, etc.
        CreateNewPrimary(0x4c), // Control the create new primary process...start, stop, etc.
        ControllerChange(0x4d), // Control the transfer primary process...start, stop, etc.
        SetLearnMode(0x50, false, true, false), // Put a controller into learn mode for replication / receipt of
        // configuration info
        AssignSucReturnRoute(0x51, true, true, true), // Assign a return route to the SUC
        EnableSuc(0x52, true, false, false), // Make a controller a Static Update Controller
        RequestNetworkUpdate(0x53, true, true, false), // Network update for a SUC(?)
        SetSucNodeID(0x54, true, true, false), // Identify a Static Update Controller node id
        DeleteSUCReturnRoute(0x55, true, true, true), // Remove return routes to the SUC
        GetSucNodeId(0x56, true, false, false), // Try to retrieve a Static Update Controller node id
                                                // (zero if no SUC present)
        SendSucId(0x57),
        RequestNodeNeighborUpdateOptions(0x5a), // Allow options for request node neighbor update
        ExploreRequestInclusion(0x5e), // Initiate a Network-Wide Inclusion process
        RequestNodeInfo(0x60, true, false, true), // Get info (supported command classes) for the specified node
        RemoveFailedNodeID(0x61, true, true, false), // Mark a specified node id as failed
        IsFailedNodeID(0x62, true, false, false), // Check to see if a specified node has failed
        ReplaceFailedNode(0x63, true, true, false), // Remove a failed node from the controller's list (?)
        GetRoutingInfo(0x80, true, false, false), // Get a specified node's neighbor information from
                                                  // the controller
        LockRoute(0x90),
        GetPriorityRoute(0x92),
        SetPriorityRoute(0x93),
        GetSecurityKeys(0x9C),
        SerialApiSlaveNodeInfo(0xA0), // Set application virtual slave node information
        ApplicationSlaveCommandHandler(0xA1), // Slave command handler
        SendSlaveNodeInfo(0xA2), // Send a slave node information frame
        SendSlaveData(0xA3), // Send data from slave
        SetSlaveLearnMode(0xA4), // Enter slave learn mode
        GetVirtualNodes(0xA5), // Return all virtual nodes
        IsVirtualNode(0xA6), // Virtual node test
        SetWutTimeout(0xB4),
        WatchDogEnable(0xB6),
        WatchDogDisable(0xB7),
        WatchDogKick(0xB8),
        SetExtIntLevel(0xB9),
        RfPowerLevelGet(0xBA), // Get RF Power level
        GetLibraryType(0xBD), // Gets the type of ZWave library on the stick
        SendTestFrame(0xBE), // Send a test frame to a node
        GetProtocolStatus(0xBF),
        SetPromiscuousMode(0xD0), // Set controller into promiscuous mode to listen to all frames
        SetRoutingMax(0xD4),
        PromiscuousApplicationCommandHandler(0xD1);

        /**
         * A mapping between the integer code and its corresponding ZWaveMessage
         * value to facilitate lookup by code.
         */
        private static Map<Integer, SerialMessageClass> codeToMessageClassMapping;

        private int key;
        private boolean response;
        private boolean request;
        private boolean node;

        private SerialMessageClass(int key) {
            this.key = key;
            this.response = false;
            this.request = false;
            this.node = false;
        }

        private SerialMessageClass(int key, boolean response, boolean request, boolean node) {
            this.key = key;
            this.response = response;
            this.request = request;
            this.node = node;
        }

        private static void initMapping() {
            codeToMessageClassMapping = new HashMap<Integer, SerialMessageClass>();
            for (SerialMessageClass s : values()) {
                codeToMessageClassMapping.put(s.key, s);
            }
        }

        /**
         * Lookup function based on the generic device class code.
         *
         * @param i the code to lookup
         * @return enumeration value of the generic device class.
         */
        public static SerialMessageClass getMessageClass(int i) {
            if (codeToMessageClassMapping == null) {
                initMapping();
            }
            return codeToMessageClassMapping.get(i);
        }

        /**
         * Returns the enumeration key.
         *
         * @return the key
         */
        public int getKey() {
            return key;
        }

        /**
         * Returns true if the transaction requires a Response frame
         *
         * @return true if the transaction requires a response frame
         */
        public boolean requiresResponse() {
            return response;
        }

        /**
         * Returns true if the transaction requires at least one Request frame
         *
         * @return true if the transaction requires a request frame
         */
        public boolean requiresRequest() {
            return request;
        }

        /**
         * Returns true if the node is contacted during the transaction.
         * This is used to ensure the node is awake before sending the transaction.
         * Transactions that only communicate with the controller therefore don't interact with the node.
         *
         * @return true if the node is required for the transaction
         */
        public boolean requiresNode() {
            return node;
        }
    }
}
