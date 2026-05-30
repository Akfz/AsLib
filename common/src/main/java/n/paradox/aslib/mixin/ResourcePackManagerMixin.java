package n.paradox.aslib.mixin;

import n.paradox.aslib.resourcepack.ResourcePackExpander;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(ResourcePackManager.class)
public class ResourcePackManagerMixin implements ResourcePackExpander {
    @Unique private final Set<ResourcePackProvider> additionalProviders = new HashSet<>();

    @Override
    public void addProvider(ResourcePackProvider provider) {
        this.additionalProviders.add(provider);
    }

    @Inject(method = "providePackProfiles", at = @At("RETURN"), cancellable = true)
    private void aslib$injectAdditionalProviders(CallbackInfoReturnable<Map<String, ResourcePackProfile>> cir) {
        Map<String, ResourcePackProfile> originalMap = cir.getReturnValue();
        Map<String, ResourcePackProfile> extendedMap = new java.util.TreeMap<>();

        extendedMap.putAll(originalMap);

        for (ResourcePackProvider provider : this.additionalProviders) {
            provider.register(profile -> {
                extendedMap.put(profile.getName(), profile);
            });
        }

        cir.setReturnValue(Collections.unmodifiableMap(extendedMap));
    }
}
