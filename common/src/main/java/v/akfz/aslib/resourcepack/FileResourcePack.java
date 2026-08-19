package v.akfz.aslib.resourcepack;

import net.minecraft.server.packs.PackResources;

/**
 * Interface representing a file-system backed resource pack capable of cache refreshing.
 */
public interface FileResourcePack {
    /**
     * @return Primary namespace of this pack.
     */
    String getSimpleNamespace();

    /**
     * Refreshes internal file location caches.
     */
    void refreshCache();

    /**
     * @return Underlying {@link PackResources} instance.
     */
    PackResources getPack();
}