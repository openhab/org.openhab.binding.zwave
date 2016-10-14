package org.openhab.binding.zwave.internal.protocol.transaction;

/**
 * Transaction priority enumeration. Indicates the message priority.
 * Queue priority concept -:
 * <ul>
 * <li>RealTime: Messages that must be sent at highest priority. Generally this is reserved for battery devices so
 * we send messages while they are awake. The high priority allows their messages to jump the queue.
 * <i>RealTime</i> messages will not be queued in the wakeup queue. This is meant to be used for time critical
 * events that have no meaning if they are delayed.
 * <li>Immediate: Messages that must be sent at highest priority. Generally this is reserved for battery devices so
 * we send messages while they are awake. The high priority allows their messages to jump the queue.
 * <li>High: Other high priority messages
 * <li>Set: Messages relating to SET commands. This should only be used for SET type commands that need to be
 * responsive - eg light switches, or things that are expected to occur quickly.
 * <li>Get: Messages relating to GET commands. Most messages relating to reading data use this priority.
 * <li>Config: Messages relating to system configuration. This can be GET or SET type commands, but these are things
 * that don't need to be responsive.
 * <li>Poll: Messages relating to polling. Normally these are GET commands, but the system overrides the priority to
 * the lowest so they don't cause any impact on the system.
 * </ul>
 *
 */
public enum TransactionPriority {
    RealTime,
    Immediate,
    High,
    Set,
    Get,
    Config,
    Poll
}
