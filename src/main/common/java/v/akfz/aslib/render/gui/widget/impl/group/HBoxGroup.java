package v.akfz.aslib.render.gui.widget.impl.group;

import v.akfz.aslib.render.gui.widget.api.AbstractGroupWidget;
import v.akfz.aslib.render.gui.widget.api.AbstractWidget;

/**
 * Horizontal stack. Children laid out left-to-right.
 */
public class HBoxGroup extends AbstractGroupWidget {

	private int spacing = 4;
	private int padding = 4;

	public HBoxGroup(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public HBoxGroup spacing(int s) { this.spacing = s; return this; }
	public HBoxGroup padding(int p) { this.padding = p; return this; }

	@Override
	public void addWidget(AbstractWidget widget) {
		super.addWidget(widget);
		layout();
	}

	public void layout() {
		int xOff = padding;
		for (AbstractWidget c : children) {
			c.setPosition(x + xOff, y + padding);
			xOff += c.getWidth() + spacing;
		}
	}

	public int getSpacing() { return spacing; }
	public int getPadding() { return padding; }
}