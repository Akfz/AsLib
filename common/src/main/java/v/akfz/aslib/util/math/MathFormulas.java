package v.akfz.aslib.util.math;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public class MathFormulas {
    public static double exponentialSmooth(double current, double target, double rate, double deltaTime) {
        double alpha = 1.0 - Math.exp(-rate * deltaTime);
        return current + (target - current) * alpha;
    }

    public static float lerp(float start, float end, float t) {
        return start + t * (end - start);
    }

    public static Vec3 lerp(Vec3 start, Vec3 end, double t) {
        return new Vec3(
                Mth.lerp(t, start.x, end.x),
                Mth.lerp(t, start.y, end.y),
                Mth.lerp(t, start.z, end.z)
        );
    }

    public static float lerpAngle(float start, float end, float t) {
        return start + t * Mth.wrapDegrees(end - start);
    }

    public static float approach(float current, float target, float step) {
        float diff = target - current;
        if (Math.abs(diff) <= step) return target;
        return current + Math.signum(diff) * step;
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static double map(double val, double minOld, double maxOld, double minNew, double maxNew) {
        return minNew + (val - minOld) * (maxNew - minNew) / (maxOld - minOld);
    }

    public static double normalize(double val, double min, double max) {
        return (val - min) / (max - min);
    }

    public static float randomInRange(float min, float max) {
        return (float) ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static int randomInRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static float smoothStep(float t) {
        t = Mth.clamp(t, 0f, 1f);
        return t * t * (3 - 2 * t);
    }

    public static float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1.0f - Mth.clamp(t, 0.0f, 1.0f), 3);
    }

    public static float easeOutExpo(float t) {
        return (t == 1.0f) ? 1.0f : 1.0f - (float) Math.pow(2.0, -10.0 * t);
    }

    public static boolean epsilonEquals(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static double sqr(double val) {
        return val * val;
    }

    public static boolean isEven(int val) {
        return (val & 1) == 0;
    }

    public static float wave(float speed, float amplitude) {
        return Mth.sin((System.currentTimeMillis() % 100000) / 1000f * speed) * amplitude;
    }
}
