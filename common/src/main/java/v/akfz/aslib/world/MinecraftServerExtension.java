package v.akfz.aslib.world;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.util.Map;

public interface MinecraftServerExtension {
    Map<ResourceKey<Level>, ServerLevel> aslib$getLevels();
    LevelStorageSource.LevelStorageAccess aslib$getStorageSource();
}