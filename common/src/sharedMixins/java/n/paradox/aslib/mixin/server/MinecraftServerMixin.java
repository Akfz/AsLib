package n.paradox.aslib.mixin.server;

import n.paradox.aslib.AsLib;
import n.paradox.aslib.event.impl.client.ClientFirstTickEvent;
import n.paradox.aslib.event.impl.server.ServerFirstTickEvent;
import n.paradox.aslib.register.AsLibRegistries;
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

    @Inject(method = "tickChildren", at = @At("TAIL"))
    private void aslib$onTickEnd(CallbackInfo ci) {
        if (!isStarted) {
            AsLib.EVENT_BUS.post(new ClientFirstTickEvent(AsLibRegistries::Init));
            isStarted = true;
        }
    }
}
