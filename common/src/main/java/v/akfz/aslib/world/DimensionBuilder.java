package v.akfz.aslib.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import v.akfz.aslib.resourcepack.dynamic.DynamicDataPack;
import v.akfz.aslib.world.preset.VanillaBiomes;
import v.akfz.aslib.world.preset.VanillaNoiseSettings;

import java.util.ArrayList;
import java.util.List;

public class DimensionBuilder {
    private final ResourceLocation id;
    private ResourceLocation dimensionType = new ResourceLocation("minecraft", "overworld");
    private GeneratorType generatorType = GeneratorType.FLAT;
    private ResourceLocation biome = new ResourceLocation("minecraft", "the_void");
    private final List<LayerEntry> flatLayers = new ArrayList<>();
    private ResourceLocation noiseSettings = new ResourceLocation("minecraft", "overworld");
    private boolean enableStructures = false;

    public DimensionBuilder(ResourceLocation id) {
        this.id = id;
    }

    public static DimensionBuilder create(ResourceLocation id) {
        return new DimensionBuilder(id);
    }

    public DimensionBuilder type(ResourceLocation type) {
        this.dimensionType = type;
        return this;
    }

    public DimensionBuilder generator(GeneratorType generatorType) {
        this.generatorType = generatorType;
        return this;
    }

    public DimensionBuilder biome(ResourceLocation biome) {
        this.biome = biome;
        return this;
    }

    public DimensionBuilder biome(VanillaBiomes biome) {
        return biome(biome.location());
    }

    public DimensionBuilder noiseSettings(ResourceLocation settings) {
        this.noiseSettings = settings;
        return this;
    }

    public DimensionBuilder noiseSettings(VanillaNoiseSettings settings) {
        return noiseSettings(settings.location());
    }

    public DimensionBuilder enableStructures(boolean enable) {
        this.enableStructures = enable;
        return this;
    }

    public DimensionBuilder addLayer(ResourceLocation blockId, int height) {
        this.flatLayers.add(new LayerEntry(blockId, height));
        return this;
    }

    public DimensionBuilder voidPreset() {
        this.generatorType = GeneratorType.FLAT;
        this.biome = new ResourceLocation("minecraft", "the_void");
        this.flatLayers.clear();
        this.enableStructures = false;
        return this;
    }

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

    public enum GeneratorType {
        FLAT,
        NOISE
    }

    public record LayerEntry(ResourceLocation blockId, int height) {}
}