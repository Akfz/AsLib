package v.akfz.aslib.world.preset;

import net.minecraft.resources.ResourceLocation;

public enum VanillaNoiseSettings {
    OVERWORLD("overworld"),
    LARGE_BIOMES("large_biomes"),
    AMPLIFIED("amplified"),
    NETHER("nether"),
    END("end"),
    CAVES("caves"),
    FLOATING_ISLANDS("floating_islands");

    private final ResourceLocation location;

    VanillaNoiseSettings(String path) {
        this.location = new ResourceLocation("minecraft", path);
    }

    public ResourceLocation location() {
        return location;
    }
}