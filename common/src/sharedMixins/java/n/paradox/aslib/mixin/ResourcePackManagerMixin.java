package n.paradox.aslib.mixin;

import n.paradox.aslib.resourcepack.ResourcePackExpander;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(PackRepository.class)
public class ResourcePackManagerMixin implements ResourcePackExpander {
    @Unique private final Set<RepositorySource> additionalProviders = new HashSet<>();

    @Override
    public void addProvider(RepositorySource provider) {
        this.additionalProviders.add(provider);
    }

    @Inject(method = "discoverAvailable", at = @At("RETURN"), cancellable = true)
    private void aslib$injectAdditionalProviders(CallbackInfoReturnable<Map<String, Pack>> cir) {
        Map<String, Pack> originalMap = cir.getReturnValue();
        Map<String, Pack> extendedMap = new java.util.TreeMap<>();

        extendedMap.putAll(originalMap);

        for (RepositorySource provider : this.additionalProviders) {
            provider.loadPacks(profile -> {
                extendedMap.put(profile.getId(), profile);
            });
        }

        cir.setReturnValue(Collections.unmodifiableMap(extendedMap));
    }
}
