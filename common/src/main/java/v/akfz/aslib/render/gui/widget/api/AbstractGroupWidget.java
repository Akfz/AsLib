package v.akfz.aslib.render.gui.widget.api;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGroupWidget extends AbstractWidget implements ContainerEventHandler {
    protected final List<AbstractWidget> children = new ArrayList<>();
    private GuiEventListener focusedChild = null;
    private boolean isDragging = false;

    public AbstractGroupWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void addWidget(AbstractWidget widget) {
        children.add(widget);
    }

    public void removeWidget(AbstractWidget widget) {
        if (focusedChild == widget) focusedChild = null;
        children.remove(widget);
    }

    public void clearWidgets() {
        children.clear();
        focusedChild = null;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public boolean isDragging() {
        return isDragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.isDragging = dragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return focusedChild;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        this.focusedChild = focused;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public List<AbstractWidget> getChildren() {
        return children;
    }
}