package v.akfz.aslib.resourcepack;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;
import v.akfz.aslib.initializer.LoaderEnvironment;
import v.akfz.aslib.util.GlobalUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>WARNING, IT'S ONLY FOR FABRIC, DOESN'T HAVE EFFECT ON FORGE</b>
 * <p>
 * Well, if your Fabric loader doesn't load mod resources, use this shit, or just install Fabric API (I guess).
 * <p>
 * It forcefully shoves your mod's assets into the PackRepository via reflection.
 * Pure black magic. You're welcome.
 * <p>
 * All mods are merged into a single {@code ModResources} pack, so we don't spam
 * one entry per mod in the pack list. The description is the list of included mods.
 * <p>
 * On late registration (a mod calls {@link #registerModAssets} after the pack was
 * already installed), the source is torn down and rebuilt so the description stays
 * in sync. Vanilla rebuilds the pack list on the next {@code discoverAvailable}.
 */
public final class ModAssetsRegistrar {

	private static final String PACK_ID = "aslib_modresources";
	private static final String PACK_DISPLAY_NAME = "ModResources";

	private static final Set<String> PENDING_MODS = ConcurrentHashMap.newKeySet();
	private static final Map<PackRepository, Set<String>> REGISTERED_MODS = new ConcurrentHashMap<>();
	private static final Map<PackRepository, CompositeFileResourcePack> COMPOSITES = new ConcurrentHashMap<>();
	private static final Map<PackRepository, RepositorySource> ACTIVE_SOURCES = new ConcurrentHashMap<>();

	public static void registerModAssets(String modId) {
		PENDING_MODS.add(modId);
		PackRepository repo = getClientRepoSafely();
		if (repo != null) {
			flush(repo);
		}
	}

	public static void flush(PackRepository repo) {
		if (repo == null || PENDING_MODS.isEmpty()) return;
		if (LoaderEnvironment.getCurrentLoader().isForgeLike()) return;
		if (!GlobalUtils.isClientSide()) return;

		PackRepository clientRepo = getClientRepoSafely();
		if (clientRepo == null || clientRepo != repo) return;

		Set<String> registered = REGISTERED_MODS.computeIfAbsent(repo, k -> ConcurrentHashMap.newKeySet());

		List<SimpleFileResourcePack> newPacks = new ArrayList<>();
		for (String modId : new ArrayList<>(PENDING_MODS)) {
			if (PENDING_MODS.remove(modId) && registered.add(modId)) {
				try {
					Path root = getModRootPathViaReflection(modId);
					if (root == null) continue;
					newPacks.add(new SimpleFileResourcePack(modId, root, modId));
				} catch (Exception e) {
					System.err.println("[ASLib] Failed to load mod assets for " + modId + ": " + e.getMessage());
				}
			}
		}
		if (newPacks.isEmpty()) return;

		CompositeFileResourcePack composite = COMPOSITES.get(repo);
		if (composite == null) {
			composite = new CompositeFileResourcePack(PACK_DISPLAY_NAME, newPacks);
			COMPOSITES.put(repo, composite);
			installSource(repo, composite);
		} else {
			for (SimpleFileResourcePack p : newPacks) composite.addDelegate(p);
			reinstallSource(repo, composite);
		}
	}

	private static void installSource(PackRepository repo, CompositeFileResourcePack composite) {
		RepositorySource source = createSource(composite);
		ACTIVE_SOURCES.put(repo, source);
		if (repo instanceof ResourcePackExpander expander) {
			expander.addProvider(source);
		} else {
			System.err.println("[ASLib] PackRepository is not a ResourcePackExpander — mixin may be missing!");
		}
	}

	private static void reinstallSource(PackRepository repo, CompositeFileResourcePack composite) {
		RepositorySource old = ACTIVE_SOURCES.remove(repo);
		if (old != null) {
			if (repo instanceof ResourcePackExpander expander) {
				expander.removeProvider(old);
			} else {
				System.err.println("[ASLib] PackRepository is not a ResourcePackExpander — mixin may be missing!");
			}
		}
		RepositorySource fresh = createSource(composite);
		ACTIVE_SOURCES.put(repo, fresh);
		if (repo instanceof ResourcePackExpander expander) {
			expander.addProvider(fresh);
		} else {
			System.err.println("[ASLib] PackRepository is not a ResourcePackExpander — mixin may be missing!");
		}
	}

	private static RepositorySource createSource(CompositeFileResourcePack composite) {
		int format = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
		List<String> mods = composite.getIncludedMods();
		Component description = Component.literal("Included: " + String.join(", ", mods));
		Pack.Info info = new Pack.Info(description, format, FeatureFlagSet.of());

		PackSource forcedBuiltin = new PackSource() {
			@Override
			public Component decorate(Component packName) {
				return PackSource.BUILT_IN.decorate(packName);
			}

			@Override
			public boolean shouldAddAutomatically() {
				return true;
			}
		};

		return adder -> {
			Pack pack = Pack.create(
					PACK_ID,
					Component.literal(PACK_DISPLAY_NAME),
					true,
					name -> composite,
					info,
					PackType.CLIENT_RESOURCES,
					Pack.Position.TOP,
					true,
					forcedBuiltin
			);
			if (pack != null) adder.accept(pack);
		};
	}

	private static PackRepository getClientRepoSafely() {
		try {
			Minecraft mc = Minecraft.getInstance();
			return mc != null ? mc.getResourcePackRepository() : null;
		} catch (Throwable t) {
			return null;
		}
	}

	private static Path getModRootPathViaReflection(String modId) throws Exception {
		Class<?> loaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
		Object loader = loaderClass.getMethod("getInstance").invoke(null);
		Optional<?> containerOpt = (Optional<?>) loaderClass.getMethod("getModContainer", String.class).invoke(loader, modId);
		if (containerOpt.isEmpty()) return null;
		Object container = containerOpt.get();
		return (Path) container.getClass().getMethod("getRootPath").invoke(container);
	}
}