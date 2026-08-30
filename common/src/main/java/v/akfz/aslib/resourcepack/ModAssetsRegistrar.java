package v.akfz.aslib.resourcepack;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import v.akfz.aslib.initializer.LoaderEnvironment;
import v.akfz.aslib.util.GlobalUtils;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Well, if your Fabric loader doesn't load mod resources, use this shit, or just install Fabric API (I guess).
 *
 * It forcefully shoves your mod's assets into the PackRepository via reflection.
 * Pure black magic. You're welcome.
 */
public final class ModAssetsRegistrar {

	private static final Set<String> PENDING_MODS = ConcurrentHashMap.newKeySet();
	private static final Set<String> REGISTERED_MODS = ConcurrentHashMap.newKeySet();

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

		for (String modId : new ArrayList<>(PENDING_MODS)) {
			if (PENDING_MODS.remove(modId) && REGISTERED_MODS.add(modId)) {
				registerInternal(repo, modId);
			}
		}
	}

	private static void registerInternal(PackRepository repo, String modId) {
		try {
			Path root = getModRootPathViaReflection(modId);
			if (root == null) return;
			SimpleFileResourcePack pack = new SimpleFileResourcePack(modId, root, modId);
			AddResourcePack.addFRP(repo, pack,
					Component.literal("Mod Assets: " + modId),
					true, Pack.Position.TOP, true,
					PackSource.BUILT_IN, PackType.CLIENT_RESOURCES);
		} catch (Exception e) {
			System.err.println("[ASLib] Failed to register mod assets for " + modId + ": " + e.getMessage());
		}
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