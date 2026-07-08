package v.akfz.aslib.gui.widget.impl.picker;

import v.akfz.aslib.render.color.Color;
import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.gui.widget.api.AbstractWidget;
import v.akfz.aslib.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.gui.widget.api.render.RenderPart;
import v.akfz.aslib.gui.widget.impl.render.DefaultColorPickerRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

//не работает в scrollwidget
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
        this.selectedColor = initialColor;
        this.onColorChange = onColorChange;
        this.mainRenderer = DEFAULT_RENDERER;
        updateHSBValues();
    }

    private void updateHSBValues() {
        float[] hsb = ColorUtils.getHSB(selectedColor);
        this.h = hsb[0]; this.s = hsb[1]; this.b = hsb[2];
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (mainRenderer == null) {
            mainRenderer = DEFAULT_RENDERER;
        }

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
                .with("b", Float.class, b);

        mainRenderer.render(graphics, mouseX, mouseY, delta, this.x, this.y, this.width, this.height, extras);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible) return false;

        double localX = mx - this.x;
        double localY = my - this.y;

        if (localX < 0 || localX > this.width || localY < 0 || localY > this.height) {
            return false;
        }

        int p = 5;
        int uw = this.width - p * 2;
        int rw = (showHSB && showRGB) ? (int)(uw * 0.55f) : (showRGB ? uw : 0);

        if (showRGB) {
            int cy = p + (showPreview ? 20 : 0);
            int channels = showAlpha ? 4 : 3;

            for (int i = 0; i < channels; i++) {
                int hy = cy + (showLabels ? 10 : 0);
                if (localX >= p && localX <= p + rw && localY >= hy && localY <= hy + 10) {
                    draggingMode = i;
                    handleDragUpdate(localX, localY);
                    return true;
                }
                cy += 10 + paddingY();
            }
        }

        if (showHSB) {
            int hx = p + (showRGB ? rw + p : 0);
            int size = Math.max(1, Math.min(this.width - hx - p - 17, this.height - (p * 2)));

            if (localX >= hx && localX <= hx + size && localY >= p && localY <= p + size) {
                draggingMode = 4;
                handleDragUpdate(localX, localY);
                return true;
            }

            if (localX >= hx + size + 5 && localX <= hx + size + 17 && localY >= p && localY <= p + size) {
                draggingMode = 5;
                handleDragUpdate(localX, localY);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingMode != -1) {
            double localX = mx - this.x;
            double localY = my - this.y;
            handleDragUpdate(localX, localY);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    private void handleDragUpdate(double localX, double localY) {
        int p = 5;
        int uw = this.width - p * 2;
        int rw = (showHSB && showRGB) ? (int)(uw * 0.55f) : (showRGB ? uw : 0);

        if (draggingMode <= 3) {
            float rel = Mth.clamp((float) (localX - p) / rw, 0f, 1f);
            applyChannelChange(draggingMode, (int)(rel * 255));
        } else {
            int hx = p + (showRGB ? rw + p : 0);
            int size = Math.max(1, Math.min(this.width - hx - p - 17, this.height - (p * 2)));

            if (draggingMode == 4) {
                s = Mth.clamp((float)(localX - hx) / size, 0f, 1f);
                b = Mth.clamp(1f - (float)(localY - p) / size, 0f, 1f);
            } else if (draggingMode == 5) {
                h = Mth.clamp((float)(localY - p) / size, 0f, 1f);
            }
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

    private int paddingY() { return 5 + (showLabels ? 10 : 0); }

    @Override
    public boolean mouseReleased(double mx, double my, int b) {
        draggingMode = -1;
        return true;
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
}