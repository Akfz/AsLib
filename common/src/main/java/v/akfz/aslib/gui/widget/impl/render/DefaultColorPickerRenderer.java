package v.akfz.aslib.gui.widget.impl.render;

import v.akfz.aslib.render.color.Color;
import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.gui.widget.api.render.RenderPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class DefaultColorPickerRenderer implements RenderPart {

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height, RenderExtras extras) {
        Color selectedColor = extras.get("selectedColor", Color.class);
        int bgColor = extras.getOrDefault("bgColor", Integer.class, 0xFF282828);
        int borderColor = extras.getOrDefault("borderColor", Integer.class, 0xFFCCCCCC);
        int sliderBgColor = extras.getOrDefault("sliderBgColor", Integer.class, 0xFF141414);
        int textColor = extras.getOrDefault("textColor", Integer.class, 0xFFFFFFFF);

        boolean showLabels = extras.getOrDefault("showLabels", Boolean.class, true);
        boolean showPreview = extras.getOrDefault("showPreview", Boolean.class, true);
        boolean showAlpha = extras.getOrDefault("showAlpha", Boolean.class, true);
        boolean showHSB = extras.getOrDefault("showHSB", Boolean.class, true);
        boolean showRGB = extras.getOrDefault("showRGB", Boolean.class, true);

        float h = extras.getOrDefault("h", Float.class, 0f);
        float s = extras.getOrDefault("s", Float.class, 0f);
        float b = extras.getOrDefault("b", Float.class, 0f);

        graphics.fill(x, y, x + width, y + height, bgColor);
        graphics.renderOutline(x, y, width, height, borderColor);

        int p = 5;
        int uw = width - (p * 2);
        int rw = (showHSB && showRGB) ? (int)(uw * 0.50f) : (showRGB ? uw : 0);
        int hx = x + p + (showRGB ? rw + p : 0);

        if (showRGB) {
            renderRGBSect(graphics, selectedColor, sliderBgColor, textColor, showPreview, showLabels, showAlpha, x + p, y + p, rw);
        }
        if (showHSB) {
            int availableW = width - (hx - x) - p;
            renderHSBSect(graphics, h, s, b, hx, y + p, availableW, height - (p * 2));
        }
    }

    private void renderRGBSect(GuiGraphics graphics, Color selectedColor, int sliderBgColor, int textColor, boolean showPreview, boolean showLabels, boolean showAlpha, int sx, int sy, int w) {
        int cy = sy;
        if (showPreview) {
            renderPreview(graphics, selectedColor, sx, cy, w);
            cy += 20;
        }

        int sh = 10;
        renderChannel(graphics, "R", ColorUtils.getIntRed(selectedColor), sx, cy, w, sh, ColorUtils.rgbToArgb(255, 0, 0), sliderBgColor, textColor, showLabels);
        cy += sh + paddingY(showLabels);
        renderChannel(graphics, "G", ColorUtils.getIntGreen(selectedColor), sx, cy, w, sh, ColorUtils.rgbToArgb(0, 255, 0), sliderBgColor, textColor, showLabels);
        cy += sh + paddingY(showLabels);
        renderChannel(graphics, "B", ColorUtils.getIntBlue(selectedColor), sx, cy, w, sh, ColorUtils.rgbToArgb(0, 0, 255), sliderBgColor, textColor, showLabels);

        if (showAlpha) {
            cy += sh + paddingY(showLabels);
            renderChannel(graphics, "A", ColorUtils.getIntAlpha(selectedColor), sx, cy, w, sh, ColorUtils.white(), sliderBgColor, textColor, showLabels);
        }
    }

    private void renderHSBSect(GuiGraphics graphics, float h, float s, float b, int sx, int sy, int w, int hSize) {
        int hw = 12, g = 5;
        int size = Math.max(1, Math.min(w - hw - g, hSize));

        Color white = ColorUtils.argbToColor(ColorUtils.white());
        Color pure = ColorUtils.hsbToRgb(h, 1f, 1f);
        int black = ColorUtils.rgbToArgb(0, 0, 0);

        for (int i = 0; i < size; i++) {
            float ratio = (float) i / (size - 1);
            int top = ColorUtils.toArgb(ColorUtils.lerp(white, pure, ratio));
            graphics.fillGradient(sx + i, sy, sx + i + 1, sy + size, top, black);
        }

        int mx = sx + (int)(s * (size - 1));
        int my = sy + (int)((1f - b) * (size - 1));
        graphics.renderOutline(mx - 2, my - 2, 5, 5, black);
        graphics.renderOutline(mx - 1, my - 1, 3, 3, ColorUtils.white());

        int hsliderX = sx + size + g;
        renderHueSlider(graphics, hsliderX, sy, hw, size);
        int hy = sy + Mth.clamp((int)(h * (size - 1)), 0, size - 1);
        graphics.fill(hsliderX - 1, hy - 1, hsliderX + hw + 1, hy + 1, ColorUtils.white());
    }

    private void renderHueSlider(GuiGraphics graphics, int x, int y, int w, int h) {
        int[] colors = {
                ColorUtils.rgbToArgb(255, 0, 0), ColorUtils.rgbToArgb(255, 255, 0),
                ColorUtils.rgbToArgb(0, 255, 0), ColorUtils.rgbToArgb(0, 255, 255),
                ColorUtils.rgbToArgb(0, 0, 255), ColorUtils.rgbToArgb(255, 0, 255),
                ColorUtils.rgbToArgb(255, 0, 0)
        };
        float step = (float) h / (colors.length - 1);
        for (int i = 0; i < colors.length - 1; i++) {
            graphics.fillGradient(x, (int)(y + i * step), x + w, (int)(y + (i + 1) * step), colors[i], colors[i + 1]);
        }
    }

    private void renderChannel(GuiGraphics graphics, String label, int val, int x, int y, int w, int h, int color, int sliderBgColor, int textColor, boolean showLabels) {
        Font font = Minecraft.getInstance().font;
        if (showLabels) {
            graphics.drawString(font, Component.literal(label + ": " + val), x, y, textColor, false);
            y += 10;
        }
        graphics.fill(x, y, x + w, y + h, sliderBgColor);
        int fw = (int) (w * (Mth.clamp(val, 0, 255) / 255f));
        graphics.fill(x, y, x + fw, y + h, color);
        graphics.fill(x + fw - 1, y - 1, x + fw + 1, y + h + 1, ColorUtils.white());
    }

    private void renderPreview(GuiGraphics graphics, Color selectedColor, int x, int y, int w) {
        int c1 = ColorUtils.white();
        int c2 = ColorUtils.rgbToArgb(200, 200, 200);
        for (int i = 0; i < w; i += 4) {
            for (int j = 0; j < 15; j += 4) {
                int c = ((i / 4 + j / 4) % 2 == 0) ? c1 : c2;
                graphics.fill(x + i, y + j, x + Math.min(i + 4, w), y + Math.min(j + 4, 15), c);
            }
        }
        graphics.fill(x, y, x + w, y + 15, ColorUtils.toArgb(selectedColor));
        graphics.renderOutline(x, y, w, 15, ColorUtils.white());
    }

    private int paddingY(boolean showLabels) { return 5 + (showLabels ? 10 : 0); }
}