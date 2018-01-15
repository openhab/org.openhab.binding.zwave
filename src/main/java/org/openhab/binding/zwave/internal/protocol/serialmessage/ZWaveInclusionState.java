/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.serialmessage;

/**
 * Defines inclusion / exclusion states
 *
 * @author Chris Jackson
 *
 */
public enum ZWaveInclusionState {
    Unknown,
    IncludeSent,
    IncludeStart,
    IncludeNodeFound,
    IncludeSlaveFound,
    IncludeControllerFound,
    IncludeProtocolDone,
    IncludeFail,
    IncludeDone,
    ExcludeSent,
    ExcludeStart,
    ExcludeNodeFound,
    ExcludeSlaveFound,
    ExcludeControllerFound,
    ExcludeFail,
    ExcludeDone,
    SecureIncludeComplete,
    SecureIncludeFailed;
}
