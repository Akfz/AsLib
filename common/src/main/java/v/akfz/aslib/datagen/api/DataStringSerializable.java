package v.akfz.aslib.datagen.api;

import net.minecraft.resources.ResourceLocation;

//DataSerializable, но вместо JsonElement - String (для не json файлов)
public abstract class DataStringSerializable implements Serializable<String> {
    private final ResourceLocation path;

    public DataStringSerializable(ResourceLocation path) {
        this.path = path;
    }

    @Override
    public ResourceLocation getRLPath() {
        return this.path;
    }

    @Override
    public abstract String serialize();
}
