package n.paradox.aslib.initializer;

//quilt currently not supported.
public class LoaderEnvironment {
    public enum Loader {
        FORGE,
        FABRIC,
        NEOFORGE,
        QUILT,
        UNKNOWN
    }

    private static Loader currentLoader;
    public static Loader getCurrentLoader() {
        return currentLoader;
    }

    public static boolean isFabricLike() {
        Loader loader = getCurrentLoader();
        return loader == Loader.FABRIC || loader == Loader.QUILT;
    }

    public static boolean isForgeLike() {
        Loader loader = getCurrentLoader();
        return loader == Loader.FORGE || loader == Loader.NEOFORGE;
    }

    public static synchronized void InitLoader() {
        if (currentLoader != null) return;

        if (hasClass("net.neoforged.fml.loading.FMLLoader") || hasClass("net.neoforged.loading.FMLLoader")) {
            currentLoader = Loader.NEOFORGE;
            return;
        }

        if (hasClass("net.minecraftforge.fml.loading.FMLLoader")) {
            currentLoader = Loader.FORGE;
            return;
        }

        if (hasClass("org.quiltmc.loader.api.QuiltLoader")) {
            currentLoader = Loader.QUILT;
            return;
        }

        if (hasClass("net.fabricmc.loader.api.FabricLoader")) {
            currentLoader = Loader.FABRIC;
            return;
        }

        currentLoader = Loader.UNKNOWN;
        System.err.println("ASLib - LoaderEnvironment : Unknown mod loader detected!");
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className, false, LoaderEnvironment.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
