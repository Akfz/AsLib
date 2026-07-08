package v.akfz.aslib.datagen.api;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

//Класс для генерации файла и пути
public abstract class DataSerializable implements Serializable<JsonElement> {
    private final ResourceLocation path;

    public DataSerializable(ResourceLocation path) {
        this.path = path;
    }

    @Override
    public ResourceLocation getRLPath() {
        return this.path;
    }

    @Override // реализация сериализации
    public abstract JsonElement serialize();
}
