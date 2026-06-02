package n.paradox.aslib.fabric;

import n.paradox.aslib.AsLib;
import net.fabricmc.api.ModInitializer;

public class ASLibFabricInitializer implements ModInitializer {
    private static final AsLib MAININSTANCE = new AsLib();
    public static AsLib getInstance() {
        return MAININSTANCE;
    }
    @Override
    public void onInitialize() {
        MAININSTANCE.init();
    }
}
