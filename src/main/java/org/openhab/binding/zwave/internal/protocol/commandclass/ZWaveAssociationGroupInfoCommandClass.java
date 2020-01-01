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

import static org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass.*;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.zwave.internal.protocol.ZWaveAssociationGroup;
import org.openhab.binding.zwave.internal.protocol.ZWaveCommandClassPayload;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveTransaction.TransactionPriority;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayload;
import org.openhab.binding.zwave.internal.protocol.transaction.ZWaveCommandClassTransactionPayloadBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the Association Group Info Command command class.
 *
 * @author Jorg de Jong
 * @author Chris Jackson
 */
@XStreamAlias("COMMAND_CLASS_ASSOCIATION_GRP_INFO")
public class ZWaveAssociationGroupInfoCommandClass extends ZWaveCommandClass
        implements ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveAssociationGroupInfoCommandClass.class);

    private static final byte ASSOCIATION_GROUP_INFO_NAME_GET = 1;
    private static final byte ASSOCIATION_GROUP_INFO_NAME_REPORT = 2;
    private static final byte ASSOCIATION_GROUP_INFO_GET = 3;
    private static final byte ASSOCIATION_GROUP_INFO_REPORT = 4;
    private static final byte ASSOCIATION_GROUP_INFO_LIST_GET = 5;
    private static final byte ASSOCIATION_GROUP_INFO_LIST_REPORT = 6;

    private static final int MAX_STRING_LENGTH = 42;

    private static final int GET_LISTMODE_MASK = 0x40;
    // not used private static final int GET_REFRESHCACHE_MASK = 0x80;

    private static final int REPORT_GROUPCOUNT_MASK = 0x3f;
    private static final int REPORT_DYNAMICINFO_MASK = 0x40;
    private static final int REPORT_LISTMODE_MASK = 0x80;

    private static final byte PROFILE_GENERAL = 0x00;
    // General sub profile
    private static final byte PROFILE_LIFELINE = 0x01;

    // List of groups that the controller should subscribe to.
    private Set<Integer> autoSubscribeGroups = new HashSet<>();

    // List of command classes that are eligible for auto subscription.
    @XStreamOmitField
    private Set<CommandClass> autoCCs = Collections
            .unmodifiableSet(Stream.of(COMMAND_CLASS_DEVICE_RESET_LOCALLY, COMMAND_CLASS_BATTERY,
                    COMMAND_CLASS_CONFIGURATION, COMMAND_CLASS_METER, COMMAND_CLASS_THERMOSTAT_OPERATING_STATE,
                    COMMAND_CLASS_THERMOSTAT_MODE, COMMAND_CLASS_THERMOSTAT_FAN_MODE, COMMAND_CLASS_SENSOR_MULTILEVEL,
                    COMMAND_CLASS_SENSOR_ALARM, COMMAND_CLASS_THERMOSTAT_FAN_STATE, COMMAND_CLASS_THERMOSTAT_SETPOINT,
                    COMMAND_CLASS_SENSOR_BINARY, COMMAND_CLASS_ALARM, COMMAND_CLASS_SWITCH_COLOR,
                    COMMAND_CLASS_SCENE_ACTIVATION, COMMAND_CLASS_CENTRAL_SCENE, COMMAND_CLASS_DOOR_LOCK,
                    COMMAND_CLASS_METER_TBL_MONITOR, COMMAND_CLASS_METER_PULSE).collect(Collectors.toSet()));

    /**
     * Creates a new instance of the ZwaveAssociationGroupInfoCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWaveAssociationGroupInfoCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    @Override
    public CommandClass getCommandClass() {
        return CommandClass.COMMAND_CLASS_ASSOCIATION_GRP_INFO;
    }

    @ZWaveResponseHandler(id = ASSOCIATION_GROUP_INFO_NAME_REPORT, name = "ASSOCIATION_GROUP_INFO_NAME_REPORT")
    public void handleAssociationGroupInfoNameReport(ZWaveCommandClassPayload payload, int endpoint) {
        int groupIdx = payload.getPayloadByte(2);
        int numBytes = payload.getPayloadByte(3);

        if (numBytes < 0) {
            logger.error("NODE {}: Group Name report error in message length ({})", getNode().getNodeId(),
                    payload.getPayloadLength());
        }

        if (numBytes == 0) {
            ZWaveAssociationGroup group = getNode().getAssociationGroup(groupIdx);
            if (group == null) {
                group = new ZWaveAssociationGroup(groupIdx);
                getNode().setAssociationGroup(group);
            }
            getNode().getAssociationGroup(groupIdx).setName("");
            return;
        }

        if (numBytes > MAX_STRING_LENGTH) {
            logger.warn("NODE {}: Group Name is too big; maximum is {} characters {}", getNode().getNodeId(),
                    MAX_STRING_LENGTH, numBytes);
            numBytes = MAX_STRING_LENGTH;
        }

        // Check for non-printable characters - ignore anything after the first one!
        for (int c = 0; c < numBytes; c++) {
            if (payload.getPayloadByte(c + 4) == 0) {
                numBytes = c;
                logger.debug("NODE {}: Group name string truncated to {} characters", getNode().getNodeId(), numBytes);
                break;
            }
        }
        byte[] strBuffer = Arrays.copyOfRange(payload.getPayloadBuffer(), 4, 4 + numBytes);
        String groupName = null;
        try {
            groupName = new String(strBuffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.debug("NODE {}: exeption during group name extraction ", getNode().getNodeId(), e);
        }

        logger.debug("NODE {}: recieved group name: '{}' for group number: {}", getNode().getNodeId(), groupName,
                groupIdx);

        ZWaveAssociationGroup group = getNode().getAssociationGroup(groupIdx);
        if (group == null) {
            group = new ZWaveAssociationGroup(groupIdx);
            getNode().setAssociationGroup(group);
        }
        getNode().getAssociationGroup(groupIdx).setName(groupName);
    }

    @ZWaveResponseHandler(id = ASSOCIATION_GROUP_INFO_REPORT, name = "ASSOCIATION_GROUP_INFO_REPORT")
    public void handleAssociationGroupInfoReport(ZWaveCommandClassPayload payload, int endpoint) {
        boolean listMode = (payload.getPayloadByte(2) & REPORT_LISTMODE_MASK) != 0;
        boolean dynamicInfo = (payload.getPayloadByte(2) & REPORT_DYNAMICINFO_MASK) != 0;

        // Number of group info elements in this message. Not the total number of groups on the device.
        // The device can send multiple reports in list mode
        int groupCount = payload.getPayloadByte(2) & REPORT_GROUPCOUNT_MASK;
        logger.debug("NODE {}: AssociationGroupInfoCmd_Info_Report: count:{} listMode:{} dynamicInfo:{}",
                getNode().getNodeId(), groupCount, listMode, dynamicInfo);

        for (int i = 0; i < groupCount; i++) {
            int groupIdx = payload.getPayloadByte(3 + i * 7);
            int mode = payload.getPayloadByte(4 + i * 7);
            int profile_msb = payload.getPayloadByte(5 + i * 7);
            int profile_lsb = payload.getPayloadByte(6 + i * 7);

            logger.debug("NODE {}:    Group={}, Profile={} {}, mode:{}", getNode().getNodeId(), groupIdx,
                    String.format("%02X", profile_msb), String.format("%02X", profile_lsb), mode);

            ZWaveAssociationGroup group = getNode().getAssociationGroup(groupIdx);
            if (group == null) {
                group = new ZWaveAssociationGroup(groupIdx);
                getNode().setAssociationGroup(group);
            }
            group.setProfile1(profile_msb);
            group.setProfile2(profile_lsb);

            if ((profile_msb == PROFILE_GENERAL) && (profile_lsb == PROFILE_LIFELINE)) {
                autoSubscribeGroups.add(groupIdx);
            }
        }
    }

    @ZWaveResponseHandler(id = ASSOCIATION_GROUP_INFO_LIST_REPORT, name = "ASSOCIATION_GROUP_INFO_LIST_REPORT")
    public void handleAssociationGroupInfoListReport(ZWaveCommandClassPayload payload, int endpoint) {
        // List the CommandClasses and commands that will be send to the associated nodes in this group.
        // For now just log it, later this could be used auto associate to the group
        // ie always associate when we find a battery command class
        int groupId = payload.getPayloadByte(2);
        int size = payload.getPayloadByte(3);
        logger.debug("NODE {}: Supported Command classes and commands for group:{} ->", getNode().getNodeId(), groupId);
        Set<CommandClass> commands = new HashSet<>();
        for (int i = 0; i < size; i += 2) {
            // Check if this node actually supports this Command Class
            ZWaveCommandClass cc = getNode()
                    .getCommandClass(CommandClass.getCommandClass(payload.getPayloadByte(4 + i)));

            if (cc != null) {
                logger.debug("NODE {}:   {} command:{}", getNode().getNodeId(), cc.getCommandClass(),
                        payload.getPayloadByte(i + 5));
                commands.add(cc.getCommandClass());
                if (autoCCs.contains(cc.getCommandClass())) {
                    autoSubscribeGroups.add(groupId);
                }
            } else {
                logger.debug("NODE {}:   unsupported {} command:{}", getNode().getNodeId(),
                        payload.getPayloadByte(4 + i), payload.getPayloadByte(5 + i));
            }
        }
        if (getNode().getAssociationGroup(groupId) == null) {
            getNode().setAssociationGroup(new ZWaveAssociationGroup(groupId));
        }
        getNode().getAssociationGroup(groupId).setCommandClasses(commands);
    }

    /**
     * Gets a SerialMessage with the GROUP_NAME_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getGroupNameMessage(int groupidx) {
        logger.debug("NODE {}: Creating new message for application command GROUP_NAME_GET for group {}",
                getNode().getNodeId(), groupidx);

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                ASSOCIATION_GROUP_INFO_NAME_GET).withPayload(groupidx)
                        .withExpectedResponseCommand(ASSOCIATION_GROUP_INFO_NAME_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the INFO_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getInfoMessage(int groupidx) {
        logger.debug("NODE {}: Creating new message for application command INFO_GET for group {}",
                getNode().getNodeId(), groupidx);

        byte listMode = 0;
        if (groupidx == 0) {
            // Request all groups
            listMode = GET_LISTMODE_MASK;
        }

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                ASSOCIATION_GROUP_INFO_GET).withPayload(listMode, groupidx)
                        .withExpectedResponseCommand(ASSOCIATION_GROUP_INFO_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Gets a SerialMessage with the COMMAND_LIST_GET command
     *
     * @return the serial message
     */
    public ZWaveCommandClassTransactionPayload getCommandListMessage(int groupidx) {
        logger.debug("NODE {}: Creating new message for application command COMMAND_LIST_GET for group {}",
                getNode().getNodeId(), groupidx);

        byte allowCache = 0;

        return new ZWaveCommandClassTransactionPayloadBuilder(getNode().getNodeId(), getCommandClass(),
                ASSOCIATION_GROUP_INFO_LIST_GET).withPayload(allowCache, groupidx)
                        .withExpectedResponseCommand(ASSOCIATION_GROUP_INFO_LIST_REPORT)
                        .withPriority(TransactionPriority.Config).build();
    }

    /**
     * Get the groups the controller should be subscribed to.
     *
     * @return a set of group numbers
     */
    public Set<Integer> getAutoSubscribeGroups() {
        return autoSubscribeGroups;
    }

    @Override
    public Collection<ZWaveCommandClassTransactionPayload> initialize(boolean refresh) {
        ArrayList<ZWaveCommandClassTransactionPayload> result = new ArrayList<ZWaveCommandClassTransactionPayload>();

        // Only initialise the root endpoint
        if (getEndpoint() != null) {
            logger.debug("NODE {}: Association group info ignoring endpoint {}", getNode().getNodeId(),
                    getEndpoint().getEndpointId());
            return result;
        }

        // We need the number of groups as discovered by the AssociationCommandClass
        if (getNode().getAssociationGroups().size() == 0) {
            return result;
        }

        logger.debug("NODE {}: Initialising association group info - {} groups known", getNode().getNodeId(),
                getNode().getAssociationGroups().size());

        // For each group request its name and other info
        // Only request it if we have not received an answer yet
        for (ZWaveAssociationGroup group : getNode().getAssociationGroups().values()) {
            if (refresh == true || group.getName() == null) {
                result.add(getGroupNameMessage(group.getIndex()));
            }
            if (refresh == true || group.isProfileKnown() == false) {
                result.add(getInfoMessage(group.getIndex()));
            }
            if (refresh == true || group.getCommandClasses() == null) {
                result.add(getCommandListMessage(group.getIndex()));
            }
        }

        return result;
    }
}
