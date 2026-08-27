package v.akfz.aslib.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TranslateUtils {
	private TranslateUtils() {}

	public static String getString(String key, Object... args) {
		return Component.translatable(key, args).getString();
	}

	public static MutableComponent getComponent(String key, Object... args) {
		return Component.translatable(key, args);
	}

	public static String buildKey(String modId, String category, String name) {
		return modId + "." + category + "." + name;
	}

	public static boolean hasTranslation(String key) {
		String translated = Component.translatable(key).getString();
		return !translated.equals(key);
	}
}