package v.akfz.aslib.render.gui.widget.api.tooltip;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public final class TooltipQueue {
	private TooltipQueue() {}

	private static final List<PendingTooltip> PENDING = new ArrayList<>();

	public static void push(AbstractToolTip tooltip, float delta) {
		if (tooltip == null) return;
		PENDING.add(new PendingTooltip(tooltip, delta));
	}

	public static void flush(GuiGraphics graphics, int mouseX, int mouseY) {
		if (PENDING.isEmpty()) return;

		for (int i = PENDING.size() - 1; i >= 0; i--) {
			PendingTooltip p = PENDING.get(i);
			graphics.pose().pushPose();
			graphics.pose().translate(0.0F, 0.0F, 400.0F);
			try {
				p.tooltip.render(graphics, mouseX, mouseY, p.delta);
			} catch (Throwable t) {
				System.err.println("[ASLib] Tooltip render failed: " + t);
				t.printStackTrace();
			}
			graphics.pose().popPose();
		}
		PENDING.clear();
	}

	private record PendingTooltip(AbstractToolTip tooltip, float delta) {}
}