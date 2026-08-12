package v.akfz.aslib.resourcepack;

import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AsLibResourceResourceReloaderHelper {
    private static final Map<String, ResourceReloadListener> packListeners = new HashMap<>();

    private static final Map<String, ResourceReloadListener> globalListeners = new ConcurrentHashMap<>();

    public static void register(String id, ResourceReloadListener listener) {
        globalListeners.put(id, listener);
    }

    public static void register(PackResources pack, ResourceReloadListener listener) {
        packListeners.put(pack.packId(), listener);
    }

    public static void unRegister(PackResources pack) {
        if (pack != null) {
            packListeners.remove(pack.packId());
        }
    }

    @Nullable
    public static ResourceReloadListener getListener(PackResources pack) {
        if (pack == null) return null;
        return packListeners.get(pack.packId());
    }

    public static Collection<ResourceReloadListener> getGlobalListeners() {
        return globalListeners.values();
    }
}