package v.akfz.aslib.mixin.client;

import v.akfz.aslib.AsLib;
import v.akfz.aslib.event.impl.FirstTickEvent;
import v.akfz.aslib.event.impl.TickUpdater;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Unique
    private boolean isStarted = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void aslib$onTickEnd(CallbackInfo ci) {
        if (!isStarted) {
            AsLib.EVENT_BUS.post(new FirstTickEvent((Minecraft) (Object)this,null));
            isStarted = true;
        }
        AsLib.EVENT_BUS.post(new TickUpdater((Minecraft) (Object)this,null));
    }
}