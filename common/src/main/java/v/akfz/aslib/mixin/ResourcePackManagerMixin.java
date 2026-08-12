package v.akfz.aslib.mixin;

import v.akfz.aslib.resourcepack.ResourcePackExpander;
import v.akfz.aslib.resourcepack.dynamic.DynamicDataPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(PackRepository.class)
public class ResourcePackManagerMixin implements ResourcePackExpander {
    @Unique private final Set<RepositorySource> aslib$additionalProviders = new HashSet<>();

    @Override
    public void addProvider(RepositorySource provider) {
        this.aslib$additionalProviders.add(provider);
    }

    @Override
    public void removeProvider(RepositorySource provider) {
        this.aslib$additionalProviders.remove(provider);
    }

    @Inject(method = "discoverAvailable", at = @At("HEAD"))
    private void aslib$registerDynamicDataPack(CallbackInfoReturnable<Map<String, Pack>> cir) {
        PackRepository repo = (PackRepository) (Object) this;
        DynamicDataPack.registerToRepository(repo);
    }

    @Inject(method = "discoverAvailable", at = @At("RETURN"), cancellable = true)
    private void aslib$injectAdditionalProviders(CallbackInfoReturnable<Map<String, Pack>> cir) {
        Map<String, Pack> originalMap = cir.getReturnValue();
        Map<String, Pack> extendedMap = new TreeMap<>(originalMap);

        for (RepositorySource provider : this.aslib$additionalProviders) {
            provider.loadPacks(profile -> extendedMap.put(profile.getId(), profile));
        }

        cir.setReturnValue(Collections.unmodifiableMap(extendedMap));
    }

    @Inject(method = "rebuildSelected", at = @At("RETURN"), cancellable = true)
    private void aslib$forceInjectRequiredPacks(Collection<String> collection, CallbackInfoReturnable<List<Pack>> cir) {
        List<Pack> originalSelected = cir.getReturnValue();

        List<Pack> extendedSelected = new ArrayList<>(originalSelected);
        boolean modified = false;

        for (RepositorySource provider : this.aslib$additionalProviders) {
            List<Pack> customPacks = new ArrayList<>();
            provider.loadPacks(customPacks::add);

            for (Pack pack : customPacks) {
                if (pack.isRequired() && !extendedSelected.contains(pack)) {
                    pack.getDefaultPosition().insert(extendedSelected, pack, com.google.common.base.Functions.identity(), false);
                    modified = true;
                }
            }
        }

        if (modified) {
            cir.setReturnValue(List.copyOf(extendedSelected));
        }
    }
}