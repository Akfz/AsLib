package n.paradox.aslib.resourcepack;

import net.minecraft.server.packs.PackResources;

public interface FileResourcePack {
    String getSimpleNamespace();
    void refreshCache();

    PackResources getPack();
}
