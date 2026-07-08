package v.akfz.aslib.mixin.server;

import v.akfz.aslib.AsLib;
import v.akfz.aslib.event.impl.ExecutionSideEvent;
import v.akfz.aslib.event.impl.FirstTickEvent;
import v.akfz.aslib.initializer.SideEnvironment;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServer.class)
public class MinecraftServerMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void aslib$start(CallbackInfo ci) {
        AsLib.EVENT_BUS.post(new ExecutionSideEvent());
        SideEnvironment.setAndFreeze(SideEnvironment.Side.Server);
    }

    @Unique
    private boolean isStarted = false;

    @Inject(method = "tickChildren", at = @At("TAIL"))
    private void aslib$onTickEnd(CallbackInfo ci) {
        if (!isStarted) {
            AsLib.EVENT_BUS.post(new FirstTickEvent());
            isStarted = true;
        }
    }
}
