package v.akfz.aslib.render.gui.widget.impl.render;

import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DefaultSliderRenderer implements RenderPart {

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height, RenderExtras extras) {
        String label = extras.getOrDefault("label", String.class, "");
        double value = extras.getOrDefault("value", Double.class, 0.0);
        double progress = extras.getOrDefault("progress", Double.class, 0.0);
        boolean showValue = extras.getOrDefault("showValue", Boolean.class, true);
        boolean dragging = extras.getOrDefault("dragging", Boolean.class, false);
        boolean hovered = extras.getOrDefault("hovered", Boolean.class, false);
        boolean focused = extras.getOrDefault("focused", Boolean.class, false);

        int trackBgColor = ColorUtils.rgbToArgb(255, 25, 25, 25);
        int trackFillColor = ColorUtils.rgbToArgb(255, 70, 140, 240);
        int borderColor = focused ? 0xFFFFFFFF : ColorUtils.rgbToArgb(255, 100, 100, 100);

        int handleColor;
        if (dragging) {
            handleColor = ColorUtils.rgbToArgb(255, 180, 180, 180);
        } else if (hovered) {
            handleColor = ColorUtils.rgbToArgb(255, 140, 140, 140);
        } else {
            handleColor = ColorUtils.rgbToArgb(255, 90, 90, 90);
        }

        graphics.fill(x, y, x + width, y + height, trackBgColor);
        graphics.renderOutline(x, y, width, height, borderColor);

        int handleWidth = 8;
        int trackScrollWidth = width - handleWidth - 4;
        int handleX = x + 4 + (int) (progress * trackScrollWidth);

        if (handleX > x + 4) {
            graphics.fill(x + 2, y + 2, handleX, y + height - 2, trackFillColor);
        }

        graphics.fill(handleX, y + 2, handleX + handleWidth, y + height - 2, handleColor);
        graphics.renderOutline(handleX, y + 2, handleWidth, height - 4, ColorUtils.white());

        String displayStr = label;
        if (showValue) {
            String formattedValue = String.format("%.2f", value);
            displayStr = label.isEmpty() ? formattedValue : label + ": " + formattedValue;
        }

        if (!displayStr.isEmpty()) {
            Font font = Minecraft.getInstance().font;
            Component component = Component.literal(displayStr);

            int textX = x + (width - font.width(component)) / 2;
            int textY = y + (height - font.lineHeight) / 2;

            graphics.drawString(font, component, textX, textY, ColorUtils.white(), true);
        }
    }
}