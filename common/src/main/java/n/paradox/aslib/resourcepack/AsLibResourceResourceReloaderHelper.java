package n.paradox.aslib.resourcepack;

import net.minecraft.resource.ResourcePack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//Управляющий класс с listener(ами) для reloader(а)
public final class AsLibResourceResourceReloaderHelper {
    private static Map<String, ResourceReloadListener> resourceReloadListenerMap = new HashMap<>();

    public static void register(ResourcePack pack, ResourceReloadListener listener) {
        resourceReloadListenerMap.put(pack.getName(),listener);
    }
    public static void unRegister(ResourcePack pack) {
        if (pack != null) {
            resourceReloadListenerMap.remove(pack.getName());
        }
    }
    public static Map<String, ResourceReloadListener> getReloadMap() {
        return Collections.unmodifiableMap(resourceReloadListenerMap);
    }

    @Nullable
    public static ResourceReloadListener getListener(ResourcePack pack) {
        if (pack == null) return null;
        return resourceReloadListenerMap.get(pack.getName());
    }
}
