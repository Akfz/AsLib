package n.paradox.aslib.resourcepack;

import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//Управляющий класс с listener(ами) для reloader(а)
public final class AsLibResourceResourceReloaderHelper {
    private static Map<String, ResourceReloadListener> resourceReloadListenerMap = new HashMap<>();

    public static void register(String id, ResourceReloadListener listener) {
        resourceReloadListenerMap.put(id,listener);
    }
    public static void register(PackResources pack, ResourceReloadListener listener) {
        resourceReloadListenerMap.put(pack.packId(),listener);
    }
    public static void unRegister(PackResources pack) {
        if (pack != null) {
            resourceReloadListenerMap.remove(pack.packId());
        }
    }
    public static Map<String, ResourceReloadListener> getReloadMap() {
        return Collections.unmodifiableMap(resourceReloadListenerMap);
    }

    @Nullable
    public static ResourceReloadListener getListener(PackResources pack) {
        if (pack == null) return null;
        return resourceReloadListenerMap.get(pack.packId());
    }
}
