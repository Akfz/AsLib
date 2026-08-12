package v.akfz.aslib.render.gui.widget.impl.button;

import v.akfz.aslib.render.gui.widget.api.AbstractWidget;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.impl.render.DefaultButtonRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.BiConsumer;

public class ButtonWidget extends AbstractWidget {
    private static final RenderPart DEFAULT_RENDERER = new DefaultButtonRenderer();

    protected boolean enabled = true;
    protected boolean pressed = false;
    protected String text;
    private BiConsumer<ButtonWidget, Mouse> onClick;
    private BiConsumer<ButtonWidget, Mouse> onRelease;

    public ButtonWidget(int x, int y, int width, int height, String text) {
        super(x, y, width, height);
        this.text = text;
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (mainRenderer == null) {
            mainRenderer = DEFAULT_RENDERER;
        }

        RenderExtras extras = new RenderExtras()
                .with("text", String.class, text)
                .with("enabled", Boolean.class, enabled)
                .with("pressed", Boolean.class, pressed)
                .with("hovered", Boolean.class, hovered)
                .with("focused", Boolean.class, focused);

        mainRenderer.render(graphics, mouseX, mouseY, delta, x, y, width, height, extras);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !enabled)
            return false;

        if (isMouseOver(mouseX, mouseY)) {
            Mouse mouseButton = convertButton(button);
            if (mouseButton != null) {
                pressed = true;
                if (onClick != null) {
                    onClick.accept(this, mouseButton);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pressed) {
            pressed = false;
            Mouse mouseButton = convertButton(button);
            if (mouseButton != null && onRelease != null) {
                onRelease.accept(this, mouseButton);
            }
            return true;
        }
        return false;
    }

    private Mouse convertButton(int button) {
        return switch (button) {
            case 0 -> Mouse.left;
            case 1 -> Mouse.right;
            case 2 -> Mouse.middle;
            default -> null;
        };
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isPressed() {
        return this.pressed;
    }

    public ButtonWidget setReleaseFunc(BiConsumer<ButtonWidget, Mouse> onRelease) {
        this.onRelease = onRelease;
        return this;
    }

    public ButtonWidget setClickFunc(BiConsumer<ButtonWidget, Mouse> onClick) {
        this.onClick = onClick;
        return this;
    }

    public enum Mouse {
        left,
        right,
        middle
    }
}