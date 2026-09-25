package v.akfz.aslib.network.bundle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BundleManager {
    private static final Map<Long, TimedHeader> PENDING_HEADERS = new ConcurrentHashMap<>();

    public static void registerHeader(BundleHeaderPacket header) {
        PENDING_HEADERS.put(header.getBundleId(), new TimedHeader(header, System.currentTimeMillis()));
    }

    public static BundleHeaderPacket getAndRemoveHeader(long bundleId) {
        TimedHeader timed = PENDING_HEADERS.remove(bundleId);
        return timed != null ? timed.header() : null;
    }

    public static void cleanupStaleHeaders() {
        long now = System.currentTimeMillis();
        PENDING_HEADERS.entrySet().removeIf(entry -> now - entry.getValue().timestamp() > 5000);
    }

    private record TimedHeader(BundleHeaderPacket header, long timestamp) {}
}