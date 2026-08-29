package v.akfz.aslib.mixin;

import v.akfz.aslib.resourcepack.ResourcePackExpander;
import v.akfz.aslib.resourcepack.configpack.ConfigPack;
import v.akfz.aslib.resourcepack.dynamic.DynamicDataPack;
import net.minecraft.server.packs.repository.Pack;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(PackRepository.class)
public class ResourcePackManagerMixin implements ResourcePackExpander {

    @Mutable @Shadow @Final private Set<RepositorySource> sources;

    @Shadow private Map<String, Pack> available;

    @Unique
    private boolean aslib$staticProvidersRegistered = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void aslib$makeSourcesMutable(RepositorySource[] sourcesArray, CallbackInfo ci) {
        this.sources = new LinkedHashSet<>(this.sources);
    }

    @Override
    public void addProvider(RepositorySource provider) {
        if (!this.sources.contains(provider)) {
            this.sources.add(provider);
        }
    }

    @Override
    public void removeProvider(RepositorySource provider) {
        this.sources.remove(provider);
    }

    @Inject(method = "discoverAvailable", at = @At("HEAD"))
    private void aslib$registerDynamicPacks(CallbackInfoReturnable<Map<String, Pack>> cir) {
        if (!aslib$staticProvidersRegistered) {
            aslib$staticProvidersRegistered = true;
            PackRepository repo = (PackRepository) (Object) this;
            DynamicDataPack.registerToRepository(repo);
            ConfigPack.registerToRepository(repo);
        }
    }

    @Inject(method = "rebuildSelected", at = @At("RETURN"), cancellable = true)
    private void aslib$forceInjectRequiredPacks(Collection<String> collection, CallbackInfoReturnable<List<Pack>> cir) {
        List<Pack> originalSelected = cir.getReturnValue();
        List<Pack> extendedSelected = new ArrayList<>(originalSelected);
        boolean modified = false;

        for (Pack pack : this.available.values()) {
            if (pack.isRequired() && !extendedSelected.contains(pack)) {
                extendedSelected.add(0, pack);
                modified = true;
            }
        }

        if (modified) {
            cir.setReturnValue(Collections.unmodifiableList(extendedSelected));
        }
    }
}