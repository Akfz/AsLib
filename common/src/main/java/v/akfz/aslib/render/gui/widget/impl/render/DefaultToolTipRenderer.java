package v.akfz.aslib.render.gui.widget.impl.render;

import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DefaultToolTipRenderer implements RenderPart {
    @SuppressWarnings("unchecked")
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height, RenderExtras extras) {
        int bgColor = (int) extras.get("backgroundColor", Integer.class);
        List<String> lines = (List<String>) extras.get("text",List.class);
        if (lines == null || lines.isEmpty())
            return;

        graphics.fill(x, y, x + width, y + height, bgColor);
        graphics.renderOutline(x, y, width, height, ColorUtils.rgbToArgb(115));

        Font font = Minecraft.getInstance().font;
        int padding = 4;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineX = x + padding;
            int lineY = y + padding + (i * (font.lineHeight + 2));
            graphics.drawString(font, Component.literal(line), lineX, lineY, ColorUtils.white(), true);
        }
    }
}
