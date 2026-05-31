package n.paradox.aslib.datagen.forge.packmcmeta;

import n.paradox.aslib.datagen.api.DataProvider;

public class GeneratePackMcmeta extends DataProvider {

    private final PackMcmetaData data;
    public GeneratePackMcmeta(PackMcmetaData data) {
        this.data = data;
    }

    @Override
    protected void registerDataSerializable() {
        add(data);
    }
}