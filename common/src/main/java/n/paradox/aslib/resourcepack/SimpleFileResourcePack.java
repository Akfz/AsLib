package n.paradox.aslib.resourcepack;

import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

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
public class SimpleFileResourcePack implements ResourcePack,FileResourcePack {
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
    public ResourcePack getPack() {
        return this;
    }

    @Override
    public InputSupplier<InputStream> openRoot(String... segments) {
        return null;
    }

    @Override
    public InputSupplier<InputStream> open(ResourceType type, Identifier id) {
        if (!id.getNamespace().equals(namespace) || type != ResourceType.CLIENT_RESOURCES) {
            return null;
        }

        String path = id.getPath();
        Path filePath = cacheFiles.get(path);

        if (filePath != null && Files.exists(filePath)) {
            return InputSupplier.create(filePath);
        }

        return null;
    }

    @Override
    public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
        if (!namespace.equals(this.namespace) || type != ResourceType.CLIENT_RESOURCES) {
            return;
        }

        for (Map.Entry<String, Path> entry : cacheFiles.entrySet()) {
            String path = entry.getKey();
            Path filePath = entry.getValue();

            if (path.startsWith(prefix)) {
                Identifier id = Identifier.of(namespace, path);
                consumer.accept(id, InputSupplier.create(filePath));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return (type == ResourceType.CLIENT_RESOURCES) ? known_namespaces : Set.of();
    }

    @Override
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
        return null;
    }

    @Override
    public String getName() {
        return pack_name;
    }

    @Override
    public void close() {
        cacheFiles.clear();
    }
}

