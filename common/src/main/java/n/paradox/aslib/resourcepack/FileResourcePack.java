package n.paradox.aslib.resourcepack;

import net.minecraft.resource.ResourcePack;

public interface FileResourcePack {
    String getSimpleNamespace();
    void refreshCache();

    ResourcePack getPack();
}
