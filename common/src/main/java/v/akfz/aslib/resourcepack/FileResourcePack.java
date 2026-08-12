package v.akfz.aslib.resourcepack;

import net.minecraft.server.packs.PackResources;

public interface FileResourcePack {
    String getSimpleNamespace();
    void refreshCache();

    PackResources getPack();
}
