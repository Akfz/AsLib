package v.akfz.aslib.datagen.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.datagen.api.Serializable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class LangData implements Serializable<JsonElement> {
	private final String namespace;
	private final String languageCode;
	private final Map<String, String> translations = new LinkedHashMap<>();

	public LangData(String namespace, String languageCode) {
		this.namespace = namespace;
		this.languageCode = languageCode;
	}

	public LangData(String namespace) {
		this(namespace, "en_us");
	}

	public LangData add(String key, String value) {
		if (key != null && value != null) {
			this.translations.put(key, value);
		}
		return this;
	}

	public LangData addAll(Map<String, String> entries) {
		if (entries != null) {
			this.translations.putAll(entries);
		}
		return this;
	}

	public LangData addEntity(String entityName, String translation) {
		return add("entity." + namespace + "." + entityName, translation);
	}

	public LangData addItem(String itemName, String translation) {
		return add("item." + namespace + "." + itemName, translation);
	}

	public LangData addBlock(String blockName, String translation) {
		return add("block." + namespace + "." + blockName, translation);
	}

	public LangData addCreativeTab(String tabName, String translation) {
		return add("itemGroup." + namespace + "." + tabName, translation);
	}

	public LangData addAdvancement(String advancementName, String title, String description) {
		add("advancements." + namespace + "." + advancementName + ".title", title);
		return add("advancements." + namespace + "." + advancementName + ".description", description);
	}

	public LangData addTooltip(String key, String translation) {
		return add("tooltip." + namespace + "." + key, translation);
	}

	public LangData addGui(String guiName, String key, String translation) {
		return add("gui." + namespace + "." + guiName + "." + key, translation);
	}

	public LangData addConfig(String key, String translation) {
		return add("config." + namespace + "." + key, translation);
	}

	public String getNamespace() {
		return namespace;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public Map<String, String> getTranslations() {
		return Collections.unmodifiableMap(translations);
	}

	@Override
	public @Nullable ResourceLocation getRLPath() {
		return new ResourceLocation(namespace, "lang/" + languageCode);
	}

	@Override
	public @Nullable Path getPath() {
		return null;
	}

	@Override
	public boolean isAsset() {
		return true;
	}

	@Override
	public String getExtension() {
		return "json";
	}

	@Override
	public boolean isSystem() {
		return false;
	}

	@Override
	public JsonElement serialize() {
		JsonObject root = new JsonObject();

		TreeMap<String, String> sortedTranslations = new TreeMap<>(translations);

		for (Map.Entry<String, String> entry : sortedTranslations.entrySet()) {
			root.addProperty(entry.getKey(), entry.getValue());
		}

		return root;
	}
}