package v.akfz.aslib.datagen.api;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

/**
 * "Class for generating a file and a path"
 */
public abstract class DataSerializable implements Serializable<JsonElement> {
    private final ResourceLocation path;

    public DataSerializable(ResourceLocation path) {
        this.path = path;
    }

    @Override
    public ResourceLocation getRLPath() {
        return this.path;
    }

    @Override
    public abstract JsonElement serialize();
}
