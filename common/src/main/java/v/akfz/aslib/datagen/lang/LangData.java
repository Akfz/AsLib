package v.akfz.aslib.datagen.lang;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import v.akfz.aslib.datagen.api.Serializable;

import java.nio.file.Path;
import java.util.*;

/**
 * Data for generating .lang files (Minecraft localization)
 */
public class LangData implements Serializable<String> {

	private final String namespace;
	private final String languageCode;
	private final Map<String, String> translations = new LinkedHashMap<>();
	private final List<String> header = new ArrayList<>();

	public LangData(String namespace, String languageCode) {
		this.namespace = namespace;
		this.languageCode = languageCode;
	}

	public LangData(String namespace) {
		this(namespace, "en_us");
	}

	public LangData add(String key, String value) {
		this.translations.put(key, value);
		return this;
	}

	public LangData addAll(Map<String, String> entries) {
		this.translations.putAll(entries);
		return this;
	}

	public LangData addHeaderComment(String comment) {
		this.header.add(comment);
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
		return "lang";
	}

	@Override
	public boolean isSystem() {
		return false;
	}

	@Override
	public String serialize() {
		StringBuilder sb = new StringBuilder();

		if (!header.isEmpty()) {
			for (String comment : header) {
				sb.append(comment).append("\n");
			}
			sb.append("\n");
		}

		TreeMap<String, String> sortedTranslations = new TreeMap<>(translations);

		for (Map.Entry<String, String> entry : sortedTranslations.entrySet()) {
			sb.append(entry.getKey())
					.append("=")
					.append(entry.getValue())
					.append("\n");
		}

		return sb.toString();
	}
}