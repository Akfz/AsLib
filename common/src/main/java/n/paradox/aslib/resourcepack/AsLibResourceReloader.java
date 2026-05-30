package n.paradox.aslib.resourcepack;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

//ьоьоьоьоьоьоьоьоььо
public final class AsLibResourceReloader implements ResourceReloader {

    @Override
    public CompletableFuture<Void> reload(
            Synchronizer synchronizer,
            ResourceManager manager,
            Profiler prepareProfiler,
            Profiler applyProfiler,
            Executor prepareExecutor,
            Executor applyExecutor
    ) {
        return synchronizer.whenPrepared(null)
                .thenRunAsync(() -> {
                    applyProfiler.push("aslib_cache_refresh");
                    try {
                        manager.streamResourcePacks().forEach(pack -> {
                            if (pack instanceof FileResourcePack frp) {
                                frp.refreshCache();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        applyProfiler.pop();
                    }

                    applyProfiler.push("aslib_reload_callbacks");
                    try {
                        manager.streamResourcePacks().forEach(pack -> {
                            ResourceReloadListener listener = AsLibResourceResourceReloaderHelper.getListener(pack);

                            if (listener != null) {
                                try {
                                    listener.onReload(manager);
                                } catch (Exception e) {
                                    System.err.println("[ASLib] Error inside listener for pack: " + pack.getName());
                                    e.printStackTrace();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        applyProfiler.pop();
                    }
                }, applyExecutor);
    }
}