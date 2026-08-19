package v.akfz.aslib.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import v.akfz.aslib.resourcepack.dynamic.DynamicDataPack;
import v.akfz.aslib.world.preset.VanillaBiomes;
import v.akfz.aslib.world.preset.VanillaDimensionTypes;
import v.akfz.aslib.world.preset.VanillaNoiseSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for dynamically constructing and registering dimension configurations and datapack JSONs.
 */
public class DimensionBuilder {
    private final ResourceLocation id;
    private ResourceLocation dimensionType = VanillaDimensionTypes.OVERWORLD.location();
    private GeneratorType generatorType = GeneratorType.FLAT;
    private ResourceLocation biome = VanillaBiomes.THE_VOID.location();
    private final List<LayerEntry> flatLayers = new ArrayList<>();
    private ResourceLocation noiseSettings = VanillaNoiseSettings.OVERWORLD.location();
    private boolean enableStructures = false;

    public DimensionBuilder(ResourceLocation id) {
        this.id = id;
    }

    /**
     * Creates a new instance of {@link DimensionBuilder}.
     *
     * @param id ResourceLocation identifier for the dimension.
     * @return New builder instance.
     */
    public static DimensionBuilder create(ResourceLocation id) {
        return new DimensionBuilder(id);
    }

    /**
     * Sets the dimension type ResourceLocation.
     */
    public DimensionBuilder type(ResourceLocation type) {
        this.dimensionType = type;
        return this;
    }

    /**
     * Sets the dimension type using {@link VanillaDimensionTypes}.
     */
    public DimensionBuilder type(VanillaDimensionTypes type) {
        return type(type.location());
    }

    /**
     * Sets the generator type (FLAT or NOISE).
     */
    public DimensionBuilder generator(GeneratorType generatorType) {
        this.generatorType = generatorType;
        return this;
    }

    /**
     * Sets the biome ResourceLocation.
     */
    public DimensionBuilder biome(ResourceLocation biome) {
        this.biome = biome;
        return this;
    }

    /**
     * Sets the biome using {@link VanillaBiomes}.
     */
    public DimensionBuilder biome(VanillaBiomes biome) {
        return biome(biome.location());
    }

    /**
     * Sets noise generator settings ResourceLocation.
     */
    public DimensionBuilder noiseSettings(ResourceLocation settings) {
        this.noiseSettings = settings;
        return this;
    }

    /**
     * Sets noise generator settings using {@link VanillaNoiseSettings}.
     */
    public DimensionBuilder noiseSettings(VanillaNoiseSettings settings) {
        return noiseSettings(settings.location());
    }

    /**
     * Sets whether structure generation is enabled.
     */
    public DimensionBuilder enableStructures(boolean enable) {
        this.enableStructures = enable;
        return this;
    }

    /**
     * Adds a layer for flat world generation.
     *
     * @param blockId ResourceLocation identifier of the block.
     * @param height  Layer thickness in blocks.
     */
    public DimensionBuilder addLayer(ResourceLocation blockId, int height) {
        this.flatLayers.add(new LayerEntry(blockId, height));
        return this;
    }

    /**
     * Applies default void dimension preset settings.
     */
    public DimensionBuilder voidPreset() {
        this.generatorType = GeneratorType.FLAT;
        this.biome = VanillaBiomes.THE_VOID.location();
        this.flatLayers.clear();
        this.enableStructures = false;
        return this;
    }

    /**
     * Registers dimension configuration internally and registers dynamic datapack JSON.
     *
     * @return ResourceKey representing the registered dimension level.
     */
    public ResourceKey<Level> register() {
        DimensionHelper.registerConfig(id, generatorType, biome, flatLayers, noiseSettings, enableStructures);

        JsonObject root = new JsonObject();
        root.addProperty("type", dimensionType.toString());

        JsonObject generator = new JsonObject();
        if (generatorType == GeneratorType.FLAT) {
            generator.addProperty("type", "minecraft:flat");

            JsonObject settings = new JsonObject();
            settings.addProperty("biome", biome.toString());

            JsonArray layers = new JsonArray();
            for (LayerEntry entry : flatLayers) {
                JsonObject layer = new JsonObject();
                layer.addProperty("block", entry.blockId().toString());
                layer.addProperty("height", entry.height());
                layers.add(layer);
            }
            settings.add("layers", layers);
            generator.add("settings", settings);
        } else {
            generator.addProperty("type", "minecraft:noise");
            generator.addProperty("settings", noiseSettings.toString());

            JsonObject biomeSource = new JsonObject();
            biomeSource.addProperty("type", "minecraft:fixed");
            biomeSource.addProperty("biome", biome.toString());
            generator.add("biome_source", biomeSource);
        }

        root.add("generator", generator);

        DynamicDataPack.addData(
                new ResourceLocation(id.getNamespace(), "dimension/" + id.getPath() + ".json"),
                root
        );

        return ResourceKey.create(Registries.DIMENSION, id);
    }

    /**
     * Supported world generator types.
     */
    public enum GeneratorType {
        FLAT,
        NOISE
    }

    /**
     * Record representing a single block layer entry for flat world generation.
     */
    public record LayerEntry(ResourceLocation blockId, int height) {}
}