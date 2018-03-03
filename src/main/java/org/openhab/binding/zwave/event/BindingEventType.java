/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.event;

/**
 * Enumeration used in {@link BindingEventDTO} to provide context to a message
 *
 * @author Chris Jackson - initial contribution
 *
 */
public enum BindingEventType {
    SUCCESS,
    WARNING,
    INFO,
    ERROR
}
