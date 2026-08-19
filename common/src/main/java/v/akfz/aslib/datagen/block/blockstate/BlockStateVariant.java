package v.akfz.aslib.datagen.block.blockstate;

import com.google.gson.JsonElement;

/**
 * mff
 */
public interface BlockStateVariant {
    String getKey();
    JsonElement serialize();
}
