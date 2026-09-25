package v.akfz.aslib.render.gui.widget.impl.group;

import v.akfz.aslib.render.gui.widget.api.AbstractGroupWidget;
import v.akfz.aslib.render.gui.widget.api.AbstractWidget;

/**
 * Vertical stack. Children are laid out top-to-bottom with {@link #spacing}
 * and {@link #padding}. Layout is recalculated automatically on
 * {@link #addWidget} — call {@link #layout()} manually if you move or
 * resize a child afterwards.
 */
public class VBoxGroup extends AbstractGroupWidget {

	private int spacing = 4;
	private int padding = 4;

	public VBoxGroup(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public VBoxGroup spacing(int s) { this.spacing = s; return this; }
	public VBoxGroup padding(int p) { this.padding = p; return this; }

	@Override
	public void addWidget(AbstractWidget widget) {
		super.addWidget(widget);
		layout();
	}

	public void layout() {
		int yOff = padding;
		for (AbstractWidget c : children) {
			c.setPosition(x + padding, y + yOff);
			yOff += c.getHeight() + spacing;
		}
	}

	public int getSpacing() { return spacing; }
	public int getPadding() { return padding; }
}