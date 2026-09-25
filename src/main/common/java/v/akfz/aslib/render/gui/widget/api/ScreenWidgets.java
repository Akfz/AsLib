package v.akfz.aslib.render.gui.widget.api;

import net.minecraft.client.gui.screens.Screen;
import v.akfz.aslib.mixin.client.ScreenAccessor;

/**
 * How to use (better in init) : {@code ScreenWidgets.install(this, root);}
 * Delegates input events to children (render excluded).
 * Warning! Focus is NOT set automatically on children.
 */
public final class ScreenWidgets {
	private ScreenWidgets() {}

	public static void install(Screen screen, AbstractGroupWidget root) {
		ScreenAccessor acc = (ScreenAccessor) screen;
		if (!acc.aslib$getChildren().contains(root)) {
			acc.aslib$getChildren().add(root);
		}
		if (!acc.aslib$getRenderables().contains(root)) {
			acc.aslib$getRenderables().add(root);
		}
	}

	public static void uninstall(Screen screen, AbstractGroupWidget root) {
		ScreenAccessor acc = (ScreenAccessor) screen;
		acc.aslib$getChildren().remove(root);
		acc.aslib$getRenderables().remove(root);
	}
}
