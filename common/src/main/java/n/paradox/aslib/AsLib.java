package n.paradox.aslib;

import n.paradox.aslib.initializer.ASModInitializer;
import n.paradox.aslib.initializer.LoaderEnvironment;
import n.paradox.aslib.register.AsLibRegistries;
import n.paradox.aslib.test.ReactionGameCommand;

public final class AsLib extends ASModInitializer {
    @Override
    public void startInit() {
        //ResourcePackTest.initializeTestPack(Minecraft.getInstance().getResourcePackRepository(),Minecraft.getInstance().gameDirectory.toPath());

        AsLibRegistries.getCommandRegistry().addCommand("aslib_testcom", new ReactionGameCommand());
        System.out.println("AsLib currentLoader : " + LoaderEnvironment.getCurrentLoader());
    }

    @Override
    public void Init() {
        AsLibRegistries.Init();
    }
}