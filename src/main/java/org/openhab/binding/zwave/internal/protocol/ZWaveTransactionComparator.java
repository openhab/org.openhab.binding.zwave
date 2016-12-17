package org.openhab.binding.zwave.internal.protocol;

import java.util.Comparator;

public class ZWaveTransactionComparator implements Comparator<ZWaveTransaction> {

    // private final ZWaveController controller;

    /**
     * Constructor. Creates a new instance of the SerialMessageComparator class.
     *
     * @param controller the {@link ZWaveController to use}
     */
    public ZWaveTransactionComparator() {
        // this.controller = controller;
    }

    /**
     * Compares a serial message to another serial message.
     * Used by the priority queue to order messages.
     *
     * @param arg0 the first serial message to compare the other to.
     * @param arg1 the other serial message to compare the first one to.
     */
    @Override
    public int compare(ZWaveTransaction arg0, ZWaveTransaction arg1) {
        return arg0.getPriority().compareTo(arg1.getPriority());
    }
}
