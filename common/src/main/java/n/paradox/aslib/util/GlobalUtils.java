package n.paradox.aslib.util;

import net.minecraft.client.Minecraft;

public final class GlobalUtils {
    public static boolean isClientSide() {
        try {
            return Minecraft.getInstance() != null;
        } catch (Exception e) { //в теории можно заменить на RuntimeException
            return false;
        }
    }

    public static boolean isClientHost() {
        if (isClientSide()) {
            return Minecraft.getInstance().hasSingleplayerServer();
        }
        return false;
    }

    //наверное работает :)
    public static boolean isDevEnvironment() {
        String classPath = System.getProperty("java.class.path", "");
        return classPath.contains(".gradle/caches") || classPath.contains("build/classes");
    }
}
