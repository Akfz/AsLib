package v.akfz.aslib.datagen.block.blockstate;

import com.google.gson.JsonElement;

public interface BlockStateVariant {
    String getKey();
    JsonElement serialize();
}
