package org.openhab.binding.zwave.internal.protocol.serialmessage;

/**
 * Defines inclusion / exclusion states
 *
 * @author Chris Jackson
 *
 */
public enum ZWaveInclusionState {
    Unknown,
    IncludeStart,
    IncludeNodeFound,
    IncludeSlaveFound,
    IncludeControllerFound,
    IncludeProtocolDone,
    IncludeFail,
    IncludeDone,
    ExcludeStart,
    ExcludeNodeFound,
    ExcludeSlaveFound,
    ExcludeControllerFound,
    ExcludeFail,
    ExcludeDone,
    SecureIncludeComplete,
    SecureIncludeFailed;
}
