package v.akfz.aslib.resourcepack;

import net.minecraft.server.packs.repository.RepositorySource;

public interface ResourcePackExpander {
    void addProvider(RepositorySource provider);
    void removeProvider(RepositorySource provider);
}

