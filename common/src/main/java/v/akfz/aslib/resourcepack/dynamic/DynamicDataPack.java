package v.akfz.aslib.resourcepack.dynamic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.resourcepack.AddResourcePack;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicDataPack implements PackResources {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final DynamicDataPack INSTANCE = new DynamicDataPack();
	private static final Set<PackRepository> REGISTERED_REPOS = ConcurrentHashMap.newKeySet();

	private final Map<ResourceLocation, byte[]> clientResources = new ConcurrentHashMap<>();
	private final Map<ResourceLocation, byte[]> serverResources = new ConcurrentHashMap<>();
	private final Set<String> clientNamespaces = ConcurrentHashMap.newKeySet();
	private final Set<String> serverNamespaces = ConcurrentHashMap.newKeySet();

	private DynamicDataPack() {}

	public static DynamicDataPack getInstance() {
		return INSTANCE;
	}

	public static void addData(ResourceLocation location, JsonElement json) {
		byte[] bytes = GSON.toJson(json).getBytes(StandardCharsets.UTF_8);
		INSTANCE.serverResources.put(location, bytes);
		INSTANCE.serverNamespaces.add(location.getNamespace());
	}

	public static void addResource(ResourceLocation location, byte[] data) {
		INSTANCE.clientResources.put(location, data);
		INSTANCE.clientNamespaces.add(location.getNamespace());
	}

	public static void registerToRepository(PackRepository repository) {
		if (REGISTERED_REPOS.add(repository)) {
			AddResourcePack.add(
					repository,
					INSTANCE,
					Component.literal("ASLib Dynamic Resources"),
					"aslib_dynamic_resources",
					Component.literal("ASLib Dynamic Resources"),
					true,
					Pack.Position.TOP,
					true,
					PackSource.BUILT_IN,
					PackType.CLIENT_RESOURCES
			);

			AddResourcePack.addServerData(
					repository,
					INSTANCE,
					Component.literal("ASLib Dynamic DataPack"),
					"aslib_dynamic_datapack",
					Component.literal("Dynamic DataPack"),
					true,
					Pack.Position.TOP,
					true,
					PackSource.BUILT_IN
			);
		}
	}

	@Override
	public @Nullable IoSupplier<InputStream> getRootResource(String... strings) {
		String pathStr = String.join("/", strings);

		if ("pack.mcmeta".equals(pathStr)) {
			int format = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
			String json = "{\"pack\":{\"description\":\"ASLib Dynamic Pack\",\"pack_format\":" + format + "}}";
			return () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		}
		return null;
	}

	@Override
	public @Nullable IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
		Map<ResourceLocation, byte[]> resources =
				packType == PackType.CLIENT_RESOURCES ? clientResources : serverResources;

		byte[] data = resources.get(location);
		if (data != null) {
			return () -> new ByteArrayInputStream(data);
		}
		return null;
	}

	@Override
	public void listResources(PackType packType, String namespace, String prefix, ResourceOutput resourceOutput) {
		Map<ResourceLocation, byte[]> resources =
				packType == PackType.CLIENT_RESOURCES ? clientResources : serverResources;

		for (Map.Entry<ResourceLocation, byte[]> entry : resources.entrySet()) {
			ResourceLocation loc = entry.getKey();
			if (loc.getNamespace().equals(namespace) && loc.getPath().startsWith(prefix)) {
				byte[] bytes = entry.getValue();
				resourceOutput.accept(loc, () -> new ByteArrayInputStream(bytes));
			}
		}
	}

	@Override
	public @NotNull Set<String> getNamespaces(PackType packType) {
		return packType == PackType.CLIENT_RESOURCES ? clientNamespaces : serverNamespaces;
	}

	@Override
	public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) {
		return null;
	}

	@Override
	public String packId() {
		return "aslib_dynamic_pack";
	}

	@Override
	public void close() {
		clientResources.clear();
		serverResources.clear();
	}
}