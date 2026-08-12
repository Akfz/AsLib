package v.akfz.aslib.render.gui.widget.impl.button;

import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.impl.render.DefaultSliderRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class SliderWidget extends ButtonWidget {
    private static final RenderPart DEFAULT_SLIDER_RENDERER = new DefaultSliderRenderer();

    private double min, max;
    private double value;
    private double step;
    private boolean showValue = true;

    private final Consumer<Double> onChange;
    private Consumer<Double> onReleaseCallback;

    public SliderWidget(int x, int y, int width, int height, double min, double max, double initial,
                        String label, Consumer<Double> onChange) {
        super(x, y, width, height, label);
        this.min = min;
        this.max = max;
        this.value = Math.max(min, Math.min(max, initial));
        this.onChange = onChange;

        this.mainRenderer = DEFAULT_SLIDER_RENDERER;

        this.setClickFunc((btn, mouse) -> {
            if (mouse == Mouse.left) {
                setValueFromMouse(net.minecraft.client.Minecraft.getInstance().mouseHandler.xpos());
            }
        });

        this.setReleaseFunc((btn, mouse) -> {
            if (mouse == Mouse.left && onReleaseCallback != null) {
                onReleaseCallback.accept(value);
            }
        });
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (mainRenderer == null) {
            mainRenderer = DEFAULT_SLIDER_RENDERER;
        }

        double progress = (max - min == 0) ? 0 : (value - min) / (max - min);

        RenderExtras extras = new RenderExtras()
                .with("label", String.class, text)
                .with("value", Double.class, value)
                .with("progress", Double.class, progress)
                .with("showValue", Boolean.class, showValue)
                .with("dragging", Boolean.class, pressed)
                .with("hovered", Boolean.class, hovered)
                .with("focused", Boolean.class, focused)
                .with("enabled", Boolean.class, enabled);

        mainRenderer.render(graphics, mouseX, mouseY, delta, x, y, width, height, extras);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean clicked = super.mouseClicked(mouseX, mouseY, button);
        if (clicked && button == 0) {
            setValueFromMouse(mouseX);
        }
        return clicked;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (pressed && button == 0) {
            setValueFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!visible || !enabled || !isMouseOver(mouseX, mouseY)) return false;

        double currentStep = (step > 0) ? step : (max - min) * 0.01;
        setValue(value + amount * currentStep);

        if (onReleaseCallback != null) {
            onReleaseCallback.accept(value);
        }
        return true;
    }

    public void setValue(double val) {
        double old = value;
        value = Math.max(min, Math.min(max, val));
        if (step > 0) {
            value = Math.round(value / step) * step;
        }
        if (old != value && onChange != null) {
            onChange.accept(value);
        }
    }

    private void setValueFromMouse(double mouseX) {
        double rel = (mouseX - x - 4) / (width - 12);
        rel = Math.max(0, Math.min(1, rel));
        double newVal = min + rel * (max - min);
        setValue(newVal);
    }

    public double getMin() { return min; }
    public void setMin(double min) { this.min = min; setValue(value); }

    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; setValue(value); }

    public double getValue() { return value; }

    public double getStep() { return step; }
    public SliderWidget setStep(double step) { this.step = step; setValue(value); return this; }

    public String getLabel() { return text; }
    public void setLabel(String label) { this.text = label; }

    public boolean isShowValue() { return showValue; }
    public void setShowValue(boolean showValue) { this.showValue = showValue; }

    public boolean isDragging() { return pressed; }

    public Consumer<Double> getOnRelease() { return onReleaseCallback; }
    public void setOnRelease(Consumer<Double> onRelease) { this.onReleaseCallback = onRelease; }
}