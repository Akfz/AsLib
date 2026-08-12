package v.akfz.aslib.render.gui.widget.impl.render;

import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DefaultCheckboxRenderer implements RenderPart {

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height, RenderExtras extras) {
        String text = extras.getOrDefault("text", String.class, "");
        boolean checked = extras.getOrDefault("checked", Boolean.class, false);
        boolean enabled = extras.getOrDefault("enabled", Boolean.class, true);
        boolean pressed = extras.getOrDefault("pressed", Boolean.class, false);
        boolean hovered = extras.getOrDefault("hovered", Boolean.class, false);
        boolean focused = extras.getOrDefault("focused", Boolean.class, false);

        int boxSize = Math.min(14, height);
        int boxX = x;
        int boxY = y + (height - boxSize) / 2;

        int boxBgColor;
        int borderColor;
        int textColor;
        int checkColor = enabled ? ColorUtils.rgbToArgb(255, 70, 140, 240) : ColorUtils.rgbToArgb(255, 100, 100, 100);

        if (!enabled) {
            boxBgColor = ColorUtils.rgbToArgb(255, 35, 35, 35);
            borderColor = ColorUtils.rgbToArgb(255, 60, 60, 60);
            textColor = 0xA0A0A0;
        } else if (pressed) {
            boxBgColor = ColorUtils.rgbToArgb(255, 20, 20, 20);
            borderColor = ColorUtils.rgbToArgb(255, 160, 160, 160);
            textColor = 0xFFFFA0;
        } else if (hovered) {
            boxBgColor = ColorUtils.rgbToArgb(255, 65, 65, 65);
            borderColor = focused ? 0xFFFFFFFF : ColorUtils.rgbToArgb(255, 180, 180, 180);
            textColor = 0xFFFFFF;
        } else {
            boxBgColor = ColorUtils.rgbToArgb(255, 45, 45, 45);
            borderColor = focused ? 0xFFFFFFFF : ColorUtils.rgbToArgb(255, 110, 110, 110);
            textColor = 0xE0E0E0;
        }

        graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, boxBgColor);
        graphics.renderOutline(boxX, boxY, boxSize, boxSize, borderColor);

        if (checked) {
            int pad = 3;
            graphics.fill(boxX + pad, boxY + pad, boxX + boxSize - pad, boxY + boxSize - pad, checkColor);
        }

        if (!text.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            Component textComponent = Component.literal(text);

            int textX = boxX + boxSize + 4;
            int textY = y + (height - font.lineHeight) / 2;

            graphics.drawString(font, textComponent, textX, textY, textColor, true);
        }
    }
}