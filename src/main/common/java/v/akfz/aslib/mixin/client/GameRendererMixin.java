package v.akfz.aslib.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import v.akfz.aslib.render.camera.CameraExtender;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Final @Shadow
    private Camera mainCamera;

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V",
                    shift = At.Shift.AFTER,
                    ordinal = 3
            )
    )
    public void renderLevelMixin(float partialTicks, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        poseStack.mulPose(Axis.ZP.rotationDegrees(-(float) ((CameraExtender) mainCamera).getRot().z));
    }
}