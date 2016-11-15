package org.openhab.binding.zwave.internal.protocol.security;

public class ZWaveNonce {
    private final int timeout = 10000000;
    private byte[] nonceBytes;
    private long timer;

    public ZWaveNonce(byte[] nonceBytes) {
        // Start the timer
        this.timer = System.nanoTime();
        // Save the nonce
        this.nonceBytes = nonceBytes;
    }

    public byte[] getNonceBytes() {
        return nonceBytes;
    }

    public boolean isValid() {
        return (System.nanoTime() - timer) < timeout;
    }
}
