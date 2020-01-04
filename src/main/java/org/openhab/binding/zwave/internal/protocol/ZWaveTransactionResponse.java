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

/**
 *
 * @author Chris Jackson - Initial Contribution
 *
 */
public class ZWaveTransactionResponse {
    public enum State {
        CANCELLED,
        TIMEOUT_WAITING_FOR_CONTROLLER,
        TIMEOUT_WAITING_FOR_RESPONSE,
        TIMEOUT_WAITING_FOR_DATA,
        COMPLETE
    };

    private State state;

    ZWaveTransactionResponse(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }
}
