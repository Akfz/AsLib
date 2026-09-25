package v.akfz.aslib.render.gui.widget.impl.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import v.akfz.aslib.render.color.Color;
import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.impl.picker.ColorPickerWidget.PickerLayout;

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

        PickerLayout layout = extras.get("pickerLayout", PickerLayout.class);
        if (layout == null) return;

        graphics.fill(x, y, x + width, y + height, bgColor);
        graphics.renderOutline(x, y, width, height, borderColor);

        if (showRGB) {
            renderRGBSect(graphics, selectedColor, layout, sliderBgColor, textColor, showPreview, showLabels, showAlpha);
        }
        if (showHSB) {
            renderHSBSect(graphics, h, s, b, layout);
        }
    }

    private void renderRGBSect(GuiGraphics graphics, Color selectedColor, PickerLayout layout, int sliderBgColor, int textColor, boolean showPreview, boolean showLabels, boolean showAlpha) {
        if (showPreview) {
            renderPreview(graphics, selectedColor, layout.previewX(), layout.previewY(), layout.previewWidth(), layout.previewHeight());
        }

        String[] labels = {"R", "G", "B", "A"};
        int[] values = {
                ColorUtils.getIntRed(selectedColor),
                ColorUtils.getIntGreen(selectedColor),
                ColorUtils.getIntBlue(selectedColor),
                ColorUtils.getIntAlpha(selectedColor)
        };
        int[] colors = {
                ColorUtils.rgbToArgb(255, 60, 60),
                ColorUtils.rgbToArgb(60, 255, 60),
                ColorUtils.rgbToArgb(60, 120, 255),
                ColorUtils.white()
        };

        int channels = showAlpha ? 4 : 3;
        for (int i = 0; i < channels; i++) {
            int cy = layout.channelStartY() + (i * layout.channelStepH());
            renderChannel(graphics, labels[i], values[i], layout.rgbX(), cy, layout.rgbWidth(), layout.barH(), layout.labelH(), colors[i], sliderBgColor, textColor, showLabels);
        }
    }

    private void renderHSBSect(GuiGraphics graphics, float h, float s, float b, PickerLayout layout) {
        int size = layout.hsbSquareSize();
        if (size <= 0) return;

        Color white = ColorUtils.argbToColor(ColorUtils.white());
        Color pure = ColorUtils.hsbToRgb(h, 1f, 1f);
        int black = ColorUtils.rgbToArgb(0, 0, 0);

        for (int i = 0; i < size; i++) {
            float ratio = (float) i / Math.max(1, size - 1);
            int top = ColorUtils.toArgb(ColorUtils.lerp(white, pure, ratio));
            graphics.fillGradient(layout.hsbX() + i, layout.hsbY(), layout.hsbX() + i + 1, layout.hsbY() + size, top, black);
        }

        int mx = layout.hsbX() + (int) (s * (size - 1));
        int my = layout.hsbY() + (int) ((1f - b) * (size - 1));
        graphics.renderOutline(mx - 2, my - 2, 5, 5, black);
        graphics.renderOutline(mx - 1, my - 1, 3, 3, ColorUtils.white());

        renderHueSlider(graphics, layout.hueX(), layout.hueY(), layout.hueWidth(), layout.hueSquareSize());
        int hy = layout.hueY() + Mth.clamp((int) (h * (size - 1)), 0, size - 1);
        graphics.fill(layout.hueX() - 1, hy - 1, layout.hueX() + layout.hueWidth() + 1, hy + 1, ColorUtils.white());
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
            graphics.fillGradient(x, (int) (y + i * step), x + w, (int) (y + (i + 1) * step), colors[i], colors[i + 1]);
        }
    }

    private void renderChannel(GuiGraphics graphics, String label, int val, int x, int y, int w, int barH, int labelH, int color, int sliderBgColor, int textColor, boolean showLabels) {
        Font font = Minecraft.getInstance().font;
        if (showLabels && labelH > 0) {
            graphics.drawString(font, Component.literal(label + ": " + val), x, y, textColor, false);
            y += labelH;
        }

        graphics.fill(x, y, x + w, y + barH, sliderBgColor);
        int fw = (int) (w * (Mth.clamp(val, 0, 255) / 255f));
        if (fw > 0) {
            graphics.fill(x, y, x + fw, y + barH, color);
        }
        graphics.fill(x + Math.max(0, fw - 1), y - 1, x + Math.min(w, fw + 1), y + barH + 1, ColorUtils.white());
    }

    private void renderPreview(GuiGraphics graphics, Color selectedColor, int x, int y, int w, int h) {
        if (w <= 0 || h <= 0) return;

        int c1 = ColorUtils.white();
        int c2 = ColorUtils.rgbToArgb(180, 180, 180);
        int checkSize = 4;

        for (int i = 0; i < w; i += checkSize) {
            for (int j = 0; j < h; j += checkSize) {
                int c = ((i / checkSize + j / checkSize) % 2 == 0) ? c1 : c2;
                graphics.fill(x + i, y + j, x + Math.min(i + checkSize, w), y + Math.min(j + checkSize, h), c);
            }
        }
        graphics.fill(x, y, x + w, y + h, ColorUtils.toArgb(selectedColor));
        graphics.renderOutline(x, y, w, h, ColorUtils.white());
    }
}