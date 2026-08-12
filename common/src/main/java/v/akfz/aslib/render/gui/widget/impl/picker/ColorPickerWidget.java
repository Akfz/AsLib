package v.akfz.aslib.render.gui.widget.impl.picker;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import v.akfz.aslib.render.color.Color;
import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.render.gui.widget.api.AbstractWidget;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.impl.render.DefaultColorPickerRenderer;

import java.util.function.Consumer;

public class ColorPickerWidget extends AbstractWidget {
    private static final RenderPart DEFAULT_RENDERER = new DefaultColorPickerRenderer();

    private Color selectedColor;
    private final Consumer<Color> onColorChange;

    private boolean showLabels = true;
    private boolean showPreview = true;
    private boolean showAlpha = true;
    private boolean showHSB = true;
    private boolean showRGB = true;

    private int bgColor = ColorUtils.rgbToArgb(180, 40, 40, 40);
    private int borderColor = ColorUtils.rgbToArgb(255, 200, 200, 200);
    private int sliderBgColor = ColorUtils.rgbToArgb(255, 20, 20, 20);
    private int textColor = ColorUtils.white();

    private float h, s, b;
    private int draggingMode = -1;

    public ColorPickerWidget(int x, int y, int width, int height, Color initialColor, Consumer<Color> onColorChange) {
        super(x, y, width, height);
        this.selectedColor = initialColor != null ? initialColor : new Color(1.0, 1.0, 1.0, 1.0);
        this.onColorChange = onColorChange;
        this.mainRenderer = DEFAULT_RENDERER;
        updateHSBValues();
    }

    private void updateHSBValues() {
        float[] hsb = ColorUtils.getHSB(selectedColor);
        this.h = hsb[0];
        this.s = hsb[1];
        this.b = hsb[2];
    }

    public PickerLayout calculateLayout() {
        int p = 5;
        int availW = Math.max(10, width - p * 2);
        int availH = Math.max(10, height - p * 2);

        int rgbW = 0;
        int hsbX = x + p;

        if (showRGB && showHSB) {
            rgbW = (int) (availW * 0.52f);
            hsbX = x + p + rgbW + p;
        } else if (showRGB) {
            rgbW = availW;
        }

        int previewH = showPreview ? Math.max(10, Math.min(22, (int) (availH * 0.18f))) : 0;
        int channels = showAlpha ? 4 : 3;
        int remainingRgbH = availH - previewH - (showPreview ? p : 0);

        int channelStepH = Math.max(8, remainingRgbH / channels);
        int labelH = showLabels ? Math.min(10, Math.max(7, (int) (channelStepH * 0.4f))) : 0;
        int barH = Math.max(4, channelStepH - labelH - 2);

        int hueW = Math.max(8, Math.min(14, (int) (availW * 0.08f)));
        int hsbAvailW = width - (hsbX - x) - p;
        int hsbSquareSize = Math.max(10, Math.min(hsbAvailW - hueW - p, availH));
        int hueX = hsbX + hsbSquareSize + p;

        return new PickerLayout(
                p,
                x + p, y + p, rgbW, availH,
                x + p, y + p, rgbW, previewH,
                y + p + previewH + (showPreview ? p : 0),
                channelStepH, labelH, barH,
                hsbX, y + p, hsbSquareSize,
                hueX, y + p, hueW, hsbSquareSize
        );
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (mainRenderer == null) {
            mainRenderer = DEFAULT_RENDERER;
        }

        PickerLayout layout = calculateLayout();

        RenderExtras extras = new RenderExtras()
                .with("selectedColor", Color.class, selectedColor)
                .with("bgColor", Integer.class, bgColor)
                .with("borderColor", Integer.class, borderColor)
                .with("sliderBgColor", Integer.class, sliderBgColor)
                .with("textColor", Integer.class, textColor)
                .with("showLabels", Boolean.class, showLabels)
                .with("showPreview", Boolean.class, showPreview)
                .with("showAlpha", Boolean.class, showAlpha)
                .with("showHSB", Boolean.class, showHSB)
                .with("showRGB", Boolean.class, showRGB)
                .with("h", Float.class, h)
                .with("s", Float.class, s)
                .with("b", Float.class, b)
                .with("pickerLayout", PickerLayout.class, layout);

        mainRenderer.render(graphics, mouseX, mouseY, delta, this.x, this.y, this.width, this.height, extras);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible || button != 0 || !isMouseOver(mx, my)) return false;

        PickerLayout layout = calculateLayout();

        if (showRGB && mx >= layout.rgbX && mx <= layout.rgbX + layout.rgbWidth) {
            int channels = showAlpha ? 4 : 3;
            int startY = layout.channelStartY;

            for (int i = 0; i < channels; i++) {
                int barY = startY + (i * layout.channelStepH) + layout.labelH;
                if (my >= barY - 2 && my <= barY + layout.barH + 2) {
                    draggingMode = i;
                    handleDragUpdate(mx, my, layout);
                    return true;
                }
            }
        }

