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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 * @author Chris Jackson - Initial contribution
 *
 */
@XStreamAlias("associationMember")
public class ZWaveAssociation {
    private final int node;
    private final Integer endpoint;

    /**
     * Creates an association linked to a node
     *
     * @param node
     */
    public ZWaveAssociation(int node) {
        this.node = node;
        this.endpoint = null;
    }

    /**
     * Creates an association linked to an endpoint
     *
     * @param node
     * @param endpoint
     */
    public ZWaveAssociation(int node, Integer endpoint) {
        this.node = node;
        this.endpoint = endpoint;
    }

    public boolean isController() {
        return node == 0;
    }

    public int getNode() {
        return node;
    }

    public Integer getEndpoint() {
        return endpoint;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("node_");
        builder.append(node);
        if (endpoint != null) {
            builder.append('_');
            builder.append(endpoint);
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
        result = prime * result + node;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ZWaveAssociation other = (ZWaveAssociation) obj;
        if (endpoint == null) {
            if (other.endpoint != null) {
                return false;
            }
        } else if (!endpoint.equals(other.endpoint)) {
            return false;
        }
        if (node != other.node) {
            return false;
        }
        return true;
    }
}
