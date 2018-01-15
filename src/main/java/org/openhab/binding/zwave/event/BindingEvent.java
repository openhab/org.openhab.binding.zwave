/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.event;

import org.eclipse.smarthome.core.events.AbstractEvent;

/**
 *
 * @author Chris Jackson
 *
 */
public class BindingEvent extends AbstractEvent {
    public final static String TYPE = BindingEvent.class.getSimpleName();

    public BindingEvent(String topic, String binding, String payload) {
        super(topic, payload, binding);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}