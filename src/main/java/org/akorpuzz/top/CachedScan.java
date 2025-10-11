package org.akorpuzz.top;

public class CachedScan {
    public final double value;
    public final long timestamp;

    private static final long EXPIRATION_MS = 5 * 60 * 1000; // 5 minutos

    public CachedScan(double value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > EXPIRATION_MS;
    }
}

