package v.akfz.aslib.resourcepack;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;

//Добавляет ресурс паки в клиент(P.s только в клиент, если понадобится обновлю до сервера)
public final class AddResourcePack {

    public static void add(PackRepository manager, PackResources pack, Component description,
                           String id, Component displayName, boolean alwaysEnabled,
                           Pack.Position pos, boolean pinned, PackSource source) {
        add(manager, pack, description, id, displayName, alwaysEnabled, pos, pinned, source, PackType.CLIENT_RESOURCES);
    }

    public static void addFRP(PackRepository manager, FileResourcePack frp, Component description,
                              boolean alwaysEnabled, Pack.Position pos,
                              boolean pinned, PackSource source) {
        addFRP(manager, frp, description, alwaysEnabled, pos, pinned, source, PackType.CLIENT_RESOURCES);
    }

    public static void add(PackRepository manager, PackResources pack, Component description,
                           String id, Component displayName, boolean alwaysEnabled,
                           Pack.Position pos, boolean pinned, PackSource source, PackType packType) {
        registerInternal(manager, id, displayName, alwaysEnabled, pos, pinned, source, description, (name) -> pack, packType);
    }

    public static void addFRP(PackRepository manager, FileResourcePack frp, Component description,
                              boolean alwaysEnabled, Pack.Position pos,
                              boolean pinned, PackSource source, PackType packType) {
        registerInternal(manager, frp.getSimpleNamespace(), Component.literal(frp.getPack().packId()),
                alwaysEnabled, pos, pinned, source, description, (name) -> frp.getPack(), packType);
    }

    public static void addServerData(PackRepository manager, PackResources pack, Component description,
                                     String id, Component displayName, boolean alwaysEnabled,
                                     Pack.Position pos, boolean pinned, PackSource source) {
        add(manager, pack, description, id, displayName, alwaysEnabled, pos, pinned, source, PackType.SERVER_DATA);
    }

    public static void addServerDataFRP(PackRepository manager, FileResourcePack frp, Component description,
                                        boolean alwaysEnabled, Pack.Position pos,
                                        boolean pinned, PackSource source) {
        addFRP(manager, frp, description, alwaysEnabled, pos, pinned, source, PackType.SERVER_DATA);
    }

    private static void registerInternal(PackRepository manager, String id, Component name,
                                         boolean alwaysEnabled, Pack.Position pos,
                                         boolean pinned, PackSource source, Component description,
                                         Pack.ResourcesSupplier factory, PackType packType) {

        if (manager instanceof ResourcePackExpander r) {
            r.addProvider(profileAdder -> {
                int currentFormat = SharedConstants.getCurrentVersion().getPackVersion(packType);

                Pack.Info metadata = new Pack.Info(
                        description,
                        currentFormat,
                        FeatureFlagSet.of()
                );

                PackSource finalSource = alwaysEnabled ? new PackSource() {
                    @Override
                    public Component decorate(Component packName) {
                        return source.decorate(packName);
                    }

                    @Override
                    public boolean shouldAddAutomatically() {
                        return true;
                    }
                } : source;

                boolean isFixed = alwaysEnabled || pinned;

                Pack profile = Pack.create(
                        id,
                        name,
                        alwaysEnabled,
                        factory,
                        metadata,
                        packType,
                        pos,
                        isFixed,
                        finalSource
                );

                if (profile != null) {
                    profileAdder.accept(profile);
                }
            });
        }
    }
}