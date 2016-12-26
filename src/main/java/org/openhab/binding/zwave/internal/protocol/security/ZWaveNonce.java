package org.openhab.binding.zwave.internal.protocol.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Class to represent a one-use token.
 * This class holds the number, and timeout for the NONCE.
 * 
 * @author Chris Jackson - Initial contribution
 *
 */
public class ZWaveNonce {
    private final long RESEED_INTERVAL = TimeUnit.HOURS.toMillis(6);
    private final long timeout = 12000000000L;
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

        // Start the timer
        this.timer = System.nanoTime();
    }

    public ZWaveNonce(byte[] nonceBytes) {
        // Save the nonce
        this.nonceBytes = nonceBytes;

        // Start the timer
        this.timer = System.nanoTime();
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

    public void setNonceBytes(byte[] nonceBytes) {
        this.nonceBytes = nonceBytes;
    }

    private String bb2hex(byte[] bb) {
        if (bb == null) {
            return "not set";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < bb.length; i++) {
            result.append(String.format("%02X ", bb[i]));
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return "ZWaveNonce [nonceBytes=(" + bb2hex(nonceBytes) + "), timer=" + (System.nanoTime() - timer) + ", valid="
                + isValid() + "]";
    }

}
