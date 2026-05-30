package n.paradox.aslib.loader;

public final class PlatformInitializer {
    private static final Platform currentPlatform;

    static {
        if (hasClass("net.neoforged.fml.loading.FMLLoader")) {
            currentPlatform = Platform.NEOFORGE;
        } else if (hasClass("net.minecraftforge.fml.loading.FMLLoader")) {
            currentPlatform = Platform.FORGE;
        } else if (hasClass("org.quiltmc.loader.api.QuiltLoader")) {
            currentPlatform = Platform.QUILT;
        } else if (hasClass("net.fabricmc.loader.api.FabricLoader")) {
            currentPlatform = Platform.FABRIC;
        } else {
            currentPlatform = Platform.UNBEKNOWN;
        }
    }

    public static Platform getCurrentPlatform() {
        return currentPlatform;
    }

    public static boolean isForgeLike() {
        return currentPlatform == Platform.FORGE || currentPlatform == Platform.NEOFORGE;
    }

    public static boolean isFabricLike() {
        return currentPlatform == Platform.FABRIC || currentPlatform == Platform.QUILT;
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className, false, PlatformInitializer.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
