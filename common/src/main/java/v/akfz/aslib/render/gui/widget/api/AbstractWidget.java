package v.akfz.aslib.render.gui.widget.api;

import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.api.tooltip.AbstractToolTip;
import v.akfz.aslib.render.gui.widget.impl.tooltip.DefaultToolTip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class AbstractWidget implements Renderable, GuiEventListener, NarratableEntry {
    protected int x, y, width, height;
    protected boolean visible = true;
    protected boolean hovered = false;

    protected RenderPart mainRenderer;
    protected boolean focused = false;
    protected AbstractToolTip toolTip;

    public AbstractWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public AbstractWidget setTextTooltip(List<String> textTooltip) {
        if (toolTip == null) {
            toolTip = new DefaultToolTip(4,4,4,4);
        }
        toolTip.setText(textTooltip);
        return this;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setScale(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isHovered() {
        return this.hovered;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        doRender(graphics, mouseX, mouseY, delta);
        if (toolTip != null && hovered) {
            toolTip.render(graphics, mouseX, mouseY, delta);
        }
    }

    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (mainRenderer == null) {
            mainRenderer = new nullRenderer();
        }

        mainRenderer.render(graphics, mouseX, mouseY, delta, x, y, width, height, new RenderExtras());
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    private static class nullRenderer implements RenderPart {

        @Override
        public void render(GuiGraphics context, int mouseX, int mouseY, float delta, int x, int y, int width,
                           int height, RenderExtras extras) {
            String text = "рендер не выбран!";
            context.fill(x, y, x + width, y + height, ColorUtils.rgbToArgb(90));
            context.renderOutline(x, y, width, height, ColorUtils.rgbToArgb(115));

            int textX = x + (width - Minecraft.getInstance().font.width(text)) / 2;
            int textY = y + (height - 8) / 2;
            context.drawString(Minecraft.getInstance().font, text, textX, textY, ColorUtils.white(),
                    true);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.hovered;
    }
}
