package v.akfz.aslib.mixin;

import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import v.akfz.aslib.resourcepack.ModAssetsRegistrar;
import v.akfz.aslib.resourcepack.ResourcePackExpander;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import v.akfz.aslib.resourcepack.configpack.ConfigPack;
import v.akfz.aslib.resourcepack.dynamic.DynamicDataPack;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Mixin(PackRepository.class)
public class ResourcePackManagerMixin implements ResourcePackExpander {

	@Mutable @Shadow @Final private Set<RepositorySource> sources;

	@Unique
	private boolean aslib$sourcesMadeMutable = false;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void aslib$makeSourcesMutable(RepositorySource[] sourcesArray, CallbackInfo ci) {
		if (!aslib$sourcesMadeMutable) {
			aslib$sourcesMadeMutable = true;
			this.sources = new LinkedHashSet<>(this.sources);
		}
	}

	@Inject(method = "discoverAvailable", at = @At("HEAD"))
	private void aslib$autoRegisterPacks(CallbackInfoReturnable<Map<String, Pack>> cir) {
		try {
			PackRepository repo = (PackRepository) (Object) this;
			ModAssetsRegistrar.flush(repo);
			ConfigPack.registerToRepository(repo);
			DynamicDataPack.registerToRepository(repo);
		} catch (Throwable t) {
			System.err.println("[ASLib] Error during pack auto-registration");
			t.printStackTrace();
		}
	}

	@Override
	public void addProvider(RepositorySource provider) {
		this.sources.add(provider);
	}

	@Override
	public void removeProvider(RepositorySource provider) {
		this.sources.remove(provider);
	}
}