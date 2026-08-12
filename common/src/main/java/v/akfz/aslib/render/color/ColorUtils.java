package v.akfz.aslib.render.color;

import net.minecraft.util.Mth;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class ColorUtils {

    // в формат 0xAARRGGBB
    public static int toArgb(Color c) {
        return rgbToArgb(getIntAlpha(c), getIntRed(c), getIntGreen(c), getIntBlue(c));
    }

    public static int getHSBColor(float h, float s, float b) {
        return toArgb(hsbToRgb(h, s, b));
    }

    public static int white() {
        return 0xFFFFFFFF;
    }

    public static Color of(Object r, Object g, Object b) {
        return new Color(r, g, b);
    }

    public static int rgbToArgb(int alpha, int red, int green, int blue) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static int rgbToArgb(int red, int green, int blue) {
        return rgbToArgb(255, red, green, blue);
    }

    public static int rgbToArgb(int main) {
        return rgbToArgb(255, main, main, main);
    }

    public static int rgbToArgb(int main, int alpha) {
        return rgbToArgb(alpha, main, main, main);
    }

    public static Color argbToColor(int argb) {
        int alpha = (argb >> 24) & 0xFF;
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        return new Color(red, green, blue, alpha);
    }

    public static int getIntRed(Color c) { return (int) Math.round(c.getRed() * 255.0); }
    public static int getIntGreen(Color c) { return (int) Math.round(c.getGreen() * 255.0); }
    public static int getIntBlue(Color c) { return (int) Math.round(c.getBlue() * 255.0); }
    public static int getIntAlpha(Color c) { return (int) Math.round(c.getAlpha() * 255.0); }

    public static boolean isLowerMid(Color color, int mid) {
        double sum = color.getRed() + color.getGreen() + color.getBlue();
        return (sum * 255.0) < mid * 3;
    }

    public static boolean isBiggerMid(Color color, int mid) {
        double sum = color.getRed() + color.getGreen() + color.getBlue();
        return (sum * 255.0) > mid * 3;
    }

    public static Color invertColor(Color color) {
        return new Color(1.0 - color.getRed(), 1.0 - color.getGreen(), 1.0 - color.getBlue(), color.getAlpha());
    }

    public static Color correctAlpha(Color color, Object alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static Color darkerColor(Color color, int amount) {
        double offset = amount / 255.0;
        return new Color(
                Math.max(0, color.getRed() - offset),
                Math.max(0, color.getGreen() - offset),
                Math.max(0, color.getBlue() - offset),
                color.getAlpha()
        );
    }

    public static Color brighten(Color color, int amount) {
        double offset = amount / 255.0;
        return new Color(
                Math.min(1.0, color.getRed() + offset),
                Math.min(1.0, color.getGreen() + offset),
                Math.min(1.0, color.getBlue() + offset),
                color.getAlpha()
        );
    }
    public static int brighten(int argb, int amount) {
        Color c = argbToColor(argb);
        return toArgb(brighten(c, amount));
    }

    public static Color toGrayscale(Color color) {
        double gray = color.getRed() * 0.2126 + color.getGreen() * 0.7152 + color.getBlue() * 0.0722;
        return new Color(gray, gray, gray, color.getAlpha());
    }

    public static Color mixColors(Color color1, Color color2, float ratio) {
        ratio = Mth.clamp(ratio, 0f, 1f);
        return new Color(
                Mth.lerp(ratio, color1.getRed(), color2.getRed()),
                Mth.lerp(ratio, color1.getGreen(), color2.getGreen()),
                Mth.lerp(ratio, color1.getBlue(), color2.getBlue()),
                Mth.lerp(ratio, color1.getAlpha(), color2.getAlpha())
        );
    }

    public static Color mixColorsNoAlpha(Color color1, Color color2, float ratio) {
        ratio = Mth.clamp(ratio, 0f, 1f);
        return new Color(
                Mth.lerp(ratio, color1.getRed(), color2.getRed()),
                Mth.lerp(ratio, color1.getGreen(), color2.getGreen()),
                Mth.lerp(ratio, color1.getBlue(), color2.getBlue()),
                1.0
        );
    }

    public static Color lerp(Color c1, Color c2, float t) {
        float clampedT = Mth.clamp(t, 0.0f, 1.0f);

        return new Color(
                Mth.lerp(clampedT, c1.getRed(), c2.getRed()),
                Mth.lerp(clampedT, c1.getGreen(), c2.getGreen()),
                Mth.lerp(clampedT, c1.getBlue(), c2.getBlue()),
                Mth.lerp(clampedT, c1.getAlpha(), c2.getAlpha())
        );
    }

    public static Color randomColor() {
        Random rand = ThreadLocalRandom.current();
        return new Color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble(), 1.0);
    }

    public static Color fromHex(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.startsWith("0x")) hex = hex.substring(2);
        return argbToColor((int) Long.parseLong(hex, 16));
    }

    public static Color hsbToRgb(float hue, float saturation, float brightness) {
        int argb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
        return argbToColor(argb);
    }

    public static Color multiplyRGB(Color color, float multiplier) {
        return new Color(
                Mth.clamp(color.getRed() * multiplier, 0.0, 1.0),
                Mth.clamp(color.getGreen() * multiplier, 0.0, 1.0),
                Mth.clamp(color.getBlue() * multiplier, 0.0, 1.0),
                color.getAlpha()
        );
    }

    public static float[] getHSB(Color c) {
        return java.awt.Color.RGBtoHSB(getIntRed(c), getIntGreen(c), getIntBlue(c), null);
    }

    public static Color withHue(Color color, float hue) {
        float[] hsb = getHSB(color);
        Color rgbColor = hsbToRgb(hue, hsb[1], hsb[2]);
        return new Color(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue(), color.getAlpha());
    }

    public static Color shiftHue(Color color, float amount) {
        float[] hsb = getHSB(color);
        float newHue = (hsb[0] + amount) % 1.0f;
        if (newHue < 0) newHue += 1.0f;
        Color rgbColor = hsbToRgb(newHue, hsb[1], hsb[2]);
        return new Color(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue(), color.getAlpha());
    }
}
