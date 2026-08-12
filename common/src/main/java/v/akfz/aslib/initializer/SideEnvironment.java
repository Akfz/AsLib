package v.akfz.aslib.initializer;

import net.minecraft.client.Minecraft;

//Клиент или сервер
public class SideEnvironment {
    public enum Side {
        Client,
        Server
    }

    private static final Side currentSide;

    static {
        currentSide = isClient() ? Side.Client : Side.Server;
    }

    private static boolean isClient() {
        try {
            return Minecraft.getInstance() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static Side getCurrentSide() {
        return currentSide;
    }
}
