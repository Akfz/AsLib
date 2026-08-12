package v.akfz.aslib.render.gui.widget.api.render;

import net.minecraft.client.gui.GuiGraphics;

//интересно, я хоть раз этим воспользуюсь?
public interface RenderPart {
    void render(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height,
                RenderExtras extras);
}

