package n.paradox.aslib.resourcepack;

import net.minecraft.SharedConstants;
import net.minecraft.resource.*;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;

//Добавляет ресурс паки в клиент(P.s только в клиент, если понадобится обновлю до сервера)
public final class AddResourcePack {
    public static void add(ResourcePackManager manager, ResourcePack pack, Text description,
                           String id, Text displayName, boolean alwaysEnabled,
                           ResourcePackProfile.InsertionPosition pos, boolean pinned, ResourcePackSource source) {

        registerInternal(manager, id, displayName, alwaysEnabled, pos, pinned, source, description, (name) -> pack);
    }

    public static void addFRP(ResourcePackManager manager, FileResourcePack frp, Text description,
                              boolean alwaysEnabled, ResourcePackProfile.InsertionPosition pos,
                              boolean pinned, ResourcePackSource source) {

        registerInternal(manager, frp.getSimpleNamespace(), Text.of(frp.getPack().getName()),
                alwaysEnabled, pos, pinned, source, description, (name) -> frp.getPack());
    }

    private static void registerInternal(ResourcePackManager manager, String id, Text name,
                                         boolean alwaysEnabled, ResourcePackProfile.InsertionPosition pos,
                                         boolean pinned, ResourcePackSource source, Text description,
                                         ResourcePackProfile.PackFactory factory) {

        if (manager instanceof ResourcePackExpander r) {
            r.addProvider(profileAdder -> {
                int currentFormat = SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES);

                ResourcePackProfile.Metadata metadata = new ResourcePackProfile.Metadata(
                        description,
                        currentFormat,
                        FeatureSet.empty()
                );

                ResourcePackProfile profile = ResourcePackProfile.of(
                        id, name, alwaysEnabled, factory, metadata,
                        ResourceType.CLIENT_RESOURCES, pos, pinned, source
                );

                profileAdder.accept(profile);
            });
        }
    }
}

