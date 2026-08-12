package v.akfz.aslib.datagen.api;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

//T - вывод, типо String или JsonElement и др.
public interface Serializable<T> {
    @Nullable ResourceLocation getRLPath();
    @Nullable Path getPath(); // если isAsset, то ищет по getRLPath, если нет, то по этому

    T serialize();

    //assets/ (для datagen resources)
    default boolean isAsset() {
        return true;
    }
    default String getExtension() {
        return "json";
    }

    // для генерации типо src/main ...
    default boolean isSystem() {
        return false;
    }
}
