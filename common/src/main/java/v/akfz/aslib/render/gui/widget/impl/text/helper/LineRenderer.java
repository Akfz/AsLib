package v.akfz.aslib.render.gui.widget.impl.text.helper;

import net.minecraft.client.gui.GuiGraphics;

public interface LineRenderer {
    void render(GuiGraphics graphics, String line, int x, int y, int lineIndex, int lineStartIndex, int defaultColor);
}