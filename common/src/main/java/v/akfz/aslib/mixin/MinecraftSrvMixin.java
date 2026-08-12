package v.akfz.aslib.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import v.akfz.aslib.AsLib;
import v.akfz.aslib.event.impl.FirstTickEvent;
import v.akfz.aslib.event.impl.TickUpdater;
import v.akfz.aslib.world.MinecraftServerExtension;

import java.util.Map;

@Mixin(MinecraftServer.class)
public abstract class MinecraftSrvMixin implements MinecraftServerExtension {

    @Unique
    private boolean started = false;

    @Inject(method = "tickChildren", at = @At("TAIL"))
    private void aslib$onTickEnd(CallbackInfo ci) {
        if (!started) {
            AsLib.EVENT_BUS.post(new FirstTickEvent());
            started = true;
        }
        AsLib.EVENT_BUS.post(new TickUpdater(false));
    }

    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow @Final protected LevelStorageSource.LevelStorageAccess storageSource;

    @Override
    public Map<ResourceKey<Level>, ServerLevel> aslib$getLevels() {
        return this.levels;
    }

    @Override
    public LevelStorageSource.LevelStorageAccess aslib$getStorageSource() {
        return this.storageSource;
    }
}