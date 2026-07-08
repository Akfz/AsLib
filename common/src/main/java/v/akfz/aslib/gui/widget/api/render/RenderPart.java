package v.akfz.aslib.gui.widget.api.render;

import net.minecraft.client.gui.GuiGraphics;

public interface RenderPart {
    void render(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height,
                RenderExtras extras);
}

