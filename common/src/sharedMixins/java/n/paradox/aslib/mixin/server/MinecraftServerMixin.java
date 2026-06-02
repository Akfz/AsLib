package n.paradox.aslib.mixin.server;

import n.paradox.aslib.AsLib;
import n.paradox.aslib.event.impl.server.ServerFirstTickEvent;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DedicatedServer.class)
public class MinecraftServerMixin {
    @Unique
    private boolean isStarted = false;

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void aslib$onTickStart(CallbackInfo ci) {

    }

    @Inject(method = "tickChildren", at = @At("TAIL"))
    private void aslib$onTickEnd(CallbackInfo ci) {
        if (!isStarted) {
            AsLib.EVENT_BUS.post(new ServerFirstTickEvent());
            isStarted = true;
        }
    }
}
