package v.akfz.aslib.datagen.block.blockstate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

//Генерирует blockstate с поворотами (x,y)
public class RotatableBlockStateVariant implements BlockStateVariant {
    private final String key;
    private final ResourceLocation modelId;
    private final int rotX;
    private final int rotY;
    private final boolean uvLock;

    public RotatableBlockStateVariant(String key, ResourceLocation modelId, int rotY) {
        this(key, modelId, 0, rotY, false);
    }

    public RotatableBlockStateVariant(String key, ResourceLocation modelId, int rotX, int rotY, boolean uvLock) {
        this.key = key;
        this.modelId = modelId;
        this.rotX = normalizeRotation(rotX);
        this.rotY = normalizeRotation(rotY);
        this.uvLock = uvLock;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("model", modelId.toString());

        if (rotX != 0) json.addProperty("x", rotX);
        if (rotY != 0) json.addProperty("y", rotY);
        if (uvLock) json.addProperty("uvlock", true);

        return json;
    }

    private int normalizeRotation(int rot) {
        int normalized = (rot % 360 + 360) % 360;
        if (normalized % 90 != 0) {
            System.err.println("[ASLib-DataGen] Warning: Rotation " + rot + " is not a multiple of 90! Snapping to 0.");
            return 0;
        }
        return normalized;
    }
}