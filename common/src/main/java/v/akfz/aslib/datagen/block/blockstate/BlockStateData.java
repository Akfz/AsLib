package v.akfz.aslib.datagen.block.blockstate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import v.akfz.aslib.datagen.api.DataSerializable;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//Генерирует blockstates из BlockStateVariant 🐱
public class BlockStateData extends DataSerializable {
    private final List<BlockStateVariant> variants = new ArrayList<>();

    public BlockStateData(ResourceLocation blockId) {
        super(new ResourceLocation(blockId.getNamespace(), "blockstates/" + blockId.getPath()));
    }

    public BlockStateData addVariant(BlockStateVariant variant) {
        if (variant != null) {
            this.variants.add(variant);
        }
        return this;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public JsonElement serialize() {
        JsonObject rootJson = new JsonObject();
        JsonObject variantsJson = new JsonObject();

        for (BlockStateVariant variant : variants) {
            variantsJson.add(variant.getKey(), variant.serialize());
        }

        rootJson.add("variants", variantsJson);
        return rootJson;
    }
}
