package v.akfz.aslib.resourcepack;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.world.flag.FeatureFlagSet;

import java.lang.reflect.Field;
import java.util.*;

public final class AddResourcePack {
	private AddResourcePack() {}

	public static void add(PackRepository manager, PackResources pack, Component description,
	                       String id, Component displayName, boolean alwaysEnabled,
	                       Pack.Position pos, boolean pinned, PackSource source) {
		add(manager, pack, description, id, displayName, alwaysEnabled, pos, pinned, source, PackType.CLIENT_RESOURCES);
	}

	public static void addFRP(PackRepository manager, FileResourcePack frp, Component description,
	                          boolean alwaysEnabled, Pack.Position pos,
	                          boolean pinned, PackSource source) {
		addFRP(manager, frp, description, alwaysEnabled, pos, pinned, source, PackType.CLIENT_RESOURCES);
	}

	public static void add(PackRepository manager, PackResources pack, Component description,
	                       String id, Component displayName, boolean alwaysEnabled,
	                       Pack.Position pos, boolean pinned, PackSource source, PackType packType) {
		registerInternal(manager, id, displayName, alwaysEnabled, pos, pinned, source, description, name -> pack, packType);
	}

	public static void addFRP(PackRepository manager, FileResourcePack frp, Component description,
	                          boolean alwaysEnabled, Pack.Position pos,
	                          boolean pinned, PackSource source, PackType packType) {
		registerInternal(manager, frp.getSimpleNamespace(), Component.literal(frp.getPack().packId()),
				alwaysEnabled, pos, pinned, source, description, name -> frp.getPack(), packType);
	}

	public static void addServerData(PackRepository manager, PackResources pack, Component description,
	                                 String id, Component displayName, boolean alwaysEnabled,
	                                 Pack.Position pos, boolean pinned, PackSource source) {
		add(manager, pack, description, id, displayName, alwaysEnabled, pos, pinned, source, PackType.SERVER_DATA);
	}

	public static void addServerDataFRP(PackRepository manager, FileResourcePack frp, Component description,
	                                    boolean alwaysEnabled, Pack.Position pos,
	                                    boolean pinned, PackSource source) {
		addFRP(manager, frp, description, alwaysEnabled, pos, pinned, source, PackType.SERVER_DATA);
	}

	private static void registerInternal(PackRepository manager, String id, Component name,
	                                     boolean alwaysEnabled, Pack.Position pos,
	                                     boolean pinned, PackSource source, Component description,
	                                     Pack.ResourcesSupplier factory, PackType packType) {

		int currentFormat = SharedConstants.getCurrentVersion().getPackVersion(packType);
		Pack.Info metadata = new Pack.Info(description, currentFormat, FeatureFlagSet.of());

		PackSource finalSource = alwaysEnabled ? new PackSource() {
			@Override
			public Component decorate(Component packName) {
				return source.decorate(packName);
			}
			@Override
			public boolean shouldAddAutomatically() {
				return true;
			}
		} : source;

		boolean isFixed = alwaysEnabled || pinned;

		RepositorySource repositorySource = profileAdder -> {
			Pack profile = Pack.create(
					id, name, alwaysEnabled, factory, metadata, packType, pos, isFixed, finalSource
			);
			if (profile != null) {
				profileAdder.accept(profile);
			}
		};

		if (manager instanceof ResourcePackExpander expander) {
			expander.addProvider(repositorySource);
		} else {
			addProviderViaReflection(manager, repositorySource);
		}
	}

	@SuppressWarnings("unchecked")
	private static void addProviderViaReflection(PackRepository manager, RepositorySource provider) {
		try {
			Field sourcesField = null;
			try {
				sourcesField = PackRepository.class.getDeclaredField("sources");
			} catch (NoSuchFieldException e) {
				try {
					sourcesField = PackRepository.class.getDeclaredField("providers");
				} catch (NoSuchFieldException e2) {
					System.err.println("[ASLib] Could not find 'sources' or 'providers' field in PackRepository");
					return;
				}
			}
			sourcesField.setAccessible(true);
			Object sourcesObj = sourcesField.get(manager);

			if (sourcesObj instanceof Set) {
				Set<RepositorySource> sources = (Set<RepositorySource>) sourcesObj;
				try {
					sources.add(provider);
				} catch (UnsupportedOperationException e) {
					Set<RepositorySource> mutableSources = new LinkedHashSet<>(sources);
					mutableSources.add(provider);
					sourcesField.set(manager, mutableSources);
				}
			} else if (sourcesObj instanceof List) {
				List<RepositorySource> sources = (List<RepositorySource>) sourcesObj;
				try {
					sources.add(provider);
				} catch (UnsupportedOperationException e) {
					List<RepositorySource> mutableSources = new ArrayList<>(sources);
					mutableSources.add(provider);
					sourcesField.set(manager, mutableSources);
				}
			}
		} catch (Exception e) {
			System.err.println("[ASLib] Failed to add RepositorySource via reflection: " + e.getMessage());
			e.printStackTrace();
		}
	}
}