package v.akfz.aslib.render.gui.widget.api.tooltip;

import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.impl.render.DefaultToolTipRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractToolTip implements Renderable {
    protected int x, y, width, height;
    protected boolean visible = true;

    protected RenderPart Renderer;
    protected int backgroundColor = ColorUtils.rgbToArgb(80);
    protected List<String> text = new ArrayList<>();

    public AbstractToolTip(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!visible || text == null || text.isEmpty())
            return;

        var tr = Minecraft.getInstance().font;

        int maxTextWidth = 0;
        for (String line : text) {
            maxTextWidth = Math.max(maxTextWidth, tr.width(line));
        }
        int totalWidth = maxTextWidth + 8;
        int totalHeight = text.size() * (tr.lineHeight + 2) + 6;

        int renderX = mouseX + 12;
        int renderY = mouseY - 12;

        int screenWidth = Minecraft.getInstance().getWindow().getScreenWidth();
        if (renderX + totalWidth > screenWidth) {
            renderX = mouseX - totalWidth - 8;
        }

        int screenHeight = Minecraft.getInstance().getWindow().getScreenHeight();
        if (renderY + totalHeight > screenHeight) {
            renderY = screenHeight - totalHeight - 4;
        }

        if (renderY < 0) {
            renderY = 4;
        }

        renderAt(context, renderX, renderY, totalWidth, totalHeight, delta);
    }

    protected void renderAt(GuiGraphics context, int x, int y, int w, int h, float delta) {
        if (Renderer == null) {
            Renderer = new DefaultToolTipRenderer();
        }

        Renderer.render(context, 0, 0, delta, x, y, w, h, new RenderExtras()
                .with("backgroundColor", Integer.class,backgroundColor)
                .with("text", List.class, text));
    }

    public void setText(List<String> text) {
        this.text = text;
    }
}
