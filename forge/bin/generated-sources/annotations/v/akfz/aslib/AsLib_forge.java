package v.akfz.aslib;

import v.akfz.aslib.AsLib;
import net.minecraftforge.fml.common.Mod;

@Mod("aslib")
public class AsLib_forge {
    private static final AsLib MAININSTANCE = new AsLib();

    public static AsLib getInstance() {
        return MAININSTANCE;
    }

    public AsLib_forge() {
        MAININSTANCE.init();
    }
}
