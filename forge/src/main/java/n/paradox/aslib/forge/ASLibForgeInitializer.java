package n.paradox.aslib.forge;

import n.paradox.aslib.AsLib;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod("aslib")
public class ASLibForgeInitializer {
    private static final AsLib MAININSTANCE = new AsLib();

    public static AsLib getInstance() {
        return MAININSTANCE;
    }

    public ASLibForgeInitializer() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    }

    private void init(FMLCommonSetupEvent event) {
        MAININSTANCE.startInit();
    }
}