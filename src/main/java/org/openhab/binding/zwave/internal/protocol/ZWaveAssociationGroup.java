/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import java.util.ArrayList;
import java.util.List;
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
    private String name;

    @XStreamConverter(HexToIntegerConverter.class)
    private Integer profile1;
    @XStreamConverter(HexToIntegerConverter.class)
    private Integer profile2;
    private Set<CommandClass> commands;

    List<ZWaveAssociation> associations = new ArrayList<ZWaveAssociation>();

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
     * Adds an association node
     *
     * @param node
     */
    public void addAssociation(int node) {
        addAssociation(node, null);
    }

    /**
     * Adds an association node and endpoint
     *
     * @param node
     * @param endpoint
     */
    public void addAssociation(int node, Integer endpoint) {
        // Check if we're already associated
        if (isAssociated(node, endpoint)) {
            return;
        }

        // No - add a new association
        ZWaveAssociation newAssociation = new ZWaveAssociation(node, endpoint);
        associations.add(newAssociation);
    }

    /**
     * Removes an association node
     *
     * @param node
     * @return
     */
    public boolean removeAssociation(int node) {
        return removeAssociation(node, null);
    }

    /**
     * Removes an association node and endpoint
     *
     * @param node
     * @param endpoint
     * @return
     */
    public boolean removeAssociation(int node, Integer endpoint) {
        int associationCnt = associations.size();
        for (int index = 0; index < associationCnt; index++) {
            ZWaveAssociation association = associations.get(index);
            if (association.getNode() == node && association.getEndpoint() == endpoint) {
                associations.remove(index);
                return true;
            }
        }

        return false;
    }

    /**
     * Tests if a node is associated to this group
     *
     * @param node
     * @return
     */
    public boolean isAssociated(ZWaveAssociation association) {
        return isAssociated(association.getNode(), association.getEndpoint());
    }

    /**
     * Tests if a node is associated to this group
     *
     * @param node
     * @return
     */
    public boolean isAssociated(int node) {
        return isAssociated(node, null);
    }

    /**
     * Tests if a node and endpoint are associated to this group
     *
     * @param node
     * @param endpoint
     * @return
     */
    public boolean isAssociated(int node, Integer endpoint) {
        int associationCnt = associations.size();
        for (int index = 0; index < associationCnt; index++) {
            ZWaveAssociation association = associations.get(index);
            if (association.getNode() == node && association.getEndpoint() == endpoint) {
                return true;
            }
        }

        return false;
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
        this.associations = associations;
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
}
