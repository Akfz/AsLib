package v.akfz.aslib.util;

import net.minecraft.client.Minecraft;
import v.akfz.aslib.initializer.SideEnvironment;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class GlobalUtils {
    private static Boolean cachedDevEnvironment = null;
    public static Path getAsLibCFGPath() {
        return Paths.get("").toAbsolutePath().resolve("AsLib");
    }

    public static boolean isClientSide() {
        return SideEnvironment.getCurrentSide().equals(SideEnvironment.Side.Client);
    }

    public static boolean isClientHost() {
        if (isClientSide()) {
            return Minecraft.getInstance().hasSingleplayerServer();
        }
        return false;
    }

    //наверное работает :)
    public static boolean isDevEnvironment() {
        if (cachedDevEnvironment != null) {
            return cachedDevEnvironment;
        }

        cachedDevEnvironment = detectDevEnvironment();
        return cachedDevEnvironment;
    }

    private static boolean detectDevEnvironment() {
        try {
            Class<?> fabricLoaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = fabricLoaderClass.getMethod("getInstance").invoke(null);
            return (boolean) fabricLoaderClass.getMethod("isDevelopmentEnvironment").invoke(instance);
        } catch (Throwable ignored) {}
        try {
            Class<?> fmlLoaderClass = Class.forName("net.minecraftforge.fml.loading.FMLLoader");
            return !(boolean) fmlLoaderClass.getMethod("isProduction").invoke(null);
        } catch (Throwable ignored) {}
        try {
            Class<?> neoLoaderClass = Class.forName("net.neoforged.fml.loading.FMLLoader");
            return !(boolean) neoLoaderClass.getMethod("isProduction").invoke(null);
        } catch (Throwable ignored) {}

        if ("true".equalsIgnoreCase(System.getProperty("fabric.development"))) {
            return true;
        }
        if ("false".equalsIgnoreCase(System.getProperty("fml.isProduction"))) {
            return true;
        }

        String classPath = System.getProperty("java.class.path", "");
        return classPath.contains(".gradle")
                || classPath.contains("build/classes")
                || classPath.contains("build\\classes")
                || classPath.contains("idea_rt.jar")
                || classPath.contains("eclipse")
                || classPath.contains("bin/main");
    }
}
