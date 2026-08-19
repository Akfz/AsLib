package v.akfz.aslib.initializer;

/**
 * Client or Server 🤕
 */
public class SideEnvironment {
    public enum Side {
        Client,
        Server
    }

    private static final Side currentSide;

    static {
        currentSide = isClientAvailable() ? Side.Client : Side.Server;
    }

    private static boolean isClientAvailable() {
        try {
            Class.forName("net.minecraft.client.Minecraft", false, SideEnvironment.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Side getCurrentSide() {
        return currentSide;
    }
}