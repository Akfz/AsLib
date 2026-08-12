package v.akfz.aslib.world;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public final class DimensionHelper {

    private static final Map<ResourceLocation, DimensionConfig> CONFIG_MAP = new ConcurrentHashMap<>();

    private DimensionHelper() {}

    public static void registerConfig(ResourceLocation id, DimensionBuilder.GeneratorType type, ResourceLocation biome, List<DimensionBuilder.LayerEntry> layers, ResourceLocation noiseSettings, boolean enableStructures) {
        CONFIG_MAP.put(id, new DimensionConfig(type, biome, new ArrayList<>(layers), noiseSettings, enableStructures));
    }

    public static DimensionBuilder builder(ResourceLocation dimensionId) {
        return DimensionBuilder.create(dimensionId);
    }

    public static ResourceKey<Level> registerVoid(ResourceLocation dimensionId) {
        return builder(dimensionId).voidPreset().register();
    }

    public static TeleportBuilder teleport(Entity entity, ServerLevel targetLevel) {
        return new TeleportBuilder(entity, targetLevel);
    }

    public static ServerLevel getOrCreateLevel(MinecraftServer server, ResourceLocation dimensionId) {
        if (server == null || dimensionId == null) return null;

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel existing = server.getLevel(key);
        if (existing != null) {
            return existing;
        }

        try {
            ServerLevel overworld = server.overworld();
            DimensionConfig config = CONFIG_MAP.get(dimensionId);

            ChunkGenerator chunkGenerator;

            if (config != null && config.generatorType == DimensionBuilder.GeneratorType.NOISE) {
                ResourceLocation noiseKey = config.noiseSettings != null ? config.noiseSettings : NoiseGeneratorSettings.OVERWORLD.location();
                ResourceLocation biomeKey = config.biome != null ? config.biome : new ResourceLocation("minecraft", "plains");

                HolderGetter<NoiseGeneratorSettings> noiseRegistry = server.registryAccess().lookupOrThrow(Registries.NOISE_SETTINGS);
                Holder<NoiseGeneratorSettings> noiseHolder = noiseRegistry.getOrThrow(ResourceKey.create(Registries.NOISE_SETTINGS, noiseKey));

                HolderGetter<Biome> biomeRegistry = server.registryAccess().lookupOrThrow(Registries.BIOME);
                Holder<Biome> biomeHolder = biomeRegistry.getOrThrow(ResourceKey.create(Registries.BIOME, biomeKey));

                chunkGenerator = new NoiseBasedChunkGenerator(new FixedBiomeSource(biomeHolder), noiseHolder);

            } else {
                ResourceLocation biomeKey = (config != null && config.biome != null) ? config.biome : Biomes.THE_VOID.location();

                HolderGetter<Biome> biomeRegistry = server.registryAccess().lookupOrThrow(Registries.BIOME);
                Holder<Biome> biomeHolder = biomeRegistry.get(ResourceKey.create(Registries.BIOME, biomeKey))
                        .orElseGet(() -> biomeRegistry.getOrThrow(Biomes.THE_VOID));

                Optional<HolderSet<StructureSet>> structureOverrides = (config != null && config.enableStructures)
                        ? Optional.empty()
                        : Optional.of(HolderSet.direct());

                FlatLevelGeneratorSettings flatSettings = new FlatLevelGeneratorSettings(
                        structureOverrides,
                        biomeHolder,
                        List.of()
                );

                if (config != null && !config.flatLayers.isEmpty()) {
                    for (DimensionBuilder.LayerEntry layer : config.flatLayers) {
                        Block block = BuiltInRegistries.BLOCK.get(layer.blockId());
                        if (block != null) {
                            flatSettings.getLayersInfo().add(new FlatLayerInfo(layer.height(), block));
                        }
                    }
                } else {
                    // Чистый воздух для Void
                    flatSettings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.AIR));
                }

                chunkGenerator = new FlatLevelSource(flatSettings);
            }

            Holder<DimensionType> dimTypeHolder = overworld.dimensionTypeRegistration();
            LevelStem levelStem = new LevelStem(dimTypeHolder, chunkGenerator);

            LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerExtension) server).aslib$getStorageSource();
            Executor backgroundExecutor = Util.backgroundExecutor();
            DerivedLevelData levelData = new DerivedLevelData(server.getWorldData(), server.getWorldData().overworldData());

            ServerLevel newLevel = new ServerLevel(
                    server,
                    backgroundExecutor,
                    storageAccess,
                    levelData,
                    key,
                    levelStem,
                    new ChunkProgressListener() {
                        @Override public void updateSpawnPos(ChunkPos spawnPos) {}
                        @Override public void onStatusChange(ChunkPos chunkPosition, ChunkStatus newStatus) {}
                        @Override public void start() {}
                        @Override public void stop() {}
                    },
                    false,
                    BiomeManager.obfuscateSeed(server.getWorldData().worldGenOptions().seed()),
                    List.of(),
                    true,
                    overworld.getRandomSequences()
            );

            overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newLevel.getWorldBorder()));

            Map<ResourceKey<Level>, ServerLevel> levelsMap = ((MinecraftServerExtension) server).aslib$getLevels();
            levelsMap.put(key, newLevel);

            return newLevel;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ServerLevel getLevel(Level currentLevel, ResourceLocation dimensionId) {
        if (currentLevel != null && currentLevel.getServer() != null) {
            return getOrCreateLevel(currentLevel.getServer(), dimensionId);
        }
        return null;
    }

    private record DimensionConfig(
            DimensionBuilder.GeneratorType generatorType,
            ResourceLocation biome,
            List<DimensionBuilder.LayerEntry> flatLayers,
            ResourceLocation noiseSettings,
            boolean enableStructures
    ) {}
}