package v.akfz.aslib.render.gui.widget.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGroupWidget extends AbstractWidget implements ContainerEventHandler {

    protected final List<AbstractWidget> children = new ArrayList<>();
    @Nullable private GuiEventListener focusedChild = null;
    @Nullable private GuiEventListener draggingChild = null;
    private boolean isDragging = false;

    public AbstractGroupWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void addWidget(AbstractWidget widget) {
        if (widget != null) children.add(widget);
    }

    public void removeWidget(AbstractWidget widget) {
        if (focusedChild == widget) focusedChild = null;
        if (draggingChild == widget) draggingChild = null;
        children.remove(widget);
    }

    public void clearWidgets() {
        children.clear();
        focusedChild = null;
        draggingChild = null;
    }

    public List<AbstractWidget> getChildren() {
        return children;
    }

    protected double[] toChildSpace(double mouseX, double mouseY) {
        return new double[]{mouseX, mouseY};
    }

    @Override public List<? extends GuiEventListener> children() { return children; }
    @Override public boolean isDragging() { return isDragging; }
    @Override public void setDragging(boolean dragging) { this.isDragging = dragging; }

    @Nullable @Override public GuiEventListener getFocused() { return focusedChild; }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if (focusedChild == focused) return;
        if (focusedChild != null) focusedChild.setFocused(false);
        focusedChild = focused;
        if (focusedChild != null) focusedChild.setFocused(true);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (focusedChild != null) {
            focusedChild.setFocused(focused);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void doRender(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        for (AbstractWidget child : children) {
            if (child.isVisible()) {
                child.render(graphics, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;

        double[] c = toChildSpace(mouseX, mouseY);
        for (int i = children.size() - 1; i >= 0; i--) {
            AbstractWidget child = children.get(i);
            if (!child.isVisible()) continue;
            if (!child.isMouseOver(c[0], c[1])) continue;

            if (child.mouseClicked(c[0], c[1], button)) {
                setFocused(child);
                draggingChild = child;
                setDragging(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double[] c = toChildSpace(mouseX, mouseY);

        GuiEventListener target = draggingChild != null ? draggingChild : focusedChild;
        draggingChild = null;
        setDragging(false);

        if (target instanceof AbstractWidget w) {
            return w.mouseReleased(c[0], c[1], button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double[] c = toChildSpace(mouseX, mouseY);

        GuiEventListener target = draggingChild != null ? draggingChild : focusedChild;
        if (target instanceof AbstractWidget w) {
            return w.mouseDragged(c[0], c[1], button, dragX, dragY);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!visible || !isMouseOver(mouseX, mouseY)) return false;

        double[] c = toChildSpace(mouseX, mouseY);
        for (int i = children.size() - 1; i >= 0; i--) {
            AbstractWidget child = children.get(i);
            if (!child.isVisible()) continue;
            if (!child.isMouseOver(c[0], c[1])) continue;
            if (child.mouseScrolled(c[0], c[1], amount)) return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_TAB) {
            boolean shift = (modifiers & org.lwjgl.glfw.GLFW.GLFW_MOD_SHIFT) != 0;
            cycleFocus(shift ? -1 : 1);
            return true;
        }
        return focusedChild != null && focusedChild.keyPressed(keyCode, scanCode, modifiers);
    }

    protected void cycleFocus(int direction) {
        if (children.isEmpty()) return;
        int size = children.size();
        int current = focusedChild != null ? children.indexOf(focusedChild) : -1;
        for (int i = 1; i <= size; i++) {
            int idx = ((current + direction * i) % size + size) % size;
            AbstractWidget c = children.get(idx);
            if (c.isVisible()) {
                setFocused(c);
                return;
            }
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return focusedChild != null && focusedChild.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return focusedChild != null && focusedChild.charTyped(codePoint, modifiers);
    }
}
