package v.akfz.aslib.resourcepack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import v.akfz.aslib.initializer.LoaderEnvironment;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Well, if your fabric loader doesnt load modres, you ts, or install fabric api (i guess)
 *
 * It forcefully shoves your mod's assets into the PackRepository via reflection.
 * Pure black magic. You're welcome.
 */
public final class ModAssetsRegistrar {

	public static void registerModAssets(PackRepository repo, String modId) {
		if (LoaderEnvironment.getCurrentLoader().isForgeLike()) return;
		try {
			Path root = getModRootPathViaReflection(modId);
			if (root == null) return;
			registerInternal(repo, modId, root);
		} catch (Exception e) {
			System.err.println("[ASLib] Failed to register mod assets for " + modId + ": " + e.getMessage());
		}
	}

	private static void registerInternal(PackRepository repo, String modId, Path root) {
		SimpleFileResourcePack pack = new SimpleFileResourcePack(modId, root, modId);
		AddResourcePack.add(repo, pack,
				Component.literal("Mod Assets: " + modId),
				"aslib_mod_assets_" + modId,
				Component.literal("Mod Assets: " + modId),
				true, Pack.Position.BOTTOM, true,
				PackSource.BUILT_IN, PackType.CLIENT_RESOURCES);
	}

	@SuppressWarnings("unchecked")
	private static Path getModRootPathViaReflection(String modId) throws Exception {
		Class<?> loaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
		Method getInstanceMethod = loaderClass.getMethod("getInstance");
		Object loader = getInstanceMethod.invoke(null);
		Method getModContainerMethod = loaderClass.getMethod("getModContainer", String.class);
		Optional<?> containerOpt = (Optional<?>) getModContainerMethod.invoke(loader, modId);
		if (containerOpt.isEmpty()) return null;
		Object container = containerOpt.get();
		Method getRootPathMethod = container.getClass().getMethod("getRootPath");
		return (Path) getRootPathMethod.invoke(container);
	}
}