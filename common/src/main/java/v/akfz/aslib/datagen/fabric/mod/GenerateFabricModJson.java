package v.akfz.aslib.datagen.fabric.mod;

import v.akfz.aslib.datagen.api.DataProvider;

/**
 * Some data you need to write yourself (like mixins or loader class)
 */
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
