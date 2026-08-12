package v.akfz.aslib.render.gui.widget.impl.button;

import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.impl.render.DefaultCheckboxRenderer;
import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public class CheckboxWidget extends ButtonWidget {
    private static final RenderPart DEFAULT_CHECKBOX_RENDERER = new DefaultCheckboxRenderer();

    private boolean checked = false;
    private Consumer<Boolean> onToggle;

    public CheckboxWidget(int x, int y, int width, int height, String text) {
        super(x, y, width, height, text);
        this.mainRenderer = DEFAULT_CHECKBOX_RENDERER;

        this.setClickFunc((btn, mouse) -> {
            if (mouse == Mouse.left) {
                setChecked(!this.checked);
            }
        });
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (mainRenderer == null) {
            mainRenderer = DEFAULT_CHECKBOX_RENDERER;
        }

        RenderExtras extras = new RenderExtras()
                .with("text", String.class, text)
                .with("checked", Boolean.class, checked)
                .with("enabled", Boolean.class, enabled)
                .with("pressed", Boolean.class, pressed)
                .with("hovered", Boolean.class, hovered)
                .with("focused", Boolean.class, focused);

        mainRenderer.render(graphics, mouseX, mouseY, delta, x, y, width, height, extras);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            if (onToggle != null) {
                onToggle.accept(checked);
            }
        }
    }

    public CheckboxWidget setOnToggle(Consumer<Boolean> onToggle) {
        this.onToggle = onToggle;
        return this;
    }
}