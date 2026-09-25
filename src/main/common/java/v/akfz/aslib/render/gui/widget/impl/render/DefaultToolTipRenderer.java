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
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta,
                       int x, int y, int width, int height, RenderExtras extras) {
        int bgColor = extras.getOrDefault("backgroundColor", Integer.class, ColorUtils.rgbToArgb(80));
        List<?> raw = extras.get("text", List.class);
        if (raw == null || raw.isEmpty()) return;

        graphics.fill(x, y, x + width, y + height, bgColor);
        graphics.renderOutline(x, y, width, height, ColorUtils.rgbToArgb(115));

        Font font = Minecraft.getInstance().font;
        int padding = 4;

        for (int i = 0; i < raw.size(); i++) {
            Object o = raw.get(i);
            String line = (o == null) ? "" : o.toString();
            int lineX = x + padding;
            int lineY = y + padding + (i * (font.lineHeight + 2));
            graphics.drawString(font, Component.literal(line), lineX, lineY, ColorUtils.white(), true);
        }
    }
}