package v.akfz.aslib.mixin;

import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import v.akfz.aslib.resourcepack.ModAssetsRegistrar;
import v.akfz.aslib.resourcepack.ResourcePackExpander;
import v.akfz.aslib.resourcepack.configpack.ConfigPack;
import v.akfz.aslib.resourcepack.dynamic.DynamicDataPack;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

@Mixin(PackRepository.class)
public class ResourcePackManagerMixin implements ResourcePackExpander {

	@Unique private static Field aslib$sourcesField = null;
	@Unique private static boolean aslib$searchDone = false;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void aslib$makeSourcesMutable(CallbackInfo ci) {
		try {
			Field f = aslib$findSourcesField();
			if (f == null) return;
			Object current = f.get(this);
			if (current instanceof Collection<?> col && !(current instanceof LinkedHashSet)) {
				f.set(this, new LinkedHashSet<RepositorySource>((Collection<RepositorySource>) col));
			}
		} catch (Throwable t) {
			System.err.println("[ASLib] Failed to make PackRepository sources mutable");
			t.printStackTrace();
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
		try {
			Field f = aslib$findSourcesField();
			if (f == null) return;
			Object current = f.get(this);
			if (current instanceof Collection<?> col) {
				Collection<RepositorySource> sources = (Collection<RepositorySource>) col;
				if (!sources.contains(provider)) sources.add(provider);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void removeProvider(RepositorySource provider) {
		try {
			Field f = aslib$findSourcesField();
			if (f == null) return;
			Object current = f.get(this);
			if (current instanceof Collection<?> col) {
				col.remove(provider);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Unique
	private Field aslib$findSourcesField() {
		if (aslib$searchDone) return aslib$sourcesField;
		aslib$searchDone = true;
		for (Field f : PackRepository.class.getDeclaredFields()) {
			if (!Collection.class.isAssignableFrom(f.getType())) continue;
			try {
				f.setAccessible(true);
				Object val = f.get(this);
				if (!(val instanceof Collection<?> col)) continue;

				if (!col.isEmpty()) {
					if (col.iterator().next() instanceof RepositorySource) {
						aslib$sourcesField = f;
						return f;
					}
				} else {
					String generic = f.getGenericType().getTypeName();
					String name = f.getName();
					if (generic.contains("RepositorySource")
							|| name.equals("sources") || name.equals("providers")) {
						aslib$sourcesField = f;
						return f;
					}
				}
			} catch (Throwable ignored) {}
		}
		return aslib$sourcesField;
	}
}