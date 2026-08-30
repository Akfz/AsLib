package v.akfz.aslib.resourcepack.configpack;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.resourcepack.AddResourcePack;
import v.akfz.aslib.resourcepack.SimpleFileResourcePack;
import v.akfz.aslib.resourcepack.configpack.preview.PreviewConfig;
import v.akfz.aslib.util.GlobalUtils;
import v.akfz.aslib.util.json.GsonHelper;
import v.akfz.aslib.util.json.JsonFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ConfigPack {
	private ConfigPack() {}

	private static boolean isLoaded = false;
	private static final List<LoadedConfigPack> LOADED_PACKS = new ArrayList<>();
	private static final Map<PackRepository, Set<String>> REGISTERED_CLIENT_PACKS = new ConcurrentHashMap<>();
	private static final Map<PackRepository, Set<String>> REGISTERED_SERVER_PACKS = new ConcurrentHashMap<>();

	public static synchronized void Init() {
		if (isLoaded) return;
		Path startPath = GlobalUtils.getAsLibCFGPath().resolve("cfgPack");
		try {
			if (!Files.exists(startPath)) {
				Files.createDirectories(startPath);
			}
			createPreview(startPath);
			findAndLoadPacks(startPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		isLoaded = true;
	}

	public static void registerToRepository(PackRepository repo) {
		registerToRepository(repo, null);
	}

	public static void registerToRepository(PackRepository repo, @Nullable PackType filterType) {
		Set<String> registeredClient = REGISTERED_CLIENT_PACKS.computeIfAbsent(repo, k -> ConcurrentHashMap.newKeySet());
		Set<String> registeredServer = REGISTERED_SERVER_PACKS.computeIfAbsent(repo, k -> ConcurrentHashMap.newKeySet());

		Init();
		for (LoadedConfigPack packHolder : LOADED_PACKS) {
			ConfigPackData data = packHolder.data();
			SimpleFileResourcePack resourcePack = packHolder.resourcePack();
			String disc = String.join("\n", data.description);
			String packId = data.id;
			String target = data.packTarget != null ? data.packTarget.toLowerCase() : "both";

			if (GlobalUtils.isClientSide() && (target.equals("client") || target.equals("both"))) {
				if (filterType == null || filterType == PackType.CLIENT_RESOURCES) {
					if (registeredClient.add(packId)) {
						AddResourcePack.addFRP(repo, resourcePack, Component.literal(disc),
								data.alwaysEnabled, data.position, data.pinned, PackSource.BUILT_IN, PackType.CLIENT_RESOURCES);
					}
				}
			}

			if (target.equals("server") || target.equals("both")) {
				if (filterType == null || filterType == PackType.SERVER_DATA) {
					if (registeredServer.add(packId)) {
						AddResourcePack.addFRP(repo, resourcePack, Component.literal(disc),
								data.alwaysEnabled, data.position, data.pinned, PackSource.BUILT_IN, PackType.SERVER_DATA);
					}
				}
			}
		}
	}

	private static void findAndLoadPacks(Path startPath) {
		try (Stream<Path> stream = Files.list(startPath)) {
			stream.filter(path -> path.toFile().isDirectory()).forEach(path -> {
				try {
					if (path.getFileName().toString().equals("preview")) return;
					loadPackData(path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadPackData(Path path) throws IOException {
		Path json = null;
		Path rpDir = null;
		try (Stream<Path> stream = Files.list(path)) {
			List<Path> files = stream.toList();
			for (Path file : files) {
				String name = file.getFileName().toString();
				if (name.endsWith(".json")) {
					json = file;
				} else if (Files.isDirectory(file) && name.equals("resourcePack")) {
					rpDir = file;
				}
			}
		}

		if (json == null || rpDir == null) return;

		ConfigPackData data = GsonHelper.read(json, ConfigPackData.class);
		if (data == null) return;

		SimpleFileResourcePack resourcePack = ConfigPackRegistry.create(data.type, rpDir, data);
		LOADED_PACKS.add(new LoadedConfigPack(data, resourcePack));
	}

	private static void createPreview(Path startPath) {
		File dirPreview = startPath.resolve("preview").toFile();
		if (!dirPreview.exists()) {
			if (dirPreview.mkdir()) {
				GsonHelper.write(new JsonFile<PreviewConfig>() {
					@Override
					public PreviewConfig data() {
						return new PreviewConfig();
					}

					@Override
					public Path getPath() {
						return startPath.resolve("preview").resolve("howtocreatecfgpack.json");
					}
				});

				File rpDir = startPath.resolve("preview").resolve("resourcePack").toFile();
				if (!rpDir.exists()) {
					rpDir.mkdir();
				}
			}
		}
	}

	private record LoadedConfigPack(ConfigPackData data, SimpleFileResourcePack resourcePack) {}
}