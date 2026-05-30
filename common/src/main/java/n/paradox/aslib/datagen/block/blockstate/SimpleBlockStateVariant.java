package n.paradox.aslib.datagen.block.blockstate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import n.paradox.aslib.datagen.block.blockstate.BlockStateVariant;
import net.minecraft.util.Identifier;

// Генерирует blockstate без поворотов и т.д, только статичный рендер модели
public class SimpleBlockStateVariant implements BlockStateVariant {
    private final String key;
    private final Identifier modelId;

    public SimpleBlockStateVariant(Identifier modelId) {
        this("", modelId);
    }

    public SimpleBlockStateVariant(String key, Identifier modelId) {
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