        if (showHSB) {
            if (mx >= layout.hsbX && mx <= layout.hsbX + layout.hsbSquareSize &&
                    my >= layout.hsbY && my <= layout.hsbY + layout.hsbSquareSize) {
                draggingMode = 4;
                handleDragUpdate(mx, my, layout);
                return true;
            }

            if (mx >= layout.hueX && mx <= layout.hueX + layout.hueWidth &&
                    my >= layout.hueY && my <= layout.hueY + layout.hueSquareSize) {
                draggingMode = 5;
                handleDragUpdate(mx, my, layout);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingMode != -1 && button == 0) {
            PickerLayout layout = calculateLayout();
            handleDragUpdate(mx, my, layout);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    private void handleDragUpdate(double mx, double my, PickerLayout layout) {
        if (draggingMode <= 3) {
            float rel = Mth.clamp((float) (mx - layout.rgbX) / Math.max(1, layout.rgbWidth), 0f, 1f);
            applyChannelChange(draggingMode, (int) (rel * 255));
        } else if (draggingMode == 4) {
            s = Mth.clamp((float) (mx - layout.hsbX) / Math.max(1, layout.hsbSquareSize), 0f, 1f);
            b = Mth.clamp(1f - (float) (my - layout.hsbY) / Math.max(1, layout.hsbSquareSize), 0f, 1f);
            updateFromHSB();
        } else if (draggingMode == 5) {
            h = Mth.clamp((float) (my - layout.hueY) / Math.max(1, layout.hueSquareSize), 0f, 1f);
            updateFromHSB();
        }
    }

    private void applyChannelChange(int id, int val) {
        float fVal = val / 255f;

        double r = selectedColor.getRed();
        double g = selectedColor.getGreen();
        double b = selectedColor.getBlue();
        double a = selectedColor.getAlpha();

        if (id == 0) r = fVal;
        else if (id == 1) g = fVal;
        else if (id == 2) b = fVal;
        else if (id == 3) a = fVal;

        this.selectedColor = new Color(r, g, b, a);
        updateHSBValues();

        if (onColorChange != null) onColorChange.accept(selectedColor);
    }

    private void updateFromHSB() {
        Color hsbColor = ColorUtils.hsbToRgb(h, s, b);

        this.selectedColor = new Color(
                hsbColor.getRed(),
                hsbColor.getGreen(),
                hsbColor.getBlue(),
                selectedColor.getAlpha()
        );

        if (onColorChange != null) onColorChange.accept(selectedColor);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingMode = -1;
        return true;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public Color getSelectedColor() { return this.selectedColor; }
    public void setSelectedColor(Color selectedColor) { this.selectedColor = selectedColor; updateHSBValues(); }
    public boolean isShowLabels() { return showLabels; }
    public ColorPickerWidget setShowLabels(boolean showLabels) { this.showLabels = showLabels; return this; }
    public boolean isShowPreview() { return showPreview; }
    public ColorPickerWidget setShowPreview(boolean showPreview) { this.showPreview = showPreview; return this; }
    public boolean isShowAlpha() { return showAlpha; }
    public ColorPickerWidget setShowAlpha(boolean showAlpha) { this.showAlpha = showAlpha; return this; }
    public boolean isShowHSB() { return showHSB; }
    public ColorPickerWidget setShowHSB(boolean showHSB) { this.showHSB = showHSB; return this; }
    public boolean isShowRGB() { return showRGB; }
    public ColorPickerWidget setShowRGB(boolean showRGB) { this.showRGB = showRGB; return this; }
    public int getBgColor() { return bgColor; }
    public ColorPickerWidget setBgColor(int bgColor) { this.bgColor = bgColor; return this; }
    public int getBorderColor() { return borderColor; }
    public ColorPickerWidget setBorderColor(int borderColor) { this.borderColor = borderColor; return this; }
    public int getSliderBgColor() { return sliderBgColor; }
    public ColorPickerWidget setSliderBgColor(int sliderBgColor) { this.sliderBgColor = sliderBgColor; return this; }
    public int getTextColor() { return textColor; }
    public ColorPickerWidget setTextColor(int textColor) { this.textColor = textColor; return this; }

    public record PickerLayout(
            int padding,
            int rgbX, int rgbY, int rgbWidth, int rgbHeight,
            int previewX, int previewY, int previewWidth, int previewHeight,
            int channelStartY, int channelStepH, int labelH, int barH,
            int hsbX, int hsbY, int hsbSquareSize,
            int hueX, int hueY, int hueWidth, int hueSquareSize
    ) {}
}