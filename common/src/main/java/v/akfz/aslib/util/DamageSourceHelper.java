package v.akfz.aslib.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.datagen.damagetype.DamageTypeData;
import v.akfz.aslib.resourcepack.dynamic.DynamicDataPack;

import java.util.Optional;

public final class DamageSourceHelper {

    private DamageSourceHelper() {}

    public static ResourceKey<DamageType> createKey(String id) {
        return createKey(new ResourceLocation(id));
    }

    public static ResourceKey<DamageType> createKey(ResourceLocation location) {
        return createKey(location, new DamageTypeData(location));
    }

    public static ResourceKey<DamageType> createKey(String id, DamageTypeData data) {
        return createKey(new ResourceLocation(id), data);
    }

    public static ResourceKey<DamageType> createKey(String id, DamageTypeData data, String subProjectName) {
        return createKey(new ResourceLocation(id), data);
    }

    public static ResourceKey<DamageType> createKey(ResourceLocation location, DamageTypeData data) {
        ResourceLocation jsonPath = new ResourceLocation(location.getNamespace(), "damage_type/" + location.getPath() + ".json");

        DynamicDataPack.addData(jsonPath, data.serialize());

        return ResourceKey.create(Registries.DAMAGE_TYPE, location);
    }

    public static DamageSource create(Level level, ResourceKey<DamageType> key) {
        return create(level, key, null, null);
    }

    public static DamageSource create(Level level, ResourceKey<DamageType> key, @Nullable Entity causingEntity) {
        return create(level, key, causingEntity, causingEntity);
    }

    public static DamageSource create(Level level, ResourceKey<DamageType> key, @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        var registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        Optional<Holder.Reference<DamageType>> holderOpt = registry.getHolder(key);

        if (holderOpt.isEmpty()) {
            System.err.println("[ASLib] WARNING: DamageType '" + key.location() + "' is not loaded in world registry! Using GENERIC fallback.");
            Holder<DamageType> fallback = registry.getHolderOrThrow(DamageTypes.GENERIC);
            return new DamageSource(fallback, directEntity, causingEntity);
        }

        return new DamageSource(holderOpt.get(), directEntity, causingEntity);
    }

    public static DamageSource create(Level level, ResourceKey<DamageType> key, Vec3 pos) {
        var registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        Optional<Holder.Reference<DamageType>> holderOpt = registry.getHolder(key);

        if (holderOpt.isEmpty()) {
            System.err.println("[ASLib] WARNING: DamageType '" + key.location() + "' is not loaded in world registry! Using GENERIC fallback.");
            Holder<DamageType> fallback = registry.getHolderOrThrow(DamageTypes.GENERIC);
            return new DamageSource(fallback, pos);
        }

        return new DamageSource(holderOpt.get(), pos);
    }
}