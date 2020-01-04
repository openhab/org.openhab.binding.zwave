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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.openhab.binding.zwave.internal.HexToIntegerConverter;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass.CommandClass;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * This class provides a storage class for zwave association groups
 * within the node class. This is then serialised to XML.
 * <p>
 * The class consolidates information from the different association classes -
 * ASSOCIATION, MULTI_INSTANCE_ASSOCIATION, and ASSOCIATION_GROUP_INFO.
 * <p>
 * This is necessary since ASSOCIATION, MULTI_INSTANCE_ASSOCIATION provide the same
 * information and overlap in their responses
 *
 * @author Chris Jackson
 */
@XStreamAlias("associationGroup")
public class ZWaveAssociationGroup {
    private int index;
    private int maxNodes;
    private String name;

    @XStreamConverter(HexToIntegerConverter.class)
    private Integer profile1;
    @XStreamConverter(HexToIntegerConverter.class)
    private Integer profile2;
    private Set<CommandClass> commands;

    private final List<ZWaveAssociation> associations = new ArrayList<ZWaveAssociation>();

    public ZWaveAssociationGroup(int index) {
        this.index = index;
    }

    /**
     * Return the group index
     *
     * @return group index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the group index
     *
     * @param newIndex the group index
     */
    public void setIndex(int newIndex) {
        index = newIndex;
    }

    /**
     * Adds an association node and endpoint
     */
    public boolean addAssociation(ZWaveAssociation association) {
        if (associations.contains(association)) {
            return false;
        }
        return associations.add(association);
    }

    /**
     * Removes an association node and endpoint
     */
    public boolean removeAssociation(int node, Integer endpoint) {
        int associationCnt = associations.size();
        for (int index = 0; index < associationCnt; index++) {
            ZWaveAssociation association = associations.get(index);
            if (Objects.equals(association.getNode(), node) && Objects.equals(association.getEndpoint(), endpoint)) {
                associations.remove(index);
                return true;
            }
        }

        return false;
    }

    /**
     * Removes an association node and endpoint
     */
    public void removeAssociation(ZWaveAssociation association) {
        removeAssociation(association.getNode(), association.getEndpoint());
    }

    /**
     * Tests if a node is associated to this group
     *
     * @param node
     * @return
     */
    public boolean isAssociated(ZWaveAssociation association) {
        return associations.contains(association);
    }

    /**
     * Returns the list of association group members
     *
     * @return
     */
    public List<ZWaveAssociation> getAssociations() {
        return associations;
    }

    /**
     * Sets the list of association group members
     *
     * @param associations
     */
    public void setAssociations(List<ZWaveAssociation> associations) {
        this.associations.clear();
        this.associations.addAll(associations);
    }

    /**
     * Returns the number of members in the group
     *
     * @return
     */
    public int getAssociationCnt() {
        return associations.size();
    }

    public Set<CommandClass> getCommandClasses() {
        return commands;
    }

    public void setCommandClasses(Set<CommandClass> commands) {
        this.commands = commands;
    }

    public boolean isProfileKnown() {
        return profile1 != null;
    }

    public Integer getProfile1() {
        if (profile1 == null) {
            // Default profile to General:NA
            return 0;
        }
        return profile1;
    }

    public Integer getProfile2() {
        if (profile2 == null) {
            // Default profile to General:NA
            return 0;
        }
        return profile2;
    }

    public void setProfile1(Integer profile1) {
        this.profile1 = profile1;
    }

    public void setProfile2(Integer profile2) {
        this.profile2 = profile2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    @Override
    public String toString() {
        return "ZWaveAssociationGroup [index=" + index + ", name=" + name + ", profile1=" + profile1 + ", profile2="
                + profile2 + ", associations=" + associations + "]";
    }
}
