package v.akfz.aslib.initializer;

//quilt currently not supported.
public final class LoaderEnvironment {
    public enum Loader {
        FORGE,
        FABRIC,
        NEOFORGE,
        UNKNOWN;

        public boolean isForgeLike() {
            return this == Loader.FORGE || this == Loader.NEOFORGE;
        }
    }

    private static Loader currentLoader;
    public static Loader getCurrentLoader() {
        return currentLoader;
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

        if (hasClass("net.fabricmc.loader.api.FabricLoader")) {
            currentLoader = Loader.FABRIC;
            return;
        }

        currentLoader = Loader.UNKNOWN;
        System.err.println("ASLib - LoaderEnvironment : Unknown mod loader detected!");
    }

    public static Loader getFastLoader() {
        if (hasClass("net.neoforged.fml.loading.FMLLoader") || hasClass("net.neoforged.loading.FMLLoader")) {
            return Loader.NEOFORGE;
        }

        if (hasClass("net.minecraftforge.fml.loading.FMLLoader")) {
            return Loader.FORGE;
        }

        if (hasClass("net.fabricmc.loader.api.FabricLoader")) {
            return Loader.FABRIC;
        }

        return Loader.UNKNOWN;
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
