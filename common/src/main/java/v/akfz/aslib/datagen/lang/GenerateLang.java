package v.akfz.aslib.datagen.lang;

import v.akfz.aslib.datagen.api.DataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data generator for .lang files
 *
 * Example usage:
 * <pre>
 * new GenerateLang()
 *     .addLang(new LangData("mymod", "en_us")
 *         .addEntity("testentity", "Test Entity")
 *         .addItem("testitem", "Test Item")
 *         .addBlock("testblock", "Test Block")
 *         .addCreativeTab("main", "My Mod")
 *         .add("key.custom", "Custom Translation"))
 *     .addLang(new LangData("mymod", "ru_ru")
 *         .addEntity("testentity", "Тестовая сущность")
 *         .addItem("testitem", "Тестовый предмет")
 *         .addBlock("testblock", "Тестовый блок")
 *         .addCreativeTab("main", "Мой мод"))
 *     .run("common");
 * </pre>
 */
public class GenerateLang extends DataProvider {

	private final List<LangData> langDataList = new ArrayList<>();

	public GenerateLang() {
	}

	public GenerateLang addLang(LangData langData) {
		this.langDataList.add(langData);
		return this;
	}

	public GenerateLang addLangs(LangData... langDataArray) {
		this.langDataList.addAll(Arrays.asList(langDataArray));
		return this;
	}

	@Override
	protected void registerDataSerializable() {
		for (LangData langData : langDataList) {
			add(langData);
		}
	}
}