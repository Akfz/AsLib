package v.akfz.aslib.datagen.forge.packmcmeta;

import v.akfz.aslib.datagen.api.DataProvider;

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