package v.akfz.aslib.resourcepack.configpack;

import v.akfz.aslib.resourcepack.SimpleFileResourcePack;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigPackRegistry {
    private ConfigPackRegistry() {}

    @FunctionalInterface
    public interface PackFactory {
        SimpleFileResourcePack create(Path rpDir, ConfigPackData data);
    }

    private static final Map<String, PackFactory> FACTORIES = new ConcurrentHashMap<>();

    static {
        register("default", (rpDir, data) -> new SimpleFileResourcePack(data.name, rpDir, data.id));
    }

    public static void register(String type, PackFactory factory) {
        FACTORIES.put(type, factory);
    }

    public static SimpleFileResourcePack create(String type, Path rpDir, ConfigPackData data) {
        PackFactory factory = FACTORIES.getOrDefault(type, FACTORIES.get("default"));
        return factory.create(rpDir, data);
    }
}