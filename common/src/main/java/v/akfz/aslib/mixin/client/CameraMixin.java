package v.akfz.aslib.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import v.akfz.aslib.render.camera.CameraExtender;
import v.akfz.aslib.render.camera.CameraManager;
import v.akfz.aslib.util.math.MathFormulas;

@Mixin(Camera.class)
public class CameraMixin implements CameraExtender {
    @Unique private Vec3 specPos = Vec3.ZERO;
    @Unique private Vec3 specRot = Vec3.ZERO;
    @Unique private boolean customRotsOnned = true;

    @Unique private static long lastFrameTime = System.nanoTime();

    @Shadow protected void setPosition(double x, double y, double z) {}

    @Shadow private float eyeHeight;
    @Shadow private float eyeHeightOld;

    @Final @Shadow private Vector3f forwards;
    @Final @Shadow private Vector3f up;
    @Final @Shadow private Vector3f left;

    @Shadow private float xRot;
    @Shadow private float yRot;
    @Unique private float roll;
    @Final @Shadow private Quaternionf rotation;

    @Unique private double currentSmoothX, currentSmoothY, currentSmoothZ;
    @Unique private float currentSmoothRotX, currentSmoothRotY, currentSmoothRotZ;

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER))
    public void onSetup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null || entity == null) {
            lastFrameTime = System.nanoTime();
            return;
        }

        long now = System.nanoTime();
        double dtInSeconds = (now - lastFrameTime) / 1_000_000_000.0;
        lastFrameTime = now;

        if (dtInSeconds <= 0.0 || dtInSeconds > 0.1) {
            dtInSeconds = 0.016;
        }

        float dt = (float) (dtInSeconds / 0.05);
        boolean isTethered = specPos != null && specPos.lengthSqr() > 1e-4;

        if (isTethered) {
            double x = Mth.lerp((double) partialTick, entity.xo, entity.getX());
            double y = Mth.lerp((double) partialTick, entity.yo, entity.getY())
                    + Mth.lerp((double) partialTick, this.eyeHeightOld, this.eyeHeight);
            double z = Mth.lerp((double) partialTick, entity.zo, entity.getZ());

            currentSmoothX = MathFormulas.exponentialSmooth(currentSmoothX, specPos.x, CameraManager.posSpeed, dt);
            currentSmoothY = MathFormulas.exponentialSmooth(currentSmoothY, specPos.y, CameraManager.posSpeed, dt);
            currentSmoothZ = MathFormulas.exponentialSmooth(currentSmoothZ, specPos.z, CameraManager.posSpeed, dt);

            this.setPosition(x + currentSmoothX, y + currentSmoothY, z + currentSmoothZ);

            float basePitch = xRot;
            float baseYaw = yRot;

            boolean hasCustomRots = customRotsOnned && specRot != null && specRot.lengthSqr() > 1e-4;

            if (hasCustomRots) {
                currentSmoothRotX = (float) MathFormulas.exponentialSmooth(currentSmoothRotX, (float) specRot.x, CameraManager.rotSpeed, dt);
                currentSmoothRotY = (float) MathFormulas.exponentialSmooth(currentSmoothRotY, (float) specRot.y, CameraManager.rotSpeed, dt);
                currentSmoothRotZ = (float) MathFormulas.exponentialSmooth(currentSmoothRotZ, (float) specRot.z, CameraManager.rotSpeed, dt);

                this.xRot += currentSmoothRotX;
                this.yRot += currentSmoothRotY;
                this.roll = currentSmoothRotZ;

                this.yRot = Mth.wrapDegrees(this.yRot);
                this.xRot = Mth.clamp(this.xRot, -90.0f, 90.0f);
            } else {
                this.xRot = basePitch;
                this.yRot = baseYaw;
                this.roll = 0.0f;
            }

            this.rotation.rotationYXZ(
                    (float) Math.toRadians(-yRot),
                    (float) Math.toRadians(xRot),
                    (float) Math.toRadians(-roll)
            );
            this.forwards.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
            this.up.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
            this.left.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);

        } else if (customRotsOnned && specRot != null && specRot.lengthSqr() > 1e-4) {
            currentSmoothRotZ = (float) MathFormulas.exponentialSmooth(currentSmoothRotZ, (float) specRot.z, CameraManager.rotSpeed, dt);
            this.roll = currentSmoothRotZ;

            this.rotation.rotationYXZ(
                    (float) Math.toRadians(-yRot),
                    (float) Math.toRadians(xRot),
                    (float) Math.toRadians(-roll)
            );
            this.forwards.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
            this.up.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
            this.left.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
        }
    }

    @Override
    public void addPos(Vec3 pos) {
        this.specPos = pos;
    }

    @Override
    public void addRot(Vec3 rot) {
        this.specRot = rot;
    }

    @Override
    public Vec3 getRot() {
        return this.specRot;
    }

    @Override
    public void clearRots() {
        this.specRot = Vec3.ZERO;
    }

    @Override
    public void clearPos() {
        this.specPos = Vec3.ZERO;
    }

    @Override
    public void setCustomRotsOnned(boolean customRotsOnned) {
        this.customRotsOnned = customRotsOnned;
    }

    @Override
    public float getRoll() {
        return this.roll;
    }
}