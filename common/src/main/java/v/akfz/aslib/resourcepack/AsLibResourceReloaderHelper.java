package v.akfz.aslib.resourcepack;

import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry manager for tracking global and pack-specific resource reload listeners.
 */
public final class AsLibResourceReloaderHelper {
    private static final Map<String, ResourceReloadListener> packListeners = new ConcurrentHashMap<>();
    private static final Map<String, ResourceReloadListener> globalListeners = new ConcurrentHashMap<>();

    private AsLibResourceReloaderHelper() {}

    /**
     * Registers a global listener triggered on any resource reload.
     */
    public static void register(String id, ResourceReloadListener listener) {
        globalListeners.put(id, listener);
    }

    /**
     * Registers a listener bound to a specific pack instance.
     */
    public static void register(PackResources pack, ResourceReloadListener listener) {
        packListeners.put(pack.packId(), listener);
    }

    /**
     * Unregisters a pack-bound listener.
     */
    public static void unRegister(PackResources pack) {
        if (pack != null) {
            packListeners.remove(pack.packId());
        }
    }

    /**
     * Retrieves the listener bound to a specific pack.
     */
    @Nullable
    public static ResourceReloadListener getListener(PackResources pack) {
        if (pack == null) return null;
        return packListeners.get(pack.packId());
    }

    /**
     * Retrieves all registered global reload listeners.
     */
    public static Collection<ResourceReloadListener> getGlobalListeners() {
        return globalListeners.values();
    }
}