package v.akfz.aslib.datagen.api;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * @param <T> - output, like String or JsonElement and etc
 */
public interface Serializable<T> {
    @Nullable ResourceLocation getRLPath();
    @Nullable Path getPath();

    T serialize();

    /**
     * @return true - find path from getRLPath, false - find path from getPath
     */
    default boolean isAsset() {
        return true;
    }
    default String getExtension() {
        return "json";
    }

    /**
     * For generations like src/main...
     */
    default boolean isSystem() {
        return false;
    }
}
