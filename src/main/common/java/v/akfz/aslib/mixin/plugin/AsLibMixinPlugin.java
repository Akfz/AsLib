package v.akfz.aslib.mixin.plugin;

import v.akfz.aslib.initializer.LoaderEnvironment;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

//В forge mixin нету разделение client и server, поэтому рекомендуется использовать этот плагин,
//теперь mixin.client - клиент, а mixin.server - сервер. (без .client или .server - общее)
// МОЖНО НЕ ИСПОЛЬЗОВАТЬ
public class AsLibMixinPlugin implements IMixinConfigPlugin {
    private boolean isClientEnvironment = false;

    @Override
    public void onLoad(String mixinPackage) {
        LoaderEnvironment.InitLoader();
        LoaderEnvironment.Loader loader = LoaderEnvironment.getCurrentLoader();

        String modName = "AsLib";
        if (mixinPackage != null) {
            String[] parts = mixinPackage.split("\\.");
            if (parts.length >= 3) {
                String rawName = parts[2];
                modName = Character.toUpperCase(rawName.charAt(0)) + rawName.substring(1);
            }
        }
        String logPrefix = "[" + modName + " MixinPlugin]";

        if (loader == LoaderEnvironment.Loader.FORGE || loader == LoaderEnvironment.Loader.NEOFORGE) {
            try {
                Class<?> fmlEnvClass = Class.forName("net.minecraftforge.fml.loading.FMLEnvironment");
                java.lang.reflect.Field distField = fmlEnvClass.getField("dist");
                isClientEnvironment = distField.get(null).toString().equals("CLIENT");

                logOncePerMod(modName, logPrefix + " Current Forge FMLEnvironment. Client is : " + isClientEnvironment);
                return;
            } catch (Exception ignored) {}
        }

        try {
            Class<?> fabricLoaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object fabricLoaderInstance = fabricLoaderClass.getMethod("getInstance").invoke(null);
            Object environmentType = fabricLoaderClass.getMethod("getEnvironmentType").invoke(fabricLoaderInstance);

            isClientEnvironment = environmentType.toString().equals("CLIENT");

            logOncePerMod(modName, logPrefix + " Current FabricLoader API. Client is : " + isClientEnvironment);
            return;
        } catch (Exception ignored) {}

        String classLoaderName = this.getClass().getClassLoader().getClass().getName();
        isClientEnvironment = classLoaderName.contains("KnotClient") || classLoaderName.contains("Client");

        logOncePerMod(modName, logPrefix + " Current ClassLoader: " + classLoaderName + ". Client is : " + isClientEnvironment);
    }

    private void logOncePerMod(String modId, String message) {
        String key = "aslib.mixin.logged." + modId.toLowerCase();
        if (System.getProperty(key) == null) {
            System.setProperty(key, "true");
            System.out.println(message);
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String lowerClassName = mixinClassName.toLowerCase();

        if (lowerClassName.contains("client")) {
            return isClientEnvironment;
        }

        if (lowerClassName.contains("server")) {
            return !isClientEnvironment;
        }

        return true;
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}