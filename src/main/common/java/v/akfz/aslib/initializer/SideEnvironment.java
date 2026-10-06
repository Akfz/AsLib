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
        return present("net.minecraft.client.Minecraft")
                || present("net.minecraft.class_310")
                || present("net.minecraft.client.main.Main");
    }

    private static boolean present(String name) {
        try { Class.forName(name, false, SideEnvironment.class.getClassLoader()); return true; }
        catch (Throwable t) { return false; }
    }

    public static Side getCurrentSide() {
        return currentSide;
    }
}