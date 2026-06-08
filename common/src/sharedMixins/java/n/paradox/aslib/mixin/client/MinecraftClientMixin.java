package n.paradox.aslib.mixin.client;

import n.paradox.aslib.AsLib;
import n.paradox.aslib.event.impl.client.ClientFirstTickEvent;
import n.paradox.aslib.resourcepack.AsLibResourceReloader;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.InactiveProfiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Unique
    private static final AsLibResourceReloader ASLIB_RELOADER = new AsLibResourceReloader();

    @Inject(
            method = "Lnet/minecraft/client/Minecraft;reloadResourcePacks(Z)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V", shift = At.Shift.BEFORE)
    )
    private void aslib$reloadResources(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        Minecraft client = Minecraft.getInstance();

        ASLIB_RELOADER.reload(
                CompletableFuture::completedFuture,
                client.getResourceManager(),
                InactiveProfiler.INSTANCE,
                InactiveProfiler.INSTANCE,
                Util.ioPool(),
                client
        );
    }

    @Unique
    private boolean isStarted = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void aslib$onTickEnd(CallbackInfo ci) {
        if (!isStarted) {
            AsLib.EVENT_BUS.post(new ClientFirstTickEvent());
            isStarted = true;
        }
    }
}