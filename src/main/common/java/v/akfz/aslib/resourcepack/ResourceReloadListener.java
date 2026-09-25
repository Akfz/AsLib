package v.akfz.aslib.resourcepack;

import net.minecraft.server.packs.resources.ResourceManager;

public interface ResourceReloadListener {
    void onReload(ResourceManager manager);
}
