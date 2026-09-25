package v.akfz.aslib.datagen.block.blockstate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * Generates simplest blockstate, without rotations etc., only static render model
 */
public class SimpleBlockStateVariant implements BlockStateVariant {
    private final String key;
    private final ResourceLocation modelId;

    public SimpleBlockStateVariant(ResourceLocation modelId) {
        this("", modelId);
    }

    public SimpleBlockStateVariant(String key, ResourceLocation modelId) {
        this.key = key;
        this.modelId = modelId;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("model", modelId.toString());
        return json;
    }
}