package n.paradox.aslib;

import n.paradox.aslib.initializer.ASModInitializer;
import n.paradox.aslib.test.ResourcePackTest;
import net.minecraft.client.Minecraft;

public final class AsLib extends ASModInitializer {
    @Override
    public void startInit() {
        //TODO remove + fix
        ResourcePackTest.initializeTestPack(Minecraft.getInstance().getResourcePackRepository(),Minecraft.getInstance().gameDirectory.toPath()); //TODO remove
        System.out.println("ASLIB : START INIT");
    }
}