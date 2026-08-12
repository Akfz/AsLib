package v.akfz.aslib.render.gui.widget.impl.text;

import v.akfz.aslib.render.gui.widget.api.AbstractWidget;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.impl.render.DefaultTextAreaRenderer;
import v.akfz.aslib.render.gui.widget.impl.text.helper.LineRenderer;
import v.akfz.aslib.render.gui.widget.impl.text.helper.TextBinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextArea extends AbstractWidget {
    private static final RenderPart DEFAULT_RENDERER = new DefaultTextAreaRenderer();
    private RenderPart mainRenderer = DEFAULT_RENDERER;

    private final StringBuilder text = new StringBuilder();
    private final int[] cursorPos = new int[] { 0 };
    private final int[] selStart = new int[] { -1 };
    private final int[] selEnd = new int[] { -1 };
    private final TextBinds textBinds = new TextBinds();

    private String placeholder = "";
    private int maxLength = Integer.MAX_VALUE;
    private boolean editable = true;
    private float textScale = 1.0f;
    private final int baseLineHeight;
    private int lineHeight;

    private int scrollX = 0;
    private int scrollY = 0;
    private boolean cursorVisible = true;
    private long lastCursorBlink = 0;
    private boolean scrollMouse = true;

    private LineRenderer lineRenderer = (graphics, line, x, y, lineIdx, startIdx, defcolor) ->
            graphics.drawString(Minecraft.getInstance().font, line, x, y, defcolor, false);

    public TextArea(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.baseLineHeight = Minecraft.getInstance().font.lineHeight + 2;
        this.lineHeight = (int) (baseLineHeight * textScale);
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (mainRenderer == null) {
            mainRenderer = DEFAULT_RENDERER;
        }

        RenderExtras extras = new RenderExtras()
                .with("text", String.class, getText())
                .with("placeholder", String.class, placeholder)
                .with("cursorPos", Integer.class, cursorPos[0])
                .with("selStart", Integer.class, selStart[0])
                .with("selEnd", Integer.class, selEnd[0])
                .with("scrollX", Integer.class, scrollX)
                .with("scrollY", Integer.class, scrollY)
                .with("lineHeight", Integer.class, lineHeight)
                .with("editable", Boolean.class, editable)
                .with("focused", Boolean.class, focused)
                .with("cursorVisible", Boolean.class, cursorVisible)
                .with("lineRenderer", LineRenderer.class, lineRenderer)
                .with("textScale", Float.class, textScale);

        mainRenderer.render(graphics, mouseX, mouseY, delta, x, y, width, height, extras);

        if (this.focused) {
            long time = System.currentTimeMillis();
            if (time - lastCursorBlink > 500) {
                cursorVisible = !cursorVisible;
                lastCursorBlink = time;
            }
        } else {
            cursorVisible = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !editable) return false;

        boolean clicked = mouseX >= this.x && mouseX < this.x + this.width &&
                mouseY >= this.y && mouseY < this.y + this.height;
        setFocused(clicked);

        if (clicked && button == 0) {
            int localY = (int) ((mouseY - this.y - 2 + scrollY) / textScale);
            int line = localY / baseLineHeight;

            List<String> lines = getLines();

            if (line >= 0 && line < lines.size()) {
                int localXToText = (int) ((mouseX - this.x - 4 + scrollX) / textScale);
                int col = TextBinds.getCharIndexAt(lines.get(line), localXToText, 0, 0);
                cursorPos[0] = getLineStartIndex(line) + col;
            } else if (line >= lines.size()) {
                cursorPos[0] = text.length();
            }
            selStart[0] = cursorPos[0];
            selEnd[0] = cursorPos[0];
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && focused && editable) {
            boolean isOver = mouseX >= this.x && mouseX < this.x + this.width &&
                    mouseY >= this.y && mouseY < this.y + this.height;
            if (!isOver) return false;

            int localY = (int) ((mouseY - this.y - 2 + scrollY) / textScale);
            int line = localY / baseLineHeight;

            List<String> lines = getLines();

            if (line >= 0 && line < lines.size()) {
                int localXToText = (int) ((mouseX - this.x - 4 + scrollX) / textScale);
                int col = TextBinds.getCharIndexAt(lines.get(line), localXToText, 0, 0);
                int globalPos = getLineStartIndex(line) + col;

                if (globalPos != cursorPos[0]) {
                    if (selStart[0] == -1) selStart[0] = cursorPos[0];
                    cursorPos[0] = globalPos;
                    selEnd[0] = cursorPos[0];
                }
            } else if (line >= lines.size()) {
                int globalPos = text.length();
                if (globalPos != cursorPos[0]) {
                    if (selStart[0] == -1) selStart[0] = cursorPos[0];
                    cursorPos[0] = globalPos;
                    selEnd[0] = cursorPos[0];
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS) {
            setTextScale(textScale + (float) amount * 0.1f);
            return true;
        }
        boolean isOver = mouseX >= this.x && mouseX < this.x + this.width &&
                mouseY >= this.y && mouseY < this.y + this.height;

        if (!scrollMouse || !isOver) return false;

        int maxScroll = Math.max(0, getLines().size() * lineHeight - (height - 4));
        scrollY = Math.max(0, Math.min(scrollY - (int) (amount * lineHeight * 3), maxScroll));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused || !editable) return false;

        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (ctrl) {
            if (keyCode == GLFW.GLFW_KEY_EQUAL || keyCode == GLFW.GLFW_KEY_KP_ADD) return scaleAndReturn(0.1f);
            if (keyCode == GLFW.GLFW_KEY_MINUS || keyCode == GLFW.GLFW_KEY_KP_SUBTRACT) return scaleAndReturn(-0.1f);
            if (keyCode == GLFW.GLFW_KEY_0 || keyCode == GLFW.GLFW_KEY_KP_0) { setTextScale(1.0f); return true; }
        }

        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            moveCursorVertical(keyCode == GLFW.GLFW_KEY_UP ? -1 : 1, shift);
            ensureCursorVisible();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            int dir = keyCode == GLFW.GLFW_KEY_PAGE_UP ? -1 : 1;
            int pageScroll = ((height - 4) / lineHeight) * lineHeight;
            int maxScroll = Math.max(0, getLines().size() * lineHeight - (height - 4));
            scrollY = Math.max(0, Math.min(scrollY + dir * pageScroll, maxScroll));
            ensureCursorVisible();
            return true;
        }

        if (textBinds.handleKeyPress(keyCode, modifiers, text, cursorPos, selStart, selEnd, true)) {
            ensureCursorVisible();
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!focused || !editable) return false;
        if (text.length() >= maxLength) return true;

        if (textBinds.handleCharTyped(chr, modifiers, text, cursorPos, selStart, selEnd)) {
            ensureCursorVisible();
            return true;
        }
        return false;
    }

    private boolean scaleAndReturn(float amount) {
        setTextScale(textScale + amount);
        return true;
    }

    private void moveCursorVertical(int direction, boolean shift) {
        int[] lineCol = getLineAndCol(cursorPos[0]);
        int targetLine = lineCol[0] + direction;
        List<String> lines = getLines();

        if (targetLine >= 0 && targetLine < lines.size()) {
            if (shift && selStart[0] == -1) {
                selStart[0] = cursorPos[0];
                selEnd[0] = cursorPos[0];
            }
            cursorPos[0] = getLineStartIndex(targetLine) + Math.min(lineCol[1], lines.get(targetLine).length());
            if (shift) selEnd[0] = cursorPos[0];
            else { selStart[0] = -1; selEnd[0] = -1; }
        }
    }

    public void ensureCursorVisible() {
        if (text.isEmpty()) return;
        int[] lineCol = getLineAndCol(cursorPos[0]);

        int cursorY = (int) (lineCol[0] * baseLineHeight * textScale);

        if (cursorY < scrollY) {
            scrollY = cursorY;
        } else if (cursorY + lineHeight > scrollY + (height - 4)) {
            scrollY = cursorY + lineHeight - (height - 4);
        }

        int textWidthBeforeCursor = Minecraft.getInstance().font.width(getLines().get(lineCol[0]).substring(0, lineCol[1]));
        int cursorX = (int) (textWidthBeforeCursor * textScale);

        if (cursorX < scrollX) {
            scrollX = cursorX;
        } else if (cursorX > scrollX + (width - 16)) {
            scrollX = cursorX - (width - 16);
        }

        scrollX = Math.max(0, scrollX);
        scrollY = Math.max(0, Math.min(scrollY, Math.max(0, getLines().size() * lineHeight - (height - 4))));
    }

    public List<String> getLines() {
        if (text.isEmpty()) return new ArrayList<>(Collections.singletonList(""));
        return new ArrayList<>(List.of(text.toString().split("\n", -1)));
    }

    private int getLineStartIndex(int line) {
        List<String> lines = getLines();
        int index = 0;
        for (int i = 0; i < line; i++) index += lines.get(i).length() + 1;
        return index;
    }

    private int[] getLineAndCol(int globalPos) {
        List<String> lines = getLines();
        int remaining = globalPos;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (remaining <= line.length()) return new int[] { i, remaining };
            remaining -= line.length() + 1;
        }
        return new int[] { lines.size() - 1, lines.get(lines.size() - 1).length() };
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public String getText() { return text.toString(); }
    public void setText(String newText) {
        text.setLength(0); text.append(newText);
        cursorPos[0] = Math.min(cursorPos[0], text.length());
        if (selStart[0] > text.length()) selStart[0] = -1;
        if (selEnd[0] > text.length()) selEnd[0] = -1;
        ensureCursorVisible();
    }

    public void setTextScale(float scale) {
        this.textScale = Math.max(0.5f, Math.min(2.0f, scale));
        this.lineHeight = (int) (baseLineHeight * textScale);
        scrollY = Math.max(0, Math.min(scrollY, Math.max(0, getLines().size() * lineHeight - (height - 4))));
        ensureCursorVisible();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused) {
            selStart[0] = -1;
            selEnd[0] = -1;
        }
        this.cursorVisible = focused;
        if (focused) {
            this.lastCursorBlink = System.currentTimeMillis();
        }
    }

    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    public int getMaxLength() { return maxLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }
    public float getTextScale() { return textScale; }
    public int getBaseLineHeight() { return baseLineHeight; }
    public int getLineHeight() { return lineHeight; }
    public int getScrollX() { return scrollX; }
    public void setScrollX(int scrollX) { this.scrollX = scrollX; }
    public int getScrollY() { return scrollY; }
    public void setScrollY(int scrollY) { this.scrollY = scrollY; }
    public boolean isCursorVisible() { return cursorVisible; }
    public void setCursorVisible(boolean cursorVisible) { this.cursorVisible = cursorVisible; }
    public boolean isScrollMouse() { return scrollMouse; }
    public void setScrollMouse(boolean scrollMouse) { this.scrollMouse = scrollMouse; }
    public int getCursorPos() { return cursorPos[0]; }
    public void setCursorPos(int pos) { cursorPos[0] = pos; ensureCursorVisible(); }
    public int getSelectionStart() { return selStart[0]; }
    public int getSelectionEnd() { return selEnd[0]; }
    public boolean hasSelection() { return selStart[0] != -1 && selEnd[0] != -1 && selStart[0] != selEnd[0]; }
    public LineRenderer getLineRenderer() { return lineRenderer; }
    public void setLineRenderer(LineRenderer lineRenderer) { this.lineRenderer = lineRenderer; }
}