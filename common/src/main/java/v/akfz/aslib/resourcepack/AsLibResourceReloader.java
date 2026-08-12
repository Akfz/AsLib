package v.akfz.aslib.resourcepack;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class AsLibResourceReloader implements PreparableReloadListener {
    @Override
    public CompletableFuture<Void> reload(PreparationBarrier synchronizer, ResourceManager manager, ProfilerFiller prepareProfiler,
                                          ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        return synchronizer.wait(null).thenRunAsync(() -> {
            applyProfiler.push("aslib_cache_refresh");
            try {
                manager.listPacks().forEach(pack -> {
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
                manager.listPacks().forEach(pack -> {
                    ResourceReloadListener listener = AsLibResourceResourceReloaderHelper.getListener(pack);

                    if (listener != null) {
                        try {
                            listener.onReload(manager);
                        } catch (Exception e) {
                            System.err.println("[ASLib] Error inside listener for pack: " + pack.packId());
                            e.printStackTrace();
                        }
                    }
                });

                for (ResourceReloadListener globalListener : AsLibResourceResourceReloaderHelper.getGlobalListeners()) {
                    try {
                        globalListener.onReload(manager);
                    } catch (Exception e) {
                        System.err.println("[ASLib] Error inside global resource reload listener");
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                applyProfiler.pop();
            }
        }, applyExecutor);
    }
}