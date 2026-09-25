package v.akfz.aslib.render.gui.widget.impl.render;

import v.akfz.aslib.render.color.ColorUtils;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.impl.text.helper.LineRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector4f;

public class DefaultTextFieldRenderer implements RenderPart {

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height, RenderExtras extras) {
        String text = extras.getOrDefault("text", String.class, "");
        String placeholder = extras.getOrDefault("placeholder", String.class, "");
        int cursorPos = extras.getOrDefault("cursorPos", Integer.class, 0);
        int selStart = extras.getOrDefault("selStart", Integer.class, -1);
        int selEnd = extras.getOrDefault("selEnd", Integer.class, -1);
        int scrollX = extras.getOrDefault("scrollX", Integer.class, 0);
        boolean editable = extras.getOrDefault("editable", Boolean.class, true);
        boolean focused = extras.getOrDefault("focused", Boolean.class, false);
        boolean cursorVisible = extras.getOrDefault("cursorVisible", Boolean.class, true);
        LineRenderer lineRenderer = extras.get("lineRenderer", LineRenderer.class);

        int bgColor = ColorUtils.rgbToArgb(255, 30, 30, 30);
        int borderColor = focused ? ColorUtils.rgbToArgb(255, 140, 140, 140) : ColorUtils.rgbToArgb(255, 70, 70, 70);
        int txtColor = editable ? 0xE0E0E0 : 0xA0A0A0;
        int placeholderColor = 0x808080;
        int selectionColor = ColorUtils.rgbToArgb(100, 0, 120, 215);
        int cursorColor = 0xFFFFFFFF;

        Font font = Minecraft.getInstance().font;

        graphics.fill(x, y, x + width, y + height, bgColor);
        graphics.renderOutline(x, y, width, height, borderColor);

        int textY = y + (height - font.lineHeight) / 2;

        if (text.isEmpty() && !focused && !placeholder.isEmpty()) {
            graphics.drawString(font, placeholder, x + 5, textY, placeholderColor, false);
            return;
        }

        Vector4f screenPos = new Vector4f((float) x, (float) y, 0.0F, 1.0F);
        screenPos.mul(graphics.pose().last().pose());

        int actualX = Math.round(screenPos.x());
        int actualY = Math.round(screenPos.y());

        graphics.enableScissor(actualX + 3, actualY + 2, actualX + width - 3, actualY + height - 2);

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x + 4 - scrollX, textY, 0);

        int selMin = Math.min(selStart, selEnd);
        int selMax = Math.max(selStart, selEnd);
        if (selStart != -1 && selEnd != -1 && selStart != selEnd) {
            int startSelCol = Math.max(0, selMin);
            int endSelCol = Math.min(text.length(), selMax);

            int selX1 = font.width(text.substring(0, startSelCol));
            int selX2 = font.width(text.substring(0, endSelCol));

            graphics.fill(selX1, 0, selX2, font.lineHeight, selectionColor);
        }

        if (lineRenderer != null) {
            lineRenderer.render(graphics, text, 0, 0, 0, 0, txtColor);
        } else {
            graphics.drawString(font, text, 0, 0, txtColor, false);
        }

        if (focused && cursorVisible && editable && cursorPos >= 0 && cursorPos <= text.length()) {
            int cursorX = font.width(text.substring(0, cursorPos));
            graphics.fill(cursorX, 0, cursorX + 1, font.lineHeight, cursorColor);
        }

        poseStack.popPose();
        graphics.disableScissor();
    }
}