package v.akfz.aslib.resourcepack;

import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Glues several {@link SimpleFileResourcePack}s into one PackResources,
 * so mod assets show up as a single entry in the resource pack list.
 * <p>
 * Lookup walks the delegates in order: first non-null wins.
 * Namespaces and listings are just the union of all delegates.
 */
public class CompositeFileResourcePack implements PackResources, FileResourcePack {

	private final String packName;
	private final List<SimpleFileResourcePack> delegates = new ArrayList<>();
	private final Set<String> clientNamespaces = ConcurrentHashMap.newKeySet();
	private final Set<String> serverNamespaces = ConcurrentHashMap.newKeySet();

	public CompositeFileResourcePack(String packName, List<SimpleFileResourcePack> initial) {
		this.packName = packName;
		if (initial != null) {
			for (SimpleFileResourcePack d : initial) addDelegate(d);
		}
	}

	public synchronized void addDelegate(SimpleFileResourcePack delegate) {
		if (delegate == null) return;
		delegates.add(delegate);
		clientNamespaces.addAll(delegate.getNamespaces(PackType.CLIENT_RESOURCES));
		serverNamespaces.addAll(delegate.getNamespaces(PackType.SERVER_DATA));
	}

	public synchronized List<String> getIncludedMods() {
		List<String> result = new ArrayList<>(delegates.size());
		for (SimpleFileResourcePack d : delegates) result.add(d.getSimpleNamespace());
		return result;
	}

	public String getPackName() {
		return packName;
	}

	@Override
	public String getSimpleNamespace() {
		return "aslib_modresources";
	}

	@Override
	public void refreshCache() {
		synchronized (this) {
			for (SimpleFileResourcePack d : delegates) d.refreshCache();
		}
	}

	@Override
	public PackResources getPack() {
		return this;
	}

	@Override
	public @Nullable IoSupplier<InputStream> getRootResource(String... strings) {
		String path = String.join("/", strings);
		if ("pack.mcmeta".equals(path)) {
			int format = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
			String json = "{\"pack\":{\"description\":\"" + packName + "\",\"pack_format\":" + format + "}}";
			return () -> new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
		}
		synchronized (this) {
			for (SimpleFileResourcePack d : delegates) {
				IoSupplier<InputStream> s = d.getRootResource(strings);
				if (s != null) return s;
			}
		}
		return null;
	}

	@Override
	public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation loc) {
		synchronized (this) {
			for (SimpleFileResourcePack d : delegates) {
				IoSupplier<InputStream> s = d.getResource(type, loc);
				if (s != null) return s;
			}
		}
		return null;
	}

	@Override
	public void listResources(PackType type, String namespace, String prefix, ResourceOutput out) {
		synchronized (this) {
			for (SimpleFileResourcePack d : delegates) {
				d.listResources(type, namespace, prefix, out);
			}
		}
	}

	@Override
	public @NotNull Set<String> getNamespaces(PackType type) {
		return type == PackType.CLIENT_RESOURCES ? clientNamespaces : serverNamespaces;
	}

	@Override
	public @Nullable <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
		return null;
	}

	@Override
	public String packId() {
		return packName;
	}

	@Override
	public void close() {
		synchronized (this) {
			for (SimpleFileResourcePack d : delegates) d.close();
			delegates.clear();
			clientNamespaces.clear();
			serverNamespaces.clear();
		}
	}
}