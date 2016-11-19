package org.openhab.binding.zwave.internal.protocol.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ZWaveNonce {
    private final long RESEED_INTERVAL = TimeUnit.HOURS.toMillis(6);
    private final long timeout = 10000000000L;
    private byte[] nonceBytes;
    private long timer;

    private static SecureRandom secureRandom = null;
    private static long reseedAt = 0;

    public ZWaveNonce() {
        if (System.currentTimeMillis() > reseedAt) {
            try {
                secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
            } catch (GeneralSecurityException e) {
                secureRandom = new SecureRandom();
            }

            secureRandom.nextBoolean();
            secureRandom.setSeed(System.nanoTime());

            reseedAt = System.currentTimeMillis() + RESEED_INTERVAL;
        }
        nonceBytes = new byte[8];
        secureRandom.nextBytes(nonceBytes);
    }

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

    public byte getId() {
        return nonceBytes[0];
    }

    @Override
    public String toString() {
        return "ZWaveNonce [nonceBytes=" + Arrays.toString(nonceBytes) + ", timer=" + (System.nanoTime() - timer)
                + ", valid=" + isValid() + "]";
    }

    public void setNonceBytes(byte[] nonceBytes) {
        this.nonceBytes = nonceBytes;
    }
}
