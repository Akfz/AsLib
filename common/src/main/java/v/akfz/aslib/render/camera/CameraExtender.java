package v.akfz.aslib.render.camera;

import net.minecraft.world.phys.Vec3;

public interface CameraExtender {
    void addPos(Vec3 pos);
    void addRot(Vec3 rot);
    Vec3 getRot();
    void clearRots();
    void clearPos();
    void setCustomRotsOnned(boolean customRotsOnned);
    float getRoll();
}
