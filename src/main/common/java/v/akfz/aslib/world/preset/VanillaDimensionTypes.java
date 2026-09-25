package v.akfz.aslib.world.preset;

import net.minecraft.resources.ResourceLocation;

/**
 * Preset enum representing standard vanilla dimension type identifiers.
 */
public enum VanillaDimensionTypes {
    OVERWORLD("overworld"),
    NETHER("the_nether"),
    END("the_end"),
    OVERWORLD_CAVES("overworld_caves");

    private final ResourceLocation location;

    VanillaDimensionTypes(String path) {
        this.location = new ResourceLocation("minecraft", path);
    }

    public ResourceLocation location() {
        return location;
    }
}