package v.akfz.aslib.render.gui.widget.impl.text;

import v.akfz.aslib.render.gui.widget.api.AbstractWidget;
import v.akfz.aslib.render.gui.widget.api.render.RenderExtras;
import v.akfz.aslib.render.gui.widget.api.render.RenderPart;
import v.akfz.aslib.render.gui.widget.impl.render.DefaultTextFieldRenderer;
import v.akfz.aslib.render.gui.widget.impl.text.helper.LineRenderer;
import v.akfz.aslib.render.gui.widget.impl.text.helper.TextBinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class TextField extends AbstractWidget {
    private static final RenderPart DEFAULT_RENDERER = new DefaultTextFieldRenderer();
    private RenderPart mainRenderer = DEFAULT_RENDERER;

    private final StringBuilder text = new StringBuilder();
    private final int[] cursorPos = new int[] { 0 };
    private final int[] selStart = new int[] { -1 };
    private final int[] selEnd = new int[] { -1 };
    private final TextBinds textBinds = new TextBinds();

    private String placeholder = "";
    private int maxLength = Integer.MAX_VALUE;
    private boolean editable = true;
    private int scrollX = 0;
    private boolean cursorVisible = true;
    private long lastCursorBlink = 0;

    private LineRenderer lineRenderer = (graphics, line, x, y, lineIdx, startIdx, defcolor) ->
            graphics.drawString(Minecraft.getInstance().font, line, x, y, defcolor, false);

    public TextField(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (mainRenderer == null) mainRenderer = DEFAULT_RENDERER;

        RenderExtras extras = new RenderExtras()
                .with("text", String.class, getText())
                .with("placeholder", String.class, placeholder)
                .with("cursorPos", Integer.class, cursorPos[0])
                .with("selStart", Integer.class, selStart[0])
                .with("selEnd", Integer.class, selEnd[0])
                .with("scrollX", Integer.class, scrollX)
                .with("editable", Boolean.class, editable)
                .with("focused", Boolean.class, focused)
                .with("cursorVisible", Boolean.class, cursorVisible)
                .with("lineRenderer", LineRenderer.class, lineRenderer);

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
            int localXToText = (int) (mouseX - this.x - 4 + scrollX);
            cursorPos[0] = TextBinds.getCharIndexAt(text.toString(), localXToText, 0, 0);
            selStart[0] = cursorPos[0];
            selEnd[0] = cursorPos[0];
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean isOver = mouseX >= this.x && mouseX < this.x + this.width &&
                mouseY >= this.y && mouseY < this.y + this.height;

        if (button == 0 && focused && editable && isOver) {
            int localXToText = (int) (mouseX - this.x - 4 + scrollX);
            int globalPos = TextBinds.getCharIndexAt(text.toString(), localXToText, 0, 0);

            if (globalPos != cursorPos[0]) {
                if (selStart[0] == -1) selStart[0] = cursorPos[0];
                cursorPos[0] = globalPos;
                selEnd[0] = cursorPos[0];
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused || !editable) return false;

        if (textBinds.handleKeyPress(keyCode, modifiers, text, cursorPos, selStart, selEnd, false)) {
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

    public void ensureCursorVisible() {
        if (text.isEmpty()) {
            scrollX = 0;
            return;
        }

        int cursorX = Minecraft.getInstance().font.width(text.substring(0, cursorPos[0]));

        if (cursorX < scrollX) {
            scrollX = cursorX;
        } else if (cursorX > scrollX + (width - 16)) {
            scrollX = cursorX - (width - 16);
        }
        scrollX = Math.max(0, scrollX);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public String getText() { return text.toString(); }
    public void setText(String newText) {
        text.setLength(0); text.append(newText.replace("\n", ""));
        cursorPos[0] = Math.min(cursorPos[0], text.length());
        if (selStart[0] > text.length()) selStart[0] = -1;
        if (selEnd[0] > text.length()) selEnd[0] = -1;
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
    public int getScrollX() { return scrollX; }
    public void setScrollX(int scrollX) { this.scrollX = scrollX; }
    public boolean isCursorVisible() { return cursorVisible; }
    public void setCursorVisible(boolean cursorVisible) { this.cursorVisible = cursorVisible; }
    public int getCursorPos() { return cursorPos[0]; }
    public void setCursorPos(int pos) { cursorPos[0] = pos; ensureCursorVisible(); }
    public int getSelectionStart() { return selStart[0]; }
    public int getSelectionEnd() { return selEnd[0]; }
    public boolean hasSelection() { return selStart[0] != -1 && selEnd[0] != -1 && selStart[0] != selEnd[0]; }
    public LineRenderer getLineRenderer() { return lineRenderer; }
    public void setLineRenderer(LineRenderer lineRenderer) { this.lineRenderer = lineRenderer; }
}