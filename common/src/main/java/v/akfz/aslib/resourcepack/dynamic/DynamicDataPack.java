package v.akfz.aslib.resourcepack.dynamic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.resourcepack.AddResourcePack;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicDataPack implements PackResources {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final DynamicDataPack INSTANCE = new DynamicDataPack();

    private static final Set<PackRepository> REGISTERED_REPOS = ConcurrentHashMap.newKeySet();

    private final Map<ResourceLocation, byte[]> virtualFiles = new ConcurrentHashMap<>();
    private final Set<String> namespaces = ConcurrentHashMap.newKeySet();

    private DynamicDataPack() {}

    public static DynamicDataPack getInstance() {
        return INSTANCE;
    }

    public static void addData(ResourceLocation location, JsonElement json) {
        byte[] bytes = GSON.toJson(json).getBytes(StandardCharsets.UTF_8);
        INSTANCE.virtualFiles.put(location, bytes);
        INSTANCE.namespaces.add(location.getNamespace());
    }

    public static void registerToRepository(PackRepository repository) {
        if (REGISTERED_REPOS.add(repository)) {
            AddResourcePack.addServerData(
                    repository,
                    INSTANCE,
                    Component.literal("ASLib Built-in Dynamic DataPack"),
                    "aslib_dynamic_datapack",
                    Component.literal("Dynamic DataPack"),
                    true,
                    Pack.Position.TOP,
                    true,
                    PackSource.BUILT_IN
            );
        }
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... strings) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        if (packType != PackType.SERVER_DATA) return null;

        byte[] data = virtualFiles.get(location);
        if (data != null) {
            return () -> new ByteArrayInputStream(data);
        }

        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String prefix, ResourceOutput resourceOutput) {
        if (packType != PackType.SERVER_DATA) return;

        for (Map.Entry<ResourceLocation, byte[]> entry : virtualFiles.entrySet()) {
            ResourceLocation loc = entry.getKey();
            if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(prefix)) {
                byte[] bytes = entry.getValue();
                resourceOutput.accept(loc, () -> new ByteArrayInputStream(bytes));
            }
        }
    }

    @Override
    public @NotNull Set<String> getNamespaces(PackType packType) {
        return packType == PackType.SERVER_DATA ? namespaces : Set.of();
    }

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
        return null;
    }

    @Override
    public String packId() {
        return "aslib_dynamic_datapack";
    }

    @Override
    public void close() {
        virtualFiles.clear();
    }
}