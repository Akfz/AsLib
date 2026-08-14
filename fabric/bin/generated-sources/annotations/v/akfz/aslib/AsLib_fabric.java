package v.akfz.aslib;

import v.akfz.aslib.AsLib;

public class AsLib_fabric implements net.fabricmc.api.ModInitializer {
    private static final AsLib MAININSTANCE = new AsLib();

    public static AsLib getInstance() {
        return MAININSTANCE;
    }

    @Override
    public void onInitialize() {
        MAININSTANCE.init();
    }
}
