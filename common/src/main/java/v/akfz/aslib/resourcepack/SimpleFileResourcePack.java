package v.akfz.aslib.resourcepack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

//Простой пример для работы с ресурс паками вне кода (т.е src/ч/resources)
public class SimpleFileResourcePack implements PackResources, FileResourcePack {
    private final String pack_name;
    private final String namespace;
    private final Set<String> known_namespaces;
    private final Path root;

    private final Map<String, Path> cacheFiles = new ConcurrentHashMap<>();

    public SimpleFileResourcePack(String packName, Path root, String namespace) {
        this.pack_name = packName;
        this.root = root;
        this.namespace = namespace;
        this.known_namespaces = Set.of(namespace);
        preloadCache();
    }

    @Override
    public String getSimpleNamespace() {
        return this.namespace;
    }

    public Map<String, Path> getCache() {
        return new HashMap<>(cacheFiles);
    }

    private void preloadCache() {
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String relativePath = root.relativize(path).toString().replace("\\", "/");
                cacheFiles.put(relativePath, path);
            });
        } catch (IOException e) {
            System.err.println("Ошибка предзагрузки кэша " + pack_name + " : " + e.getMessage());
        }
    }

    @Override
    public void refreshCache() {
        cacheFiles.clear();
        preloadCache();
    }

    @Override
    public PackResources getPack() {
        return this;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... strings) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        if (!resourceLocation.getNamespace().equals(namespace)) {
            return null;
        }

        String path = resourceLocation.getPath();

        Path filePath = cacheFiles.get(path);

        if (filePath == null || !Files.exists(filePath)) {
            String folderPrefix = (packType == PackType.CLIENT_RESOURCES ? "assets/" : "data/") + namespace + "/";
            filePath = cacheFiles.get(folderPrefix + path);
        }

        if (filePath != null && Files.exists(filePath)) {
            return IoSupplier.create(filePath);
        }

        return null;
    }

    @Override
    public void listResources(PackType packType, String namespace, String prefix, ResourceOutput resourceOutput) {
        if (!namespace.equals(this.namespace)) {
            return;
        }

        String folderPrefix = (packType == PackType.CLIENT_RESOURCES ? "assets/" : "data/") + namespace + "/";

        for (Map.Entry<String, Path> entry : cacheFiles.entrySet()) {
            String key = entry.getKey();
            Path filePath = entry.getValue();

            String relativePath = key;
            if (key.startsWith(folderPrefix)) {
                relativePath = key.substring(folderPrefix.length());
            }

            if (relativePath.startsWith(prefix)) {
                ResourceLocation id = new ResourceLocation(namespace, relativePath);
                resourceOutput.accept(id, IoSupplier.create(filePath));
            }
        }
    }

    @Override
    public @NotNull Set<String> getNamespaces(PackType packType) {
        return known_namespaces;
    }

    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
        return null;
    }

    @Override
    public String packId() {
        return pack_name;
    }

    @Override
    public void close() {
        cacheFiles.clear();
    }
}