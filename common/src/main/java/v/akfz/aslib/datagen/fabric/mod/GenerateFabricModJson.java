package v.akfz.aslib.datagen.fabric.mod;

import v.akfz.aslib.datagen.api.DataProvider;

// некоторые данные (миксин, класс) нужно дописывать вручную
public class GenerateFabricModJson extends DataProvider {

    private final FabricModJsonData data;
    public GenerateFabricModJson(FabricModJsonData data) {
        this.data = data;
    }

    @Override
    protected void registerDataSerializable() {
        add(data);
    }
}
