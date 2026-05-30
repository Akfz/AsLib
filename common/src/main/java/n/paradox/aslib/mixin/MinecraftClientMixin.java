package n.paradox.aslib.mixin;

import n.paradox.aslib.resourcepack.AsLibResourceReloader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Unique private static final AsLibResourceReloader ASLIB_RELOADER = new AsLibResourceReloader();

    @Inject(
            method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V",shift = At.Shift.BEFORE)
    )
    private void aslib$reloadResources(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getResourcePackManager() == null || client.getResourceManager() == null) {
            return;
        }

        ASLIB_RELOADER.reload(
                CompletableFuture::completedFuture,
                client.getResourceManager(),
                net.minecraft.util.profiler.DummyProfiler.INSTANCE,
                net.minecraft.util.profiler.DummyProfiler.INSTANCE,
                Util.getMainWorkerExecutor(),
                client
        );
    }
}