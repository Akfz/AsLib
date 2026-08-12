package v.akfz.aslib.render.gui.widget.impl.render;

import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DefaultButtonRenderer implements RenderPart {

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta, int x, int y, int width, int height, RenderExtras extras) {
        String text = extras.getOrDefault("text", String.class, "");
        boolean enabled = extras.getOrDefault("enabled", Boolean.class, true);
        boolean pressed = extras.getOrDefault("pressed", Boolean.class, false);
        boolean hovered = extras.getOrDefault("hovered", Boolean.class, false);
        boolean focused = extras.getOrDefault("focused", Boolean.class, false);

        int backgroundColor;
        int borderColor;
        int textColor;

        if (!enabled) {
            backgroundColor = ColorUtils.rgbToArgb(40);
            borderColor = ColorUtils.rgbToArgb(70);
            textColor = 0xA0A0A0;
        } else if (pressed) {
            backgroundColor = ColorUtils.rgbToArgb(30);
            borderColor = ColorUtils.rgbToArgb(180);
            textColor = 0xFFFFA0;
        } else if (hovered) {
            backgroundColor = ColorUtils.rgbToArgb(100);
            borderColor = focused ? 0xFFFFFFFF : ColorUtils.rgbToArgb(180);
            textColor = 0xFFFFFF;
        } else {
            backgroundColor = ColorUtils.rgbToArgb(60);
            borderColor = focused ? 0xFFFFFFFF : ColorUtils.rgbToArgb(110);
            textColor = 0xE0E0E0;
        }

        context.fill(x, y, x + width, y + height, backgroundColor);

        context.renderOutline(x, y, width, height, borderColor);

        Font font = Minecraft.getInstance().font;
        Component textComponent = Component.literal(text);

        int textX = x + (width - font.width(textComponent)) / 2;
        int textY = y + (height - font.lineHeight) / 2;

        context.drawString(font, textComponent, textX, textY, textColor, true);
    }
}