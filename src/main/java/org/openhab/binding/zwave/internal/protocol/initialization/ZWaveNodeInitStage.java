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
package org.openhab.binding.zwave.internal.protocol.initialization;

/**
 * Node Stage Enumeration for node initialisation sequence.
 * The initialisation will be performed in the order of the enum definitions.
 *
 * @author Chris Jackson
 */
public enum ZWaveNodeInitStage {
    EMPTYNODE(true),
    PROTOINFO(true),
    IDENTIFY_NODE(true),
    INIT_NEIGHBORS(true),
    FAILED_CHECK(true),
    WAIT(true),
    PING(true),
    REQUEST_NIF(true),

    // States below form the main part of the initialisation
    // For newly included devices, we start here
    INCLUSION_START(true),
    MANUFACTURER(true),
    SECURITY_REPORT(true),
    APP_VERSION(true),
    DISCOVERY_COMPLETE(true),
    VERSION(true),
    ENDPOINTS(true),
    UPDATE_DATABASE(true),
    STATIC_VALUES(true),
    ASSOCIATIONS(false),
    SET_WAKEUP(false),
    SET_ASSOCIATION(false),
    SET_LIFELINE(false),
    DELETE_SUC_ROUTES(false),
    SUC_ROUTE(false),
    STATIC_END(false),

    // States below are not restored from the configuration files
    SESSION_START(false),
    GET_CONFIGURATION(false),
    DYNAMIC_VALUES(false),
    DYNAMIC_END(false),

    DONE(false),

    // States below are performed during initialisation, but also during heal
    HEAL_START(false),
    UPDATE_NEIGHBORS(false),
    GET_NEIGHBORS(false),
    DELETE_ROUTES(false),
    RETURN_ROUTES(false),
    HEAL_END(false);

    private boolean mandatory;

    private ZWaveNodeInitStage(boolean manditory) {
        this.mandatory = manditory;
    }

    /**
     * Return the next stage after the current stage
     *
     * @return the next stage
     */
    public ZWaveNodeInitStage getNextStage() {
        for (ZWaveNodeInitStage stage : values()) {
            if (stage.ordinal() == this.ordinal() + 1) {
                return stage;
            }
        }

        return null;
    }

    /**
     * Check if the current stage has completed the static stages.
     *
     * @return true if static stages are complete
     */
    public boolean isStaticComplete() {
        return (ordinal() > SESSION_START.ordinal());
    }

    /**
     *
     * @return true if dynamic stages are complete
     */
    public boolean isDynamicComplete() {
        return (ordinal() > DYNAMIC_END.ordinal());
    }

    /**
     * Check if the current stage has completed the static stages.
     *
     * @return true if static stages complete
     */
    public boolean isStageMandatory() {
        return mandatory;
    }
}
