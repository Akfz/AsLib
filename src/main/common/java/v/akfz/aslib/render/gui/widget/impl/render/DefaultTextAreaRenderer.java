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

import java.util.ArrayList;
import java.util.List;

public class DefaultTextAreaRenderer implements RenderPart {

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta, int x, int y, int width, int height, RenderExtras extras) {
        String text = extras.getOrDefault("text", String.class, "");
        String placeholder = extras.getOrDefault("placeholder", String.class, "");
        int cursorPos = extras.getOrDefault("cursorPos", Integer.class, 0);
        int selStart = extras.getOrDefault("selStart", Integer.class, -1);
        int selEnd = extras.getOrDefault("selEnd", Integer.class, -1);
        int scrollX = extras.getOrDefault("scrollX", Integer.class, 0);
        int scrollY = extras.getOrDefault("scrollY", Integer.class, 0);
        boolean editable = extras.getOrDefault("editable", Boolean.class, true);
        boolean focused = extras.getOrDefault("focused", Boolean.class, false);
        boolean cursorVisible = extras.getOrDefault("cursorVisible", Boolean.class, true);
        LineRenderer lineRenderer = extras.get("lineRenderer", LineRenderer.class);
        float textScale = extras.getOrDefault("textScale", Float.class, 1.0f);

        int bgColor = ColorUtils.rgbToArgb(255, 30, 30, 30);
        int borderColor = focused ? ColorUtils.rgbToArgb(255, 140, 140, 140) : ColorUtils.rgbToArgb(255, 70, 70, 70);
        int txtColor = editable ? 0xE0E0E0 : 0xA0A0A0;
        int placeholderColor = 0x808080;
        int selectionColor = ColorUtils.rgbToArgb(100, 0, 120, 215);
        int cursorColor = 0xFFFFFFFF;

        Font font = Minecraft.getInstance().font;

        graphics.fill(x, y, x + width, y + height, bgColor);
        graphics.renderOutline(x, y, width, height, borderColor);

        if (text.isEmpty() && !focused && !placeholder.isEmpty()) {
            graphics.drawString(font, placeholder, x + 5, y + 4, placeholderColor, false);
            return;
        }

        List<String> lines = new ArrayList<>();
        if (text.isEmpty()) {
            lines.add("");
        } else {
            lines.addAll(List.of(text.split("\n", -1)));
        }

        int cursorLine = -1;
        int cursorCol = -1;
        int remaining = cursorPos;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (remaining <= line.length()) {
                cursorLine = i;
                cursorCol = remaining;
                break;
            }
            remaining -= line.length() + 1;
        }
        if (cursorLine == -1 && !lines.isEmpty()) {
            cursorLine = lines.size() - 1;
            cursorCol = lines.get(cursorLine).length();
        }

        Vector4f scissorPos1 = new Vector4f((float) (x + 3), (float) (y + 3), 0.0F, 1.0F);
        Vector4f scissorPos2 = new Vector4f((float) (x + width - 3), (float) (y + height - 3), 0.0F, 1.0F);

        scissorPos1.mul(graphics.pose().last().pose());
        scissorPos2.mul(graphics.pose().last().pose());

        graphics.enableScissor(
                Math.round(scissorPos1.x()),
                Math.round(scissorPos1.y()),
                Math.round(scissorPos2.x()),
                Math.round(scissorPos2.y())
        );

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        poseStack.translate(x + 4 - (scrollX / textScale), y + 2 - (scrollY / textScale), 0);
        poseStack.scale(textScale, textScale, 1.0f);

        int currentGlobalIdx = 0;
        int localLineHeight = font.lineHeight + 2;

        int selMin = Math.min(selStart, selEnd);
        int selMax = Math.max(selStart, selEnd);
        boolean hasSel = selStart != -1 && selEnd != -1 && selStart != selEnd;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineLen = line.length();
            int lineY = i * localLineHeight;

            if (hasSel) {
                int lineMin = currentGlobalIdx;
                int lineMax = currentGlobalIdx + lineLen;

                if (!(lineMax < selMin || lineMin > selMax)) {
                    int startSelCol = Math.max(0, selMin - lineMin);
                    int endSelCol = Math.min(lineLen, selMax - lineMin);

                    int selX1 = font.width(line.substring(0, startSelCol));
                    int selX2 = font.width(line.substring(0, endSelCol));

                    if (selMax > lineMax && endSelCol == lineLen) {
                        selX2 += 4;
                    }

                    if (selX1 != selX2) {
                        graphics.fill(selX1, lineY, selX2, lineY + font.lineHeight, selectionColor);
                    }
                }
            }

            if (lineRenderer != null) {
                lineRenderer.render(graphics, line, 0, lineY, i, currentGlobalIdx, txtColor);
            } else {
                graphics.drawString(font, line, 0, lineY, txtColor, false);
            }

            if (focused && cursorVisible && editable && i == cursorLine) {
                String lineBeforeCursor = line.substring(0, cursorCol);
                int cursorX = font.width(lineBeforeCursor);

                poseStack.pushPose();
                poseStack.translate(0, 0, 1.0f);

                graphics.vLine(cursorX, lineY - 1, lineY + font.lineHeight, cursorColor);

                poseStack.popPose();
            }

            currentGlobalIdx += lineLen + 1;
        }

        poseStack.popPose();
        graphics.disableScissor();
    }
}