/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
