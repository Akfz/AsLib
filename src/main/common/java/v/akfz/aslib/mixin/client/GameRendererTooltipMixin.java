package v.akfz.aslib.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import v.akfz.aslib.render.gui.widget.api.tooltip.TooltipQueue;

@Mixin(Screen.class)
public class GameRendererTooltipMixin {

	@Inject(method = "render", at = @At("TAIL"))
	private void aslib$flushTooltips(GuiGraphics pGuiGraphics,int pMouseX,int pMouseY,float pPartialTick,CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null && pGuiGraphics != null) {
			TooltipQueue.flush(pGuiGraphics,(int) mc.mouseHandler.xpos(),(int) mc.mouseHandler.ypos());
		}
	}
}