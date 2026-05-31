package n.paradox.aslib.datagen.forge.modstoml;

import n.paradox.aslib.datagen.api.DataProvider;

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