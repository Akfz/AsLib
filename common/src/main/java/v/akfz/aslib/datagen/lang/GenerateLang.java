package v.akfz.aslib.datagen.lang;

import v.akfz.aslib.datagen.api.DataProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenerateLang extends DataProvider {

	private final List<LangData> langDataList = new ArrayList<>();

	public GenerateLang() {
	}

	public GenerateLang addLang(LangData langData) {
		if (langData != null) {
			this.langDataList.add(langData);
		}
		return this;
	}

	public GenerateLang addLangs(LangData... langDataArray) {
		if (langDataArray != null) {
			this.langDataList.addAll(Arrays.asList(langDataArray));
		}
		return this;
	}

	@Override
	protected void registerDataSerializable() {
		for (LangData langData : langDataList) {
			add(langData);
		}
	}
}