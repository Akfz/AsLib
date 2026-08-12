package v.akfz.aslib.world.preset;

import net.minecraft.resources.ResourceLocation;

public enum VanillaBiomes {
    THE_VOID("the_void"),
    PLAINS("plains"),
    DESERT("desert"),
    FOREST("forest"),
    TAIGA("taiga"),
    SWAMP("swamp"),
    JUNGLE("jungle"),
    OCEAN("ocean"),
    DEEP_OCEAN("deep_ocean"),
    DEEP_DARK("deep_dark"),
    MEADOW("meadow"),
    NETHER_WASTES("nether_wastes"),
    CRIMSON_FOREST("crimson_forest"),
    WARPED_FOREST("warped_forest"),
    SOUL_SAND_VALLEY("soul_sand_valley"),
    BASALT_DELTAS("basalt_deltas"),
    THE_END("the_end"),
    END_MIDLANDS("end_midlands"),
    END_BARRENS("end_barrens");

    private final ResourceLocation location;

    VanillaBiomes(String path) {
        this.location = new ResourceLocation("minecraft", path);
    }

    public ResourceLocation location() {
        return location;
    }
}