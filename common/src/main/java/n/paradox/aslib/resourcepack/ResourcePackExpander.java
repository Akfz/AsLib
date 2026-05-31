package n.paradox.aslib.resourcepack;

import net.minecraft.server.packs.repository.RepositorySource;

public interface ResourcePackExpander {
    void addProvider(RepositorySource provider);
}

