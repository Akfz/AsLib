package v.akfz.aslib.util.math;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class Math3D {
    // точка пересечения или ближайшая точка на луче к целевой точке в 3D пространстве.
    public static Vec3 getClosestPointOnRay(Vec3 rayOrigin, Vec3 rayDirection, Vec3 targetPoint) {
        Vec3 toTarget = targetPoint.subtract(rayOrigin);
        double t = toTarget.dot(rayDirection);
        if (t < 0.0) {
            return rayOrigin;
        }
        return rayOrigin.add(rayDirection.scale(t));
    }

    // пересечение луча со сферой
    public static double intersectRaySphere(Vec3 rayOrigin, Vec3 rayDir, Vec3 sphereCenter, double sphereRadius) {
        Vec3 oc = rayOrigin.subtract(sphereCenter);
        double b = oc.dot(rayDir);
        double c = oc.dot(oc) - (sphereRadius * sphereRadius);
        double h = b * b - c;
        if (h < 0.0) return -1.0;
        h = Math.sqrt(h);
        double t0 = -b - h;
        if (t0 >= 0.0) return t0;
        double t1 = -b + h;
        if (t1 >= 0.0) return t1;
        return -1.0;
    }

    // пересечение луча с AABB
    public static double intersectRayAABB(Vec3 rayOrigin, Vec3 rayDir, AABB aabb) {
        double tmin = Double.NEGATIVE_INFINITY;
        double tmax = Double.POSITIVE_INFINITY;

        if (Math.abs(rayDir.x) > 1e-6) {
            double tx1 = (aabb.minX - rayOrigin.x) / rayDir.x;
            double tx2 = (aabb.maxX - rayOrigin.x) / rayDir.x;
            tmin = Math.max(tmin, Math.min(tx1, tx2));
            tmax = Math.min(tmax, Math.max(tx1, tx2));
        } else if (rayOrigin.x < aabb.minX || rayOrigin.x > aabb.maxX) {
            return -1.0;
        }

        if (Math.abs(rayDir.y) > 1e-6) {
            double ty1 = (aabb.minY - rayOrigin.y) / rayDir.y;
            double ty2 = (aabb.maxY - rayOrigin.y) / rayDir.y;
            tmin = Math.max(tmin, Math.min(ty1, ty2));
            tmax = Math.min(tmax, Math.max(ty1, ty2));
        } else if (rayOrigin.y < aabb.minY || rayOrigin.y > aabb.maxY) {
            return -1.0;
        }

        if (Math.abs(rayDir.z) > 1e-6) {
            double tz1 = (aabb.minZ - rayOrigin.z) / rayDir.z;
            double tz2 = (aabb.maxZ - rayOrigin.z) / rayDir.z;
            tmin = Math.max(tmin, Math.min(tz1, tz2));
            tmax = Math.min(tmax, Math.max(tz1, tz2));
        } else if (rayOrigin.z < aabb.minZ || rayOrigin.z > aabb.maxZ) {
            return -1.0;
        }

        if (tmax >= tmin && tmax >= 0.0) {
            return Math.max(tmin, 0.0);
        }
        return -1.0;
    }

    // луч с экрана
    public static Vec3 getRayFromScreen(double mouseX, double mouseY, int screenWidth, int screenHeight, Matrix4f invProj, Matrix4f invView) {
        float ndcX = (float) ((2.0 * mouseX) / screenWidth - 1.0);
        float ndcY = (float) (1.0 - (2.0 * mouseY) / screenHeight);

        Vector4f rayClip = new Vector4f(ndcX, ndcY, -1.0f, 1.0f);

        Vector4f rayEye = invProj.transform(rayClip, new Vector4f());
        rayEye.z = -1.0f;
        rayEye.w = 0.0f;

        Vector4f rayWorld = invView.transform(rayEye, new Vector4f());

        return new Vec3(rayWorld.x(), rayWorld.y(), rayWorld.z()).normalize();
    }

    @Nullable // луч с мира в экран
    public static Vector3f worldToScreen(Vec3 worldPos, Vec3 cameraPos, Matrix4f viewProjMatrix, int screenWidth, int screenHeight) {
        Vec3 relativePos = worldPos.subtract(cameraPos);
        Vector4f clipSpace = new Vector4f((float) relativePos.x, (float) relativePos.y, (float) relativePos.z, 1.0f);

        viewProjMatrix.transform(clipSpace);

        if (clipSpace.w() <= 0.0f) {
            return null;
        }

        float ndcX = clipSpace.x() / clipSpace.w();
        float ndcY = clipSpace.y() / clipSpace.w();
        float ndcZ = clipSpace.z() / clipSpace.w();

        float screenX = ((ndcX + 1.0f) / 2.0f) * screenWidth;
        float screenY = ((1.0f - ndcY) / 2.0f) * screenHeight;

        return new Vector3f(screenX, screenY, ndcZ);
    }

    // yaw pitch в vec3
    public static Vec3 getDirectionFromRotation(float yaw, float pitch) {
        float radPitch = pitch * ((float) Math.PI / 180.0F);
        float radYaw = -yaw * ((float) Math.PI / 180.0F);
        float cosYaw = (float) Math.cos(radYaw);
        float sinYaw = (float) Math.sin(radYaw);
        float cosPitch = (float) Math.cos(radPitch);
        float sinPitch = (float) Math.sin(radPitch);
        return new Vec3(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
    }
}