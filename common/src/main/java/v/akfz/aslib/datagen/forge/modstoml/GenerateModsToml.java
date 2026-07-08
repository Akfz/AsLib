package v.akfz.aslib.datagen.forge.modstoml;

import v.akfz.aslib.datagen.api.DataProvider;

public class GenerateModsToml extends DataProvider {

    private final ModsTomlData data;
    public GenerateModsToml(ModsTomlData data) {
        this.data = data;
    }

    @Override
    protected void registerDataSerializable() {
        add(data);
    }
}