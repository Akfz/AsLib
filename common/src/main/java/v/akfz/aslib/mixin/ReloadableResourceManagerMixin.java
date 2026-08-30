package v.akfz.aslib.mixin;

import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import v.akfz.aslib.resourcepack.AsLibResourceReloader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerMixin {

	@Shadow @Final private List<PreparableReloadListener> listeners;

	@Inject(method = "createReload", at = @At("HEAD"))
	private void aslib$addReloadListener(Executor backgroundExecutor, Executor gameExecutor,
	                                     CompletableFuture<?> waitingFor, List<PackResources> resourcePacks,
	                                     CallbackInfoReturnable<ReloadInstance> cir) {

		boolean alreadyAdded = false;
		for (PreparableReloadListener listener : this.listeners) {
			if (listener instanceof AsLibResourceReloader) {
				alreadyAdded = true;
				break;
			}
		}

		if (!alreadyAdded) {
			this.listeners.add(new AsLibResourceReloader());
		}
	}
}