package v.akfz.aslib.datagen.block.blockstate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

//Генерирует blockstate с кастомными данными
public class CustomBlockStateVariant implements BlockStateVariant {
    private final String key;
    private final JsonObject customData;

    public CustomBlockStateVariant(String key, JsonObject customData) {
        this.key = key;
        this.customData = customData != null ? customData : new JsonObject();
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public JsonElement serialize() {
        return this.customData;
    }
}