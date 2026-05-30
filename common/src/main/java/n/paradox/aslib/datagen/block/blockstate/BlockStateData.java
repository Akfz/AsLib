package n.paradox.aslib.datagen.block.blockstate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import n.paradox.aslib.datagen.api.DataSerializable;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

//Генерирует blockstates из BlockStateVariant 🐱
public class BlockStateData extends DataSerializable {
    private final List<BlockStateVariant> variants = new ArrayList<>();

    public BlockStateData(Identifier blockId) {
        super(new Identifier(blockId.getNamespace(), "blockstates/" + blockId.getPath()));
    }

    public BlockStateData addVariant(BlockStateVariant variant) {
        if (variant != null) {
            this.variants.add(variant);
        }
        return this;
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
