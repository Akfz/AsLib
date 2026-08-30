package v.akfz.aslib.datagen.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.datagen.api.Serializable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Generates a mixin config file (*.mixins.json).
 * <p>
 * Defaults:
 * <ul>
 *   <li>file name: <code>${mixin_name}.json</code> from gradle.properties</li>
 *   <li>refmap: <code>${mixin_name}.refmap.json</code></li>
 *   <li>required: true</li>
 *   <li>minVersion: "0.8"</li>
 *   <li>compatibilityLevel: "JAVA_17"</li>
 *   <li>injectors.defaultRequire: 1</li>
 * </ul>
 */
public class MixinConfigData implements Serializable<JsonElement> {

	private String configName;
	private boolean required = true;
	private String minVersion = "0.8";
	private String packageName;
	private String compatibilityLevel = "JAVA_17";
	private String plugin;
	private String refmap;
	private boolean refmapExplicitlyDisabled = false;

	private final List<String> mixins = new ArrayList<>();
	private final List<String> client = new ArrayList<>();
	private final List<String> server = new ArrayList<>();
	private final Set<String> injectors = new LinkedHashSet<>();
	private int defaultRequire = 1;

	public MixinConfigData() {
		Properties props = loadGradleProperties();
		this.configName = props.getProperty("mixin_name", props.getProperty("mod_id", "mod") + ".mixins");
		this.refmap = this.configName + ".refmap.json";
	}

	public MixinConfigData(String configName) {
		this.configName = configName;
		this.refmap = configName + ".refmap.json";
	}

	private static Properties loadGradleProperties() {
		Properties props = new Properties();
		File currentDir = new File(System.getProperty("user.dir"));
		File propsFile = null;
		while (currentDir != null) {
			File checkFile = new File(currentDir, "gradle.properties");
			if (checkFile.exists()) {
				propsFile = checkFile;
				break;
			}
			currentDir = currentDir.getParentFile();
		}
		if (propsFile != null && propsFile.exists()) {
			try (FileInputStream fis = new FileInputStream(propsFile)) {
				props.load(fis);
			} catch (IOException e) {
				System.err.println("[ASLib-DataGen] Failed to read gradle.properties: " + e.getMessage());
			}
		}
		return props;
	}

	public MixinConfigData configName(String configName) {
		this.configName = configName;
		if (this.refmap != null && !this.refmapExplicitlyDisabled) {
			this.refmap = configName + ".refmap.json";
		}
		return this;
	}

	public MixinConfigData required(boolean required) {
		this.required = required;
		return this;
	}

	public MixinConfigData minVersion(String minVersion) {
		this.minVersion = minVersion;
		return this;
	}

	public MixinConfigData packageName(String packageName) {
		this.packageName = packageName;
		return this;
	}

	public MixinConfigData compatibilityLevel(String compatibilityLevel) {
		this.compatibilityLevel = compatibilityLevel;
		return this;
	}

	public MixinConfigData plugin(String plugin) {
		this.plugin = plugin;
		return this;
	}

	public MixinConfigData addAsLibPlugin() {
		this.plugin = "v.akfz.aslib.mixin.plugin.AsLibMixinPlugin";
		return this;
	}

	public MixinConfigData refmap(String refmap) {
		this.refmap = refmap;
		this.refmapExplicitlyDisabled = (refmap == null || refmap.isEmpty());
		return this;
	}

	public MixinConfigData noRefmap() {
		this.refmap = null;
		this.refmapExplicitlyDisabled = true;
		return this;
	}

	public MixinConfigData defaultRequire(int defaultRequire) {
		this.defaultRequire = defaultRequire;
		return this;
	}

	public MixinConfigData injectorOption(String key) {
		if (key != null && !key.isEmpty()) {
			this.injectors.add(key);
		}
		return this;
	}

	public MixinConfigData addMixin(String mixin) {
		if (mixin != null && !mixin.isEmpty() && !this.mixins.contains(mixin)) {
			this.mixins.add(mixin);
		}
		return this;
	}

	public MixinConfigData addMixins(String... mixins) {
		if (mixins != null) {
			for (String m : mixins) addMixin(m);
		}
		return this;
	}

	public MixinConfigData addClientMixin(String mixin) {
		if (mixin != null && !mixin.isEmpty() && !this.client.contains(mixin)) {
			this.client.add(mixin);
		}
		return this;
	}

	public MixinConfigData addClientMixins(String... mixins) {
		if (mixins != null) {
			for (String m : mixins) addClientMixin(m);
		}
		return this;
	}

	public MixinConfigData addServerMixin(String mixin) {
		if (mixin != null && !mixin.isEmpty() && !this.server.contains(mixin)) {
			this.server.add(mixin);
		}
		return this;
	}

	public MixinConfigData addServerMixins(String... mixins) {
		if (mixins != null) {
			for (String m : mixins) addServerMixin(m);
		}
		return this;
	}

	public String getConfigName() { return configName; }
	public boolean isRequired() { return required; }
	public String getMinVersion() { return minVersion; }
	public String getPackageName() { return packageName; }
	public String getCompatibilityLevel() { return compatibilityLevel; }
	public String getPlugin() { return plugin; }
	public String getRefmap() { return refmap; }
	public List<String> getMixins() { return Collections.unmodifiableList(mixins); }
	public List<String> getClient() { return Collections.unmodifiableList(client); }
	public List<String> getServer() { return Collections.unmodifiableList(server); }
	public int getDefaultRequire() { return defaultRequire; }

	@Override
	public @Nullable ResourceLocation getRLPath() {
		return null;
	}

	@Override
	public Path getPath() {
		return Path.of(configName + ".json");
	}

	@Override
	public boolean isSystem() {
		return true;
	}

	@Override
	public String getExtension() {
		return "json";
	}

	@Override
	public JsonElement serialize() {
		JsonObject root = new JsonObject();
		root.addProperty("required", required);
		if (minVersion != null && !minVersion.isEmpty()) {
			root.addProperty("minVersion", minVersion);
		}
		if (packageName != null && !packageName.isEmpty()) {
			root.addProperty("package", packageName);
		}
		if (compatibilityLevel != null && !compatibilityLevel.isEmpty()) {
			root.addProperty("compatibilityLevel", compatibilityLevel);
		}
		if (plugin != null && !plugin.isEmpty()) {
			root.addProperty("plugin", plugin);
		}
		if (refmap != null && !refmap.isEmpty()) {
			root.addProperty("refmap", refmap);
		}

		if (!mixins.isEmpty()) {
			root.add("mixins", toJsonArray(mixins));
		}
		if (!client.isEmpty()) {
			root.add("client", toJsonArray(client));
		}
		if (!server.isEmpty()) {
			root.add("server", toJsonArray(server));
		}

		JsonObject injectorsJson = new JsonObject();
		injectorsJson.addProperty("defaultRequire", defaultRequire);
		for (String opt : injectors) {
			injectorsJson.addProperty(opt, true);
		}
		root.add("injectors", injectorsJson);

		return root;
	}

	private static com.google.gson.JsonArray toJsonArray(List<String> list) {
		com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
		for (String s : list) arr.add(s);
		return arr;
	}
}