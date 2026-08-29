package v.akfz.aslib.mixin;

import v.akfz.aslib.resourcepack.ResourcePackExpander;
import v.akfz.aslib.resourcepack.configpack.ConfigPack;
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

    @Unique
    private final Set<RepositorySource> aslib$additionalProviders = new LinkedHashSet<>();

    @Unique
    private final Map<String, Pack> aslib$cachedAdditionalPacks = new LinkedHashMap<>();

    @Unique
    private boolean aslib$providersLoaded = false;

    @Unique
    private boolean aslib$staticProvidersRegistered = false;

    @Override
    public void addProvider(RepositorySource provider) {
        if (this.aslib$additionalProviders.add(provider)) {
            this.aslib$providersLoaded = false;
        }
    }

    @Override
    public void removeProvider(RepositorySource provider) {
        if (this.aslib$additionalProviders.remove(provider)) {
            this.aslib$providersLoaded = false;
        }
    }

    @Inject(method = "discoverAvailable", at = @At("HEAD"))
    private void aslib$registerAndLoadDynamicPacks(CallbackInfoReturnable<Map<String, Pack>> cir) {
        if (!aslib$staticProvidersRegistered) {
            aslib$staticProvidersRegistered = true;
            PackRepository repo = (PackRepository) (Object) this;
            DynamicDataPack.registerToRepository(repo);
            ConfigPack.registerToRepository(repo);
        }

        if (!aslib$providersLoaded && !this.aslib$additionalProviders.isEmpty()) {
            aslib$providersLoaded = true;
            for (RepositorySource provider : this.aslib$additionalProviders) {
                provider.loadPacks(pack -> {
                    if (!this.aslib$cachedAdditionalPacks.containsKey(pack.getId())) {
                        this.aslib$cachedAdditionalPacks.put(pack.getId(), pack);
                    }
                });
            }
        }
    }

    @Inject(method = "discoverAvailable", at = @At("RETURN"), cancellable = true)
    private void aslib$injectAdditionalProviders(CallbackInfoReturnable<Map<String, Pack>> cir) {
        Map<String, Pack> originalMap = cir.getReturnValue();
        Map<String, Pack> extendedMap = new LinkedHashMap<>(originalMap);

        for (Map.Entry<String, Pack> entry : this.aslib$cachedAdditionalPacks.entrySet()) {
            if (!extendedMap.containsKey(entry.getKey())) {
                extendedMap.put(entry.getKey(), entry.getValue());
            }
        }

        cir.setReturnValue(Collections.unmodifiableMap(extendedMap));
    }

    @Inject(method = "rebuildSelected", at = @At("RETURN"), cancellable = true)
    private void aslib$forceInjectRequiredPacks(Collection<String> collection, CallbackInfoReturnable<List<Pack>> cir) {
        List<Pack> originalSelected = cir.getReturnValue();
        List<Pack> extendedSelected = new ArrayList<>(originalSelected);
        boolean modified = false;

        for (Pack pack : this.aslib$cachedAdditionalPacks.values()) {
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